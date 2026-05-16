<template>
  <section class="panel">
    <div class="filter-section">
      <div class="section-header">
        <h3>筛选条件</h3>
      </div>
      <div class="filter-content">
        <div class="filter-row">
          <div class="filter-item">
            <label>活动名称</label>
            <input v-model.trim="keywordFilter" placeholder="搜索活动名称" @keyup.enter="loadActivities" />
          </div>
          <div class="filter-item">
            <label>活动类型</label>
            <select v-model="locationFilter" @change="loadActivities">
              <option value="all">全部类型</option>
              <option value="线上">线上</option>
              <option value="线下">线下</option>
            </select>
          </div>
          <div class="filter-item">
            <label>审批状态</label>
            <select v-model="approvalStatusFilter" @change="loadActivities">
              <option value="all">全部审批状态</option>
              <option value="pending">待审批</option>
              <option value="approved">已通过</option>
              <option value="rejected">已拒绝</option>
            </select>
          </div>
          <div class="filter-item">
            <label>活动状态</label>
            <select v-model="statusFilter" @change="loadActivities">
              <option value="all">全部状态</option>
              <option value="upcoming">即将开始</option>
              <option value="ongoing">进行中</option>
              <option value="ended">已结束</option>
            </select>
          </div>
          <div class="filter-item">
            <label>开始时间</label>
            <input v-model="startDateFilter" type="date" @change="loadActivities" />
          </div>
        </div>
        <div class="filter-row">
          <div class="filter-item">
            <label>结束时间</label>
            <input v-model="endDateFilter" type="date" @change="loadActivities" />
          </div>
        </div>
        <div class="filter-summary">
          <span class="muted">共 {{ filteredActivities.length }} 条</span>
          <button class="btn ghost small" @click="resetFilters">重置筛选</button>
        </div>
      </div>
    </div>

    <div class="create-section">
      <div class="section-header">
        <h3>新增活动</h3>
      </div>
      <div class="create-form">
        <div class="form-group">
          <input v-model.trim="createForm.title" placeholder="活动名称" />
        </div>
        <div class="form-group">
          <select v-model="createForm.location">
            <option v-for="mode in locationOptions" :key="mode" :value="mode">{{ mode }}</option>
          </select>
        </div>
        <div class="form-group">
          <input v-model="createForm.startDate" type="datetime-local" class="datetime-input" placeholder="开始时间" />
        </div>
        <div class="form-group">
          <input v-model="createForm.endDate" type="datetime-local" class="datetime-input" placeholder="结束时间" />
        </div>
        <div class="form-group">
          <input v-model.trim="createForm.description" placeholder="活动描述" />
        </div>
        <div class="form-actions">
          <button class="btn primary small" @click="createActivity">新增活动</button>
        </div>
      </div>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>活动名</th>
            <th>发起者</th>
            <th>类别</th>
            <th>开始时间</th>
            <th>结束时间</th>
            <th>活动描述</th>
            <th>参与人数</th>
            <th>审批状态</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="activity in filteredActivities" :key="activity.id">
            <td><input v-model.trim="activity.title" class="inline-input" /></td>
            <td>{{ activity.organizerName || '-' }}</td>
            <td>
              <select v-model="activity.location" class="inline-input">
                <option v-for="mode in locationOptions" :key="mode" :value="mode">{{ mode }}</option>
              </select>
            </td>
            <td><input v-model="activity.startDate" type="datetime-local" class="datetime-input" /></td>
            <td><input v-model="activity.endDate" type="datetime-local" class="datetime-input" /></td>
            <td><input v-model.trim="activity.description" class="inline-input" /></td>
            <td>{{ activity.participantCount || 0 }}</td>
            <td>
              <span :class="['status-tag', activity.approvalStatus]">
                {{ getApprovalStatusLabel(activity.approvalStatus) }}
              </span>
            </td>
            <td>{{ activity.statusLabel }}</td>
            <td>
              <button v-if="activity.approvalStatus === 'pending'" class="btn primary small" @click="approveActivity(activity.id)">通过</button>
              <button v-if="activity.approvalStatus === 'pending'" class="btn danger small" @click="rejectActivity(activity.id)">拒绝</button>
              <button class="btn ghost small" @click="saveActivity(activity.id)">保存</button>
              <button class="btn ghost small" @click="advanceStatus(activity.id)">切换状态</button>
              <button class="btn danger small" @click="removeActivity(activity.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  createAdminActivity,
  deleteAdminActivity,
  fetchAdminActivities,
  updateAdminActivity,
  updateAdminActivityStatus,
  type AdminActivityDto
} from '../../api/admin'
import { useAlert } from '../../composables/useAlert'

