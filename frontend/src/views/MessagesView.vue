<template>
  <section class="page-header">
    <div>
      <h1>站内信</h1>
      <p class="muted">与用户、社群及活动负责人保持沟通。</p>
    </div>
    <button class="btn primary" @click="openNewConversation">新建会话</button>
  </section>

  <section class="section-block">
    <MessageThread
      :threads="threads"
      :active-id="activeThreadId"
      @select="selectThread"
      @send="sendMessage"
    />
  </section>

  <!-- 新建会话对话框 -->
  <div v-if="showNewConversation" class="modal-overlay" @click.self="closeModal">
    <div class="modal">
      <div class="modal-header">
        <h3>📨 发送私信</h3>
        <button class="close-btn" @click="closeModal">✕</button>
      </div>

      <div class="modal-body">
        <div v-if="!selectedUser" class="search-section">
          <label class="section-label">搜索用户</label>
          <div class="search-input-wrap">
            <input
              v-model.trim="searchKeyword"
              type="text"
              placeholder="输入用户名搜索..."
              class="modal-input"
              @input="debouncedSearch"
            />
            <span v-if="searching" class="search-loading">搜索中...</span>
          </div>

          <div v-if="searchResults.length > 0" class="search-results">
            <div
              v-for="user in searchResults"
              :key="user.id"
              class="user-item"
              @click="selectUser(user)"
            >
              <div class="user-avatar">{{ user.username.substring(0, 2).toUpperCase() }}</div>
              <div class="user-info">
                <span class="user-name">{{ user.username }}</span>
                <span class="user-title">{{ user.title || '书友' }}</span>
              </div>
            </div>
          </div>
          <div v-else-if="searchKeyword && !searching" class="no-results">
            未找到用户
          </div>
        </div>

        <div v-else class="message-section">
          <div class="selected-user">
            <span>发送给：</span>
            <strong>{{ selectedUser.username }}</strong>
            <button class="btn-link" @click="clearSelectedUser">更换</button>
          </div>

          <label class="section-label">消息内容</label>
          <textarea
            v-model.trim="newMessageContent"
            placeholder="输入想要发送的消息..."
            class="modal-textarea"
            rows="4"
          ></textarea>
        </div>
      </div>

      <div class="modal-footer">
        <button class="btn ghost" @click="closeModal">取消</button>
        <button
          v-if="!selectedUser"
          class="btn primary"
          disabled
        >
          下一步
        </button>
        <button
          v-else
          class="btn primary"
          :disabled="!newMessageContent.trim() || sending"
          @click="sendNewMessage"
        >
          {{ sending ? '发送中...' : '发送' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import MessageThread from '../components/MessageThread.vue'
import { getApiData } from '../api/http'
import { useAlert } from '../composables/useAlert'

interface MessageItem {
  id: string
  from: 'me' | 'them'
  text: string
  timestamp?: string
}

interface ThreadItem {
  id: string
  name: string
  initials: string
  preview: string
  status: string
  participantId?: string
  participantEmail?: string
  messages: MessageItem[]
}

interface SearchUser {
  id: string
  username: string
  title: string
  avatar: string
}

const { showAlert } = useAlert()
const threads = ref<ThreadItem[]>([])
const activeThreadId = ref<string>('')
const showNewConversation = ref(false)
const searchKeyword = ref('')
const searchResults = ref<SearchUser[]>([])
const searching = ref(false)
const selectedUser = ref<SearchUser | null>(null)
const newMessageContent = ref('')
const sending = ref(false)

let searchTimer: ReturnType<typeof setTimeout> | null = null

async function loadConversations() {
  try {
    const result = await getApiData<any>('/api/messages?page=1&pageSize=50')
    if (result.items && result.items.length > 0) {
      threads.value = result.items.map((item: any) => ({
        id: item.id,
        name: item.participant?.username || '未知用户',
        initials: (item.participant?.username || '?').substring(0, 2).toUpperCase(),
        preview: item.lastMessage || '',
        status: item.unreadCount > 0 ? `未读 ${item.unreadCount}` : '在线',
        participantId: item.participant?.id,
        participantEmail: item.participant?.email,
        messages: []
      }))
      if (threads.value.length > 0) {
        activeThreadId.value = threads.value[0].id
        await loadConversationDetail(threads.value[0].id)
      }
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

async function loadConversationDetail(conversationId: string) {
  try {
    const result = await getApiData<any>(`/api/messages/conversations/${conversationId}?page=1&pageSize=50`)
    const thread = threads.value.find(t => t.id === conversationId)
    if (thread && result.messages) {
      thread.messages = result.messages.map((msg: any) => ({
        id: msg.id,
        from: msg.sender?.id === 'me' || msg.isMine ? 'me' : 'them',
        text: msg.content,
        timestamp: msg.timestamp
      }))
      if (thread.messages.length > 0) {
        thread.preview = thread.messages[thread.messages.length - 1].text
      }
    }
  } catch (error) {
    console.error('加载会话详情失败:', error)
  }
}

function selectThread(threadId: string) {
  activeThreadId.value = threadId
  loadConversationDetail(threadId)
}

async function sendMessage(content: string) {
  if (!content.trim() || !activeThreadId.value) return
  try {
    const thread = threads.value.find(t => t.id === activeThreadId.value)
    if (!thread) return

    const result = await getApiData<any>('/api/messages/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        conversationId: activeThreadId.value,
        content: content
      })
    })

    thread.messages.push({
      id: result.id || 'temp-' + Date.now(),
      from: 'me',
      text: content,
      timestamp: result.timestamp || new Date().toISOString()
    })
    thread.preview = content
  } catch (error: any) {
    showAlert(error?.message || '发送失败', 'error')
  }
}

function openNewConversation() {
  showNewConversation.value = true
  searchKeyword.value = ''
  searchResults.value = []
  selectedUser.value = null
  newMessageContent.value = ''
}

function closeModal() {
  showNewConversation.value = false
  searchKeyword.value = ''
  searchResults.value = []
  selectedUser.value = null
  newMessageContent.value = ''
}

function debouncedSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  searching.value = true
  searchTimer = setTimeout(() => {
    searchUsers()
  }, 300)
}

async function searchUsers() {
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    searching.value = false
    return
  }
  try {
    const result = await getApiData<any>(`/api/users/search?keyword=${encodeURIComponent(searchKeyword.value)}&page=1&pageSize=10`)
    searchResults.value = result.items || []
  } catch (error) {
    console.error('搜索用户失败:', error)
    searchResults.value = []
  } finally {
    searching.value = false
  }
}

