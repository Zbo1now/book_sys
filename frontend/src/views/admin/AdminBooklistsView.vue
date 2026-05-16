<template>
  <section class="panel">
    <div class="toolbar">
      <input v-model.trim="keyword" placeholder="搜索书单名称" />
      <div class="toolbar-right">
        <button class="btn ghost small" @click="loadLists">查询</button>
        <span class="muted">共 {{ filteredLists.length }} 条</span>
      </div>
    </div>

    <div class="create-row">
      <input v-model.trim="createForm.title" placeholder="新书单名称" />
      <input v-model.trim="createForm.description" placeholder="新书单描述" />
      <button class="btn primary small" @click="createList">新增书单</button>
    </div>

    <div class="card-list">
      <article v-for="list in filteredLists" :key="list.id" class="item-card">
        <div>
          <input v-model.trim="list.title" class="inline-input title-input" />
          <p class="muted">创建者：{{ list.creator }}</p>
          <p class="muted">图书数：{{ list.bookCount }} / 关注：{{ list.followers }}</p>
          <input v-model.trim="list.description" class="inline-input" placeholder="书单描述" />
        </div>
        <div class="actions">
          <button class="btn ghost small" @click="openBookPicker(list)">添加图书</button>
          <button class="btn ghost small" @click="saveList(list.id)">保存</button>
          <button class="btn ghost small" @click="toggleVisibility(list.id)">
            {{ list.visible ? '设为私密' : '设为公开' }}
          </button>
          <button class="btn ghost small danger" @click="removeList(list.id)">删除</button>
        </div>
      </article>
    </div>
  </section>

  <BookPickerModal
    :visible="showBookPicker"
    title="全部图书"
    :books="allBooks"
    v-model="pickerSelectedBookIds"
    @close="showBookPicker = false"
    @confirm="confirmPickBooks"
  />
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import {
  createAdminBooklist,
  deleteAdminBooklist,
  fetchAdminBooklists,
  updateAdminBooklist
} from '../../api/admin'
import { fetchBooks } from '../../api/books'
import BookPickerModal from '../../components/BookPickerModal.vue'
import { useAlert } from '../../composables/useAlert'

type AdminBooklist = {
  id: string
  title: string
  description: string
  creator: string
  bookCount: number
  followers: number
  visible: boolean
  bookIds: string[]
}

type BookOption = { id: string; title: string }

const { showAlert, showConfirm } = useAlert()
const lists = ref<AdminBooklist[]>([])
const keyword = ref('')
const createForm = ref({ title: '', description: '', bookIds: [] as string[] })
const allBooks = ref<BookOption[]>([])
const showBookPicker = ref(false)
const pickerTargetListId = ref('')
const pickerSelectedBookIds = ref<string[]>([])

const filteredLists = computed(() => {
  if (!keyword.value) return lists.value
  const q = keyword.value.toLowerCase()
  return lists.value.filter((item) => item.title.toLowerCase().includes(q))
})

function toggleVisibility(id: string) {
  const target = lists.value.find((item) => item.id === id)
  if (!target) return
  target.visible = !target.visible
  saveList(id)
}

async function removeList(id: string) {
  showConfirm('确定要删除这个书单吗？', async () => {
    await deleteAdminBooklist(id)
    lists.value = lists.value.filter((item) => item.id !== id)
    showAlert('删除成功', 'success')
  })
}

async function saveList(id: string) {
  const target = lists.value.find((item) => item.id === id)
  if (!target) {
    showAlert('书单不存在', 'warning')
    return
  }
  try {
    await updateAdminBooklist(id, {
      title: target.title,
      description: target.description,
      isPublic: target.visible,
      bookIds: target.bookIds
    })
    await loadLists()
    showAlert('保存成功', 'success')
  } catch (error: any) {
    showAlert('保存失败: ' + (error?.message || '未知错误'), 'error')
  }
}

function openBookPicker(list: AdminBooklist) {
  pickerTargetListId.value = list.id
  pickerSelectedBookIds.value = [...list.bookIds]
  showBookPicker.value = true
}

async function confirmPickBooks(data: { title: string; description: string; bookIds: string[]; isPublic: boolean }) {
  const target = lists.value.find((item) => item.id === pickerTargetListId.value)
  if (!target) return
  target.bookIds = [...data.bookIds]
  await saveList(target.id)
  showBookPicker.value = false
}

async function createList() {
  if (!createForm.value.title) {
    showAlert('请输入书单名称', 'warning')
    return
  }
  try {
    await createAdminBooklist({
      title: createForm.value.title,
      description: createForm.value.description,
      isPublic: true,
      bookIds: createForm.value.bookIds
    })
    createForm.value = { title: '', description: '', bookIds: [] }
    await loadLists()
    showAlert('创建成功', 'success')
  } catch (error: any) {
    showAlert('创建失败: ' + (error?.message || '未知错误'), 'error')
  }
}

async function loadAllBooks() {
  const result = await fetchBooks({ page: 1, pageSize: 300 })
  allBooks.value = result.items.map((item) => ({ id: item.id, title: item.title }))
}

async function loadLists() {
  const result = await fetchAdminBooklists({ page: 1, pageSize: 200 })
  lists.value = result.items.map((item) => ({
    id: item.id,
    title: item.title,
    description: item.description ?? '',
    creator: item.creator?.username || '未知',
    bookCount: Number(item.bookCount || 0),
    followers: Number(item.followers || 0),
    visible: item.isPublic !== false,
    bookIds: item.bookIds ?? []
  }))
}

onMounted(async () => {
  await loadAllBooks()
  await loadLists()
})
</script>

<style scoped>
.panel {
  border-radius: var(--radius-md);
  background: rgba(255, 255, 255, 0.75);
  border: 1px solid var(--border);
  padding: 16px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.create-row {
  display: grid;
  grid-template-columns: minmax(180px, 240px) 1fr auto;
  gap: 10px;
  margin-bottom: 14px;
}

input {
  width: min(360px, 100%);
  border: 1px solid var(--border);
  border-radius: 10px;
  padding: 10px 12px;
}

.card-list {
  display: grid;
  gap: 12px;
}

.item-card {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 14px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 14px;
  background: rgba(255, 255, 255, 0.6);
}

.actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.inline-input {
  width: 100%;
  margin-top: 8px;
  padding: 8px 10px;
  border: 1px solid var(--border);
  border-radius: 10px;
}

.title-input {
  font-weight: 700;
  font-size: 1rem;
}

.danger {
  color: #b42318;
}
</style>
