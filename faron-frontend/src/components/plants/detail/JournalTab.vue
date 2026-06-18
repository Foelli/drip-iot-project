<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Component } from 'vue'
import {
  Camera,
  CreateOutline,
  Home,
  Image as ImageIcon,
  Leaf,
  Settings,
  Water,
  WifiOutline,
} from '@vicons/ionicons5'
import type { Plant } from '@/types/Plant'

defineProps<{ plant: Plant }>()

// Event types ----------------------------------------------------------------
type PlantEvent =
  | {
      id: string
      ts: Date
      type: 'watering'
      payload: { trigger: 'Auto' | 'Manual'; before: number; duration_s: number; delta: number }
    }
  | { id: string; ts: Date; type: 'note'; payload: { body: string; photo_url?: string } }
  | { id: string; ts: Date; type: 'move'; payload: { from: string; to: string } }
  | {
      id: string
      ts: Date
      type: 'threshold'
      payload: { metric: string; from: string; to: string }
    }
  | {
      id: string
      ts: Date
      type: 'device'
      payload: { name: string; status: 'online' | 'offline'; detail: string }
    }
  | { id: string; ts: Date; type: 'care'; payload: { kind: string; detail: string } }
  | { id: string; ts: Date; type: 'photo'; payload: { url: string; caption?: string } }

type EventType = PlantEvent['type']

const EVENT_CONFIG: Record<EventType, { icon: Component; color: string; label: string }> = {
  watering: { icon: Water, color: '#3b82f6', label: 'Watering' },
  note: { icon: CreateOutline, color: 'var(--success)', label: 'Notes' },
  move: { icon: Home, color: 'var(--warning)', label: 'Moves' },
  threshold: { icon: Settings, color: '#a855f7', label: 'Settings' },
  device: { icon: WifiOutline, color: 'var(--neutral-400)', label: 'Device' },
  care: { icon: Leaf, color: 'var(--success)', label: 'Care' },
  photo: { icon: ImageIcon, color: '#ec4899', label: 'Photos' },
}

const filterTypes: EventType[] = [
  'watering',
  'move',
  'threshold',
  'device',
  'note',
  'photo',
  'care',
]

// Mock data ------------------------------------------------------------------
function daysAgoAt(days: number, h: number, m: number): Date {
  const d = new Date()
  d.setDate(d.getDate() - days)
  d.setHours(h, m, 0, 0)
  return d
}

const events = ref<PlantEvent[]>([
  {
    id: '1',
    ts: daysAgoAt(0, 9, 14),
    type: 'watering',
    payload: { trigger: 'Auto', before: 38, duration_s: 8, delta: 24 },
  },
  {
    id: '2',
    ts: daysAgoAt(0, 8, 2),
    type: 'note',
    payload: { body: 'Looks happier after I moved it nearer the window.' },
  },
  {
    id: '3',
    ts: daysAgoAt(1, 14, 30),
    type: 'move',
    payload: { from: 'Bedroom', to: 'Living Room' },
  },
  {
    id: '4',
    ts: daysAgoAt(1, 11, 17),
    type: 'threshold',
    payload: { metric: 'Moisture band', from: '40–70%', to: '45–75%' },
  },
  {
    id: '5',
    ts: daysAgoAt(3, 10, 21),
    type: 'watering',
    payload: { trigger: 'Auto', before: 40, duration_s: 8, delta: 28 },
  },
  {
    id: '6',
    ts: daysAgoAt(4, 9, 15),
    type: 'device',
    payload: { name: 'Pico-01', status: 'offline', detail: 'silent for ~2 hours' },
  },
  {
    id: '7',
    ts: daysAgoAt(5, 16, 42),
    type: 'care',
    payload: { kind: 'Repotted', detail: 'Moved to a 6" terra cotta pot.' },
  },
  {
    id: '8',
    ts: daysAgoAt(6, 13, 5),
    type: 'watering',
    payload: { trigger: 'Manual', before: 45, duration_s: 12, delta: 31 },
  },
  {
    id: '9',
    ts: daysAgoAt(8, 19, 22),
    type: 'photo',
    payload: { url: 'https://picsum.photos/seed/leaf/240/180', caption: 'New leaf unfurling.' },
  },
  {
    id: '10',
    ts: daysAgoAt(10, 10, 0),
    type: 'note',
    payload: { body: 'Spotted slight yellowing on the lowest leaf — keeping an eye on it.' },
  },
])

// Filter ---------------------------------------------------------------------
const activeFilters = ref<EventType[]>([])
function toggleFilter(t: EventType, on: boolean) {
  if (on) activeFilters.value = [...activeFilters.value, t]
  else activeFilters.value = activeFilters.value.filter((x) => x !== t)
}

const filteredEvents = computed(() =>
  activeFilters.value.length === 0
    ? events.value
    : events.value.filter((e) => activeFilters.value.includes(e.type)),
)

// Day grouping ---------------------------------------------------------------
const DAY_MS = 86_400_000
function dayBucket(d: Date): 'Today' | 'Yesterday' | 'Last week' | 'Earlier' {
  const now = new Date()
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  const startOfThat = new Date(d.getFullYear(), d.getMonth(), d.getDate()).getTime()
  const diffDays = Math.floor((startOfToday - startOfThat) / DAY_MS)
  if (diffDays <= 0) return 'Today'
  if (diffDays === 1) return 'Yesterday'
  if (diffDays < 7) return 'Last week'
  return 'Earlier'
}

