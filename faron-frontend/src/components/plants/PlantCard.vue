<script setup lang="ts">
import { computed } from 'vue'
import { Water } from '@vicons/ionicons5'
import type { Plant } from '@/types/Plant'
import { useMessage } from 'naive-ui'
import { OFFLINE_CONFIG, STATUS_CONFIG } from './plantDisplay'

const props = defineProps<{ plant: Plant }>()
const message = useMessage()

const WATER_COLOR = '#3b82f6'

function handleWaterClick() {
  message.info('Automatic watering is handled by the Pico when moisture falls below the threshold.')
}

const TEMP_MIN = 0
const TEMP_MAX = 40

const temperaturePercentage = computed(() => {
  const clamped = Math.max(TEMP_MIN, Math.min(TEMP_MAX, props.plant.readings.temperature))
  return ((clamped - TEMP_MIN) / (TEMP_MAX - TEMP_MIN)) * 100
})

const temperatureColor = computed(() => {
  const clamped = Math.max(TEMP_MIN, Math.min(TEMP_MAX, props.plant.readings.temperature))
  const t = (clamped - TEMP_MIN) / (TEMP_MAX - TEMP_MIN)
  const hue = 220 - t * 220
  return `hsl(${hue}, 75%, 50%)`
})

// Corner dot: device-offline overrides health (gray), otherwise green/yellow/red.
const statusDot = computed(() =>
  props.plant.device.online ? STATUS_CONFIG[props.plant.readings.status] : OFFLINE_CONFIG,
)

const imageUrl = computed(
  () =>
    props.plant.photo_url ??
    props.plant.species.default_image?.regular_url ??
    props.plant.species.default_image?.medium_url ??
    '/favicon.ico',
)
</script>

<template>
  <n-card
    class="plant-card"
    size="small"
    hoverable
    :bordered="true"
    embedded
    :segmented="{ action: true }"
  >
    <template #cover>
      <div class="plant-card-cover">
        <img class="plant-card-cover__img" :src="imageUrl" alt="Plant image" />
        <span
          class="plant-card-status"
          :style="{ background: statusDot.color }"
          :title="statusDot.label"
        ></span>
      </div>
    </template>

    <div class="plant-card-body">
      <header class="plant-card-heading">
        <h3 class="plant-card__name">{{ plant.custom_name }}</h3>
        <p class="plant-card__room">{{ plant.room }}</p>
      </header>

      <div class="plant-card-moisture">
        <n-progress
          type="line"
          :percentage="plant.readings.moisture"
          :height="8"
          :border-radius="4"
          :show-indicator="false"
        />
        <span class="plant-card-moisture__value">{{ Math.round(plant.readings.moisture) }}%</span>
      </div>

      <div class="plant-card-temperature">
        <n-progress
          type="line"
          :percentage="temperaturePercentage"
          :color="temperatureColor"
          :height="8"
          :border-radius="4"
          :show-indicator="false"
        />
        <span class="plant-card-temperature__value"
          >{{ Math.round(plant.readings.temperature) }}°C</span
        >
      </div>
    </div>

    <template #action>
      <n-button
        @click.stop="handleWaterClick"
        size="small"
        secondary
        block
        :color="WATER_COLOR"
        class="water-button"
      >
        <template #icon>
          <n-icon><Water /></n-icon>
        </template>
        Auto watering
      </n-button>
    </template>
  </n-card>
</template>

<style scoped>
.n-card {
  width: 100%;
  height: 100%;
  border-radius: var(--radius-lg);
  overflow: hidden;
  cursor: pointer;
  transition:
    transform 0.14s ease,
    background-color 0.15s ease,
    box-shadow 0.15s ease;
}

.n-card:hover {
  background-color: var(--neutral-100);
  box-shadow: inset 0 1px 0 rgba(0, 0, 0, 0.04);
  transform: translateY(-2px);
}

:root.dark .n-card:hover {
  background-color: var(--neutral-700);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.12);
}

.plant-card-cover {
  position: relative;
}

.plant-card-cover__img {
  display: block;
  width: 100%;
  aspect-ratio: 4 / 3;
  object-fit: cover;
}

.plant-card-status {
  position: absolute;
  top: 0.75rem;
  right: 0.75rem;
  width: 0.75rem;
  height: 0.75rem;
  border-radius: 50%;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.25);
}

.plant-card-body {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  margin-top: 0.75rem;
}

.plant-card__name {
  font-size: 1rem;
  font-weight: 600;
}

.plant-card__room {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.plant-card-moisture,
.plant-card-temperature {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.plant-card-moisture__value,
.plant-card-temperature__value {
  min-width: 2.75rem;
  font-size: 0.8125rem;
  color: var(--color-text-muted);
  text-align: right;
}

.plant-card__next {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

.water-button {
  font-weight: 700;
}

.watering-progress {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  /* match the small button's height so the action row doesn't jump */
  height: 28px;
}
.watering-progress :deep(.n-progress) {
  flex: 1;
}
</style>
