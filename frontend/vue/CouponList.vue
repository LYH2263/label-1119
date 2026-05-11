<template>
  <div class="coupon-list">
    <div class="page-header">
      <h2 class="page-title">
        <i class="fas fa-gift"></i>
        优惠券中心
      </h2>
      <p class="page-subtitle">精选优惠券，等你来领</p>
    </div>

    <div class="coupon-tabs">
      <button 
        :class="['tab-btn', { active: activeTab === 'all' }]"
        @click="switchTab('all')">
        全部优惠券
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'full' }]"
        @click="switchTab('full')">
        <i class="fas fa-ticket-alt"></i>
        满减券
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'discount' }]"
        @click="switchTab('discount')">
        <i class="fas fa-percent"></i>
        折扣券
      </button>
      <button 
        :class="['tab-btn', { active: activeTab === 'free' }]"
        @click="switchTab('free')">
        <i class="fas fa-coins"></i>
        无门槛券
      </button>
    </div>

    <div class="coupon-grid" v-if="filteredCoupons.length > 0">
      <div 
        v-for="coupon in filteredCoupons" 
        :key="coupon.id" 
        class="coupon-card"
        :class="getCouponTypeClass(coupon.type)">
        <div class="coupon-left">
          <div class="coupon-value">
            <template v-if="coupon.type === 2">
              <span class="value-num">{{ (coupon.discount * 10).toFixed(1) }}</span>
              <span class="value-unit">折</span>
            </template>
            <template v-else>
              <span class="value-symbol">¥</span>
              <span class="value-num">{{ coupon.amount }}</span>
            </template>
          </div>
          <div class="coupon-condition">
            {{ coupon.minAmount > 0 ? '满' + coupon.minAmount + '可用' : '无门槛使用' }}
          </div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ coupon.name }}</div>
          <div class="coupon-desc">{{ coupon.description || '全场通用' }}</div>
          <div class="coupon-valid">
            <i class="far fa-clock"></i>
            {{ formatDate(coupon.validStartTime) }} - {{ formatDate(coupon.validEndTime) }}
          </div>
          <div class="coupon-stock">
            剩余 {{ coupon.totalCount - coupon.receivedCount }} / {{ coupon.totalCount }} 张
          </div>
          <button 
            class="claim-btn"
            :class="{ claimed: claimedIds.includes(coupon.id) }"
            :disabled="claimedIds.includes(coupon.id)"
            @click="claimCoupon(coupon)">
            {{ claimedIds.includes(coupon.id) ? '已领取' : '立即领取' }}
          </button>
        </div>
        <div class="coupon-deco">
          <div class="deco-circle top"></div>
          <div class="deco-circle bottom"></div>
        </div>
      </div>
    </div>

    <div class="empty-state" v-else>
      <div class="empty-icon">
        <i class="fas fa-ticket-alt"></i>
      </div>
      <p class="empty-text">暂无可用优惠券</p>
      <p class="empty-subtext">请稍后再来看看吧~</p>
    </div>

    <div class="toast" :class="{ show: showToast }">
      <i :class="toastIcon"></i>
      {{ toastMessage }}
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const couponList = ref([])
const activeTab = ref('all')
const claimedIds = ref([])
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref('success')

const userId = 4

const filteredCoupons = computed(() => {
  if (activeTab.value === 'all') {
    return couponList.value
  }
  const typeMap = { full: 1, discount: 2, free: 3 }
  return couponList.value.filter(c => c.type === typeMap[activeTab.value])
})

const toastIcon = computed(() => {
  return toastType.value === 'success' ? 'fas fa-check-circle' : 'fas fa-exclamation-circle'
})

