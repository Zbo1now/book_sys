<template>
  <section class="hero">
    <div class="hero-copy">
      <p class="eyebrow">阅读社群服务</p>
      <h1>以阅读偏好为核心的书友交流平台。</h1>
      <p class="muted">支持书单协作、活动组织与高质量讨论。</p>
      <div class="hero-actions">
        <button class="btn primary" @click="scrollToRecommendations">查看推荐</button>
      </div>
    </div>
    <div class="hero-card">
      <div class="hero-card-top">
        <span class="pill">推荐清单</span>
        <span class="muted">本期</span>
      </div>
      <h2>城市夜读参考清单</h2>
      <p class="muted">偏氛围叙事，适合稳定节奏阅读。</p>
      <div class="hero-card-footer">
        <div class="avatar-stack">
          <span>AQ</span>
          <span>JM</span>
          <span>LS</span>
        </div>
        <button class="btn primary" @click="goToBooklists">进入书单</button>
      </div>
    </div>
  </section>

  <section class="section-block">
    <SectionHeader
      title="编辑精选"
      subtitle="本期重点推荐书目。"
      action="查看全部"
      @action="router.push({ name: 'books' })"
    />
    <div class="grid-4">
      <BookCard v-for="book in books" :key="book.id" :book="book" />
    </div>
  </section>

  <section v-if="recommendations.length > 0" class="section-block">
    <SectionHeader
      title="为你推荐"
      :subtitle="recommendationReason"
    />
    <div class="grid-4">
      <BookCard v-for="book in recommendations" :key="book.id" :book="book" />
    </div>
  </section>

  <section class="section-block">
    <SectionHeader
      title="热门书单"
      subtitle="由书友与社群创建的高关注书单。"
      action="浏览书单"
      @action="router.push({ name: 'booklists' })"
    />
    <div class="grid-3">
      <BooklistCard v-for="list in lists" :key="list.id" :list="list" />
    </div>
    <div v-if="lists.length === 0" class="empty-tip">
      暂无书单
    </div>
  </section>

  <section class="section-block">
    <SectionHeader
      title="近期活动"
      subtitle="线上研讨、线下活动与读书交流。"
      action="查看活动"
      @action="router.push({ name: 'activities' })"
    />
    <div class="grid-3">
      <ActivityCard v-for="activity in activities" :key="activity.id" :activity="activity" />
    </div>
    <div v-if="activities.length === 0" class="empty-tip">
      暂无活动
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import SectionHeader from '../components/SectionHeader.vue'
import BookCard from '../components/BookCard.vue'
import BooklistCard from '../components/BooklistCard.vue'
import ActivityCard from '../components/ActivityCard.vue'
import { fetchFeaturedBooks } from '../api/books'
import { fetchRecommendations } from '../api/recommendations'
import { fetchBooklists } from '../api/booklists'
import { fetchActivities } from '../api/activities'

const books = ref<any[]>([])
const recommendations = ref<any[]>([])
const lists = ref<any[]>([])
const activities = ref<any[]>([])
const recommendationReason = ref('根据您的阅读偏好推荐')
const showRecommendations = ref(false)
const router = useRouter()

function scrollToRecommendations() {
  showRecommendations.value = true
  const el = document.querySelector('.section-block:nth-of-type(3)')
  if (el) {
    el.scrollIntoView({ behavior: 'smooth' })
  }
}

function goToBooklists() {
  router.push({ name: 'booklists' })
}

function formatMode(location?: string) {
  if (!location) return '线上'
  if (location.includes('线上') && location.includes('线下')) return '线上+线下'
  if (location.includes('线下')) return '线下'
  return '线上'
}

function formatDateTime(isoStr?: string) {
  if (!isoStr) return ''
  try {
    const d = new Date(isoStr)
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
  } catch {
    return isoStr
  }
}

onMounted(async () => {
  const result = await fetchFeaturedBooks({ page: 1, pageSize: 8 })
  books.value = result.items

  try {
    const recResult = await fetchRecommendations(8)
    if (recResult.items && recResult.items.length > 0) {
      recommendations.value = recResult.items
      recommendationReason.value = recResult.reason || '根据您的阅读偏好推荐'
      showRecommendations.value = true
    }
  } catch (error) {
    console.error('加载推荐失败:', error)
  }

  try {
    const booklistsResult = await fetchBooklists({ page: 1, pageSize: 3 })
    lists.value = booklistsResult.items.map((item: any) => ({
      id: item.id,
      title: item.title,
      curator: `创建者：${item.curator?.username || '平台'}`,
      description: item.description || '',
      visibility: item.isPublic ? '公开' : '私密',
      books: item.bookCount || 0,
      likes: item.likeCount || 0,
      saves: item.followerCount || 0
    }))
  } catch (error) {
    console.error('加载书单失败:', error)
    lists.value = []
  }

  try {
    const activitiesResult = await fetchActivities({ page: 1, pageSize: 3 })
    activities.value = activitiesResult.items.map((item: any) => ({
      id: item.id,
      title: item.title,
      description: item.description || '',
      startDate: item.startDate || '',
      endDate: item.endDate || '',
      startDateDisplay: formatDateTime(item.startDate),
      endDateDisplay: formatDateTime(item.endDate),
      mode: formatMode(item.location),
      host: `主理：${item.organizer?.username || '平台'}`,
      attendees: item.participantCount ?? 0,
      isParticipating: item.isParticipating ?? false,
      dynamicStatus: item.dynamicStatus || 'upcoming'
    }))
  } catch (error) {
    console.error('加载活动失败:', error)
    activities.value = []
  }
})
</script>

<style scoped>
.empty-tip {
  text-align: center;
  padding: 3rem;
  color: var(--text-muted, #888);
}
</style>
