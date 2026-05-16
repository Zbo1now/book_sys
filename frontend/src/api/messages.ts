import { getApiData } from './http'

export async function fetchUnreadCount(): Promise<{ count: number }> {
  return getApiData<{ count: number }>('/api/messages/unread-count')
}

export async function markConversationRead(conversationId: string): Promise<{ readCount: number }> {
  return getApiData<{ readCount: number }>(`/api/messages/conversations/${conversationId}/read`, {
    method: 'PUT'
  })
}
