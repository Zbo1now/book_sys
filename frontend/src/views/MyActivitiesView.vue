<template>
  <section class="page-header">
    <div class="header-content">
      <h1>我的活动</h1>
      <p class="muted">查看您发起的活动及审批状态。</p>
    </div>
  </section>

  <section class="filter-section">
    <div class="filter-row">
      <button class="btn ghost small" :class="{ active: approvalFilter === '' }" @click="approvalFilter = ''; loadMyActivities()">全部</button>
      <button class="btn ghost small" :class="{ active: approvalFilter === 'pending' }" @click="approvalFilter = 'pending'; loadMyActivities()">待审批</button>
      <button class="btn ghost small" :class="{ active: approvalFilter === 'approved' }" @click="approvalFilter = 'approved'; loadMyActivities()">已通过</button>
      <button class="btn ghost small" :class="{ active: approvalFilter === 'rejected' }" @click="approvalFilter = 'rejected'; loadMyActivities()">已拒绝</button>
    </div>
  </section>

  <section class="section-block">
    <div class="grid-3">
      <ActivityCard
        v-for="activity in activities"
        :key="activity.id"
        :activity="activity"
        show-signup
        :signing="signingId === activity.id"
        @signup="handleSignup"
        @cancel="handleCancel"
      />
    </div>
    <div v-if="activities.length === 0" class="empty-tip">
      暂无活动
    </div>
  </section>

  <AlertModal
    :visible="showAlert"
    :title="alertTitle"
    :message="alertMessage"
    :type="alertType"
    :buttonText="alertType === 'warning' ? '去登录' : '我知道了'"
    @close="showAlert = false"
    @primary="handleAlertPrimary"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import ActivityCard from '../components/ActivityCard.vue'
import AlertModal from '../components/AlertModal.vue'
import { fetchMyActivities } from '../api/activities'
import { getApiData } from '../api/http'

const router = useRouter()
const activities = ref<any[]>([])
const approvalFilter = ref('')
const signingId = ref<string | null>(null)
const loading = ref(true)

const showAlert = ref(false)
const alertTitle = ref('提示')
const alertMessage = ref('')
const alertType = ref<'error' | 'warning' | 'info' | 'success'>('info')

function showMessage(title: string, message: string, type: 'error' | 'warning' | 'info' | 'success' = 'info') {
  alertTitle.value = title
  alertMessage.value = message
  alertType.value = type
  showAlert.value = true
}

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

async function loadMyActivities() {
  loading.value = true
  try {
    const result = await fetchMyActivities({ 
      page: 1, 
      pageSize: 100,
      approvalStatus: approvalFilter.value || undefined 
    })
    activities.value = result.items.map((item: any) => ({
      id: item.id,
      title: item.title,
      description: item.description,
      startDate: item.startDate || '',
      endDate: item.endDate || '',
      startDateDisplay: formatDateTime(item.startDate),
      endDateDisplay: formatDateTime(item.endDate),
      mode: item.location?.includes('线上') ? '线上' : '线下',
      host: `主理：${item.organizer?.username || '平台'}`,
      attendees: item.participantCount ?? 0,
      isParticipating: item.isParticipating ?? false,
      dynamicStatus: item.dynamicStatus || 'upcoming',
      approvalStatus: item.approvalStatus || 'pending'
    }))
  } catch (error: any) {
    if (error?.message === '未授权，请先登录') {
      alertTitle.value = '提示'
      alertMessage.value = '请先登录后再查看我的活动'
      alertType.value = 'warning'
      showAlert.value = true
    } else {
      showMessage('加载失败', error?.message || '无法加载活动数据', 'error')
    }
  } finally {
    loading.value = false
  }
}

function goToLogin() {
  showAlert.value = false
  router.push({ name: 'auth', query: { redirect: '/my-activities' } })
}

function handleAlertPrimary() {
  if (alertType.value === 'warning') {
    goToLogin()
  }
}

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

onMounted(loadMyActivities)
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
</style>
