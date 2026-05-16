<template>
  <section class="detail-layout single-column">
    <div class="detail-body">
      <h1>{{ booklist?.title || '' }}</h1>
      <p class="muted">
        创建者：{{ booklist?.creator?.username ?? '未知' }} · {{ booklist?.bookCount ?? 0 }} 本 · {{ booklist?.followers ?? 0 }} 收藏 · {{ booklist?.likes ?? 0 }} 点赞
      </p>
      <p class="detail-desc">{{ booklist?.description || '' }}</p>

      <div class="action-bar">
        <button 
          class="btn" 
          :class="booklist?.isLiked ? 'primary' : 'ghost'" 
          @click="handleLike"
          :disabled="!isAuthed"
        >
          {{ booklist?.isLiked ? '已点赞' : '点赞' }} ({{ booklist?.likes ?? 0 }})
        </button>
        <button 
          class="btn" 
          :class="booklist?.isFollowed ? 'primary' : 'ghost'" 
          @click="handleFollow"
          :disabled="!isAuthed"
        >
          {{ booklist?.isFollowed ? '已收藏' : '收藏' }} ({{ booklist?.followers ?? 0 }})
        </button>
      </div>

      <div class="section-block">
        <SectionHeader title="书单内图书" subtitle="按添加顺序排序。" />
        <div v-if="isOwner" class="booklist-edit-row">
          <button class="btn primary" @click="handleAddBook">新增图书</button>
        </div>
        <div class="grid-4">
          <BookCard v-for="book in books" :key="book.id" :book="book" />
        </div>
      </div>

      <div class="section-block">
        <SectionHeader title="书单点赞者" subtitle="对该书单表达认可的用户。" />
        <p class="muted" v-if="!likedUsers.length">暂无点赞者</p>
        <div v-else class="liker-list">
          <span class="pill clickable" v-for="u in likedUsers" :key="u.id" @click="openUserInfo(u.id)"><UserNameLink :username="u.username" :user-id="u.id" /></span>
        </div>
      </div>

      <div class="section-block">
        <SectionHeader title="书单评论" subtitle="书友对这份书单的交流内容。" />
        <div v-if="isAuthed" class="comment-form">
          <textarea v-model.trim="commentText" placeholder="写下你的评论" />
          <button class="btn primary" @click="submitComment">发表评论</button>
        </div>
        <p v-else class="muted auth-hint">登录后参与评论</p>
        <p class="muted" v-if="!comments.length">暂无评论</p>
        <div v-else class="comment-list">
          <article class="comment-item" v-for="c in comments" :key="c.id">
            <p class="comment-user clickable" @click="c.userId && openUserInfo(c.userId)"><UserNameLink :username="c.username" :user-id="c.userId" /></p>
            <p class="muted">{{ c.content }}</p>
          </article>
        </div>
      </div>
    </div>
  </section>

  <BookPickerModal
    :visible="showBookPicker"
    title="全部图书"
    :books="allBooks"
    v-model="selectedBookIds"
    @close="showBookPicker = false"
    @confirm="saveBooklistBooks"
  />
  <UserInfoModal
    :visible="showUserInfoModal"
    :user-id="selectedInfoUserId"
    @close="showUserInfoModal = false"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import SectionHeader from '../components/SectionHeader.vue'
import UserNameLink from '../components/UserNameLink.vue'
import UserInfoModal from '../components/UserInfoModal.vue'
import BookCard from '../components/BookCard.vue'
import BookPickerModal from '../components/BookPickerModal.vue'
import { createBooklistComment, fetchBooklistDetail, updateBooklist, toggleBooklistLike, toggleBooklistFollow } from '../api/booklists'
import { fetchBooks } from '../api/books'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const booklist = ref<any | null>(null)
const books = ref<any[]>([])
const allBooks = ref<Array<{ id: string; title: string; author?: string }>>([])
const selectedBookIds = ref<string[]>([])
const likedUsers = ref<Array<{ id: string; username: string }>>([])
const comments = ref<Array<{ id: string; username: string; userId?: string; content: string }>>([])

const showUserInfoModal = ref(false)
const selectedInfoUserId = ref('')

function openUserInfo(userId: string) {
  selectedInfoUserId.value = userId
  showUserInfoModal.value = true
}
const showBookPicker = ref(false)
const commentText = ref('')
const isAuthed = computed(() => authStore.isAuthed)
const isOwner = computed(() => {
  if (!authStore.userId || !booklist.value?.creator?.id) return false
  const uid = String(authStore.userId)
  const cid = String(booklist.value.creator.id)
  return cid === `u-${uid}` || cid === uid
})

async function requireAuth() {
  if (!authStore.isAuthed) {
    await router.push({ name: 'auth', query: { redirect: route.fullPath } })
    return false
  }
  return true
}

async function load() {
  const id = String(route.params.id || '')
  if (!id) return
  const detail = await fetchBooklistDetail(id)
  booklist.value = detail
  selectedBookIds.value = (detail.bookIds ?? detail.books?.map((b: any) => b.id) ?? []) as string[]
  likedUsers.value = detail.likedUsers ?? []
  comments.value = detail.comments ?? []
  books.value = (detail.books ?? []).map((b: any) => ({
    id: b.id,
    title: b.title,
    author: b.author,
    cover: b.cover,
    rating: b.rating ?? '0.0',
    reviews: 0,
    tag: '书单'
  }))
}

async function loadAllBooks() {
  const result = await fetchBooks({ page: 1, pageSize: 300 })
  allBooks.value = result.items.map((item) => ({ id: item.id, title: item.title, author: item.author }))
}

async function saveBooklistBooks() {
  const id = String(route.params.id || '')
  if (!id) {
    return
  }
  await updateBooklist(id, { bookIds: selectedBookIds.value })
  showBookPicker.value = false
  await load()
}

async function handleAddBook() {
  if (!await requireAuth()) return
  showBookPicker.value = true
}

async function submitComment() {
  const id = String(route.params.id || '')
  if (!id || !commentText.value) {
    return
  }
  await createBooklistComment(id, commentText.value)
  commentText.value = ''
  await load()
}

async function handleLike() {
  if (!isAuthed.value) return
  const id = String(route.params.id || '')
  if (!id) return
  const result = await toggleBooklistLike(id)
  if (booklist.value) {
    booklist.value.isLiked = result.liked
    booklist.value.likes = result.likeCount
  }
}

async function handleFollow() {
  if (!isAuthed.value) return
  const id = String(route.params.id || '')
  if (!id) return
  const result = await toggleBooklistFollow(id)
  if (booklist.value) {
    booklist.value.isFollowed = result.followed
    booklist.value.followers = result.followerCount
  }
}

onMounted(async () => {
  await Promise.all([loadAllBooks(), load()])
})
watch(() => route.params.id, load)
</script>

<style scoped>
.clickable {
  cursor: pointer;
}

.clickable:hover {
  opacity: 0.8;
}

.single-column {
  grid-template-columns: 1fr;
}

.action-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
}

.booklist-edit-row {
  margin-bottom: 16px;
}

.auth-hint {
  font-size: 0.9rem;
  margin-bottom: 12px;
}

.liker-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pill {
  padding: 4px 12px;
  background: rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  font-size: 0.9rem;
}

.comment-form {
  display: grid;
  gap: 10px;
  margin-bottom: 12px;
}

textarea {
  min-height: 88px;
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 12px;
  resize: vertical;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.comment-item {
  padding: 12px;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
}

.comment-user {
  font-weight: 600;
  margin-bottom: 4px;
}
</style>
