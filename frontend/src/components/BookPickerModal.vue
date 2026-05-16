<template>
  <div v-if="visible" class="picker-overlay" @click.self="$emit('close')">
    <div class="picker-modal">
      <header class="picker-header">
        <div class="meta-fields">
          <div class="input-with-hint">
            <input
              v-model="innerTitle"
              type="text"
              placeholder="书单名称"
              class="meta-input"
              :class="{ 'error': !innerTitle.trim() && showTitleError }"
              @keydown.enter="handleConfirm"
            />
            <span v-if="!innerTitle.trim() && showTitleError" class="error-hint">请输入书单名称</span>
          </div>
          <input
            v-model="innerDescription"
            type="text"
            placeholder="书单描述（选填）"
            class="meta-input"
            @keydown.enter="handleConfirm"
          />
          <div class="visibility-toggle">
            <label class="toggle-option" :class="{ active: isPublic }" @click="isPublic = true">
              <span>🌐 公开</span>
            </label>
            <label class="toggle-option" :class="{ active: !isPublic }" @click="isPublic = false">
              <span>🔒 私密</span>
            </label>
          </div>
        </div>
        <div class="search-wrapper">
          <input
            v-model="searchQuery"
            type="text"
            placeholder="搜索书名或作者..."
            class="search-input"
          />
          <button v-if="searchQuery" class="clear-btn" @click="searchQuery = ''">×</button>
        </div>
        <button class="btn ghost small" @click="$emit('close')">关闭</button>
      </header>

      <div class="picker-body">
        <p v-if="filteredBooks.length === 0 && searchQuery" class="no-result">未找到匹配图书</p>
        <label v-for="book in filteredBooks" :key="book.id" class="picker-row">
          <input type="checkbox" :value="book.id" v-model="innerSelected" />
          <span class="book-title">{{ book.title }}</span>
          <span v-if="book.author" class="book-author">{{ book.author }}</span>
        </label>
      </div>

      <footer class="picker-footer">
        <span class="muted">{{ innerSelected.length }} 本已选</span>
        <div class="footer-btns">
          <button class="btn ghost" @click="$emit('close')">取消</button>
          <button class="btn primary" @click="handleConfirm">确认创建</button>
        </div>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'

type BookOption = { id: string; title: string; author?: string }

const props = defineProps<{
  visible: boolean
  books: BookOption[]
  modelValue: string[]
  initialTitle?: string
  initialDescription?: string
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update:modelValue', value: string[]): void
  (e: 'confirm', value: { title: string; description: string; bookIds: string[]; isPublic: boolean }): void
}>()

const innerSelected = ref<string[]>([])
const innerTitle = ref('')
const innerDescription = ref('')
const searchQuery = ref('')
const showTitleError = ref(false)
const isPublic = ref(true)

const filteredBooks = computed(() => {
  if (!searchQuery.value.trim()) {
    return props.books
  }
  const q = searchQuery.value.toLowerCase().trim()
  return props.books.filter(
    (book) =>
      book.title.toLowerCase().includes(q) ||
      (book.author?.toLowerCase().includes(q) ?? false)
  )
})

watch(
  () => props.modelValue,
  (next) => {
    innerSelected.value = [...next]
  },
  { immediate: true }
)

watch(
  () => props.visible,
  (isVisible) => {
    if (isVisible) {
      innerTitle.value = props.initialTitle ?? ''
      innerDescription.value = props.initialDescription ?? ''
      innerSelected.value = [...props.modelValue]
      searchQuery.value = ''
      showTitleError.value = false
      isPublic.value = true
    }
  }
)

function handleConfirm() {
  if (!innerTitle.value.trim()) {
    showTitleError.value = true
    return
  }
  emit('update:modelValue', innerSelected.value)
  emit('confirm', {
    title: innerTitle.value.trim(),
    description: innerDescription.value.trim(),
    bookIds: innerSelected.value,
    isPublic: isPublic.value
  })
}
</script>

<style scoped>
.picker-overlay {
  position: fixed;
  inset: 0;
  background: rgba(29, 27, 26, 0.6);
  backdrop-filter: blur(8px);
  display: grid;
  place-items: center;
  z-index: 120;
  animation: modalFadeIn 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.picker-modal {
  width: min(720px, 92vw);
  max-height: 80vh;
  background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
  border-radius: 24px;
  box-shadow: 
    0 25px 60px -12px rgba(29, 27, 26, 0.25),
    0 0 0 1px rgba(255, 255, 255, 0.8);
  display: grid;
  grid-template-rows: auto 1fr auto;
  animation: modalSlideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.9);
}

