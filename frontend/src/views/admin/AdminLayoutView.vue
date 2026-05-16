<template>
  <div class="admin-shell">
    <aside class="admin-side">
      <div class="admin-brand">
        <p class="badge">ShuYou Admin</p>
        <h2>管理后台</h2>
      </div>

      <nav class="admin-nav">
        <RouterLink to="/admin/dashboard" class="admin-link">概览</RouterLink>
        <RouterLink to="/admin/books" class="admin-link">图书管理</RouterLink>
        <RouterLink to="/admin/booklists" class="admin-link">书单管理</RouterLink>
        <RouterLink to="/admin/activities" class="admin-link">活动管理</RouterLink>
        <RouterLink to="/admin/users" class="admin-link">用户管理</RouterLink>
      </nav>

      <button class="btn ghost" type="button" @click="handleLogout">退出后台</button>
    </aside>

    <main class="admin-main">
      <header class="admin-top">
        <h1>{{ title }}</h1>
        <p class="muted">当前登录：{{ adminStore.adminName || '管理员' }}</p>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAdminStore } from '../../stores/admin'
import { useAuthStore } from '../../stores/auth'

const route = useRoute()
const adminStore = useAdminStore()
const authStore = useAuthStore()

const title = computed(() => String(route.meta.title || '后台管理'))

function handleLogout() {
  adminStore.logout()
  authStore.logout()
  window.location.href = '/admin/login'
}
</script>

<style scoped>
.admin-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 240px 1fr;
  background: radial-gradient(circle at top left, #fff6eb 0%, var(--bg) 45%, var(--bg-deep) 100%);
}

.admin-side {
  background: rgba(255, 249, 242, 0.82);
  color: var(--ink);
  border-right: 1px solid var(--border);
  backdrop-filter: blur(8px);
  padding: 24px 18px;
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.badge {
  font-size: 0.8rem;
  opacity: 0.9;
  color: var(--muted);
}

.admin-brand h2 {
  margin-top: 8px;
}

.admin-nav {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.admin-link {
  padding: 10px 12px;
  border-radius: 10px;
  color: var(--muted);
}

.admin-link.router-link-active {
  background: rgba(211, 111, 62, 0.14);
  color: var(--ink);
}

.admin-main {
  padding: 26px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.admin-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

@media (max-width: 960px) {
  .admin-shell {
    grid-template-columns: 1fr;
  }

  .admin-side {
    border-bottom-left-radius: 18px;
    border-bottom-right-radius: 18px;
    border-right: none;
    border-bottom: 1px solid var(--border);
  }
}
</style>
