<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  Add,
  CheckmarkOutline,
  CloseOutline,
  CreateOutline,
  RefreshOutline,
  TrashOutline,
} from '@vicons/ionicons5'
import { API_BASE_URL, api } from '@/api/client'

defineOptions({ name: 'SettingsView' })

// --- Hub & backend ----------------------------------------------------------
const hub = reactive({
  backend_url: API_BASE_URL,
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
  status: 'pending' as ConnStatus,
  last_ping_s: 0,
})

const connectionLabel = computed(() => {
  if (connection.status === 'connected')
    return `Connected · last ping ${connection.last_ping_s}s ago`
  if (connection.status === 'pending') return 'Reconnecting…'
  return 'Disconnected'
})
const connectionColor = computed(() => {
  if (connection.status === 'connected') return 'var(--success)'
  if (connection.status === 'pending') return 'var(--warning)'
  return 'var(--danger)'
})

async function reconnect() {
  connection.status = 'pending'
  try {
    await api.health()
    connection.status = 'connected'
    connection.last_ping_s = 0
  } catch (error) {
    console.error(error)
    connection.status = 'disconnected'
  }
}

onMounted(reconnect)

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
</script>

<template>
  <section class="settings-view">
    <div class="view-header">
      <h1>Settings</h1>
    </div>

    <div class="settings-content">
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
            <n-button size="small" :loading="connection.status === 'pending'" @click="reconnect">
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
        Rooms appear in the sidebar's room switcher and on each plant card. Renaming a room updates
        it everywhere it's referenced.
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

.muted {
  color: var(--color-text-muted);
  font-size: 0.875rem;
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
</style>
