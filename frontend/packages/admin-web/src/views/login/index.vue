<template>
  <div class="login-wrapper">
    <div class="login-bg">
      <div class="bg-shape shape-1"></div>
      <div class="bg-shape shape-2"></div>
      <div class="bg-shape shape-3"></div>
    </div>
    <div class="login-container">
      <div class="login-header">
        <div class="logo-icon">
          <el-icon :size="36"><Tools /></el-icon>
        </div>
        <h1>企业工单系统</h1>
        <p>请选择身份并登录</p>
      </div>

      <div class="role-selector">
        <div
          v-for="role in roleOptions"
          :key="role.value"
          class="role-card"
          :class="{ active: selectedRole === role.value }"
          @click="selectedRole = role.value"
        >
          <div class="role-icon">
            <el-icon :size="24"><component :is="role.icon" /></el-icon>
          </div>
          <div class="role-info">
            <span class="role-name">{{ role.label }}</span>
            <span class="role-desc">{{ role.desc }}</span>
          </div>
          <div class="role-check" v-if="selectedRole === role.value">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>

      <el-form :model="form" :rules="rules" ref="formRef" @submit.prevent="onSubmit" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            :prefix-icon="User"
            size="large"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            :prefix-icon="Lock"
            size="large"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            native-type="submit"
            :loading="loading"
            size="large"
            class="login-btn"
            round
          >
            {{ loading ? '登录中...' : '登 录' }}
          </el-button>
        </el-form-item>
      </el-form>

      <div class="login-footer">
        <span>还没有账号？请联系管理员创建</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@shared/api/auth'
import { ROLE_OPTIONS } from '@shared/types'
import { ElMessage } from 'element-plus'
import { User, Lock, Tools, Check } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()
const selectedRole = ref(3)

const roleOptions = ROLE_OPTIONS

const form = reactive({
  username: '',
  password: '',
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

const APP_URLS = {
  1: 'http://localhost:5173',
  2: 'http://localhost:5174',
  3: 'http://localhost:5175',
}

const onSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  if (!selectedRole.value) {
    ElMessage.warning('请选择登录身份')
    return
  }

  loading.value = true
  try {
    const res = await authApi.login(form)
    const { token, user } = res.data

    if (user.role !== selectedRole.value) {
      ElMessage.error(`该账号不属于${getRoleName(selectedRole.value)}角色`)
      return
    }

    localStorage.setItem('token', token)
    localStorage.setItem('userInfo', JSON.stringify(user))
    userStore.setToken(token)
    await userStore.getUserInfo()

    ElMessage.success(`欢迎回来，${user.realName || user.username}`)

    if (user.role === 1) {
      window.location.href = APP_URLS[1]
    } else if (user.role === 2) {
      window.location.href = APP_URLS[2]
    } else {
      router.push('/dashboard')
    }
  } catch (error) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const getRoleName = (role) => {
  const map = { 1: '报修人', 2: '工程师', 3: '管理员' }
  return map[role] || ''
}
</script>

<style scoped lang="scss">
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  .bg-shape {
    position: absolute;
    border-radius: 50%;
    opacity: 0.1;
    background: #fff;
  }
  .shape-1 {
    width: 600px;
    height: 600px;
    top: -200px;
    right: -100px;
  }
  .shape-2 {
    width: 400px;
    height: 400px;
    bottom: -100px;
    left: -80px;
  }
  .shape-3 {
    width: 300px;
    height: 300px;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
}

.login-container {
  width: 440px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 20px;
  padding: 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
  animation: fadeInUp 0.6s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 28px;

  .logo-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    background: linear-gradient(135deg, #667eea, #764ba2);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    margin: 0 auto 16px;
  }

  h1 {
    font-size: 22px;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 6px;
  }

  p {
    font-size: 14px;
    color: #909399;
  }
}

.role-selector {
  display: flex;
  gap: 10px;
  margin-bottom: 24px;
}

.role-card {
  flex: 1;
  padding: 12px 8px;
  border: 2px solid #e4e7ed;
  border-radius: 12px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
  position: relative;

  &:hover {
    border-color: #a0a4ff;
    background: #f8f8ff;
  }

  &.active {
    border-color: #667eea;
    background: linear-gradient(135deg, rgba(102, 126, 234, 0.08), rgba(118, 75, 162, 0.08));
    box-shadow: 0 2px 12px rgba(102, 126, 234, 0.2);
  }

  .role-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: #f0f2f5;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 6px;
    color: #606266;
    transition: all 0.3s ease;
  }

  &.active .role-icon {
    background: linear-gradient(135deg, #667eea, #764ba2);
    color: #fff;
  }

  .role-name {
    display: block;
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 2px;
  }

  .role-desc {
    display: block;
    font-size: 10px;
    color: #909399;
  }

  .role-check {
    position: absolute;
    top: -6px;
    right: -6px;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background: #667eea;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
  }
}

.login-form {
  .el-form-item {
    margin-bottom: 18px;
  }
}

.login-btn {
  width: 100%;
  height: 44px;
  font-size: 16px;
  letter-spacing: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;

  &:hover {
    opacity: 0.9;
  }
}

.login-footer {
  text-align: center;
  font-size: 13px;
  color: #909399;
  margin-top: 8px;
}
</style>
