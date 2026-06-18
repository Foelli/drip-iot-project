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

import AppLogo from './AppLogo.vue'

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
    <AppLogo />

    <n-divider />

    <n-menu
      class="main-menu"
      :options="mainMenuOptions"
      :value="selectedKey"
      @update:value="handleMenuSelect"
      :outline="true"
    />

    <div class="sidebar-spacer" />

    <n-menu
      class="bottom-menu"
      :options="bottomMenuOptions"
      :value="selectedKey"
      @update:value="handleMenuSelect"
    />
  </n-layout-sider>
</template>

<style scoped>
.navbar {
  height: 100vh;
  overflow: hidden;

  --sidebar-x-padding: 0.75rem;
  /* outer horizontal inset shared by the logo, main menu and bottom menu so
     they all start/end at the same x */
  --sidebar-gutter: 8px;
  --sidebar-item-text-active: var(--color-accent);
  --sidebar-item-bg-hover: var(--color-surface-hover);
  --sidebar-item-bg-selected: var(--color-accent-soft);
}

/* Make the sider's content wrapper a full-height flex column so the spacer
   between the two menus can absorb all remaining vertical space. */
:deep(.n-layout-sider-scroll-container) {
  display: flex;
  flex-direction: column;
  height: 100%;
}

/* Kill naive's default 24px divider margins so the divider sits flush at the
   bottom of the 4.5rem brand row (removes the empty gap under the logo and
   aligns the line with the topbar's bottom edge). */
:deep(.n-divider:not(.n-divider--vertical)) {
  margin-top: 0;
  margin-bottom: 0;
}

/* The flexible gap that pushes main-menu to the top and bottom-menu to the base. */
.sidebar-spacer {
  flex: 1 1 auto;
}

/* ============================================================
   MENU SPACING
   Plain (non-:deep) rules — these target our own class names on
   the <n-menu> wrappers, so scoped styles reach them directly.
============================================================ */
.main-menu {
margin-top: auto;
}

/* bottom-menu sits at the base because .sidebar-spacer eats the free space above it */

/* BOTH MENUS — same horizontal gutter + inner padding so items line up */
:deep(.n-menu-item-content) {
  margin: 4px var(--sidebar-gutter);
  padding-left: var(--sidebar-x-padding) !important;
  border-radius: 8px;
  box-sizing: border-box;
  transition: color 150ms ease;
}

/* BOTH MENUS — hover/selected background spans the inset item box */
:deep(.n-menu-item-content::before) {
  left: 0;
  right: 0;
  border-radius: 8px;
  transition: background-color 150ms ease;
}

/* BOTH MENUS */
:deep(.n-menu-item-content:hover::before) {
  background-color: var(--sidebar-item-bg-hover);
}

/* BOTH MENUS */
:deep(.n-menu-item-content--selected::before) {
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
