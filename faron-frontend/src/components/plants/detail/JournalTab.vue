<script setup lang="ts">
import type { Plant } from '@/types/Plant'

defineProps<{ plant: Plant }>()
</script>

<template>
  <div class="journal-tab">
    <n-empty description="Journal coming soon" />
  </div>
</template>

<style scoped>
.journal-tab {
  padding: 2rem 0;
}
</style>
