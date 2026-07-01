<script setup lang="ts">
import { computed, onMounted, onUnmounted } from 'vue'
import { darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import AppShell from './layout/AppShell.vue'
import { useThemeStore } from './stores/theme'
import { notifyNewWateringEvents } from './services/discordNotifications'

const themeStore = useThemeStore()
const theme = computed(() => (themeStore.isDark ? darkTheme : null))

const themeOverrides: GlobalThemeOverrides = {
  common: {
    primaryColor: '#16a34a', // accent-600
    primaryColorHover: '#15803d', // accent-700
    primaryColorPressed: '#166534', // accent-800
    primaryColorSuppl: '#22c55e', // accent-500
  },
}

const WATERING_NOTIFICATION_PLANT_ID = 1
const WATERING_NOTIFICATION_POLL_MS = 30_000
let wateringNotificationTimer: number | undefined

async function pollWateringNotifications() {
  try {
    await notifyNewWateringEvents(WATERING_NOTIFICATION_PLANT_ID)
  } catch (error) {
    console.error('Could not process watering notifications', error)
  }
}

onMounted(() => {
  void pollWateringNotifications()
  wateringNotificationTimer = window.setInterval(
    pollWateringNotifications,
    WATERING_NOTIFICATION_POLL_MS,
  )
})

onUnmounted(() => {
  if (wateringNotificationTimer != null) {
    window.clearInterval(wateringNotificationTimer)
  }
})
</script>

<template>
  <n-config-provider :theme="theme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <n-notification-provider>
        <AppShell />
      </n-notification-provider>
    </n-message-provider>
  </n-config-provider>
</template>
