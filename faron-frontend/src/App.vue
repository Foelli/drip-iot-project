<script setup lang="ts">
import { computed } from 'vue'
import { darkTheme, type GlobalThemeOverrides } from 'naive-ui'
import AppShell from './layout/AppShell.vue'
import { useThemeStore } from './stores/theme'

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
</script>

<template>
  <n-config-provider :theme="theme" :theme-overrides="themeOverrides">
    <n-message-provider>
      <AppShell />
    </n-message-provider>
  </n-config-provider>
</template>
