<template>
  <div class="order-detail">
    <el-card v-loading="loading" class="detail-card">
      <template #header>
        <div class="card-header">
          <span>工单详情 - {{ order?.orderNo }}</span>
          <el-button @click="router.back()" :icon="ArrowLeft">返回</el-button>
        </div>
      </template>

      <el-descriptions :column="2" border size="large">
        <el-descriptions-item label="工单标题" :span="2">
          <strong>{{ order?.title }}</strong>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(order?.status)" size="default">
            {{ getStatusText(order?.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="优先级">
          <el-tag :type="getPriorityType(order?.priority)" size="default" effect="dark">
            {{ getPriorityText(order?.priority) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="报修人">{{ order?.reporterName }}</el-descriptions-item>
        <el-descriptions-item label="工程师">{{ order?.engineerName || '未指派' }}</el-descriptions-item>
        <el-descriptions-item label="报修地点">{{ order?.location }}</el-descriptions-item>
        <el-descriptions-item label="设备信息">{{ order?.deviceInfo || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ order?.createTime }}</el-descriptions-item>
        <el-descriptions-item label="SLA截止">{{ order?.slaDeadline || '-' }}</el-descriptions-item>
        <el-descriptions-item label="故障描述" :span="2">
          <div class="desc-text">{{ order?.description }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="诊断结果" :span="2">
          {{ order?.diagnosis || '暂无诊断' }}
        </el-descriptions-item>
        <el-descriptions-item label="维修措施" :span="2">
          {{ order?.repairAction || '暂无' }}
        </el-descriptions-item>
        <el-descriptions-item label="实际费用">
          <span class="cost">¥{{ order?.actualCost || 0 }}</span>
        </el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">
        <el-icon><Clock /></el-icon> 操作日志
      </el-divider>

      <el-timeline>
        <el-timeline-item
            v-for="log in logs"
            :key="log.id"
            :timestamp="log.createTime"
            placement="top"
            :type="log.actionType === 7 ? 'danger' : 'primary'"
        >
          <div class="log-item">
            <span class="log-operator">{{ log.operatorName }}</span>
            <span class="log-remark">{{ log.remark }}</span>
          </div>
        </el-timeline-item>
        <el-empty v-if="logs.length === 0" description="暂无操作记录" :image-size="60" />
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import request from '@shared/api/request'
import { getPriorityText, getPriorityType, getStatusText, getStatusType } from '@shared/utils/format'
import { ArrowLeft, Clock } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const order = ref(null)
const logs = ref([])

const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const [orderRes, logsRes] = await Promise.all([
      request.get(`/admin/order/detail/${id}`),
      request.get(`/admin/order/logs/${id}`),
    ])
    order.value = orderRes.data
    logs.value = logsRes.data
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped lang="scss">
.order-detail {
  .detail-card {
    border-radius: 12px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
  }

  .desc-text {
    white-space: pre-wrap;
    line-height: 1.6;
  }

  .cost {
    font-size: 18px;
    font-weight: 700;
    color: #f56c6c;
  }

  .log-item {
    .log-operator {
      font-weight: 600;
      color: #409EFF;
      margin-right: 8px;
    }
    .log-remark {
      color: #606266;
    }
  }
}
</style>
