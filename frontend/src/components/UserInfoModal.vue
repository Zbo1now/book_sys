<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click.self="$emit('close')">
      <div class="modal" v-if="user">
        <button class="close-btn" @click="$emit('close')">&#10005;</button>
        <div class="modal-body">
          <div class="user-header">
            <UserAvatar :avatar-url="user.avatar" :username="user.username" :size="72" />
            <h3 class="user-name">{{ user.username }}</h3>
            <p class="user-title">{{ user.title }}</p>
          </div>
          <p class="user-bio">{{ user.bio || '暂无简介' }}</p>
          <div class="user-stats">
            <div class="stat">
              <strong>{{ user.stats?.following || 0 }}</strong>
              <span>关注</span>
            </div>
            <div class="stat">
              <strong>{{ user.stats?.followers || 0 }}</strong>
              <span>粉丝</span>
            </div>
            <div class="stat">
              <strong>{{ user.stats?.booklists || 0 }}</strong>
              <span>书单</span>
            </div>
          </div>
          <div class="modal-actions">
            <button class="btn primary" @click="goToProfile">去他主页</button>
            <button class="btn ghost" @click="sendMessage">发送私信</button>
          </div>
        </div>
      </div>
      <div v-else class="modal loading-modal">
        <p>加载中...</p>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import UserAvatar from './UserAvatar.vue'
import { fetchUser, type UserProfile } from '../api/user'

const props = defineProps<{
  userId: string
  visible: boolean
}>()

defineEmits<{
  close: []
}>()

const router = useRouter()
const user = ref<UserProfile | null>(null)

async function load() {
  if (!props.userId) return
  try {
    user.value = await fetchUser(props.userId)
  } catch {
    user.value = null
  }
}

watch(() => [props.userId, props.visible] as const, ([id, vis]) => {
  if (vis && id) load()
})

function goToProfile() {
  router.push(`/user/${props.userId}`)
}

function sendMessage() {
  if (!user.value) return
  const numericId = props.userId.startsWith('u-') ? props.userId.substring(2) : props.userId
  router.push({ path: '/messages', query: { conv: numericId } })
}
</script>

<style scoped>
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
  width: 380px;
  max-width: 90vw;
  position: relative;
  animation: slideUp 0.3s ease;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

.loading-modal {
  padding: 2rem;
  text-align: center;
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.close-btn {
  position: absolute;
  top: 1rem;
  right: 1rem;
  background: none;
  border: none;
  font-size: 1.2rem;
  cursor: pointer;
  color: #888;
  padding: 0.25rem;
  border-radius: 4px;
  z-index: 1;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 2rem 1.5rem 1.5rem;
}

.user-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.user-name {
  margin: 0;
  font-size: 1.2rem;
  color: #1a1a1a;
}

.user-title {
  margin: 0;
  color: #888;
  font-size: 0.9rem;
}

.user-bio {
  text-align: center;
  color: #555;
  font-size: 0.9rem;
  line-height: 1.5;
  margin-bottom: 1rem;
}

.user-stats {
  display: flex;
  justify-content: center;
  gap: 2rem;
  margin-bottom: 1.5rem;
}

.stat {
  text-align: center;
}

.stat strong {
  display: block;
  font-size: 1.2rem;
  color: #1a1a1a;
}

.stat span {
  font-size: 0.8rem;
  color: #888;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: center;
  padding-top: 1rem;
  border-top: 1px solid #eee;
}
</style>
