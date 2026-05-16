<template>
  <section class="profile-hero">
    <div class="profile-card">
      <UserAvatar :avatar-url="profile.avatar" :username="profile.username" :size="80" />
      <div>
        <h1>{{ profile.username }}</h1>
        <p class="muted">{{ profile.title }}</p>
        <div class="profile-actions">
          <template v-if="isViewingOther">
            <button v-if="!isFollowing" class="btn primary" @click="handleFollow">关注</button>
            <button v-else class="btn ghost" @click="handleUnfollow">已关注</button>
            <button class="btn primary" @click="goToMessages">发送私信</button>
          </template>
          <button v-else class="btn primary" @click="showEditModal = true">编辑资料</button>
        </div>
      </div>
    </div>
    <div class="profile-stats">
      <div class="stat-item" @click="showFollowingPanel = true">
        <h3>{{ profile.stats?.following || 0 }}</h3>
        <span class="muted">关注</span>
      </div>
      <div class="stat-item" @click="showFollowersPanel = true">
        <h3>{{ profile.stats?.followers || 0 }}</h3>
        <span class="muted">粉丝</span>
      </div>
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
      <div class="modal-header">
        <h3>编辑资料</h3>
        <button class="close-btn" @click="showEditModal = false">&#10005;</button>
      </div>
      <div class="modal-body">
        <div class="form-group">
          <label>头像</label>
          <div class="avatar-upload-row">
            <UserAvatar :avatar-url="previewAvatarUrl || profile.avatar" :username="editForm.username || profile.username" :size="60" />
            <input type="file" accept="image/png,image/jpeg,image/webp" @change="onAvatarFileChange" class="file-input" />
          </div>
        </div>
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
      </div>
      <div class="modal-footer">
        <button class="btn ghost" @click="showEditModal = false">取消</button>
        <button class="btn primary" @click="saveProfile" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button>
      </div>
    </div>
  </div>

  <!-- 关注列表面板 -->
  <div v-if="showFollowingPanel" class="slide-panel-overlay" @click.self="showFollowingPanel = false">
    <div class="slide-panel">
      <div class="panel-header">
        <h3>关注列表</h3>
        <button class="close-btn" @click="showFollowingPanel = false">&#10005;</button>
      </div>
      <div class="panel-body">
        <div v-if="followingList.length === 0" class="empty-tip">暂无关注</div>
        <div v-for="user in followingList" :key="user.id" class="follow-user-item">
          <div class="follow-user-left" @click="openUserInfo(user.id)">
            <UserAvatar :avatar-url="user.avatar" :username="user.username" :size="40" />
            <div>
              <p class="follow-user-name">{{ user.username }}</p>
              <p class="follow-user-title">{{ user.title }}</p>
            </div>
          </div>
          <button class="btn ghost small" @click="openMessage(user)">私信</button>
        </div>
      </div>
    </div>
  </div>

  <!-- 粉丝列表面板 -->
  <div v-if="showFollowersPanel" class="slide-panel-overlay" @click.self="showFollowersPanel = false">
    <div class="slide-panel">
      <div class="panel-header">
        <h3>粉丝列表</h3>
        <button class="close-btn" @click="showFollowersPanel = false">&#10005;</button>
      </div>
      <div class="panel-body">
        <div v-if="followersList.length === 0" class="empty-tip">暂无粉丝</div>
        <div v-for="user in followersList" :key="user.id" class="follow-user-item">
          <div class="follow-user-left" @click="openUserInfo(user.id)">
            <UserAvatar :avatar-url="user.avatar" :username="user.username" :size="40" />
            <div>
              <p class="follow-user-name">{{ user.username }}</p>
              <p class="follow-user-title">{{ user.title }}</p>
            </div>
          </div>
          <button class="btn ghost small" @click="openMessage(user)">私信</button>
        </div>
      </div>
    </div>
  </div>

  <UserInfoModal
    :visible="showUserInfoModal"
    :user-id="selectedInfoUserId"
    @close="showUserInfoModal = false"
  />
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SectionHeader from '../components/SectionHeader.vue'
import UserAvatar from '../components/UserAvatar.vue'
import UserInfoModal from '../components/UserInfoModal.vue'
import { fetchMe, updateProfile, fetchActivity, fetchUser, fetchFollowing, fetchFollowers, followUser, unfollowUser, uploadAvatar, type UserProfile, type FollowUserItem } from '../api/user'
import { useAlert } from '../composables/useAlert'
import { useAuthStore } from '../stores/auth'

const route = useRoute()
const router = useRouter()
const { showAlert } = useAlert()
const authStore = useAuthStore()

const viewedUserId = computed(() => (route.params.userId as string) || (route.query.userId as string) || undefined)
const isViewingOther = computed(() => !!viewedUserId.value && viewedUserId.value !== authStore.userId)

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
const previewAvatarUrl = ref('')
const isFollowing = ref(false)

const showFollowingPanel = ref(false)
const followingList = ref<FollowUserItem[]>([])
const showFollowersPanel = ref(false)
const followersList = ref<FollowUserItem[]>([])

const showUserInfoModal = ref(false)
const selectedInfoUserId = ref('')

