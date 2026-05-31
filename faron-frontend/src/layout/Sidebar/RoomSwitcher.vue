<script setup lang="ts">
defineOptions({
  name: 'RoomSwitcher',
})

import { ref } from 'vue'
import { Home } from '@vicons/ionicons5'

enum Room {
  LivingRoom = 'living-room',
  Bedroom = 'bedroom',
  Kitchen = 'kitchen',
}

const selectedRoom = ref<Room>(Room.LivingRoom)

const roomOptions = [
  {
    label: 'Living Room',
    value: Room.LivingRoom,
  },
  {
    label: 'Bedroom',
    value: Room.Bedroom,
  },
  {
    label: 'Kitchen',
    value: Room.Kitchen,
  },
]
</script>

<template>
  <div class="room-switcher">
    <n-icon class="home-icon">
      <Home />
    </n-icon>

    <n-select class="dropdown" v-model:value="selectedRoom" :options="roomOptions" />
  </div>
</template>

<style scoped>
.room-switcher {
  margin: 1rem;
  padding: 1rem var(--sidebar-x-padding);
  display: flex;
  align-items: center;
  gap: 0.5rem;

  background: var(--color-surface-elevated);
  border: 1px solid var(--color-border-subtle);
  border-radius: 0.5rem;
  box-shadow: var(--shadow-card);
  transition: border-color 150ms ease, box-shadow 150ms ease;
}

.room-switcher:hover {
  border-color: var(--color-border);
}

.home-icon {
  font-size: 1.5rem;
  color: var(--color-text-muted);
}

.dropdown {
  flex: 1;
}

/* strip n-select's default chrome so it inherits the card surface
   instead of painting its own (blue-tinted) background and border */
.room-switcher :deep(.n-base-selection),
.room-switcher :deep(.n-base-selection .n-base-selection-label) {
  background-color: transparent;
}

.room-switcher :deep(.n-base-selection .n-base-selection__border),
.room-switcher :deep(.n-base-selection .n-base-selection__state-border) {
  border: none;
  box-shadow: none;
}

/* in dark mode, naive-ui's default label color reads dim against our
   neutral-900 surface — lift the always-visible selected value to
   match body text. Multiple selectors cover non-filterable, filterable,
   and placeholder states. */
@media (prefers-color-scheme: dark) {
  .room-switcher :deep(.n-base-selection),
  .room-switcher :deep(.n-base-selection-input),
  .room-switcher :deep(.n-base-selection-label),
  .room-switcher :deep(.n-base-selection-label__input),
  .room-switcher :deep(.n-base-selection-placeholder) {
    color: var(--color-text);
  }
}
</style>