const groupedEvents = computed(() => {
  const order: ('Today' | 'Yesterday' | 'Last week' | 'Earlier')[] = [
    'Today',
    'Yesterday',
    'Last week',
    'Earlier',
  ]
  return order
    .map((label) => ({
      label,
      items: filteredEvents.value.filter((e) => dayBucket(e.ts) === label),
    }))
    .filter((g) => g.items.length > 0)
})

// Title / body / time helpers ------------------------------------------------
function titleFor(e: PlantEvent): string {
  switch (e.type) {
    case 'watering':
      return `Watered (${e.payload.trigger.toLowerCase()})`
    case 'note':
      return 'Note'
    case 'move':
      return 'Moved'
    case 'threshold':
      return 'Threshold updated'
    case 'device':
      return `Device ${e.payload.status}`
    case 'care':
      return e.payload.kind
    case 'photo':
      return 'Photo'
  }
}

function bodyFor(e: PlantEvent): string {
  switch (e.type) {
    case 'watering':
      return `Triggered at ${e.payload.before}% · ${e.payload.duration_s}s pump · +${e.payload.delta} pts`
    case 'note':
      return e.payload.body
    case 'move':
      return `${e.payload.from} → ${e.payload.to}`
    case 'threshold':
      return `${e.payload.metric}: ${e.payload.from} → ${e.payload.to}`
    case 'device':
      return `${e.payload.name} · ${e.payload.detail}`
    case 'care':
      return e.payload.detail
    case 'photo':
      return e.payload.caption ?? ''
  }
}

function photoFor(e: PlantEvent): string | undefined {
  if (e.type === 'photo') return e.payload.url
  if (e.type === 'note') return e.payload.photo_url
  return undefined
}

function formatTime(d: Date): string {
  const bucket = dayBucket(d)
  if (bucket === 'Today' || bucket === 'Yesterday') {
    return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit', hour12: false })
  }
  return d.toLocaleString('en-US', {
    weekday: 'short',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

// Composer -------------------------------------------------------------------
const draftNote = ref('')
const canPost = computed(() => draftNote.value.trim().length > 0)
function postNote() {
  if (!canPost.value) return
  events.value.unshift({
    id: `e-${Date.now()}`,
    ts: new Date(),
    type: 'note',
    payload: { body: draftNote.value.trim() },
  })
  draftNote.value = ''
}
</script>

<template>
  <div class="journal-tab">
    <!-- Composer -->
    <div class="composer">
      <n-input
        v-model:value="draftNote"
        type="textarea"
        placeholder="Add a note…"
        :autosize="{ minRows: 2, maxRows: 6 }"
      />
      <n-flex justify="space-between" align="center" class="composer__actions">
        <n-button quaternary>
          <template #icon><n-icon><Camera /></n-icon></template>
          Photo
        </n-button>
        <n-button type="primary" :disabled="!canPost" @click="postNote">Post</n-button>
      </n-flex>
    </div>

    <!-- Filter chips -->
    <n-flex :size="6" :wrap="true" class="filters">
      <n-tag
        :checked="activeFilters.length === 0"
        checkable
        round
        @update:checked="(on: boolean) => { if (on) activeFilters = [] }"
      >
        All
      </n-tag>
      <n-tag
        v-for="t in filterTypes"
        :key="t"
        :checked="activeFilters.includes(t)"
        checkable
        round
        @update:checked="(on: boolean) => toggleFilter(t, on)"
      >
        <template #icon>
          <n-icon><component :is="EVENT_CONFIG[t].icon" /></n-icon>
        </template>
        {{ EVENT_CONFIG[t].label }}
      </n-tag>
    </n-flex>

    <!-- Empty filter state -->
    <n-empty
      v-if="filteredEvents.length === 0"
      description="No entries match these filters."
      class="empty-state"
    />

    <!-- Day-grouped timeline -->
    <template v-for="group in groupedEvents" :key="group.label">
      <n-divider title-placement="left">{{ group.label }}</n-divider>
      <n-timeline>
        <n-timeline-item
          v-for="ev in group.items"
          :key="ev.id"
          :color="EVENT_CONFIG[ev.type].color"
          :title="titleFor(ev)"
          :time="formatTime(ev.ts)"
        >
          <template #icon>
            <n-icon><component :is="EVENT_CONFIG[ev.type].icon" /></n-icon>
          </template>
          <div v-if="bodyFor(ev)" class="event-body">{{ bodyFor(ev) }}</div>
          <img
            v-if="photoFor(ev)"
            :src="photoFor(ev)"
            :alt="titleFor(ev)"
            class="event-photo"
          />
        </n-timeline-item>
      </n-timeline>
    </template>
  </div>
</template>

<style scoped>
.journal-tab {
  padding: 0;
}

.composer {
  border: 1px solid var(--color-border-subtle);
  border-radius: var(--radius-md);
  padding: 0.75rem;
  margin-bottom: 0.75rem;
}
.composer__actions {
  margin-top: 0.5rem;
}

.filters {
  margin-bottom: 0.25rem;
}

.event-body {
  font-size: 0.875rem;
  color: var(--color-text-muted);
}
.event-photo {
  display: block;
  margin-top: 0.5rem;
  max-width: 240px;
  border-radius: var(--radius-md);
}

.empty-state {
  padding: 2rem 0;
}
</style>
