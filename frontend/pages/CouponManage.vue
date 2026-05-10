<template>
  <div class="coupon-manage">
    <div class="page-header">
      <h2>优惠券管理</h2>
      <button class="btn-primary" @click="openCreateDialog">
        <i class="fas fa-plus"></i> 创建优惠券
      </button>
    </div>

    <div class="filter-bar">
      <select v-model="filters.type" @change="loadCoupons">
        <option :value="null">全部类型</option>
        <option value="FULL_REDUCTION">满减券</option>
        <option value="DISCOUNT">折扣券</option>
        <option value="NO_THRESHOLD">无门槛券</option>
      </select>
      <select v-model="filters.status" @change="loadCoupons">
        <option :value="null">全部状态</option>
        <option :value="1">启用</option>
        <option :value="0">禁用</option>
      </select>
    </div>

    <div class="coupon-table">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>名称</th>
            <th>类型</th>
            <th>面额/折扣</th>
            <th>使用门槛</th>
            <th>已领/总量</th>
            <th>已使用</th>
            <th>有效期</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="coupon in coupons" :key="coupon.id">
            <td>{{ coupon.id }}</td>
            <td>{{ coupon.name }}</td>
            <td>
              <span class="coupon-type-badge" :class="getTypeClass(coupon.type)">
                {{ getTypeName(coupon.type) }}
              </span>
            </td>
            <td>{{ formatDiscountValue(coupon) }}</td>
            <td>{{ coupon.minAmount > 0 ? `¥${coupon.minAmount}` : '无门槛' }}</td>
            <td>{{ coupon.receivedCount }} / {{ coupon.totalCount }}</td>
            <td>{{ coupon.usedCount }}</td>
            <td>{{ formatDate(coupon.startTime) }} ~ {{ formatDate(coupon.endTime) }}</td>
            <td>
              <span class="status-badge" :class="coupon.status === 1 ? 'status-enabled' : 'status-disabled'">
                {{ coupon.status === 1 ? '启用' : '禁用' }}
              </span>
            </td>
            <td>
              <button class="btn-sm" @click="viewStats(coupon.id)">统计</button>
              <button
                class="btn-sm"
                :class="coupon.status === 1 ? 'btn-warning' : 'btn-success'"
                @click="toggleStatus(coupon)"
              >
                {{ coupon.status === 1 ? '禁用' : '启用' }}
              </button>
            </td>
          </tr>
          <tr v-if="coupons.length === 0">
            <td colspan="10" class="empty-text">暂无优惠券数据</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div class="pagination" v-if="totalPages > 1">
      <button class="page-btn" :disabled="currentPage <= 1" @click="goToPage(currentPage - 1)">
        <i class="fas fa-chevron-left"></i>
      </button>
      <button
        v-for="p in displayPages"
        :key="p"
        class="page-btn"
        :class="{ active: p === currentPage }"
        @click="goToPage(p)"
      >
        {{ p }}
      </button>
      <button class="page-btn" :disabled="currentPage >= totalPages" @click="goToPage(currentPage + 1)">
        <i class="fas fa-chevron-right"></i>
      </button>
    </div>

    <div class="modal-overlay" v-if="showCreateModal" @click.self="showCreateModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>创建优惠券</h3>
          <button class="close-btn" @click="showCreateModal = false"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div class="form-group">
            <label>优惠券名称 <span class="required">*</span></label>
            <input v-model="form.name" placeholder="请输入优惠券名称" />
          </div>
          <div class="form-group">
            <label>优惠券类型 <span class="required">*</span></label>
            <select v-model="form.type">
              <option value="FULL_REDUCTION">满减券</option>
              <option value="DISCOUNT">折扣券</option>
              <option value="NO_THRESHOLD">无门槛券</option>
            </select>
          </div>
          <div class="form-group">
            <label>{{ form.type === 'DISCOUNT' ? '折扣率(如8.5表示85折)' : '优惠面额(元)' }} <span class="required">*</span></label>
            <input v-model.number="form.discountValue" type="number" step="0.01" min="0.01" placeholder="请输入" />
          </div>
          <div class="form-group" v-if="form.type === 'FULL_REDUCTION' || form.type === 'DISCOUNT'">
            <label>使用门槛金额(元) <span class="required">*</span></label>
            <input v-model.number="form.minAmount" type="number" step="0.01" min="0" placeholder="满多少元可用" />
          </div>
          <div class="form-group">
            <label>发放总量 <span class="required">*</span></label>
            <input v-model.number="form.totalCount" type="number" min="1" placeholder="请输入发放总量" />
          </div>
          <div class="form-group">
            <label>有效期开始 <span class="required">*</span></label>
            <input v-model="form.startTime" type="datetime-local" />
          </div>
          <div class="form-group">
            <label>有效期结束 <span class="required">*</span></label>
            <input v-model="form.endTime" type="datetime-local" />
          </div>
          <div class="form-group">
            <label>优惠券描述</label>
            <textarea v-model="form.description" placeholder="请输入优惠券描述（选填）" rows="3"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showCreateModal = false">取消</button>
          <button class="btn-primary" @click="createCoupon">确认创建</button>
        </div>
      </div>
    </div>

    <div class="modal-overlay" v-if="showStatsModal" @click.self="showStatsModal = false">
      <div class="modal-content">
        <div class="modal-header">
          <h3>优惠券使用统计</h3>
          <button class="close-btn" @click="showStatsModal = false"><i class="fas fa-times"></i></button>
        </div>
        <div class="modal-body">
          <div class="stats-grid" v-if="stats">
            <div class="stats-card">
              <div class="stats-value">{{ stats.totalCount || 0 }}</div>
              <div class="stats-label">发放总量</div>
            </div>
            <div class="stats-card">
              <div class="stats-value">{{ stats.receivedCount || 0 }}</div>
              <div class="stats-label">已领取</div>
            </div>
            <div class="stats-card">
              <div class="stats-value">{{ stats.usedCount || 0 }}</div>
              <div class="stats-label">已使用</div>
            </div>
            <div class="stats-card">
              <div class="stats-value">{{ stats.usageRate || 0 }}%</div>
              <div class="stats-label">使用率</div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn-secondary" @click="showStatsModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const API_BASE = '/api'
