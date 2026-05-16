export const BOOK_CATEGORIES = [
  '文学小说',
  '历史传记',
  '计算机编程',
  '心理学',
  '经济学',
  '少儿绘本',
  '科普科幻',
  '管理学'
] as const

export const BOOK_CATEGORIES_WITH_ALL = ['全部', ...BOOK_CATEGORIES] as const
