import { api, type WateringEvent } from '@/api/client'

export interface DiscordNotificationSettings {
  enabled: boolean
  webhook_url: string
  bot_username: string
}

export const DISCORD_SETTINGS_KEY = 'drip.discord.settings'
const LAST_WATERING_EVENT_KEY_PREFIX = 'drip.discord.lastWateringEvent.'

export function defaultDiscordSettings(): DiscordNotificationSettings {
  return {
    enabled: false,
    webhook_url: '',
    bot_username: 'DRIP',
  }
}

export function discordIsValidUrl(url: string): boolean {
  return /^https:\/\/(discord|discordapp)\.com\/api\/webhooks\/\d+\/[\w-]+$/.test(url.trim())
}

export function loadDiscordSettings(): DiscordNotificationSettings {
  const fallback = defaultDiscordSettings()
  const raw = localStorage.getItem(DISCORD_SETTINGS_KEY)
  if (!raw) return fallback

  try {
    return { ...fallback, ...JSON.parse(raw) }
  } catch {
    return fallback
  }
}

export function saveDiscordSettings(settings: DiscordNotificationSettings) {
  localStorage.setItem(DISCORD_SETTINGS_KEY, JSON.stringify(settings))
}

export async function sendDiscordMessage(
  settings: DiscordNotificationSettings,
  content: string,
): Promise<void> {
  if (!settings.enabled || !discordIsValidUrl(settings.webhook_url)) return

  const response = await fetch(settings.webhook_url.trim(), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      username: settings.bot_username || 'DRIP',
      content,
    }),
  })

  if (!response.ok) {
    throw new Error(`Discord webhook failed with status ${response.status}`)
  }
}

function lastEventKey(plantId: number) {
  return `${LAST_WATERING_EVENT_KEY_PREFIX}${plantId}`
}

export function getLastNotifiedWateringEventId(plantId: number): number {
  return Number(localStorage.getItem(lastEventKey(plantId)) ?? 0)
}

export function setLastNotifiedWateringEventId(plantId: number, eventId: number) {
  localStorage.setItem(lastEventKey(plantId), String(eventId))
}

export function wateringEventMessage(event: WateringEvent): string {
  const moisture =
    event.moistureBefore == null ? 'unknown moisture' : `${event.moistureBefore}% moisture`
  const duration = Math.round(event.pumpDurationMs / 1000)

  return `Plant ${event.plantId} was watered for ${duration}s after reading ${moisture}.`
}

export async function notifyNewWateringEvents(plantId: number) {
  const settings = loadDiscordSettings()
  if (!settings.enabled || !discordIsValidUrl(settings.webhook_url)) return

  const events = await api.getWateringEvents(plantId)
  const lastNotifiedId = getLastNotifiedWateringEventId(plantId)
  const newEvents = events
    .filter((event) => event.id > lastNotifiedId)
    .sort((a, b) => a.id - b.id)

  for (const event of newEvents) {
    await sendDiscordMessage(settings, wateringEventMessage(event))
    setLastNotifiedWateringEventId(plantId, event.id)
  }
}
