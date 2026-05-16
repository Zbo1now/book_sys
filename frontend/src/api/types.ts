export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export interface PagedResult<T> {
  total: number
  page: number
  pageSize: number
  items: T[]
}

export interface BookDto {
  id: string
  title: string
  author: string
  rating: string
  reviews: number
  tag: string
  cover: string
  description: string
}

export interface BookDetailDto extends BookDto {
  publishDate?: string
  publisher?: string
  pages?: number
  isbn?: string
  reviewList?: Array<{
    id: number
    rating: number
    content: string
    userName: string
    createdAt: string
    likeCount?: number
    liked?: boolean
  }>
}

export interface BooklistSummaryDto {
  id: string
  title: string
  description: string
  cover?: string
  creator?: { id: string; username: string; avatar?: string; title?: string }
  bookCount?: number
  followers?: number
  rating?: number
}

export interface BooklistDetailDto extends BooklistSummaryDto {
  bookIds?: string[]
  likedUsers?: Array<{ id: string; username: string }>
  comments?: Array<{ id: string; username: string; content: string }>
  books?: Array<{
    id: string
    title: string
    author: string
    cover?: string
    rating?: string | number
  }>
}
