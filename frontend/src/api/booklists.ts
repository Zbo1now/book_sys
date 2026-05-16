import type { BooklistDetailDto, BooklistSummaryDto, PagedResult } from './types'
import { getApiData } from './http'

export async function fetchBooklists(params?: { page?: number; pageSize?: number; scope?: 'hall' | 'personal' | 'all' }): Promise<PagedResult<BooklistSummaryDto>> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.scope) searchParams.set('scope', params.scope)

  const qs = searchParams.toString()
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<PagedResult<BooklistSummaryDto>>(`/api/booklists${qs ? `?${qs}` : ''}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
}

export async function fetchBooklistDetail(booklistId: string): Promise<BooklistDetailDto> {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<BooklistDetailDto>(`/api/booklists/${encodeURIComponent(booklistId)}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
}

export async function createBooklist(payload: {
  title: string
  description?: string
  cover?: string
  isPublic?: boolean
  bookIds?: string[]
}) {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<BooklistDetailDto>('/api/booklists', {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: JSON.stringify(payload)
  })
}

export async function updateBooklist(
  booklistId: string,
  payload: {
    title?: string
    description?: string
    cover?: string
    isPublic?: boolean
    bookIds?: string[]
  }
) {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<BooklistDetailDto>(`/api/booklists/${encodeURIComponent(booklistId)}`, {
    method: 'PUT',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: JSON.stringify(payload)
  })
}

export async function deleteBooklist(booklistId: string) {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<void>(`/api/booklists/${encodeURIComponent(booklistId)}`, {
    method: 'DELETE',
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
}

export async function createBooklistComment(booklistId: string, content: string) {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<{ id: string; username: string; content: string; createdAt: string }>(
    `/api/booklists/${encodeURIComponent(booklistId)}/comments`,
    {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: JSON.stringify({ content })
    }
  )
}

export async function toggleBooklistLike(booklistId: string): Promise<{ liked: boolean; likeCount: number }> {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<{ liked: boolean; likeCount: number }>(
    `/api/booklists/${encodeURIComponent(booklistId)}/like`,
    {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    }
  )
}

export async function toggleBooklistFollow(booklistId: string): Promise<{ followed: boolean; followerCount: number }> {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<{ followed: boolean; followerCount: number }>(
    `/api/booklists/${encodeURIComponent(booklistId)}/follow`,
    {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {}
    }
  )
}

export async function fetchCollectedBooklists() {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<{ total: number; items: BooklistSummaryDto[] }>('/api/booklists/collected', {
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })
}
