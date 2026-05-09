<template>
  <div class="my-coupons">
    <div class="page-header">
      <h2>我的优惠券</h2>
      <el-tabs v-model="activeTab" class="coupon-tabs" @tab-change="onTabChange">
        <el-tab-pane label="未使用" name="UNUSED" />
        <el-tab-pane label="已使用" name="USED" />
        <el-tab-pane label="已过期" name="EXPIRED" />
      </el-tabs>
    </div>

    <div class="coupon-grid">
      <div
        v-for="userCoupon in userCoupons"
        :key="userCoupon.id"
        class="coupon-card"
        :class="[
          'type-' + userCoupon.coupon?.type,
          'status-' + userCoupon.status
        ]"
      >
        <div class="coupon-left">
          <div class="coupon-value">
            <span v-if="userCoupon.coupon?.type === 'DISCOUNT'" class="discount">
              {{ ((userCoupon.coupon?.value || 0) * 10).toFixed(0) }}
              <span class="unit">折</span>
            </span>
            <span v-else class="amount">
              <span class="symbol">¥</span>{{ userCoupon.coupon?.value || 0 }}
            </span>
          </div>
          <div class="coupon-condition">
            <span v-if="userCoupon.coupon?.minAmount > 0">
              满{{ userCoupon.coupon?.minAmount }}可用
            </span>
            <span v-else>无门槛使用</span>
          </div>
        </div>
        <div class="coupon-divider">
          <div class="circle top"></div>
          <div class="circle bottom"></div>
        </div>
        <div class="coupon-right">
          <div class="coupon-name">{{ userCoupon.coupon?.name }}</div>
          <div class="coupon-type">{{ getTypeName(userCoupon.coupon?.type) }}</div>
          <div class="coupon-time">
            {{ formatDate(userCoupon.coupon?.startTime) }} - {{ formatDate(userCoupon.coupon?.endTime) }}
          </div>
          <div class="coupon-status">
            <el-tag v-if="userCoupon.status === 'USED'" type="info" size="small">
              已使用
            </el-tag>
            <el-tag v-else-if="userCoupon.status === 'EXPIRED'" type="warning" size="small">
              已过期
            </el-tag>
            <el-tag v-else type="success" size="small">
              可使用
            </el-tag>
          </div>
        </div>
        <div v-if="userCoupon.status !== 'UNUSED'" class="coupon-mask">
          <span>{{ userCoupon.status === 'USED' ? '已使用' : '已过期' }}</span>
        </div>
      </div>

      <el-empty v-if="userCoupons.length === 0 && !loading" :description="emptyDescription" />
    </div>

    <el-pagination
      v-model:current-page="page"
      v-model:page-size="size"
      :page-sizes="[5, 10, 20]"
      :total="total"
      layout="prev, pager, next"
      @size-change="getMyCoupons"
      @current-change="getMyCoupons"
      class="pagination"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const API_BASE = '/api'

const userCoupons = ref([])
const loading = ref(false)
const total = ref(0)
const page = ref(1)
const size = ref(10)
const activeTab = ref('UNUSED')

const emptyDescription = computed(() => {
  const map = {
    UNUSED: '暂无可用优惠券',
    USED: '暂无已使用的优惠券',
    EXPIRED: '暂无已过期的优惠券'
  }
  return map[activeTab.value]
})

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
  return `${d.getFullYear()}.${d.getMonth() + 1}.${d.getDate()}`
}

const getMyCoupons = async () => {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE}/coupons/my`, {
      params: { page: page.value, size: size.value, status: activeTab.value }
    })
    if (res.data.code === 200) {
      userCoupons.value = res.data.data.records
      total.value = res.data.data.total
    } else {
      ElMessage.error(res.data.message)
    }
  } catch (e) {
    ElMessage.error('获取优惠券失败，请先登录')
  } finally {
    loading.value = false
  }
}

const onTabChange = () => {
  page.value = 1
  getMyCoupons()
}

onMounted(() => {
  getMyCoupons()
})
</script>

<style scoped>
.my-coupons {
  padding: 30px;
  background: #f5f7fa;
  min-height: 100vh;
}
.page-header {
  max-width: 1200px;
  margin: 0 auto 20px;
}
.page-header h2 {
  font-size: 24px;
  color: #303133;
  margin-bottom: 15px;
}
.coupon-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}
.coupon-tabs :deep(.el-tabs__item) {
  font-size: 16px;
}
.coupon-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(380px, 1fr));
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
}
.coupon-card {
  position: relative;
  display: flex;
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
}
.coupon-left {
  width: 130px;
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
.status-USED .coupon-left,
.status-EXPIRED .coupon-left {
  background: linear-gradient(135deg, #b2bec3, #636e72) !important;
}
.coupon-value {
  font-weight: bold;
  margin-bottom: 8px;
}
.coupon-value .symbol {
  font-size: 16px;
}
.coupon-value .amount {
  font-size: 32px;
}
.coupon-value .discount {
  font-size: 32px;
}
.coupon-value .unit {
  font-size: 14px;
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
  font-size: 15px;
  font-weight: bold;
  color: #303133;
}
.coupon-type {
  font-size: 12px;
  color: #909399;
}
.coupon-time {
  font-size: 12px;
  color: #909399;
}
.coupon-mask {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #909399;
  font-size: 18px;
  font-weight: bold;
}
.pagination {
  margin-top: 30px;
  justify-content: center;
  display: flex;
}
</style>