type AdminActivity = AdminActivityDto & {
  statusLabel: string
}

const { showAlert, showConfirm } = useAlert()
const activities = ref<AdminActivity[]>([])
const statusFilter = ref('all')
const approvalStatusFilter = ref('all')
const locationFilter = ref('all')
const keywordFilter = ref('')
const startDateFilter = ref('')
const endDateFilter = ref('')
const statusOrder = ['upcoming', 'ongoing', 'ended']
const locationOptions = ['线上', '线下']
const createForm = ref({
  title: '',
  location: locationOptions[0],
  startDate: '',
  endDate: '',
  description: ''
})

const filteredActivities = computed(() => {
  return activities.value.filter((item) => {
    if (statusFilter.value !== 'all' && item.status !== statusFilter.value) {
      return false
    }
    if (locationFilter.value !== 'all' && item.location !== locationFilter.value) {
      return false
    }
    if (keywordFilter.value && !item.title.includes(keywordFilter.value)) {
      return false
    }
    if (startDateFilter.value) {
      const activityStart = item.startDate ? new Date(item.startDate) : null
      const filterStart = new Date(startDateFilter.value)
      if (activityStart && activityStart < filterStart) {
        return false
      }
    }
    if (endDateFilter.value) {
      const activityEnd = item.endDate ? new Date(item.endDate) : null
      const filterEnd = new Date(endDateFilter.value)
      filterEnd.setDate(filterEnd.getDate() + 1)
      if (activityEnd && activityEnd > filterEnd) {
        return false
      }
    }
    return true
  })
})

function resetFilters() {
  statusFilter.value = 'all'
  approvalStatusFilter.value = 'all'
  locationFilter.value = 'all'
  keywordFilter.value = ''
  startDateFilter.value = ''
  endDateFilter.value = ''
  loadActivities()
}

function formatStatus(status?: string) {
  if (status === 'ongoing') return '进行中'
  if (status === 'ended') return '已结束'
  return '即将开始'
}

async function advanceStatus(id: string) {
  const target = activities.value.find((item) => item.id === id)
  if (!target) return
  const current = statusOrder.indexOf(target.status || 'upcoming')
  const next = statusOrder[(current + 1) % statusOrder.length]
  try {
    const updated = await updateAdminActivityStatus(id, next)
    Object.assign(target, updated, { statusLabel: formatStatus(updated.status) })
  } catch (error: any) {
    showAlert('状态更新失败: ' + (error?.message || '未知错误'), 'error')
  }
}

async function saveActivity(id: string) {
  const target = activities.value.find((item) => item.id === id)
  if (!target) {
    showAlert('活动不存在', 'warning')
    return
  }
  try {
    const updated = await updateAdminActivity(id, {
      title: target.title,
      location: target.location,
      startDate: target.startDate,
      endDate: target.endDate,
      description: target.description
    })
    Object.assign(target, updated, { statusLabel: formatStatus(updated.status) })
    showAlert('保存成功', 'success')
  } catch (error: any) {
    showAlert('保存失败: ' + (error?.message || '未知错误'), 'error')
  }
}

async function removeActivity(id: string) {
  showConfirm('确定要删除这个活动吗？', async () => {
    try {
      await deleteAdminActivity(id)
      activities.value = activities.value.filter((item) => item.id !== id)
    } catch (error: any) {
      showAlert('删除失败: ' + (error?.message || '未知错误'), 'error')
    }
  })
}

function getApprovalStatusLabel(status?: string): string {
  if (status === 'approved') return '已通过'
  if (status === 'rejected') return '已拒绝'
  return '待审批'
}

