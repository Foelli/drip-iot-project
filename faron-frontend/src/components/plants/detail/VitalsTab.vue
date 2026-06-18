<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Plant } from '@/types/Plant'

const props = defineProps<{ plant: Plant }>()

type Range = '24h' | '7d' | '30d' | '90d'
const range = ref<Range>('7d')

const rangeOptions: { label: string; value: Range }[] = [
  { label: '24h', value: '24h' },
  { label: '7d', value: '7d' },
  { label: '30d', value: '30d' },
  { label: '90d', value: '90d' },
]

const POINT_COUNT: Record<Range, number> = { '24h': 24, '7d': 56, '30d': 60, '90d': 90 }

// Thresholds — read this plant's settings, fall back if not overridden.
const thresholds = computed(() => {
  const t = props.plant.settings.thresholds
  return {
    moisture: { min: t?.moisture_min ?? 40, max: t?.moisture_max ?? 70 },
    temp: { min: t?.temp_min ?? 18, max: t?.temp_max ?? 26 },
  }
})

// Mock readings — sinusoidal trend + small noise so charts look natural.
type Light = 'low' | 'medium' | 'high'
function makeReadings(n: number) {
  const out: { moisture: number; temperature: number; light: Light }[] = []
  let m = 60
  let tmp = 22
  for (let i = 0; i < n; i++) {
    m += Math.sin(i / 4) * 2 + (Math.random() - 0.5) * 4
    tmp += Math.cos(i / 5) * 0.6 + (Math.random() - 0.5) * 1
    m = Math.max(20, Math.min(85, m))
    tmp = Math.max(15, Math.min(30, tmp))
    const hr = i % 24
    let light: Light = 'low'
    if (hr >= 9 && hr < 16) light = 'high'
    else if ((hr >= 6 && hr < 9) || (hr >= 16 && hr < 19)) light = 'medium'
    out.push({ moisture: m, temperature: tmp, light })
  }
  return out
}

const readings = computed(() => makeReadings(POINT_COUNT[range.value]))

function stats(values: number[], band: { min: number; max: number }) {
  const min = Math.round(Math.min(...values))
  const max = Math.round(Math.max(...values))
  const avg = Math.round(values.reduce((s, v) => s + v, 0) / values.length)
  const inBand = Math.round(
    (values.filter((v) => v >= band.min && v <= band.max).length / values.length) * 100,
  )
  return { min, max, avg, inBand }
}

const moistureStats = computed(() =>
  stats(
    readings.value.map((r) => r.moisture),
    thresholds.value.moisture,
  ),
)
const tempStats = computed(() =>
  stats(
    readings.value.map((r) => r.temperature),
    thresholds.value.temp,
  ),
)

const lightSummary = computed(() => {
  const counts = { low: 0, medium: 0, high: 0 }
  readings.value.forEach((r) => counts[r.light]++)
  const total = readings.value.length
  const avgLabel =
    counts.high > total * 0.4 ? 'Bright' : counts.medium > total * 0.4 ? 'Medium' : 'Low'
  const brightHours = ((counts.high / total) * 24).toFixed(1)
  return { avgLabel, brightHours }
})

// Chart geometry — fixed viewBox, SVG stretches to container width.
const CHART_W = 600
const CHART_H = 120

