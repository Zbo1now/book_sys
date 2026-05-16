<template>
  <div class="app-shell">
    <NavBar v-if="showShell" />
    <main :class="showShell ? 'page-shell' : 'page-shell auth-shell'">
      <RouterView />
    </main>
    <AppFooter v-if="showShell" />
    
    <Teleport to="body">
      <div v-if="alertVisible" class="modal-overlay" @click="hideAlert">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <div class="modal-icon-wrapper" :class="alertTypeClass">
              <span class="modal-icon">{{ alertIcon }}</span>
            </div>
            <h3>{{ alertType === 'success' ? '成功' : alertType === 'error' ? '错误' : alertType === 'warning' ? '警告' : '提示' }}</h3>
          </div>
          <div class="modal-body">
            <p>{{ alertMessage }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn primary" @click="hideAlert">{{ alertButtonText }}</button>
          </div>
        </div>
      </div>
    </Teleport>

    <Teleport to="body">
      <div v-if="confirmVisible" class="modal-overlay" @click="cancelConfirm">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <div class="modal-icon-wrapper">
              <span class="modal-icon">⚠️</span>
            </div>
            <h3>确认</h3>
          </div>
          <div class="modal-body">
            <p>{{ confirmMessage }}</p>
          </div>
          <div class="modal-footer">
            <button class="btn secondary" @click="cancelConfirm">取消</button>
            <button class="btn primary" @click="confirm">确定</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import NavBar from './components/NavBar.vue'
import AppFooter from './components/AppFooter.vue'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAlert } from './composables/useAlert'

const route = useRoute()
const showShell = computed(() => !['auth', 'admin'].includes(String(route.meta.layout || 'default')))

const {
  alertVisible,
  alertMessage,
  alertType,
  alertButtonText,
  confirmVisible,
  confirmMessage,
  hideAlert,
  confirm,
  cancelConfirm
} = useAlert()

const alertIcon = computed(() => {
  switch (alertType.value) {
    case 'error': return '❌'
    case 'warning': return '⚠️'
    case 'success': return '✅'
    default: return 'ℹ️'
  }
})

const alertTypeClass = computed(() => {
  switch (alertType.value) {
    case 'error': return 'icon-error'
    case 'warning': return 'icon-warning'
    case 'success': return 'icon-success'
    default: return 'icon-info'
  }
})
</script>
