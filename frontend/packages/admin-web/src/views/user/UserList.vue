<template>
  <div class="user-list">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增用户
          </el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="姓名" width="120" />
        <el-table-column prop="phone" label="手机号" width="150" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getRoleTagType(row.role)" size="small">
              {{ getRoleText(row.role) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-switch
                :model-value="row.status === 1"
                @change="(val) => updateStatus(row, val ? 1 : 0)"
                active-color="#13ce66"
                inline-prompt
                active-text="启用"
                inactive-text="禁用"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="170" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
            v-model:current-page="pagination.current"
            v-model:page-size="pagination.size"
            :total="pagination.total"
            layout="total, prev, pager, next"
            @current-change="fetchData"
            background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '@shared/api/user'
import { getRoleText } from '@shared/utils/format'
import { Plus } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await userApi.getList({
      page: pagination.value.current,
      size: pagination.value.size,
    })
    tableData.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

const updateStatus = async (row, status) => {
  try {
    await userApi.updateStatus(row.id, status)
    row.status = status
    ElMessage.success(status === 1 ? '已启用' : '已禁用')
  } catch {
    ElMessage.error('操作失败')
  }
}

const handleAdd = () => {
  ElMessage.info('请联系管理员通过后台添加用户')
}

const handleEdit = (row) => {
  ElMessage.info('编辑功能开发中')
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？`, '确认删除', {
      type: 'warning',
      confirmButtonText: '确定删除',
    })
    await userApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

const getRoleTagType = (role) => {
  const map = { 1: '', 2: 'success', 3: 'warning' }
  return map[role] || ''
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.user-list {
  .table-card {
    border-radius: 12px;
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 16px;
    font-weight: 600;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}
</style>
