<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="22"><Tools /></el-icon>
        <span>工单管理系统</span>
      </div>
      <el-menu
          :default-active="activeMenu"
          router
          background-color="#1d1e2c"
          text-color="#a6a7b3"
          active-text-color="#fff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataLine /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-sub-menu index="/order">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>工单管理</span>
          </template>
          <el-menu-item index="/order/list">工单列表</el-menu-item>
        </el-sub-menu>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/part">
          <el-icon><Box /></el-icon>
          <span>备件管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentTitle">{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-badge :value="0" :max="99" class="notice-badge">
            <el-icon :size="20"><Bell /></el-icon>
          </el-badge>
          <el-dropdown @command="handleCommand" trigger="click">
            <span class="user-info">
              <el-avatar :size="32" :src="userInfo?.avatar" class="user-avatar">
                {{ (userInfo?.realName || userInfo?.username || 'U')[0] }}
              </el-avatar>
              <span class="user-name">{{ userInfo?.realName || userInfo?.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessageBox } from 'element-plus'
import {
  DataLine, Document, User, Box, ArrowDown, Tools, Bell, SwitchButton
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const userInfo = computed(() => userStore.userInfo)
userStore.getUserInfo()

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => {
  return route.meta?.title || ''
})

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
      userStore.logout()
      window.location.href = 'http://localhost:5172/login'
    } catch {}
  }
}
</script>

<style scoped lang="scss">
.layout-container {
  height: 100vh;
}

.aside {
  background-color: #1d1e2c;
  overflow-y: auto;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #fff;
    font-size: 17px;
    font-weight: 700;
    background: linear-gradient(135deg, #667eea, #764ba2);
    letter-spacing: 2px;
  }

  .el-menu {
    border-right: none;
  }

  .el-menu-item.is-active {
    background: linear-gradient(90deg, rgba(102, 126, 234, 0.3), transparent) !important;
    border-right: 3px solid #667eea;
  }
}

.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  .header-left {
    display: flex;
    align-items: center;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;
  }

  .notice-badge {
    cursor: pointer;
    color: #606266;
    &:hover { color: #409EFF; }
  }

  .user-info {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 8px;

    .user-avatar {
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: #fff;
      font-weight: 600;
    }

    .user-name {
      font-size: 14px;
      color: #303133;
    }
  }
}

.el-main {
  background: #f5f7fa;
  padding: 20px;
}
</style>
