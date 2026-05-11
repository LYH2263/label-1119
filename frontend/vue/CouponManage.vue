<template>
  <div class="coupon-manage">
    <div class="page-header">
      <h2 class="page-title">
        <i class="fas fa-ticket-alt"></i>
        优惠券管理
      </h2>
      <button class="btn btn-primary" @click="openCreateModal">
        <i class="fas fa-plus"></i>
        创建优惠券
      </button>
    </div>

    <div class="filter-bar">
      <div class="filter-item">
        <label>优惠券名称</label>
        <input v-model="searchForm.name" type="text" placeholder="请输入优惠券名称" @keyup.enter="searchCoupons">
      </div>
      <div class="filter-item">
        <label>优惠券类型</label>
        <select v-model="searchForm.type">
          <option value="">全部类型</option>
          <option :value="1">满减券</option>
          <option :value="2">折扣券</option>
          <option :value="3">无门槛券</option>
        </select>
      </div>
      <div class="filter-item">
        <label>状态</label>
        <select v-model="searchForm.status">
          <option value="">全部状态</option>
          <option :value="1">启用</option>
          <option :value="0">禁用</option>
        </select>
      </div>
      <button class="btn btn-primary" @click="searchCoupons">
        <i class="fas fa-search"></i>
        查询
      </button>
      <button class="btn btn-outline" @click="resetSearch">
        <i class="fas fa-redo"></i>
        重置
      </button>
    </div>

    <div class="table-container">
      <table class="data-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>优惠券名称</th>
            <th>类型</th>
            <th>优惠信息</th>
            <th>使用门槛</th>
            <th>发放总量</th>
            <th>已领取</th>
            <th>已使用</th>
            <th>有效期</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="coupon in couponList" :key="coupon.id">
            <td>{{ coupon.id }}</td>
            <td>{{ coupon.name }}</td>
            <td>
              <span :class="['type-tag', getTypeClass(coupon.type)]">
                {{ getTypeName(coupon.type) }}
              </span>
            </td>
            <td>{{ getDiscountInfo(coupon) }}</td>
            <td>{{ coupon.minAmount > 0 ? '满' + coupon.minAmount + '元' : '无门槛' }}</td>
            <td>{{ coupon.totalCount }}</td>
            <td>{{ coupon.receivedCount }}</td>
            <td>{{ coupon.usedCount }}</td>
            <td>
              <div class="valid-date">
                <div>{{ formatDate(coupon.validStartTime) }}</div>
                <div class="date-arrow">至</div>
                <div>{{ formatDate(coupon.validEndTime) }}</div>
              </div>
            </td>
            <td>
              <span :class="['status-tag', coupon.status === 1 ? 'status-active' : 'status-inactive']">
                {{ coupon.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <div class="action-btns">
                <button class="btn btn-sm btn-info" @click="viewStats(coupon)">
                  <i class="fas fa-chart-bar"></i>
                  统计
                </button>
                <button 
                  class="btn btn-sm" 
                  :class="coupon.status === 1 ? 'btn-warning' : 'btn-success'"
                  @click="toggleStatus(coupon)">
                  <i :class="coupon.status === 1 ? 'fas fa-ban' : 'fas fa-check'"></i>
                  {{ coupon.status === 1 ? '禁用' : '启用' }}
                </button>
              </div>
            </td>
          </tr>
          <tr v-if="couponList.length === 0">
            <td colspan="11" class="empty-cell">
              <i class="fas fa-inbox"></i>
              <p>暂无数据</p>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination-container">
      <div class="pagination-info">
        共 {{ pagination.total }} 条记录，第 {{ pagination.current }} / {{ pagination.pages }} 页
      </div>
      <div class="pagination">
        <button :disabled="pagination.current <= 1" @click="changePage(pagination.current - 1)">
          <i class="fas fa-chevron-left"></i>
        </button>
        <button v-for="page in pageNumbers" :key="page" 
                :class="{ active: page === pagination.current }"
                @click="changePage(page)">
          {{ page }}
        </button>
        <button :disabled="pagination.current >= pagination.pages" @click="changePage(pagination.current + 1)">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <div class="modal" :class="{ show: showCreateModal }" @click.self="closeCreateModal">
      <div class="modal-content">
        <div class="modal-header">
          <h3>{{ editingCoupon ? '编辑优惠券' : '创建优惠券' }}</h3>
          <button class="close-btn" @click="closeCreateModal">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitCoupon">
            <div class="form-row">
              <div class="form-group">
                <label>优惠券名称 <span class="required">*</span></label>
                <input v-model="couponForm.name" type="text" required placeholder="请输入优惠券名称">
              </div>
              <div class="form-group">
                <label>优惠券类型 <span class="required">*</span></label>
                <select v-model="couponForm.type" required>
                  <option :value="1">满减券</option>
                  <option :value="2">折扣券</option>
                  <option :value="3">无门槛券</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group" v-if="couponForm.type !== 2">
                <label>优惠金额（元） <span class="required">*</span></label>
                <input v-model.number="couponForm.amount" type="number" step="0.01" min="0" required placeholder="请输入优惠金额">
              </div>
              <div class="form-group" v-if="couponForm.type === 2">
                <label>折扣比例 <span class="required">*</span></label>
                <input v-model.number="couponForm.discount" type="number" step="0.01" min="0" max="1" required placeholder="例如：0.8 表示8折">
              </div>
              <div class="form-group">
                <label>使用门槛（元）</label>
                <input v-model.number="couponForm.minAmount" type="number" step="0.01" min="0" placeholder="0 表示无门槛">
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>发放总量 <span class="required">*</span></label>
                <input v-model.number="couponForm.totalCount" type="number" min="1" required placeholder="请输入发放总量">
              </div>
              <div class="form-group">
                <label>状态</label>
                <select v-model="couponForm.status">
                  <option :value="1">启用</option>
                  <option :value="0">禁用</option>
                </select>
              </div>
            </div>
            <div class="form-row">
              <div class="form-group">
                <label>有效期开始时间 <span class="required">*</span></label>
                <input v-model="couponForm.validStartTime" type="datetime-local" required>
              </div>
              <div class="form-group">
                <label>有效期结束时间 <span class="required">*</span></label>
                <input v-model="couponForm.validEndTime" type="datetime-local" required>
              </div>
            </div>
            <div class="form-group">
              <label>优惠券描述</label>
              <textarea v-model="couponForm.description" rows="3" placeholder="请输入优惠券描述"></textarea>
            </div>
            <div class="form-actions">
              <button type="button" class="btn btn-outline" @click="closeCreateModal">取消</button>
              <button type="submit" class="btn btn-primary">确认提交</button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <div class="modal" :class="{ show: showStatsModal }" @click.self="closeStatsModal">
      <div class="modal-content modal-medium">
        <div class="modal-header">
          <h3>优惠券统计 - {{ currentCoupon?.name }}</h3>
          <button class="close-btn" @click="closeStatsModal">
            <i class="fas fa-times"></i>
          </button>
        </div>
        <div class="modal-body">
          <div class="stats-grid">
            <div class="stat-card">
              <div class="stat-icon blue">
                <i class="fas fa-ticket-alt"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ couponStats.totalCount || 0 }}</div>
                <div class="stat-label">发放总量</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon green">
                <i class="fas fa-hand-holding"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ couponStats.receivedCount || 0 }}</div>
                <div class="stat-label">已领取</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon purple">
                <i class="fas fa-check-circle"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ couponStats.usedCount || 0 }}</div>
                <div class="stat-label">已使用</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon orange">
                <i class="fas fa-hourglass-half"></i>
              </div>
              <div class="stat-info">
                <div class="stat-value">{{ couponStats.remainingCount || 0 }}</div>
                <div class="stat-label">剩余数量</div>
              </div>
            </div>
          </div>
          <div class="stats-progress">
            <div class="progress-item">
              <div class="progress-header">
                <span>领取率</span>
                <span>{{ couponStats.receiveRate || 0 }}%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: (couponStats.receiveRate || 0) + '%' }"></div>
              </div>
            </div>
            <div class="progress-item">
              <div class="progress-header">
                <span>使用率</span>
                <span>{{ couponStats.useRate || 0 }}%</span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill success" :style="{ width: (couponStats.useRate || 0) + '%' }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const couponList = ref([])
