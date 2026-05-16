<template>
  <section class="page-header">
    <div class="header-content">
      <h1>社交活动</h1>
      <p class="muted">浏览平台活动并参与，或发起新活动等待审批。</p>
    </div>
    <button class="btn primary" @click="showCreateModal = true">发起活动</button>
  </section>

  <section class="filter-section">
    <div class="filter-row">
      <button class="btn ghost small" :class="{ active: modeFilter === '' }" @click="modeFilter = ''">全部</button>
      <button class="btn ghost small" :class="{ active: modeFilter === 'online' }" @click="modeFilter = 'online'">线上</button>
      <button class="btn ghost small" :class="{ active: modeFilter === 'offline' }" @click="modeFilter = 'offline'">线下</button>
    </div>
    <div class="filter-row">
      <button class="btn ghost small" :class="{ active: statusFilter === '' }" @click="statusFilter = ''">全部状态</button>
      <button class="btn ghost small" :class="{ active: statusFilter === 'upcoming' }" @click="statusFilter = 'upcoming'">待开始</button>
      <button class="btn ghost small" :class="{ active: statusFilter === 'ongoing' }" @click="statusFilter = 'ongoing'">进行中</button>
      <button class="btn ghost small" :class="{ active: statusFilter === 'ended' }" @click="statusFilter = 'ended'">已结束</button>
    </div>
  </section>

  <section class="section-block">
    <div class="grid-3">
      <ActivityCard
        v-for="activity in filteredActivities"
        :key="activity.id"
        :activity="activity"
        show-signup
        :signing="signingId === activity.id"
        @signup="handleSignup"
        @cancel="handleCancel"
      />
    </div>
    <div v-if="filteredActivities.length === 0" class="empty-tip">
      暂无活动
    </div>
  </section>

  <AlertModal
    :visible="showAlert"
    :title="alertTitle"
    :message="alertMessage"
    :type="alertType"
    @close="showAlert = false"
  />

  <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
    <div class="modal-content">
      <div class="modal-header">
        <h2>发起新活动</h2>
        <button class="modal-close" @click="showCreateModal = false">×</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>活动名称 *</label>
          <input v-model.trim="createForm.title" placeholder="请输入活动名称" />
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>活动类型</label>
            <select v-model="createForm.activityType">
              <option value="online">线上活动</option>
              <option value="offline">线下活动</option>
            </select>
          </div>
          <div class="form-group">
            <label>活动地点</label>
            <input v-model.trim="createForm.location" placeholder="线上活动请填写链接" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label>开始时间</label>
            <input v-model="createForm.startDate" type="datetime-local" />
          </div>
          <div class="form-group">
            <label>结束时间</label>
            <input v-model="createForm.endDate" type="datetime-local" />
          </div>
        </div>
        <div class="form-group">
          <label>最大参与人数</label>
          <input v-model.number="createForm.maxParticipants" type="number" min="1" placeholder="不限制请留空" />
        </div>
        <div class="form-group">
          <label>活动描述</label>
          <textarea v-model.trim="createForm.description" placeholder="请描述活动内容..." rows="3"></textarea>
        </div>
        <p class="form-tip">活动提交后需等待管理员审批，审批通过后将在平台展示。</p>
      </div>
      <div class="modal-footer">
        <button class="btn ghost" @click="showCreateModal = false">取消</button>
        <button class="btn primary" @click="handleCreateActivity">提交活动</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import ActivityCard from '../components/ActivityCard.vue'
import AlertModal from '../components/AlertModal.vue'
import { fetchActivities, createActivity } from '../api/activities'
import { getApiData } from '../api/http'

const activities = ref<any[]>([])
const modeFilter = ref('')
const statusFilter = ref('')
const signingId = ref<string | null>(null)
const showCreateModal = ref(false)

const showAlert = ref(false)
const alertTitle = ref('提示')
const alertMessage = ref('')
const alertType = ref<'error' | 'warning' | 'info' | 'success'>('info')

const createForm = ref({
  title: '',
  description: '',
  activityType: 'offline' as 'online' | 'offline',
  location: '',
  startDate: '',
  endDate: '',
  maxParticipants: 0
})

function showMessage(title: string, message: string, type: 'error' | 'warning' | 'info' | 'success' = 'info') {
  alertTitle.value = title
  alertMessage.value = message
  alertType.value = type
  showAlert.value = true
}

function formatMode(location?: string) {
  if (!location) return '线上'
  if (location.includes('线上') && location.includes('线下')) return '线上+线下'
  if (location.includes('线下')) return '线下'
  return '线上'
}

