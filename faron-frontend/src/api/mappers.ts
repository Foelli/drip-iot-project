import type { BackendPlant, Measurement } from './client'
import type { Plant, PlantStatus } from '@/types/Plant'

function deriveStatus(moisture: number, min: number, max: number): PlantStatus {
  if (moisture < min || moisture > max) return 'needs_attention'
  return 'healthy'
}

export function mapBackendPlantToPlant(
  backendPlant: BackendPlant,
  latestMeasurement?: Measurement | null,
): Plant {
  const moisture = latestMeasurement?.soilMoisture ?? backendPlant.moisture ?? 0
  const temperature = latestMeasurement?.temperature ?? backendPlant.temperature ?? 0
  const airMoisture = latestMeasurement?.airMoisture ?? backendPlant.airMoisture ?? 0
  const moistureMax = 100
  const tempMin = backendPlant.idealTempMin
  const tempMax = backendPlant.idealTempMax
  const airMoistureMin = backendPlant.idealAirMoistureMin
  const airMoistureMax = backendPlant.idealAirMoistureMax

  return {
    id: backendPlant.id,
    custom_name: backendPlant.customName ?? backendPlant.commonName ?? `Plant ${backendPlant.id}`,
    room: 'Unknown',
    photo_url: backendPlant.thumbnailUrl,
    species: {
      id: backendPlant.apiId,
      common_name: backendPlant.commonName ?? 'Unknown plant',
      scientific_name: backendPlant.scientificName ? [backendPlant.scientificName] : [],
      family: null,
      watering: 'Unknown',
      watering_general_benchmark: { value: 'Unknown', unit: 'days' },
      soil: [],
      care_level: 'Unknown',
      maintenance: 'Unknown',
      description: backendPlant.description ?? '',
      default_image: null,
    },
    device: {
      name: `Plant ${backendPlant.id} Pico`,
      online: latestMeasurement != null,
      firmware: null,
      last_seen: latestMeasurement?.measuredAt ?? null,
    },
    readings: {
      moisture,
      temperature,
      air_moisture: airMoisture,
      status: deriveStatus(moisture, backendPlant.moistureThreshold, moistureMax),
    },
    care_info: {
      ideal_moisture_min: backendPlant.idealMoistureMin,
      ideal_moisture_max: backendPlant.idealMoistureMax,
      ideal_temp_min: tempMin,
      ideal_temp_max: tempMax,
      ideal_air_moisture_min: airMoistureMin,
      ideal_air_moisture_max: airMoistureMax,
      notes: backendPlant.careNotes ?? backendPlant.description ?? '',
    },
    settings: {
      thresholds: {
        moisture_min: backendPlant.idealMoistureMin,
        moisture_max: backendPlant.idealMoistureMax,
        temp_min: tempMin,
        temp_max: tempMax,
        air_moisture_min: airMoistureMin,
        air_moisture_max: airMoistureMax,
      },
      automation: {
        auto_water: backendPlant.wateringEnabled,
        pump_duration_s: Math.round(backendPlant.pumpDurationMs / 1000),
        cooldown_h: 6,
        quiet_hours: { enabled: true, from: '22:00', to: '07:00' },
        notifications: true,
      },
    },
    wateringEnabled: backendPlant.wateringEnabled,
    moistureThreshold: backendPlant.moistureThreshold,
    pumpDurationMs: backendPlant.pumpDurationMs,
    waterSettleMs: backendPlant.waterSettleMs,
  }
}
