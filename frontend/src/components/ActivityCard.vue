<template>
  <article class="card activity-card">
    <div class="activity-top">
      <span class="pill">{{ activity.mode }}</span>
      <span :class="statusClass">{{ statusText }}</span>
      <span v-if="activity.approvalStatus" :class="approvalStatusClass">{{ approvalStatusText }}</span>
      <div class="time-info" v-if="activity.startDateDisplay">
        <span class="time-text">{{ activity.startDateDisplay }}</span>
        <span v-if="activity.endDateDisplay" class="time-end"> ~ {{ activity.endDateDisplay }}</span>
      </div>
      <span v-else class="muted">时间待定</span>
    </div>
    <h3>{{ activity.title }}</h3>
    <p class="activity-desc">{{ activity.description }}</p>
    <div class="activity-meta">
      <span>{{ activity.host }}</span>
      <span>{{ activity.attendees }} 人已报名</span>
    </div>
    <div v-if="showSignup" class="activity-actions">
      <button
        v-if="!activity.isParticipating"
        class="btn primary small"
        :class="{ disabled: activity.dynamicStatus === 'ended' }"
        :disabled="signing || activity.dynamicStatus === 'ended'"
        @click.stop="handleSignupClick"
      >
        {{ signing ? '报名中...' : activity.dynamicStatus === 'ended' ? '活动已结束' : '立即报名' }}
      </button>
      <button
        v-else
        class="btn ghost small"
        :disabled="signing || activity.dynamicStatus === 'ended'"
        @click.stop="$emit('cancel', activity.id)"
      >
        {{ signing ? '取消中...' : '已报名(取消)' }}
      </button>
    </div>
  </article>

  <Teleport to="body">
    <div v-if="showModal" class="modal-overlay" @click="showModal = false">
      <div class="modal-content" @click.stop>
        <div class="modal-header">
          <div class="modal-icon-wrapper">
            <span class="modal-icon">{{ activity.dynamicStatus === 'ended' ? '📅' : '⏰' }}</span>
          </div>
          <h3>{{ activity.dynamicStatus === 'ended' ? '活动已结束' : '报名已截止' }}</h3>
        </div>
        <div class="modal-body">
          <p>{{ activity.dynamicStatus === 'ended' ? '该活动已经结束，感谢您的关注！' : '距离活动开始不足3小时，报名通道已关闭。' }}</p>
          <p v-if="activity.dynamicStatus !== 'ended' && activity.startDateDisplay" class="time-hint">活动开始时间：{{ activity.startDateDisplay }}</p>
        </div>
        <div class="modal-footer">
          <button class="btn primary" @click="showModal = false">我知道了</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'

interface ActivityCardData {
  id: string
  title: string
  description: string
  startDate?: string
  endDate?: string
  startDateDisplay: string
  endDateDisplay: string
  mode: string
  host: string
  attendees: number
  isParticipating?: boolean
  dynamicStatus?: string
  approvalStatus?: string
}

const props = withDefaults(defineProps<{ activity: ActivityCardData; showSignup?: boolean; signing?: boolean }>(), {
  showSignup: false,
  signing: false
})

const emit = defineEmits<{
  signup: [id: string]
  cancel: [id: string]
}>()

const showModal = ref(false)

function handleSignupClick() {
  if (props.activity.dynamicStatus === 'ended') {
    showModal.value = true
  } else {
    emit('signup', props.activity.id)
  }
}

const statusText = computed(() => {
  switch (props.activity.dynamicStatus) {
    case 'upcoming':
      return '待开始'
    case 'ongoing':
      return '进行中'
    case 'ended':
      return '已结束'
    default:
      return '待开始'
  }
})

const statusClass = computed(() => {
  switch (props.activity.dynamicStatus) {
    case 'upcoming':
      return 'status-upcoming'
    case 'ongoing':
      return 'status-ongoing'
    case 'ended':
      return 'status-ended'
    default:
      return 'status-upcoming'
  }
})

const approvalStatusText = computed(() => {
  switch (props.activity.approvalStatus) {
    case 'pending':
      return '待审批'
    case 'approved':
      return '已通过'
    case 'rejected':
      return '已拒绝'
    default:
      return ''
  }
})

const approvalStatusClass = computed(() => {
  switch (props.activity.approvalStatus) {
    case 'pending':
      return 'approval-pending'
    case 'approved':
      return 'approval-approved'
    case 'rejected':
      return 'approval-rejected'
    default:
      return ''
  }
})
</script>

<style scoped>
.activity-top {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex-wrap: wrap;
}

.time-info {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-wrap: wrap;
}

.time-text {
  font-size: 0.85rem;
  color: var(--text-muted, #666);
}

.time-end {
  font-size: 0.85rem;
  color: var(--text-muted, #666);
}

.status-upcoming {
  background-color: #e0f2fe;
  color: #0369a1;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
}

.status-ongoing {
  background-color: #dcfce7;
  color: #166534;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
}

.status-ended {
  background-color: #fef2f2;
  color: #dc2626;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
}

.approval-pending {
  background-color: #fef3c7;
  color: #d97706;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
}

.approval-approved {
  background-color: #dcfce7;
  color: #16a34a;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
}

.approval-rejected {
  background-color: #fee2e2;
  color: #dc2626;
  padding: 2px 8px;
  border-radius: 9999px;
  font-size: 0.75rem;
}

.activity-actions {
  margin-top: 0.75rem;
}

.btn.disabled {
  background-color: #e5e7eb;
  color: #9ca3af;
  cursor: not-allowed;
}

.time-hint {
  font-size: 0.875rem;
  color: #94a3b8;
  margin-top: 0.75rem !important;
  padding: 0.75rem 1rem;
  background: rgba(29, 27, 26, 0.06);
  border-radius: 12px;
  display: inline-block;
}
</style>
