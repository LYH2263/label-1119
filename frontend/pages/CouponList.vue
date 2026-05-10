<template>
  <div class="coupon-list">
    <div class="page-header">
      <h2>领券中心</h2>
      <p class="subtitle">精选优惠券，新鲜好货更优惠</p>
    </div>

    <div class="coupon-grid">
      <div
        v-for="coupon in coupons"
        :key="coupon.id"
        class="coupon-card"
        :class="getTypeClass(coupon.type)"
      >
        <div class="coupon-left">
          <div class="coupon-value">
            <template v-if="coupon.type === 'DISCOUNT'">
              <span class="value-number">{{ coupon.discountValue }}</span>
              <span class="value-unit">折</span>
            </template>
            <template v-else>
              <span class="value-symbol">¥</span>
              <span class="value-number">{{ formatValue(coupon.discountValue) }}</span>
            </template>
          </div>
          <div class="coupon-condition">
            {{ coupon.minAmount > 0 ? `满${coupon.minAmount}元可用` : '无门槛' }}
          </div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ coupon.name }}</div>
          <div class="coupon-type-tag" :class="getTypeClass(coupon.type)">
            {{ getTypeName(coupon.type) }}
          </div>
          <div class="coupon-period">
            {{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
          </div>
          <div class="coupon-stock">
            剩余 {{ coupon.totalCount - coupon.receivedCount }} 张
          </div>
          <button
            class="receive-btn"
            :class="{ received: coupon.received, disabled: isDisabled(coupon) }"
            :disabled="coupon.received || isDisabled(coupon)"
            @click="receiveCoupon(coupon)"
          >
            {{ getButtonText(coupon) }}
          </button>
        </div>
        <div class="coupon-divider"></div>
      </div>

      <div v-if="coupons.length === 0" class="empty-state">
        <i class="fas fa-ticket-alt"></i>
        <p>暂无可领取的优惠券</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const API_BASE = '/api'
const getToken = () => localStorage.getItem('huanshengxian_token')

const coupons = ref([])

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

async function loadAvailableCoupons() {
  try {
    const token = getToken()
    if (token) {
      coupons.value = await request('/coupons/available')
    } else {
      coupons.value = await request('/coupons/list?page=1&size=50&status=1')
      coupons.value = (coupons.value.records || []).map(c => ({ ...c, received: false }))
    }
  } catch (error) {
    console.error('加载优惠券失败:', error)
  }
}

function getTypeName(type) {
  const map = { FULL_REDUCTION: '满减券', DISCOUNT: '折扣券', NO_THRESHOLD: '无门槛券' }
  return map[type] || '未知'
}

function getTypeClass(type) {
  const map = { FULL_REDUCTION: 'type-full', DISCOUNT: 'type-discount', NO_THRESHOLD: 'type-no-threshold' }
  return map[type] || ''
}

function formatValue(val) {
  return Number(val).toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return '--'
  return dateStr.replace('T', ' ').substring(0, 10)
}

function isDisabled(coupon) {
  return coupon.receivedCount >= coupon.totalCount
}

function getButtonText(coupon) {
  if (coupon.received) return '已领取'
  if (coupon.receivedCount >= coupon.totalCount) return '已抢光'
  return '立即领取'
}

async function receiveCoupon(coupon) {
  const token = getToken()
  if (!token) {
    alert('请先登录')
    return
  }
  try {
    await request(`/coupons/${coupon.id}/receive`, { method: 'POST' })
    coupon.received = true
    coupon.receivedCount += 1
  } catch (error) {
    alert(error.message)
  }
}

onMounted(() => {
  loadAvailableCoupons()
})
</script>

<style scoped>
.coupon-list {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  text-align: center;
  margin-bottom: 32px;
}

.page-header h2 {
  font-size: 26px;
  color: #333;
  margin: 0 0 8px 0;
}

.subtitle {
  color: #888;
  font-size: 14px;
  margin: 0;
}

.coupon-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-card {
  display: flex;
  align-items: stretch;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  position: relative;
  transition: transform 0.2s;
}

.coupon-card:hover {
  transform: translateY(-2px);
}

.coupon-left {
  width: 140px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #fff;
}

.type-full .coupon-left { background: linear-gradient(135deg, #ff9800, #f57c00); }
.type-discount .coupon-left { background: linear-gradient(135deg, #42a5f5, #1565c0); }
.type-no-threshold .coupon-left { background: linear-gradient(135deg, #66bb6a, #2e7d32); }

.coupon-value {
  display: flex;
  align-items: baseline;
  margin-bottom: 4px;
}

.value-symbol {
  font-size: 18px;
  margin-right: 2px;
}

.value-number {
  font-size: 36px;
  font-weight: 700;
  line-height: 1;
}

.value-unit {
  font-size: 14px;
  margin-left: 2px;
}

.coupon-condition {
  font-size: 12px;
  opacity: 0.9;
}

.coupon-right {
  flex: 1;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.coupon-name {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 6px;
}

.coupon-type-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  width: fit-content;
  margin-bottom: 6px;
}

.type-full { background: #fff3e0; color: #e65100; }
.type-discount { background: #e3f2fd; color: #1565c0; }
.type-no-threshold { background: #e8f5e9; color: #2e7d32; }

.coupon-period {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.coupon-stock {
  font-size: 12px;
  color: #e53935;
}

.receive-btn {
  position: absolute;
  right: 20px;
  top: 50%;
  transform: translateY(-50%);
  padding: 8px 20px;
  border: none;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  color: #fff;
  background: linear-gradient(135deg, #4caf50, #388e3c);
}

.receive-btn:hover { opacity: 0.9; }

.receive-btn.received {
  background: #e0e0e0;
  color: #999;
  cursor: default;
}

.receive-btn.disabled {
  background: #e0e0e0;
  color: #999;
  cursor: not-allowed;
}

.coupon-divider {
  position: absolute;
  left: 140px;
  top: 8px;
  bottom: 8px;
  border-left: 2px dashed #e0e0e0;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #bbb;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-state p {
  font-size: 14px;
}
</style>
