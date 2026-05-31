import type { Component } from 'vue'
import {
  AlertCircle,
  CheckmarkCircle,
  CloseCircle,
  Cloudy,
  PartlySunny,
  Sunny,
} from '@vicons/ionicons5'
import type { PlantLight, PlantStatus } from '@/types/Plant'

/**
 * Shared display config for plant status + light level, used by both the
 * grid card and the detail view. Keeps icon/color/label choices in one place.
 */

export interface StatusConfig {
  icon: Component
  color: string
  label: string
}

export const STATUS_CONFIG: Record<PlantStatus, StatusConfig> = {
  healthy: { icon: CheckmarkCircle, color: 'var(--success)', label: 'Healthy' },
  needs_attention: { icon: AlertCircle, color: 'var(--warning)', label: 'Needs attention' },
  critical: { icon: CloseCircle, color: 'var(--danger)', label: 'Critical' },
}

/** Shown instead of status when the paired device is offline. */
export const OFFLINE_CONFIG = { color: 'var(--neutral-400)', label: 'Offline' } as const

export interface LightConfig {
  icon: Component
  label: string
}

export const LIGHT_CONFIG: Record<PlantLight, LightConfig> = {
  low: { icon: Cloudy, label: 'Low' },
  medium: { icon: PartlySunny, label: 'Med' },
  high: { icon: Sunny, label: 'Bright' },
}
