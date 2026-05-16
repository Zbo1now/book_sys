<template>
  <RouterLink v-if="list.id" :to="`/booklists/${list.id}`" class="card booklist-card">
    <div class="booklist-header">
      <div>
        <h3>{{ list.title }}</h3>
        <p class="muted">{{ list.curator }}</p>
      </div>
      <span class="pill">{{ list.visibility }}</span>
    </div>
    <p class="booklist-desc">{{ list.description }}</p>
    <div class="booklist-meta">
      <span>{{ list.books }} 本</span>
      <span>{{ list.likes }} 赞</span>
      <span>{{ list.saves }} 收藏</span>
    </div>
  </RouterLink>
  <article v-else class="card booklist-card">
    <div class="booklist-header">
      <div>
        <h3>{{ list.title }}</h3>
        <p class="muted">{{ list.curator }}</p>
      </div>
      <span class="pill">{{ list.visibility }}</span>
    </div>
    <p class="booklist-desc">{{ list.description }}</p>
    <div class="booklist-meta">
      <span>{{ list.books }} 本</span>
      <span>{{ list.likes }} 赞</span>
      <span>{{ list.saves }} 收藏</span>
    </div>
  </article>
</template>

<script setup lang="ts">
interface BooklistCardData {
  id?: string
  title: string
  curator: string
  description: string
  visibility: string
  books: number
  likes: number
  saves: number
}

defineProps<{ list: BooklistCardData }>()
</script>
