<script setup lang="ts">
import { computed, h, onMounted, ref } from 'vue'
import { type DataTableColumns } from 'naive-ui'
import { api, type Measurement } from '@/api/client'
import { mapBackendPlantToPlant } from '@/api/mappers'
import { OFFLINE_CONFIG, STATUS_CONFIG } from '@/components/plants/plantDisplay'
import type { Plant, PlantStatus } from '@/types/Plant'

defineOptions({ name: 'VitalsView' })

type Range = '24h' | '7d' | '30d' | '90d'
type Metric = 'moisture' | 'temp'
type Severity = 'critical' | 'needs_attention' | 'offline'

interface AttentionRow {
  id: number
  severity: Severity
  plant: string
  issue: string
  room: string
  last_reading: string
}

interface HeatmapRow {
  plant: string
  data: { moisture: (number | null)[]; temp: (number | null)[] }
}

interface TrendRow {
  plant: string
  color: string
  moisture: number[]
  temp: number[]
}

interface DeviceRow {
  id: number
  device: string
  plant: string
  online: boolean
  firmware: string
  last_seen: string
}

const plants = ref<Plant[]>([])
const measurementsByPlant = ref<Record<number, Measurement[]>>({})
const loading = ref(false)
const error = ref<string | null>(null)
const range = ref<Range>('7d')
const heatmapMetric = ref<Metric>('moisture')

const rangeOptions: { label: string; value: Range }[] = [
  { label: '24h', value: '24h' },
  { label: '7d', value: '7d' },
  { label: '30d', value: '30d' },
  { label: '90d', value: '90d' },
]
const metricOptions: { label: string; value: Metric }[] = [
  { label: 'Moisture', value: 'moisture' },
  { label: 'Temp', value: 'temp' },
]

const rangeToCells: Record<Range, number> = { '24h': 24, '7d': 7, '30d': 30, '90d': 90 }
const rangeToMs: Record<Range, number> = {
  '24h': 24 * 60 * 60 * 1000,
  '7d': 7 * 24 * 60 * 60 * 1000,
  '30d': 30 * 24 * 60 * 60 * 1000,
  '90d': 90 * 24 * 60 * 60 * 1000,
}

const summary = computed(() => {
  const statusCounts = plants.value.reduce(
    (counts, plant) => {
      counts[plant.readings.status] += 1
      return counts
    },
    { healthy: 0, needs_attention: 0, critical: 0 } satisfies Record<PlantStatus, number>,
  )

  return {
    plants_total: plants.value.length,
    healthy: statusCounts.healthy,
    needs_attention: statusCounts.needs_attention,
    critical: statusCounts.critical,
    devices_online: plants.value.filter((plant) => plant.device.online).length,
    devices_total: plants.value.length,
  }
})

