<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  Add,
  CheckmarkOutline,
  CloseOutline,
  CreateOutline,
  LogoDiscord,
  RefreshOutline,
  TrashOutline,
} from '@vicons/ionicons5'
import {
  discordIsValidUrl,
  loadDiscordSettings,
  saveDiscordSettings,
  sendDiscordMessage,
} from '@/services/discordNotifications'

defineOptions({ name: 'SettingsView' })

const router = useRouter()

// ---------------------------------------------------------------------------
// All state here is local + reactive. Nothing persists — once the backend has
// a config endpoint these refs are what you bind through the API.
// ---------------------------------------------------------------------------

// --- Appearance -------------------------------------------------------------
type Theme = 'light' | 'dark' | 'system'
type Units = 'metric' | 'imperial'

const appearance = reactive({
  theme: 'system' as Theme,
  units: 'metric' as Units,
})

const themeOptions: { label: string; value: Theme }[] = [
  { label: 'Light', value: 'light' },
  { label: 'Dark', value: 'dark' },
  { label: 'System', value: 'system' },
]
const unitOptions: { label: string; value: Units }[] = [
  { label: '°C', value: 'metric' },
  { label: '°F', value: 'imperial' },
]

// --- Notifications ----------------------------------------------------------
const notifications = reactive({
  enabled: true,
  quiet_hours: {
    enabled: true,
    from: '22:00',
    to: '07:00',
  },
  events: {
    device_offline: true,
    threshold_breached: true,
    watering_failed: true,
    watering_succeeded: false,
  },
})

interface EventToggle {
  key: keyof typeof notifications.events
  label: string
  hint?: string
}
const eventToggles: EventToggle[] = [
  { key: 'device_offline', label: 'Device offline' },
  { key: 'threshold_breached', label: 'Threshold breached' },
  { key: 'watering_failed', label: 'Watering failed' },
  { key: 'watering_succeeded', label: 'Watering succeeded', hint: 'Most users mute this.' },
]

const discord = reactive({
  ...loadDiscordSettings(),
  testing: false,
  last_test: null as null | { ok: boolean; when: string; message: string },
})

const discordUrlValid = computed(() => discordIsValidUrl(discord.webhook_url))
const discordUrlInvalid = computed(
  () => discord.webhook_url.length > 0 && !discordUrlValid.value,
)

async function sendDiscordTestPing() {
  if (!discordUrlValid.value) return
  discord.testing = true
  const now = new Date()
  const when = now.toLocaleTimeString('en-US', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false,
    })

  try {
    await sendDiscordMessage(discord, 'DRIP test notification: Discord webhook is connected.')
    discord.last_test = {
      ok: true,
      when,
      message: 'Pinged successfully',
    }
  } catch (error) {
    console.error(error)
    discord.last_test = {
      ok: false,
      when,
      message: 'Ping failed',
    }
  } finally {
    discord.testing = false
  }
}

watch(
  () => ({
    enabled: discord.enabled,
    webhook_url: discord.webhook_url,
    bot_username: discord.bot_username,
  }),
  saveDiscordSettings,
  { deep: true },
)

// --- Hub & backend ----------------------------------------------------------
const hub = reactive({
  backend_url: 'http://drip.local:8080',
  telemetry_interval_s: 60,
})

const telemetryOptions = [
  { label: 'Every 10 s', value: 10 },
  { label: 'Every 30 s', value: 30 },
  { label: 'Every 60 s', value: 60 },
  { label: 'Every 5 min', value: 300 },
  { label: 'Every 15 min', value: 900 },
]

type ConnStatus = 'connected' | 'pending' | 'disconnected'
const connection = reactive({
  status: 'connected' as ConnStatus,
  last_ping_s: 2,
})

const connectionLabel = computed(() => {
  if (connection.status === 'connected') return `Connected · last ping ${connection.last_ping_s}s ago`
  if (connection.status === 'pending') return 'Reconnecting…'
  return 'Disconnected'
})
const connectionColor = computed(() => {
  if (connection.status === 'connected') return 'var(--success)'
  if (connection.status === 'pending') return 'var(--warning)'
  return 'var(--danger)'
})

function reconnect() {
  connection.status = 'pending'
  setTimeout(() => {
    connection.status = 'connected'
    connection.last_ping_s = 0
  }, 700)
}

// --- Rooms ------------------------------------------------------------------
interface Room {
  id: number
  name: string
}

const rooms = ref<Room[]>([
  { id: 1, name: 'Living Room' },
  { id: 2, name: 'Bedroom' },
  { id: 3, name: 'Kitchen' },
])
let nextRoomId = 4

const editingRoomId = ref<number | null>(null)
const editingRoomName = ref('')
const newRoomName = ref('')

