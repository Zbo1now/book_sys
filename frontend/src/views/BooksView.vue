<template>
  <section class="page-header">
    <div>
      <h1>图书库</h1>
      <p class="muted">支持按 8 大分类筛选真实出版书目。</p>
    </div>
    <div class="search-box">
      <input 
        v-model="searchQuery" 
        type="text" 
        placeholder="搜索书名或作者..." 
        @keyup.enter="handleSearch"
      />
      <button class="btn primary" @click="handleSearch">搜索</button>
    </div>
    <div class="filter-row">
      <button
        v-for="category in categories"
        :key="category"
        class="btn small"
        :class="activeCategory === category ? 'primary' : 'ghost'"
        @click="selectCategory(category)"
      >
        {{ category }}
      </button>
    </div>
  </section>

  <section class="section-block">
    <p v-if="loading" class="muted">正在加载图书数据...</p>
    <p v-else-if="errorMessage" class="muted">{{ errorMessage }}</p>
    <div v-else class="grid-4">
      <BookCard v-for="book in books" :key="book.id" :book="book" />
    </div>
    <div v-if="!loading && books.length === 0" class="empty-state">
      <p class="muted">没有找到匹配的图书</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import BookCard from '../components/BookCard.vue'
import { fetchBooks } from '../api/books'
import { BOOK_CATEGORIES_WITH_ALL } from '../constants/bookCategories'

const books = ref<any[]>([])
const loading = ref(false)
const errorMessage = ref('')
const categories = BOOK_CATEGORIES_WITH_ALL
const activeCategory = ref('全部')
const searchQuery = ref('')

async function loadBooks() {
  loading.value = true
  errorMessage.value = ''
  try {
    const tag = activeCategory.value === '全部' ? undefined : activeCategory.value
    const search = searchQuery.value.trim() || undefined
    const result = await fetchBooks({ page: 1, pageSize: 100, tag, search })
    books.value = result.items
  } catch (error) {
    console.error(error)
    errorMessage.value = '图书数据加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

function selectCategory(category: string) {
  if (activeCategory.value === category) {
    return
  }
  activeCategory.value = category
  loadBooks()
}

function handleSearch() {
  loadBooks()
}

onMounted(async () => {
  await loadBooks()
})
</script>

<style scoped>
.search-box {
  display: flex;
  gap: 0.5rem;
  margin: 1rem 0;
  max-width: 500px;
}

.search-box input {
  flex: 1;
  padding: 0.5rem 1rem;
  border: 1px solid var(--border-color, #ddd);
  border-radius: 6px;
  font-size: 0.95rem;
}

.search-box input:focus {
  outline: none;
  border-color: var(--primary-color, #4a90d9);
}

.empty-state {
  text-align: center;
  padding: 3rem;
}
</style>
