<script setup lang="ts">
defineOptions({
  name: 'AppSidebar',
})

import { computed, h, type Component } from 'vue'
import type { MenuOption } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import { NIcon } from 'naive-ui'

import {
  Grid,
  GridOutline,
  Leaf,
  LeafOutline,
  Settings,
  SettingsOutline,
  InformationCircle,
  InformationCircleOutline,
  BarChart,
  BarChartOutline,
} from '@vicons/ionicons5'

import RoomSwitcher from './RoomSwitcher.vue'

const router = useRouter()
const route = useRoute()

const selectedKey = computed(() => route.path)

const mainMenuOptions = computed<MenuOption[]>(() => [
  {
    label: 'Overview',
    key: '/',
    icon: renderIcon('/', Grid, GridOutline),
  },
  {
    label: 'Plants',
    key: '/plants',
    icon: renderIcon('/plants', Leaf, LeafOutline),
  },
  {
    label: 'Vitals',
    key: '/vitals',
    icon: renderIcon('/vitals', BarChart, BarChartOutline),
  },
])

const bottomMenuOptions = computed<MenuOption[]>(() => [
  {
    label: 'Settings',
    key: '/settings',
    icon: renderIcon('/settings', Settings, SettingsOutline),
  },
  {
    label: 'About',
    key: '/about',
    icon: renderIcon('/about', InformationCircle, InformationCircleOutline),
  },
])

function handleMenuSelect(key: string) {
  router.push(key)
}

function renderIcon(key: string, activeIcon: Component, inactiveIcon: Component) {
  return () =>
    h(NIcon, null, {
      default: () => h(selectedKey.value === key ? activeIcon : inactiveIcon),
    })
}
</script>

<template>
  <n-layout-sider
    class="navbar"
    bordered
    :width="240"
    collapsible
    :collapsed-width="50"
    :collapsed="false"
  >
    <room-switcher />

    <n-divider />

    <n-menu
      class="main-menu"
      :options="mainMenuOptions"
      :value="selectedKey"
      @update:value="handleMenuSelect"
      :outline="true"
    />

    <n-menu
      class="bottom-menu"
      :options="bottomMenuOptions"
      :value="selectedKey"
      @update:value="handleMenuSelect"
    />
    <n-divider />
  </n-layout-sider>
</template>

<style scoped>
.navbar {
  height: 100vh;
  overflow: hidden;

  --sidebar-x-padding: 0.75rem;
  --sidebar-item-text-active: var(--color-accent);
  --sidebar-item-bg-hover: var(--color-surface-hover);
  --sidebar-item-bg-selected: var(--color-accent-soft);
}

/* ============================================================
   MENU SPACING
   Plain (non-:deep) rules — these target our own class names on
   the <n-menu> wrappers, so scoped styles reach them directly.
============================================================ */
.main-menu {
  margin-top: 1rem;
}

.bottom-menu {
  /* push the bottom menu toward the base of the sidebar */
  margin-top: clamp(2rem, calc(300vh - 28rem), 25rem);
  margin-bottom: 1rem;
}

/* MAIN MENU */
:deep(.main-menu .n-menu-item-content) {
  margin: 4px 8px;
  padding-left: var(--sidebar-x-padding) !important;
  border-radius: 8px;
  box-sizing: border-box;
  transition: color 150ms ease;
}

/* MAIN MENU */
:deep(.main-menu .n-menu-item-content::before) {
  left: 0;
  right: 0;
  border-radius: 8px;
  transition: background-color 150ms ease;
}

/* MAIN MENU */
:deep(.main-menu .n-menu-item-content:hover::before) {
  background-color: var(--sidebar-item-bg-hover);
}

/* MAIN MENU */
:deep(.main-menu .n-menu-item-content--selected::before) {
  background-color: var(--sidebar-item-bg-selected);
}

/* SELECTED STATE (BOTH MENUS) */
:deep(.n-menu-item-content--selected),
:deep(.n-menu-item-content--selected .n-icon) {
  color: var(--sidebar-item-text-active);
}

/* BOTTOM MENU */
:deep(.bottom-menu .n-menu-item-content-header),
:deep(.bottom-menu .n-menu-item-content .n-icon) {
  color: var(--neutral-400);
}
</style>