async function loadProfile() {
  try {
    if (isViewingOther.value && viewedUserId.value) {
      const data = await fetchUser(viewedUserId.value)
      profile.value = data
    } else {
      const data = await fetchMe()
      profile.value = data
      if (data.avatar) {
        authStore.setAvatar(data.avatar)
      }
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
  previewAvatarUrl.value = ''
}

watch(showEditModal, (val) => {
  if (val) openEditModal()
})

async function onAvatarFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return
  try {
    const result = await uploadAvatar(file)
    previewAvatarUrl.value = result.avatarUrl
  } catch (error: any) {
    showAlert(error?.message || '上传失败', 'error')
  }
}

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
      bio: editForm.value.bio || profile.value.bio,
      avatar: previewAvatarUrl.value || profile.value.avatar
    }
    if (previewAvatarUrl.value) {
      authStore.setAvatar(previewAvatarUrl.value)
    }
    showEditModal.value = false
    showAlert('保存成功', 'success')
  } catch (error: any) {
    showAlert(error?.message || '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

async function handleFollow() {
  if (!viewedUserId.value) return
  try {
    const result = await followUser(viewedUserId.value)
    isFollowing.value = true
    profile.value.stats.followers = result.followerCount
  } catch (error: any) {
    showAlert(error?.message || '关注失败', 'error')
  }
}

async function handleUnfollow() {
  if (!viewedUserId.value) return
  try {
    const result = await unfollowUser(viewedUserId.value)
    isFollowing.value = false
    profile.value.stats.followers = result.followerCount
  } catch (error: any) {
    showAlert(error?.message || '取消关注失败', 'error')
  }
}

async function loadFollowingList() {
  if (!viewedUserId.value && !authStore.userId) return
  try {
    const targetId = isViewingOther.value ? viewedUserId.value! : authStore.userId
    const result = await fetchFollowing(targetId)
    followingList.value = result.items
  } catch (error) {
    console.error('加载关注列表失败:', error)
  }
}

async function loadFollowersList() {
  if (!viewedUserId.value && !authStore.userId) return
  try {
    const targetId = isViewingOther.value ? viewedUserId.value! : authStore.userId
    const result = await fetchFollowers(targetId)
    followersList.value = result.items
  } catch (error) {
    console.error('加载粉丝列表失败:', error)
  }
}

watch(showFollowingPanel, (val) => {
  if (val) loadFollowingList()
})

watch(showFollowersPanel, (val) => {
  if (val) loadFollowersList()
})

function openMessage(user: FollowUserItem) {
  const numericId = user.id.startsWith('u-') ? user.id.substring(2) : user.id
  router.push({ path: '/messages', query: { conv: numericId } })
}

function goToMessages() {
  if (!viewedUserId.value) return
  const numericId = viewedUserId.value.startsWith('u-') ? viewedUserId.value.substring(2) : viewedUserId.value
  router.push({ path: '/messages', query: { conv: numericId } })
}

function openUserInfo(userId: string) {
  selectedInfoUserId.value = userId
  showUserInfoModal.value = true
}

onMounted(() => {
  loadProfile()
  loadFeed()
})

watch(() => route.params.userId, () => {
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

.profile-actions {
  display: flex;
  gap: 0.75rem;
  margin-top: 1rem;
}

.profile-stats {
  display: flex;
  gap: 2rem;
}

.stat-item {
  text-align: center;
  cursor: pointer;
  padding: 0.5rem;
  border-radius: 8px;
  transition: background 0.15s;
}

.stat-item:hover {
  background: rgba(74, 144, 164, 0.08);
}

.profile-stats h3 {
  font-size: 1.5rem;
  margin-bottom: 0.25rem;
}

.activity-feed {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.feed-item {
  display: flex;
  align-items: flex-start;
  gap: 0.75rem;
}

.feed-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--primary-color, #4a90a4);
  margin-top: 0.5rem;
  flex-shrink: 0;
}

.feed-title {
  font-weight: 500;
  margin-bottom: 0.15rem;
}

/* Modal */
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
  width: 480px;
  max-width: 90vw;
  overflow: hidden;
  animation: slideUp 0.3s ease;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

@keyframes slideUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
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
  border: 2px solid #e8e8e8;
  border-radius: 10px;
  box-sizing: border-box;
}

.modal-input:focus,
.modal-textarea:focus {
  outline: none;
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.modal-textarea {
  min-height: 100px;
  resize: vertical;
}

.avatar-upload-row {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.file-input {
  font-size: 0.9rem;
}

/* Slide panel */
.slide-panel-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
}

.slide-panel {
  width: 380px;
  max-width: 85vw;
  background: white;
  height: 100%;
  display: flex;
  flex-direction: column;
  animation: slideIn 0.25s ease;
}

@keyframes slideIn {
  from { transform: translateX(100%); }
  to { transform: translateX(0); }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid #eee;
}

.panel-header h3 {
  margin: 0;
  font-size: 1.1rem;
}

.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem 0;
}

.follow-user-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1.5rem;
  transition: background 0.15s;
}

.follow-user-item:hover {
  background: #f5f9fa;
}

.follow-user-left {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
  flex: 1;
}

.follow-user-name {
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.follow-user-title {
  font-size: 0.8rem;
  color: #888;
  margin: 0.15rem 0 0;
}
</style>