const getToken = () => localStorage.getItem('huanshengxian_token')

const coupons = ref([])
const currentPage = ref(1)
const totalPages = ref(1)
const pageSize = ref(10)
const showCreateModal = ref(false)
const showStatsModal = ref(false)
const stats = ref(null)

const filters = reactive({
  type: null,
  status: null
})

const form = reactive({
  name: '',
  type: 'FULL_REDUCTION',
  discountValue: null,
  minAmount: 0,
  totalCount: 100,
  startTime: '',
  endTime: '',
  description: ''
})

const displayPages = computed(() => {
  const pages = []
  for (let i = 1; i <= totalPages.value; i++) {
    if (i === 1 || i === totalPages.value || (i >= currentPage.value - 2 && i <= currentPage.value + 2)) {
      pages.push(i)
    } else if (i === currentPage.value - 3 || i === currentPage.value + 3) {
      pages.push('...')
    }
  }
  return pages
})

async function request(url, options = {}) {
  const headers = { ...options.headers }
  if (options.body && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json'
  }
  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }
  const response = await fetch(`${API_BASE}${url}`, { ...options, headers })
  const data = await response.json()
  if (data.code !== 200) {
    throw new Error(data.message || '请求失败')
  }
  return data.data
}

async function loadCoupons() {
  try {
    let url = `/coupons/list?page=${currentPage.value}&size=${pageSize.value}`
    if (filters.type !== null) url += `&type=${filters.type}`
    if (filters.status !== null) url += `&status=${filters.status}`
    const data = await request(url)
    coupons.value = data.records || []
    totalPages.value = data.pages || 1
  } catch (error) {
    console.error('加载优惠券列表失败:', error)
  }
}

function goToPage(page) {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  loadCoupons()
}

function getTypeName(type) {
  const map = { FULL_REDUCTION: '满减券', DISCOUNT: '折扣券', NO_THRESHOLD: '无门槛券' }
  return map[type] || '未知'
}

function getTypeClass(type) {
  const map = { FULL_REDUCTION: 'type-full', DISCOUNT: 'type-discount', NO_THRESHOLD: 'type-no-threshold' }
  return map[type] || ''
}

