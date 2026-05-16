<template>
  <section class="profile-hero">
    <div class="profile-card">
      <div class="profile-avatar">
        <span>{{ initials }}</span>
      </div>
      <div>
        <h1>{{ profile.username }}</h1>
        <p class="muted">{{ profile.title }}</p>
        <div class="profile-actions">
          <button v-if="!isViewingOther" class="btn primary" @click="showEditModal = true">编辑资料</button>
        </div>
      </div>
    </div>
    <div class="profile-stats">
      <!-- <div>
        <h3>{{ profile.stats?.following || 0 }}</h3>
        <span class="muted">关注</span>
      </div>
      <div>
        <h3>{{ profile.stats?.followers || 0 }}</h3>
        <span class="muted">粉丝</span>
      </div>
      <div>
        <h3>{{ profile.stats?.booklists || 0 }}</h3>
        <span class="muted">书单</span>
      </div> -->
    </div>
  </section>

  <section class="section-block">
    <SectionHeader title="近期动态" subtitle="近期评分、书单与关注记录。" />
    <div v-if="feed.length > 0" class="activity-feed">
      <div class="feed-item" v-for="item in feed" :key="item.title">
        <div class="feed-dot"></div>
        <div>
          <p class="feed-title">{{ item.title }}</p>
          <p class="muted">{{ item.meta }}</p>
        </div>
      </div>
    </div>
    <div v-else class="empty-tip">暂无动态记录</div>
  </section>

  <!-- 编辑资料弹窗 -->
  <div v-if="showEditModal" class="modal-overlay" @click.self="showEditModal = false">
    <div class="modal">
      <h3>编辑资料</h3>
      <div class="form-group">
        <label>用户名</label>
        <input v-model.trim="editForm.username" type="text" placeholder="用户名" class="modal-input" />
      </div>
      <div class="form-group">
        <label>头衔</label>
        <input v-model.trim="editForm.title" type="text" placeholder="头衔" class="modal-input" />
      </div>
      <div class="form-group">
        <label>个人简介</label>
        <textarea v-model.trim="editForm.bio" placeholder="个人简介" class="modal-textarea"></textarea>
      </div>
      <div class="modal-actions">
        <button class="btn ghost" @click="showEditModal = false">取消</button>
        <button class="btn primary" @click="saveProfile" :disabled="saving">保存</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import SectionHeader from '../components/SectionHeader.vue'
import { fetchMe, updateProfile, fetchActivity, fetchUser, type UserProfile } from '../api/user'
import { useAlert } from '../composables/useAlert'

const route = useRoute()
const { showAlert } = useAlert()

const viewedUserId = computed(() => route.query.userId as string | undefined)
const isViewingOther = computed(() => !!viewedUserId)

const profile = ref<UserProfile>({
  id: '',
  username: '加载中...',
  avatar: '',
  title: '',
  bio: '',
  stats: { following: 0, followers: 0, booklists: 0 }
})

const showEditModal = ref(false)
const saving = ref(false)
const feed = ref<Array<{ type: string; title: string; meta: string }>>([])
const editForm = ref({ username: '', title: '', bio: '' })

const initials = computed(() => {
  if (!profile.value.username) return '?'
  return profile.value.username.substring(0, 2).toUpperCase()
})

async function loadProfile() {
  try {
    if (isViewingOther.value && viewedUserId.value) {
      const data = await fetchUser(viewedUserId.value)
      profile.value = data
    } else {
      const data = await fetchMe()
      profile.value = data
    }
  } catch (error) {
    console.error('加载用户信息失败:', error)
  }
}

async function loadFeed() {
  try {
    const targetId = isViewingOther.value ? viewedUserId.value : undefined
    const result = await fetchActivity(1, 20, targetId)
    feed.value = result.items
  } catch (error) {
    console.error('加载动态失败:', error)
  }
}

function openEditModal() {
  editForm.value = {
    username: profile.value.username || '',
    title: profile.value.title || '',
    bio: profile.value.bio || ''
  }
}

watch(showEditModal, (val) => {
  if (val) openEditModal()
})

async function saveProfile() {
  saving.value = true
  try {
    await updateProfile({
      username: editForm.value.username || undefined,
      title: editForm.value.title || undefined,
      bio: editForm.value.bio || undefined
    })
    profile.value = {
      ...profile.value,
      username: editForm.value.username || profile.value.username,
      title: editForm.value.title || profile.value.title,
      bio: editForm.value.bio || profile.value.bio
    }
    showEditModal.value = false
    showAlert('保存成功', 'success')
  } catch (error: any) {
    showAlert(error?.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadProfile()
  loadFeed()
})

watch(() => route.query.userId, () => {
  loadProfile()
  loadFeed()
})
</script>

<style scoped>
.empty-tip {
  text-align: center;
  padding: 2rem;
  color: var(--text-muted, #888);
}

.profile-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid var(--border, #e0e0e0);
  border-radius: 16px;
  padding: 2rem;
  margin-bottom: 2rem;
}

.profile-card {
  display: flex;
  gap: 1.5rem;
  align-items: center;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--primary-color, #4a90a4);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 1.5rem;
  font-weight: 700;
}

.profile-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.profile-stats {
  display: flex;
  gap: 2rem;
}

.profile-stats > div {
  text-align: center;
}

.profile-stats h3 {
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
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: white;
  border-radius: 12px;
  padding: 1.5rem;
  width: 450px;
  max-width: 90vw;
}

.modal h3 {
  margin-bottom: 1rem;
}

.form-group {
  margin-bottom: 1rem;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: 600;
  font-size: 0.9rem;
}

.modal-input,
.modal-textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 8px;
  box-sizing: border-box;
}

.modal-textarea {
  min-height: 100px;
  resize: vertical;
}

.modal-actions {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  margin-top: 1rem;
}
</style>