function selectUser(user: SearchUser) {
  selectedUser.value = user
  searchKeyword.value = ''
  searchResults.value = []
}

function clearSelectedUser() {
  selectedUser.value = null
}

async function sendNewMessage() {
  if (!selectedUser.value || !newMessageContent.value.trim()) {
    showAlert('请填写消息内容', 'warning')
    return
  }
  sending.value = true
  try {
    const userId = selectedUser.value.id.replace('u-', '')
    const result = await getApiData<any>('/api/messages/send', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        recipientId: userId,
        content: newMessageContent.value
      })
    })

    threads.value.unshift({
      id: result.conversationId || 'conv-' + userId,
      name: selectedUser.value.username,
      initials: selectedUser.value.username.substring(0, 2).toUpperCase(),
      preview: newMessageContent.value,
      status: '在线',
      participantId: selectedUser.value.id,
      messages: [{
        id: result.id || 'temp-' + Date.now(),
        from: 'me',
        text: newMessageContent.value,
        timestamp: result.timestamp || new Date().toISOString()
      }]
    })

    showAlert('发送成功！', 'success')
    closeModal()
  } catch (error: any) {
    showAlert(error?.message || '发送失败', 'error')
  } finally {
    sending.value = false
  }
}

onMounted(loadConversations)
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 1.5rem;
}

.page-header h1 {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.modal {
  background: white;
  border-radius: 20px;
  width: 440px;
  max-width: 90vw;
  overflow: hidden;
  animation: slideUp 0.3s ease;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.2rem;
  font-weight: 600;
  color: #1a1a1a;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #888;
  padding: 0.25rem;
  border-radius: 4px;
  transition: all 0.2s;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 1.25rem 1.5rem;
}

.section-label {
  display: block;
  margin-bottom: 0.6rem;
  font-weight: 600;
  color: #333;
  font-size: 0.9rem;
}

.search-input-wrap {
  position: relative;
}

.modal-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.2s;
  box-sizing: border-box;
}

.modal-input:focus {
  outline: none;
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.search-loading {
  position: absolute;
  right: 1rem;
  top: 50%;
  transform: translateY(-50%);
  color: #888;
  font-size: 0.85rem;
}

.search-results {
  margin-top: 0.75rem;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  max-height: 240px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background 0.15s;
}

.user-item:hover {
  background: #f5f9fa;
}

.user-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.user-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a90a4 0%, #6bb3c4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.85rem;
  flex-shrink: 0;
}

.user-info {
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 0.95rem;
}

.user-title {
  font-size: 0.8rem;
  color: #888;
}

.no-results {
  text-align: center;
  padding: 1.5rem;
  color: #888;
  font-size: 0.9rem;
}

.selected-user {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #f5f9fa;
  border-radius: 10px;
  margin-bottom: 1rem;
  font-size: 0.9rem;
  color: #333;
}

.selected-user strong {
  color: #4a90a4;
}

.btn-link {
  background: none;
  border: none;
  color: #4a90a4;
  cursor: pointer;
  font-size: 0.85rem;
  margin-left: auto;
  text-decoration: underline;
}

.btn-link:hover {
  color: #357a8a;
}

.modal-textarea {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 0.95rem;
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
  transition: all 0.2s;
  box-sizing: border-box;
}

.modal-textarea:focus {
  outline: none;
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding: 1rem 1.5rem;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
  background: #fafafa;
}

.modal-footer .btn {
  min-width: 80px;
}
</style>
