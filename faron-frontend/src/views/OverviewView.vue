<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { OFFLINE_CONFIG, STATUS_CONFIG } from '@/components/plants/plantDisplay'

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

// --- Care queue (mock) -------------------------------------------------------
type Severity = 'critical' | 'needs_attention' | 'offline'
interface CareItem {
  plant_id: number
  plant: string
  severity: Severity
  reason: string
}

const careItems: CareItem[] = [
  {
    plant_id: 4,
    plant: 'My Monstera',
    severity: 'critical',
    reason: 'Soil too dry (28%) — water now',
  },
  {
    plant_id: 2,
    plant: 'My Snake Plant',
    severity: 'needs_attention',
    reason: 'Too cold (8°C) — move out of draft',
  },
  {
    plant_id: 3,
    plant: 'My Spider Plant',
    severity: 'offline',
    reason: 'Device offline 3 h — re-pair',
  },
]

function severityColor(s: Severity): string {
  return s === 'offline' ? OFFLINE_CONFIG.color : STATUS_CONFIG[s].color
}

const statusLine = computed(() => {
  const n = careItems.length
  if (n === 0) return 'All plants are doing well today.'
  return `${n} plant${n === 1 ? '' : 's'} need a quick look today.`
})

function openPlant(id: number) {
  router.push({ path: '/plants', query: { open: String(id) } })
}
function goToVitals() {
  router.push('/vitals')
}

// --- Fleet summary -----------------------------------------------------------
const fleet = {
  healthy: 8,
  healthy_delta: 1,
  attention: 3,
  attention_delta: 2,
  critical: 1,
  devices_online: 10,
  devices_total: 12,
}

// --- Recent activity ---------------------------------------------------------
interface ActivityRow {
  plant_id: number
  plant: string
  body: string
  when: string
}
const activity: ActivityRow[] = [
  { plant_id: 1, plant: 'Fiddle Leaf', body: 'Watered (+24 pts)', when: '09:14' },
  { plant_id: 1, plant: 'Fiddle Leaf', body: 'Note: looks happier near the window', when: '08:02' },
  { plant_id: 4, plant: 'Monstera', body: 'Moved: Bedroom → Living Room', when: 'Yesterday' },
  { plant_id: 2, plant: 'Snake Plant', body: 'Repotted to 6" terra cotta', when: '5d ago' },
]

const journalAnchor = computed(() => activity[0]?.plant_id ?? 1)

// --- Watering this week ------------------------------------------------------
// Mon..Sun, mock counts. Bars scale to the busiest day.
const weeklyWaterings = [1, 2, 4, 1, 2, 0, 1]
const weeklyTotal = weeklyWaterings.reduce((a, b) => a + b, 0)
const weeklyMax = Math.max(...weeklyWaterings, 1)
const dayLetters = ['M', 'T', 'W', 'T', 'F', 'S', 'S']
</script>

<template>
  <section class="overview-view">
    <div class="hero">
      <h1>{{ greeting }}, Simon</h1>
      <p class="hero__date">{{ dateLabel }} · {{ statusLine }}</p>
    </div>

    <div class="overview-grid">
      <!-- Care queue -------------------------------------------------------
        FUTURE: derive items from the backend, not just from moisture/temp.

        Specifically, when a water command was sent but soil moisture didn't
        rise within ~N minutes, surface a "Watering had no effect" item here.

        From the soil sensor's POV three failure classes look identical:
          1. Reservoir empty — pump fired into air
          2. Battery dying  — pump didn't actually run, or ran too weakly
          3. Clogged tube / dislodged emitter — water flowed but didn't reach
             this plant

        --- Option A: battery voltage (zero extra hardware on Pico W) ---
        The Pico W can already read its system supply voltage on ADC channel
        3 (the on-board VSYS divider). No extra components, just firmware:
          - Sample VSYS once per telemetry cycle, report `battery_v`.
          - Healthy LiPo sits ~3.7–4.2 V; below ~3.3 V the LDO struggles to
            drive the pump motor.
          - Also sample DURING the pump pulse — a battery that's OK at rest
            but droops under load is a classic dying-cell signature.
        This alone splits "device dying" from "everything else" and is the
        recommended first step because it's free.

        Other options once we want to fully disambiguate (1) vs (3):
          - reservoir float switch (one GPIO, ~€2) — deterministic
          - pump current sense via INA219 (~€3, I²C) — also catches dry-run
            during the first pulse instead of waiting on moisture.

        Until any of that lands we can only show "Watering had no effect —
        check the reservoir or device" as a single combined item.
      -->
      <article class="tile tile--care">
        <header class="tile__header">
          <h2>Today ({{ careItems.length }})</h2>
          <n-button text size="small" @click="goToVitals">See all →</n-button>
        </header>
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
            <span class="delta delta--good">▲{{ fleet.healthy_delta }}</span>
          </li>
          <li>
            <span class="fleet-num">{{ fleet.attention }}</span>
            <span class="fleet-label">Needs attention</span>
            <span class="delta delta--bad">▲{{ fleet.attention_delta }}</span>
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
          <n-button text size="small" @click="openPlant(journalAnchor)">
            Open journal →
          </n-button>
        </header>
        <ul class="activity-list">
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
