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

export interface FollowUserItem {
  id: string
  username: string
  avatar: string
  title: string
  isFollowing: boolean
}

export async function fetchFollowing(userId: string, page = 1, pageSize = 20): Promise<PagedResult<FollowUserItem>> {
  const params = new URLSearchParams()
  params.set('page', String(page))
  params.set('pageSize', String(pageSize))
  return getApiData<PagedResult<FollowUserItem>>(`/api/users/${encodeURIComponent(userId)}/following?${params.toString()}`)
}

export async function fetchFollowers(userId: string, page = 1, pageSize = 20): Promise<PagedResult<FollowUserItem>> {
  const params = new URLSearchParams()
  params.set('page', String(page))
  params.set('pageSize', String(pageSize))
  return getApiData<PagedResult<FollowUserItem>>(`/api/users/${encodeURIComponent(userId)}/followers?${params.toString()}`)
}

export async function followUser(userId: string): Promise<{ following: boolean; followerCount: number }> {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<{ following: boolean; followerCount: number }>(
    `/api/users/${encodeURIComponent(userId)}/follow`,
    {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: JSON.stringify({ action: 'follow' })
    }
  )
}

export async function unfollowUser(userId: string): Promise<{ following: boolean; followerCount: number }> {
  const token = localStorage.getItem('auth_token') || ''
  return getApiData<{ following: boolean; followerCount: number }>(
    `/api/users/${encodeURIComponent(userId)}/follow`,
    {
      method: 'POST',
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body: JSON.stringify({ action: 'unfollow' })
    }
  )
}

export async function uploadAvatar(file: File): Promise<{ avatarUrl: string }> {
  const token = localStorage.getItem('auth_token') || ''
  const formData = new FormData()
  formData.append('file', file)
  const response = await fetch('/api/users/profile/avatar', {
    method: 'POST',
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    body: formData
  })
  if (!response.ok) {
    const err = await response.json().catch(() => ({ message: '上传失败' }))
    throw new Error((err as { message?: string }).message || '上传失败')
  }
  const json = await response.json() as { code: number; data: { avatarUrl: string } }
  return json.data
}

export interface SearchUserItem {
  id: string
  username: string
  avatar: string
  title: string
}

export async function searchUsers(keyword: string, page = 1, pageSize = 20): Promise<PagedResult<SearchUserItem>> {
  const params = new URLSearchParams()
  params.set('keyword', keyword)
  params.set('page', String(page))
  params.set('pageSize', String(pageSize))
  return getApiData<PagedResult<SearchUserItem>>(`/api/users/search?${params.toString()}`)
}