const filteredActivities = computed(() => {
  let result = activities.value
  if (modeFilter.value) {
    result = result.filter(a => {
      if (modeFilter.value === 'online') return a.mode.includes('线上')
      if (modeFilter.value === 'offline') return a.mode.includes('线下')
      return true
    })
  }
  if (statusFilter.value) {
    result = result.filter(a => a.dynamicStatus === statusFilter.value)
  }
  return result
})

function formatDateTime(isoStr?: string) {
  if (!isoStr) return ''
  try {
    const d = new Date(isoStr)
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch {
    return isoStr
  }
}

onMounted(async () => {
  const result = await fetchActivities({ page: 1, pageSize: 100 })
  activities.value = result.items.map((item: any) => ({
    id: item.id,
    title: item.title,
    description: item.description,
    startDate: item.startDate || '',
    endDate: item.endDate || '',
    startDateDisplay: formatDateTime(item.startDate),
    endDateDisplay: formatDateTime(item.endDate),
    mode: formatMode(item.location),
    host: `主理：${item.organizer?.username || '平台'}`,
    attendees: item.participantCount ?? 0,
    isParticipating: item.isParticipating ?? false,
    dynamicStatus: item.dynamicStatus || 'upcoming'
  }))
})

async function handleSignup(id: string) {
  signingId.value = id
  try {
    const result = await getApiData<any>(`/api/activities/${id}/participate`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ action: 'join' })
    })
    const act = activities.value.find(a => a.id === id)
    if (act) {
      act.isParticipating = result.participating
      act.attendees = result.participantCount
    }
    showMessage('报名成功', '您已成功报名该活动', 'success')
  } catch (error: any) {
    showMessage('报名失败', error?.message || '报名失败，请稍后重试', 'warning')
  } finally {
    signingId.value = null
  }
}

async function handleCancel(id: string) {
    signingId.value = id
    try {
      const result = await getApiData<any>(`/api/activities/${id}/participate`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'leave' })
      })
      const act = activities.value.find(a => a.id === id)
      if (act) {
        act.isParticipating = result.participating
        act.attendees = result.participantCount
      }
      showMessage('取消成功', '您已取消报名该活动', 'info')
    } catch (error: any) {
      showMessage('取消失败', error?.message || '取消失败，请稍后重试', 'error')
    } finally {
      signingId.value = null
    }
  }

  async function handleCreateActivity() {
    if (!createForm.value.title.trim()) {
      showMessage('提示', '请输入活动名称', 'warning')
      return
    }
    try {
      await createActivity({
        title: createForm.value.title,
        description: createForm.value.description || undefined,
        activityType: createForm.value.activityType,
        location: createForm.value.location || undefined,
        startDate: createForm.value.startDate || undefined,
        endDate: createForm.value.endDate || undefined,
        maxParticipants: createForm.value.maxParticipants > 0 ? createForm.value.maxParticipants : undefined
      })
      showCreateModal.value = false
      createForm.value = {
        title: '',
        description: '',
        activityType: 'offline',
        location: '',
        startDate: '',
        endDate: '',
        maxParticipants: 0
      }
      showMessage('提交成功', '活动已提交，等待管理员审批', 'success')
    } catch (error: any) {
      showMessage('提交失败', error?.message || '提交失败，请稍后重试', 'error')
    }
  }
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1rem;
}

.header-content h1 {
  margin: 0 0 0.25rem 0;
}

.filter-section {
  display: flex;
  gap: 1rem;
  margin-bottom: 1.5rem;
  padding: 1rem;
  background: rgba(255, 255, 255, 0.8);
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.filter-row {
  display: flex;
  gap: 0.5rem;
}

.btn.ghost.active {
  background: var(--primary-color, #4a90a1);
  color: white;
  border-color: var(--primary-color, #4a90a1);
}

.empty-tip {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted, #888);
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 500px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e5e7eb;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.1rem;
}

.modal-close {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 2rem;
  height: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
}

.modal-body {
  padding: 1.25rem;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  padding: 1rem 1.25rem;
  border-top: 1px solid #e5e7eb;
}

.form-group {
  margin-bottom: 1rem;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.375rem;
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 0.5rem 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.9rem;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.form-tip {
  font-size: 0.8rem;
  color: #6b7280;
  margin-top: 0.5rem;
  padding: 0.75rem;
  background: #f9fafb;
  border-radius: 6px;
}
</style>
