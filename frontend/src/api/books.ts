import type { BookDetailDto, BookDto, PagedResult } from './types'
import { getApiData } from './http'

export async function fetchBooks(params?: {
  page?: number
  pageSize?: number
  search?: string
  tag?: string
}): Promise<PagedResult<BookDto>> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.search) searchParams.set('search', params.search)
  if (params?.tag) searchParams.set('tag', params.tag)

  const qs = searchParams.toString()
  return getApiData<PagedResult<BookDto>>(`/api/books${qs ? `?${qs}` : ''}`)
}

export async function fetchFeaturedBooks(params?: { page?: number; pageSize?: number }): Promise<PagedResult<BookDto>> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))

  const qs = searchParams.toString()
  return getApiData<PagedResult<BookDto>>(`/api/books/featured${qs ? `?${qs}` : ''}`)
}

export async function fetchBookDetail(bookId: string): Promise<BookDetailDto> {
  return getApiData<BookDetailDto>(`/api/books/${encodeURIComponent(bookId)}`)
}

export interface BookReview {
  id: number
  rating: number
  content: string
  userName: string
  createdAt: string
  likeCount?: number
  liked?: boolean
}

export async function fetchBookReviews(bookId: string, params?: { page?: number; pageSize?: number; sortBy?: string }): Promise<BookReview[]> {
  const searchParams = new URLSearchParams()
  if (params?.page) searchParams.set('page', String(params.page))
  if (params?.pageSize) searchParams.set('pageSize', String(params.pageSize))
  if (params?.sortBy) searchParams.set('sortBy', params.sortBy)

  const qs = searchParams.toString()
  return getApiData<BookReview[]>(`/api/books/${encodeURIComponent(bookId)}/reviews${qs ? `?${qs}` : ''}`)
}

export async function addBookReview(bookId: string, data: { rating: number; content?: string }): Promise<BookReview> {
  return getApiData<BookReview>(`/api/books/${encodeURIComponent(bookId)}/reviews`, {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export async function likeBookReview(bookId: string, reviewId: number): Promise<{ reviewId: number; liked: boolean; likeCount: number }> {
  return getApiData<{ reviewId: number; liked: boolean; likeCount: number }>(
    `/api/books/${encodeURIComponent(bookId)}/reviews/${reviewId}/like`,
    { method: 'POST' }
  )
}

export async function unlikeBookReview(bookId: string, reviewId: number): Promise<void> {
  return getApiData<void>(
    `/api/books/${encodeURIComponent(bookId)}/reviews/${reviewId}/like`,
    { method: 'DELETE' }
  )
}

export async function deleteBookReview(bookId: string, reviewId: number): Promise<void> {
  return getApiData<void>(
    `/api/books/${encodeURIComponent(bookId)}/reviews/${reviewId}`,
    { method: 'DELETE' }
  )
}

export async function fetchBookCategories(): Promise<string[]> {
  return getApiData<string[]>('/api/books/categories')
}

export async function fetchMyBookReview(bookId: string): Promise<BookReview | null> {
  return getApiData<BookReview | null>(`/api/books/${encodeURIComponent(bookId)}/my-review`)
}
