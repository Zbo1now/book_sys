import type { ApiResponse } from './types'

const TOKEN_KEY = 'auth_token'

export class ApiError extends Error {
  code: number

  constructor(code: number, message: string) {
    super(message)
    this.name = 'ApiError'
    this.code = code
  }
}

function getAuthHeader(): string | undefined {
  const token = localStorage.getItem(TOKEN_KEY)
  return token ? `Bearer ${token}` : undefined
}

export async function getApiData<T>(path: string, init?: RequestInit): Promise<T> {
  const isFormData = typeof FormData !== 'undefined' && init?.body instanceof FormData
  const existingHeaders = init?.headers as Record<string, string> | undefined

  const mergedHeaders: Record<string, string> = {
    ...existingHeaders
  }

  if (!isFormData && !mergedHeaders['Content-Type']) {
    mergedHeaders['Content-Type'] = 'application/json'
  }

  if (!mergedHeaders['Authorization']) {
    const authHeader = getAuthHeader()
    if (authHeader) {
      mergedHeaders['Authorization'] = authHeader
    }
  }

  const response = await fetch(path, {
    ...init,
    headers: mergedHeaders
  })

  let payload: ApiResponse<T> | null = null
  let responseText = ''
  try {
    responseText = await response.text()
    if (responseText) {
      payload = JSON.parse(responseText) as ApiResponse<T>
    }
  } catch (parseError) {
    console.error('Failed to parse JSON response:', parseError, 'Response text:', responseText.substring(0, 200))
  }

  if (!response.ok) {
    const message = payload?.message
      || (response.status === 401 ? '未授权，请重新登录' : `请求失败 (${response.status})`)
      || `HTTP ${response.status}`
    console.error(`API Error [${response.status}]: ${message}`, { path, status: response.status })
    throw new ApiError(payload?.code ?? response.status, message)
  }

  if (payload && payload.code !== 0) {
    console.warn(`API Response Error: ${payload.message}`, { code: payload.code, path })
    throw new ApiError(payload.code, payload.message || '请求失败')
  }

  if (payload) {
    return payload.data as T
  }

  console.warn('Empty response body', { path })
  return null as T
}
