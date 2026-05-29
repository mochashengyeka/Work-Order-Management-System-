<template>
  <div class="order-detail">
    <van-nav-bar title="工单处理" left-arrow @click-left="router.back()" fixed placeholder />
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
        <van-cell title="报修人" :value="order.reporterName" />
        <van-cell title="报修地点" :value="order.location" />
        <van-cell title="设备信息" :value="order.deviceInfo || '-'" />
        <van-cell title="创建时间" :value="formatTime(order.createTime)" />
        <van-cell title="故障描述" :value="order.description" :label="true" />
      </van-cell-group>

      <van-cell-group inset v-if="order.status === 3" title="维修处理">
        <van-field
          v-model="form.diagnosis"
          label="诊断结果"
          type="textarea"
          rows="3"
          placeholder="请输入诊断结果"
          autosize
        />
        <van-field
          v-model="form.repairAction"
          label="维修措施"
          type="textarea"
          rows="3"
          placeholder="请输入维修措施"
          autosize
        />
        <van-field name="images" label="维修照片">
          <template #input>
            <Uploader v-model="fileList" />
          </template>
        </van-field>
        <van-cell title="使用备件" is-link @click="showPartsSelector = true">
          <template #value>
            <span v-if="selectedParts.length" class="parts-count">
              {{ selectedParts.length }} 种备件
            </span>
            <span v-else class="no-parts">未选择</span>
          </template>
        </van-cell>
      </van-cell-group>

      <van-cell-group inset v-if="order.status >= 4" title="维修记录">
        <van-cell title="诊断结果" :value="order.diagnosis" :label="true" />
        <van-cell title="维修措施" :value="order.repairAction" :label="true" />
        <van-cell title="实际费用">
          <template #value>
            <span class="cost">¥{{ order.actualCost || 0 }}</span>
          </template>
        </van-cell>
      </van-cell-group>

      <div class="action-bar" v-if="order.status === 3">
        <van-button type="primary" round block @click="handleFinish" :loading="finishing">
          完成维修
        </van-button>
      </div>
    </template>

    <van-popup v-model:show="showPartsSelector" round position="bottom" :style="{ height: '65%' }">
      <div class="parts-selector">
        <div class="parts-header">
          <h3>选择备件</h3>
          <van-icon name="cross" size="20" @click="showPartsSelector = false" />
        </div>
        <van-list v-model:loading="partsLoading" :finished="partsFinished" @load="loadParts">
          <van-cell v-for="part in partsList" :key="part.id" @click="togglePart(part)">
            <template #title>
              <div class="part-name">
                <span>{{ part.partName }}</span>
                <van-tag type="primary" size="mini">{{ part.partCode }}</van-tag>
              </div>
            </template>
            <template #label>
              <span>库存: {{ part.stockQuantity }} | 单价: ¥{{ part.price }}</span>
            </template>
            <template #right-icon>
              <van-stepper
                v-model="partQuantities[part.id]"
                :min="0"
                :max="part.stockQuantity"
                @click.stop
                @change="(val) => { if (!val) delete partQuantities[part.id] }"
              />
            </template>
          </van-cell>
        </van-list>
        <div class="parts-footer">
          <van-button @click="showPartsSelector = false">取消</van-button>
          <van-button type="primary" @click="confirmParts">确定 ({{ selectedParts.length }})</van-button>
        </div>
      </div>
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { engineerOrderApi } from '@shared/api/order'
import request from '@shared/api/request'
import { getPriorityText, getPriorityType, getStatusText, getStatusType, formatTime } from '@shared/utils/format'
import { showToast, showSuccessToast, showConfirmDialog } from 'vant'
import Uploader from '@/components/Uploader.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const finishing = ref(false)
const order = ref({})
const fileList = ref([])
const form = reactive({ diagnosis: '', repairAction: '' })

const showPartsSelector = ref(false)
const partsList = ref([])
const partsLoading = ref(false)
const partsFinished = ref(false)
const partsPage = ref(1)
const partQuantities = ref({})

const selectedParts = computed(() => {
  return Object.entries(partQuantities.value)
    .filter(([_, qty]) => qty > 0)
    .map(([id, qty]) => ({ partId: Number(id), quantity: qty }))
})

const loadDetail = async () => {
  loading.value = true
  try {
    const id = route.params.id
    const res = await request.get(`/engineer/order/detail/${id}`)
    order.value = res.data
  } finally {
    loading.value = false
  }
}

const handleFinish = async () => {
  if (!form.diagnosis || !form.repairAction) {
    showToast('请填写诊断结果和维修措施')
    return
  }
  try {
    await showConfirmDialog({
      title: '确认完成',
      message: '确定维修已完成？',
    })
    finishing.value = true
    const data = {
      diagnosis: form.diagnosis,
      repairAction: form.repairAction,
      repairImages: JSON.stringify(fileList.value.map(f => f.url)),
      partsUsed: selectedParts.value,
    }
    await engineerOrderApi.finish(order.value.id, data)
    showSuccessToast('提交成功')
    router.replace('/tasks')
  } catch {} finally {
    finishing.value = false
  }
}

const loadParts = async () => {
  partsLoading.value = true
  try {
    const res = await engineerOrderApi.getParts({ page: partsPage.value, size: 20 })
    const data = res.data
    if (partsPage.value === 1) partsList.value = data.records
    else partsList.value.push(...data.records)
    partsFinished.value = partsList.value.length >= data.total
    if (data.total > 0) partsPage.value++
  } finally {
    partsLoading.value = false
  }
}

const togglePart = (part) => {
  if (!partQuantities.value[part.id]) {
    partQuantities.value[part.id] = 1
  }
}

const confirmParts = () => {
  showPartsSelector.value = false
}

onMounted(loadDetail)
</script>

<style scoped lang="scss">
.order-detail {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;
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

.parts-count {
  color: #1989fa;
  font-weight: 500;
}

.no-parts {
  color: #969799;
}

.action-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: #fff;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.06);
}

.parts-selector {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.parts-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #ebedf0;

  h3 { font-size: 17px; }
}

.parts-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid #ebedf0;

  .van-button { flex: 1; }
}

.part-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}
</style>
