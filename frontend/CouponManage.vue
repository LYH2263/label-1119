<template>
  <div class="coupon-manage">
    <el-card class="header-card">
      <template #header>
        <div class="card-header">
          <span>优惠券管理</span>
          <el-button type="primary" @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            新建优惠券
          </el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" class="query-form">
        <el-form-item label="优惠券名称">
          <el-input v-model="queryParams.name" placeholder="请输入名称" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.type" placeholder="请选择类型" clearable>
            <el-option label="满减券" value="FULL_REDUCTION" />
            <el-option label="折扣券" value="DISCOUNT" />
            <el-option label="无门槛券" value="NO_THRESHOLD" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="getCouponList">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="优惠券名称" min-width="150" />
        <el-table-column label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTagType(row.type)">
              {{ getTypeName(row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="面额/折扣" width="120">
          <template #default="{ row }">
            <span v-if="row.type === 'DISCOUNT'">
              {{ (row.value * 10).toFixed(0) }}折
            </span>
            <span v-else>¥{{ row.value }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="minAmount" label="使用门槛" width="100">
          <template #default="{ row }">
            <span>满¥{{ row.minAmount }}</span>
          </template>
        </el-table-column>
        <el-table-column label="发放情况" width="180">
          <template #default="{ row }">
            <span>{{ row.receivedCount }}/{{ row.totalCount }}</span>
            <el-progress
              :percentage="Math.round((row.receivedCount / row.totalCount) * 100)"
              :stroke-width="8"
              style="margin-top: 5px;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="usedCount" label="已使用" width="80" />
        <el-table-column label="有效期" width="280">
          <template #default="{ row }">
            <div>{{ formatDate(row.startTime) }}</div>
            <div>至 {{ formatDate(row.endTime) }}</div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              active-text="启用"
              inactive-text="禁用"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="viewStats(row)">
              <el-icon><DataLine /></el-icon>
              统计
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="getCouponList"
        @current-change="getCouponList"
        class="pagination"
      />
    </el-card>

    <el-dialog
      v-model="createDialogVisible"
      title="新建优惠券"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="120px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="createForm.name" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="createForm.type" placeholder="请选择类型" @change="onTypeChange">
            <el-option label="满减券" value="FULL_REDUCTION" />
            <el-option label="折扣券" value="DISCOUNT" />
            <el-option label="无门槛券" value="NO_THRESHOLD" />
          </el-select>
        </el-form-item>
        <el-form-item label="面额/折扣值" prop="value">
          <el-input-number
            v-model="createForm.value"
            :min="0"
            :precision="2"
            :step="createForm.type === 'DISCOUNT' ? 0.1 : 1"
          />
          <span style="margin-left: 10px; color: #909399;">
            {{ createForm.type === 'DISCOUNT' ? '（如0.8表示8折）' : '（元）' }}
          </span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number v-model="createForm.minAmount" :min="0" :precision="2" />
          <span style="margin-left: 10px; color: #909399;">（元，0表示无门槛）</span>
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="createForm.totalCount" :min="1" />
        </el-form-item>
        <el-form-item label="有效期开始" prop="startTime">
          <el-date-picker
            v-model="createForm.startTime"
            type="datetime"
            placeholder="选择开始时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        <el-form-item label="有效期结束" prop="endTime">
          <el-date-picker
            v-model="createForm.endTime"
            type="datetime"
            placeholder="选择结束时间"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statsDialogVisible" title="优惠券统计" width="500px">
      <div v-if="statsData" class="stats-content">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="优惠券名称">{{ statsData.name }}</el-descriptions-item>
          <el-descriptions-item label="类型">{{ getTypeName(statsData.type) }}</el-descriptions-item>
          <el-descriptions-item label="发放总量">
            <el-tag type="primary" effect="plain">{{ statsData.totalCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="已领取数量">
            <el-tag type="success" effect="plain">{{ statsData.receivedCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="已使用数量">
            <el-tag type="warning" effect="plain">{{ statsData.usedCount }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="使用率">
            {{ statsData.totalCount > 0 ? ((statsData.usedCount / statsData.totalCount) * 100).toFixed(2) : 0 }}%
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Refresh, DataLine } from '@element-plus/icons-vue'
import axios from 'axios'

const API_BASE = '/api'

const loading = ref(false)
const total = ref(0)
const tableData = ref([])

const queryParams = reactive({
  page: 1,
  size: 10,
  name: '',
  type: ''
})

const getCouponList = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE}/coupons`, { params: queryParams })
    if (res.data.code === 200) {
      tableData.value = res.data.data.records
      total.value = res.data.data.total
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('获取列表失败')
  } finally {
    loading.value = false
  }
}

const resetQuery = () => {
  queryParams.name = ''
  queryParams.type = ''
  queryParams.page = 1
  getCouponList()
}

const getTypeName = (type) => {
  const map = {
    FULL_REDUCTION: '满减券',
    DISCOUNT: '折扣券',
    NO_THRESHOLD: '无门槛券'
  }
  return map[type] || type
}

const getTypeTagType = (type) => {
  const map = {
    FULL_REDUCTION: 'danger',
    DISCOUNT: 'warning',
    NO_THRESHOLD: 'success'
  }
  return map[type] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  return new Date(date).toLocaleString('zh-CN')
}

const handleStatusChange = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '启用' : '禁用'}该优惠券吗？`,
      '提示',
      { type: 'warning' }
    )
    const res = await axios.put(`${API_BASE}/coupons/${row.id}/status`, null, {
      params: { status: row.status }
    })
    if (res.data.code === 200) {
      ElMessage.success('状态更新成功')
    } else {
      ElMessage.error(res.data.message)
      getCouponList()
    }
  } catch (e) {
    if (e !== 'cancel') {
      getCouponList()
    }
  }
}

const createDialogVisible = ref(false)
const createFormRef = ref(null)
const createForm = reactive({
  name: '',
  type: '',
  value: 0,
  minAmount: 0,
  totalCount: 100,
  startTime: '',
  endTime: ''
})

const createRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入面额', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }]
}

const onTypeChange = () => {
  if (createForm.type === 'DISCOUNT') {
    createForm.value = 0.8
  } else {
    createForm.value = 10
  }
  if (createForm.type === 'NO_THRESHOLD') {
    createForm.minAmount = 0
  }
}

const openCreateDialog = () => {
  createFormRef.value?.resetFields()
  createDialogVisible.value = true
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const res = await axios.post(`${API_BASE}/coupons`, createForm)
        if (res.data.code === 200) {
          ElMessage.success('创建成功')
          createDialogVisible.value = false
          getCouponList()
        } else {
          ElMessage.error(res.data.message)
        }
      } catch (e) {
        ElMessage.error('创建失败')
      }
    }
  })
}

const statsDialogVisible = ref(false)
const statsData = ref(null)

const viewStats = async (row) => {
  try {
    const res = await axios.get(`${API_BASE}/coupons/${row.id}/stats`)
    if (res.data.code === 200) {
      statsData.value = res.data.data
      statsDialogVisible.value = true
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('获取统计失败')
  }
}

getCouponList()
</script>

<style scoped>
.coupon-manage {
  padding: 20px;
}
.header-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.query-form {
  margin-bottom: 20px;
}
.pagination {
  margin-top: 20px;
  justify-content: flex-end;
  display: flex;
}
.stats-content {
  padding: 10px;
}
</style>
