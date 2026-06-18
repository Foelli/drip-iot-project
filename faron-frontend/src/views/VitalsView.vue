<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { type DataTableColumns, NButton } from 'naive-ui'
import { OFFLINE_CONFIG, STATUS_CONFIG } from '@/components/plants/plantDisplay'

defineOptions({ name: 'VitalsView' })

// --- Fleet summary mock ---
const summary = ref({
  plants_total: 12,
  healthy: 8,
  healthy_delta: 1,
  needs_attention: 3,
  needs_attention_delta: 2,
  critical: 1,
  critical_delta: 0,
  devices_online: 10,
  devices_total: 12,
})

type Delta = { display: string; tone: 'good' | 'bad' } | null
function deltaFor(value: number, goodDirection: 'up' | 'down'): Delta {
  if (value === 0) return null
  const arrow = value > 0 ? '▲' : '▼'
  const isGood = value > 0 === (goodDirection === 'up')
  return { display: `${arrow}${Math.abs(value)}`, tone: isGood ? 'good' : 'bad' }
}

const healthyDelta = computed(() => deltaFor(summary.value.healthy_delta, 'up'))
const attentionDelta = computed(() => deltaFor(summary.value.needs_attention_delta, 'down'))
const criticalDelta = computed(() => deltaFor(summary.value.critical_delta, 'down'))

// --- Attention queue mock ---
type Severity = 'critical' | 'needs_attention' | 'offline'
interface AttentionRow {
  id: number
  severity: Severity
  plant: string
  issue: string
  room: string
  last_reading: string
}

function attentionDotColor(severity: Severity): string {
  return severity === 'offline' ? OFFLINE_CONFIG.color : STATUS_CONFIG[severity].color
}

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

const attentionRows: AttentionRow[] = [
  {
    id: 1,
    severity: 'critical',
    plant: 'My Monstera',
    issue: 'Soil too dry (28%)',
    room: 'Living Room',
    last_reading: 'just now',
  },
  {
    id: 2,
    severity: 'needs_attention',
    plant: 'My Snake Plant',
    issue: 'Too cold (8°C)',
    room: 'Bedroom',
    last_reading: '1 min ago',
  },
  {
    id: 3,
    severity: 'offline',
    plant: 'My Spider Plant',
    issue: 'Device offline',
    room: 'Kitchen',
    last_reading: '3 h ago',
  },
  {
    id: 4,
    severity: 'needs_attention',
    plant: 'My Fiddle Leaf',
    issue: 'No water in 12 d',
    room: 'Living Room',
    last_reading: 'just now',
  },
]

type Range = '24h' | '7d' | '30d' | '90d'
const range = ref<Range>('7d')

const rangeOptions: { label: string; value: Range }[] = [
  { label: '24h', value: '24h' },
  { label: '7d', value: '7d' },
  { label: '30d', value: '30d' },
  { label: '90d', value: '90d' },
]

type Metric = 'moisture' | 'temp'
const heatmapMetric = ref<Metric>('moisture')
const metricOptions: { label: string; value: Metric }[] = [
  { label: 'Moisture', value: 'moisture' },
  { label: 'Temp', value: 'temp' },
]

// --- Time-in-band heat map mock ---
interface HeatmapRow {
  plant: string
  data: { moisture: (number | null)[]; temp: (number | null)[] }
}

function gen(n: number, baseline: number): (number | null)[] {
  return Array.from({ length: n }, () => {
    const v = Math.max(0, Math.min(1, baseline + (Math.random() - 0.5) * 0.5)) * 100
    return Math.round(v)
  })
}
function offline(n: number): (number | null)[] {
  const out: (number | null)[] = []
  for (let i = 0; i < n; i++) out.push(null)
  return out
}