function startEdit(room: Room) {
  editingRoomId.value = room.id
  editingRoomName.value = room.name
}
function cancelEdit() {
  editingRoomId.value = null
  editingRoomName.value = ''
}
function saveEdit() {
  const room = rooms.value.find((r) => r.id === editingRoomId.value)
  const next = editingRoomName.value.trim()
  if (room && next.length > 0) room.name = next
  cancelEdit()
}
function deleteRoom(id: number) {
  rooms.value = rooms.value.filter((r) => r.id !== id)
}
function addRoom() {
  const name = newRoomName.value.trim()
  if (name.length === 0) return
  rooms.value.push({ id: nextRoomId++, name })
  newRoomName.value = ''
}

// --- System info (read-only mock) -------------------------------------------
const system = {
  backend_version: 'v0.3.1',
  backend_uptime: '4 d 7 h',
  frontend_build: 'build a1b2c3d',
  storage_used_mb: 312,
  storage_total_gb: 64,
  fleet_firmware: [
    { version: 'v0.4.1', count: 3, behind: false },
    { version: 'v0.3.8', count: 1, behind: true },
  ],
}

function goToDevices() {
  router.push('/vitals')
}
</script>

<template>
  <section class="settings-view">
    <div class="view-header">
      <h1>Settings</h1>
    </div>

    <div class="settings-content">
      <!-- Appearance -------------------------------------------------- -->
      <n-divider title-placement="left">Appearance</n-divider>
      <div class="form-grid">
        <div class="form-row">
          <label class="form-row__label">Theme</label>
          <n-radio-group v-model:value="appearance.theme" size="small">
            <n-radio-button v-for="o in themeOptions" :key="o.value" :value="o.value">
              {{ o.label }}
            </n-radio-button>
          </n-radio-group>
        </div>
        <div class="form-row">
          <label class="form-row__label">Units</label>
          <n-radio-group v-model:value="appearance.units" size="small">
            <n-radio-button v-for="o in unitOptions" :key="o.value" :value="o.value">
              {{ o.label }}
            </n-radio-button>
          </n-radio-group>
        </div>
      </div>

      <!-- Notifications ----------------------------------------------- -->
      <n-divider title-placement="left">Notifications</n-divider>
      <div class="form-grid">
        <div class="form-row">
          <label class="form-row__label">
            Critical alerts
            <span class="form-row__hint">Turn off to silence every notification.</span>
          </label>
          <n-switch v-model:value="notifications.enabled" />
        </div>

        <div class="form-row form-row--start">
          <label class="form-row__label">Channels</label>
        </div>

        <div class="form-row form-row--start">
          <label class="form-row__label">
            Quiet hours
            <span class="form-row__hint">All non-critical notifications muted in this window.</span>
          </label>
          <div class="quiet-hours">
            <n-switch v-model:value="notifications.quiet_hours.enabled" />
            <n-time-picker
              v-model:formatted-value="notifications.quiet_hours.from"
              format="HH:mm"
              value-format="HH:mm"
              :disabled="!notifications.quiet_hours.enabled"
              size="small"
            />
            <span class="muted">to</span>
            <n-time-picker
              v-model:formatted-value="notifications.quiet_hours.to"
              format="HH:mm"
              value-format="HH:mm"
              :disabled="!notifications.quiet_hours.enabled"
              size="small"
            />
          </div>
        </div>

        <div class="form-row form-row--start">
          <label class="form-row__label">Notify on</label>
          <div class="event-toggles">
            <label v-for="e in eventToggles" :key="e.key" class="event-toggle">
              <n-checkbox v-model:checked="notifications.events[e.key]">
                {{ e.label }}
              </n-checkbox>
              <span v-if="e.hint" class="form-row__hint">{{ e.hint }}</span>
            </label>
          </div>
        </div>
      </div>

      <!-- Discord webhook (sub-card inside Notifications) ------------- -->
      <div class="discord-card" :class="{ 'discord-card--off': !discord.enabled }">
        <header class="discord-card__head">
          <div class="discord-card__title">
            <n-icon :size="20" class="discord-card__icon"><LogoDiscord /></n-icon>
            <div>
              <div class="discord-card__name">Discord webhook</div>
              <div class="form-row__hint">
                Mirror alerts to a Discord channel via an incoming webhook.
              </div>
            </div>
          </div>
          <n-switch v-model:value="discord.enabled" />
        </header>

        <div class="discord-card__body">
          <div class="form-row">
            <label class="form-row__label">
              Webhook URL
              <span class="form-row__hint">
                Server Settings → Integrations → Webhooks → New Webhook → Copy URL.
              </span>
            </label>
            <div class="discord-url">
              <n-input
                v-model:value="discord.webhook_url"
                placeholder="https://discord.com/api/webhooks/…"
                :status="discordUrlInvalid ? 'error' : undefined"
                :disabled="!discord.enabled"
              />
              <span v-if="discordUrlInvalid" class="form-row__hint form-row__hint--error">
                That doesn't look like a Discord webhook URL.
              </span>
            </div>
          </div>

          <div class="form-row">
            <label class="form-row__label">
              Bot username
              <span class="form-row__hint">Optional override for the message author.</span>
            </label>
            <n-input
              v-model:value="discord.bot_username"
              :disabled="!discord.enabled"
              class="form-row__input"
            />
          </div>

          <div class="form-row">
            <label class="form-row__label">Test</label>
            <div class="discord-test">
              <n-button
                size="small"
                :loading="discord.testing"
                :disabled="!discord.enabled || !discordUrlValid"
                @click="sendDiscordTestPing"
              >
                Send test ping
              </n-button>
              <span
                v-if="discord.last_test"
                :class="[
                  'discord-test__result',
                  discord.last_test.ok ? 'discord-test__result--ok' : 'discord-test__result--fail',
                ]"
              >
                {{ discord.last_test.ok ? '✓' : '✗' }} {{ discord.last_test.message }}
                · {{ discord.last_test.when }}
              </span>
              <span v-else class="form-row__hint">Never tested.</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Hub & backend ----------------------------------------------- -->
      <n-divider title-placement="left">Hub &amp; backend</n-divider>
      <div class="form-grid">
        <div class="form-row">
          <label class="form-row__label">
            Backend URL
            <span class="form-row__hint">Where the Ktor backend is reachable on the LAN.</span>
          </label>
          <n-input
            v-model:value="hub.backend_url"
            placeholder="http://drip.local:8080"
            class="form-row__input"
          />
        </div>

        <div class="form-row">
          <label class="form-row__label">Connection</label>
          <div class="connection-status">
            <span class="connection-dot" :style="{ background: connectionColor }"></span>
            <span>{{ connectionLabel }}</span>
            <n-button
              size="small"
              :loading="connection.status === 'pending'"
              @click="reconnect"
            >
              <template #icon>
                <n-icon><RefreshOutline /></n-icon>
              </template>
              Reconnect
            </n-button>
          </div>
        </div>

        <div class="form-row">
          <label class="form-row__label">
            Telemetry interval
            <span class="form-row__hint">How often the Picos report sensor readings.</span>
          </label>
          <n-select
            v-model:value="hub.telemetry_interval_s"
            :options="telemetryOptions"
            size="small"
            class="form-row__select"
          />
        </div>
      </div>

      <!-- Rooms ------------------------------------------------------- -->
      <n-divider title-placement="left">Rooms</n-divider>
      <p class="section-intro">
        Rooms appear in the sidebar's room switcher and on each plant card. Renaming a room
        updates it everywhere it's referenced.
      </p>
      <ul class="room-list">
        <li v-for="room in rooms" :key="room.id" class="room-list__item">
          <template v-if="editingRoomId === room.id">
            <n-input
              v-model:value="editingRoomName"
              size="small"
              autofocus
              @keyup.enter="saveEdit"
              @keydown.esc="cancelEdit"
            />
            <n-button size="small" type="primary" @click="saveEdit">
              <template #icon>
                <n-icon><CheckmarkOutline /></n-icon>
              </template>
              Save
            </n-button>
            <n-button size="small" quaternary @click="cancelEdit">
              <template #icon>
                <n-icon><CloseOutline /></n-icon>
              </template>
              Cancel
            </n-button>
          </template>
          <template v-else>
            <span class="room-list__name">{{ room.name }}</span>
            <n-button size="small" quaternary @click="startEdit(room)">
              <template #icon>
                <n-icon><CreateOutline /></n-icon>
              </template>
              Rename
            </n-button>
            <n-button size="small" quaternary @click="deleteRoom(room.id)">
              <template #icon>
                <n-icon><TrashOutline /></n-icon>
              </template>
              Delete
            </n-button>
          </template>
        </li>
      </ul>
      <div class="room-add">
        <n-input
          v-model:value="newRoomName"
          size="small"
          placeholder="Add a room…"
          @keyup.enter="addRoom"
        />
        <n-button size="small" type="primary" :disabled="!newRoomName.trim()" @click="addRoom">
          <template #icon>
            <n-icon><Add /></n-icon>
          </template>
          Add room
        </n-button>
      </div>

      <!-- System info ------------------------------------------------- -->
      <n-divider title-placement="left">System info</n-divider>
      <dl class="system-info">
        <div class="system-info__row">
          <dt>Backend</dt>
          <dd>{{ system.backend_version }} · up {{ system.backend_uptime }}</dd>
        </div>
        <div class="system-info__row">
          <dt>Frontend</dt>
          <dd>{{ system.frontend_build }}</dd>
        </div>
        <div class="system-info__row">
          <dt>Storage</dt>
          <dd>{{ system.storage_used_mb }} MB / {{ system.storage_total_gb }} GB</dd>
        </div>
        <div class="system-info__row">
          <dt>Fleet firmware</dt>
          <dd>
            <span v-for="(g, i) in system.fleet_firmware" :key="g.version">
              <span :class="{ 'firmware-behind': g.behind }">
                {{ g.count }} on {{ g.version }}
              </span>
              <span v-if="i < system.fleet_firmware.length - 1"> · </span>
            </span>
            <n-button text size="small" class="system-info__link" @click="goToDevices">
              View devices →
            </n-button>
          </dd>
        </div>
      </dl>
    </div>
  </section>
