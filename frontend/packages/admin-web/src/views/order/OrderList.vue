<template>
  <div class="order-list">
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 140px">
            <el-option label="待派单" :value="1" />
            <el-option label="待接单" :value="2" />
            <el-option label="处理中" :value="3" />
            <el-option label="待验收" :value="4" />
            <el-option label="已完成" :value="5" />
            <el-option label="已取消" :value="6" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="searchForm.priority" placeholder="全部" clearable style="width: 120px">
            <el-option label="紧急" :value="1" />
            <el-option label="高" :value="2" />
            <el-option label="中" :value="3" />
            <el-option label="低" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="工单号/标题" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe style="width: 100%">
        <el-table-column prop="orderNo" label="工单号" width="180" />
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column label="优先级" width="85" align="center">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small" effect="dark">
              {{ getPriorityText(row.priority) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="地点" width="150" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleDetail(row)">
              <el-icon><View /></el-icon>详情
            </el-button>
            <el-button
                v-if="row.status === 1"
                type="success"
                link
                @click="handleAssign(row)"
            >
              <el-icon><UserFilled /></el-icon>派单
            </el-button>
            <el-button
                v-if="row.status !== 5 && row.status !== 6"
                type="danger"
                link
                @click="handleForceClose(row)"
            >
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :page-sizes="[10, 20, 50]"
            :total="pagination.total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSearch"
            @current-change="handleSearch"
            background
        />
      </div>
    </el-card>

    <el-dialog v-model="assignDialogVisible" title="指派工程师" width="440px" :close-on-click-modal="false">
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="当前工单">
          <span class="dialog-order-title">{{ currentOrder?.title }}</span>
        </el-form-item>
        <el-form-item label="选择工程师">
          <el-select v-model="assignForm.engineerId" placeholder="请选择工程师" style="width: 100%">
            <el-option
                v-for="item in engineers"
                :key="item.id"
                :label="`${item.realName} (${item.phone})`"
                :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAssign" :loading="assignLoading">
          确认派单
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminOrderApi } from '@shared/api/order'
import { getPriorityText, getPriorityType, getStatusText, getStatusType } from '@shared/utils/format'
import { Search, View, UserFilled } from '@element-plus/icons-vue'

const router = useRouter()
const loading = ref(false)
const tableData = ref([])
const engineers = ref([])
const assignDialogVisible = ref(false)
const assignLoading = ref(false)
const currentOrder = ref(null)

const searchForm = reactive({
  status: null,
  priority: null,
  keyword: '',
})

const assignForm = reactive({ engineerId: null })

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
})

const handleSearch = async () => {
  loading.value = true
  try {
    const res = await adminOrderApi.getList({
      page: pagination.current,
      size: pagination.size,
      ...searchForm,
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchForm.status = null
  searchForm.priority = null
  searchForm.keyword = ''
  pagination.current = 1
  handleSearch()
}

const handleDetail = (row) => {
  router.push(`/order/detail/${row.id}`)
}

const handleAssign = async (row) => {
  currentOrder.value = row
  assignDialogVisible.value = true
  if (engineers.value.length === 0) {
    const res = await adminOrderApi.getEngineers()
    engineers.value = res.data
  }
}

const submitAssign = async () => {
  if (!assignForm.engineerId || !currentOrder.value) {
    ElMessage.warning('请选择工程师')
    return
  }
  assignLoading.value = true
  try {
    await adminOrderApi.assign(currentOrder.value.id, assignForm.engineerId)
    ElMessage.success('派单成功')
    assignDialogVisible.value = false
    assignForm.engineerId = null
    handleSearch()
  } finally {
    assignLoading.value = false
  }
}

const handleForceClose = async (row) => {
  try {
    const { value } = await ElMessageBox.prompt('请输入关闭原因', '强制关闭', {
      confirmButtonText: '确认关闭',
      cancelButtonText: '取消',
      type: 'warning',
      inputType: 'textarea',
    })
    await adminOrderApi.forceClose(row.id, value)
    ElMessage.success('工单已关闭')
    tableData.value = tableData.value.filter(item => item.id !== row.id)
    pagination.total--
  } catch {}
}

onMounted(handleSearch)
</script>

<style scoped lang="scss">
.order-list {
  .search-card {
    margin-bottom: 16px;
    border-radius: 12px;
  }

  .table-card {
    border-radius: 12px;
  }

  .dialog-order-title {
    font-weight: 600;
    color: #303133;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
