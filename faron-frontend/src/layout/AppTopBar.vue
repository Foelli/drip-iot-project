<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useMessage, useNotification } from 'naive-ui'
import { CalendarOutline, NotificationsOutline, Moon, MoonOutline } from '@vicons/ionicons5'
import { useThemeStore } from '@/stores/theme'

const route = useRoute()
const router = useRouter()

const themeStore = useThemeStore()
const toggleTheme = () => themeStore.toggle()

const hasUnread = ref(true)

const sectionLabels: Record<string, string> = {
  '/': 'Overview',
  '/plants': 'Plants',
  '/vitals': 'Vitals',
  '/settings': 'Settings',
  '/about': 'About',
}

const bottomMenuPaths = new Set(['/settings', '/about'])

const currentLabel = computed(() => sectionLabels[route.path] ?? 'Unknown')
const isHome = computed(() => route.path === '/')
const isBottomMenu = computed(() => bottomMenuPaths.has(route.path))
const showOverviewCrumb = computed(() => !isHome.value && !isBottomMenu.value)

const message = useMessage()
const notification = useNotification()
const handleNotificationClick = () => {
  if (hasUnread.value) {
    notification.info({
      title: 'New Notification',
      content: 'You have new notifications to check out.',
    })
    hasUnread.value = false
  } else {
    message.info('No new notifications')
  }
}
</script>

<template>
  <div class="app-top-bar">
    <n-breadcrumb>
      <n-breadcrumb-item>DRIP Dashboard</n-breadcrumb-item>
      <n-breadcrumb-item v-if="showOverviewCrumb" @click="router.push('/')"
        >Overview</n-breadcrumb-item
      >
      <n-breadcrumb-item>{{ currentLabel }}</n-breadcrumb-item>
    </n-breadcrumb>
    <div class="topbar-actions">
      <!-- Date pill -->
      <n-button size="large" quaternary :focusable="false">
        <template #icon>
          <n-icon><CalendarOutline /></n-icon>
        </template>
        {{ new Date().toLocaleDateString(undefined, { month: 'short', day: 'numeric' }) }}
      </n-button>

      <!-- Notifications -->
      <n-badge dot :show="hasUnread">
        <n-button size="large" circle @click="handleNotificationClick">
          <n-icon><NotificationsOutline /></n-icon>
        </n-button>
      </n-badge>

      <!-- Theme toggle: filled Moon in light mode, outlined in dark mode -->
      <n-button size="large" circle @click="toggleTheme">
        <n-icon>
          <MoonOutline v-if="!themeStore.isDark" />
          <Moon v-else />
        </n-icon>
      </n-button>
    </div>
  </div>
</template>

<style>
.app-top-bar {
  display: flex;
  align-items: center;
  height: 4.5rem;
  padding: 0 1.5rem;
  box-shadow: var(--shadow-card);
}

/* bump breadcrumb text up one notch (naive's default is ~14px) */
.app-top-bar .n-breadcrumb {
  font-size: 1rem;
}

.topbar-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
</style>
