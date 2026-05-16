<template>
  <div class="user-avatar" :style="style">
    <img v-if="avatarUrl" :src="avatarUrl" :alt="username" class="avatar-img" />
    <span v-else class="avatar-text">{{ initials }}</span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  avatarUrl?: string
  username: string
  size?: number
}>(), {
  size: 40
})

const initials = computed(() => props.username.substring(0, 2).toUpperCase())
const style = computed(() => ({
  width: props.size + 'px',
  height: props.size + 'px',
  fontSize: (props.size * 0.38) + 'px'
}))
</script>

<style scoped>
.user-avatar {
  border-radius: 50%;
  background: linear-gradient(135deg, #4a90a4 0%, #6bb3c4 100%);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: 600;
  flex-shrink: 0;
  overflow: hidden;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.avatar-text {
  line-height: 1;
}
</style>
