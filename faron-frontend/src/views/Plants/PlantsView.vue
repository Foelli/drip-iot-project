<script setup lang="ts">
import { Add } from '@vicons/ionicons5'
import { onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import PlantCard from '@/components/plants/PlantCard.vue'
import PlantDetail from '@/components/plants/PlantDetail.vue'
import type { Plant, PlantLight, PlantSettings } from '@/types/Plant'

const onAddPlant = () => {
  // TODO: open add plant form
}

const showDetail = ref(false)
const selectedPlant = ref<Plant | null>(null)

function openDetail(plant: Plant) {
  selectedPlant.value = plant
  showDetail.value = true
}

// Allow other views to deep-link into a plant via `/plants?open=<id>` (e.g. the
// Overview's care queue rows). We watch the query so back/forward navigation
// also re-opens the right drawer.
const route = useRoute()
function maybeOpenFromQuery() {
  const id = route.query.open
  if (typeof id !== 'string') return
  const target = plants.value.find((p) => String(p.id) === id)
  if (target) openDetail(target)
}
onMounted(maybeOpenFromQuery)
watch(() => route.query.open, maybeOpenFromQuery)

// Shared automation defaults so each mock plant only specifies its thresholds.
function makeSettings(
  lightTarget: PlantLight,
  waterEveryDays: number,
  thresholds: { moisture_min: number; moisture_max: number; temp_min: number; temp_max: number },
): PlantSettings {
  return {
    thresholds: { ...thresholds, light_target: lightTarget, water_every_days: waterEveryDays },
    automation: {
      auto_water: true,
      pump_duration_s: 8,
      cooldown_h: 6,
      quiet_hours: { enabled: true, from: '22:00', to: '07:00' },
      notifications: false,
    },
  }
}

// Varied mock values so the temperature gradient is visible across the grid
// (cold blue → cool green → warm yellow → hot red) without editing yet.
// Edit these in Vue DevTools to live-update individual cards.
const plants = ref<Plant[]>([
  {
    id: 1,
    custom_name: 'My Fiddle Leaf Fig',
    room: 'Living Room',
    photo_url: null,
    species: {
      id: 1,
      common_name: 'Fiddle Leaf Fig',
      scientific_name: ['Ficus lyrata'],
      family: 'Moraceae',
      watering: 'Average',
      watering_general_benchmark: { value: '7-10', unit: 'days' },
      sunlight: ['part shade', 'bright indirect'],
      soil: ['Well-drained'],
      care_level: 'Medium',
      maintenance: 'Moderate',
      description: 'Loves humidity, avoid cold drafts.',
      default_image: null,
    },
    device: { name: 'Pico-01', online: true, firmware: 'v0.4.1', last_seen: '2026-05-28T12:00:00Z' },
    readings: { moisture: 62, temperature: 22, light: 'high', status: 'healthy' },
    settings: makeSettings('high', 8, { moisture_min: 40, moisture_max: 70, temp_min: 18, temp_max: 26 }),
  },
  {
    id: 2,
    custom_name: 'My Snake Plant',
    room: 'Bedroom',
    photo_url: null,
    species: {
      id: 2,
      common_name: 'Snake Plant',
      scientific_name: ['Sansevieria trifasciata'],
      family: 'Asparagaceae',
      watering: 'Minimum',
      watering_general_benchmark: { value: '14-21', unit: 'days' },
      sunlight: ['full shade', 'part shade'],
      soil: ['Sandy', 'Well-drained'],
      care_level: 'Easy',
      maintenance: 'Low',
      description: 'Extremely tolerant of neglect; let the soil dry out fully.',
      default_image: null,
    },
    device: { name: 'Pico-02', online: true, firmware: 'v0.4.1', last_seen: '2026-05-28T11:58:00Z' },
    readings: { moisture: 28, temperature: 8, light: 'low', status: 'needs_attention' },
    settings: makeSettings('low', 18, { moisture_min: 15, moisture_max: 40, temp_min: 15, temp_max: 27 }),
  },
  {
    id: 3,
    custom_name: 'My Spider Plant',
    room: 'Kitchen',
    photo_url: null,
    species: {
      id: 3,
      common_name: 'Spider Plant',
      scientific_name: ['Chlorophytum comosum'],
      family: 'Asparagaceae',
      watering: 'Average',
      watering_general_benchmark: { value: '5-7', unit: 'days' },
      sunlight: ['part shade', 'bright indirect'],
      soil: ['Well-drained'],
      care_level: 'Easy',
      maintenance: 'Low',
      description: 'Fast-growing and forgiving; produces plantlets on long stems.',
      default_image: null,
    },
    device: { name: 'Pico-03', online: false, firmware: 'v0.3.8', last_seen: '2026-05-27T19:02:00Z' },
    readings: { moisture: 81, temperature: 30, light: 'medium', status: 'needs_attention' },
    settings: makeSettings('medium', 6, { moisture_min: 45, moisture_max: 75, temp_min: 16, temp_max: 28 }),
  },
  {
    id: 4,
    custom_name: 'My Monstera',
    room: 'Living Room',
    photo_url: null,
    species: {
      id: 4,
      common_name: 'Monstera Deliciosa',
      scientific_name: ['Monstera deliciosa'],
      family: 'Araceae',
      watering: 'Average',
      watering_general_benchmark: { value: '7-10', unit: 'days' },
      sunlight: ['part shade', 'bright indirect'],
      soil: ['Peaty', 'Well-drained'],
      care_level: 'Medium',
      maintenance: 'Moderate',
      description: 'Likes to climb; provide a moss pole and bright indirect light.',
      default_image: null,
    },
    device: { name: 'Pico-04', online: true, firmware: 'v0.4.1', last_seen: '2026-05-28T12:01:00Z' },
    readings: { moisture: 45, temperature: 36, light: 'high', status: 'critical' },
    settings: makeSettings('high', 8, { moisture_min: 40, moisture_max: 70, temp_min: 18, temp_max: 27 }),
  },
])
</script>

<template>
  <section class="plants-view">
    <div class="view-header">
      <h1>Plants</h1>
      <n-button type="primary" @click="onAddPlant">
        <template #icon>
          <n-icon><Add /></n-icon>
        </template>
        Add Plant
      </n-button>
    </div>
    <div class="plants-grid">
      <PlantCard
        v-for="plant in plants"
        :key="plant.id"
        :plant="plant"
        @click="openDetail(plant)"
      />
    </div>

    <n-drawer v-model:show="showDetail" width="55%" placement="right">
      <n-drawer-content :title="selectedPlant?.custom_name" closable>
        <PlantDetail v-if="selectedPlant" :plant="selectedPlant" />
      </n-drawer-content>
    </n-drawer>
  </section>
</template>

<style scoped>
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3rem;
}
.plants-grid {
  display: grid;
  /* fixed-width cards; auto-fill drops columns as space shrinks */
  grid-template-columns: repeat(auto-fill, 16rem);
  gap: 1rem;
}
h2 {
  margin: 0;
}
</style>
