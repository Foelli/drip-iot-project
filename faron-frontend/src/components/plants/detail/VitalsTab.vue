<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { api, type Measurement } from '@/api/client'
import type { Plant } from '@/types/Plant'

const props = defineProps<{ plant: Plant }>()

type Range = '24h' | '7d' | '30d' | '90d'
const range = ref<Range>('7d')
const measurements = ref<Measurement[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const rangeOptions: { label: string; value: Range }[] = [
  { label: '24h', value: '24h' },
  { label: '7d', value: '7d' },
  { label: '30d', value: '30d' },
  { label: '90d', value: '90d' },
]

const RANGE_MS: Record<Range, number> = {
  '24h': 24 * 60 * 60 * 1000,
  '7d': 7 * 24 * 60 * 60 * 1000,
  '30d': 30 * 24 * 60 * 60 * 1000,
  '90d': 90 * 24 * 60 * 60 * 1000,
}

const thresholds = computed(() => {
  const t = props.plant.settings.thresholds
  return {
    moisture: { min: t?.moisture_min ?? 40, max: t?.moisture_max ?? 100 },
    temp: { min: t?.temp_min ?? 18, max: t?.temp_max ?? 30 },
    airMoisture: { min: t?.air_moisture_min ?? 40, max: t?.air_moisture_max ?? 70 },
  }
})

const rangeStart = computed(() => Date.now() - RANGE_MS[range.value])
const rangeMeasurements = computed(() =>
  measurements.value
    .filter((measurement) => new Date(measurement.measuredAt).getTime() >= rangeStart.value)
    .sort((a, b) => new Date(a.measuredAt).getTime() - new Date(b.measuredAt).getTime()),
)
function stats(values: number[], band: { min: number; max: number }) {
  if (values.length === 0) return { min: null, max: null, avg: null, inBand: null }

  const min = Math.round(Math.min(...values))
  const max = Math.round(Math.max(...values))
  const avg = Math.round(values.reduce((sum, value) => sum + value, 0) / values.length)
  const inBand = Math.round(
    (values.filter((value) => value >= band.min && value <= band.max).length / values.length) * 100,
  )

  return { min, max, avg, inBand }
}

function valuesFor(valueFor: (measurement: Measurement) => number | null): number[] {
  return rangeMeasurements.value.map(valueFor).filter((value): value is number => value !== null)
}

const moistureValues = computed(() => valuesFor((measurement) => measurement.soilMoisture))
const tempValues = computed(() => valuesFor((measurement) => measurement.temperature))
const airMoistureValues = computed(() => valuesFor((measurement) => measurement.airMoisture))

const moistureStats = computed(() => stats(moistureValues.value, thresholds.value.moisture))
const tempStats = computed(() => stats(tempValues.value, thresholds.value.temp))
const airMoistureStats = computed(() =>
  stats(airMoistureValues.value, thresholds.value.airMoisture),
)

const CHART_W = 600
const CHART_H = 120

function linePoints(
  readings: Measurement[],
  valueFor: (measurement: Measurement) => number | null,
  dMin: number,
  dMax: number,
) {
  const points = readings
    .map((measurement) => {
      const value = valueFor(measurement)
      if (value === null) return null
      return { time: new Date(measurement.measuredAt).getTime(), value }
    })
    .filter((point): point is { time: number; value: number } => point !== null)

  if (points.length === 0) return ''

  const start = rangeStart.value
  const end = start + RANGE_MS[range.value]

  return points
    .map((point) => {
      const x = ((point.time - start) / (end - start)) * CHART_W
      const clamped = Math.max(dMin, Math.min(dMax, point.value))
      const y = CHART_H - ((clamped - dMin) / (dMax - dMin)) * CHART_H
      return `${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}

function band(low: number, high: number, dMin: number, dMax: number) {
  const y1 = CHART_H - ((high - dMin) / (dMax - dMin)) * CHART_H
  const y2 = CHART_H - ((low - dMin) / (dMax - dMin)) * CHART_H
  return { y: y1, h: Math.max(0, y2 - y1) }
}

const moistureLine = computed(() =>
  linePoints(rangeMeasurements.value, (measurement) => measurement.soilMoisture, 0, 100),
)
const tempLine = computed(() =>
  linePoints(rangeMeasurements.value, (measurement) => measurement.temperature, 10, 35),
)
const airMoistureLine = computed(() =>
  linePoints(rangeMeasurements.value, (measurement) => measurement.airMoisture, 0, 100),
)
const moistureBand = computed(() =>
  band(thresholds.value.moisture.min, thresholds.value.moisture.max, 0, 100),
)
const tempBand = computed(() => band(thresholds.value.temp.min, thresholds.value.temp.max, 10, 35))
const airMoistureBand = computed(() =>
  band(thresholds.value.airMoisture.min, thresholds.value.airMoisture.max, 0, 100),
)

function formatStat(value: number | null) {
  return value ?? '-'
}

async function loadVitals() {
  loading.value = true
  error.value = null

  try {
    measurements.value = await api.getMeasurements(props.plant.id)
  } catch (err) {
    console.error(err)
    error.value = 'Could not load plant vitals.'
  } finally {
    loading.value = false
  }
}

onMounted(loadVitals)
watch(() => props.plant.id, loadVitals)
</script>

<template>
  <div class="vitals-tab">
    <n-flex justify="space-between" align="center" class="range-row">
      <n-text depth="3">Range</n-text>
      <n-radio-group v-model:value="range" size="small">
        <n-radio-button v-for="o in rangeOptions" :key="o.value" :value="o.value">
          {{ o.label }}
        </n-radio-button>
      </n-radio-group>
    </n-flex>

    <n-alert v-if="error" type="error" :bordered="false">
      {{ error }}
    </n-alert>

    <n-spin v-else :show="loading">
      <n-divider title-placement="left">Moisture</n-divider>
      <div class="stat-strip">
        <n-statistic label="Min" :value="formatStat(moistureStats.min)">
          <template #suffix>%</template>
        </n-statistic>
        <n-statistic label="Avg" :value="formatStat(moistureStats.avg)">
          <template #suffix>%</template>
        </n-statistic>
        <n-statistic label="Max" :value="formatStat(moistureStats.max)">
          <template #suffix>%</template>
        </n-statistic>
        <n-statistic label="In band" :value="formatStat(moistureStats.inBand)">
          <template #suffix>%</template>
        </n-statistic>
      </div>
      <svg
        v-if="moistureLine"
        :viewBox="`0 0 ${CHART_W} ${CHART_H}`"
        class="chart"
        preserveAspectRatio="none"
      >
        <rect
          x="0"
          :y="moistureBand.y"
          :width="CHART_W"
          :height="moistureBand.h"
          class="band-moisture"
        />
        <polyline
          :points="moistureLine"
          fill="none"
          class="line-moisture"
          vector-effect="non-scaling-stroke"
        />
      </svg>
      <n-empty v-else description="No moisture readings in this range." size="small" />

      <n-divider title-placement="left">Temperature</n-divider>
      <div class="stat-strip">
        <n-statistic label="Min" :value="formatStat(tempStats.min)">
          <template #suffix>°C</template>
        </n-statistic>
        <n-statistic label="Avg" :value="formatStat(tempStats.avg)">
          <template #suffix>°C</template>
        </n-statistic>
        <n-statistic label="Max" :value="formatStat(tempStats.max)">
          <template #suffix>°C</template>
        </n-statistic>
        <n-statistic label="In band" :value="formatStat(tempStats.inBand)">
          <template #suffix>%</template>
        </n-statistic>
      </div>
      <svg
        v-if="tempLine"
        :viewBox="`0 0 ${CHART_W} ${CHART_H}`"
        class="chart"
        preserveAspectRatio="none"
      >
        <rect x="0" :y="tempBand.y" :width="CHART_W" :height="tempBand.h" class="band-temp" />
        <polyline
          :points="tempLine"
          fill="none"
          class="line-temp"
          vector-effect="non-scaling-stroke"
        />
      </svg>
      <n-empty v-else description="No temperature readings in this range." size="small" />

      <n-divider title-placement="left">Air moisture</n-divider>
      <div class="stat-strip">
        <n-statistic label="Min" :value="formatStat(airMoistureStats.min)">
          <template #suffix>%</template>
        </n-statistic>
        <n-statistic label="Avg" :value="formatStat(airMoistureStats.avg)">
          <template #suffix>%</template>
        </n-statistic>
        <n-statistic label="Max" :value="formatStat(airMoistureStats.max)">
          <template #suffix>%</template>
        </n-statistic>
        <n-statistic label="In band" :value="formatStat(airMoistureStats.inBand)">
          <template #suffix>%</template>
        </n-statistic>
      </div>
      <svg
        v-if="airMoistureLine"
        :viewBox="`0 0 ${CHART_W} ${CHART_H}`"
        class="chart"
        preserveAspectRatio="none"
      >
        <rect
          x="0"
          :y="airMoistureBand.y"
          :width="CHART_W"
          :height="airMoistureBand.h"
          class="band-air-moisture"
        />
        <polyline
          :points="airMoistureLine"
          fill="none"
          class="line-air-moisture"
          vector-effect="non-scaling-stroke"
        />
      </svg>
      <n-empty v-else description="No air moisture readings in this range." size="small" />
    </n-spin>
  </div>
</template>

<style scoped>
.vitals-tab {
  padding: 0;
}

.range-row {
  margin-bottom: 0.25rem;
}

.stat-strip {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}
.stat-strip > .n-statistic {
  padding: 0.5rem 0.75rem;
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
}

.chart {
  width: 100%;
  height: 144px;
  display: block;
}
.band-moisture {
  fill: rgba(59, 130, 246, 0.15);
}
.line-moisture {
  stroke: #3b82f6;
  stroke-width: 1.5;
}
.band-temp {
  fill: rgba(249, 115, 22, 0.15);
}
.line-temp {
  stroke: #f97316;
  stroke-width: 1.5;
}
.band-air-moisture {
  fill: rgba(20, 184, 166, 0.15);
}
.line-air-moisture {
  stroke: #14b8a6;
  stroke-width: 1.5;
}
</style>
