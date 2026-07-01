import type { Component } from 'vue'
import { AlertCircle, CheckmarkCircle, CloseCircle } from '@vicons/ionicons5'
import type { PlantStatus } from '@/types/Plant'

/**
 * Shared display config for plant status. Keeps icon/color/label choices in
 * one place.
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
