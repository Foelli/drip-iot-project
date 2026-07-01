<script setup lang="ts">
import { Add } from '@vicons/ionicons5'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useMessage } from 'naive-ui'
import { api } from '@/api/client'
import { mapBackendPlantToPlant } from '@/api/mappers'
import PlantCard from '@/components/plants/PlantCard.vue'
import PlantDetail from '@/components/plants/PlantDetail.vue'
import type { Plant } from '@/types/Plant'

const message = useMessage()
const route = useRoute()
const plants = ref<Plant[]>([])
const loading = ref(false)
const error = ref<string | null>(null)
const showDetail = ref(false)
const selectedPlant = ref<Plant | null>(null)
const showAddPlant = ref(false)
const creatingPlant = ref(false)

const addPlantForm = reactive({
  commonName: '',
  scientificName: '',
  customName: '',
  notes: '',
})

const hasPlants = computed(() => plants.value.length > 0)
const canCreatePlant = computed(() => addPlantForm.commonName.trim().length > 0)

function openDetail(plant: Plant) {
  selectedPlant.value = plant
  showDetail.value = true
}

function openAddPlant() {
  showAddPlant.value = true
}

function resetAddPlantForm() {
  addPlantForm.commonName = ''
  addPlantForm.scientificName = ''
  addPlantForm.customName = ''
  addPlantForm.notes = ''
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

async function createPlant() {
  if (!canCreatePlant.value) return

  creatingPlant.value = true
  try {
    const commonName = addPlantForm.commonName.trim()
    const scientificName = addPlantForm.scientificName.trim()
    const customName = addPlantForm.customName.trim()
    const notes = addPlantForm.notes.trim()
    const careInfo = await api.generatePlantCareInfo({
      commonName,
      scientificName: scientificName || null,
      notes: notes || null,
    })

    await api.createPlant({
      apiId: 0,
      commonName,
      scientificName: scientificName || null,
      customName: customName || commonName,
      description: notes || null,
      wateringEnabled: true,
      moistureThreshold: careInfo.idealMoistureMin,
      pumpDurationMs: 2000,
      waterSettleMs: 20000,
      idealMoistureMin: careInfo.idealMoistureMin,
      idealMoistureMax: careInfo.idealMoistureMax,
      idealTempMin: careInfo.idealTempMin,
      idealTempMax: careInfo.idealTempMax,
      idealAirMoistureMin: careInfo.idealAirMoistureMin,
      idealAirMoistureMax: careInfo.idealAirMoistureMax,
      careNotes: careInfo.careNotes,
    })

    message.success('Plant created with generated care info')
    showAddPlant.value = false
    resetAddPlantForm()
    await loadPlants()
  } catch (err) {
    console.error(err)
    message.error('Could not create plant or generate care info')
  } finally {
    creatingPlant.value = false
  }
}

onMounted(loadPlants)
watch(() => route.query.open, maybeOpenFromQuery)
</script>

<template>
  <section class="plants-view">
    <div class="view-header">
      <h1>Plants</h1>
      <n-button type="primary" @click="openAddPlant">
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

    <n-drawer v-model:show="showAddPlant" width="34rem" placement="right">
      <n-drawer-content title="Add Plant" closable>
        <n-form label-placement="top" class="add-plant-form">
          <n-form-item label="Plant name">
            <n-input
              v-model:value="addPlantForm.commonName"
              placeholder="Monstera, basil, snake plant..."
              :disabled="creatingPlant"
            />
          </n-form-item>
          <n-form-item label="Scientific name">
            <n-input
              v-model:value="addPlantForm.scientificName"
              placeholder="Optional"
              :disabled="creatingPlant"
            />
          </n-form-item>
          <n-form-item label="Display name">
            <n-input
              v-model:value="addPlantForm.customName"
              placeholder="Optional"
              :disabled="creatingPlant"
            />
          </n-form-item>
          <n-form-item label="Care notes">
            <n-input
              v-model:value="addPlantForm.notes"
              type="textarea"
              placeholder="Optional context for care guidance"
              :autosize="{ minRows: 3, maxRows: 5 }"
              :disabled="creatingPlant"
            />
          </n-form-item>
          <n-button
            type="primary"
            :loading="creatingPlant"
            :disabled="!canCreatePlant"
            @click="createPlant"
          >
            Generate care info & create
          </n-button>
        </n-form>
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
.add-plant-form {
  max-width: 28rem;
}
</style>
