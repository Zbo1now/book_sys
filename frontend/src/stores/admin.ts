import { defineStore } from 'pinia'
import { loginAdmin } from '../api/admin'

export const ADMIN_TOKEN_KEY = 'admin_auth_token'
export const ADMIN_NAME_KEY = 'admin_auth_name'

export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem(ADMIN_TOKEN_KEY) || '',
    adminName: localStorage.getItem(ADMIN_NAME_KEY) || ''
  }),
  getters: {
    isAdminAuthed: (state) => Boolean(state.token)
  },
  actions: {
    async login(account: string, password: string) {
      const data = await loginAdmin(account, password)
      this.token = data.token
      this.adminName = data.username
      localStorage.setItem(ADMIN_TOKEN_KEY, data.token)
      localStorage.setItem(ADMIN_NAME_KEY, this.adminName)
    },
    logout() {
      this.token = ''
      this.adminName = ''
      localStorage.removeItem(ADMIN_TOKEN_KEY)
      localStorage.removeItem(ADMIN_NAME_KEY)
    }
  }
})
