<template>
  <component :is="wrapper" class="card book-card" v-bind="wrapperProps">
    <div class="book-cover">
      <img v-if="showCover" :src="book.cover" :alt="book.title" />
      <div v-else class="cover-fallback" />
      <span v-if="book.tag" class="book-badge">{{ book.tag }}</span>
    </div>
    <div class="book-info">
      <h3>{{ book.title }}</h3>
      <p class="muted">{{ book.author }}</p>
      <div class="book-meta">
        <span v-if="book.rating !== undefined && book.rating !== null">{{ book.rating }} 分</span>
        <span v-if="book.reviews !== undefined && book.reviews !== null">{{ book.reviews }} 条评价</span>
      </div>
    </div>
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface BookCardData {
  id?: string
  title: string
  author: string
  rating?: string | number
  reviews?: number
  tag?: string
  cover?: string
}

const props = defineProps<{ book: BookCardData }>()

const showCover = computed(() => !!props.book.cover && props.book.cover !== 'image-url')

const wrapper = computed(() => (props.book.id ? 'RouterLink' : 'article'))
const wrapperProps = computed(() => (props.book.id ? { to: `/books/${props.book.id}` } : {}))
</script>

<style scoped>
.cover-fallback {
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.06);
  border-radius: 16px;
}
</style>
