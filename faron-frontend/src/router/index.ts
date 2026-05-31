import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import OverviewView from '../views/OverviewView.vue'
import PlantsView from '../views/Plants/PlantsView.vue'
import SettingsView from '../views/SettingsView.vue'
import VitalsView from '../views/VitalsView.vue'
import AboutView from '../views/AboutView.vue'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'overview', component: OverviewView },
  { path: '/plants', name: 'plants', component: PlantsView },
  { path: '/vitals', name: 'vitals', component: VitalsView },
  { path: '/settings', name: 'settings', component: SettingsView },
  { path: '/about', name: 'about', component: AboutView },
]
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
})

export default router
