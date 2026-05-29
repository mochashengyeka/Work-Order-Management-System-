<template>
  <div class="login-page">
    <div class="login-header">
      <div class="header-icon">
        <van-icon name="user-o" size="40" color="#fff" />
      </div>
      <h1>企业工单系统</h1>
      <p>请选择身份并登录</p>
    </div>

    <div class="role-tabs">
      <div
        v-for="role in roleOptions"
        :key="role.value"
        class="role-tab"
        :class="{ active: selectedRole === role.value }"
        @click="selectedRole = role.value"
      >
        <van-icon :name="role.icon" size="22" />
        <span>{{ role.label }}</span>
      </div>
    </div>

    <van-form @submit="onSubmit" ref="formRef" class="login-form">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          left-icon="user-o"
          :rules="[{ required: true, message: '请输入用户名' }]"
          clearable
        />
        <van-field
          v-model="form.password"
          name="password"
          label="密码"
          type="password"
          placeholder="请输入密码"
          left-icon="lock"
          :rules="[{ required: true, message: '请输入密码' }]"
        />
      </van-cell-group>

      <div class="submit-wrapper">
        <van-button round block type="primary" native-type="submit" :loading="loading" size="large">
          {{ loading ? '登录中...' : '登 录' }}
        </van-button>
      </div>
    </van-form>

    <div class="login-footer">
      <span>还没有账号？请联系管理员创建</span>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@shared/api/auth'
import { ROLE_OPTIONS } from '@shared/types'
import { showToast, showSuccessToast, showFailToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const formRef = ref()
const selectedRole = ref(1)

const roleOptions = ROLE_OPTIONS.map(r => ({
  ...r,
  icon: r.value === 1 ? 'user-o' : r.value === 2 ? 'setting-o' : 'manager-o',
}))

const form = reactive({
  username: '',
  password: '',
})

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
    showToast('请选择登录身份')
    return
  }

  loading.value = true
  try {
    const res = await authApi.login(form)
    const { token, user } = res.data

    if (user.role !== selectedRole.value) {
      const roleNames = { 1: '报修人', 2: '工程师', 3: '管理员' }
      showFailToast(`该账号不属于${roleNames[selectedRole.value]}角色`)
      return
    }

    localStorage.setItem('token', token)
    localStorage.setItem('userInfo', JSON.stringify(user))
    userStore.setToken(token)
    await userStore.getUserInfo()

    showSuccessToast(`欢迎回来，${user.realName || user.username}`)

    if (user.role === 2) {
      window.location.href = APP_URLS[2]
    } else if (user.role === 3) {
      window.location.href = APP_URLS[3]
    } else {
      router.replace('/order/my')
    }
  } catch (error) {
    showFailToast(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.login-page {
  min-height: 100vh;
  background: linear-gradient(180deg, #07c160 0%, #07c160 200px, #f7f8fa 200px);
}

.login-header {
  text-align: center;
  padding: 40px 0 30px;

  .header-icon {
    width: 72px;
    height: 72px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.2);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    backdrop-filter: blur(10px);
  }

  h1 {
    font-size: 22px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 6px;
  }

  p {
    font-size: 14px;
    color: rgba(255, 255, 255, 0.8);
  }
}

.role-tabs {
  display: flex;
  margin: 0 16px 20px;
  background: #fff;
  border-radius: 12px;
  padding: 6px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.role-tab {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  padding: 10px 4px;
  border-radius: 8px;
  font-size: 12px;
  color: #969799;
  cursor: pointer;
  transition: all 0.3s;

  &.active {
    background: linear-gradient(135deg, #07c160, #05a64a);
    color: #fff;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(7, 193, 96, 0.3);
  }
}

.login-form {
  margin-top: 8px;
}

.submit-wrapper {
  margin: 24px 16px;
}

.login-footer {
  text-align: center;
  font-size: 13px;
  color: #969799;
  margin-top: 16px;
}
</style>
