<script setup lang="ts">
import { computed } from 'vue'
import type { Plant } from '@/types/Plant'
import { LIGHT_CONFIG } from '../plantDisplay'

const props = defineProps<{ plant: Plant }>()

const lightConfig = computed(() => LIGHT_CONFIG[props.plant.readings.light])
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
      <n-statistic label="Light">
        <template #prefix>
          <n-icon><component :is="lightConfig.icon" /></n-icon>
        </template>
        {{ lightConfig.label }}
      </n-statistic>
      <n-statistic label="Last watered" value="2 days ago" />
    </div>

    <n-divider title-placement="left">Care info</n-divider>
    <n-descriptions :column="1" label-placement="left" bordered>
      <n-descriptions-item label="Water every">~ 7 days</n-descriptions-item>
      <n-descriptions-item label="Ideal moisture">50 – 70 %</n-descriptions-item>
      <n-descriptions-item label="Ideal temp">18 – 26 °C</n-descriptions-item>
      <n-descriptions-item label="Light">Bright indirect</n-descriptions-item>
      <n-descriptions-item label="Notes">Loves humidity, avoid cold drafts.</n-descriptions-item>
    </n-descriptions>

    <div class="section-row">
      <n-divider title-placement="left" class="section-row__divider">Recent activity</n-divider>
      <n-button text size="small">Journal ›</n-button>
    </div>
    <n-timeline :reverse="true">
      <n-timeline-item title="Watered" color="#3b82f6" :time="new Date('2024-06-01T10:00:00')">
        Gave it 500ml of water.
      </n-timeline-item>
      <n-timeline-item title="Moved" color="var(--warning)" :time="new Date('2024-05-28T14:30:00')">
        Relocated to the living room for better light.
      </n-timeline-item>
      <n-timeline-item
        title="Device offline"
        color="var(--neutral-400)"
        :time="new Date('2024-05-20T09:15:00')"
      >
        The Pico went offline for ~2 hours, likely due to a Wi-Fi issue.
      </n-timeline-item>
    </n-timeline>
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
