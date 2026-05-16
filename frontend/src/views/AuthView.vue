<template>
	<section class="auth-page">
		<div class="auth-card">
			<header class="auth-header">
				<div class="brand-mark">SY</div>
				<div>
					<h2>{{ mode === 'login' ? '欢迎回来' : '创建账号' }}</h2>
					<p class="muted">
						{{ mode === 'login' ? '登录后进入书友主页。' : '注册后即可加入书友交流。' }}
					</p>
				</div>
			</header>

			<div class="auth-tabs" role="tablist" aria-label="登录与注册切换">
				<button
					type="button"
					class="tab"
					:class="{ active: mode === 'login' }"
					@click="switchMode('login')"
				>
					登录
				</button>
				<button
					type="button"
					class="tab"
					:class="{ active: mode === 'register' }"
					@click="switchMode('register')"
				>
					注册
				</button>
			</div>

			<form class="auth-form" @submit.prevent="onSubmit">
				<label v-if="mode === 'register'" class="field">
					<span>用户名</span>
					<input
						v-model.trim="form.username"
						autocomplete="username"
						inputmode="text"
						placeholder="请输入用户名"
						:disabled="pending"
					/>
				</label>

				<label v-if="mode === 'login' && loginMethod === 'username'" class="field">
					<span>用户名</span>
					<input
						v-model.trim="form.username"
						autocomplete="username"
						inputmode="text"
						placeholder="请输入用户名"
						:disabled="pending"
					/>
				</label>

				<label v-if="mode === 'register' || (mode === 'login' && loginMethod === 'email')" class="field">
					<span>邮箱</span>
					<input
						v-model.trim="form.email"
						type="email"
						autocomplete="email"
						inputmode="email"
						placeholder="name@example.com"
						:disabled="pending"
					/>
				</label>

				<label class="field">
					<span>密码</span>
					<input
						v-model="form.password"
						type="password"
						autocomplete="current-password"
						placeholder="请输入密码"
						:disabled="pending"
					/>
				</label>

				<div class="field">
					<span>验证码</span>
					<div class="captcha-row">
						<input
							v-model.trim="form.captchaValue"
							autocomplete="off"
							inputmode="text"
							placeholder="请输入验证码"
							:disabled="pending"
						/>
						<img
							v-if="captcha.imageBase64"
							:src="captchaImageSrc"
							class="captcha-image"
							:class="{ loading: pending }"
							@click="refreshCaptcha"
							alt="验证码"
						/>
						<div v-else class="captcha-placeholder" @click="refreshCaptcha">
							刷新
						</div>
					</div>
					<p class="auth-note">点击图片可刷新验证码</p>
				</div>

				<div v-if="mode === 'login'" class="field role-selector">
					<span>登录身份</span>
					<select
						v-model="form.role"
						class="role-select"
						:disabled="pending"
					>
						<option value="user">普通用户</option>
						<option value="admin">管理员</option>
					</select>
				</div>

				<p v-if="error" class="form-hint">{{ error }}</p>

				<div class="submit-area">
					<button
						v-if="mode === 'login'"
						type="button"
						class="method-toggle"
						@click="loginMethod = loginMethod === 'username' ? 'email' : 'username'"
					>
						{{ loginMethod === 'username' ? '使用邮箱登录' : '使用用户名登录' }}
					</button>
					<button type="submit" class="btn primary submit-btn" :disabled="pending">
						{{ pending ? '处理中…' : mode === 'login' ? '登录' : '注册' }}
					</button>
				</div>
			</form>

			<p class="auth-note">
				{{ apiBaseHint }}
			</p>
		</div>
	</section>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../stores/auth'

type Mode = 'login' | 'register'
type Role = 'user' | 'admin'

type ApiResponse<T> = {
	code: number
	message: string
	data: T
}

type CaptchaResponse = {
	id: string
	code: string
	imageBase64: string
}

type AuthResponse = {
	userId: number
	username: string
	email: string
	token: string
	role?: string
}

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const mode = ref<Mode>('login')
const loginMethod = ref<'username' | 'email'>('username')
const pending = ref(false)
const error = ref('')

const apiBase = ((import.meta as any).env?.VITE_API_BASE_URL as string | undefined) || 'http://localhost:8080'
const apiBaseHint = computed(() => `后端地址：${apiBase}`)

const captcha = reactive<{ id: string; code: string; imageBase64: string }>({ id: '', code: '', imageBase64: '' })

const captchaImageSrc = computed(() => {
	if (!captcha.imageBase64) return ''
	return `data:image/png;base64,${captcha.imageBase64}`
})

const form = reactive({
	username: '',
	email: '',
	password: '',
	captchaValue: '',
	role: 'user' as Role
})

function buildUrl(path: string) {
	const trimmed = apiBase.replace(/\/$/, '')
	return `${trimmed}${path}`
}

function resetError() {
	error.value = ''
}

function switchMode(next: Mode) {
	if (pending.value) return
	mode.value = next
	resetError()
	form.captchaValue = ''
	refreshCaptcha()
}