.picker-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding: 1.25rem 1.5rem;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  gap: 1rem;
  flex-wrap: wrap;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
}

.meta-fields {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
  min-width: 200px;
}

.meta-input {
  width: 100%;
  padding: 0.65rem 0.875rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.9rem;
  outline: none;
  box-sizing: border-box;
  background: white;
  transition: all 0.2s;
}

.meta-input:focus {
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.meta-input::placeholder {
  color: #94a3b8;
}

.meta-input.error {
  border-color: #e54b4f;
  box-shadow: 0 0 0 3px rgba(229, 75, 79, 0.1);
}

.input-with-hint {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.error-hint {
  font-size: 0.75rem;
  color: #e54b4f;
  margin-left: 0.25rem;
}

.visibility-toggle {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.toggle-option {
  padding: 0.45rem 0.875rem;
  border-radius: 8px;
  font-size: 0.85rem;
  cursor: pointer;
  background: #f1f5f9;
  border: 2px solid transparent;
  transition: all 0.2s;
  user-select: none;
}

.toggle-option:hover {
  background: #e2e8f0;
}

.toggle-option.active {
  background: rgba(74, 144, 164, 0.1);
  border-color: #4a90a4;
  color: #4a90a4;
}

.search-wrapper {
  position: relative;
  flex: 1;
  max-width: 280px;
}

.search-input {
  width: 100%;
  padding: 0.65rem 2.5rem 0.65rem 0.875rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  font-size: 0.9rem;
  outline: none;
  box-sizing: border-box;
  background: white;
  transition: all 0.2s;
}

.search-input:focus {
  border-color: #4a90a4;
  box-shadow: 0 0 0 3px rgba(74, 144, 164, 0.1);
}

.search-input::placeholder {
  color: #94a3b8;
}

.clear-btn {
  position: absolute;
  right: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  background: #f1f5f9;
  border: none;
  cursor: pointer;
  font-size: 0.9rem;
  color: #64748b;
  line-height: 1;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
  transition: all 0.2s;
}

.clear-btn:hover {
  background: #e2e8f0;
}

.no-result {
  padding: 2rem;
  text-align: center;
  color: #94a3b8;
  font-size: 0.9rem;
}

.picker-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 1rem 1.5rem;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
  flex-wrap: wrap;
  gap: 0.75rem;
  background: #fafbfc;
}

.muted {
  color: #94a3b8;
  font-size: 0.875rem;
}

.footer-btns {
  display: flex;
  gap: 0.75rem;
}

.footer-btns .btn {
  padding: 0.65rem 1.25rem;
  border-radius: 10px;
  font-weight: 500;
  font-size: 0.9rem;
  transition: all 0.2s;
}

.footer-btns .btn.primary {
  background: linear-gradient(135deg, #4a90a4 0%, #3a7a8a 100%);
  border: none;
  color: white;
  box-shadow: 0 2px 8px rgba(74, 144, 164, 0.3);
}

.footer-btns .btn.primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(74, 144, 164, 0.4);
}

.footer-btns .btn.ghost {
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  color: #475569;
}

.footer-btns .btn.ghost:hover {
  background: #e2e8f0;
}

.picker-body {
  overflow: auto;
  padding: 1rem 1.5rem;
  display: grid;
  gap: 0.5rem;
}

.picker-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  padding: 0.75rem 1rem;
  background: white;
  cursor: pointer;
  transition: all 0.2s;
}

.picker-row:hover {
  border-color: #cbd5e1;
  background: #fafbfc;
}

.picker-row:has(input:checked) {
  border-color: #4a90a4;
  background: rgba(74, 144, 164, 0.05);
}

.picker-row input[type="checkbox"] {
  width: 18px;
  height: 18px;
  accent-color: #4a90a4;
  cursor: pointer;
}

.book-title {
  font-weight: 500;
  color: #1e293b;
}

.book-author {
  font-size: 0.85rem;
  color: #64748b;
  margin-left: auto;
}

.btn.ghost.small {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  font-size: 0.85rem;
  background: rgba(0, 0, 0, 0.05);
  border: none;
  color: #64748b;
  cursor: pointer;
  transition: all 0.2s;
}

.btn.ghost.small:hover {
  background: rgba(0, 0, 0, 0.1);
}
</style>
