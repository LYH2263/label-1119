<template>
  <div class="coupon-list">
    <div class="page-header">
      <h2>优惠券中心</h2>
      <p>精选好券，限时领取</p>
    </div>

    <div class="coupon-grid">
      <div
        v-for="coupon in coupons"
        :key="coupon.id"
        class="coupon-card"
        :class="'type-' + coupon.type"
      >
        <div class="coupon-left">
          <div class="coupon-value">
            <span v-if="coupon.type === 'DISCOUNT'" class="discount">
              {{ (coupon.value * 10).toFixed(0) }}
              <span class="unit">折</span>
            </span>
            <span v-else class="amount">
              <span class="symbol">¥</span>{{ coupon.value }}
            </span>
          </div>
          <div class="coupon-condition">
            <span v-if="coupon.minAmount > 0">满{{ coupon.minAmount }}可用</span>
            <span v-else>无门槛使用</span>
          </div>
        </div>
        <div class="coupon-divider">
          <div class="circle top"></div>
          <div class="circle bottom"></div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ coupon.name }}</div>
          <div class="coupon-type">{{ getTypeName(coupon.type) }}</div>
          <div class="coupon-stock">
            已领 {{ coupon.receivedCount }}/{{ coupon.totalCount }}
          </div>
          <div class="coupon-time">
            {{ formatDate(coupon.startTime) }} - {{ formatDate(coupon.endTime) }}
          </div>
          <el-button
            type="danger"
            size="small"
            class="claim-btn"
            :loading="coupon.claiming"
            :disabled="coupon.claimed"
            @click="claimCoupon(coupon)"
          >
            {{ coupon.claimed ? '已领取' : '立即领取' }}
          </el-button>
        </div>
      </div>

      <el-empty v-if="coupons.length === 0 && !loading" description="暂无可用优惠券" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const API_BASE = '/api'

const coupons = ref([])
const loading = ref(false)

const getTypeName = (type) => {
  const map = {
    FULL_REDUCTION: '满减券',
    DISCOUNT: '折扣券',
    NO_THRESHOLD: '无门槛券'
  }
  return map[type] || type
}

const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  return `${d.getMonth() + 1}月${d.getDate()}日`
}

const getCoupons = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE}/coupons/available`)
    if (res.data.code === 200) {
      coupons.value = res.data.data.map(c => ({ ...c, claiming: false, claimed: false }))
    }
  } catch (e) {
    ElMessage.error('获取优惠券失败')
  } finally {
    loading.value = false
  }
}

const claimCoupon = async (coupon) => {
  coupon.claiming = true
  try {
    const res = await axios.post(`${API_BASE}/coupons/${coupon.id}/claim`)
    if (res.data.code === 200) {
      ElMessage.success('领取成功')
      coupon.claimed = true
      coupon.receivedCount++
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    if (e.response?.data?.message) {
      ElMessage.error(e.response.data.message)
    } else {
      ElMessage.error('领取失败，请先登录')
    }
  } finally {
    coupon.claiming = false
  }
}

onMounted(() => {
  getCoupons()
})
</script>

<style scoped>
.coupon-list {
  padding: 30px;
  background: #f5f7fa;
  min-height: 100vh;
}
.page-header {
  text-align: center;
  margin-bottom: 30px;
}
.page-header h2 {
  font-size: 28px;
  color: #303133;
  margin-bottom: 8px;
}
.page-header p {
  color: #909399;
  font-size: 14px;
}
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  max-width: 1400px;
  margin: 0 auto;
}
.coupon-card {
  display: flex;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.2s, box-shadow 0.2s;
}
.coupon-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.12);
}
.coupon-left {
  width: 140px;
  padding: 20px 15px;
  color: white;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.type-FULL_REDUCTION .coupon-left {
  background: linear-gradient(135deg, #ff6b6b, #ee5a5a);
}
.type-DISCOUNT .coupon-left {
  background: linear-gradient(135deg, #feca57, #ff9f43);
}
.type-NO_THRESHOLD .coupon-left {
  background: linear-gradient(135deg, #1dd1a1, #10ac84);
}
.coupon-value {
  font-weight: bold;
  margin-bottom: 8px;
}
.coupon-value .symbol {
  font-size: 18px;
}
.coupon-value .amount {
  font-size: 36px;
}
.coupon-value .discount {
  font-size: 36px;
}
.coupon-value .unit {
  font-size: 16px;
}
.coupon-condition {
  font-size: 12px;
  opacity: 0.95;
}
.coupon-divider {
  width: 12px;
  position: relative;
  background: #f5f7fa;
}
.coupon-divider .circle {
  position: absolute;
  left: -6px;
  width: 12px;
  height: 12px;
  background: #f5f7fa;
  border-radius: 50%;
}
.coupon-divider .circle.top {
  top: -6px;
}
.coupon-divider .circle.bottom {
  bottom: -6px;
}
.coupon-right {
  flex: 1;
  padding: 15px 20px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.coupon-name {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}
.coupon-type {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.coupon-stock {
  font-size: 12px;
  color: #67c23a;
}
.coupon-time {
  font-size: 12px;
  color: #909399;
}
.claim-btn {
  width: 100%;
  margin-top: 8px;
}
</style>
