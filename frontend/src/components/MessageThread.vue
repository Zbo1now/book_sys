<template>
  <div class="message-thread">
    <div class="thread-list">
      <div
        v-for="thread in threads"
        :key="thread.id"
        class="thread-item"
        :class="{ active: thread.id === activeId }"
        @click="$emit('select', thread.id)"
      >
        <div class="thread-avatar">{{ thread.initials }}</div>
        <div class="thread-info">
          <p class="thread-name">{{ thread.name }}</p>
          <p class="thread-preview">{{ thread.preview }}</p>
        </div>
        <span v-if="thread.unreadCount && thread.unreadCount > 0" class="thread-badge">{{ thread.unreadCount > 99 ? '99+' : thread.unreadCount }}</span>
      </div>
      <div v-if="threads.length === 0" class="no-threads">
        暂无会话记录
      </div>
    </div>
    <div class="thread-panel">
      <template v-if="activeThread">
        <div class="thread-header">
          <div>
            <p class="thread-name">{{ activeThread.name }}</p>
            <p class="muted">{{ activeThread.status }}</p>
          </div>
          <button v-if="activeThread.participantId" class="btn ghost small" @click="viewProfile">查看档案</button>
        </div>
        <div class="thread-body" ref="messageBody">
          <div
            v-for="message in activeThread.messages"
            :key="message.id"
            class="thread-bubble"
            :class="message.from"
          >
            {{ message.text }}
          </div>
        </div>
        <div class="thread-input">
          <input
            v-model="inputText"
            type="text"
            placeholder="请输入消息..."
            @keyup.enter="handleSend"
          />
          <button class="btn primary" @click="handleSend">发送</button>
        </div>
      </template>
      <div v-else class="no-selected">
        <p>请选择一个会话或新建会话</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

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
  unreadCount?: number
  messages: MessageItem[]
  participantId?: string
}

const props = defineProps<{
  threads: ThreadItem[]
  activeId: string
}>()

const emit = defineEmits<{
  select: [id: string]
  send: [content: string]
}>()

const inputText = ref('')
const messageBody = ref<HTMLElement | null>(null)

const activeThread = computed(() => {
  return props.threads.find((thread) => thread.id === props.activeId) || null
})

function handleSend() {
  if (!inputText.value.trim()) return
  emit('send', inputText.value)
  inputText.value = ''
}

function viewProfile() {
  const thread = activeThread.value
  if (!thread || !thread.participantId) return
  router.push({ name: 'profile', query: { userId: thread.participantId } })
}

watch(() => activeThread.value?.messages.length, () => {
  nextTick(() => {
    if (messageBody.value) {
      messageBody.value.scrollTop = messageBody.value.scrollHeight
    }
  })
})
</script>

<style scoped>
.message-thread {
  display: flex;
  gap: 1.5rem;
  height: calc(100vh - 200px);
  min-height: 500px;
}

.thread-list {
  width: 280px;
  flex-shrink: 0;
  border-right: 1px solid var(--border-color, #eee);
  padding-right: 1rem;
  overflow-y: auto;
}

.thread-item {
  display: flex;
  gap: 0.75rem;
  padding: 0.75rem;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s;
  align-items: center;
}

.thread-item:hover,
.thread-item.active {
  background: var(--bg-secondary, #f5f5f5);
}

.thread-info {
  flex: 1;
  min-width: 0;
}

.thread-badge {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  background: #e74c3c;
  color: #fff;
  font-size: 0.7rem;
  font-weight: 700;
  line-height: 20px;
  text-align: center;
  border-radius: 10px;
  flex-shrink: 0;
}

.thread-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: var(--primary-color, #4a90a4);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.85rem;
  font-weight: 600;
  flex-shrink: 0;
}

.thread-name {
  font-weight: 600;
  margin-bottom: 0.25rem;
}

.thread-preview {
  font-size: 0.85rem;
  color: var(--text-muted, #888);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.thread-panel {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.thread-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 1rem;
  border-bottom: 1px solid var(--border-color, #eee);
}

.thread-body {
  flex: 1;
  overflow-y: auto;
  padding: 1rem 0;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.thread-bubble {
  max-width: 70%;
  padding: 0.75rem 1rem;
  border-radius: 12px;
  line-height: 1.4;
}

.thread-bubble.me {
  align-self: flex-end;
  background: var(--primary-color, #4a90a4);
  color: white;
  border-bottom-right-radius: 4px;
}

.thread-bubble.them {
  align-self: flex-start;
  background: var(--bg-secondary, #f0f0f0);
  color: var(--text-color, #333);
  border-bottom-left-radius: 4px;
}

.thread-input {
  display: flex;
  gap: 0.75rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border-color, #eee);
}

.thread-input input {
  flex: 1;
  padding: 0.75rem 1rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 24px;
  outline: none;
}

.thread-input input:focus {
  border-color: var(--primary-color, #4a90a4);
}

.no-threads,
.no-selected {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: var(--text-muted, #888);
}
</style>
