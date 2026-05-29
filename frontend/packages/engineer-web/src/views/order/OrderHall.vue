<template>
  <div class="order-hall">
    <van-nav-bar title="接单大厅" fixed placeholder>
      <template #right>
        <van-icon name="search" size="20" @click="showSearch = !showSearch" />
      </template>
    </van-nav-bar>

    <div class="user-bar">
      <van-icon name="manager-o" size="20" color="#fff" />
      <span class="user-name">{{ userInfo?.realName || userInfo?.username || '工程师' }}</span>
      <span class="user-dept">工程师</span>
    </div>

    <van-search
      v-show="showSearch"
      v-model="searchKeyword"
      placeholder="搜索工单号/标题"
      @search="onSearch"
      @clear="onSearch"
    />

    <div class="stats-card">
      <div class="stats-item">
        <span class="stats-value primary">{{ totalOrders }}</span>
        <span class="stats-label">待抢单</span>
      </div>
      <div class="stats-divider"></div>
      <div class="stats-item" @click="router.push('/tasks')">
        <span class="stats-value success">{{ myOrdersCount }}</span>
        <span class="stats-label">我的工单</span>
        <van-icon name="arrow" size="14" />
      </div>
    </div>

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list
        v-model:loading="loading"
        :finished="finished"
        finished-text="没有更多了"
        @load="onLoad"
      >
        <div v-for="item in list" :key="item.id" class="order-card" @click="goDetail(item.id)">
          <div class="card-header">
            <div class="card-tags">
              <van-tag :type="getPriorityType(item.priority)" size="medium">
                {{ getPriorityText(item.priority) }}
              </van-tag>
              <van-tag v-if="isNearDeadline(item)" type="danger" size="medium" class="deadline-tag">
                即将超时
              </van-tag>
            </div>
            <span class="order-no">{{ item.orderNo }}</span>
          </div>
          <div class="card-title">{{ item.title }}</div>
          <div class="card-info">
            <div class="info-row">
              <span class="info-item">
                <van-icon name="location-o" size="14" />
                <span>{{ item.location || '-' }}</span>
              </span>
              <span class="info-item">
                <van-icon name="clock-o" size="14" />
                <span>{{ formatTime(item.createTime) }}</span>
              </span>
            </div>
            <div class="info-row" v-if="item.reporterName">
              <span class="info-item">
                <van-icon name="user-o" size="14" />
                <span>报修人：{{ item.reporterName }}</span>
              </span>
            </div>
          </div>
          <div class="card-footer">
            <van-button
              type="primary"
              size="small"
              round
              class="grab-button"
              :loading="grabbingId === item.id"
              loading-text="抢单中..."
              @click.stop="handleGrab(item)"
            >
              立即抢单
            </van-button>
          </div>
        </div>

        <van-empty v-if="!loading && list.length === 0" description="暂无可抢单的工单" />
      </van-list>
    </van-pull-refresh>

    <van-tabbar v-model="activeTab" route :fixed="true" :border="true" active-color="#1989fa">
      <van-tabbar-item icon="wap-home-o" to="/hall">大厅</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/tasks">工单</van-tabbar-item>
      <van-tabbar-item icon="chart-trending-o" to="/statistics">统计</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/parts">备件</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { engineerOrderApi } from '@shared/api/order'
import { getPriorityText, getPriorityType } from '@shared/utils/format'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'
import dayjs from 'dayjs'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref(null)
const activeTab = ref(0)
const loading = ref(true)
const finished = ref(false)
const refreshing = ref(false)
const list = ref([])
const page = ref(1)
const totalOrders = ref(0)
const myOrdersCount = ref(0)
const grabbingId = ref(null)
const showSearch = ref(false)
const searchKeyword = ref('')

const onLoad = async () => {
  try {
    const res = await engineerOrderApi.getOrderHall({
      page: page.value,
      size: 10,
      keyword: searchKeyword.value || undefined,
    })
    const data = res.data
    if (page.value === 1) {
      list.value = data.records
    } else {
      const existingIds = new Set(list.value.map(o => o.id))
      const newItems = data.records.filter(o => !existingIds.has(o.id))
      list.value.push(...newItems)
    }
    totalOrders.value = data.total
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
  refreshing.value = true
  loading.value = true
  onLoad()
}

const onSearch = () => {
  page.value = 1
  finished.value = false
  list.value = []
  loading.value = true
  onLoad()
}

const handleGrab = async (item) => {
  try {
    await showConfirmDialog({
      title: '确认抢单',
      message: `确定要抢取工单「${item.title}」吗？`,
      confirmButtonText: '确认抢单',
      confirmButtonColor: '#1989fa',
    })
  } catch {
    return
  }

  grabbingId.value = item.id
  try {
    await engineerOrderApi.grab(item.id)
    showSuccessToast('抢单成功')
    list.value = list.value.filter(o => o.id !== item.id)
    totalOrders.value--
    myOrdersCount.value++
  } catch (error) {
    showFailToast(error.message || '抢单失败')
    onRefresh()
  } finally {
    grabbingId.value = null
  }
}

const goDetail = (id) => router.push(`/order/${id}`)

const formatTime = (time) => dayjs(time).format('MM-DD HH:mm')
const isNearDeadline = (item) => {
  if (!item.slaDeadline) return false
  return dayjs(item.slaDeadline).diff(dayjs(), 'hour') < 1
}

const loadMyCount = async () => {
  const res = await engineerOrderApi.getMyOrders({ page: 1, size: 1 })
  myOrdersCount.value = res.data.total
}

onMounted(async () => {
  userInfo.value = await userStore.getUserInfo()
  onLoad()
  loadMyCount()
})
</script>

<style scoped lang="scss">
.order-hall {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 60px;
}

.user-bar {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 16px;
  margin: 8px 12px;
  background: linear-gradient(135deg, #1989fa, #07c160);
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

.stats-card {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  margin: 8px 12px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.stats-item {
  flex: 1;
  text-align: center;
  cursor: default;

  &:last-child {
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
  }
}

.stats-divider {
  width: 1px;
  height: 36px;
  background: #ebedf0;
}

.stats-value {
  display: block;
  font-size: 26px;
  font-weight: 700;

  &.primary { color: #1989fa; }
  &.success { color: #07c160; }
}

.stats-label {
  font-size: 13px;
  color: #969799;
}

.order-card {
  margin: 8px 12px;
  padding: 14px 16px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.card-tags {
  display: flex;
  gap: 6px;
  align-items: center;
}

.deadline-tag {
  animation: blink 1s infinite;
}

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.order-no {
  font-size: 12px;
  color: #969799;
}

.card-title {
  font-size: 15px;
  font-weight: 500;
  color: #323233;
  margin-bottom: 8px;
  line-height: 1.4;
}

.card-info {
  margin-bottom: 12px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 3px;
}

.card-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  border-top: 1px solid #f5f5f5;
}

.grab-button {
  min-width: 100px;
  font-weight: 600;
}
</style>
