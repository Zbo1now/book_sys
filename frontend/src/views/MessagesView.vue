<template>
  <section class="page-header">
    <div>
      <h1>站内信</h1>
      <p class="muted">与用户、社群及活动负责人保持沟通。</p>
    </div>
  </section>

  <section class="section-block">
    <div class="search-bar">
      <input
        v-model.trim="searchKeyword"
        type="text"
        placeholder="搜索用户..."
        class="search-input"
        @input="debouncedSearch"
      />
      <div v-if="searchResults.length > 0" class="search-dropdown">
        <div
          v-for="user in searchResults"
          :key="user.id"
          class="search-user-item"
          @click="openUserInfo(user.id)"
        >
          <UserAvatar :avatar-url="user.avatar" :username="user.username" :size="36" />
          <div>
            <span class="search-user-name">{{ user.username }}</span>
            <span class="search-user-title">{{ user.title || '书友' }}</span>
          </div>
        </div>
      </div>
      <div v-else-if="searchKeyword && !searching" class="search-dropdown">
        <p class="no-results">未找到用户</p>
      </div>
    </div>

    <MessageThread
      :threads="threads"
      :active-id="activeThreadId"
      @select="selectThread"
      @send="sendMessage"
    />
  </section>

  <UserInfoModal
    :visible="showUserInfoModal"
    :user-id="selectedInfoUserId"
    @close="showUserInfoModal = false"
  />
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import MessageThread from '../components/MessageThread.vue'
import UserAvatar from '../components/UserAvatar.vue'
import UserInfoModal from '../components/UserInfoModal.vue'
import { getApiData } from '../api/http'
import { useAlert } from '../composables/useAlert'
import { markConversationRead } from '../api/messages'
import { useMessageUnread } from '../composables/useMessageUnread'

const route = useRoute()

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
  unreadCount: number
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
const { setCount: setGlobalUnread, refresh: refreshGlobalUnread } = useMessageUnread()
const threads = ref<ThreadItem[]>([])
const activeThreadId = ref<string>('')
const searchKeyword = ref('')
const searchResults = ref<SearchUser[]>([])
const searching = ref(false)
const showUserInfoModal = ref(false)
const selectedInfoUserId = ref('')

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
        unreadCount: item.unreadCount || 0,
        participantId: item.participant?.id,
        participantEmail: item.participant?.email,
        messages: []
      }))
    }
  } catch (error) {
    console.error('加载会话列表失败:', error)
  }
}

async function loadConversationDetail(conversationId: string) {
  try {
    const result = await getApiData<any>(`/api/messages/conversations/${conversationId}?page=1&pageSize=50`)
    const thread = threads.value.find(t => t.id === conversationId)
    if (!thread) return
    if (result.participant?.username) {
      thread.name = result.participant.username
      thread.initials = result.participant.username.substring(0, 2).toUpperCase()
      thread.participantId = result.participant.id
    }
    if (result.messages) {
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
  const thread = threads.value.find(t => t.id === threadId)
  if (thread && thread.unreadCount > 0) {
    markConversationRead(threadId).then((result) => {
      thread.unreadCount = 0
      thread.status = '在线'
      setGlobalUnread(result.readCount)
    }).catch(() => {})
  }
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

function debouncedSearch() {
  if (searchTimer) clearTimeout(searchTimer)
  if (!searchKeyword.value.trim()) {
    searchResults.value = []
    return
  }
  searching.value = true
  searchTimer = setTimeout(() => {
    doSearch()
  }, 300)
}

async function doSearch() {
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

function openUserInfo(userId: string) {
  selectedInfoUserId.value = userId
  showUserInfoModal.value = true
  searchKeyword.value = ''
  searchResults.value = []
}

async function openConversation(convId: string) {
  let thread = threads.value.find(t => t.id === convId)
  if (!thread) {
    thread = {
      id: convId,
      name: '加载中...',
      initials: '?',
      preview: '',
      status: '在线',
      unreadCount: 0,
      messages: []
    }
    threads.value.unshift(thread)
  }
  activeThreadId.value = convId
  await loadConversationDetail(convId)
}

onMounted(async () => {
  await loadConversations()
  refreshGlobalUnread()

  const convParam = route.query.conv as string | undefined
  const toUserId = route.query.to as string | undefined

  if (convParam) {
    const convId = convParam.startsWith('conv-') ? convParam : 'conv-' + convParam
    openConversation(convId)
  } else if (toUserId) {
    const numericId = toUserId.startsWith('u-') ? toUserId.substring(2) : toUserId
    openConversation('conv-' + numericId)
  } else if (threads.value.length > 0) {
    activeThreadId.value = threads.value[0].id
    await loadConversationDetail(threads.value[0].id)
  }
})

watch(() => route.query.conv, (newConv) => {
  if (newConv) {
    const convId = String(newConv).startsWith('conv-') ? String(newConv) : 'conv-' + newConv
    openConversation(convId)
  }
})
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

.search-bar {
  position: relative;
  margin-bottom: 1rem;
}

.search-input {
  width: 100%;
  padding: 0.75rem 1rem;
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  font-size: 0.95rem;
  transition: all 0.2s;
  box-sizing: border-box;
}

.search-input:focus {
  outline: none;
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.search-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  margin-top: 4px;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  background: white;
  max-height: 260px;
  overflow-y: auto;
  z-index: 100;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.search-user-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background 0.15s;
}

.search-user-item:hover {
  background: #f5f9fa;
}

.search-user-item:not(:last-child) {
  border-bottom: 1px solid #f0f0f0;
}

.search-user-name {
  font-weight: 600;
  color: #1a1a1a;
  font-size: 0.95rem;
  display: block;
}

.search-user-title {
  font-size: 0.8rem;
  color: #888;
}

.no-results {
  text-align: center;
  padding: 1.5rem;
  color: #888;
  font-size: 0.9rem;
  margin: 0;
}
</style>