function relativeTime(value: string | null): string {
  if (!value) return 'never'

  const diffMinutes = Math.max(0, Math.round((Date.now() - new Date(value).getTime()) / 60000))
  if (diffMinutes < 1) return 'just now'
  if (diffMinutes < 60) return `${diffMinutes} min ago`

  const diffHours = Math.round(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours} h ago`

  const diffDays = Math.round(diffHours / 24)
  return `${diffDays} d ago`
}

function attentionDotColor(severity: Severity): string {
  return severity === 'offline' ? OFFLINE_CONFIG.color : STATUS_CONFIG[severity].color
}

function attentionIssue(plant: Plant, severity: Severity): string {
  if (severity === 'offline') return 'Device offline'

  const moisture = Math.round(plant.readings.moisture)
  const threshold = plant.moistureThreshold ?? plant.settings.thresholds?.moisture_min

  if (threshold != null && moisture < threshold) {
    return `Soil moisture ${moisture}% below ${threshold}% target`
  }

  return STATUS_CONFIG[plant.readings.status].label
}

const attentionRows = computed<AttentionRow[]>(() =>
  plants.value
    .map((plant) => {
      const severity: Severity | null = !plant.device.online
        ? 'offline'
        : plant.readings.status === 'healthy'
          ? null
          : plant.readings.status

      if (!severity) return null

      return {
        id: plant.id,
        severity,
        plant: plant.custom_name,
        issue: attentionIssue(plant, severity),
        room: plant.room,
        last_reading: relativeTime(plant.device.last_seen),
      }
    })
    .filter((row): row is AttentionRow => row !== null),
)

const attentionColumns: DataTableColumns<AttentionRow> = [
  {
    title: 'Plant',
    key: 'plant',
    render: (row) =>
      h('span', { style: { display: 'inline-flex', alignItems: 'center', gap: '0.5rem' } }, [
        h('span', {
          style: {
            display: 'inline-block',
            width: '0.625rem',
            height: '0.625rem',
            borderRadius: '50%',
            backgroundColor: attentionDotColor(row.severity),
          },
        }),
        row.plant,
      ]),
  },
  { title: 'Issue', key: 'issue' },
  { title: 'Room', key: 'room' },
  { title: 'Last reading', key: 'last_reading' },
]

function readingsForRange(plantId: number): Measurement[] {
  const cutoff = Date.now() - rangeToMs[range.value]
  return (measurementsByPlant.value[plantId] ?? [])
    .filter((measurement) => new Date(measurement.measuredAt).getTime() >= cutoff)
    .sort((a, b) => new Date(a.measuredAt).getTime() - new Date(b.measuredAt).getTime())
}

function bucketReadings<T>(
  readings: Measurement[],
  cells: number,
  valueFor: (measurement: Measurement) => T | null,
): (T | null)[] {
  const cutoff = Date.now() - rangeToMs[range.value]
  const bucketMs = rangeToMs[range.value] / cells

  return Array.from({ length: cells }, (_, index) => {
    const start = cutoff + index * bucketMs
    const end = start + bucketMs
    const bucket = readings.filter((reading) => {
      const time = new Date(reading.measuredAt).getTime()
      return time >= start && time < end
    })
    const values = bucket.map(valueFor).filter((value): value is T => value !== null)

    if (values.length === 0) return null
    return values[Math.floor(values.length / 2)] ?? null
  })
}

const heatmapData = computed<HeatmapRow[]>(() =>
  plants.value.map((plant) => {
    const readings = readingsForRange(plant.id)
    const cells = rangeToCells[range.value]
    const thresholds = plant.settings.thresholds
    const moistureMin = thresholds?.moisture_min ?? 40
    const moistureMax = thresholds?.moisture_max ?? 100
    const tempMin = thresholds?.temp_min ?? 18
    const tempMax = thresholds?.temp_max ?? 30
    const moistureBuckets = bucketReadings(readings, cells, (reading) => reading.soilMoisture)
    const tempBuckets = bucketReadings(readings, cells, (reading) => reading.temperature)

    return {
      plant: plant.custom_name,
      data: {
        moisture: moistureBuckets.map((value) =>
          value === null ? null : value >= moistureMin && value <= moistureMax ? 100 : 0,
        ),
        temp: tempBuckets.map((value) =>
          value === null ? null : value >= tempMin && value <= tempMax ? 100 : 0,
        ),
      },
    }
  }),
)

const visibleHeatmap = computed(() =>
  heatmapData.value.map((row) => ({
    plant: row.plant,
    cells: row.data[heatmapMetric.value],
  })),
)

const cellSize = computed(() => {
  const n = rangeToCells[range.value]
  return Math.max(6, Math.min(30, Math.floor(420 / n)))
})

const METRIC_RGB: Record<Metric, string> = {
  moisture: '59, 130, 246',
  temp: '249, 115, 22',
}
const shades = computed<[string, string, string, string, string]>(() => {
  const rgb = METRIC_RGB[heatmapMetric.value]
  return [
    `rgba(${rgb}, 0.18)`,
    `rgba(${rgb}, 0.35)`,
    `rgba(${rgb}, 0.55)`,
    `rgba(${rgb}, 0.75)`,
    `rgba(${rgb}, 1)`,
  ]
})
function cellColor(value: number | null): string {
  if (value === null) return 'var(--neutral-400)'
  const s = shades.value
  if (value >= 80) return s[4]
  if (value >= 60) return s[3]
  if (value >= 40) return s[2]
  if (value >= 20) return s[1]
  return s[0]
}

const dayLabels = computed<string[]>(() => {
  if (range.value !== '7d') return []
  const out: string[] = []
  const today = new Date()
  for (let i = 6; i >= 0; i--) {
    const d = new Date(today)
    d.setDate(d.getDate() - i)
    out.push(d.toLocaleDateString('en-US', { weekday: 'short' }))
  }
  return out
})
function cellTitle(value: number | null): string {
  return value === null ? 'No reading' : `${value}% in band`
}
function rowAverage(cells: (number | null)[]): string {
  const valid = cells.filter((cell): cell is number => cell !== null)
  if (valid.length === 0) return '- no data'
  return `${Math.round(valid.reduce((a, b) => a + b, 0) / valid.length)}%`
}

const TREND_COLORS = ['#3b82f6', '#22c55e', '#f97316', '#a855f7', '#14b8a6', '#ef4444']
const trendsData = computed<TrendRow[]>(() =>
  plants.value.map((plant, index) => {
    const readings = readingsForRange(plant.id)
    return {
      plant: plant.custom_name,
      color: TREND_COLORS[index % TREND_COLORS.length] ?? '#3b82f6',
      moisture: readings
        .map((reading) => reading.soilMoisture)
        .filter((value): value is number => value !== null),
      temp: readings
        .map((reading) => reading.temperature)
        .filter((value): value is number => value !== null),
    }
  }),
)

const moistureYTicks = [100, 50, 0]
const tempYTicks = [35, 22, 10]
const X_AXIS_LABELS: Record<Range, string[]> = {
  '24h': ['24h ago', '12h ago', 'now'],
  '7d': ['7d ago', '3d ago', 'now'],
  '30d': ['30d ago', '15d ago', 'now'],
  '90d': ['90d ago', '45d ago', 'now'],
}
const xAxisLabels = computed<string[]>(() => X_AXIS_LABELS[range.value])

const TRENDS_W = 600
const TRENDS_H = 120

function trendPath(values: number[], dMin: number, dMax: number): string {
  if (values.length === 0) return ''

  const last = values.length - 1 || 1
  return values
    .map((value, index) => {
      const x = (index / last) * TRENDS_W
      const clamped = Math.max(dMin, Math.min(dMax, value))
      const y = TRENDS_H - ((clamped - dMin) / (dMax - dMin)) * TRENDS_H
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}

const visibleTrends = computed(() =>
  trendsData.value.map((trend) => ({
    plant: trend.plant,
    color: trend.color,
    moistureLine: trendPath(trend.moisture, 0, 100),
    tempLine: trendPath(trend.temp, 10, 35),
  })),
)

const hasTrendData = computed(() =>
  visibleTrends.value.some((trend) => trend.moistureLine.length > 0 || trend.tempLine.length > 0),
)

const deviceRows = computed<DeviceRow[]>(() =>
  plants.value.map((plant) => ({
    id: plant.id,
    device: plant.device.name,
    plant: plant.custom_name,
    online: plant.device.online,
    firmware: plant.device.firmware ?? '-',
    last_seen: relativeTime(plant.device.last_seen),
  })),
)

const deviceColumns: DataTableColumns<DeviceRow> = [
  { title: 'Device', key: 'device' },
  { title: 'Plant', key: 'plant' },
  {
    title: 'Status',
    key: 'online',
    render: (row) =>
      h('span', { style: { display: 'inline-flex', alignItems: 'center', gap: '0.5rem' } }, [
        h('span', {
          style: {
            display: 'inline-block',
            width: '0.5rem',
            height: '0.5rem',
            borderRadius: '50%',
            backgroundColor: row.online ? 'var(--success)' : 'var(--danger)',
          },
        }),
        row.online ? 'Online' : 'Offline',
      ]),
  },
  { title: 'Firmware', key: 'firmware' },
  { title: 'Last seen', key: 'last_seen' },
]

async function loadVitals() {
  loading.value = true
  error.value = null

  try {
    const backendPlants = await api.getPlants()
    const mappedPlants = await Promise.all(
      backendPlants.map(async (backendPlant) => {
        const latestMeasurement = await api.getLatestMeasurement(backendPlant.id).catch(() => null)
        return mapBackendPlantToPlant(backendPlant, latestMeasurement)
      }),
    )

    plants.value = mappedPlants

    const measurementEntries = await Promise.all(
      mappedPlants.map(async (plant) => {
        const readings = await api.getMeasurements(plant.id).catch(() => [])
        return [plant.id, readings] as const
      }),
    )

    measurementsByPlant.value = Object.fromEntries(measurementEntries)
  } catch (err) {
    console.error(err)
    error.value = 'Could not load vitals from the backend.'
  } finally {
    loading.value = false
  }
}

onMounted(loadVitals)
</script>

<template>
  <section class="vitals-view">
    <div class="view-header">
      <h1>Vitals</h1>
    </div>
    <n-radio-group v-model:value="range" class="range-control" size="small">
      <n-radio-button
        v-for="o in rangeOptions"
        :key="o.value"
        class="rangeOptions"
        :value="o.value"
      >
        {{ o.label }}
      </n-radio-button>
    </n-radio-group>
    <div class="vitals-content">
      <n-alert v-if="error" type="error" :bordered="false">
        {{ error }}
      </n-alert>

      <n-spin v-else :show="loading">
        <n-divider title-placement="left">Fleet Status</n-divider>
        <div class="stat-strip">
          <n-statistic label="Plants" :value="summary.plants_total" />
          <n-statistic label="Healthy" :value="summary.healthy" />
          <n-statistic label="Needs attention" :value="summary.needs_attention" />
          <n-statistic label="Critical" :value="summary.critical" />
          <n-statistic label="Devices online">
            {{ summary.devices_online }}<span class="muted">/{{ summary.devices_total }}</span>
          </n-statistic>
        </div>

        <div class="section-row">
          <n-divider title-placement="left" class="section-row__divider">
            Needs Attention now
          </n-divider>
        </div>
        <n-data-table
          v-if="attentionRows.length > 0"
          :columns="attentionColumns"
          :data="attentionRows"
          size="small"
          :pagination="false"
        />
        <n-empty v-else description="No plants need attention." size="small" />

        <div class="section-row">
          <n-divider title-placement="left" class="section-row__divider">
            Time in band ({{ range }})
          </n-divider>
          <n-text depth="3">Metric:</n-text>
          <n-radio-group v-model:value="heatmapMetric" size="small">
            <n-radio v-for="o in metricOptions" :key="o.value" :value="o.value">
              {{ o.label }}
            </n-radio>
          </n-radio-group>
        </div>

        <div class="heatmap">
          <div v-if="range === '7d'" class="heatmap-row heatmap-row--header">
            <div class="heatmap-row__label"></div>
            <div class="heatmap-row__cells">
              <div
                v-for="(label, i) in dayLabels"
                :key="i"
                class="heatmap-cell-label"
                :style="{ width: cellSize + 'px' }"
              >
                {{ label }}
              </div>
            </div>
            <div class="heatmap-row__total">In band</div>
          </div>
          <div v-for="row in visibleHeatmap" :key="row.plant" class="heatmap-row">
            <div class="heatmap-row__label">{{ row.plant }}</div>
            <div class="heatmap-row__cells">
              <div
                v-for="(cell, i) in row.cells"
                :key="i"
                class="heatmap-cell"
                :style="{
                  width: cellSize + 'px',
                  height: cellSize + 'px',
                  background: cellColor(cell),
                }"
                :title="cellTitle(cell)"
              />
            </div>
            <div class="heatmap-row__total">{{ rowAverage(row.cells) }}</div>
          </div>
          <n-empty
            v-if="visibleHeatmap.length === 0"
            description="No plant readings yet."
            size="small"
          />
          <div class="heatmap-legend">
            <span class="heatmap-legend__title">% time in band:</span>
            <span class="heatmap-legend__item">
              <span class="heatmap-legend__swatch" :style="{ background: shades[0] }"></span>
              0-19
            </span>
            <span class="heatmap-legend__item">
              <span class="heatmap-legend__swatch" :style="{ background: shades[1] }"></span>
              20-39
            </span>
            <span class="heatmap-legend__item">
              <span class="heatmap-legend__swatch" :style="{ background: shades[2] }"></span>
              40-59
            </span>
            <span class="heatmap-legend__item">
              <span class="heatmap-legend__swatch" :style="{ background: shades[3] }"></span>
              60-79
            </span>
            <span class="heatmap-legend__item">
              <span class="heatmap-legend__swatch" :style="{ background: shades[4] }"></span>
              80-100
            </span>
            <span class="heatmap-legend__item heatmap-legend__item--offline">
              <span class="heatmap-legend__swatch" style="background: var(--neutral-400)"></span>
              No reading
            </span>
          </div>
        </div>

        <n-divider title-placement="left">Trends across the fleet</n-divider>
        <div v-if="hasTrendData">
          <div class="trends-legend">
            <span v-for="trend in trendsData" :key="trend.plant" class="trends-legend__item">
              <span class="trends-legend__dot" :style="{ background: trend.color }"></span>
              {{ trend.plant }}
            </span>
          </div>
          <div class="trend-chart">
            <div class="trend-chart__title">Moisture (%)</div>
            <div class="trend-chart__body">
              <div class="trend-chart__y-axis">
                <span v-for="tick in moistureYTicks" :key="tick">{{ tick }}</span>
              </div>
              <div class="trend-chart__plot">
                <svg viewBox="0 0 600 120" preserveAspectRatio="none" class="trend-chart__svg">
                  <polyline
                    v-for="trend in visibleTrends"
                    :key="trend.plant"
                    :points="trend.moistureLine"
                    fill="none"
                    :stroke="trend.color"
                    stroke-width="1.5"
                    vector-effect="non-scaling-stroke"
                  />
                </svg>
                <div class="trend-chart__x-axis">
                  <span v-for="(label, i) in xAxisLabels" :key="i">{{ label }}</span>
                </div>
              </div>
            </div>
          </div>
          <div class="trend-chart">
            <div class="trend-chart__title">Temperature (deg C)</div>
            <div class="trend-chart__body">
              <div class="trend-chart__y-axis">
                <span v-for="tick in tempYTicks" :key="tick">{{ tick }}</span>
              </div>
              <div class="trend-chart__plot">
                <svg viewBox="0 0 600 120" preserveAspectRatio="none" class="trend-chart__svg">
                  <polyline
                    v-for="trend in visibleTrends"
                    :key="trend.plant"
                    :points="trend.tempLine"
                    fill="none"
                    :stroke="trend.color"
                    stroke-width="1.5"
                    vector-effect="non-scaling-stroke"
                  />
                </svg>
                <div class="trend-chart__x-axis">
                  <span v-for="(label, i) in xAxisLabels" :key="i">{{ label }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <n-empty v-else description="No measurement history for this range." size="small" />

        <n-divider title-placement="left">Devices</n-divider>
        <n-data-table
          :columns="deviceColumns"
          :data="deviceRows"
          size="small"
          :pagination="false"
        />
      </n-spin>
    </div>
  </section>
</template>

<style scoped>
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3rem;
}

.range-control {
  position: sticky;
  top: 0.5rem;
  z-index: 20;
  display: flex;
  justify-content: flex-end;
  margin-top: -5rem;
  margin-bottom: 2rem;
  pointer-events: none;
}
.range-control :deep(.n-radio-button) {
  pointer-events: auto;
}

.rangeOptions {
  min-width: 3rem;
  text-align: center;
}
.stat-strip {
  display: grid;
  grid-template-columns: repeat(5, 1fr);
  gap: 0.75rem;
}
.stat-strip > .n-statistic {
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
}

.muted {
  font-size: 0.75rem;
  color: var(--color-text-muted);
}

.section-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.section-row__divider {
  flex: 1;
}

.heatmap {
  margin-top: 0.75rem;
}
.heatmap-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.25rem;
  font-size: 0.875rem;
}
.heatmap-row__label {
  flex: 0 0 8rem;
  color: var(--color-text-muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.heatmap-row__cells {
  display: flex;
  flex: 1;
  gap: 2px;
  min-width: 0;
}
.heatmap-cell {
  flex: 0 0 auto;
  border-radius: 2px;
}
.heatmap-row__total {
  flex: 0 0 5rem;
  text-align: right;
  font-variant-numeric: tabular-nums;
}

.heatmap-row--header {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}
.heatmap-cell-label {
  flex: 0 0 auto;
  text-align: center;
  white-space: nowrap;
}

.heatmap-legend {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}
.heatmap-legend__title {
  margin-right: 0.25rem;
  font-weight: 600;
}
.heatmap-legend__item {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  font-variant-numeric: tabular-nums;
}
.heatmap-legend__item--offline {
  margin-left: 1rem;
}
.heatmap-legend__swatch {
  display: inline-block;
  width: 0.875rem;
  height: 0.875rem;
  border-radius: 2px;
}

.trends-legend {
  display: flex;
  flex-wrap: wrap;
  gap: 1.25rem;
  margin: 0.5rem 0 1rem;
  font-size: 0.8125rem;
}
.trends-legend__item {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
}
.trends-legend__dot {
  display: inline-block;
  width: 1.25rem;
  height: 0.25rem;
  border-radius: 2px;
}

.trend-chart {
  margin-bottom: 1rem;
}
.trend-chart__title {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  margin-bottom: 0.25rem;
}
.trend-chart__body {
  display: flex;
  gap: 0.5rem;
  align-items: stretch;
}
.trend-chart__y-axis {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: flex-end;
  height: 140px;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
  padding: 0.125rem 0;
  box-sizing: border-box;
  min-width: 1.75rem;
}
.trend-chart__plot {
  flex: 1;
  min-width: 0;
}
.trend-chart__svg {
  display: block;
  width: 100%;
  height: 140px;
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  box-sizing: border-box;
}
.trend-chart__x-axis {
  display: flex;
  justify-content: space-between;
  margin-top: 0.25rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
}
</style>
