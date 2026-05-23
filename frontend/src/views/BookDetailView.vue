<template>
  <section class="detail-layout">
    <div class="detail-cover">
      <span class="pill">{{ book?.tag || '图书' }}</span>
      <div class="cover-block">
        <img v-if="book?.cover" :src="book.cover" :alt="book.title" />
      </div>
      <div class="detail-actions">
        <button class="btn primary" @click="openReviewModal">写评论/评分</button>
      </div>
    </div>
    <div class="detail-body">
      <h1>{{ book?.title || '' }}</h1>
      <p class="muted">
        作者：{{ book?.author || '' }}
        <span v-if="book?.publisher"> · {{ book.publisher }}</span>
        <span v-if="book?.publishDate"> · {{ book.publishDate }}</span>
      </p>
      <div class="detail-rating">
        <span class="rating-score">{{ book?.rating || '0.0' }}</span>
        <span class="muted">{{ book?.reviews ?? 0 }} 条社区评分</span>
      </div>
      <p class="detail-desc">{{ book?.description || '' }}</p>
      <div class="detail-tags">
        <span v-if="book?.isbn" class="pill">ISBN：{{ book.isbn }}</span>
        <span v-if="book?.pages" class="pill">页数：{{ book.pages }}</span>
        <span v-if="book?.language" class="pill">语言：{{ book.language }}</span>
        <span v-if="book?.category" class="pill">分类：{{ book.category }}</span>
      </div>

      <div class="detail-main">
        <div class="detail-reviews">
          <div class="section-block">
            <div class="section-header-row">
              <SectionHeader title="读者评论" subtitle="来自书友的真实反馈。" />
              <div class="sort-controls">
                <button 
                  class="btn ghost small" 
                  :class="{ active: sortType === 'hot' }"
                  @click="sortType = 'hot'"
                >
                  🔥 最热
                </button>
                <button 
                  class="btn ghost small" 
                  :class="{ active: sortType === 'newest' }"
                  @click="sortType = 'newest'"
                >
                  ⏱️ 最新
                </button>
                <button 
                  class="btn ghost small" 
                  :class="{ active: sortType === 'oldest' }"
                  @click="sortType = 'oldest'"
                >
                  📅 最早
                </button>
              </div>
            </div>
            <div v-if="displayReviews.length > 0" class="review-list">
              <div v-for="review in displayReviews" :key="review.id" class="review-item">
                <div class="review-header">
                  <div class="user-info">
                    <span class="avatar-clickable" @click="review.userId && openUserInfo(review.userId)" :class="{ clickable: !!review.userId }">
                      <UserAvatar :avatar-url="review.avatarUrl || undefined" :username="review.userName" :size="40" />
                    </span>
                    <div class="user-details">
                      <UserNameLink :username="review.userName" :user-id="review.userId" />
                      <span class="review-time muted">{{ formatTime(review.createdAt) }}</span>
                    </div>
                  </div>
                  <div class="review-meta">
                    <span class="review-rating">⭐ {{ Math.round(review.rating) }}</span>
                    <div class="review-actions">
                      <button 
                        class="review-action-btn like-btn"
                        :class="{ liked: review.liked }"
                        @click="toggleLike(review)"
                      >
                        <span>{{ review.liked ? '❤️' : '🤍' }}</span>
                        <span>{{ review.likeCount || 0 }}</span>
                      </button>
                      <button 
                        v-if="canDeleteReview(review)"
                        class="review-action-btn delete-btn"
                        @click="confirmDeleteReview(review)"
                      >
                        <span>🗑️</span>
                      </button>
                    </div>
                  </div>
                </div>
                <div class="review-content-wrapper">
                  <p class="review-content">{{ review.content || '用户未填写评论内容' }}</p>
                </div>
              </div>
            </div>
            <p v-else class="no-reviews">暂无评论数据，快来发表第一条评论吧！</p>
          </div>
        </div>
      </div>
    </div>

    <div v-if="showReviewModal" class="modal-overlay" @click.self="showReviewModal = false">
      <div class="modal-container">
        <div class="modal-content">
          <div class="modal-header">
            <h3>📝 发表评论</h3>
            <button class="close-btn" @click="showReviewModal = false">✕</button>
          </div>
          
          <div class="modal-body">
            <div class="rating-section">
              <label class="section-label">评分</label>
              <div class="star-rating">
                <span 
                  v-for="i in 5" 
                  :key="i" 
                  class="star" 
                  :class="{ active: i <= Math.round(reviewForm.rating) }"
                  @click="reviewForm.rating = i"
                >★</span>
              </div>
              <span class="rating-value">{{ reviewForm.rating }} 分</span>
            </div>
            
            <div class="content-section">
              <label class="section-label">评论内容</label>
              <textarea 
                v-model="reviewForm.content" 
                placeholder="分享你的阅读感受、收获或建议..." 
                rows="5"
                maxlength="500"
              ></textarea>
              <div class="char-count">{{ reviewForm.content.length }}/500</div>
            </div>
          </div>
          
          <div class="modal-footer">
            <button class="btn secondary" @click="showReviewModal = false">取消</button>
            <button class="btn primary" @click="submitReview" :disabled="submitting">
              <span v-if="submitting" class="spinner"></span>
              {{ submitting ? '提交中...' : '发布评论' }}
            </button>
          </div>
          
          <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>
        </div>
      </div>
    </div>
  </section>
  <UserInfoModal
    :visible="showUserInfoModal"
    :user-id="selectedInfoUserId"
    @close="showUserInfoModal = false"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import SectionHeader from '../components/SectionHeader.vue'
