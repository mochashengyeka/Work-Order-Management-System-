<template>
  <div class="part-list">
    <el-card class="table-card">
      <template #header>
        <div class="card-header">
          <span>备件库存</span>
          <el-button type="primary" @click="handleAdd">
            <el-icon><Plus /></el-icon>新增备件
          </el-button>
        </div>
      </template>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="partCode" label="编码" width="130" />
        <el-table-column prop="partName" label="名称" min-width="150" />
        <el-table-column prop="category" label="分类" width="120" />
        <el-table-column prop="specification" label="规格" width="150" />
        <el-table-column label="库存" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.stockQuantity <= row.minStock ? 'danger' : 'success'" effect="dark" round>
              {{ row.stockQuantity }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="单价(元)" width="100" align="right">
          <template #default="{ row }">
            <span class="price">¥{{ row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>编辑
            </el-button>
            <el-button type="success" link @click="handleStock(row)">
              <el-icon><Plus /></el-icon>入库
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">
              <el-icon><Delete /></el-icon>
            </el-button>
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

    <el-dialog
        v-model="dialogVisible"
        :title="dialogTitle"
        width="520px"
        @closed="resetForm"
        :close-on-click-modal="false"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="90px">
        <el-form-item label="备件编码" prop="partCode">
          <el-input v-model="form.partCode" :disabled="isEdit" placeholder="请输入备件编码" />
        </el-form-item>
        <el-form-item label="备件名称" prop="partName">
          <el-input v-model="form.partName" placeholder="请输入备件名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类" prop="category">
              <el-input v-model="form.category" placeholder="如：电子元件" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="规格" prop="specification">
              <el-input v-model="form.specification" placeholder="规格型号" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="单价" prop="price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最小库存" prop="minStock">
              <el-input-number v-model="form.minStock" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="stockDialogVisible" title="库存入库" width="340px">
      <el-form :model="stockForm" label-width="80px">
        <el-form-item label="当前库存">
          <span class="current-stock">{{ currentPart?.stockQuantity }}</span>
        </el-form-item>
        <el-form-item label="入库数量">
          <el-input-number v-model="stockForm.quantity" :min="1" :max="9999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="入库后库存">
          <span class="after-stock">{{ (currentPart?.stockQuantity || 0) + stockForm.quantity }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="stockDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitStock">确定入库</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@shared/api/request'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const pagination = ref({ current: 1, size: 10, total: 0 })

const dialogVisible = ref(false)
const stockDialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const formRef = ref()
const currentPart = ref(null)

const form = reactive({
  partCode: '',
  partName: '',
  category: '',
  specification: '',
  price: 0,
  minStock: 10,
})

const stockForm = reactive({ quantity: 1 })

const rules = {
  partCode: [{ required: true, message: '请输入编码', trigger: 'blur' }],
  partName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }],
}

const dialogTitle = ref('新增备件')

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/admin/part/list', {
      params: { page: pagination.value.current, size: pagination.value.size },
    })
    tableData.value = res.data.records
    pagination.value.total = res.data.total
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    partCode: '', partName: '', category: '', specification: '', price: 0, minStock: 10,
  })
  isEdit.value = false
}

const handleAdd = () => {
  resetForm()
  dialogTitle.value = '新增备件'
  dialogVisible.value = true
}

const handleEdit = (row) => {
  Object.assign(form, row)
  isEdit.value = true
  dialogTitle.value = '编辑备件'
  dialogVisible.value = true
}

const handleStock = (row) => {
  currentPart.value = row
  stockForm.quantity = 1
  stockDialogVisible.value = true
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定要删除备件 "${row.partName}" 吗？`, '确认删除', { type: 'warning' })
    await request.delete(`/admin/part/delete/${row.id}`)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

const submitForm = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }
  submitLoading.value = true
  try {
    if (isEdit.value) {
      await request.put('/admin/part/update', form)
      ElMessage.success('更新成功')
    } else {
      await request.post('/admin/part/add', form)
      ElMessage.success('添加成功')
    }
    dialogVisible.value = false
    fetchData()
  } finally {
    submitLoading.value = false
  }
}

const submitStock = async () => {
  if (!currentPart.value) return
  const newStock = currentPart.value.stockQuantity + stockForm.quantity
  await request.put(`/admin/part/stock/${currentPart.value.id}`, null, {
    params: { quantity: newStock },
  })
  ElMessage.success('入库成功')
  stockDialogVisible.value = false
  fetchData()
}

onMounted(fetchData)
</script>

<style scoped lang="scss">
.part-list {
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

  .price {
    font-weight: 600;
    color: #f56c6c;
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }

  .current-stock {
    font-size: 18px;
    font-weight: 600;
    color: #303133;
  }

  .after-stock {
    font-size: 18px;
    font-weight: 700;
    color: #67C23A;
  }
}
</style>
