import type { PagedResult } from './types'
import { getApiData } from './http'

export interface UserProfile {
  id: string
  username: string
  email?: string
  avatar: string
  title: string
  bio: string
  stats: {
    following: number
    followers: number
    booklists: number
  }
  joinDate?: string
}

export interface ProfileUpdateData {
  username?: string
  title?: string
  bio?: string
  avatar?: string
}

export async function fetchMe(): Promise<UserProfile> {
  return getApiData<UserProfile>('/api/users/profile/me')
}

export async function updateProfile(data: ProfileUpdateData): Promise<{ updatedAt: string }> {
  return getApiData<{ updatedAt: string }>('/api/users/profile', {
    method: 'PUT',
    body: JSON.stringify(data)
  })
}

export async function fetchUser(userId: string): Promise<UserProfile> {
  return getApiData<UserProfile>(`/api/users/${encodeURIComponent(userId)}`)
}

export interface FeedItem {
  type: string
  title: string
  meta: string
}

export async function fetchActivity(page = 1, pageSize = 20, userId?: string): Promise<PagedResult<FeedItem>> {
  const params = new URLSearchParams()
  params.set('page', String(page))
  params.set('pageSize', String(pageSize))
  if (userId) params.set('userId', userId)
  return getApiData<PagedResult<FeedItem>>(`/api/users/activity?${params.toString()}`)
}

export async function exportProfile(): Promise<Blob> {
  const response = await fetch('/api/users/profile/export')
  if (!response.ok) {
    throw new Error('导出失败')
  }
  return response.blob()
}
