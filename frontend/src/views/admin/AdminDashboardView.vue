<template>
  <section class="stat-grid">
    <article class="stat-card">
      <p>平台用户</p>
      <h2>{{ metrics.userCount }}</h2>
    </article>
    <article class="stat-card">
      <p>活跃用户</p>
      <h2>{{ metrics.activeUserCount }}</h2>
    </article>
    <article class="stat-card">
      <p>管理员数量</p>
      <h2>{{ metrics.adminCount }}</h2>
    </article>
    <article class="stat-card">
      <p>图书总量</p>
      <h2>{{ metrics.bookCount }}</h2>
    </article>
    <article class="stat-card">
      <p>书单总量</p>
      <h2>{{ metrics.booklistCount }}</h2>
    </article>
    <article class="stat-card">
      <p>活动总量</p>
      <h2>{{ metrics.activityCount }}</h2>
    </article>
  </section>

  <section class="panel" v-if="recentUsers.length">
    <h3>最新注册用户</h3>
    <div class="recent-list">
      <article class="recent-item" v-for="user in recentUsers" :key="user.id">
        <p class="name">{{ user.username }}</p>
        <p class="muted">{{ user.email }}</p>
        <p class="muted">角色：{{ user.role }} / 状态：{{ user.status === 1 ? '正常' : '禁用' }}</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { fetchAdminDashboard, type AdminUserDto } from '../../api/admin'

const metrics = reactive({
  userCount: 0,
  activeUserCount: 0,
  adminCount: 0,
  bookCount: 0,
  booklistCount: 0,
  activityCount: 0,
})
const recentUsers = ref<AdminUserDto[]>([])

onMounted(async () => {
  try {
    const dashboard = await fetchAdminDashboard()
    Object.assign(metrics, dashboard.metrics)
    recentUsers.value = dashboard.recentUsers
  } catch {
    // keep default values when API is unavailable
  }
})
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
}

.stat-card {
  background: rgba(255, 255, 255, 0.75);
  border-radius: var(--radius-md);
  padding: 18px;
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
}

.stat-card p {
  color: var(--muted);
  font-weight: 600;
}

.panel {
  margin-top: 10px;
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--border);
  padding: 18px;
}

.recent-list {
  margin-top: 12px;
  display: grid;
  gap: 10px;
}

.recent-item {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.6);
}

.name {
  font-weight: 700;
}
</style>
