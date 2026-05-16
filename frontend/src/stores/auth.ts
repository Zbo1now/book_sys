import { defineStore } from 'pinia'

const TOKEN_KEY = 'auth_token'
const USER_KEY = 'auth_user'
const USER_ROLE_KEY = 'auth_role'
const USER_ID_KEY = 'auth_userId'
const AVATAR_KEY = 'auth_avatar'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    username: localStorage.getItem(USER_KEY) || '',
    userId: localStorage.getItem(USER_ID_KEY) || '',
    role: localStorage.getItem(USER_ROLE_KEY) || 'user' as 'user' | 'admin',
    avatarUrl: localStorage.getItem(AVATAR_KEY) || ''
  }),
  getters: {
    isAuthed: (state) => Boolean(state.token),
    isAdmin: (state) => state.role === 'admin'
  },
  actions: {
    login(token: string, username: string, userId?: string, role: 'user' | 'admin' = 'user', avatarUrl = '') {
      this.token = token
      this.username = username
      this.role = role
      this.avatarUrl = avatarUrl
      if (userId) {
        this.userId = userId
        localStorage.setItem(USER_ID_KEY, userId)
      }
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(USER_KEY, username)
      localStorage.setItem(USER_ROLE_KEY, role)
      localStorage.setItem(AVATAR_KEY, avatarUrl)
    },
    register(token: string, username: string) {
      this.login(token, username)
    },
    setAvatar(url: string) {
      this.avatarUrl = url
      localStorage.setItem(AVATAR_KEY, url)
    },
    logout() {
      this.token = ''
      this.username = ''
      this.userId = ''
      this.role = 'user'
      this.avatarUrl = ''
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(USER_KEY)
      localStorage.removeItem(USER_ID_KEY)
      localStorage.removeItem(USER_ROLE_KEY)
      localStorage.removeItem(AVATAR_KEY)
    }
  }
})
