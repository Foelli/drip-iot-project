<script setup lang="ts">
import type { Plant } from '@/types/Plant'

defineProps<{ plant: Plant }>()
</script>

<template>
  <div class="vitals-tab">
    <n-empty description="Vitals charts coming soon" />
  </div>
</template>

<style scoped>
.vitals-tab {
  padding: 2rem 0;
}
</style>
