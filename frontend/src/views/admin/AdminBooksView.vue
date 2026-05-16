<template>
  <section class="panel">
    <p v-if="errorMessage" class="error-msg">{{ errorMessage }}</p>

    <div class="toolbar">
      <input v-model.trim="keyword" placeholder="搜索图书名或作者" @keyup.enter="loadBooks" />
      <select v-model="selectedTag" @change="loadBooks">
        <option value="">全部分类</option>
        <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
      </select>
      <div class="toolbar-right">
        <button class="btn ghost small" @click="loadBooks">查询</button>
        <span class="muted">共 {{ total }} 条</span>
      </div>
    </div>

    <div class="create-row">
      <input v-model.trim="createForm.title" placeholder="书名 *" />
      <input v-model.trim="createForm.author" placeholder="作者 *" />
      <select v-model="createForm.tag">
        <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
      </select>
      <input type="file" accept="image/*" @change="onCoverSelected" />
      <button class="btn primary small" @click="createBook">新增图书</button>
    </div>

    <div class="table-wrap">
      <table>
        <thead>
          <tr>
            <th>图书ID</th>
            <th>封面</th>
            <th>书名</th>
            <th>作者</th>
            <th>分类</th>
            <th>评分</th>
            <th>评论数</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="book in books" :key="book.id">
            <td>{{ book.id }}</td>
            <td>
              <div class="cover-cell">
                <img v-if="book.cover" :src="book.cover" :alt="book.title" class="cover-thumb" />
                <span v-else class="muted">无封面</span>
                <input type="file" accept="image/*" @change="(e) => onBookCoverSelected(e, book)" class="cover-upload" />
              </div>
            </td>
            <td><input v-model.trim="book.title" class="inline-input" /></td>
            <td><input v-model.trim="book.author" class="inline-input" /></td>
            <td>
              <select v-model="book.tag" class="inline-input">
                <option v-for="category in categories" :key="category" :value="category">{{ category }}</option>
              </select>
            </td>
            <td>{{ book.rating }}</td>
            <td>{{ book.reviews }}</td>
            <td>
              <span :class="book.featured ? 'status on' : 'status off'">{{ book.featured ? '推荐' : '普通' }}</span>
            </td>
            <td>
              <button class="btn ghost small" @click="saveBook(book)">保存</button>
            
              <button class="btn danger small" @click="removeBook(book.id)">删除</button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { createAdminBook, fetchAdminBooks, fetchAdminBookCategories, updateAdminBook, deleteAdminBook, type AdminBookDto } from '../../api/admin'
import { fetchBooks } from '../../api/books'
import { BOOK_CATEGORIES } from '../../constants/bookCategories'
import { useAlert } from '../../composables/useAlert'

type AdminBook = {
  id: string
  title: string
  author: string
  tag: string
  rating: string
  reviews: number
  cover: string
  featured: boolean
  newCoverFile?: File | null
}

const { showAlert, showConfirm } = useAlert()
const books = ref<AdminBook[]>([])
const total = ref(0)
const keyword = ref('')
const selectedTag = ref('')
const categories = ref<string[]>([...BOOK_CATEGORIES])
const createForm = ref({ title: '', author: '', tag: categories.value[0] })
const coverFile = ref<File | null>(null)
const errorMessage = ref('')

function mapBook(item: AdminBookDto): AdminBook {
  return {
    id: item.id,
    title: item.title,
    author: item.author,
    tag: item.tag || categories.value[0],
    rating: item.rating,
    reviews: item.reviews || 0,
    cover: item.cover || '',
    featured: item.featured
  }
}

async function loadBooks() {
  errorMessage.value = ''
  try {
    const result = await fetchAdminBooks({ 
      page: 1, 
      pageSize: 200, 
      keyword: keyword.value || undefined,
      tag: selectedTag.value || undefined
    })
    books.value = result.items.map(mapBook)
    total.value = result.total
  } catch (error: any) {
    const message = String(error?.message || '')
    if (message.includes('未授权') || message.includes('权限')) {
      errorMessage.value = '管理员登录状态已失效，请重新登录后台。'
    } else {
      errorMessage.value = '后台数据加载失败，已切换展示前台图书数据。'
    }
    const fallback = await fetchBooks({ 
      page: 1, 
      pageSize: 200, 
      search: keyword.value || undefined,
      tag: selectedTag.value || undefined
    })
    books.value = fallback.items.map((item) => ({
      id: item.id,
      title: item.title,
      author: item.author,
      tag: categories.value.includes(item.tag as any) ? item.tag : categories.value[0],
      rating: item.rating,
      reviews: 0,
      cover: item.cover || '',
      featured: false
    }))
    total.value = fallback.total
  }
}