const getCouponTypeClass = (type) => {
  const classMap = { 1: 'type-full', 2: 'type-discount', 3: 'type-free' }
  return classMap[type] || ''
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}.${month}.${day}`
}

const switchTab = (tab) => {
  activeTab.value = tab
}

const showToastMessage = (message, type = 'success') => {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 2000)
}

const fetchCoupons = async () => {
  try {
    const response = await fetch(`/api/coupons/available?userId=${userId}`)
    const result = await response.json()
    
    if (result.code === 200) {
      couponList.value = result.data
    }
  } catch (error) {
    console.error('获取优惠券列表失败:', error)
  }
}

const claimCoupon = async (coupon) => {
  try {
    const response = await fetch(`/api/coupons/${coupon.id}/claim?userId=${userId}`, {
      method: 'POST'
    })
    const result = await response.json()
    
    if (result.code === 200) {
      claimedIds.value.push(coupon.id)
      showToastMessage('领取成功！', 'success')
    } else {
      showToastMessage(result.message || '领取失败', 'error')
    }
  } catch (error) {
    console.error('领取优惠券失败:', error)
    showToastMessage('领取失败，请稍后重试', 'error')
  }
}

onMounted(() => {
  fetchCoupons()
})
</script>

<style scoped>
.coupon-list {
  padding: 40px 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.page-header {
  text-align: center;
  margin-bottom: 40px;
  color: white;
}

.page-title {
  font-size: 32px;
  margin: 0 0 8px 0;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
}

.page-title i {
  font-size: 36px;
}

.page-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.coupon-tabs {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.tab-btn {
  padding: 12px 24px;
  border: none;
  border-radius: 25px;
  background: rgba(255, 255, 255, 0.2);
  color: white;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  display: flex;
  align-items: center;
  gap: 8px;
}

.tab-btn:hover {
  background: rgba(255, 255, 255, 0.3);
}

.tab-btn.active {
  background: white;
  color: #667eea;
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
}

.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.coupon-card {
  position: relative;
  background: white;
  border-radius: 12px;
  display: flex;
  overflow: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  transition: transform 0.3s, box-shadow 0.3s;
}

.coupon-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.15);
}

.coupon-left {
  width: 140px;
  padding: 24px 16px;
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

.coupon-value {
  display: flex;
  align-items: flex-start;
  margin-bottom: 8px;
}

.value-symbol {
  font-size: 18px;
  margin-top: 6px;
}

.value-num {
  font-size: 48px;
  font-weight: 700;
  line-height: 1;
}

.value-unit {
  font-size: 18px;
  margin-top: 10px;
}

.coupon-condition {
  font-size: 12px;
  opacity: 0.95;
  text-align: center;
}

.coupon-right {
  flex: 1;
  padding: 20px;
  display: flex;
  flex-direction: column;
}

.coupon-name {
  font-size: 18px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.coupon-desc {
  font-size: 13px;
  color: #666;
  margin-bottom: 12px;
  flex: 1;
}

.coupon-valid {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.coupon-stock {
  font-size: 12px;
  color: #ff6b6b;
  margin-bottom: 12px;
}

.claim-btn {
  padding: 10px 24px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  align-self: flex-start;
}

.claim-btn:hover:not(:disabled) {
  transform: scale(1.05);
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.claim-btn:disabled {
  cursor: not-allowed;
  opacity: 0.7;
}

.claim-btn.claimed {
  background: #d9d9d9;
  color: #999;
}

.coupon-deco {
  position: absolute;
  left: 140px;
  top: 0;
  bottom: 0;
  width: 0;
  pointer-events: none;
}

.deco-circle {
  position: absolute;
  width: 16px;
  height: 16px;
  background: #f5f7fa;
  border-radius: 50%;
  left: -8px;
}

.deco-circle.top {
  top: -8px;
}

.deco-circle.bottom {
  bottom: -8px;
}

.empty-state {
  text-align: center;
  padding: 80px 20px;
  color: white;
}

.empty-icon {
  font-size: 80px;
  margin-bottom: 24px;
  opacity: 0.6;
}

.empty-text {
  font-size: 20px;
  margin-bottom: 8px;
}

.empty-subtext {
  font-size: 14px;
  opacity: 0.8;
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

.toast .fa-check-circle {
  color: #52c41a;
}

.toast .fa-exclamation-circle {
  color: #ff6b6b;
}
</style>
