<script setup lang="ts">
import { computed, h, reactive, ref } from 'vue'
import type { Component, VNodeChild } from 'vue'
import { NFlex, NIcon, useMessage } from 'naive-ui'
import { Thermometer, Water } from '@vicons/ionicons5'
import { api } from '@/api/client'
import type { Plant } from '@/types/Plant'

const props = defineProps<{ plant: Plant }>()
const message = useMessage()
const saving = ref(false)

function currentDefaults() {
  return {
    custom_name: props.plant.custom_name,
    room: props.plant.room,
    moisture_min:
      props.plant.moistureThreshold ?? props.plant.settings.thresholds?.moisture_min ?? 40,
    moisture_max: props.plant.settings.thresholds?.moisture_max ?? 70,
    temp_min: props.plant.settings.thresholds?.temp_min ?? 18,
    temp_max: props.plant.settings.thresholds?.temp_max ?? 26,
    air_moisture_min: props.plant.settings.thresholds?.air_moisture_min ?? 40,
    air_moisture_max: props.plant.settings.thresholds?.air_moisture_max ?? 70,
    auto_water: props.plant.wateringEnabled ?? props.plant.settings.automation.auto_water,
    pump_duration_s:
      props.plant.pumpDurationMs != null
        ? Math.round(props.plant.pumpDurationMs / 1000)
        : props.plant.settings.automation.pump_duration_s,
    cooldown_h: props.plant.settings.automation.cooldown_h,
    quiet_hours_enabled: props.plant.settings.automation.quiet_hours.enabled,
    quiet_from: props.plant.settings.automation.quiet_hours.from,
    quiet_to: props.plant.settings.automation.quiet_hours.to,
    notifications: props.plant.settings.automation.notifications,
  }
}

const form = reactive(currentDefaults())

function resetToDefaults() {
  Object.assign(form, currentDefaults())
}

async function saveSettings() {
  const customName = form.custom_name.trim()
  if (!customName) {
    message.warning('Plant name is required')
    return
  }

  saving.value = true
  try {
    await api.updatePlantSettings(props.plant.id, {
      customName,
      wateringEnabled: form.auto_water,
      moistureThreshold: form.moisture_min,
      pumpDurationMs: form.pump_duration_s * 1000,
      waterSettleMs: props.plant.waterSettleMs ?? 20000,
    })

    form.custom_name = customName
    props.plant.custom_name = customName
    props.plant.wateringEnabled = form.auto_water
    props.plant.moistureThreshold = form.moisture_min
    props.plant.pumpDurationMs = form.pump_duration_s * 1000
    props.plant.settings.automation.auto_water = form.auto_water
    props.plant.settings.automation.pump_duration_s = form.pump_duration_s
    if (props.plant.settings.thresholds) {
      props.plant.settings.thresholds.moisture_min = form.moisture_min
      props.plant.settings.thresholds.moisture_max = form.moisture_max
      props.plant.settings.thresholds.temp_min = form.temp_min
      props.plant.settings.thresholds.temp_max = form.temp_max
      props.plant.settings.thresholds.air_moisture_min = form.air_moisture_min
      props.plant.settings.thresholds.air_moisture_max = form.air_moisture_max
    }

    message.success('Plant settings saved')
  } catch (error) {
    console.error(error)
    message.error('Could not save plant settings')
  } finally {
    saving.value = false
  }
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

const airMoistureRange = computed<[number, number]>({
  get: (): [number, number] => [form.air_moisture_min, form.air_moisture_max],
  set: ([min, max]) => {
    form.air_moisture_min = min
    form.air_moisture_max = max
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
const airMoistureMarks = makeMarks(0, 100, 25, '%', Water, () => '#14b8a6')

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
const airMoistureTheme = { fillColor: '#14b8a6', fillColorHover: '#0d9488' }
const tempTheme = { fillColor: '#f97316', fillColorHover: '#ea580c' }

const roomOptions = [
  { label: props.plant.room, value: props.plant.room },
  { label: 'Unknown', value: 'Unknown' },
  { label: 'Living Room', value: 'Living Room' },
  { label: 'Bedroom', value: 'Bedroom' },
  { label: 'Kitchen', value: 'Kitchen' },
  { label: 'Office', value: 'Office' },
]

const imageUrl = computed(
  () =>
    props.plant.photo_url ??
    props.plant.species.default_image?.regular_url ??
    props.plant.species.default_image?.medium_url ??
    '/favicon.ico',
)
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
          <n-avatar :size="40" :src="imageUrl" />
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
      <n-form-item label="Air moisture range">
        <n-slider
          v-model:value="airMoistureRange"
          range
          :min="0"
          :max="100"
          :marks="airMoistureMarks"
          :format-tooltip="formatMoisture"
          :theme-overrides="airMoistureTheme"
        />
      </n-form-item>

      <n-divider title-placement="left">Watering automation</n-divider>
      <n-form-item label="Automatic watering">
        <n-switch v-model:value="form.auto_water" />
      </n-form-item>
      <n-form-item label="Pump pulse duration">
        <n-input-number
          v-model:value="form.pump_duration_s"
          :min="1"
          :max="30"
          :step="1"
          :disabled="!form.auto_water"
        >
          <template #suffix>seconds</template>
        </n-input-number>
      </n-form-item>
      <n-button type="primary" :loading="saving" @click="saveSettings">Save settings</n-button>
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
