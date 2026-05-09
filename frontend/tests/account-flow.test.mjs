import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'

const frontendRoot = path.resolve(import.meta.dirname, '..')
const appJs = fs.readFileSync(path.resolve(frontendRoot, 'js/app.js'), 'utf-8')
const indexHtml = fs.readFileSync(path.resolve(frontendRoot, 'index.html'), 'utf-8')

test('用户菜单应提供订单、地址和反馈的真实入口', () => {
  assert.match(indexHtml, /showOrders\(\)/)
  assert.match(indexHtml, /showAddresses\(\)/)
  assert.match(indexHtml, /showFeedback\(\)/)
  assert.match(indexHtml, /id="accountModal"/)
  assert.doesNotMatch(appJs, /showToast\('订单页面开发中/)
  assert.doesNotMatch(appJs, /showToast\('地址管理页面开发中/)
  assert.doesNotMatch(appJs, /showToast\('评价页面开发中/)
})

test('结算流程应调用真实订单接口而不是本地假支付', () => {
  assert.match(appJs, /request\('\/orders'/)
  assert.match(appJs, /request\('\/addresses'/)
  assert.match(appJs, /renderAccountModal/)
})
