export type PlantStatus = 'healthy' | 'needs_attention' | 'critical'

/**
 * Species/catalog data — sourced from the Perenual API.
 * Read-only from the app's perspective, shared across every plant of the
 * same species, and cacheable by `id`.
 */
export interface SpeciesImage {
  image_id: number
  original_url: string
  regular_url: string
  medium_url: string
  small_url: string
  thumbnail: string
}

export interface Species {
  id: number
  common_name: string
  /** Perenual returns this as an array, e.g. ["Ficus lyrata"] */
  scientific_name: string[]
  family: string | null
  /** qualitative label, e.g. "Frequent" */
  watering: string
  /** numeric benchmark, e.g. { value: "5-7", unit: "days" } */
  watering_general_benchmark: { value: string; unit: string }
  soil: string[]
  /** e.g. "Easy" | "Medium" | "Difficult" */
  care_level: string
  /** e.g. "Low" | "Moderate" | "High" */
  maintenance: string
  description: string
  default_image: SpeciesImage | null
}

/**
 * The paired Pico device — pairing + connectivity, sourced from the backend.
 */
export interface Device {
  /** name of the paired Pico, e.g. "Pico-01" */
  name: string
  /** whether the paired Pico has reported recently */
  online: boolean
  firmware: string | null
  /** ISO timestamp of the last report, null if never seen */
  last_seen: string | null
}

/**
 * Live telemetry — streamed from the Pico via the backend. Updates frequently
 * and is read-only, so it lives apart from the editable settings.
 */
export interface PlantReadings {
  /** soil moisture, 0–100 % */
  moisture: number
  /** ambient temperature in °C */
  temperature: number
  /** ambient air moisture / humidity, 0–100 % */
  air_moisture: number
  /** derived health, drives the status dot/chip */
  status: PlantStatus
}

/**
 * Plant care guidance. These values are intended to be generated when a plant
 * is created and then stored with that plant.
 */
export interface CareInfo {
  ideal_moisture_min: number
  ideal_moisture_max: number
  ideal_temp_min: number
  ideal_temp_max: number
  ideal_air_moisture_min: number
  ideal_air_moisture_max: number
  notes: string
}

/**
 * Per-plant care thresholds. `null` on PlantSettings means "inherit the
 * species defaults" rather than a user override.
 */
export interface Thresholds {
  moisture_min: number
  moisture_max: number
  temp_min: number
  temp_max: number
  air_moisture_min: number
  air_moisture_max: number
}

/**
 * What the system does on its own for this plant.
 */
export interface Automation {
  auto_water: boolean
  pump_duration_s: number
  cooldown_h: number
  quiet_hours: { enabled: boolean; from: string; to: string }
  notifications: boolean
}

/**
 * Editable per-plant configuration — the Settings form binds to a clone of
 * this and PATCHes it on Save.
 */
export interface PlantSettings {
  thresholds: Thresholds | null
  automation: Automation
}

/**
 * The Plant aggregate the frontend works with. Identity fields stay at the
 * top level; everything else is grouped by source + lifecycle + mutability.
 */
export interface Plant {
  id: number
  custom_name: string
  room: string
  /** user photo override; null → fall back to species.default_image */
  photo_url: string | null
  species: Species
  device: Device
  readings: PlantReadings
  settings: PlantSettings
  care_info: CareInfo
  wateringEnabled?: boolean
  moistureThreshold?: number
  pumpDurationMs?: number
  waterSettleMs?: number
}
