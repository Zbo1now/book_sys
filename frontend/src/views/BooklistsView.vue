<template>
  <section class="page-header">
    <div>
      <h1>书单管理</h1>
      <p class="muted">分为个人书单与书单大厅，支持权限与公开状态管理。</p>
    </div>
    <button v-if="activeScope === 'personal'" class="btn primary" @click="openCreateModal">
      新增书单
    </button>
  </section>

  <section class="section-block tab-row">
    <button class="btn" :class="activeScope === 'hall' ? 'primary' : 'ghost'" @click="switchScope('hall')">书单大厅</button>
    <button class="btn" :class="activeScope === 'collected' ? 'primary' : 'ghost'" @click="switchScope('collected')">我的收藏</button>
    <button class="btn" :class="activeScope === 'personal' ? 'primary' : 'ghost'" @click="switchScope('personal')">用户个人书单</button>
  </section>

  <section class="section-block">
    <div class="grid-3">
      <article v-for="list in lists" :key="list.id" class="list-item-wrap">
        <RouterLink :to="`/booklists/${list.id}`">
          <BooklistCard :list="list" />
        </RouterLink>
        <div v-if="activeScope === 'personal' && list.isOwner" class="list-actions">
          <div class="description-edit" v-if="editingDescription === list.id">
            <textarea
              v-model="editDescriptionText"
              class="description-input"
              placeholder="输入书单简介..."
              rows="2"
            ></textarea>
            <button class="btn primary small" @click="saveDescription(list.id)">保存</button>
            <button class="btn ghost small" @click="cancelEditDescription">取消</button>
          </div>
          <button v-else class="btn ghost small" @click="editDescription(list)">编辑简介</button>
          <button class="btn ghost small" @click="toggleVisibility(list)">{{ list.isPublic ? '设为私密' : '设为公开' }}</button>
          <button class="btn ghost small danger" @click="removeList(list.id)">删除书单</button>
        </div>
      </article>
      <div v-if="lists.length === 0" class="empty-tip">
        <span v-if="activeScope === 'collected'">暂无收藏的书单</span>
        <span v-else>暂无书单</span>
      </div>
    </div>
  </section>

  <BookPickerModal
    :visible="showBookPicker"
    :books="allBooks"
    v-model="selectedBookIds"
    @close="showBookPicker = false"
    @confirm="handleCreate"
  />
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import BooklistCard from '../components/BooklistCard.vue'
import BookPickerModal from '../components/BookPickerModal.vue'
import { createBooklist, deleteBooklist, fetchBooklists, updateBooklist, fetchCollectedBooklists } from '../api/booklists'
import { fetchBooks } from '../api/books'
import { useAuthStore } from '../stores/auth'
import { useRouter } from 'vue-router'
import { useAlert } from '../composables/useAlert'

type Scope = 'hall' | 'personal' | 'collected'

const lists = ref<any[]>([])
const allBooks = ref<Array<{ id: string; title: string; author?: string }>>([])
const showBookPicker = ref(false)
const selectedBookIds = ref<string[]>([])
const activeScope = ref<Scope>('hall')
const authStore = useAuthStore()
const router = useRouter()
const editingDescription = ref<string | null>(null)
const editDescriptionText = ref('')
const { showAlert, showConfirm } = useAlert()

async function openCreateModal() {
  if (!authStore.isAuthed) {
    await router.push({ name: 'auth', query: { redirect: '/booklists' } })
    return
  }
  selectedBookIds.value = []
  showBookPicker.value = true
}

async function switchScope(scope: Scope) {
  if (scope === 'personal' && !authStore.isAuthed) {
    await router.push({ name: 'auth', query: { redirect: '/booklists' } })
    return
  }
  if (scope === 'collected' && !authStore.isAuthed) {
    await router.push({ name: 'auth', query: { redirect: '/booklists' } })
    return
  }
  activeScope.value = scope
  await loadBooklists()
}

async function loadBooklists() {
  if (activeScope.value === 'collected') {
    const result = await fetchCollectedBooklists()
    lists.value = result.items.map((item: any) => ({
      id: item.id,
      title: item.title,
      curator: `创建者：${item.creator?.username ?? '未知'}`,
      description: item.description ?? '',
      visibility: item.isPublic === false ? '私密' : '公开',
      books: item.bookCount ?? 0,
      likes: item.likes ?? 0,
      saves: item.followers ?? 0,
      isPublic: item.isPublic !== false,
      isOwner: false
    }))
    return
  }
  const result = await fetchBooklists({ page: 1, pageSize: 50, scope: activeScope.value })
  lists.value = result.items.map((item: any) => ({
    id: item.id,
    title: item.title,
    curator: `创建者：${item.creator?.username ?? '未知'}`,
    description: item.description ?? '',
    visibility: item.isPublic === false ? '私密' : '公开',
    books: item.bookCount ?? 0,
    likes: item.likes ?? 0,
    saves: item.followers ?? 0,
    isPublic: item.isPublic !== false,
    isOwner: (() => {
      const uid = String(authStore.userId || '')
      const cid = String(item.creator?.id || '')
      return uid !== '' && (cid === `u-${uid}` || cid === uid)
    })()
  }))
}

async function handleCreate(data: { title: string; description: string; bookIds: string[]; isPublic: boolean }) {
  if (!data.title) return
  await createBooklist({
    title: data.title,
    description: data.description,
    isPublic: data.isPublic,
    bookIds: data.bookIds
  })
  showBookPicker.value = false
  activeScope.value = 'personal'
  await loadBooklists()
}

async function toggleVisibility(list: any) {
  await updateBooklist(list.id, { isPublic: !list.isPublic })
  await loadBooklists()
}

async function editDescription(list: any) {
  editingDescription.value = list.id
  editDescriptionText.value = list.description || ''
}

function cancelEditDescription() {
  editingDescription.value = null
  editDescriptionText.value = ''
}

async function saveDescription(id: string) {
  await updateBooklist(id, { description: editDescriptionText.value })
  editingDescription.value = null
  editDescriptionText.value = ''
  await loadBooklists()
}

async function removeList(id: string) {
  showConfirm('确定要删除这个书单吗？', async () => {
    await deleteBooklist(id)
    await loadBooklists()
    showAlert('删除成功', 'success')
  })
}

onMounted(async () => {
  await loadBooklists()
  const result = await fetchBooks({ page: 1, pageSize: 300 })
  allBooks.value = result.items.map((item) => ({ id: item.id, title: item.title, author: item.author }))
})
</script>

<style scoped>
.tab-row {
  display: flex;
  gap: 10px;
}

.list-item-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.list-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  flex-wrap: wrap;
}

.description-edit {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.description-input {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 0.875rem;
  resize: none;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.description-input:focus {
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.btn.danger {
  color: #b42318;
}

.empty-tip {
  grid-column: 1 / -1;
  text-align: center;
  padding: 2rem;
  color: var(--text-muted, #888);
}
</style>
