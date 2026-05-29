<template>
  <router-view />
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { wsClient } from '@/utils/websocket'
import { showDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const handleUrge = (data) => {
  showDialog({
    title: '催单提醒',
    message: `${data.message || '用户催促您尽快处理工单'}\n\n工单号：${data.orderNo || ''}`,
    confirmButtonText: '立即处理',
    cancelButtonText: '稍后处理',
    showCancelButton: true,
    confirmButtonColor: '#1989fa',
  }).then(() => {
    if (data.orderId) {
      router.push(`/order/${data.orderId}`)
    } else {
      router.push('/tasks')
    }
  }).catch(() => {})
}

onMounted(async () => {
  const userInfo = await userStore.getUserInfo()
  if (userInfo && userInfo.id) {
    wsClient.connect(userInfo.id)
    wsClient.on('URGE', handleUrge)
  }
})

onUnmounted(() => {
  wsClient.off('URGE', handleUrge)
  wsClient.disconnect()
})
</script>
