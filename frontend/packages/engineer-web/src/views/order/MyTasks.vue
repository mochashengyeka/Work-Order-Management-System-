<template>
  <div class="my-tasks">
    <van-nav-bar title="我的工单" fixed placeholder />

    <div class="user-bar">
      <van-icon name="manager-o" size="20" color="#fff" />
      <span class="user-name">{{ userInfo?.realName || userInfo?.username || '工程师' }}</span>
      <van-tag type="primary" size="small">工程师</van-tag>
    </div>

    <div class="stats-mini" v-if="!loading">
      <div class="stat-mini-item" :class="{ active: activeTab === '2' }" @click="switchTab('2')">
        <span class="mini-value">{{ stats.pendingAccept || 0 }}</span>
        <span class="mini-label">待接单</span>
      </div>
      <div class="stat-mini-item" :class="{ active: activeTab === '3' }" @click="switchTab('3')">
        <span class="mini-value">{{ stats.processing || 0 }}</span>
        <span class="mini-label">处理中</span>
      </div>
      <div class="stat-mini-item" :class="{ active: activeTab === '4' }" @click="switchTab('4')">
        <span class="mini-value">{{ stats.pending || 0 }}</span>
        <span class="mini-label">待验收</span>
      </div>
      <div class="stat-mini-item" :class="{ active: activeTab === '5' }" @click="switchTab('5')">
        <span class="mini-value">{{ stats.completed || 0 }}</span>
        <span class="mini-label">已完成</span>
      </div>
    </div>

    <van-tabs v-model:active="activeTab" @change="onTabChange" sticky offset-top="46">
      <van-tab title="待接单" name="2" />
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
        <div v-for="item in list" :key="item.id" class="order-card" @click="goDetail(item.id)">
          <div class="card-header">
            <div class="card-tags">
              <van-tag :type="getPriorityType(item.priority)" size="medium">
                {{ getPriorityText(item.priority) }}
              </van-tag>
              <van-tag :type="getStatusType(item.status)" size="medium">
                {{ getStatusText(item.status) }}
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
          </div>
          <div class="card-footer" v-if="item.status === 2">
            <van-button
              type="primary"
              size="small"
              round
              class="action-button"
              :loading="acceptingId === item.id"
              loading-text="接单中..."
              @click.stop="handleAccept(item)"
            >
              确认接单
            </van-button>
          </div>
          <div class="card-footer" v-else-if="item.status === 3">
            <van-button
              type="primary"
              size="small"
              round
              class="action-button"
              @click.stop="goDetail(item.id)"
            >
              处理工单
            </van-button>
          </div>
          <div class="card-footer" v-else-if="item.status === 4">
            <van-button
              type="warning"
              size="small"
              round
              plain
              class="action-button"
              @click.stop="goDetail(item.id)"
            >
              查看详情
            </van-button>
          </div>
        </div>

        <van-empty v-if="!loading && list.length === 0" description="暂无工单" />
      </van-list>
    </van-pull-refresh>

    <van-tabbar v-model="activeTab" route :fixed="true" active-color="#1989fa">
      <van-tabbar-item icon="wap-home-o" to="/hall">大厅</van-tabbar-item>
      <van-tabbar-item icon="orders-o" to="/tasks">工单</van-tabbar-item>
      <van-tabbar-item icon="chart-trending-o" to="/statistics">统计</van-tabbar-item>
      <van-tabbar-item icon="apps-o" to="/parts">备件</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { engineerOrderApi } from '@shared/api/order'
import { getPriorityText, getPriorityType, getStatusText, getStatusType, formatTime } from '@shared/utils/format'
import { showSuccessToast, showFailToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const userInfo = ref(null)
const activeTab = ref('3')
const loading = ref(true)
const finished = ref(false)
const refreshing = ref(false)
const list = ref([])
const page = ref(1)
const acceptingId = ref(null)
const stats = reactive({ pendingAccept: 0, processing: 0, pending: 0, completed: 0 })

const loadStats = async () => {
  try {
    const [res2, res3, res4, res5] = await Promise.all([
      engineerOrderApi.getMyOrders({ page: 1, size: 1, status: 2 }),
      engineerOrderApi.getMyOrders({ page: 1, size: 1, status: 3 }),
      engineerOrderApi.getMyOrders({ page: 1, size: 1, status: 4 }),
      engineerOrderApi.getMyOrders({ page: 1, size: 1, status: 5 }),
    ])
    stats.pendingAccept = res2.data.total
    stats.processing = res3.data.total
    stats.pending = res4.data.total
    stats.completed = res5.data.total
  } catch {}
}

const onLoad = async () => {
  try {
    const res = await engineerOrderApi.getMyOrders({
      page: page.value,
      size: 10,
      status: activeTab.value,
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

const switchTab = (tab) => {
  if (activeTab.value === tab) return
  activeTab.value = tab
  onTabChange()
}

const goDetail = (id) => router.push(`/order/${id}`)

const handleAccept = async (item) => {
  try {
    await showConfirmDialog({
      title: '确认接单',
      message: `确定要接取工单「${item.title}」吗？`,
      confirmButtonText: '确认接单',
      confirmButtonColor: '#1989fa',
    })
  } catch {
    return
  }

  acceptingId.value = item.id
  try {
    await engineerOrderApi.grab(item.id)
    showSuccessToast('接单成功，开始处理')
    list.value = list.value.filter(o => o.id !== item.id)
    stats.pendingAccept--
    stats.processing++
  } catch (error) {
    showFailToast(error.message || '接单失败')
    onRefresh()
  } finally {
    acceptingId.value = null
  }
}

onMounted(async () => {
  userInfo.value = await userStore.getUserInfo()
  loadStats()
  onLoad()
})
</script>

<style scoped lang="scss">
.my-tasks {
  background: #f7f8fa;
  min-height: 100vh;
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
}

.stats-mini {
  display: flex;
  padding: 12px 16px;
  gap: 8px;
}

.stat-mini-item {
  flex: 1;
  text-align: center;
  background: #fff;
  border-radius: 10px;
  padding: 10px 0;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: all 0.2s;

  &.active {
    background: #ecf5ff;
    box-shadow: 0 2px 8px rgba(25, 137, 250, 0.15);
  }
}

.mini-value {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: #1989fa;
}

.mini-label {
  font-size: 11px;
  color: #969799;
  margin-top: 2px;
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
  margin-bottom: 4px;
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
  margin-top: 8px;
  border-top: 1px solid #f5f5f5;
}

.action-button {
  min-width: 100px;
  font-weight: 600;
}
</style>
