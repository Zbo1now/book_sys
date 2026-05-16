import { getApiData } from './http'
import type { PagedResult } from './types'
import { ADMIN_TOKEN_KEY } from '../stores/admin'

export interface AdminAuthDto {
  userId: number
  username: string
  email: string
  token: string
}

export interface AdminUserDto {
  id: string
  username: string
  email: string
  title: string
  bio: string
  phone: string
  role: 'USER' | 'ADMIN'
  status: number
  createdAt: string
  updatedAt: string
}

export interface AdminDashboardDto {
  metrics: {
    userCount: number
    activeUserCount: number
    adminCount: number
    bookCount: number
    booklistCount: number
    activityCount: number
  }
  recentUsers: AdminUserDto[]
}

export interface AdminBookDto {
  id: string
  title: string
  author: string
  tag: string
  description: string
  featured: boolean
  rating: string
  reviews: number
  cover: string
}

export interface AdminBooklistDto {
  id: string
  title: string
  description: string
  cover?: string
  creator?: { id: string; username: string }
  bookIds?: string[]
  bookCount?: number
  followers?: number
  isPublic?: boolean
}

export interface AdminActivityDto {
  id: string
  title: string
  description: string
  cover?: string
  startDate?: string
  endDate?: string
  location?: string
  participantCount?: number
  maxParticipants?: number
  status?: string
  approvalStatus?: string
  organizerName?: string
}

function authHeaders() {
  const headers: Record<string, string> = {}
  const token = localStorage.getItem(ADMIN_TOKEN_KEY)
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  return headers
}

export async function loginAdmin(account: string, password: string): Promise<AdminAuthDto> {
  return getApiData<AdminAuthDto>('/api/admin/auth/login', {
    method: 'POST',
    body: JSON.stringify({ account, password })
  })
}

export async function fetchAdminDashboard(): Promise<AdminDashboardDto> {
  return getApiData<AdminDashboardDto>('/api/admin/dashboard', {
    headers: authHeaders()
  })
}

export async function fetchAdminUsers(params?: {
  page?: number
  pageSize?: number
  keyword?: string
  status?: number
}): Promise<PagedResult<AdminUserDto>> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.keyword) searchParams.set('keyword', params.keyword)
  if (typeof params?.status === 'number') searchParams.set('status', String(params.status))

  const qs = searchParams.toString()
  return getApiData<PagedResult<AdminUserDto>>(`/api/admin/users${qs ? `?${qs}` : ''}`, {
    headers: authHeaders()
  })
}

export async function updateAdminUser(
  userId: string,
  payload: Partial<Pick<AdminUserDto, 'username' | 'email' | 'title' | 'bio' | 'phone' | 'role'>>
): Promise<AdminUserDto> {
  return getApiData<AdminUserDto>(`/api/admin/users/${encodeURIComponent(userId)}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload)
  })
}

export async function updateAdminUserStatus(userId: string, status: number) {
  return getApiData<{ id: string; status: number }>(
    `/api/admin/users/${encodeURIComponent(userId)}/status?status=${status === 1 ? 1 : 0}`,
    {
      method: 'PUT',
      headers: authHeaders()
    }
  )
}

export async function fetchAdminBooks(params?: {
  page?: number
  pageSize?: number
  keyword?: string
  tag?: string
}): Promise<PagedResult<AdminBookDto>> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.keyword) searchParams.set('keyword', params.keyword)
  if (params?.tag) searchParams.set('tag', params.tag)

  const qs = searchParams.toString()
  return getApiData<PagedResult<AdminBookDto>>(`/api/admin/books${qs ? `?${qs}` : ''}`, {
    headers: authHeaders()
  })
}

export async function fetchAdminBookCategories(): Promise<string[]> {
  return getApiData<string[]>('/api/admin/books/categories', {
    headers: authHeaders()
  })
}

export async function updateAdminBook(
  bookId: string,
  payload: Partial<Pick<AdminBookDto, 'title' | 'author' | 'tag' | 'description' | 'featured'>> | FormData
): Promise<AdminBookDto> {
  const isFormData = payload instanceof FormData
  return getApiData<AdminBookDto>(`/api/admin/books/${encodeURIComponent(bookId)}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: isFormData ? payload : JSON.stringify(payload),
  })
}

