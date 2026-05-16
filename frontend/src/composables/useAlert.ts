import { ref } from 'vue'

const alertVisible = ref(false)
const alertTitle = ref('提示')
const alertMessage = ref('')
const alertType = ref<'error' | 'warning' | 'info' | 'success'>('info')
const alertButtonText = ref('我知道了')

const confirmVisible = ref(false)
const confirmTitle = ref('确认')
const confirmMessage = ref('')
const confirmCallback = ref<(() => void) | null>(null)

export function useAlert() {
  function showAlert(message: string, type: 'error' | 'warning' | 'info' | 'success' = 'info') {
    alertMessage.value = message
    alertType.value = type
    alertVisible.value = true
  }

  function hideAlert() {
    alertVisible.value = false
  }

  function showConfirm(message: string, callback: () => void) {
    confirmMessage.value = message
    confirmCallback.value = callback
    confirmVisible.value = true
  }

  function confirm() {
    if (confirmCallback.value) {
      confirmCallback.value()
    }
    confirmVisible.value = false
    confirmCallback.value = null
  }

  function cancelConfirm() {
    confirmVisible.value = false
    confirmCallback.value = null
  }

  return {
    alertVisible,
    alertTitle,
    alertMessage,
    alertType,
    alertButtonText,
    confirmVisible,
    confirmTitle,
    confirmMessage,
    showAlert,
    hideAlert,
    showConfirm,
    confirm,
    cancelConfirm
  }
}
