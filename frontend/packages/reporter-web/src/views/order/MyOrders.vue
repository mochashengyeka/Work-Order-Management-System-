<template>
  <div class="my-orders">
    <van-nav-bar title="我的工单" fixed placeholder>
      <template #right>
        <van-icon name="plus" size="22" @click="router.push('/order/create')" />
      </template>
    </van-nav-bar>

    <div class="user-bar">
      <van-icon name="user-o" size="20" color="#fff" />
      <span class="user-name">{{ userInfo?.realName || userInfo?.username || '报修人' }}</span>
      <span class="user-dept">报修人</span>
    </div>

    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky offset-top="46">
      <van-tab title="全部" name="" />
      <van-tab title="待派单" name="1" />
      <van-tab title="处理中" name="3" />
      <van-tab title="待验收" name="4" />
      <van-tab title="已完成" name="5" />
    </van-tabs>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <van-cell
          v-for="item in list"
          :key="item.id"
          @click="goDetail(item.id)"
          class="order-cell"
        >
          <template #title>
            <div class="order-header">
              <van-tag :type="getPriorityType(item.priority)" size="medium">
                {{ getPriorityText(item.priority) }}
              </van-tag>
              <span class="order-no">{{ item.orderNo }}</span>
            </div>
          </template>
          <template #label>
            <div class="order-title">{{ item.title }}</div>
            <div class="order-meta">
              <span><van-icon name="location-o" /> {{ item.location }}</span>
              <span>{{ formatTime(item.createTime) }}</span>
            </div>
          </template>
          <template #value>
            <van-tag :type="getStatusType(item.status)" size="medium">
              {{ getStatusText(item.status) }}
            </van-tag>
          </template>
        </van-cell>
      </van-list>
    </van-pull-refresh>

    <van-floating-bubble icon="plus" @click="router.push('/order/create')" />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { reporterOrderApi } from '@shared/api/order'
import { getPriorityText, getPriorityType, getStatusText, getStatusType, formatTime } from '@shared/utils/format'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref(null)
const activeTab = ref('')
const loading = ref(true)
const finished = ref(false)
const refreshing = ref(false)
const list = ref([])
const page = ref(1)

const onLoad = async () => {
  try {
    const res = await reporterOrderApi.getMyOrders({
      page: page.value,
      size: 10,
      status: activeTab.value || undefined,
    })
    const data = res.data
    if (page.value === 1) {
      list.value = data.records
    } else {
      const existingIds = new Set(list.value.map(o => o.id))
      const newItems = data.records.filter(o => !existingIds.has(o.id))
      list.value.push(...newItems)
    }
    finished.value = list.value.length >= data.total
    if (!finished.value) page.value++
  } catch {
    finished.value = true
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onRefresh = () => {
  page.value = 1
  finished.value = false
  loading.value = true
  refreshing.value = true
  onLoad()
}

const onTabChange = () => {
  page.value = 1
  finished.value = false
  list.value = []
  loading.value = true
  onLoad()
}

const goDetail = (id) => router.push(`/order/detail/${id}`)

onMounted(async () => {
  userInfo.value = await userStore.getUserInfo()
  onLoad()
})
</script>

<style scoped lang="scss">
.my-orders {
  background: #f7f8fa;
  min-height: 100vh;
}

.user-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  margin: 8px 12px;
  background: linear-gradient(135deg, #07c160, #05a64a);
  border-radius: 12px;
  color: #fff;

  .user-name {
    flex: 1;
    font-size: 15px;
    font-weight: 600;
  }

  .user-dept {
    font-size: 12px;
    opacity: 0.8;
  }
}

.order-cell {
  margin: 0 12px 8px;
  border-radius: 10px !important;
  background: #fff;
}

.order-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.order-no {
  font-size: 12px;
  color: #969799;
}

.order-title {
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 6px;
  color: #323233;
}

.order-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #969799;
}
</style>