export async function deleteAdminBook(bookId: string): Promise<{ id: string; deleted: boolean }> {
  return getApiData<{ id: string; deleted: boolean }>(`/api/admin/books/${encodeURIComponent(bookId)}`, {
    method: 'DELETE',
    headers: authHeaders()
  })
}

export async function createAdminBook(form: FormData): Promise<AdminBookDto> {
  return getApiData<AdminBookDto>('/api/admin/books', {
    method: 'POST',
    headers: authHeaders(),
    body: form
  })
}

export async function fetchAdminBooklists(params?: { page?: number; pageSize?: number }) {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  const qs = searchParams.toString()
  return getApiData<{ total: number; page: number; pageSize: number; items: AdminBooklistDto[] }>(
    `/api/admin/booklists${qs ? `?${qs}` : ''}`,
    { headers: authHeaders() }
  )
}

export async function createAdminBooklist(payload: {
  title: string
  description?: string
  cover?: string
  isPublic?: boolean
  bookIds?: string[]
}) {
  return getApiData<AdminBooklistDto>('/api/admin/booklists', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload)
  })
}

export async function updateAdminBooklist(booklistId: string, payload: {
  title?: string
  description?: string
  cover?: string
  isPublic?: boolean
  bookIds?: string[]
}) {
  return getApiData<AdminBooklistDto>(`/api/admin/booklists/${encodeURIComponent(booklistId)}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload)
  })
}

export async function deleteAdminBooklist(booklistId: string) {
  return getApiData<void>(`/api/admin/booklists/${encodeURIComponent(booklistId)}`, {
    method: 'DELETE',
    headers: authHeaders()
  })
}

export async function fetchAdminActivities(params?: { page?: number; pageSize?: number; status?: string; approvalStatus?: string }) {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.status) searchParams.set('status', params.status)
  if (params?.approvalStatus) searchParams.set('approvalStatus', params.approvalStatus)
  const qs = searchParams.toString()
  return getApiData<{ total: number; page: number; pageSize: number; items: AdminActivityDto[] }>(
    `/api/admin/activities${qs ? `?${qs}` : ''}`,
    { headers: authHeaders() }
  )
}

export async function createAdminActivity(payload: {
  title: string
  description?: string
  cover?: string
  startDate?: string
  endDate?: string
  location?: string
  maxParticipants?: number
}) {
  return getApiData<AdminActivityDto>('/api/admin/activities', {
    method: 'POST',
    headers: authHeaders(),
    body: JSON.stringify(payload)
  })
}

export async function updateAdminActivity(activityId: string, payload: {
  title?: string
  description?: string
  cover?: string
  startDate?: string
  endDate?: string
  location?: string
  maxParticipants?: number
  approvalStatus?: string
}) {
  return getApiData<AdminActivityDto>(`/api/admin/activities/${encodeURIComponent(activityId)}`, {
    method: 'PUT',
    headers: authHeaders(),
    body: JSON.stringify(payload)
  })
}

export async function deleteAdminActivity(activityId: string) {
  return getApiData<void>(`/api/admin/activities/${encodeURIComponent(activityId)}`, {
    method: 'DELETE',
    headers: authHeaders()
  })
}

export async function updateAdminActivityStatus(activityId: string, status: string): Promise<AdminActivityDto> {
  return getApiData<AdminActivityDto>(`/api/admin/activities/${encodeURIComponent(activityId)}/status?status=${encodeURIComponent(status)}`, {
    method: 'PUT',
    headers: authHeaders()
  })
}