function linePoints(values: number[], dMin: number, dMax: number) {
  const last = values.length - 1 || 1
  return values
    .map((v, i) => {
      const x = (i / last) * CHART_W
      const y = CHART_H - ((v - dMin) / (dMax - dMin)) * CHART_H
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
  linePoints(
    readings.value.map((r) => r.moisture),
    0,
    100,
  ),
)
const tempLine = computed(() =>
  linePoints(
    readings.value.map((r) => r.temperature),
    10,
    35,
  ),
)
const moistureBand = computed(() =>
  band(thresholds.value.moisture.min, thresholds.value.moisture.max, 0, 100),
)
const tempBand = computed(() => band(thresholds.value.temp.min, thresholds.value.temp.max, 10, 35))

// Watering events plotted on the moisture chart x-axis.
const wateringMarkers = computed(() => {
  const n = readings.value.length
  return [0.15, 0.5, 0.85].map((p) => Math.floor(n * p))
})
const xFromIndex = (i: number) => (i / (readings.value.length - 1 || 1)) * CHART_W

// Light stepped bars.
const LIGHT_HEIGHT: Record<Light, number> = { low: 0.25, medium: 0.6, high: 1 }
const LIGHT_COLOR: Record<Light, string> = {
  low: '#94a3b8', // slate — dim
  medium: '#fbbf24', // amber — partly sunny
  high: '#f59e0b', // amber-bright — bright
}
const lightBars = computed(() => {
  const n = readings.value.length
  const w = CHART_W / n
  return readings.value.map((r, i) => {
    const h = LIGHT_HEIGHT[r.light] * CHART_H
    return {
      x: (i / n) * CHART_W,
      y: CHART_H - h,
      w: Math.max(1, w - 0.5),
      h,
      fill: LIGHT_COLOR[r.light],
    }
  })
})

// Hardcoded watering events for the table.
const wateringRows = [
  { time: 'Today 09:14', trigger: 'Auto (38%)', duration: '8 s', delta: '+24 pts' },
  { time: 'Mon 14:02', trigger: 'Manual', duration: '12 s', delta: '+31 pts' },
  { time: 'Sat 10:21', trigger: 'Auto (40%)', duration: '8 s', delta: '+28 pts' },
]
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

    <n-divider title-placement="left">Moisture</n-divider>
    <div class="stat-strip">
      <n-statistic label="Min" :value="moistureStats.min">
        <template #suffix>%</template>
      </n-statistic>
      <n-statistic label="Avg" :value="moistureStats.avg">
        <template #suffix>%</template>
      </n-statistic>
      <n-statistic label="Max" :value="moistureStats.max">
        <template #suffix>%</template>
      </n-statistic>
      <n-statistic label="In band" :value="moistureStats.inBand">
        <template #suffix>%</template>
      </n-statistic>
    </div>
    <svg
      :viewBox="`0 0 ${CHART_W} ${CHART_H + 24}`"
      class="chart chart--with-events"
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
      <g v-for="i in wateringMarkers" :key="`w-${i}`">
        <line
          :x1="xFromIndex(i)"
          y1="0"
          :x2="xFromIndex(i)"
          :y2="CHART_H"
          class="event-line"
          vector-effect="non-scaling-stroke"
        />
        <circle :cx="xFromIndex(i)" :cy="CHART_H + 12" r="4" class="event-drop" />
      </g>
    </svg>

    <n-divider title-placement="left">Temperature</n-divider>
    <div class="stat-strip">
      <n-statistic label="Min" :value="tempStats.min">
        <template #suffix>°C</template>
      </n-statistic>
      <n-statistic label="Avg" :value="tempStats.avg">
        <template #suffix>°C</template>
      </n-statistic>
      <n-statistic label="Max" :value="tempStats.max">
        <template #suffix>°C</template>
      </n-statistic>
      <n-statistic label="In band" :value="tempStats.inBand">
        <template #suffix>%</template>
      </n-statistic>
    </div>
    <svg :viewBox="`0 0 ${CHART_W} ${CHART_H}`" class="chart" preserveAspectRatio="none">
      <rect x="0" :y="tempBand.y" :width="CHART_W" :height="tempBand.h" class="band-temp" />
      <polyline
        :points="tempLine"
        fill="none"
        class="line-temp"
        vector-effect="non-scaling-stroke"
      />
    </svg>

    <n-divider title-placement="left">Light</n-divider>
    <div class="stat-strip stat-strip--2">
      <n-statistic label="Avg level">{{ lightSummary.avgLabel }}</n-statistic>
      <n-statistic label="Bright hours / day">{{ lightSummary.brightHours }}</n-statistic>
    </div>
    <svg :viewBox="`0 0 ${CHART_W} ${CHART_H}`" class="chart" preserveAspectRatio="none">
      <rect
        v-for="(b, idx) in lightBars"
        :key="idx"
        :x="b.x"
        :y="b.y"
        :width="b.w"
        :height="b.h"
        :fill="b.fill"
      />
    </svg>

    <div class="section-row">
      <n-divider title-placement="left" class="section-row__divider">Watering events</n-divider>
      <n-button text size="small">View all in Journal ›</n-button>
    </div>
    <n-table size="small" :single-line="false">
      <thead>
        <tr>
          <th>Time</th>
          <th>Trigger</th>
          <th>Duration</th>
          <th>Δ moisture</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in wateringRows" :key="row.time">
          <td>{{ row.time }}</td>
          <td>{{ row.trigger }}</td>
          <td>{{ row.duration }}</td>
          <td>{{ row.delta }}</td>
        </tr>
      </tbody>
    </n-table>
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
.stat-strip--2 {
  grid-template-columns: repeat(2, 1fr);
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
.chart--with-events {
  height: 172px;
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
.event-line {
  stroke: rgba(59, 130, 246, 0.35);
  stroke-dasharray: 2 2;
}
.event-drop {
  fill: #3b82f6;
}

.section-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.section-row__divider {
  flex: 1;
}
</style>
