import test from 'node:test'
import assert from 'node:assert/strict'
import fs from 'node:fs'
import path from 'node:path'

const projectRoot = path.resolve(import.meta.dirname, '..', '..')
const initSql = fs.readFileSync(path.resolve(projectRoot, 'mysql/init.sql'), 'utf-8')

function extractImagePaths(prefix) {
  return Array.from(initSql.matchAll(new RegExp(`${prefix}[^\\n]*'\\[(.*?)\\]'`, 'g')))
    .flatMap(([, json]) => JSON.parse(`[${json}]`))
}

function extractBannerPaths() {
  return Array.from(initSql.matchAll(/\('.*?', '(.*?)', '#\//g)).map(([, imageUrl]) => imageUrl)
}

test('商品图片应全部使用本地 assets 路径', () => {
  const productImages = extractImagePaths('\\(')
  assert.equal(productImages.length, 12)
  productImages.forEach((imagePath) => {
    assert.match(imagePath, /^\/assets\/products\/.+\.(jpg|jpeg|png|webp)$/)
    assert.doesNotMatch(imagePath, /^https?:\/\//)
    assert.equal(fs.existsSync(path.resolve(projectRoot, 'frontend', imagePath.slice(1))), true)
  })
})

test('轮播图应全部使用本地 assets 路径', () => {
  const bannerImages = extractBannerPaths()
  assert.equal(bannerImages.length, 4)
  bannerImages.forEach((imagePath) => {
    assert.match(imagePath, /^\/assets\/banners\/.+\.(jpg|jpeg|png|webp)$/)
    assert.doesNotMatch(imagePath, /^https?:\/\//)
    assert.equal(fs.existsSync(path.resolve(projectRoot, 'frontend', imagePath.slice(1))), true)
  })
})