</template>

<style scoped>
.view-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 3rem;
}

.settings-content {
  max-width: 48rem;
}

/* --- Section dividers + form rows: same look as PlantDetail tabs ----- */
.settings-content :deep(.n-divider:not(.n-divider--vertical)) {
  margin-top: 1.5rem;
  margin-bottom: 1rem;
}
.settings-content :deep(.n-divider__title) {
  font-size: 1.125rem;
}

.section-intro {
  font-size: 0.875rem;
  color: var(--color-text-muted);
  margin: 0 0 1rem;
}

/* --- Form grid ------------------------------------------------------- */
.form-grid {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.form-row {
  display: grid;
  grid-template-columns: 12rem 1fr;
  align-items: center;
  gap: 1rem;
}
.form-row--start {
  align-items: start;
}
.form-row__label {
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  gap: 0.15rem;
}
.form-row__hint {
  font-size: 0.75rem;
  color: var(--color-text-muted);
  font-weight: 400;
  line-height: 1.4;
}
.form-row__input {
  max-width: 22rem;
}
.form-row__select {
  max-width: 12rem;
}

/* --- Quiet hours pickers --------------------------------------------- */
.quiet-hours {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-wrap: wrap;
}
.muted {
  color: var(--color-text-muted);
  font-size: 0.875rem;
}

/* --- Event toggles --------------------------------------------------- */
.event-toggles {
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.event-toggle {
  display: flex;
  flex-direction: column;
}

/* --- Discord webhook sub-card --------------------------------------- */
.discord-card {
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: 1rem 1.25rem;
  margin-top: 1.25rem;
}
.discord-card__head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid var(--color-border-subtle);
}
.discord-card__title {
  display: flex;
  align-items: flex-start;
  gap: 0.625rem;
}
/* Discord brand purple stays readable in both themes. */
.discord-card__icon {
  color: #5865f2;
  margin-top: 0.1rem;
}
.discord-card__name {
  font-weight: 600;
  font-size: 0.95rem;
}
.discord-card__body {
  display: flex;
  flex-direction: column;
  gap: 0.875rem;
  padding-top: 0.875rem;
}
/* Visual cue when the integration is off — fields stay visible but feel idle. */
.discord-card--off .discord-card__body {
  opacity: 0.6;
}

.discord-url {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  max-width: 28rem;
}

.discord-test {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.discord-test__result {
  font-size: 0.8125rem;
  font-variant-numeric: tabular-nums;
}
.discord-test__result--ok {
  color: var(--success);
}
.discord-test__result--fail {
  color: var(--danger);
}

.form-row__hint--error {
  color: var(--danger);
}

/* --- Connection status ----------------------------------------------- */
.connection-status {
  display: flex;
  align-items: center;
  gap: 0.625rem;
  font-size: 0.875rem;
}
.connection-dot {
  display: inline-block;
  width: 0.625rem;
  height: 0.625rem;
  border-radius: 50%;
}

/* --- Rooms list ------------------------------------------------------ */
.room-list {
  list-style: none;
  margin: 0 0 0.75rem;
  padding: 0;
}
.room-list__item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid var(--color-border-subtle);
}
.room-list__item:last-child {
  border-bottom: none;
}
.room-list__name {
  flex: 1;
  font-size: 0.9rem;
}
.room-add {
  display: flex;
  gap: 0.5rem;
  align-items: center;
  max-width: 28rem;
}

/* --- System info ----------------------------------------------------- */
.system-info {
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
.system-info__row {
  display: grid;
  grid-template-columns: 12rem 1fr;
  gap: 1rem;
  align-items: baseline;
  font-size: 0.875rem;
}
.system-info__row dt {
  color: var(--color-text-muted);
}
.system-info__row dd {
  margin: 0;
  font-variant-numeric: tabular-nums;
}
.firmware-behind {
  color: var(--warning);
  font-weight: 500;
}
.system-info__link {
  margin-left: 0.5rem;
}
</style>
