<template>
  <van-uploader
    :model-value="modelValue"
    :max-count="6"
    :max-size="10 * 1024 * 1024"
    @update:model-value="$emit('update:modelValue', $event)"
    :after-read="afterRead"
    :before-delete="beforeDelete"
    accept="image/*"
    preview-size="80"
  />
</template>

<script setup>
import { showFailToast } from 'vant'
import request from '@shared/api/request'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => [],
  },
})

const emit = defineEmits(['update:modelValue'])

const afterRead = async (file) => {
  const formData = new FormData()
  formData.append('file', file.file)
  try {
    const res = await request.post('/common/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
    file.url = res.data
    file.status = 'done'
    const newList = [...props.modelValue, file]
    emit('update:modelValue', newList)
  } catch (error) {
    showFailToast(error.message || '上传失败')
    file.status = 'failed'
  }
}

const beforeDelete = ({ index }) => {
  const newList = props.modelValue.filter((_, i) => i !== index)
  emit('update:modelValue', newList)
  return true
}
</script>
