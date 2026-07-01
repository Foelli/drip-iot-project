<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, type WateringEvent } from '@/api/client'
import { mapBackendPlantToPlant } from '@/api/mappers'
import { OFFLINE_CONFIG, STATUS_CONFIG } from '@/components/plants/plantDisplay'
import type { Plant, PlantStatus } from '@/types/Plant'

defineOptions({ name: 'OverviewView' })

const router = useRouter()

// --- Greeting -----------------------------------------------------------------
function greetingFor(d: Date): string {
  const h = d.getHours()
  if (h < 5) return 'Good night'
  if (h < 12) return 'Good morning'
  if (h < 18) return 'Good afternoon'
  return 'Good evening'
}

const now = new Date()
const greeting = greetingFor(now)
const dateLabel = now.toLocaleDateString('en-US', {
  weekday: 'long',
  month: 'long',
  day: 'numeric',
})

const plants = ref<Plant[]>([])
const wateringEvents = ref<WateringEvent[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

type Severity = 'critical' | 'needs_attention' | 'offline'
interface CareItem {
  plant_id: number
  plant: string
  severity: Severity
  reason: string
}

function severityColor(s: Severity): string {
  return s === 'offline' ? OFFLINE_CONFIG.color : STATUS_CONFIG[s].color
}

function careReason(plant: Plant, severity: Severity): string {
  if (severity === 'offline') return 'Device has not reported yet'

  const threshold = plant.moistureThreshold ?? plant.settings.thresholds?.moisture_min
  const moisture = Math.round(plant.readings.moisture)

  if (threshold != null && moisture < threshold) {
    return `Soil moisture is ${moisture}% (target ${threshold}%+)`
  }

  return `${STATUS_CONFIG[plant.readings.status].label} based on latest reading`
}

const careItems = computed<CareItem[]>(() =>
  plants.value
    .map((plant) => {
      if (!plant.device.online) {
        return {
          plant_id: plant.id,
          plant: plant.custom_name,
          severity: 'offline',
          reason: careReason(plant, 'offline'),
        }
      }

      if (plant.readings.status === 'healthy') return null

      const severity = plant.readings.status
      return {
        plant_id: plant.id,
        plant: plant.custom_name,
        severity,
        reason: careReason(plant, severity),
      }
    })
    .filter((item): item is CareItem => item !== null),
)

const statusLine = computed(() => {
  if (loading.value) return 'Checking your plants now.'
  if (error.value) return 'Could not reach the plant backend.'

  const n = careItems.value.length
  if (n === 0) return 'All plants are doing well today.'
  return `${n} plant${n === 1 ? '' : 's'} need a quick look today.`
})

function openPlant(id: number) {
  router.push({ path: '/plants', query: { open: String(id) } })
}
function goToVitals() {
  router.push('/vitals')
}

const fleet = computed(() => {
  const statusCounts = plants.value.reduce(
    (counts, plant) => {
      counts[plant.readings.status] += 1
      return counts
    },
    { healthy: 0, needs_attention: 0, critical: 0 } satisfies Record<PlantStatus, number>,
  )

  return {
    healthy: statusCounts.healthy,
    attention: statusCounts.needs_attention,
    critical: statusCounts.critical,
    devices_online: plants.value.filter((plant) => plant.device.online).length,
    devices_total: plants.value.length,
  }
})

interface ActivityRow {
  plant_id: number
  plant: string
  body: string
  when: string
}

function relativeTime(value: string): string {
  const then = new Date(value).getTime()
  const diffMs = Date.now() - then
  const diffMinutes = Math.max(0, Math.round(diffMs / 60000))

  if (diffMinutes < 1) return 'now'
  if (diffMinutes < 60) return `${diffMinutes}m ago`

  const diffHours = Math.round(diffMinutes / 60)
  if (diffHours < 24) return `${diffHours}h ago`

  const diffDays = Math.round(diffHours / 24)
  return `${diffDays}d ago`
}

const plantNameById = computed(
  () => new Map(plants.value.map((plant) => [plant.id, plant.custom_name])),
)
const activity = computed<ActivityRow[]>(() =>
  [...wateringEvents.value]
    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
    .slice(0, 4)
    .map((event) => ({
      plant_id: event.plantId,
      plant: plantNameById.value.get(event.plantId) ?? `Plant ${event.plantId}`,
      body:
        event.moistureBefore == null
          ? `Watered for ${Math.round(event.pumpDurationMs / 1000)}s`
          : `Watered at ${Math.round(event.moistureBefore)}% moisture`,
      when: relativeTime(event.createdAt),
    })),
)

const activityAnchor = computed(() => activity.value[0]?.plant_id ?? plants.value[0]?.id ?? 1)

const weeklyWaterings = computed(() => {
  const days: number[] = Array.from({ length: 7 }, () => 0)
  const today = new Date()

  for (const event of wateringEvents.value) {
    const eventDate = new Date(event.createdAt)
    const diffDays = Math.floor(
      (Date.UTC(today.getFullYear(), today.getMonth(), today.getDate()) -
        Date.UTC(eventDate.getFullYear(), eventDate.getMonth(), eventDate.getDate())) /
        86400000,
    )

    if (diffDays >= 0 && diffDays < 7) {
      days[6 - diffDays] = (days[6 - diffDays] ?? 0) + 1
    }
  }

  return days
})
const weeklyTotal = computed(() => weeklyWaterings.value.reduce((a, b) => a + b, 0))
const weeklyMax = computed(() => Math.max(...weeklyWaterings.value, 1))
const dayLetters = ['M', 'T', 'W', 'T', 'F', 'S', 'S']

async function loadOverview() {
  loading.value = true
  error.value = null

  try {
    const backendPlants = await api.getPlants()
    const mappedPlants = await Promise.all(
      backendPlants.map(async (backendPlant) => {
        const latestMeasurement = await api.getLatestMeasurement(backendPlant.id).catch(() => null)

        return mapBackendPlantToPlant(backendPlant, latestMeasurement)
      }),
    )

    plants.value = mappedPlants

    const eventsByPlant = await Promise.all(
      mappedPlants.map((plant) => api.getWateringEvents(plant.id).catch(() => [])),
    )
    wateringEvents.value = eventsByPlant.flat()
  } catch (err) {
    console.error(err)
    error.value = 'Could not load overview from the backend.'
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<template>
  <section class="overview-view">
    <div class="hero">
      <h1>{{ greeting }}, Simon</h1>
      <p class="hero__date">{{ dateLabel }} · {{ statusLine }}</p>
    </div>

    <div class="overview-grid">
      <article class="tile tile--care">
        <header class="tile__header">
          <h2>Today ({{ careItems.length }})</h2>
          <n-button text size="small" @click="goToVitals">See all →</n-button>
        </header>
        <n-alert v-if="error" type="error" :bordered="false">
          {{ error }}
        </n-alert>
        <n-spin v-else :show="loading">
          <ul v-if="careItems.length > 0" class="care-list">
            <li v-for="c in careItems" :key="c.plant_id" class="care-item">
              <span class="care-dot" :style="{ background: severityColor(c.severity) }"></span>
              <div class="care-text">
                <div class="care-name">{{ c.plant }}</div>
                <div class="care-reason">{{ c.reason }}</div>
              </div>
              <n-button text size="small" @click="openPlant(c.plant_id)">Go →</n-button>
            </li>
          </ul>
          <n-empty v-else description="Nothing to do — every plant is happy." size="small" />
        </n-spin>
      </article>

      <!-- Fleet status ----------------------------------------------------- -->
      <article class="tile">
        <header class="tile__header">
          <h2>Fleet status</h2>
          <n-button text size="small" @click="goToVitals">View Vitals →</n-button>
        </header>
        <ul class="fleet-list">
          <li>
            <span class="fleet-num">{{ fleet.healthy }}</span>
            <span class="fleet-label">Healthy</span>
          </li>
          <li>
            <span class="fleet-num">{{ fleet.attention }}</span>
            <span class="fleet-label">Needs attention</span>
          </li>
          <li>
            <span class="fleet-num">{{ fleet.critical }}</span>
            <span class="fleet-label">Critical</span>
          </li>
          <li>
            <span class="fleet-num">
              {{ fleet.devices_online }}<span class="muted">/{{ fleet.devices_total }}</span>
            </span>
            <span class="fleet-label">Devices online</span>
          </li>
        </ul>
      </article>

      <!-- Recent activity -------------------------------------------------- -->
      <article class="tile">
        <header class="tile__header">
          <h2>Recent activity</h2>
          <n-button text size="small" @click="openPlant(activityAnchor)"> Open plant → </n-button>
        </header>
        <ul v-if="activity.length > 0" class="activity-list">
          <li
            v-for="(a, i) in activity"
            :key="i"
            class="activity-item"
            @click="openPlant(a.plant_id)"
          >
            <span class="activity-when">{{ a.when }}</span>
            <span class="activity-body">
              <strong>{{ a.plant }}</strong> · {{ a.body }}
            </span>
          </li>
        </ul>
        <n-empty v-else description="No watering activity yet." size="small" />
      </article>

      <!-- Watering this week ----------------------------------------------- -->
      <article class="tile">
        <header class="tile__header">
          <h2>Watering this week</h2>
          <n-button text size="small" @click="goToVitals">View Vitals →</n-button>
        </header>
        <p class="weekly-total">
          <strong>{{ weeklyTotal }}</strong> waterings · last 7 days
        </p>
        <div class="weekly-bars">
          <div v-for="(v, i) in weeklyWaterings" :key="i" class="weekly-bar-col">
            <div class="weekly-bar-bg">
              <div class="weekly-bar" :style="{ height: (v / weeklyMax) * 100 + '%' }"></div>
            </div>
            <span class="weekly-label">{{ dayLetters[i] }}</span>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<style scoped>
.hero {
  margin-bottom: 2rem;
}
.hero h1 {
  margin: 0;
}
.hero__date {
  color: var(--color-text-muted);
  margin: 0.25rem 0 0;
}

.overview-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.tile {
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: 1rem 1.25rem;
}
.tile__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 0.75rem;
}
.tile__header h2 {
  margin: 0;
  font-size: 1rem;
}

/* --- Care queue -------------------------------------------------------- */
.care-list,
.fleet-list,
.activity-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.care-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border-subtle);
}
.care-item:last-child {
  border-bottom: none;
}
.care-dot {
  flex: 0 0 0.625rem;
  width: 0.625rem;
  height: 0.625rem;
  border-radius: 50%;
}
.care-text {
  flex: 1;
  min-width: 0;
}
.care-name {
  font-weight: 500;
  font-size: 0.9rem;
}
.care-reason {
  font-size: 0.8125rem;
  color: var(--color-text-muted);
}

