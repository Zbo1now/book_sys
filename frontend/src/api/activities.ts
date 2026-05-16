import type { PagedResult } from './types'
import { getApiData } from './http'

export interface ActivityDto {
  id: string
  title: string
  description: string
  cover?: string
  organizer?: { id: string; username: string }
  startDate?: string
  endDate?: string
  location?: string
  participantCount?: number
  maxParticipants?: number
  status?: string
  dynamicStatus?: string
  isParticipating?: boolean
  approvalStatus?: string
  activityType?: string
}

export interface CreateActivityRequest {
  title: string
  description?: string
  cover?: string
  startDate?: string
  endDate?: string
  location?: string
  maxParticipants?: number
  activityType?: 'online' | 'offline'
}

export async function createActivity(request: CreateActivityRequest): Promise<ActivityDto> {
  return getApiData<ActivityDto>('/api/activities', {
    method: 'POST',
    body: JSON.stringify(request)
  })
}

export async function fetchActivityDetail(id: string): Promise<ActivityDto> {
  return getApiData<ActivityDto>(`/api/activities/${encodeURIComponent(id)}`)
}

export async function participateActivity(id: string, action: 'join' | 'leave'): Promise<{ participating: boolean; participantCount: number }> {
  return getApiData<{ participating: boolean; participantCount: number }>(`/api/activities/${encodeURIComponent(id)}/participate`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ action })
  })
}

export async function fetchActivities(params?: {
  page?: number
  pageSize?: number
  status?: string
}): Promise<PagedResult<ActivityDto>> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.status) searchParams.set('status', params.status)

  const qs = searchParams.toString()
  return getApiData<PagedResult<ActivityDto>>(`/api/activities${qs ? `?${qs}` : ''}`)
}

export async function fetchMyActivities(params?: {
  page?: number
  pageSize?: number
  approvalStatus?: string
}): Promise<PagedResult<ActivityDto>> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.approvalStatus) searchParams.set('approvalStatus', params.approvalStatus)

  const qs = searchParams.toString()
  return getApiData<PagedResult<ActivityDto>>(`/api/activities/my${qs ? `?${qs}` : ''}`)
}
