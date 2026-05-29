<template>
  <div class="order-detail">
    <van-nav-bar title="工单详情" left-arrow @click-left="router.back()" fixed placeholder />
    <van-loading v-if="loading" type="spinner" class="loading" />

    <template v-else>
      <van-cell-group inset title="基本信息">
        <van-cell title="工单编号" :value="order.orderNo" />
        <van-cell title="状态">
          <template #value>
            <van-tag :type="getStatusType(order.status)">{{ getStatusText(order.status) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="优先级">
          <template #value>
            <van-tag :type="getPriorityType(order.priority)">{{ getPriorityText(order.priority) }}</van-tag>
          </template>
        </van-cell>
        <van-cell title="报修地点" :value="order.location" />
        <van-cell title="设备信息" :value="order.deviceInfo || '-'" />
        <van-cell title="创建时间" :value="formatTime(order.createTime)" />
        <van-cell title="故障描述" :value="order.description" :label="true" />
      </van-cell-group>

      <van-cell-group inset v-if="order.engineerName" title="维修信息">
        <van-cell title="维修工程师" :value="order.engineerName" />
        <van-cell title="接单时间" :value="formatTime(order.acceptTime)" />
        <van-cell title="诊断结果" :value="order.diagnosis || '-'" :label="true" />
        <van-cell title="维修措施" :value="order.repairAction || '-'" :label="true" />
        <van-cell title="实际费用">
          <template #value>
            <span class="cost">¥{{ order.actualCost || 0 }}</span>
          </template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset title="操作记录">
        <van-cell v-for="log in logs" :key="log.id">
          <template #title>
            <span class="log-remark">{{ log.remark }}</span>
          </template>
          <template #label>
            <span class="log-time">{{ formatTime(log.createTime) }}</span>
          </template>
        </van-cell>
        <van-cell v-if="logs.length === 0" title="暂无操作记录" />
      </van-cell-group>

      <div class="action-bar" v-if="canOperate">
        <van-button
          v-if="order.status === 4"
          type="primary"
          round
          block
          @click="handleConfirm"
          size="large"
        >
          确认验收
        </van-button>
        <van-button
          v-if="order.status < 4"
          type="danger"
          round
          block
          @click="handleCancel"
        >
          取消工单
        </van-button>
        <van-button
          v-if="order.status === 3"
          type="warning"
          round
          block
          @click="handleUrge"
          class="mt-8"
        >
          催单
        </van-button>
        <van-button
          v-if="order.status === 5 && !feedback"
          type="primary"
          round
          block
          @click="showFeedback = true"
          class="mt-8"
        >
          评价工单
        </van-button>
      </div>
    </template>

    <van-popup v-model:show="showFeedback" round position="bottom" :close-on-click-overlay="false">
      <div class="feedback-popup">
        <h3>评价工单</h3>
        <div class="rating-wrapper">
          <van-rate v-model="feedbackForm.rating" :size="32" color="#ff976a" void-color="#eee" />
          <span class="rating-text">{{ ratingTexts[feedbackForm.rating] }}</span>
        </div>
        <van-field
          v-model="feedbackForm.content"
          type="textarea"
          rows="3"
          placeholder="请输入评价内容（选填）"
          autosize
        />
        <div class="feedback-actions">
          <van-button @click="showFeedback = false" round>取消</van-button>
          <van-button type="primary" @click="submitFeedback" :loading="feedbackLoading" round>
            提交评价
          </van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { reporterOrderApi } from '@shared/api/order'
import { getPriorityText, getPriorityType, getStatusText, getStatusType, formatTime } from '@shared/utils/format'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const order = ref({})
const logs = ref([])
const feedback = ref(null)
const showFeedback = ref(false)
const feedbackLoading = ref(false)
const feedbackForm = ref({ rating: 5, content: '' })

const ratingTexts = { 1: '非常差', 2: '差', 3: '一般', 4: '好', 5: '非常好' }

const canOperate = computed(() => {
  const s = order.value.status
  return s === 3 || s === 4 || (s === 5 && !feedback.value)
})

const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await reporterOrderApi.getDetail(Number(id))
    order.value = res.data.order
    logs.value = res.data.logs || []
    feedback.value = res.data.feedback
  } finally {
    loading.value = false
  }
}

const handleConfirm = async () => {
  try {
    await showConfirmDialog({ title: '确认验收', message: '请确认工单已维修完成？' })
    await reporterOrderApi.confirm(order.value.id)
    showSuccessToast('验收成功')
    loadDetail()
  } catch {}
}

const handleCancel = async () => {
  try {
    await showConfirmDialog({ title: '取消工单', message: '确定要取消该工单吗？' })
    await reporterOrderApi.cancel(order.value.id, '用户主动取消')
    showSuccessToast('工单已取消')
    loadDetail()
  } catch {}
}

const handleUrge = async () => {
  try {
    await showConfirmDialog({
      title: '催单确认',
      message: '确定要催促工程师尽快处理吗？',
      confirmButtonText: '确认催单',
      confirmButtonColor: '#ff976a',
    })
    await reporterOrderApi.urge(order.value.id)
    showSuccessToast('已催单，请耐心等待')
  } catch {}
}

const submitFeedback = async () => {
  if (feedbackForm.value.rating === 0) {
    showToast('请打分')
    return
  }
  feedbackLoading.value = true
  try {
    await reporterOrderApi.feedback(order.value.id, feedbackForm.value)
    showSuccessToast('评价成功')
    showFeedback.value = false
    loadDetail()
  } finally {
    feedbackLoading.value = false
  }
}

onMounted(loadDetail)
</script>

<style scoped lang="scss">
.order-detail {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 100px;
}

.loading {
  display: flex;
  justify-content: center;
  margin-top: 80px;
}

.cost {
  font-size: 18px;
  font-weight: 700;
  color: #ee0a24;
}

.log-remark {
  font-size: 14px;
  color: #323233;
}

.log-time {
  font-size: 12px;
  color: #969799;
}

.mt-8 {
  margin-top: 8px;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
}

.feedback-popup {
  padding: 24px 20px;
  padding-bottom: calc(24px + env(safe-area-inset-bottom));

  h3 {
    text-align: center;
    font-size: 18px;
    margin-bottom: 20px;
  }

  .rating-wrapper {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    margin-bottom: 20px;
  }

  .rating-text {
    font-size: 14px;
    color: #ff976a;
    font-weight: 500;
  }

  .feedback-actions {
    display: flex;
    gap: 12px;
    margin-top: 20px;

    .van-button { flex: 1; }
  }
}
</style>
