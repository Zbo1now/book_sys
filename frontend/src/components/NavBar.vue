<template>
  <header class="nav-bar">
    <div class="nav-inner">
      <div class="brand">
        <div class="brand-mark">SY</div>
        <div>
          <p class="brand-title">ShuYou</p>
          <p class="brand-subtitle">书友交流平台</p>
        </div>
      </div>
      <nav class="nav-links">
        <RouterLink to="/" class="nav-link">总览</RouterLink>
        <RouterLink to="/books" class="nav-link">图书</RouterLink>
        <RouterLink to="/booklists" class="nav-link">书单</RouterLink>
        <RouterLink to="/activities" class="nav-link">活动</RouterLink>
        <RouterLink to="/my-activities" class="nav-link">我的活动</RouterLink>
        <RouterLink to="/messages" class="nav-link">
            站内信
            <span v-if="unreadCount > 0" class="unread-badge">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
          </RouterLink>
        <RouterLink to="/profile" class="nav-link">个人中心</RouterLink>
      </nav>
      <div class="nav-actions">
        <RouterLink v-if="!isAuthed" :to="authLink" class="btn primary">登录/注册</RouterLink>
        <template v-else>
          <RouterLink to="/profile" class="nav-user-info">
            <UserAvatar :avatar-url="authStore.avatarUrl" :username="authStore.username" :size="32" />
            <span class="nav-username">{{ authStore.username }}</span>
          </RouterLink>
          <button class="btn ghost" type="button" @click="handleLogout">退出登录</button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { useMessageUnread } from '../composables/useMessageUnread'
import UserAvatar from './UserAvatar.vue'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const { unreadCount, refresh: refreshUnread } = useMessageUnread()

const isAuthed = computed(() => authStore.isAuthed)

const authLink = computed(() => ({
  name: 'auth',
  query: {
    redirect: route.fullPath
  }
}))

onMounted(() => {
  if (authStore.isAuthed) {
    refreshUnread()
  }
})

async function handleLogout() {
  authStore.logout()
  if (route.name !== 'auth') {
    await router.push({ name: 'auth' })
  }
}
</script>

<style scoped>
.nav-user-info {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  text-decoration: none;
  color: #333;
  padding: 0.25rem 0.5rem;
  border-radius: 8px;
  transition: background 0.15s;
}

.nav-user-info:hover {
  background: rgba(74, 144, 164, 0.08);
}

.nav-username {
  font-weight: 500;
  font-size: 0.9rem;
  white-space: nowrap;
}

.nav-link {
  position: relative;
}

.unread-badge {
  position: absolute;
  top: -8px;
  right: -12px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  background: #e74c3c;
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  line-height: 18px;
  text-align: center;
  border-radius: 9px;
  white-space: nowrap;
}
</style>
