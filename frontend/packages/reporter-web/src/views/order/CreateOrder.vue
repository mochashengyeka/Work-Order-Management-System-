<template>
  <div class="create-order">
    <van-nav-bar title="创建工单" left-arrow @click-left="router.back()" fixed placeholder />

    <van-form @submit="onSubmit" ref="formRef">
      <van-cell-group inset title="工单信息">
        <van-field
          v-model="form.title"
          name="title"
          label="工单标题"
          placeholder="请输入工单标题"
          :rules="[{ required: true, message: '请输入工单标题' }]"
        />
        <van-field
          v-model="form.priorityText"
          is-link
          readonly
          name="priority"
          label="优先级"
          placeholder="请选择优先级"
          @click="showPriorityPicker = true"
          :rules="[{ required: true, message: '请选择优先级' }]"
        />
        <van-field
          v-model="form.location"
          name="location"
          label="报修地点"
          placeholder="请输入具体位置"
          :rules="[{ required: true, message: '请输入报修地点' }]"
        />
        <van-field
          v-model="form.deviceInfo"
          name="deviceInfo"
          label="设备信息"
          placeholder="请输入设备型号/资产编号"
        />
        <van-field
          v-model="form.description"
          name="description"
          label="故障描述"
          type="textarea"
          rows="4"
          autosize
          placeholder="请详细描述故障现象"
          :rules="[{ required: true, message: '请输入故障描述' }]"
        />
        <van-field name="images" label="上传图片">
          <template #input>
            <Uploader v-model="fileList" />
          </template>
        </van-field>
      </van-cell-group>

      <div class="submit-wrapper">
        <van-button round block type="primary" native-type="submit" :loading="loading" size="large">
          提交工单
        </van-button>
      </div>
    </van-form>

    <van-popup v-model:show="showPriorityPicker" round position="bottom">
      <van-picker
        :columns="priorityOptions"
        @confirm="onPriorityConfirm"
        @cancel="showPriorityPicker = false"
        title="选择优先级"
      />
    </van-popup>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { reporterOrderApi } from '@shared/api/order'
import Uploader from '@/components/Uploader.vue'

const router = useRouter()
const loading = ref(false)
const showPriorityPicker = ref(false)
const fileList = ref([])
const formRef = ref()

const priorityOptions = [
  { text: '紧急 (2小时内处理)', value: 1 },
  { text: '高 (4小时内处理)', value: 2 },
  { text: '中 (8小时内处理)', value: 3 },
  { text: '低 (24小时内处理)', value: 4 },
]

const form = reactive({
  title: '',
  priorityText: '',
  priority: 3,
  location: '',
  deviceInfo: '',
  description: '',
})

const onPriorityConfirm = ({ selectedOptions }) => {
  form.priorityText = selectedOptions[0]?.text || ''
  form.priority = selectedOptions[0]?.value || 3
  showPriorityPicker.value = false
}

const onSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const data = {
      title: form.title,
      description: form.description,
      priority: form.priority,
      location: form.location,
      deviceInfo: form.deviceInfo,
      images: JSON.stringify(fileList.value.map(f => f.url)),
    }
    await reporterOrderApi.create(data)
    showSuccessToast('工单创建成功')
    router.replace('/order/my')
  } catch (error) {
    showToast(error.message || '创建失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.create-order {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}

.submit-wrapper {
  margin: 24px 16px;
}
</style>