async function saveBook(book: AdminBook) {
  const form = new FormData()
  form.append('title', book.title)
  form.append('author', book.author)
  form.append('tag', book.tag)
  form.append('featured', String(book.featured))
  if (book.newCoverFile) {
    form.append('cover', book.newCoverFile)
  }
  try {
    const updated = await updateAdminBook(book.id, form)
    Object.assign(book, mapBook(updated))
    showAlert('保存成功', 'success')
  } catch (error: any) {
    showAlert('保存失败: ' + (error?.message || '未知错误'), 'error')
  }
}

async function removeBook(id: string) {
  showConfirm('确定要删除这本图书吗？', async () => {
    try {
      await deleteAdminBook(id)
      books.value = books.value.filter((book) => book.id !== id)
      total.value--
      showAlert('删除成功', 'success')
    } catch (error: any) {
      errorMessage.value = error?.message || '删除失败'
    }
  })
}

function onCoverSelected(event: Event) {
  const input = event.target as HTMLInputElement
  coverFile.value = input.files?.[0] || null
}

function onBookCoverSelected(event: Event, book: AdminBook) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0] || null
  if (file) {
    book.newCoverFile = file
    const reader = new FileReader()
    reader.onload = (e) => {
      book.cover = e.target?.result as string || book.cover
    }
    reader.readAsDataURL(file)
  }
}

async function createBook() {
  if (!createForm.value.title || !createForm.value.author) {
    errorMessage.value = '书名和作者不能为空'
    return
  }
  const form = new FormData()
  form.append('title', createForm.value.title)
  form.append('author', createForm.value.author)
  form.append('tag', createForm.value.tag)
  if (coverFile.value) {
    form.append('cover', coverFile.value)
  }
  try {
    const created = await createAdminBook(form)
    books.value.unshift(mapBook(created))
    total.value++
    createForm.value = { title: '', author: '', tag: categories.value[0] }
    coverFile.value = null
    errorMessage.value = ''
  } catch (error: any) {
    errorMessage.value = error?.message || '创建失败'
  }
}

onMounted(async () => {
  try {
    const serverCategories = await fetchAdminBookCategories()
    if (serverCategories.length > 0) {
      categories.value = serverCategories
    }
  } catch { /* keep local fallback */ }
  await loadBooks()
})
</script>

<style scoped>
.cover-thumb {
  width: 40px;
  height: 56px;
  object-fit: cover;
  border-radius: 4px;
}

.cover-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  align-items: flex-start;
}

.cover-upload {
  font-size: 0.75rem;
  width: 80px;
}

.toolbar {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
}

.toolbar input,
.toolbar select {
  padding: 0.5rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 4px;
}

.toolbar-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: auto;
}

.create-row {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 1rem;
  flex-wrap: wrap;
}

.create-row input,
.create-row select {
  padding: 0.5rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 4px;
  min-width: 120px;
}

.create-row input[type="file"] {
  padding: 0.35rem;
}

.table-wrap {
  overflow-x: auto;
}

table {
  width: 100%;
  border-collapse: collapse;
}

th, td {
  padding: 0.75rem;
  text-align: left;
  border-bottom: 1px solid var(--border-color, #eee);
}

th {
  background: var(--bg-secondary, #f8f9fa);
  font-weight: 600;
}

.inline-input {
  padding: 0.35rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 4px;
  width: 100%;
  min-width: 80px;
}

.status {
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.85rem;
}

.status.on {
  background: #d4edda;
  color: #155724;
}

.status.off {
  background: #f8d7da;
  color: #721c24;
}

.btn.danger {
  background: #dc3545;
  color: white;
}

.btn.danger:hover {
  background: #c82333;
}

.error-msg {
  color: #dc3545;
  margin-bottom: 1rem;
}
</style>
