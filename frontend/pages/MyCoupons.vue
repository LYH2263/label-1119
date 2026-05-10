<template>
  <div class="my-coupons">
    <div class="page-header">
      <h2>我的优惠券</h2>
    </div>

    <div class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-btn"
        :class="{ active: activeTab === tab.value }"
        @click="switchTab(tab.value)"
      >
        {{ tab.label }}
        <span v-if="tab.count > 0" class="tab-count">{{ tab.count }}</span>
      </button>
    </div>

    <div class="coupon-list">
      <div
        v-for="item in filteredCoupons"
        :key="item.id"
        class="coupon-card"
        :class="getStatusClass(item.status)"
      >
        <div class="coupon-left">
          <div class="coupon-value" v-if="item.coupon">
            <template v-if="item.coupon.type === 'DISCOUNT'">
              <span class="value-number">{{ item.coupon.discountValue }}</span>
              <span class="value-unit">折</span>
            </template>
            <template v-else>
              <span class="value-symbol">¥</span>
              <span class="value-number">{{ formatValue(item.coupon.discountValue) }}</span>
            </template>
          </div>
          <div class="coupon-condition" v-if="item.coupon">
            {{ item.coupon.minAmount > 0 ? `满${item.coupon.minAmount}元可用` : '无门槛' }}
          </div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name" v-if="item.coupon">{{ item.coupon.name }}</div>
          <div class="coupon-type-tag" v-if="item.coupon" :class="getTypeClass(item.coupon.type)">
            {{ getTypeName(item.coupon.type) }}
          </div>
          <div class="coupon-period" v-if="item.coupon">
            {{ formatDate(item.coupon.startTime) }} - {{ formatDate(item.coupon.endTime) }}
          </div>
          <div class="coupon-receive-time">
            领取时间：{{ formatDate(item.receivedAt) }}
          </div>
          <div class="coupon-used-time" v-if="item.status === 1 && item.usedAt">
            使用时间：{{ formatDate(item.usedAt) }}
          </div>
        </div>
        <div class="coupon-divider"></div>
        <div class="coupon-status-tag">
          <span v-if="item.status === 0" class="tag-unused">未使用</span>
          <span v-else-if="item.status === 1" class="tag-used">已使用</span>
          <span v-else class="tag-expired">已过期</span>
        </div>
      </div>

      <div v-if="filteredCoupons.length === 0" class="empty-state">
        <i class="fas fa-ticket-alt"></i>
        <p>{{ emptyText }}</p>
        <button class="go-receive-btn" @click="$emit('go-receive')">去领券中心</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'

const API_BASE = '/api'
const getToken = () => localStorage.getItem('huanshengxian_token')

const emit = defineEmits(['go-receive'])

const allCoupons = ref([])
const activeTab = ref(null)

const tabs = reactive([
  { label: '全部', value: null, count: 0 },
  { label: '未使用', value: 0, count: 0 },
  { label: '已使用', value: 1, count: 0 },
  { label: '已过期', value: 2, count: 0 }
])

const filteredCoupons = computed(() => {
  if (activeTab.value === null) {
    return allCoupons.value
  }
  return allCoupons.value.filter(item => item.status === activeTab.value)
})

