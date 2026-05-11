<template>
  <div class="my-coupons">
    <div class="page-header">
      <h2 class="page-title">
        <i class="fas fa-ticket-alt"></i>
        我的优惠券
      </h2>
      <div class="coupon-count">
        <span class="count-num">{{ totalUnused }}</span>
        <span class="count-text">张可用</span>
      </div>
    </div>

    <div class="status-tabs">
      <button 
        v-for="tab in tabs" 
        :key="tab.value"
        :class="['tab-btn', { active: activeTab === tab.value }]"
        @click="switchTab(tab.value)">
        {{ tab.label }}
        <span v-if="tab.value !== null" class="tab-count">
          ({{ getTabCount(tab.value) }})
        </span>
      </button>
    </div>

    <div class="coupon-list" v-if="userCoupons.length > 0">
      <div 
        v-for="userCoupon in userCoupons" 
        :key="userCoupon.id" 
        class="coupon-item"
        :class="[getStatusClass(userCoupon), getCouponTypeClass(userCoupon.coupon?.type)]">
        <div class="coupon-left">
          <div class="coupon-value">
            <template v-if="userCoupon.coupon?.type === 2">
              <span class="value-num">{{ (userCoupon.coupon.discount * 10).toFixed(1) }}</span>
              <span class="value-unit">折</span>
            </template>
            <template v-else>
              <span class="value-symbol">¥</span>
              <span class="value-num">{{ userCoupon.coupon?.amount }}</span>
            </template>
          </div>
          <div class="coupon-condition">
            {{ userCoupon.coupon?.minAmount > 0 ? '满' + userCoupon.coupon.minAmount + '可用' : '无门槛使用' }}
          </div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ userCoupon.coupon?.name }}</div>
          <div class="coupon-desc">{{ userCoupon.coupon?.description || '全场通用' }}</div>
          <div class="coupon-valid">
            <i class="far fa-clock"></i>
            有效期至 {{ formatDate(userCoupon.coupon?.validEndTime) }}
          </div>
          <div class="coupon-status">
            <span :class="['status-badge', 'status-' + userCoupon.status]">
              {{ getStatusText(userCoupon.status) }}
            </span>
            <template v-if="userCoupon.status === 0">
              <button class="use-btn" @click="useCoupon(userCoupon)">
                立即使用
              </button>
            </template>
          </div>
        </div>
        <div class="coupon-deco">
          <div class="deco-circle top"></div>
          <div class="deco-circle bottom"></div>
        </div>
        <div class="coupon-mask" v-if="userCoupon.status !== 0">
          <i :class="getMaskIcon(userCoupon.status)"></i>
          <span>{{ getMaskText(userCoupon.status) }}</span>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <div class="empty-icon">
        <i class="fas fa-ticket-alt"></i>
      </div>
      <p class="empty-text">{{ getEmptyText() }}</p>
      <button class="go-btn" @click="goToCouponCenter">
        去领取优惠券
      </button>
    </div>

    <div class="pagination-container" v-if="pagination.pages > 1">
      <div class="pagination-info">
        共 {{ pagination.total }} 条记录
      </div>
      <div class="pagination">
        <button :disabled="pagination.current <= 1" @click="changePage(pagination.current - 1)">
          <i class="fas fa-chevron-left"></i>
        </button>
        <span class="page-info">{{ pagination.current }} / {{ pagination.pages }}</span>
        <button :disabled="pagination.current >= pagination.pages" @click="changePage(pagination.current + 1)">
          <i class="fas fa-chevron-right"></i>
        </button>
      </div>
    </div>

    <div class="toast" :class="{ show: showToast, [toastType]: true }">
      <i :class="toastIcon"></i>
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const userCoupons = ref([])
const activeTab = ref(null)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

const userId = 4

const pagination = reactive({
  current: 1,
  size: 10,
  total: 0,
  pages: 0
})

const tabs = [
  { label: '全部', value: null },
  { label: '未使用', value: 0 },
  { label: '已使用', value: 1 },
  { label: '已过期', value: 2 }
]

const totalUnused = computed(() => {
  return userCoupons.value.filter(uc => uc.status === 0).length
})

const toastIcon = computed(() => {
  return toastType.value === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'
})

const getTypeClass = (type) => {
  const classMap = { 1: 'type-full', 2: 'type-discount', 3: 'type-free' }
  return classMap[type] || ''
}

const getCouponTypeClass = (type) => {
  const classMap = { 1: 'type-full', 2: 'type-discount', 3: 'type-free' }
  return classMap[type] || ''
}

const getStatusClass = (userCoupon) => {
  const classMap = { 0: 'status-unused', 1: 'status-used', 2: 'status-expired' }
  return classMap[userCoupon.status] || ''
}

const getStatusText = (status) => {
  const textMap = { 0: '可使用', 1: '已使用', 2: '已过期' }
  return textMap[status] || '未知'
}

const getMaskIcon = (status) => {
  const iconMap = { 1: 'fas fa-check', 2: 'fas fa-clock' }
  return iconMap[status] || ''
}

const getMaskText = (status) => {
  const textMap = { 1: '已使用', 2: '已过期' }
  return textMap[status] || ''
}

const getTabCount = (status) => {
  return userCoupons.value.filter(uc => uc.status === status).length
}

