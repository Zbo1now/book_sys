<template>
  <RouterLink v-if="userId" :to="`/user/${userId}`" class="user-link">{{ username }}</RouterLink>
  <span v-else class="user-text">{{ username }}</span>
</template>

<script setup lang="ts">
defineProps<{
  username: string
  userId?: string
}>()
</script>

<style scoped>
.user-link {
  color: #4a90a4;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.15s;
}

.user-link:hover {
  color: #2d6a7e;
  text-decoration: underline;
}

.user-text {
  font-weight: 500;
  color: #333;
}
</style>
