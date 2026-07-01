export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1'

export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
    public body: unknown,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    method,
    headers: {
      'Content-Type': 'application/json',
    },
    body: body ? JSON.stringify(body) : undefined,
  })

  if (!response.ok) {
    const errorBody = await response.json().catch(() => null)
    throw new ApiError(response.status, response.statusText, errorBody)
  }

  if (response.status === 204) {
    return undefined as T
  }

  const text = await response.text()
  if (!text) return undefined as T

  return JSON.parse(text)
}

export interface UpdatePlantWateringSettingsRequest {
  wateringEnabled: boolean
  moistureThreshold: number
  pumpDurationMs: number
  waterSettleMs: number
}

export interface BackendPlant {
  id: number
  apiId: number
  commonName: string | null
  scientificName: string | null
  customName: string | null
  thumbnailUrl: string | null
  description: string | null
  temperature: number | null
  moisture: number | null
  airMoisture: number | null
  wateringEnabled: boolean
  moistureThreshold: number
  pumpDurationMs: number
  waterSettleMs: number
  idealMoistureMin: number
  idealMoistureMax: number
  idealTempMin: number
  idealTempMax: number
  idealAirMoistureMin: number
  idealAirMoistureMax: number
  careNotes: string | null
}

export interface CareInfoResponse {
  idealMoistureMin: number
  idealMoistureMax: number
  idealTempMin: number
  idealTempMax: number
  idealAirMoistureMin: number
  idealAirMoistureMax: number
  careNotes: string
}

export interface GenerateCareInfoRequest {
  commonName: string
  scientificName?: string | null
  notes?: string | null
}

export interface CreatePlantRequest {
  apiId: number
  commonName?: string | null
  scientificName?: string | null
  customName?: string | null
  thumbnailUrl?: string | null
  description?: string | null
  temperature?: number | null
  moisture?: number | null
  airMoisture?: number | null
  wateringEnabled?: boolean
  moistureThreshold?: number
  pumpDurationMs?: number
  waterSettleMs?: number
  idealMoistureMin?: number
  idealMoistureMax?: number
  idealTempMin?: number
  idealTempMax?: number
  idealAirMoistureMin?: number
  idealAirMoistureMax?: number
  careNotes?: string | null
}

export interface Measurement {
  id: number
  plantId: number
  temperature: number | null
  soilMoisture: number | null
  airMoisture: number | null
  measuredAt: string
}

export interface WateringEvent {
  id: number
  plantId: number
  moistureBefore: number | null
  pumpDurationMs: number
  createdAt: string
}

export const api = {
  async health() {
    const response = await fetch(`${API_BASE_URL}/health`)
    if (!response.ok) {
      throw new ApiError(
        response.status,
        response.statusText,
        await response.text().catch(() => null),
      )
    }
    return response.text()
  },

  getPlants() {
    return request<BackendPlant[]>('GET', '/plants')
  },

  createPlant(body: CreatePlantRequest) {
    return request<void>('POST', '/plants', body)
  },

  generatePlantCareInfo(body: GenerateCareInfoRequest) {
    return request<CareInfoResponse>('POST', '/plants/care-info', body)
  },

  getPlant(plantId: number) {
    return request<BackendPlant>('GET', `/plants/${plantId}`)
  },

  getMeasurements(plantId: number) {
    return request<Measurement[]>('GET', `/plants/${plantId}/measurements`)
  },

  getLatestMeasurement(plantId: number) {
    return request<Measurement>('GET', `/plants/${plantId}/measurements/latest`)
  },

  updatePlantWateringSettings(plantId: number, body: UpdatePlantWateringSettingsRequest) {
    return request<void>('PUT', `/plants/${plantId}`, body)
  },

  getWateringEvents(plantId: number) {
    return request<WateringEvent[]>('GET', `/plants/${plantId}/watering-events`)
  },
}
