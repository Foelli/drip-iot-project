<script setup lang="ts">
import { computed, h, reactive } from 'vue'
import type { Component, VNodeChild } from 'vue'
import { NFlex, NIcon } from 'naive-ui'
import { Thermometer, Water } from '@vicons/ionicons5'
import type { Plant } from '@/types/Plant'

defineProps<{ plant: Plant }>()

// Hardcoded defaults for now — swap to a draft cloned from props.plant later:
//   const form = reactive(structuredClone(toRaw(props.plant.settings)))
const DEFAULTS = {
  // This plant
  custom_name: 'My Fiddle Leaf Fig',
  room: 'Living Room',
  // Care thresholds
  moisture_min: 40,
  moisture_max: 70,
  temp_min: 18,
  temp_max: 26,
  light_target: 'medium' as 'low' | 'medium' | 'high',
  water_every_days: 7,
  // Automation
  auto_water: true,
  pump_duration_s: 8,
  cooldown_h: 6,
  quiet_hours_enabled: true,
  quiet_from: '22:00',
  quiet_to: '07:00',
  notifications: false,
}

const form = reactive({ ...DEFAULTS })

function resetToDefaults() {
  Object.assign(form, DEFAULTS)
}

// Range slider binds to a [min, max] tuple; keep it synced to the two fields.
const moistureRange = computed<[number, number]>({
  get: (): [number, number] => [form.moisture_min, form.moisture_max],
  set: ([min, max]) => {
    form.moisture_min = min
    form.moisture_max = max
  },
})

const tempRange = computed<[number, number]>({
  get: (): [number, number] => [form.temp_min, form.temp_max],
  set: ([min, max]) => {
    form.temp_min = min
    form.temp_max = max
  },
})

const formatMoisture = (v: number) => `${v}%`

const formatTemp = (v: number) => `${v}°C`

// Custom marks: a plain dot every 5, with an icon + colored label every 25.
function renderMark(label: string, color: string, icon: Component) {
  return h(NFlex, { vertical: true, align: 'center', size: 2, style: 'width: 44px' }, () => [
    h(NIcon, { size: 18, color, component: icon }),
    h('span', { style: { color, fontSize: '12px' } }, label),
  ])
}

// Marks every `step` units; icon + colored label at each.
function makeMarks(
  min: number,
  max: number,
  step: number,
  unit: string,
  icon: Component,
  colorFor: (v: number) => string,
) {
  const marks: Record<number, () => VNodeChild> = {}
  for (let v = min; v <= max; v += step) {
    marks[v] = () => renderMark(`${v}${unit}`, colorFor(v), icon)
  }
  return marks
}

const moistureMarks = makeMarks(0, 100, 25, '%', Water, () => '#3b82f6')

const TEMP_COLORS: Record<number, string> = {
  15: '#2563eb', // deep blue
  20: '#3b82f6', // blue
  25: '#0ea5e9', // light blue
  30: '#ca8a04', // gold
  35: '#f59e0b', // amber
  40: '#f97316', // orange
  45: '#dc2626', // red
}
const tempColor = (v: number) => TEMP_COLORS[v] ?? '#dc2626'
const tempMarks = makeMarks(15, 45, 5, '°C', Thermometer, tempColor)

// Same colors as a left→right gradient for the slider rail.
const tempGradient = computed(() => {
  const min = 15
  const max = 45
  const stops = Object.entries(TEMP_COLORS).map(
    ([v, c]) => `${c} ${((Number(v) - min) / (max - min)) * 100}%`,
  )
  return `linear-gradient(to right, ${stops.join(', ')})`
})

// Slider fill colors (hex, not CSS vars — naive derives hover/pressed shades).
const moistureTheme = { fillColor: '#3b82f6', fillColorHover: '#2563eb' }
const tempTheme = { fillColor: '#f97316', fillColorHover: '#ea580c' }

const roomOptions = [
  { label: 'Living Room', value: 'Living Room' },
  { label: 'Bedroom', value: 'Bedroom' },
  { label: 'Office', value: 'Office' },
]
</script>

<template>
  <div class="settings-tab">
    <n-form label-placement="top" class="settings-form">
      <n-divider title-placement="left">This plant</n-divider>
      <n-form-item label="Name">
        <n-input v-model:value="form.custom_name" />
      </n-form-item>

      <n-form-item label="Room">
        <n-select v-model:value="form.room" :options="roomOptions" />
      </n-form-item>

      <n-form-item label="Photo">
        <n-flex align="center" :size="12" :wrap="false">
          <n-avatar :size="40" src="https://picsum.photos/600" />
          <n-upload :action="null" :show-file-list="false">
            <n-button type="primary">Upload Image</n-button>
          </n-upload>
        </n-flex>
      </n-form-item>

      <n-divider title-placement="left">Care thresholds</n-divider>
      <n-button type="primary" size="small" class="reset-btn" @click="resetToDefaults"
        >Reset to defaults</n-button
      >
      <n-form-item label="Moisture range">
        <n-slider
          v-model:value="moistureRange"
          range
          :min="0"
          :max="100"
          :marks="moistureMarks"
          :format-tooltip="formatMoisture"
          :theme-overrides="moistureTheme"
        />
      </n-form-item>
      <n-form-item label="Temp range">
        <n-slider
          v-model:value="tempRange"
          range
          :min="15"
          :max="45"
          :marks="tempMarks"
          :format-tooltip="formatTemp"
          :theme-overrides="tempTheme"
          class="temp-slider"
          :style="{ '--temp-gradient': tempGradient }"
        />
      </n-form-item>
      <n-form-item label="Light target">
        <n-radio-group v-model:value="form.light_target">
          <n-radio value="low">Low light</n-radio>
          <n-radio value="medium">Medium light</n-radio>
          <n-radio value="high">High light</n-radio>
        </n-radio-group>
      </n-form-item>
    </n-form>
  </div>
</template>

<style scoped>
.settings-form :deep(.n-slider) {
  width: 75%;
  margin-left: 1rem;
}

/* hide the green dots on the rail; keep the icon/label marks below */
.settings-form :deep(.n-slider-dots) {
  display: none;
}

/* paint the temp gradient across the whole rail; let it show through the fill */
.temp-slider :deep(.n-slider-rail) {
  background-image: var(--temp-gradient);
}
.temp-slider :deep(.n-slider-rail__fill) {
  background-color: transparent !important;
}

/* uniform field labels */
.settings-form :deep(.n-form-item-label) {
  font-weight: 600;
}

.reset-btn {
  margin-bottom: 1rem;
}
</style>
