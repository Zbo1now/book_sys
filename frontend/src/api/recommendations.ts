import { getApiData } from './http'

export interface RecommendedBook {
  id: string
  title: string
  author: string
  cover: string
  rating: string
  tag: string
  reviews: number
}

export interface RecommendationResult {
  items: RecommendedBook[]
  reason: string
  total: number
}

export async function fetchRecommendations(limit?: number): Promise<RecommendationResult> {
  const qs = limit ? `?limit=${limit}` : ''
  return getApiData<RecommendationResult>(`/api/recommendations${qs}`)
}
