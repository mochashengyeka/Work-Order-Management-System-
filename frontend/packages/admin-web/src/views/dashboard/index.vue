<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statsCards" :key="item.title">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-icon" :style="{ background: item.gradient }">
              <el-icon :size="28"><component :is="item.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-title">{{ item.title }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="14">
        <el-card class="chart-card">
          <template #header>
            <div class="card-title">
              <span>工单状态分布</span>
            </div>
          </template>
          <div ref="statusChartRef" style="height: 340px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card class="overview-card">
          <template #header>
            <div class="card-title">
              <span>今日概况</span>
            </div>
          </template>
          <div class="today-stats">
            <div class="today-item">
              <div class="today-label">
                <el-icon color="#409EFF"><Plus /></el-icon>
                <span>今日新增</span>
              </div>
              <span class="today-value blue">{{ dashboardData.todayNew || 0 }}</span>
            </div>
            <div class="today-item">
              <div class="today-label">
                <el-icon color="#E6A23C"><Clock /></el-icon>
                <span>超时工单</span>
              </div>
              <span class="today-value warning">{{ dashboardData.overdue || 0 }}</span>
            </div>
            <div class="today-item">
              <div class="today-label">
                <el-icon color="#67C23A"><CircleCheck /></el-icon>
                <span>今日完成</span>
              </div>
              <span class="today-value success">{{ dashboardData.todayCompleted || 0 }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { adminOrderApi } from '@shared/api/order'
import { Document, Clock, Check, Tools, Plus, CircleCheck } from '@element-plus/icons-vue'

const statsCards = ref([
  { title: '总工单', value: 0, icon: Document, gradient: 'linear-gradient(135deg, #409EFF, #36a3f7)' },
  { title: '待处理', value: 0, icon: Clock, gradient: 'linear-gradient(135deg, #E6A23C, #f7a433)' },
  { title: '处理中', value: 0, icon: Tools, gradient: 'linear-gradient(135deg, #67C23A, #85ce61)' },
  { title: '已完成', value: 0, icon: Check, gradient: 'linear-gradient(135deg, #909399, #b4b6bb)' },
])

const dashboardData = ref({})
const statusChartRef = ref()

const loadData = async () => {
  const res = await adminOrderApi.getDashboard()
  const data = res.data
  dashboardData.value = data

  statsCards.value[0].value = data.totalOrders || 0
  statsCards.value[1].value = (data.pendingAssign || 0) + (data.pendingAccept || 0)
  statsCards.value[2].value = data.processing || 0
  statsCards.value[3].value = data.completed || 0

  if (statusChartRef.value) {
    const chart = echarts.init(statusChartRef.value)
    chart.setOption({
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)',
      },
      legend: {
        bottom: 0,
      },
      series: [
        {
          type: 'pie',
          radius: ['50%', '75%'],
          center: ['50%', '45%'],
          avoidLabelOverlap: false,
          itemStyle: {
            borderRadius: 6,
            borderColor: '#fff',
            borderWidth: 3,
          },
          label: {
            show: true,
            formatter: '{b}\n{d}%',
          },
          color: ['#409EFF', '#E6A23C', '#67C23A', '#F56C6C', '#909399'],
          data: [
            { value: data.pendingAssign || 0, name: '待派单' },
            { value: data.pendingAccept || 0, name: '待接单' },
            { value: data.processing || 0, name: '处理中' },
            { value: data.pendingConfirm || 0, name: '待验收' },
            { value: data.completed || 0, name: '已完成' },
          ],
        },
      ],
    })
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.dashboard {
  .stat-card {
    border-radius: 12px;
    transition: transform 0.3s;

    &:hover {
      transform: translateY(-4px);
    }

    .stat-content {
      display: flex;
      align-items: center;
    }

    .stat-icon {
      width: 56px;
      height: 56px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      margin-right: 16px;
    }

    .stat-value {
      font-size: 30px;
      font-weight: 700;
      color: #1a1a2e;
    }

    .stat-title {
      font-size: 13px;
      color: #909399;
      margin-top: 4px;
    }
  }

  .chart-card, .overview-card {
    border-radius: 12px;
  }

  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;

    &::before {
      content: '';
      display: inline-block;
      width: 4px;
      height: 16px;
      background: #409EFF;
      border-radius: 2px;
      margin-right: 10px;
      vertical-align: -2px;
    }
  }

  .today-stats {
    .today-item {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 20px 0;
      border-bottom: 1px solid #f0f0f0;

      &:last-child {
        border-bottom: none;
      }
    }

    .today-label {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 14px;
      color: #606266;
    }

    .today-value {
      font-size: 28px;
      font-weight: 700;

      &.blue { color: #409EFF; }
      &.warning { color: #E6A23C; }
      &.success { color: #67C23A; }
    }
  }
}
</style>
