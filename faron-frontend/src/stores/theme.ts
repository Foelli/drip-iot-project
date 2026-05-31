import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { useOsTheme } from 'naive-ui'

/**
 * Theme store — single source of truth for light/dark mode.
 *
 * - Persists the user's choice to localStorage.
 * - Seeds the initial value from the OS preference on first load.
 * - Keeps the <html> class in sync, so base.css `:root.dark` rules apply.
 * - App.vue reads `isDark` to pick naive-ui's theme.
 */
export const useThemeStore = defineStore('theme', () => {
  const STORAGE_KEY = 'drip-theme'
  const osTheme = useOsTheme() // 'light' | 'dark' | null

  // Read persisted choice; fall back to whatever the OS says now.
  const stored = localStorage.getItem(STORAGE_KEY)
  const initial: 'light' | 'dark' =
    stored === 'light' || stored === 'dark'
      ? stored
      : osTheme.value === 'dark'
        ? 'dark'
        : 'light'

  const mode = ref<'light' | 'dark'>(initial)
  const isDark = computed(() => mode.value === 'dark')

  function toggle() {
    mode.value = isDark.value ? 'light' : 'dark'
  }

  // Sync: <html> class controls CSS tokens, localStorage persists across reloads.
  watch(
    mode,
    (next) => {
      document.documentElement.classList.toggle('dark', next === 'dark')
      localStorage.setItem(STORAGE_KEY, next)
    },
    { immediate: true },
  )

  return { mode, isDark, toggle }
})
