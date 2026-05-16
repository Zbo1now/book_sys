<template>
  <section class="panel">
    <div class="toolbar">
      <input v-model.trim="keyword" placeholder="搜索用户名或邮箱" @keyup.enter="loadUsers()" />
      <div class="toolbar-right">
        <select v-model="statusFilter" @change="loadUsers()">
          <option value="all">全部状态</option>
          <option value="1">正常</option>
          <option value="0">禁用</option>
        </select>
        <button class="btn ghost small" @click="loadUsers()">查询</button>
        <span class="muted">共 {{ total }} 条</span>
      </div>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>用户ID</th>
            <th>用户名</th>
            <th>邮箱</th>
            <th>身份</th>
            <th>账号状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="user in users" :key="user.id">
            <td>{{ user.id.replace('u-', '') }}</td>
            <td>
              <input 
                v-if="editingId === user.id" 
                v-model.trim="user.username" 
                class="inline-input" 
                autofocus
              />
              <span v-else>{{ user.username }}</span>
            </td>
            <td>
              <input 
                v-if="editingId === user.id" 
                v-model.trim="user.email" 
                class="inline-input" 
              />
              <span v-else>{{ user.email }}</span>
            </td>
            <td>
              <select 
                v-if="editingId === user.id" 
                v-model="user.role" 
                class="inline-select"
              >
                <option value="USER">USER</option>
                <option value="ADMIN">ADMIN</option>
              </select>
              <span v-else :class="user.role === 'ADMIN' ? 'role-admin' : 'role-user'">{{ user.role }}</span>
            </td>
            <td>
              <span :class="user.enabled ? 'status on' : 'status off'">{{ user.enabled ? '正常' : '禁用' }}</span>
            </td>
            <td>
              <template v-if="editingId === user.id">
                <button class="btn primary small" @click="saveUser(user)">保存</button>
                <button class="btn cancel small" @click="cancelEdit">取消</button>
              </template>
              <template v-else>
                <button class="btn edit small" @click="editUser(user.id)">编辑</button>
                <button class="btn danger small" @click="toggleUser(user.id)">
                  {{ user.enabled ? '禁用' : '恢复' }}
                </button>
              </template>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="total > pageSize">
      <button class="btn ghost small" :disabled="currentPage === 1" @click="firstPage">首页</button>
      <button class="btn ghost small" :disabled="currentPage === 1" @click="prevPage">上一页</button>
      <span class="page-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
      <div class="page-jump">
        <input 
          type="number" 
          v-model.number="jumpPage" 
          min="1" 
          :max="totalPages"
          @keyup.enter="jumpToPage"
          placeholder="页码"
        />
        <button class="btn ghost small" @click="jumpToPage">跳转</button>
      </div>
      <button class="btn ghost small" :disabled="currentPage >= totalPages" @click="nextPage">下一页</button>
      <button class="btn ghost small" :disabled="currentPage >= totalPages" @click="lastPage">尾页</button>
      <select v-model="pageSize" @change="handlePageSizeChange">
        <option :value="10">10条/页</option>
        <option :value="20">20条/页</option>
        <option :value="50">50条/页</option>
      </select>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { fetchAdminUsers, updateAdminUser, updateAdminUserStatus, type AdminUserDto } from '../../api/admin'
import { useAlert } from '../../composables/useAlert'

type AdminUser = {
  id: string
  username: string
  email: string
  role: 'USER' | 'ADMIN'
  enabled: boolean
}

