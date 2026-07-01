<script setup lang="ts">
import { Add } from '@vicons/ionicons5'
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '@/api/client'
import { mapBackendPlantToPlant } from '@/api/mappers'
import PlantCard from '@/components/plants/PlantCard.vue'
import PlantDetail from '@/components/plants/PlantDetail.vue'
import type { Plant } from '@/types/Plant'

const onAddPlant = () => {
  // TODO: open add plant form
}

const route = useRoute()
const plants = ref<Plant[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const showDetail = ref(false)
const selectedPlant = ref<Plant | null>(null)

const hasPlants = computed(() => plants.value.length > 0)

function openDetail(plant: Plant) {
  selectedPlant.value = plant
  showDetail.value = true
}

function maybeOpenFromQuery() {
  const id = route.query.open
  if (typeof id !== 'string') return

  const target = plants.value.find((plant) => String(plant.id) === id)
  if (target) openDetail(target)
}

async function loadPlants() {
  loading.value = true
  error.value = null

  try {
    const backendPlants = await api.getPlants()

    plants.value = await Promise.all(
      backendPlants.map(async (backendPlant) => {
        const latestMeasurement = await api.getLatestMeasurement(backendPlant.id).catch(() => null)

        return mapBackendPlantToPlant(backendPlant, latestMeasurement)
      }),
    )

    if (selectedPlant.value) {
      selectedPlant.value =
        plants.value.find((plant) => plant.id === selectedPlant.value?.id) ?? null
    }

    maybeOpenFromQuery()
  } catch (err) {
    console.error(err)
    error.value = 'Could not load plants from the backend.'
  } finally {
    loading.value = false
  }
}

onMounted(loadPlants)
watch(() => route.query.open, maybeOpenFromQuery)
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

    <n-spin :show="loading">
      <n-alert v-if="error" type="error" :bordered="false">
        {{ error }}
      </n-alert>

      <n-empty v-else-if="!hasPlants" description="No plants found." />

      <div v-else class="plants-grid">
        <PlantCard
          v-for="plant in plants"
          :key="plant.id"
          :plant="plant"
          @click="openDetail(plant)"
        />
      </div>
    </n-spin>

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
  grid-template-columns: repeat(auto-fill, 16rem);
  gap: 1rem;
}
h2 {
  margin: 0;
}
</style>