const heatmapData: HeatmapRow[] = [
  { plant: 'My Fiddle Leaf',  data: { moisture: gen(90, 0.85), temp: gen(90, 0.78) } },
  { plant: 'My Snake Plant',  data: { moisture: gen(90, 0.45), temp: gen(90, 0.55) } },
  { plant: 'My Spider Plant', data: { moisture: offline(90),   temp: offline(90)   } },
  { plant: 'My Monstera',     data: { moisture: gen(90, 0.60), temp: gen(90, 0.65) } },
]

const rangeToCells: Record<Range, number> = { '24h': 1, '7d': 7, '30d': 30, '90d': 90 }

const visibleHeatmap = computed(() =>
  heatmapData.map((r) => ({
    plant: r.plant,
    cells: r.data[heatmapMetric.value].slice(-rangeToCells[range.value]),
  })),
)

// Squares, sized so the row fits without scrolling. Larger cap at sparser
// ranges so day-name labels still fit at 7d.
const cellSize = computed(() => {
  const n = rangeToCells[range.value]
  return Math.max(6, Math.min(30, Math.floor(420 / n)))
})

// Metric drives the hue: moisture → blue, temp → orange. Five opacity steps each.
const METRIC_RGB: Record<Metric, string> = {
  moisture: '59, 130, 246', // blue-500  (#3b82f6)
  temp: '249, 115, 22', // orange-500 (#f97316)
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
function cellColor(v: number | null): string {
  if (v === null) return 'var(--neutral-400)'
  const s = shades.value
  if (v >= 80) return s[4]
  if (v >= 60) return s[3]
  if (v >= 40) return s[2]
  if (v >= 20) return s[1]
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
function cellTitle(v: number | null): string {
  return v === null ? 'Offline' : `${v}% in band`
}
function rowAverage(cells: (number | null)[]): string {
  const valid = cells.filter((c): c is number => c !== null)
  if (valid.length === 0) return '— offline'
  const avg = Math.round(valid.reduce((a, b) => a + b, 0) / valid.length)
  return `${avg}%`
}

// --- Trends across the fleet -------------------------------------------------
interface TrendRow {
  plant: string
  color: string
  moisture: number[]
  temp: number[]
}

function trendGen(n: number, baseline: number, amplitude: number, jitter: number): number[] {
  return Array.from({ length: n }, (_, i) => {
    const v = baseline + Math.sin(i / 4) * amplitude + (Math.random() - 0.5) * jitter
    return Math.round(v * 10) / 10
  })
}

// Colors taken from the wireframe (Fiddle blue, Snake green, Monstera orange).
const trendsData: TrendRow[] = [
  {
    plant: 'My Fiddle Leaf',
    color: '#3b82f6',
    moisture: trendGen(90, 65, 8, 6),
    temp: trendGen(90, 22, 1.5, 1),
  },
  {
    plant: 'My Snake Plant',
    color: '#22c55e',
    moisture: trendGen(90, 30, 5, 4),
    temp: trendGen(90, 19, 1, 0.8),
  },
  {
    plant: 'My Monstera',
    color: '#f97316',
    moisture: trendGen(90, 50, 12, 8),
    temp: trendGen(90, 26, 2.5, 1.5),
  },
]

// Y-axis ticks per chart (top → bottom, must match the dMin/dMax used in trendPath).
const moistureYTicks = [100, 50, 0]
const tempYTicks = [35, 22, 10]

// X-axis ticks scale with the active range (start → mid → now).
const X_AXIS_LABELS: Record<Range, string[]> = {
  '24h': ['24h ago', '12h ago', 'now'],
  '7d': ['7d ago', '3d ago', 'now'],
  '30d': ['30d ago', '15d ago', 'now'],
  '90d': ['90d ago', '45d ago', 'now'],
}
const xAxisLabels = computed<string[]>(() => X_AXIS_LABELS[range.value])

const TRENDS_W = 600
const TRENDS_H = 120

function trendPath(values: number[], dMin: number, dMax: number, count: number): string {
  const sliced = values.slice(-count)
  const last = sliced.length - 1 || 1
  return sliced
    .map((v, i) => {
      const x = (i / last) * TRENDS_W
      const y = TRENDS_H - ((v - dMin) / (dMax - dMin)) * TRENDS_H
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}

const visibleTrends = computed(() => {
  const n = rangeToCells[range.value]
  return trendsData.map((t) => ({
    plant: t.plant,
    color: t.color,
    moistureLine: trendPath(t.moisture, 0, 100, n),
    tempLine: trendPath(t.temp, 10, 35, n),
  }))
})

// --- Devices -----------------------------------------------------------------
interface DeviceRow {
  id: number
  device: string
  plant: string
  online: boolean
  firmware: string
  last_seen: string
}

const deviceRows: DeviceRow[] = [
  { id: 1, device: 'Pico-01', plant: 'My Fiddle Leaf',  online: true,  firmware: 'v0.4.1', last_seen: '2 min ago' },
  { id: 2, device: 'Pico-02', plant: 'My Snake Plant',  online: true,  firmware: 'v0.4.1', last_seen: '1 min ago' },
  { id: 3, device: 'Pico-03', plant: 'My Spider Plant', online: false, firmware: 'v0.3.8', last_seen: '3 h ago' },
  { id: 4, device: 'Pico-04', plant: 'My Monstera',     online: true,  firmware: 'v0.4.1', last_seen: '2 min ago' },
]

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
  {
    title: '',
    key: 'actions',
    render: (row) =>
      row.online ? '' : h(NButton, { size: 'tiny', secondary: true }, () => 'Re-pair'),
  },
]
</script>

<template>
  <section class="vitals-view">
    <div class="view-header">
      <h1>Vitals</h1>
    </div>
    <n-radio-group class="range-control" v-model:value="range" size="small">
      <n-radio-button class="rangeOptions" v-for="o in rangeOptions" :key="o.value" :value="o.value">
        {{ o.label }}
      </n-radio-button>
    </n-radio-group>
    <div class="vitals-content">
      <!-- Fleet status -->
      <n-divider title-placement="left">Fleet Status</n-divider>
      <div class="stat-strip">
        <n-statistic label="Plants" :value="summary.plants_total" />

        <n-statistic label="Healthy" :value="summary.healthy">
          <template #suffix>
            <span v-if="healthyDelta" :class="`delta delta--${healthyDelta.tone}`">
              {{ healthyDelta.display }}
            </span>
          </template>
        </n-statistic>

        <n-statistic label="Needs attention" :value="summary.needs_attention">
          <template #suffix>
            <span v-if="attentionDelta" :class="`delta delta--${attentionDelta.tone}`">
              {{ attentionDelta.display }}
            </span>
          </template>
        </n-statistic>

        <n-statistic label="Critical" :value="summary.critical">
          <template #suffix>
            <span v-if="criticalDelta" :class="`delta delta--${criticalDelta.tone}`">
              {{ criticalDelta.display }}
            </span>
          </template>
        </n-statistic>

        <n-statistic label="Devices online">
          {{ summary.devices_online }}<span class="muted">/{{ summary.devices_total }}</span>
        </n-statistic>
      </div>

      <!-- Attention -->
      <div class="section-row">
        <n-divider title-placement="left" class="section-row__divider">
          Needs Attention now
        </n-divider>
        <n-button text size="small">View all activity ›</n-button>
      </div>
      <n-data-table
        :columns="attentionColumns"
        :data="attentionRows"
        size="small"
        :pagination="false"
      />

      <!-- Time in band -->
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
              v-for="(l, i) in dayLabels"
              :key="i"
              class="heatmap-cell-label"
              :style="{ width: cellSize + 'px' }"
            >
              {{ l }}
            </div>
          </div>
          <div class="heatmap-row__total">In band</div>
        </div>
        <div v-for="row in visibleHeatmap" :key="row.plant" class="heatmap-row">
          <div class="heatmap-row__label">{{ row.plant }}</div>
          <div class="heatmap-row__cells">
            <div
              v-for="(c, i) in row.cells"
              :key="i"
              class="heatmap-cell"
              :style="{
                width: cellSize + 'px',
                height: cellSize + 'px',
                background: cellColor(c),
              }"
              :title="cellTitle(c)"
            />
          </div>
          <div class="heatmap-row__total">{{ rowAverage(row.cells) }}</div>
        </div>
        <div class="heatmap-legend">
          <span class="heatmap-legend__title">% time in band:</span>
          <span class="heatmap-legend__item">
            <span class="heatmap-legend__swatch" :style="{ background: shades[0] }"></span>
            0&ndash;19
          </span>
          <span class="heatmap-legend__item">
            <span class="heatmap-legend__swatch" :style="{ background: shades[1] }"></span>
            20&ndash;39
          </span>
          <span class="heatmap-legend__item">
            <span class="heatmap-legend__swatch" :style="{ background: shades[2] }"></span>
            40&ndash;59
          </span>
          <span class="heatmap-legend__item">
            <span class="heatmap-legend__swatch" :style="{ background: shades[3] }"></span>
            60&ndash;79
          </span>
          <span class="heatmap-legend__item">
            <span class="heatmap-legend__swatch" :style="{ background: shades[4] }"></span>
            80&ndash;100
          </span>
          <span class="heatmap-legend__item heatmap-legend__item--offline">
            <span class="heatmap-legend__swatch" style="background: var(--neutral-400)"></span>
            Offline
          </span>
        </div>
      </div>

      <!-- Trends -->
      <n-divider title-placement="left">Trends across the fleet</n-divider>
      <div class="trends-legend">
        <span v-for="t in trendsData" :key="t.plant" class="trends-legend__item">
          <span class="trends-legend__dot" :style="{ background: t.color }"></span>
          {{ t.plant }}
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
                v-for="t in visibleTrends"
                :key="t.plant"
                :points="t.moistureLine"
                fill="none"
                :stroke="t.color"
                stroke-width="1.5"
                vector-effect="non-scaling-stroke"
              />
            </svg>
            <div class="trend-chart__x-axis">
              <span v-for="(l, i) in xAxisLabels" :key="i">{{ l }}</span>
            </div>
          </div>
        </div>
      </div>
      <div class="trend-chart">
        <div class="trend-chart__title">Temperature (°C)</div>
        <div class="trend-chart__body">
          <div class="trend-chart__y-axis">
            <span v-for="tick in tempYTicks" :key="tick">{{ tick }}</span>
          </div>
          <div class="trend-chart__plot">
            <svg viewBox="0 0 600 120" preserveAspectRatio="none" class="trend-chart__svg">
              <polyline
                v-for="t in visibleTrends"
                :key="t.plant"
                :points="t.tempLine"
                fill="none"
                :stroke="t.color"
                stroke-width="1.5"
                vector-effect="non-scaling-stroke"
              />
            </svg>
            <div class="trend-chart__x-axis">
              <span v-for="(l, i) in xAxisLabels" :key="i">{{ l }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Devices -->
      <n-divider title-placement="left">Devices</n-divider>
      <n-data-table
        :columns="deviceColumns"
        :data="deviceRows"
        size="small"
        :pagination="false"
      />
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

/* Only the range selector sticks (the title scrolls away). It lives in the
   tall .vitals-view so it isn't released when the header scrolls off. No
   background needed — the radio buttons are opaque on their own. The negative
   top margin tucks it back up onto the title's row in the resting state. */
.range-control {
  position: sticky;
  top: 0.5rem;
  z-index: 20;
  display: flex;
  justify-content: flex-end;
  /* tuck up onto the title's row in the resting state */
  margin-top: -5rem;
  margin-bottom: 2rem;
  /* the empty area left of the buttons must not block clicks on the title */
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

.delta {
  font-size: 0.75rem;
  margin-left: 0.4rem;
  font-weight: 600;
}
.delta--good {
  color: var(--success);
}
.delta--bad {
  color: var(--danger);
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
