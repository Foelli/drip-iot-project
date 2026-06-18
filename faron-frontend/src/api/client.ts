const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1'

export class ApiError extends Error {
  constructor(public status: number, message: string, public body: unknown) {
    super(message)
    this.name = 'ApiError'
  }
}

  async function request<T>(method  : string, path: string, body: unknown): Promise<T> {
    const response = await fetch(`${BASE_URL}${path}`, {
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

    return response.json()
  }

  const api