import UserAvatar from '../components/UserAvatar.vue'
import UserNameLink from '../components/UserNameLink.vue'
import UserInfoModal from '../components/UserInfoModal.vue'
import { fetchBookDetail, fetchBookReviews, addBookReview, likeBookReview, unlikeBookReview, deleteBookReview, type BookReview } from '../api/books'
import { useAuthStore } from '../stores/auth'
import { useAlert } from '../composables/useAlert'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const { showAlert, showConfirm } = useAlert()
const showUserInfoModal = ref(false)
const selectedInfoUserId = ref('')

function openUserInfo(userId: string) {
  selectedInfoUserId.value = userId
  showUserInfoModal.value = true
}
const book = ref<any | null>(null)
const reviews = ref<BookReview[]>([])
const showReviewModal = ref(false)
const deletingReview = ref<BookReview | null>(null)
const submitting = ref(false)
const errorMessage = ref('')
const reviewForm = ref({
  rating: 5.0,
  content: ''
})
const sortType = ref<'hot' | 'newest' | 'oldest'>('hot')

const displayReviews = computed(() => {
  if (!reviews.value || reviews.value.length === 0) return []
  const sorted = [...reviews.value]
  
  if (sortType.value === 'hot') {
    sorted.sort((a, b) => {
      const likeDiff = (b.likeCount || 0) - (a.likeCount || 0)
      if (likeDiff !== 0) return likeDiff
      return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
    })
  } else if (sortType.value === 'oldest') {
    sorted.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
  } else {
    sorted.sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime())
  }
  
  return sorted
})

function canDeleteReview(review: BookReview): boolean {
  if (!authStore.username) return false
  return review.userName === authStore.username
}

async function toggleLike(review: BookReview) {
  if (!authStore.token) {
    showAlert('请先登录', 'warning')
    return
  }
  
  const bookId = String(route.params.id || '')
  if (!bookId) return
  
  try {
    if (review.liked) {
      await unlikeBookReview(bookId, review.id)
      review.liked = false
      review.likeCount = (review.likeCount || 1) - 1
    } else {
      const result = await likeBookReview(bookId, review.id)
      review.liked = true
      review.likeCount = result.likeCount
    }
  } catch (error: any) {
    showAlert(error?.message || '操作失败', 'error')
  }
}

function confirmDeleteReview(review: BookReview) {
  deletingReview.value = review
  showConfirm('确定要删除这条评论吗？', async () => {
    const bookId = String(route.params.id || '')
    if (!bookId || !deletingReview.value) return
    
    try {
      await deleteBookReview(bookId, deletingReview.value.id)
      reviews.value = reviews.value.filter(r => r.id !== deletingReview.value?.id)
      showAlert('删除成功', 'success')
    } catch (error: any) {
      showAlert(error?.message || '删除失败', 'error')
    } finally {
      deletingReview.value = null
    }
  })
}

