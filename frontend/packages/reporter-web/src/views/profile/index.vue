<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-wrapper">
        <van-image
          round
          width="80"
          height="80"
          :src="userInfo?.avatar || defaultAvatar"
          fit="cover"
        >
          <template #loading>
            <van-loading type="spinner" size="20" />
          </template>
        </van-image>
      </div>
      <div class="user-name">{{ userInfo?.realName || userInfo?.username || '用户' }}</div>
      <div class="user-role">
        <van-tag type="success" size="medium">{{ getRoleText(userInfo?.role) }}</van-tag>
      </div>
    </div>

    <van-cell-group inset>
      <van-cell title="用户名" :value="userInfo?.username" icon="user-o" />
      <van-cell title="姓名" :value="userInfo?.realName || '-'" icon="contact" />
      <van-cell title="手机号" :value="userInfo?.phone || '未绑定'" icon="phone-o" />
      <van-cell title="邮箱" :value="userInfo?.email || '未绑定'" icon="envelope-o" />
    </van-cell-group>

    <div class="logout-wrapper">
      <van-button type="danger" round block @click="handleLogout" icon="revoke">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { getRoleText } from '@shared/utils/format'
import { showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
userStore.getUserInfo()

const defaultAvatar = 'https://img.yzcdn.cn/vant/cat.jpeg'

const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出登录吗？',
    })
    userStore.logout()
    window.location.href = 'http://localhost:5172/login'
  } catch {}
}
</script>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.profile-header {
  background: linear-gradient(135deg, #07c160, #05a64a);
  padding: 40px 0 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;

  .avatar-wrapper {
    border: 3px solid rgba(255, 255, 255, 0.4);
    border-radius: 50%;
    padding: 3px;
  }

  .user-name {
    font-size: 20px;
    font-weight: 600;
    color: #fff;
  }
}

.logout-wrapper {
  margin: 32px 16px;
}
</style>