function formatDiscountValue(coupon) {
  if (coupon.type === 'DISCOUNT') {
    return `${coupon.discountValue}折`
  }
  return `¥${Number(coupon.discountValue).toFixed(2)}`
}

function formatDate(dateStr) {
  if (!dateStr) return '--'
  return dateStr.replace('T', ' ').substring(0, 10)
}

function openCreateDialog() {
  Object.assign(form, {
    name: '',
    type: 'FULL_REDUCTION',
    discountValue: null,
    minAmount: 0,
    totalCount: 100,
    startTime: '',
    endTime: '',
    description: ''
  })
  showCreateModal.value = true
}

async function createCoupon() {
  try {
    const payload = {
      name: form.name,
      type: form.type,
      discountValue: form.discountValue,
      minAmount: form.type === 'NO_THRESHOLD' ? 0 : (form.minAmount || 0),
      totalCount: form.totalCount,
      startTime: form.startTime ? form.startTime.replace('T', ' ') + ':00' : null,
      endTime: form.endTime ? form.endTime.replace('T', ' ') + ':00' : null,
      description: form.description
    }
    await request('/coupons', {
      method: 'POST',
      body: JSON.stringify(payload)
    })
    showCreateModal.value = false
    loadCoupons()
  } catch (error) {
    alert(error.message)
  }
}

async function toggleStatus(coupon) {
  try {
    const newStatus = coupon.status === 1 ? 0 : 1
    await request(`/coupons/${coupon.id}/status?status=${newStatus}`, { method: 'PUT' })
    loadCoupons()
  } catch (error) {
    alert(error.message)
  }
}

async function viewStats(couponId) {
  try {
    stats.value = await request(`/coupons/${couponId}/stats`)
    showStatsModal.value = true
  } catch (error) {
    alert(error.message)
  }
}

onMounted(() => {
  loadCoupons()
})
</script>

<style scoped>
.coupon-manage {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 22px;
  color: #333;
  margin: 0;
}

.filter-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}

.filter-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  background: #fff;
}

.coupon-table {
  overflow-x: auto;
}

.coupon-table table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.coupon-table th,
.coupon-table td {
  padding: 12px 16px;
  text-align: center;
  font-size: 14px;
  border-bottom: 1px solid #f0f0f0;
}

.coupon-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #555;
}

.coupon-table td {
  color: #333;
}

.empty-text {
  color: #999;
  padding: 40px 0 !important;
}

.coupon-type-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.type-full { background: #fff3e0; color: #e65100; }
.type-discount { background: #e3f2fd; color: #1565c0; }
.type-no-threshold { background: #e8f5e9; color: #2e7d32; }

.status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
}

.status-enabled { background: #e8f5e9; color: #2e7d32; }
.status-disabled { background: #fce4ec; color: #c62828; }

.btn-primary {
  background: #4caf50;
  color: #fff;
  border: none;
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.btn-primary:hover { background: #43a047; }

.btn-secondary {
  background: #f5f5f5;
  color: #666;
  border: 1px solid #ddd;
  padding: 8px 20px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
}

.btn-sm {
  padding: 4px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 12px;
  margin-right: 4px;
}

.btn-warning { border-color: #ff9800; color: #ff9800; }
.btn-success { border-color: #4caf50; color: #4caf50; }

.pagination {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
}

.page-btn {
  padding: 6px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  background: #fff;
  cursor: pointer;
  font-size: 14px;
}

.page-btn.active { background: #4caf50; color: #fff; border-color: #4caf50; }
.page-btn:disabled { opacity: 0.5; cursor: not-allowed; }

.modal-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 12px;
  width: 560px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-header h3 { margin: 0; font-size: 18px; }

.close-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: #999;
}

.modal-body { padding: 24px; }
.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.form-group { margin-bottom: 16px; }
.form-group label { display: block; margin-bottom: 6px; font-size: 14px; color: #555; font-weight: 500; }
.required { color: #e53935; }

.form-group input,
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  box-sizing: border-box;
}

.form-group textarea { resize: vertical; }

.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stats-card {
  text-align: center;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.stats-value { font-size: 28px; font-weight: 700; color: #4caf50; }
.stats-label { font-size: 14px; color: #888; margin-top: 4px; }
</style>