/* --- Fleet list -------------------------------------------------------- */
.fleet-list li {
  display: grid;
  grid-template-columns: 3rem 1fr auto;
  align-items: baseline;
  gap: 0.75rem;
  padding: 0.35rem 0;
}
.fleet-num {
  font-size: 1.5rem;
  font-weight: 600;
  font-variant-numeric: tabular-nums;
  text-align: right;
}
.fleet-label {
  font-size: 0.875rem;
}
.delta {
  font-size: 0.75rem;
  font-weight: 600;
}
.delta--good {
  color: var(--success);
}
.delta--bad {
  color: var(--danger);
}
.muted {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

/* --- Activity ---------------------------------------------------------- */
.activity-item {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
  padding: 0.4rem 0;
  font-size: 0.875rem;
  cursor: pointer;
  border-bottom: 1px solid var(--color-border-subtle);
}
.activity-item:last-child {
  border-bottom: none;
}
.activity-item:hover {
  color: var(--success);
}
.activity-when {
  flex: 0 0 5rem;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  font-variant-numeric: tabular-nums;
}
.activity-body {
  flex: 1;
  min-width: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* --- Watering ---------------------------------------------------------- */
.weekly-total {
  margin: 0 0 0.5rem;
  font-size: 0.875rem;
  color: var(--color-text-muted);
}
.weekly-total strong {
  color: inherit;
  font-size: 1.25rem;
  font-variant-numeric: tabular-nums;
  margin-right: 0.25rem;
}
.weekly-bars {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 0.5rem;
  height: 6rem;
}
.weekly-bar-col {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0.25rem;
  height: 100%;
}
.weekly-bar-bg {
  flex: 1;
  display: flex;
  align-items: flex-end;
}
.weekly-bar {
  width: 100%;
  background: #3b82f6;
  border-radius: 2px 2px 0 0;
  min-height: 2px;
}
.weekly-label {
  text-align: center;
  font-size: 0.75rem;
  color: var(--color-text-muted);
}
</style>