const showCreateModal = ref(false)
const showStatsModal = ref(false)
const editingCoupon = ref(null)
const currentCoupon = ref(null)
const couponStats = reactive({})

const searchForm = reactive({
  name: '',
  type: '',
  status: ''
})

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

const couponForm = reactive({
  name: '',
  type: 1,
  amount: null,
  discount: null,
  minAmount: 0,
  totalCount: 100,
  status: 1,
  validStartTime: '',
  validEndTime: '',
  description: ''
})

const pageNumbers = computed(() => {
  const pages = []
  const start = Math.max(1, pagination.current - 2)
  const end = Math.min(pagination.pages, pagination.current + 2)
  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const getTypeName = (type) => {
  const typeMap = { 1: '满减券', 2: '折扣券', 3: '无门槛券' }
  return typeMap[type] || '未知'
}

const getTypeClass = (type) => {
  const classMap = { 1: 'type-full', 2: 'type-discount', 3: 'type-free' }
  return classMap[type] || ''
}

const getDiscountInfo = (coupon) => {
  if (coupon.type === 1 || coupon.type === 3) {
    return `减 ${coupon.amount} 元`
  } else if (coupon.type === 2) {
    return `${(coupon.discount * 10).toFixed(1)} 折`
  }
  return ''
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const searchCoupons = () => {
  pagination.current = 1
  fetchCoupons()
}

const resetSearch = () => {
  searchForm.name = ''
  searchForm.type = ''
  searchForm.status = ''
  searchCoupons()
}

const changePage = (page) => {
  if (page < 1 || page > pagination.pages) return
  pagination.current = page
  fetchCoupons()
}

const fetchCoupons = async () => {
  try {
    const params = new URLSearchParams({
      page: pagination.current,
      size: pagination.size
    })
    if (searchForm.name) params.append('name', searchForm.name)
    if (searchForm.type !== '') params.append('type', searchForm.type)
    if (searchForm.status !== '') params.append('status', searchForm.status)

    const response = await fetch(`/api/coupons?${params}`)
    const result = await response.json()
    
    if (result.code === 200) {
      couponList.value = result.data.records
      pagination.total = result.data.total
      pagination.pages = result.data.pages
      pagination.current = result.data.current
      pagination.size = result.data.size
    }
  } catch (error) {
    console.error('获取优惠券列表失败:', error)
  }
}

const openCreateModal = () => {
  editingCoupon.value = null
  Object.assign(couponForm, {
    name: '',
    type: 1,
    amount: null,
    discount: null,
    minAmount: 0,
    totalCount: 100,
    status: 1,
    validStartTime: '',
    validEndTime: '',
    description: ''
  })
  showCreateModal.value = true
}

const closeCreateModal = () => {
  showCreateModal.value = false
}

const submitCoupon = async () => {
  try {
    const response = await fetch('/api/coupons', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(couponForm)
    })
    const result = await response.json()
    
    if (result.code === 200) {
      alert('创建成功')
      closeCreateModal()
      fetchCoupons()
    } else {
      alert(result.message || '创建失败')
    }
  } catch (error) {
    console.error('创建优惠券失败:', error)
    alert('创建失败')
  }
}

const toggleStatus = async (coupon) => {
  const newStatus = coupon.status === 1 ? 0 : 1
  try {
    const response = await fetch(`/api/coupons/${coupon.id}/status?status=${newStatus}`, {
      method: 'PUT'
    })
    const result = await response.json()
    
    if (result.code === 200) {
      fetchCoupons()
    } else {
      alert(result.message || '操作失败')
    }
  } catch (error) {
    console.error('更新状态失败:', error)
    alert('操作失败')
  }
}

const viewStats = async (coupon) => {
  currentCoupon.value = coupon
  showStatsModal.value = true
  
  try {
    const response = await fetch(`/api/coupons/${coupon.id}/stats`)
    const result = await response.json()
    
    if (result.code === 200) {
      Object.assign(couponStats, result.data)
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

const closeStatsModal = () => {
  showStatsModal.value = false
  currentCoupon.value = null
  Object.keys(couponStats).forEach(key => delete couponStats[key])
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.coupon-manage {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin: 0;
}

.page-title i {
  color: #409eff;
  margin-right: 8px;
}

.filter-bar {
  background: white;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  gap: 16px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.filter-item label {
  font-size: 14px;
  color: #666;
}

.filter-item input,
.filter-item select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-width: 150px;
}

.btn {
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  transition: all 0.3s;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover {
  background: #66b1ff;
}

.btn-outline {
  background: white;
  color: #606266;
  border: 1px solid #dcdfe6;
}

.btn-outline:hover {
  border-color: #409eff;
  color: #409eff;
}

.btn-sm {
  padding: 4px 8px;
  font-size: 12px;
}

.btn-info {
  background: #909399;
  color: white;
}

.btn-info:hover {
  background: #a6a9ad;
}

.btn-warning {
  background: #e6a23c;
  color: white;
}

.btn-warning:hover {
  background: #ebb563;
}

.btn-success {
  background: #67c23a;
  color: white;
}

.btn-success:hover {
  background: #85ce61;
}

.table-container {
  background: white;
  border-radius: 8px;
  overflow: hidden;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #ebeef5;
}

.data-table th {
  background: #f5f7fa;
  color: #606266;
  font-weight: 600;
}

.data-table tr:hover {
  background: #f5f7fa;
}

.type-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.type-full {
  background: #ecf5ff;
  color: #409eff;
}

.type-discount {
  background: #f0f9eb;
  color: #67c23a;
}

.type-free {
  background: #fdf6ec;
  color: #e6a23c;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 10px;
  font-size: 12px;
}

.status-active {
  background: #f0f9eb;
  color: #67c23a;
}

.status-inactive {
  background: #f4f4f5;
  color: #909399;
}

.valid-date {
  font-size: 12px;
  color: #606266;
}

.date-arrow {
  text-align: center;
  color: #909399;
}

.action-btns {
  display: flex;
  gap: 8px;
}

.empty-cell {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.empty-cell i {
  font-size: 48px;
  margin-bottom: 16px;
  color: #dcdfe6;
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-top: 1px solid #ebeef5;
}

.pagination-info {
  color: #606266;
  font-size: 14px;
}

.pagination {
  display: flex;
  gap: 8px;
}

.pagination button {
  min-width: 32px;
  height: 32px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  color: #606266;
}

.pagination button:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.pagination button.active {
  background: #409eff;
  color: white;
  border-color: #409eff;
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  z-index: 1000;
}

.modal.show {
  opacity: 1;
  visibility: visible;
}

.modal-content {
  background: white;
  border-radius: 8px;
  max-width: 600px;
  width: 90%;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-medium {
  max-width: 500px;
}

.modal-header {
  padding: 20px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-header h3 {
  margin: 0;
  color: #303133;
}

.close-btn {
  background: none;
  border: none;
  font-size: 20px;
  color: #909399;
  cursor: pointer;
}

.modal-body {
  padding: 20px;
}

.form-row {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  flex: 1;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.form-group .required {
  color: #f56c6c;
}

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #409eff;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 8px;
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: white;
}

.stat-icon.blue {
  background: #409eff;
}

.stat-icon.green {
  background: #67c23a;
}

.stat-icon.purple {
  background: #909399;
}

.stat-icon.orange {
  background: #e6a23c;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stats-progress {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
  font-size: 14px;
  color: #606266;
}

.progress-bar {
  height: 8px;
  background: #ebeef5;
  border-radius: 4px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  background: #409eff;
  border-radius: 4px;
  transition: width 0.3s;
}

.progress-fill.success {
  background: #67c23a;
}
</style>
