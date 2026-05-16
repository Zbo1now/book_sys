<template>
  <div class="section-header">
    <div>
      <p class="section-kicker" v-if="kicker">{{ kicker }}</p>
      <h2 class="section-title">{{ title }}</h2>
      <p class="section-subtitle" v-if="subtitle">{{ subtitle }}</p>
    </div>
    <button v-if="action" class="btn ghost small" @click="$emit('action')">{{ action }}</button>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; subtitle?: string; kicker?: string; action?: string }>()
</script>
