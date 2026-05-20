<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useMenuStore } from '@/stores/menu'
import { usePermissionStore } from '@/stores/permission'
import http from '@/api/http'

const router = useRouter()
const menuStore = useMenuStore()
const permissionStore = usePermissionStore()
const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  username: '',
  password: '',
})

const login = async () => {
  errorMessage.value = ''
  if (!form.username || !form.password) {
    errorMessage.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const res = await http.post('/api/auth/login', form)
    if (res.data?.code === 0 && res.data?.data?.token) {
      localStorage.setItem('token', res.data.data.token)
      localStorage.setItem('tokenHeaderName', res.data.data.tokenHeaderName || 'satoken')
      localStorage.setItem('user', JSON.stringify(res.data.data))
      menuStore.loaded = false
      permissionStore.loaded = false
      await menuStore.loadFromApi()
      await permissionStore.loadFromApi()
      await router.push('/dashboard/home')
      return
    }
    errorMessage.value = res.data?.message || '登录失败，请稍后重试'
  } catch (error: any) {
    errorMessage.value = error?.response?.data?.message || '网络异常，请检查后端服务'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login-page">
    <div class="bg-glow bg-glow-1"></div>
    <div class="bg-glow bg-glow-2"></div>

    <div class="login-card">
      <p class="brand-tag">智研SDP平台</p>
      <h1>欢迎登录</h1>
      <p class="sub-title">Smart Development Platform</p>

      <form class="login-form" @submit.prevent="login">
        <label>
          <span>账号</span>
          <input v-model.trim="form.username" type="text" placeholder="请输入用户名" autocomplete="username" />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model.trim="form.password"
            type="password"
            placeholder="请输入密码"
            autocomplete="current-password"
          />
        </label>

        <p v-if="errorMessage" class="error-text">{{ errorMessage }}</p>
        <button type="submit" :disabled="loading">{{ loading ? '登录中...' : '进入系统' }}</button>
      </form>
    </div>
  </div>
</template>
<style lang="scss" scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--login-bg);
}

.bg-glow {
  position: absolute;
  width: 420px;
  height: 420px;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
}

.bg-glow-1 {
  background: var(--login-glow-1);
  top: -120px;
  left: -80px;
}

.bg-glow-2 {
  background: var(--login-glow-2);
  right: -120px;
  bottom: -120px;
}

.login-card {
  width: 420px;
  max-width: calc(100vw - 40px);
  padding: 40px 32px;
  border-radius: 20px;
  background: var(--login-card-bg);
  border: 1px solid var(--panel-border);
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(8px);
  color: var(--text-color);
  box-sizing: border-box;
}

.brand-tag {
  margin: 0;
  color: var(--login-brand);
  letter-spacing: 3px;
  font-size: 13px;
}

h1 {
  margin: 12px 0 4px;
  font-size: 28px;
  color: var(--text-color);
}

.sub-title {
  margin: 0 0 26px;
  color: var(--text-muted);
  font-size: 14px;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

label span {
  color: var(--text-weak);
  font-size: 13px;
}

input {
  height: 42px;
  border-radius: 10px;
  border: 1px solid var(--login-input-border);
  background: var(--login-input-bg);
  color: var(--text-color);
  padding: 0 12px;
  outline: none;
  transition:
    border-color 0.2s,
    box-shadow 0.2s;
}

input::placeholder {
  color: var(--text-muted);
}

input:focus {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 3px var(--primary-soft);
}

button {
  margin-top: 6px;
  height: 44px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--login-glow-2) 100%);
  color: var(--on-primary);
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition:
    transform 0.15s,
    box-shadow 0.2s;
}

button:hover {
  transform: translateY(-1px);
  box-shadow: 0 10px 24px var(--primary-soft);
}

button:disabled {
  opacity: 0.72;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.error-text {
  margin: 0;
  color: var(--login-error);
  font-size: 13px;
}
</style>
