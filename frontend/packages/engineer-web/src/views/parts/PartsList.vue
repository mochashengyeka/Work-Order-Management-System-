<template>
  <div class="parts-page">
    <van-nav-bar title="备件库存" fixed placeholder />
    <van-search v-model="keyword" placeholder="搜索备件名称/编码" @search="onSearch" @clear="onSearch" />

    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <van-cell v-for="item in list" :key="item.id" class="part-cell">
          <template #title>
            <div class="part-header">
              <span class="part-name">{{ item.partName }}</span>
              <van-tag type="primary" size="mini">{{ item.partCode }}</van-tag>
            </div>
          </template>
          <template #label>
            <div class="part-detail">
              <span>{{ item.specification || '-' }}</span>
              <span>单价: ¥{{ item.price }}</span>
            </div>
          </template>
          <template #value>
            <div class="stock-info">
              <span :class="['stock-num', { low: item.stockQuantity <= item.minStock }]">
                {{ item.stockQuantity }}
              </span>
              <span class="stock-unit">个</span>
            </div>
          </template>
        </van-cell>
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
import { ref } from 'vue'
import { engineerOrderApi } from '@shared/api/order'

const activeTab = ref(3)
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const list = ref([])
const page = ref(1)
const keyword = ref('')

const onLoad = async () => {
  try {
    const res = await engineerOrderApi.getParts({
      page: page.value,
      size: 20,
      keyword: keyword.value || undefined,
    })
    const data = res.data
    if (page.value === 1) list.value = data.records
    else list.value.push(...data.records)
    finished.value = list.value.length >= data.total
    if (data.total > 0) page.value++
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
  onLoad()
}

const onSearch = () => {
  page.value = 1
  finished.value = false
  list.value = []
  onLoad()
}

onLoad()
</script>

<style scoped lang="scss">
.parts-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 60px;
}

.part-cell {
  margin: 0 12px 8px;
  border-radius: 10px !important;
  background: #fff;
}

.part-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;

  .part-name {
    font-size: 16px;
    font-weight: 500;
  }
}

.part-detail {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: #969799;
}

.stock-info {
  text-align: right;

  .stock-num {
    font-size: 22px;
    font-weight: 700;
    color: #07c160;

    &.low {
      color: #ee0a24;
    }
  }

  .stock-unit {
    font-size: 13px;
    color: #969799;
    margin-left: 2px;
  }
}
</style>