const emptyText = computed(() => {
  if (activeTab.value === 0) return '暂无未使用的优惠券'
  if (activeTab.value === 1) return '暂无已使用的优惠券'
  if (activeTab.value === 2) return '暂无已过期的优惠券'
  return '暂无优惠券，去领券中心看看吧'
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

async function loadMyCoupons() {
  try {
    allCoupons.value = await request('/coupons/mine')
    updateTabCounts()
  } catch (error) {
    console.error('加载我的优惠券失败:', error)
  }
}

function updateTabCounts() {
  tabs[0].count = allCoupons.value.length
  tabs[1].count = allCoupons.value.filter(item => item.status === 0).length
  tabs[2].count = allCoupons.value.filter(item => item.status === 1).length
  tabs[3].count = allCoupons.value.filter(item => item.status === 2).length
}

function switchTab(value) {
  activeTab.value = value
}

function getTypeName(type) {
  const map = { FULL_REDUCTION: '满减券', DISCOUNT: '折扣券', NO_THRESHOLD: '无门槛券' }
  return map[type] || '未知'
}

function getTypeClass(type) {
  const map = { FULL_REDUCTION: 'type-full', DISCOUNT: 'type-discount', NO_THRESHOLD: 'type-no-threshold' }
  return map[type] || ''
}

function getStatusClass(status) {
  const map = { 0: 'status-unused', 1: 'status-used', 2: 'status-expired' }
  return map[status] || ''
}

function formatValue(val) {
  return Number(val).toFixed(2)
}

function formatDate(dateStr) {
  if (!dateStr) return '--'
  return dateStr.replace('T', ' ').substring(0, 10)
}

onMounted(() => {
  loadMyCoupons()
})

defineExpose({ loadMyCoupons })
</script>

<style scoped>
.my-coupons {
  padding: 24px;
  max-width: 960px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  font-size: 22px;
  color: #333;
  margin: 0;
}

.tab-bar {
  display: flex;
  gap: 4px;
  margin-bottom: 20px;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 0;
}

.tab-btn {
  padding: 10px 20px;
  border: none;
  background: none;
  cursor: pointer;
  font-size: 15px;
  color: #666;
  position: relative;
  transition: color 0.2s;
}

.tab-btn:hover { color: #4caf50; }

.tab-btn.active {
  color: #4caf50;
  font-weight: 600;
}

.tab-btn.active::after {
  content: '';
  position: absolute;
  bottom: -2px;
  left: 0;
  right: 0;
  height: 3px;
  background: #4caf50;
  border-radius: 2px;
}

.tab-count {
  display: inline-block;
  background: #ffe0b2;
  color: #e65100;
  font-size: 11px;
  padding: 1px 6px;
  border-radius: 8px;
  margin-left: 4px;
  font-weight: 500;
}

.coupon-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.coupon-card {
  display: flex;
  align-items: stretch;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  position: relative;
  transition: opacity 0.2s;
}

.coupon-card.status-used,
.coupon-card.status-expired {
  opacity: 0.6;
}

.coupon-left {
  width: 120px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 16px;
  color: #fff;
}

.status-unused .coupon-left { background: linear-gradient(135deg, #4caf50, #388e3c); }
.status-used .coupon-left { background: linear-gradient(135deg, #9e9e9e, #757575); }
.status-expired .coupon-left { background: linear-gradient(135deg, #bdbdbd, #9e9e9e); }

.coupon-value {
  display: flex;
  align-items: baseline;
  margin-bottom: 2px;
}

.value-symbol { font-size: 14px; margin-right: 2px; }
.value-number { font-size: 30px; font-weight: 700; line-height: 1; }
.value-unit { font-size: 12px; margin-left: 2px; }

.coupon-condition { font-size: 11px; opacity: 0.9; }

.coupon-right {
  flex: 1;
  padding: 12px 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding-right: 80px;
}

.coupon-name {
  font-size: 15px;
  font-weight: 600;
  color: #333;
  margin-bottom: 4px;
}

.coupon-type-tag {
  display: inline-block;
  padding: 1px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  width: fit-content;
  margin-bottom: 4px;
}

.type-full { background: #fff3e0; color: #e65100; }
.type-discount { background: #e3f2fd; color: #1565c0; }
.type-no-threshold { background: #e8f5e9; color: #2e7d32; }

.coupon-period {
  font-size: 12px;
  color: #999;
  margin-bottom: 2px;
}

.coupon-receive-time {
  font-size: 11px;
  color: #bbb;
}

.coupon-used-time {
  font-size: 11px;
  color: #999;
}

.coupon-divider {
  position: absolute;
  left: 120px;
  top: 8px;
  bottom: 8px;
  border-left: 2px dashed #e0e0e0;
}

.coupon-status-tag {
  position: absolute;
  right: 16px;
  top: 50%;
  transform: translateY(-50%);
}

.coupon-status-tag span {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.tag-unused { background: #e8f5e9; color: #2e7d32; }
.tag-used { background: #eceff1; color: #78909c; }
.tag-expired { background: #fce4ec; color: #c62828; }

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
  margin-bottom: 16px;
}

.go-receive-btn {
  padding: 8px 24px;
  background: #4caf50;
  color: #fff;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  font-size: 14px;
}

.go-receive-btn:hover { background: #43a047; }
</style>