async function requireAuth() {
  if (!authStore.isAuthed) {
    await router.push({ name: 'auth', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function openReviewModal() {
  if (!await requireAuth()) return
  showReviewModal.value = true
}

async function load() {
  const id = String(route.params.id || '')
  if (!id) return
  book.value = await fetchBookDetail(id)
  if (book.value?.reviewList) {
    reviews.value = book.value.reviewList
  } else {
    reviews.value = await fetchBookReviews(id, { page: 1, pageSize: 20 })
  }
}

async function submitReview() {
  if (!authStore.token) {
    errorMessage.value = '请先登录后再发表评论'
    return
  }
  if (reviewForm.value.rating < 0.5 || reviewForm.value.rating > 5) {
    errorMessage.value = '评分必须在0.5到5之间'
    return
  }

  submitting.value = true
  errorMessage.value = ''
  try {
    const result = await addBookReview(
      String(route.params.id),
      {
        rating: reviewForm.value.rating,
        content: reviewForm.value.content || undefined
      }
    )
    const existingIndex = reviews.value.findIndex(r => r.id === result.id)
    if (existingIndex >= 0) {
      reviews.value[existingIndex] = { ...result, createdAt: new Date().toISOString() }
    } else {
      reviews.value.unshift({ ...result, createdAt: new Date().toISOString() })
    }
    showReviewModal.value = false
    reviewForm.value = { rating: 5.0, content: '' }
  } catch (error: any) {
    errorMessage.value = error?.message || '提交失败，请重试'
  } finally {
    submitting.value = false
  }
}

function formatTime(dateStr: string): string {
  try {
    const date = new Date(dateStr)
    return date.toLocaleDateString('zh-CN', {
      year: 'numeric',
      month: 'short',
      day: 'numeric'
    })
  } catch {
    return dateStr
  }
}

onMounted(load)
watch(() => route.params.id, load)
</script>

<style scoped>
.avatar-clickable.clickable {
  cursor: pointer;
  transition: opacity 0.15s;
}

.avatar-clickable.clickable:hover {
  opacity: 0.8;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 1rem;
}

.sort-controls {
  display: flex;
  gap: 0.5rem;
}

.sort-controls .btn {
  min-width: 70px;
  padding: 0.4rem 0.8rem;
  font-size: 0.85rem;
}

.sort-controls .btn.active {
  background: linear-gradient(135deg, #4a90a4 0%, #3a7a8a 100%);
  color: white;
  border-color: transparent;
  box-shadow: 0 2px 8px rgba(74, 144, 164, 0.3);
}

.review-list {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  margin-top: 1rem;
}

.review-item {
  padding: 1.25rem;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  transition: all 0.2s ease;
}

.review-item:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.review-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 0.75rem;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.avatar-placeholder {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4a90a4 0%, #6bb3c4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  font-size: 0.85rem;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.review-user {
  font-weight: 600;
  color: var(--text-primary, #1a1a1a);
  font-size: 0.95rem;
}

.review-time {
  font-size: 0.8rem;
  color: var(--text-muted, #888);
}

.review-meta {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.review-rating {
  color: #f5a623;
  font-weight: 600;
  font-size: 0.9rem;
  background: rgba(245, 166, 35, 0.1);
  padding: 0.25rem 0.5rem;
  border-radius: 20px;
}

.review-content-wrapper {
  background: var(--bg-secondary, #f8f9fa);
  padding: 1rem;
  border-radius: 8px;
}

.review-content {
  color: var(--text-secondary, #444);
  line-height: 1.7;
  font-size: 0.95rem;
  margin: 0;
}

.review-actions {
  display: flex;
  gap: 0.5rem;
}

.review-action-btn {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.3rem 0.6rem;
  border: none;
  background: transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  color: var(--text-muted, #888);
  transition: all 0.2s;
}

.review-action-btn:hover {
  background: var(--bg-secondary, #f0f0f0);
}

.like-btn.liked {
  color: #e74c3c;
}

.delete-btn:hover {
  background: #ffeaea;
  color: #dc3545;
}

.no-reviews {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted, #888);
  background: var(--bg-secondary, #f8f9fa);
  border-radius: 12px;
}

.detail-main {
  margin-top: 1.5rem;
}

.modal-container {
  width: 100%;
  max-width: 520px;
  padding: 1rem;
}

.modal-content {
  border-radius: 24px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 1.5rem 1rem;
  border-bottom: 1px solid rgba(29, 27, 26, 0.06);
}

.modal-header h3 {
  margin: 0;
  font-size: 1.45rem;
  font-weight: 600;
  color: var(--ink);
}

.modal-body {
  padding: 1.5rem;
}

.section-label {
  display: block;
  margin-bottom: 0.75rem;
  font-weight: 600;
  color: var(--text-primary, #333);
  font-size: 0.9rem;
}

.rating-section {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.25rem;
}

.star-rating {
  display: flex;
  gap: 0.25rem;
}

.star {
  font-size: 1.75rem;
  color: #e0e0e0;
  cursor: pointer;
  transition: all 0.2s;
}

.star:hover,
.star.active {
  color: #f5a623;
}

.star.active {
  text-shadow: 0 0 10px rgba(245, 166, 35, 0.5);
}

.rating-value {
  font-weight: 600;
  color: var(--text-primary, #333);
  font-size: 1rem;
  min-width: 60px;
}

.content-section {
  position: relative;
}

.content-section textarea {
  width: 100%;
  padding: 1rem;
  border: 2px solid #e0e0e0;
  border-radius: 12px;
  resize: vertical;
  font-family: inherit;
  font-size: 0.95rem;
  line-height: 1.6;
  transition: all 0.2s;
  box-sizing: border-box;
}

.content-section textarea:focus {
  outline: none;
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.content-section textarea::placeholder {
  color: var(--text-muted, #aaa);
}

.char-count {
  text-align: right;
  font-size: 0.8rem;
  color: var(--text-muted, #888);
  margin-top: 0.5rem;
}

.modal-footer {
  display: flex;
  gap: 0.75rem;
  justify-content: flex-end;
  padding: 1rem 1.5rem 1.5rem;
  border-top: 1px solid rgba(29, 27, 26, 0.06);
}

.modal-footer .btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  display: inline-block;
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  margin-right: 0.5rem;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.error-msg {
  color: #dc3545;
  margin: 0 1.5rem 1.5rem;
  padding: 0.75rem;
  background: #fff5f5;
  border-radius: 8px;
  text-align: center;
  font-size: 0.9rem;
}
</style>
