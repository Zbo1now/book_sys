import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    id: 'u-1024',
    name: '林乔',
    title: '城市阅读者',
    avatar: '',
    stats: {
      following: 132,
      followers: 981,
      booklists: 18
    }
  })
})