const getEmptyText = () => {
  if (activeTab.value === 0) return '暂无可用优惠券'
  if (activeTab.value === 1) return '暂无已使用的优惠券'
  if (activeTab.value === 2) return '暂无已过期的优惠券'
  return '您还没有优惠券'
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const switchTab = (tab) => {
  activeTab.value = tab
  pagination.current = 1
  fetchUserCoupons()
}

const changePage = (page) => {
  if (page < 1 || page > pagination.pages) return
  pagination.current = page
  fetchUserCoupons()
}

const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 2000)
}

const fetchUserCoupons = async () => {
  try {
    const params = new URLSearchParams({
      userId: userId,
      page: pagination.current,
      size: pagination.size
    })
    if (activeTab.value !== null) {
      params.append('status', activeTab.value)
    }

    const response = await fetch(`/api/coupons/my?${params}`)
    const result = await response.json()
    
    if (result.code === 200) {
      userCoupons.value = result.data.records
      pagination.total = result.data.total
      pagination.pages = result.data.pages
      pagination.current = result.data.current
      pagination.size = result.data.size
    }
  } catch (error) {
    console.error('获取用户优惠券失败:', error)
  }
}

const useCoupon = (userCoupon) => {
  showToastMessage('请在下单时选择使用此优惠券', 'success')
}

const goToCouponCenter = () => {
  window.location.href = '#/coupon-center'
}

onMounted(() => {
  fetchUserCoupons()
})
</script>

<style scoped>
.my-coupons {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  color: #333;
  margin: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page-title i {
  color: #409eff;
}

.coupon-count {
  display: flex;
  align-items: baseline;
  gap: 4px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 12px 24px;
  border-radius: 25px;
  color: white;
}

.count-num {
  font-size: 28px;
  font-weight: 700;
}

.count-text {
  font-size: 14px;
  opacity: 0.9;
}

.status-tabs {
  display: flex;
  gap: 8px;
  margin-bottom: 24px;
  background: white;
  padding: 8px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.tab-btn {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 6px;
  background: transparent;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.tab-btn:hover {
  background: #f5f7fa;
}

.tab-btn.active {
  background: #409eff;
  color: white;
}

.tab-count {
  font-size: 12px;
  opacity: 0.8;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-item {
  position: relative;
  background: white;
  border-radius: 12px;
  display: flex;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s, box-shadow 0.3s;
}

.coupon-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.coupon-left {
  width: 120px;
  padding: 20px 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: white;
  position: relative;
}

.type-full .coupon-left {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
}

.type-discount .coupon-left {
  background: linear-gradient(135deg, #52c41a 0%, #389e0d 100%);
}

.type-free .coupon-left {
  background: linear-gradient(135deg, #fa8c16 0%, #d46b08 100%);
}

.status-used .coupon-left,
.status-expired .coupon-left {
  background: linear-gradient(135deg, #bfbfbf 0%, #8c8c8c 100%);
}

.coupon-value {
  display: flex;
  align-items: flex-start;
  margin-bottom: 4px;
}

.value-symbol {
  font-size: 16px;
  margin-top: 4px;
}

.value-num {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.value-unit {
  font-size: 16px;
  margin-top: 8px;
}

.coupon-condition {
  font-size: 11px;
  opacity: 0.95;
  text-align: center;
}

.coupon-right {
  flex: 1;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.coupon-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  flex: 1;
}

.coupon-valid {
  font-size: 12px;
  color: #999;
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.coupon-status {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
}

.status-0 {
  background: #f0f9eb;
  color: #67c23a;
}

.status-1 {
  background: #f4f4f5;
  color: #909399;
}

.status-2 {
  background: #fef0f0;
  color: #f56c6c;
}

.use-btn {
  padding: 8px 20px;
  border: none;
  border-radius: 18px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.use-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.coupon-deco {
  position: absolute;
  left: 120px;
  top: 0;
  bottom: 0;
  width: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  width: 14px;
  height: 14px;
  background: #f5f7fa;
  border-radius: 50%;
  left: -7px;
}

.deco-circle.top {
  top: -7px;
}

.deco-circle.bottom {
  bottom: -7px;
}

.coupon-mask {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #909399;
  font-size: 18px;
  font-weight: 500;
}

.coupon-mask i {
  font-size: 48px;
  color: #d9d9d9;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: white;
  border-radius: 12px;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 20px;
  color: #d9d9d9;
}

.empty-text {
  font-size: 16px;
  color: #909399;
  margin-bottom: 24px;
}

.go-btn {
  padding: 12px 32px;
  border: none;
  border-radius: 25px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.go-btn:hover {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding: 16px 20px;
  background: white;
  border-radius: 8px;
}

.pagination-info {
  color: #606266;
  font-size: 14px;
}

.pagination {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pagination button {
  min-width: 32px;
  height: 32px;
  border: 1px solid #dcdfe6;
  background: white;
  border-radius: 4px;
  cursor: pointer;
  color: #606266;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pagination button:hover:not(:disabled) {
  border-color: #409eff;
  color: #409eff;
}

.pagination button:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.page-info {
  font-size: 14px;
  color: #606266;
}

.toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%) scale(0.8);
  padding: 16px 32px;
  background: rgba(0, 0, 0, 0.8);
  color: white;
  border-radius: 8px;
  font-size: 16px;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s;
  z-index: 1000;
  display: flex;
  align-items: center;
  gap: 8px;
}

.toast.show {
  opacity: 1;
  visibility: visible;
  transform: translate(-50%, -50%) scale(1);
}

.toast.success {
  background: rgba(103, 194, 58, 0.9);
}

.toast.error {
  background: rgba(245, 108, 108, 0.9);
}

.toast .fa-check-circle {
  color: white;
}

.toast .fa-exclamation-circle {
  color: white;
}
</style>