const { showAlert } = useAlert()
const users = ref<AdminUser[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const jumpPage = ref(1)
const editingId = ref<string | null>(null)

const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

const keyword = ref('')
const statusFilter = ref('all')

function mapUser(item: AdminUserDto): AdminUser {
  return {
    id: item.id,
    username: item.username,
    email: item.email,
    role: item.role,
    enabled: item.status === 1
  }
}

async function loadUsers(resetPage = true) {
  if (resetPage) {
    currentPage.value = 1
  }
  try {
    const status = statusFilter.value === 'all' ? undefined : Number(statusFilter.value)
    const result = await fetchAdminUsers({ 
      page: currentPage.value, 
      pageSize: pageSize.value, 
      keyword: keyword.value || undefined, 
      status 
    })
    users.value = result.items.map(mapUser)
    total.value = result.total
  } catch (e: any) {
    showAlert('加载失败: ' + (e?.message || '未知错误'), 'error')
  }
}

function prevPage() {
  if (currentPage.value > 1) {
    currentPage.value--
    loadUsers(false)
  }
}

function nextPage() {
  if (currentPage.value < totalPages.value) {
    currentPage.value++
    loadUsers(false)
  }
}

function handlePageSizeChange() {
  loadUsers(true)
}

function firstPage() {
  if (currentPage.value !== 1) {
    currentPage.value = 1
    loadUsers(false)
  }
}

function lastPage() {
  if (currentPage.value !== totalPages.value) {
    currentPage.value = totalPages.value
    loadUsers(false)
  }
}

function jumpToPage() {
  const targetPage = jumpPage.value
  if (targetPage && targetPage >= 1 && targetPage <= totalPages.value && targetPage !== currentPage.value) {
    currentPage.value = targetPage
    loadUsers(false)
  }
}

function editUser(id: string) {
  editingId.value = id
}

function cancelEdit() {
  editingId.value = null
  loadUsers(false)
}

async function toggleUser(id: string) {
  const target = users.value.find((item) => item.id === id)
  if (!target) return
  const nextStatus = target.enabled ? 0 : 1
  try {
    await updateAdminUserStatus(id, nextStatus)
    target.enabled = !target.enabled
    showAlert(nextStatus === 1 ? '用户已恢复' : '用户已禁用', nextStatus === 1 ? 'success' : 'info')
  } catch (e: any) {
    showAlert('操作失败: ' + (e?.message || '未知错误'), 'error')
  }
}

function isValidEmail(email: string): boolean {
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return emailRegex.test(email)
}

async function saveUser(user: AdminUser) {
  if (!user.email || user.email.trim() === '') {
    showAlert('邮箱不能为空', 'error')
    return
  }
  if (!isValidEmail(user.email)) {
    showAlert('邮箱格式不正确', 'error')
    return
  }
  try {
    const latest = await updateAdminUser(user.id, {
      username: user.username,
      email: user.email,
      role: user.role
    })
    Object.assign(user, mapUser(latest))
    editingId.value = null
    showAlert('保存成功', 'success')
  } catch (e: any) {
    showAlert('保存失败: ' + (e?.message || '未知错误'), 'error')
  }
}

loadUsers()
</script>

<style scoped>
.panel {
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid #e5e7eb;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #f3f4f6;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toolbar input {
  width: min(360px, 100%);
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 14px;
  font-size: 14px;
  transition: all 0.2s ease;
  background: #ffffff;
}

.toolbar input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

.toolbar select {
  border: 1px solid #d1d5db;
  border-radius: 8px;
  padding: 10px 12px;
  background: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.toolbar select:focus {
  outline: none;
  border-color: #3b82f6;
}

.table-wrap {
  overflow: auto;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

table {
  width: 100%;
  border-collapse: collapse;
}

thead {
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

th {
  text-align: left;
  padding: 14px 16px;
  font-weight: 600;
  color: #374151;
  font-size: 14px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  border-bottom: 2px solid #e5e7eb;
  white-space: nowrap;
}

td {
  text-align: left;
  padding: 14px 16px;
  border-bottom: 1px solid #f3f4f6;
  font-size: 14px;
  color: #4b5563;
  transition: background-color 0.2s ease;
}

tbody tr:hover td {
  background-color: #f9fafb;
}

tbody tr:last-child td {
  border-bottom: none;
}

.inline-input {
  width: 100%;
  max-width: 200px;
  min-width: 100px;
  padding: 6px 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
  background: #ffffff;
  transition: all 0.2s ease;
}

.inline-input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.inline-select {
  min-width: 80px;
  max-width: 120px;
  padding: 6px 8px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 13px;
  background: #ffffff;
  cursor: pointer;
  transition: all 0.2s ease;
}

.inline-select:focus {
  outline: none;
  border-color: #3b82f6;
}

table {
  table-layout: fixed;
}

th:nth-child(1), td:nth-child(1) { width: 10%; min-width: 80px; }
th:nth-child(2), td:nth-child(2) { width: 18%; }
th:nth-child(3), td:nth-child(3) { width: 25%; }
th:nth-child(4), td:nth-child(4) { width: 12%; }
th:nth-child(5), td:nth-child(5) { width: 12%; }
th:nth-child(6), td:nth-child(6) { width: 23%; }

td {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
}

.status.on {
  color: #059669;
  background: #d1fae5;
}

.status.on::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #059669;
}

.status.off {
  color: #dc2626;
  background: #fee2e2;
}

.status.off::before {
  content: '';
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #dc2626;
}

.pagination {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 8px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid #f3f4f6;
}

.pagination button {
  min-width: 36px;
  height: 36px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  transition: all 0.2s ease;
}

.pagination button:hover:not(:disabled) {
  background: #eff6ff;
  color: #3b82f6;
}

.pagination button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
  background: #f9fafb;
}

.page-info {
  font-size: 14px;
  color: #6b7280;
  min-width: 80px;
  text-align: center;
}

.page-jump {
  display: flex;
  align-items: center;
  gap: 6px;
}

.page-jump input {
  width: 56px;
  height: 36px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 8px;
  font-size: 13px;
  text-align: center;
  transition: all 0.2s ease;
}

.page-jump input:focus {
  outline: none;
  border-color: #3b82f6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
}

.pagination select {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 10px;
  background: #ffffff;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.pagination select:focus {
  outline: none;
  border-color: #3b82f6;
}

.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
  border: none;
}

.btn.ghost {
  background: transparent;
  border: 1px solid #d1d5db;
  color: #4b5563;
}

.btn.ghost:hover {
  background: #f3f4f6;
}

.btn.ghost.small {
  padding: 6px 12px;
  font-size: 13px;
}

.btn.primary {
  background: #3b82f6;
  color: #ffffff;
  border: none;
}

.btn.primary:hover {
  background: #2563eb;
}

.btn.ghost.primary {
  background: #eff6ff;
  border-color: #3b82f6;
  color: #3b82f6;
}

.btn.ghost.primary:hover {
  background: #dbeafe;
}

.role-admin {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  background: #fef3c7;
  color: #d97706;
  font-size: 12px;
  font-weight: 600;
}

.role-user {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  background: #e0e7ff;
  color: #4f46e5;
  font-size: 12px;
  font-weight: 600;
}

.btn.edit {
  background: #60a5fa;
  color: #ffffff;
  border: none;
}

.btn.edit:hover {
  background: #3b82f6;
}

.btn.cancel {
  background: #9ca3af;
  color: #ffffff;
  border: none;
}

.btn.cancel:hover {
  background: #6b7280;
}

.btn.danger {
  background: #f87171;
  color: #ffffff;
  border: none;
}

.btn.danger:hover {
  background: #ef4444;
}

.muted {
  color: #9ca3af;
  font-size: 14px;
}
</style>
