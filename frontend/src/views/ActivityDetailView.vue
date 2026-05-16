<template>
  <section class="detail-layout single-column">
    <div class="detail-body">
      <div class="activity-hero">
        <div class="activity-status-row">
          <span :class="['status-pill', activity.dynamicStatus]">{{ statusLabel }}</span>
          <span v-if="activity.approvalStatus && activity.approvalStatus !== 'approved'" :class="['status-pill', 'approval-' + activity.approvalStatus]">
            {{ approvalLabel }}
          </span>
          <span class="pill type-pill">{{ activity.activityType === 'online' ? '线上' : '线下' }}</span>
        </div>
        <h1>{{ activity.title }}</h1>
        <p class="host-info">
          发起人：<span class="host-name clickable" @click="openUserInfo(activity.organizer?.id)">{{ activity.organizer?.username || '未知' }}</span>
        </p>
      </div>

      <div class="activity-meta-grid">
        <div class="meta-item">
          <span class="meta-icon">📍</span>
          <span>{{ activity.location || '待定' }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-icon">🕐</span>
          <span>{{ formattedStart }} ~ {{ formattedEnd }}</span>
        </div>
        <div class="meta-item">
          <span class="meta-icon">👥</span>
          <span>{{ activity.participantCount || 0 }} / {{ activity.maxParticipants || '不限' }} 人</span>
        </div>
      </div>

      <div class="section-block">
        <h3>活动详情</h3>
        <p class="description-text">{{ activity.description || '暂无描述' }}</p>
      </div>

      <div class="section-block" v-if="isAuthed">
        <div class="signup-area">
          <button
            v-if="!activity.isParticipating"
            class="btn primary"
            :disabled="signing || activity.dynamicStatus === 'ended'"
            @click="handleSignup"
          >
            {{ signupButtonText }}
          </button>
          <template v-else>
            <button class="btn ghost" :disabled="signing" @click="handleCancel">
              {{ signing ? '取消中...' : '取消报名' }}
            </button>
            <span class="joined-hint">已报名</span>
          </template>
          <p v-if="activity.dynamicStatus === 'ended'" class="muted ended-hint">活动已结束</p>
        </div>
      </div>
      <div v-else class="section-block">
        <p class="muted auth-hint">
          <RouterLink :to="{ name: 'auth', query: { redirect: $route.fullPath } }">登录</RouterLink>后参与活动
        </p>
      </div>

      <div class="section-block" v-if="participants.length > 0">
        <h3>已报名用户 ({{ participants.length }})</h3>
        <div class="participant-list">
          <div
            v-for="p in participants"
            :key="p.id"
            class="participant-item clickable"
            @click="openUserInfo(p.id)"
          >
            <UserAvatar :avatar-url="p.avatar" :username="p.username" :size="36" />
            <span class="participant-name">{{ p.username }}</span>
          </div>
        </div>
      </div>
      <div v-else class="section-block">
        <p class="muted">暂无报名用户</p>
      </div>
    </div>

    <UserInfoModal
      :visible="showUserInfoModal"
      :user-id="selectedInfoUserId"
      @close="showUserInfoModal = false"
    />
  </section>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { fetchActivityDetail, participateActivity, type ActivityDto } from '../api/activities'
import { useAuthStore } from '../stores/auth'
import { useAlert } from '../composables/useAlert'
import UserAvatar from '../components/UserAvatar.vue'
import UserInfoModal from '../components/UserInfoModal.vue'

const route = useRoute()
const authStore = useAuthStore()
const { showAlert } = useAlert()

const activity = ref<ActivityDto & { participants?: { id: string; username: string; avatar: string }[] }>({
  id: '',
  title: '加载中...',
  description: ''
})
const signing = ref(false)

const showUserInfoModal = ref(false)
const selectedInfoUserId = ref('')

const isAuthed = computed(() => authStore.isAuthed)

const statusLabel = computed(() => {
  switch (activity.value.dynamicStatus) {
    case 'upcoming': return '待开始'
    case 'ongoing': return '进行中'
    case 'ended': return '已结束'
    default: return '待开始'
  }
})

const approvalLabel = computed(() => {
  switch (activity.value.approvalStatus) {
    case 'pending': return '待审核'
    case 'rejected': return '已拒绝'
    default: return ''
  }
})

const signupButtonText = computed(() => {
  if (signing.value) return '报名中...'
  if (activity.value.dynamicStatus === 'ended') return '活动已结束'
  return '立即报名'
})

const formattedStart = computed(() => formatDateTime(activity.value.startDate))
const formattedEnd = computed(() => formatDateTime(activity.value.endDate))

const participants = computed(() => (activity.value as any).participants || [])

function formatDateTime(isoStr?: string) {
  if (!isoStr) return '待定'
  try {
    const d = new Date(isoStr)
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch {
    return isoStr
  }
}

function openUserInfo(userId: string | undefined) {
  if (!userId) return
  selectedInfoUserId.value = userId
  showUserInfoModal.value = true
}

async function load() {
  const id = String(route.params.id || '')
  if (!id) return
  try {
    activity.value = await fetchActivityDetail(id)
  } catch (error: any) {
    console.error('加载活动详情失败:', error)
  }
}

async function handleSignup() {
  const id = String(route.params.id || '')
  if (!id) return
  signing.value = true
  try {
    const result = await participateActivity(id, 'join')
    activity.value.isParticipating = result.participating
    activity.value.participantCount = result.participantCount
    // reload to get updated participant list
    await load()
    showAlert('报名成功', 'success')
  } catch (error: any) {
    showAlert(error?.message || '报名失败', 'warning')
  } finally {
    signing.value = false
  }
}

async function handleCancel() {
  const id = String(route.params.id || '')
  if (!id) return
  signing.value = true
  try {
    const result = await participateActivity(id, 'leave')
    activity.value.isParticipating = result.participating
    activity.value.participantCount = result.participantCount
    await load()
    showAlert('已取消报名', 'info')
  } catch (error: any) {
    showAlert(error?.message || '取消失败', 'error')
  } finally {
    signing.value = false
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<style scoped>
.single-column {
  grid-template-columns: 1fr;
}

.clickable {
  cursor: pointer;
}
.clickable:hover {
  opacity: 0.8;
}

.activity-hero {
  margin-bottom: 1.5rem;
}

.activity-status-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 0.75rem;
}

.status-pill {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: 600;
}

.status-pill.upcoming {
  background: #e0f2fe;
  color: #0369a1;
}

.status-pill.ongoing {
  background: #dcfce7;
  color: #166534;
}

.status-pill.ended {
  background: #fef2f2;
  color: #dc2626;
}

.status-pill.approval-pending {
  background: #fef3c7;
  color: #d97706;
}

.status-pill.approval-rejected {
  background: #fee2e2;
  color: #dc2626;
}

.type-pill {
  background: #f3f4f6;
  color: #6b7280;
}

.host-info {
  color: #666;
  font-size: 0.95rem;
}

.host-name {
  color: var(--primary-color, #4a90a4);
  font-weight: 500;
}

.activity-meta-grid {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  gap: 1rem;
  padding: 1.25rem;
  background: #f9fafb;
  border-radius: 12px;
  margin-bottom: 1.5rem;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 0.9rem;
  color: #555;
}

.meta-icon {
  font-size: 1.1rem;
}

.description-text {
  line-height: 1.7;
  color: #444;
  white-space: pre-wrap;
}

.signup-area {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.joined-hint {
  color: #16a34a;
  font-weight: 500;
  font-size: 0.9rem;
}

.ended-hint {
  margin-top: 0.5rem;
}

.auth-hint {
  font-size: 0.9rem;
}

.participant-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 0.75rem;
}

.participant-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 0.75rem;
  background: #f5f5f5;
  border-radius: 8px;
  transition: background 0.15s;
}

.participant-item:hover {
  background: #e8f4f8;
}

.participant-name {
  font-size: 0.9rem;
  font-weight: 500;
}
</style>
