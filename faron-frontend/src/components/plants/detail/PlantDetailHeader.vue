<script setup lang="ts">
import { computed } from 'vue'
import { Camera, Location } from '@vicons/ionicons5'
import type { Plant } from '@/types/Plant'
import { STATUS_CONFIG } from '../plantDisplay'

const props = defineProps<{ plant: Plant }>()

const statusConfig = computed(() => STATUS_CONFIG[props.plant.readings.status])

// Device chip: shows the Pico's name in green when online, "Offline" in red otherwise.
const deviceConfig = computed(() =>
  props.plant.device.online
    ? { label: props.plant.device.name, color: 'var(--success)' }
    : { label: 'Offline', color: 'var(--danger)' },
)
</script>

<template>
  <div class="plant-detail-header">
    <n-upload :action="null" :show-file-list="false" class="avatar-upload">
      <div class="avatar-edit" title="Change photo">
        <n-avatar :size="150" src="https://picsum.photos/600" />
        <span class="avatar-edit__badge">
          <n-icon :size="16"><Camera /></n-icon>
        </span>
      </div>
    </n-upload>
    <div class="plant-detail-header__info">
      <div class="plant-detail-header__title">
        <n-h2>{{ plant.species.common_name }}</n-h2>
        <n-text>{{ plant.species.scientific_name.join(', ') }}</n-text>
      </div>
      <n-flex>
        <n-tag round :bordered="false">
          <template #icon>
            <n-icon :color="statusConfig.color">
              <component :is="statusConfig.icon" />
            </n-icon>
          </template>
          {{ statusConfig.label }}
        </n-tag>
        <n-tag round :bordered="false">
          <template #icon>
            <n-icon><Location /></n-icon>
          </template>
          {{ plant.room }}
        </n-tag>
        <n-tag round :bordered="false" :color="{ textColor: deviceConfig.color }" class="device-tag">
          {{ deviceConfig.label }}
        </n-tag>
      </n-flex>
    </div>
  </div>
</template>

<style scoped>
.plant-detail-header {
  display: flex;
  align-items: stretch;
  gap: 1rem;
}

.plant-detail-header__info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.plant-detail-header__title {
  display: flex;
  flex-direction: column;
  gap: 0.125rem;
}

.plant-detail-header__title h2 {
  margin-bottom: 0;
  font-size: 1.75rem;
}

.plant-detail-header__title .n-text {
  font-size: 1rem;
}

.device-tag {
  font-weight: 600;
}

/* keep the upload sized to the avatar instead of stretching in the flex row */
.avatar-upload {
  width: auto;
  flex: 0 0 auto;
}
.avatar-upload :deep(.n-upload-trigger) {
  width: auto;
}

.avatar-edit {
  position: relative;
  display: inline-flex;
  cursor: pointer;
  border-radius: 50%;
}
.avatar-edit :deep(.n-avatar) {
  transition: filter 0.15s ease;
}
.avatar-edit:hover :deep(.n-avatar) {
  filter: brightness(0.85);
}

.avatar-edit__badge {
  position: absolute;
  right: 6px;
  bottom: 6px;
  display: grid;
  place-items: center;
  width: 34px;
  height: 34px;
  border-radius: 50%;
  background: var(--color-accent);
  color: #fff;
  /* ring matches the surface so the badge reads as a separate chip */
  box-shadow: 0 0 0 3px var(--color-surface);
  transition: transform 0.12s ease;
}
.avatar-edit:hover .avatar-edit__badge {
  transform: scale(1.08);
}
</style>
