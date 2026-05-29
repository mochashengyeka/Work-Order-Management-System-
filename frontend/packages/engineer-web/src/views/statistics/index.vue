<template>
  <div class="statistics-page">
    <van-nav-bar title="我的统计" fixed placeholder />

    <van-loading v-if="loading" type="spinner" class="loading" />

    <template v-else>
      <div class="stat-grid">
        <div class="stat-card">
          <van-icon name="orders-o" size="24" color="#1989fa" />
          <span class="stat-val">{{ stats.todayOrders || 0 }}</span>
          <span class="stat-label">今日接单</span>
        </div>
        <div class="stat-card">
          <van-icon name="chart-trending-o" size="24" color="#07c160" />
          <span class="stat-val">{{ stats.monthOrders || 0 }}</span>
          <span class="stat-label">本月接单</span>
        </div>
        <div class="stat-card">
          <van-icon name="checked" size="24" color="#ff976a" />
          <span class="stat-val">{{ stats.totalCompleted || 0 }}</span>
          <span class="stat-label">累计完成</span>
        </div>
        <div class="stat-card">
          <van-icon name="star-o" size="24" color="#ee0a24" />
          <span class="stat-val">{{ stats.avgRating || 0 }}<small>分</small></span>
          <span class="stat-label">平均评分</span>
        </div>
      </div>

      <van-cell-group inset title="收入概况">
        <van-cell title="本月收入" :value="'¥' + (stats.monthIncome || 0)" />
        <van-cell title="累计收入" :value="'¥' + (stats.totalIncome || 0)" />
      </van-cell-group>
    </template>

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
import request from '@shared/api/request'

const activeTab = ref(2)
const loading = ref(true)
const stats = reactive({
  todayOrders: 0,
  monthOrders: 0,
  totalCompleted: 0,
  avgRating: 0,
  monthIncome: 0,
  totalIncome: 0,
})

const loadData = async () => {
  try {
    const res = await request.get('/engineer/statistics')
    Object.assign(stats, res.data)
  } catch {} finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.statistics-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 60px;
}

.loading {
  display: flex;
  justify-content: center;
  margin-top: 120px;
}

.stat-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  padding: 16px;
}

.stat-card {
  background: #fff;
  border-radius: 12px;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);

  .stat-val {
    font-size: 26px;
    font-weight: 700;
    color: #323233;

    small { font-size: 14px; font-weight: 400; }
  }

  .stat-label {
    font-size: 13px;
    color: #969799;
  }
}
</style>
