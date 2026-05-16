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
        <RouterLink to="/messages" class="nav-link">站内信</RouterLink>
        <RouterLink to="/profile" class="nav-link">个人中心</RouterLink>
      </nav>
      <div class="nav-actions">
        <RouterLink v-if="!isAuthed" :to="authLink" class="btn primary">登录/注册</RouterLink>
        <template v-else>
          <button class="btn ghost" type="button" @click="handleLogout">退出登录</button>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const isAuthed = computed(() => authStore.isAuthed)

const authLink = computed(() => ({
  name: 'auth',
  query: {
    redirect: route.fullPath
  }
}))

async function handleLogout() {
  authStore.logout()
  if (route.name !== 'auth') {
    await router.push({ name: 'auth' })
  }
}
</script>
