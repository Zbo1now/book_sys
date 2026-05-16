<template>
  <Teleport to="body">
    <div v-if="visible" class="modal-overlay" @click="handleClose">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div class="modal-icon-wrapper" :class="iconClass">
            <span class="modal-icon">{{ icon }}</span>
          </div>
          <h3>{{ title }}</h3>
        </div>
        <div class="modal-body">
          <p>{{ message }}</p>
        </div>
        <div class="modal-footer">
          <button v-if="showSecondary" class="btn ghost" @click="handleSecondary">{{ secondaryText }}</button>
          <button class="btn primary" @click="handlePrimary">{{ buttonText }}</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  visible: boolean
  title?: string
  message?: string
  type?: 'error' | 'warning' | 'info' | 'success'
  buttonText?: string
  showSecondary?: boolean
  secondaryText?: string
}>(), {
  title: '提示',
  message: '',
  type: 'info',
  buttonText: '我知道了',
  showSecondary: false,
  secondaryText: '取消'
})

const emit = defineEmits<{
  close: []
  primary: []
  secondary: []
}>()

const icon = computed(() => {
  switch (props.type) {
    case 'error': return '❌'
    case 'warning': return '⚠️'
    case 'success': return '✅'
    default: return 'ℹ️'
  }
})

const iconClass = computed(() => `icon-${props.type}`)

function handleClose() {
  emit('close')
}

function handlePrimary() {
  emit('primary')
  emit('close')
}

function handleSecondary() {
  emit('secondary')
  emit('close')
}
</script>

<style scoped>
.modal-icon-wrapper {
  background: linear-gradient(135deg, #d36f3e 0%, #ab542b 100%);
  box-shadow: 0 12px 35px rgba(211, 111, 62, 0.35);
}

.icon-error {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  box-shadow: 0 12px 35px rgba(239, 68, 68, 0.35);
}

.icon-warning {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  box-shadow: 0 12px 35px rgba(245, 158, 11, 0.35);
}

.icon-success {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  box-shadow: 0 12px 35px rgba(16, 185, 129, 0.35);
}

.icon-info {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  box-shadow: 0 12px 35px rgba(102, 126, 234, 0.35);
}
</style>
