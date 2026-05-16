<template>
  <section class="admin-login-page">
    <div class="admin-login-card">
      <p class="badge">Admin Console</p>
      <h1>管理员后台登录</h1>
      <p class="muted">登录后将自动跳转到后台首页，并使用真实数据库权限校验。</p>

      <form class="admin-login-form" @submit.prevent="onSubmit">
        <label>
          <span>管理员账号</span>
          <input v-model.trim="form.account" placeholder="请输入管理员账号" :disabled="pending" />
        </label>

        <label>
          <span>管理员密码</span>
          <input
            v-model="form.password"
            type="password"
            placeholder="请输入管理员密码"
            :disabled="pending"
          />
        </label>

        <p v-if="error" class="error">{{ error }}</p>

        <button class="btn primary login-btn" type="submit" :disabled="pending">
          {{ pending ? '登录中...' : '登录后台' }}
        </button>
      </form>

    </div>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAdminStore } from '../../stores/admin'

const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const pending = ref(false)
const error = ref('')
const form = reactive({
  account: '',
  password: ''
})

async function onSubmit() {
  if (!form.account || !form.password) {
    error.value = '请输入管理员账号和密码'
    return
  }

  pending.value = true
  error.value = ''
  try {
    await adminStore.login(form.account, form.password)
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/dashboard'
    await router.push(redirect)
  } catch (e: any) {
    error.value = e?.message || '登录失败'
  } finally {
    pending.value = false
  }
}
</script>

<style scoped>
.admin-login-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
  background: radial-gradient(circle at top left, #fff6eb 0%, var(--bg) 45%, var(--bg-deep) 100%);
}

.admin-login-card {
  width: min(460px, 92vw);
  padding: 28px;
  border-radius: var(--radius-lg);
  background: rgba(255, 255, 255, 0.78);
  border: 1px solid var(--border);
  box-shadow: var(--shadow);
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.badge {
  width: fit-content;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 0.8rem;
  background: rgba(211, 111, 62, 0.18);
  color: var(--brand-dark);
  font-weight: 700;
}

.admin-login-form {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
  font-weight: 600;
}

input {
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  padding: 12px;
  font-size: 0.95rem;
  background: rgba(255, 255, 255, 0.9);
}

.login-btn {
  width: 100%;
  margin-top: 8px;
}

.error {
  color: #b42318;
  font-weight: 600;
}

</style>
