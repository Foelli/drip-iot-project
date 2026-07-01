<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { api, type WateringEvent } from '@/api/client'
import type { Plant } from '@/types/Plant'

const props = defineProps<{ plant: Plant }>()

const wateringEvents = ref<WateringEvent[]>([])

const latestWateringEvent = computed(() => wateringEvents.value[0] ?? null)
const lastWateredLabel = computed(() => {
  if (!latestWateringEvent.value) return 'Never'

  const createdAt = new Date(latestWateringEvent.value.createdAt)
  const diffMs = Date.now() - createdAt.getTime()
  const diffMinutes = Math.max(0, Math.round(diffMs / 60000))

  if (diffMinutes < 1) return 'Just now'
  if (diffMinutes < 60) return `${diffMinutes} min ago`

  const diffHours = Math.round(diffMinutes / 60)
  if (diffHours < 48) return `${diffHours} h ago`

  return `${Math.round(diffHours / 24)} days ago`
})

const idealMoisture = computed(
  () =>
    `${props.plant.care_info.ideal_moisture_min} - ${props.plant.care_info.ideal_moisture_max} %`,
)
const idealTemp = computed(
  () => `${props.plant.care_info.ideal_temp_min} - ${props.plant.care_info.ideal_temp_max} °C`,
)
const idealAirMoisture = computed(
  () =>
    `${props.plant.care_info.ideal_air_moisture_min} - ${props.plant.care_info.ideal_air_moisture_max} %`,
)
const notes = computed(
  () =>
    props.plant.care_info.notes ||
    props.plant.species.description ||
    props.plant.species.common_name,
)

async function loadWateringEvents() {
  wateringEvents.value = await api.getWateringEvents(props.plant.id).catch(() => [])
}

onMounted(loadWateringEvents)
watch(() => props.plant.id, loadWateringEvents)
</script>

<template>
  <div class="overview-tab">
    <n-divider title-placement="left">Now</n-divider>
    <div class="stat-grid">
      <n-statistic label="Moisture" :value="plant.readings.moisture">
        <template #suffix>%</template>
      </n-statistic>
      <n-statistic label="Temp" :value="plant.readings.temperature">
        <template #suffix>°C</template>
      </n-statistic>
      <n-statistic label="Air moisture" :value="plant.readings.air_moisture">
        <template #suffix>%</template>
      </n-statistic>
      <n-statistic label="Last watered" :value="lastWateredLabel" />
    </div>

    <n-divider title-placement="left">Care info</n-divider>
    <n-descriptions :column="1" label-placement="left" bordered>
      <n-descriptions-item label="Ideal moisture">{{ idealMoisture }}</n-descriptions-item>
      <n-descriptions-item label="Ideal temp">{{ idealTemp }}</n-descriptions-item>
      <n-descriptions-item label="Ideal air moisture">{{ idealAirMoisture }}</n-descriptions-item>
      <n-descriptions-item label="Notes">{{ notes }}</n-descriptions-item>
    </n-descriptions>

    <div class="section-row">
      <n-divider title-placement="left" class="section-row__divider">Recent activity</n-divider>
    </div>
    <n-timeline v-if="latestWateringEvent" :reverse="true">
      <n-timeline-item
        title="Watered"
        color="#3b82f6"
        :time="new Date(latestWateringEvent.createdAt)"
      >
        Pump ran for {{ Math.round(latestWateringEvent.pumpDurationMs / 1000) }} seconds. Moisture
        before watering: {{ latestWateringEvent.moistureBefore ?? 'unknown' }}%.
      </n-timeline-item>
    </n-timeline>
    <n-empty v-else description="No watering events yet." size="small" />
  </div>
</template>

<style scoped>
.section-row {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.section-row__divider {
  flex: 1;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 0.75rem;
}
.stat-grid > .n-statistic {
  padding: 0.75rem 1rem;
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
}
</style>
