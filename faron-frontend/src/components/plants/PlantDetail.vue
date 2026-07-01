<script setup lang="ts">
import type { Plant } from '@/types/Plant'
import PlantDetailHeader from './detail/PlantDetailHeader.vue'
import OverviewTab from './detail/OverviewTab.vue'
import VitalsTab from './detail/VitalsTab.vue'
import SettingsTab from './detail/SettingsTab.vue'

defineProps<{ plant: Plant }>()
</script>

<template>
  <section class="plant-detail">
    <PlantDetailHeader :plant="plant" />

    <n-tabs class="plant-tabs" type="line" animated default-value="overview">
      <n-tab-pane name="overview" tab="Overview">
        <OverviewTab :plant="plant" />
      </n-tab-pane>
      <n-tab-pane name="vitals" tab="Vitals">
        <VitalsTab :plant="plant" />
      </n-tab-pane>
      <n-tab-pane name="settings" tab="Settings">
        <SettingsTab :plant="plant" />
      </n-tab-pane>
    </n-tabs>
  </section>
</template>

<style scoped>
.plant-detail {
  padding: 1rem;
}

.plant-tabs {
  margin-top: 1.5rem;
}

/* unified across every tab: section-divider rhythm + larger section-label text */
.plant-tabs :deep(.n-divider:not(.n-divider--vertical)) {
  margin-top: 1.5rem;
  margin-bottom: 1rem;
}
.plant-tabs :deep(.n-divider__title) {
  font-size: 1.125rem;
}
</style>
