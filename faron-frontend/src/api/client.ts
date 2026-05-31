import { apiPath } from './config'

export async function apiRequest(path: string, options: RequestInit = {}) {
  const response = await fetch(apiPath(path), {
    headers: {
      Accept: 'application/json',
      ...options.headers,
    },
    ...options,
  })

  if (!response.ok) {
    throw new Error(`API request failed with status ${response.status}`)
  }

  if (response.status === 204) {
    return null
  }

  return response.json()
}

export function getJson(path: string, options: RequestInit = {}) {
  return apiRequest(path, {
    method: 'GET',
    ...options,
  })
}