async function approveActivity(id: string) {
  const target = activities.value.find((item) => item.id === id)
  if (!target) return
  try {
    await updateAdminActivity(id, { approvalStatus: 'approved' })
    target.approvalStatus = 'approved'
    showAlert('审批通过', 'success')
  } catch (error: any) {
    showAlert('审批失败: ' + (error?.message || '未知错误'), 'error')
  }
}

async function rejectActivity(id: string) {
  showConfirm('确定要拒绝这个活动吗？', async () => {
    const target = activities.value.find((item) => item.id === id)
    if (!target) return
    try {
      await updateAdminActivity(id, { approvalStatus: 'rejected' })
      target.approvalStatus = 'rejected'
      showAlert('已拒绝', 'info')
    } catch (error: any) {
      showAlert('操作失败: ' + (error?.message || '未知错误'), 'error')
    }
  })
}

async function createActivity() {
  if (!createForm.value.title) {
    showAlert('请输入活动名称', 'warning')
    return
  }
  try {
    await createAdminActivity({
      title: createForm.value.title,
      location: createForm.value.location,
      startDate: createForm.value.startDate || undefined,
      endDate: createForm.value.endDate || undefined,
      description: createForm.value.description
    })
    createForm.value = {
      title: '',
      location: locationOptions[0],
      startDate: '',
      endDate: '',
      description: ''
    }
    await loadActivities()
  } catch (error: any) {
    showAlert('创建失败: ' + (error?.message || '未知错误'), 'error')
  }
}

function formatDateTimeForInput(dateTime?: string): string {
  if (!dateTime) return ''
  const date = new Date(dateTime)
  if (isNaN(date.getTime())) return ''
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day}T${hours}:${minutes}`
}

async function loadActivities() {
  const result = await fetchAdminActivities({ 
    page: 1, 
    pageSize: 200, 
    status: statusFilter.value === 'all' ? undefined : statusFilter.value,
    approvalStatus: approvalStatusFilter.value === 'all' ? undefined : approvalStatusFilter.value
  })
  activities.value = result.items.map((item) => ({
    ...item,
    statusLabel: formatStatus(item.status),
    approvalStatus: item.approvalStatus || 'pending',
    startDate: formatDateTimeForInput(item.startDate),
    endDate: formatDateTimeForInput(item.endDate)
  }))
}

onMounted(loadActivities)
</script>

<style scoped>
.panel {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 20px;
}

.filter-section,
.create-section {
  background: #f9fafb;
  border-radius: 8px;
  margin-bottom: 20px;
  border: 1px solid #e5e7eb;
}

.section-header {
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f3f4f6;
  border-radius: 8px 8px 0 0;
}

.section-header h3 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

.filter-content {
  padding: 16px;
}

.filter-row {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr 1fr;
  gap: 16px;
  margin-bottom: 12px;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.filter-item label {
  font-size: 13px;
  color: #6b7280;
  font-weight: 500;
}

.filter-item input,
.filter-item select {
  width: 100%;
  max-width: 200px;
}

.filter-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px dashed #e5e7eb;
}

.create-form {
  display: grid;
  grid-template-columns: 1.2fr 0.8fr 1.2fr 1.2fr 1.5fr auto;
  gap: 12px;
  padding: 16px;
  align-items: center;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-group label {
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
}

input {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: #ffffff;
}

input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

select {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  background: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

select:focus {
  outline: none;
  border-color: #3b82f6;
}

.table-wrap {
  overflow: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th,
td {
  text-align: left;
  border-bottom: 1px solid var(--border);
  padding: 10px 8px;
}

.inline-input {
  min-width: 120px;
}

.datetime-input {
  min-width: 180px;
}

.status-tag {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-tag.pending {
  background: #fef3c7;
  color: #d97706;
}

.status-tag.approved {
  background: #dcfce7;
  color: #16a34a;
}

.status-tag.rejected {
  background: #fee2e2;
  color: #dc2626;
}

.btn.danger {
  background: #ef4444;
  color: white;
  border-color: #ef4444;
}

.btn.danger:hover {
  background: #dc2626;
  border-color: #dc2626;
}
</style>