async function requestJson<T>(url: string, init?: RequestInit): Promise<ApiResponse<T>> {
	const res = await fetch(url, {
		...init,
		headers: {
			'Content-Type': 'application/json',
			...(init?.headers || {})
		}
	})

	let payload: any
	try {
		payload = await res.json()
	} catch {
		throw new Error('服务返回非 JSON 响应')
	}

	if (!res.ok) {
		const message = payload?.message || `HTTP ${res.status}`
		throw new Error(message)
	}

	return payload as ApiResponse<T>
}

async function refreshCaptcha() {
	try {
		const resp = await requestJson<CaptchaResponse>(buildUrl('/api/auth/captcha'))
		if (resp.code !== 0) throw new Error(resp.message || '获取验证码失败')
		captcha.id = resp.data.id
		captcha.code = resp.data.code
		captcha.imageBase64 = resp.data.imageBase64
	} catch (e: any) {
		error.value = e?.message || '获取验证码失败'
		captcha.id = ''
		captcha.code = ''
		captcha.imageBase64 = ''
	}
}

function validate() {
	if (mode.value === 'register' && !form.username) return '请输入用户名'
	if (mode.value === 'login' && loginMethod.value === 'username' && !form.username) return '请输入用户名'
	if (mode.value === 'login' && loginMethod.value === 'email' && !form.email) return '请输入邮箱'
	if (mode.value === 'register' && !form.email) return '请输入邮箱'
	if (!form.password) return '请输入密码'
	if (!captcha.id) return '验证码未就绪，请刷新'
	if (!form.captchaValue) return '请输入验证码'
	return ''
}

async function onSubmit() {
	const msg = validate()
	if (msg) {
		error.value = msg
		return
	}

	pending.value = true
	error.value = ''

	try {
		let resp: ApiResponse<AuthResponse>

		if (mode.value === 'login') {
			const account = loginMethod.value === 'username' ? form.username : form.email
			resp = await requestJson<AuthResponse>(buildUrl('/api/auth/login'), {
				method: 'POST',
				body: JSON.stringify({ account, password: form.password, captchaId: captcha.id, captchaValue: form.captchaValue, role: form.role })
			})
		} else {
			resp = await requestJson<AuthResponse>(buildUrl('/api/auth/register'), {
				method: 'POST',
				body: JSON.stringify({ username: form.username, email: form.email, password: form.password, captchaId: captcha.id, captchaValue: form.captchaValue })
			})
		}

		if (resp.code !== 0) throw new Error(resp.message || '操作失败')

		const userRole = resp.data.role?.toLowerCase() as 'user' | 'admin'
		authStore.login(resp.data.token, resp.data.username, String(resp.data.userId), userRole)

		if (userRole === 'admin') {
			await router.push('/admin')
		} else {
			const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/'
			await router.push(redirect)
		}
	} catch (e: any) {
		error.value = e?.message || '请求失败'
		await refreshCaptcha()
		form.captchaValue = ''
	} finally {
		pending.value = false
	}
}

onMounted(async () => {
	if (authStore.isAuthed) {
		await router.replace('/')
		return
	}
	await refreshCaptcha()
})
</script>

<style scoped>
.role-selector {
	margin-bottom: 0.5rem;
}

.role-select {
	width: 100%;
	padding: 10px 12px;
	margin-top: 8px;
	border: 1px solid var(--border-color, #ddd);
	border-radius: 8px;
	background: white;
	font-size: 0.9rem;
	color: var(--text-color, #333);
	cursor: pointer;
	outline: none;
	transition: border-color 0.2s;
}

.role-select:focus {
	border-color: var(--primary-color, #4a90a4);
}

.role-select:disabled {
	opacity: 0.6;
	cursor: not-allowed;
}

.submit-area {
	position: relative;
	margin-top: 0.5rem;
}

.method-toggle {
	position: absolute;
	top: -1.5rem;
	right: 0;
	background: none;
	border: none;
	color: var(--primary-color, #4a90a4);
	font-size: 0.8rem;
	cursor: pointer;
	padding: 2px 4px;
}

.method-toggle:hover {
	text-decoration: underline;
	color: #357a8a;
}

.submit-btn {
	width: 100%;
}

.captcha-row {
	display: flex;
	gap: 8px;
	margin-top: 8px;
}

.captcha-row input {
	flex: 1;
}

.captcha-image {
	width: 120px;
	height: 40px;
	border-radius: 8px;
	cursor: pointer;
	object-fit: cover;
	border: 1px solid var(--border-color, #ddd);
	transition: all 0.2s ease;
}

.captcha-image:hover {
	border-color: var(--primary-color, #4a90a4);
	transform: scale(1.02);
}

.captcha-image.loading {
	opacity: 0.5;
}

.captcha-placeholder {
	width: 120px;
	height: 40px;
	border-radius: 8px;
	border: 1px solid var(--border-color, #ddd);
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	font-size: 0.85rem;
	color: var(--text-muted, #666);
	background: #f5f5f5;
	transition: all 0.2s ease;
}

.captcha-placeholder:hover {
	background: #eee;
	border-color: var(--primary-color, #4a90a4);
}
</style>
