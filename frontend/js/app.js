/**
 * 环生鲜电商平台 - 前端脚本
 */

// API 基础地址
const API_BASE = '/api';
const STORAGE_KEYS = {
    token: 'huanshengxian_token',
    user: 'huanshengxian_user'
};

// 全局状态
const state = {
    user: null,
    token: null,
    cart: [],
    addresses: [],
    orders: [],
    feedbacks: [],
    banners: [],
    categories: [],
    suppliers: [],
    supplierInsights: [],
    supplierDashboard: null,
    currentBanner: 0,
    bannerTimer: null,
    currentPage: 1,
    totalPages: 1
};

// ========================================
// 工具函数
// ========================================

// 显示 Toast 提示
function showToast(message, type = 'success') {
    const container = document.getElementById('toastContainer');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icons = {
        success: 'fa-check-circle',
        error: 'fa-times-circle',
        warning: 'fa-exclamation-circle'
    };
    
    toast.innerHTML = `<i class="fas ${icons[type]}"></i><span>${message}</span>`;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.style.animation = 'slideIn 0.3s ease reverse';
        setTimeout(() => toast.remove(), 300);
    }, 3000);
}

// API 请求封装
async function request(url, options = {}) {
    const headers = {
        ...options.headers
    };

    if (options.body && !headers['Content-Type']) {
        headers['Content-Type'] = 'application/json';
    }
    
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    
    try {
        const response = await fetch(`${API_BASE}${url}`, {
            ...options,
            headers
        });

        const contentType = response.headers.get('content-type') || '';
        const isJson = contentType.includes('application/json');
        const rawBody = await response.text();
        const hasBody = rawBody.trim().length > 0;
        let payload = rawBody;

        if (isJson && hasBody) {
            try {
                payload = JSON.parse(rawBody);
            } catch {
                throw new Error('服务返回了不完整的 JSON 数据');
            }
        }

        if (!response.ok) {
            if (response.status === 401) {
                clearAuthState();
            }
            throw new Error(isJson && typeof payload === 'object'
                ? (payload.message || '请求失败')
                : `请求失败（${response.status}）`);
        }

        if (!hasBody) {
            return null;
        }

        if (!isJson || typeof payload !== 'object') {
            throw new Error('服务返回格式异常');
        }

        if (payload.code !== 200) {
            if (payload.code === 401) {
                clearAuthState();
            }
            throw new Error(payload.message || '请求失败');
        }
        
        return payload.data;
    } catch (error) {
        console.error('Request error:', error);
        throw error;
    }
}

// 格式化价格
function formatPrice(price) {
    return parseFloat(price).toFixed(2);
}

function createPlaceholderImage(title = '商品', icon = '🥬', startColor = '#c8f7c5', endColor = '#7cc576') {
    const safeTitle = String(title).slice(0, 10);
    const svg = `
      <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 400 300">
        <defs>
          <linearGradient id="g" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" stop-color="${startColor}" />
            <stop offset="100%" stop-color="${endColor}" />
          </linearGradient>
        </defs>
        <rect width="400" height="300" rx="24" fill="url(#g)" />
        <circle cx="200" cy="120" r="54" fill="rgba(255,255,255,0.26)" />
        <text x="200" y="136" text-anchor="middle" font-size="54">${icon}</text>
        <text x="200" y="225" text-anchor="middle" font-size="28" fill="#ffffff" font-family="Arial, sans-serif">${safeTitle}</text>
      </svg>
    `;
    return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
}

function getDefaultBannerImage() {
    return createPlaceholderImage('环生鲜');
}

function getProductVisual(name = '') {
    const normalized = String(name);
    const visualMap = [
        { keywords: ['白菜', '菠菜', '生菜'], icon: '🥬', startColor: '#7ed957', endColor: '#2e8b57' },
        { keywords: ['胡萝卜'], icon: '🥕', startColor: '#ffb36b', endColor: '#ff7f11' },
        { keywords: ['紫薯'], icon: '🍠', startColor: '#b388eb', endColor: '#6a4c93' },
        { keywords: ['西红柿'], icon: '🍅', startColor: '#ff8c8c', endColor: '#e63946' },
        { keywords: ['彩椒'], icon: '🫑', startColor: '#ffd166', endColor: '#ef476f' },
        { keywords: ['苹果'], icon: '🍎', startColor: '#ff9f9f', endColor: '#c1121f' },
        { keywords: ['梨'], icon: '🍐', startColor: '#d9ed92', endColor: '#90be6d' },
        { keywords: ['橙', '柑'], icon: '🍊', startColor: '#ffd166', endColor: '#f77f00' },
        { keywords: ['芒果'], icon: '🥭', startColor: '#ffd670', endColor: '#f4a261' }
    ];
    return visualMap.find(item => item.keywords.some(keyword => normalized.includes(keyword)))
        || { icon: '🧺', startColor: '#b7e4c7', endColor: '#52b788' };
}

// 解析图片 URL
function parseImages(images, fallbackTitle = '商品') {
    if (!images) return createPlaceholderImage(fallbackTitle);
    try {
        const arr = JSON.parse(images);
        return arr[0] || createPlaceholderImage(fallbackTitle);
    } catch {
        return images || createPlaceholderImage(fallbackTitle);
    }
}

function getProductDisplayImage(product) {
    const fallbackTitle = product?.name || '商品';
    const parsed = parseImages(product?.images, fallbackTitle);
    if (!parsed || /images\.unsplash\.com/.test(parsed)) {
        const visual = getProductVisual(fallbackTitle);
        return createPlaceholderImage(fallbackTitle, visual.icon, visual.startColor, visual.endColor);
    }
    return parsed;
}

function setAuthState(token, user) {
    state.token = token;
    state.user = user;
}

function persistAuthState() {
    if (state.token && state.user) {
        localStorage.setItem(STORAGE_KEYS.token, state.token);
        localStorage.setItem(STORAGE_KEYS.user, JSON.stringify(state.user));
    }
}

function clearAuthState(showMessage = false) {
    state.token = null;
    state.user = null;
    state.cart = [];
    state.supplierDashboard = null;

    localStorage.removeItem(STORAGE_KEYS.token);
    localStorage.removeItem(STORAGE_KEYS.user);

    updateUserUI();
    updateCartUI();
    if (showMessage) {
        showToast('登录状态已失效，请重新登录', 'warning');
    }
}

function isSupplierUser() {
    return state.user?.role === 'SUPPLIER';
}

// ========================================
// 用户认证
// ========================================

// 检查登录状态
function checkAuth() {
    // 清理旧版通用 key，避免与其他项目串号。
    localStorage.removeItem('token');
    localStorage.removeItem('user');

    const token = localStorage.getItem(STORAGE_KEYS.token);
    const user = localStorage.getItem(STORAGE_KEYS.user);
    
    if (token && user) {
        setAuthState(token, JSON.parse(user));
        updateUserUI();
    }
}

// 更新用户界面
function updateUserUI() {
    const userNameEl = document.getElementById('userName');
    const supplierEntry = document.getElementById('supplierEntry');
    
    if (state.user) {
        userNameEl.textContent = state.user.nickname || state.user.username;
        supplierEntry.style.display = isSupplierUser() ? 'block' : 'none';
        // 加载购物车
        loadCart();
    } else {
        userNameEl.textContent = '登录/注册';
        supplierEntry.style.display = 'none';
    }
}

// 切换用户菜单
function toggleUserMenu() {
    if (!state.user) {
        showModal('authModal');
        return;
    }
    
    const dropdown = document.getElementById('userDropdown');
    dropdown.classList.toggle('show');
}

// 切换登录/注册标签
function switchAuthTab(tab) {
    const tabs = document.querySelectorAll('.auth-tab');
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    
    tabs.forEach(t => t.classList.remove('active'));
    
    if (tab === 'login') {
        tabs[0].classList.add('active');
        loginForm.style.display = 'flex';
        registerForm.style.display = 'none';
    } else {
        tabs[1].classList.add('active');
        loginForm.style.display = 'none';
        registerForm.style.display = 'flex';
    }
}

// 登录
async function login(username, password) {
    try {
        const data = await request('/auth/login', {
            method: 'POST',
            body: JSON.stringify({ username, password })
        });
        
        setAuthState(data.token, data.user);
        persistAuthState();
        
        updateUserUI();
        closeModal('authModal');
        showToast('登录成功');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 注册
async function register(username, password, nickname, phone) {
    try {
        await request('/auth/register', {
            method: 'POST',
            body: JSON.stringify({ username, password, nickname, phone })
        });
        
        showToast('注册成功，请登录');
        switchAuthTab('login');
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 退出登录
function logout() {
    clearAuthState();
    showToast('已退出登录');
    
    document.getElementById('userDropdown').classList.remove('show');
}

// ========================================
// 轮播图
// ========================================

// 加载轮播图
async function loadBanners() {
    try {
        const banners = await request('/banners');
        state.banners = banners;
        renderBanners();
        startBannerAutoPlay();
    } catch (error) {
        console.error('加载轮播图失败:', error);
    }
}

// 渲染轮播图
function renderBanners() {
    const slider = document.getElementById('bannerSlider');
    const dots = document.getElementById('bannerDots');
    
    if (state.banners.length === 0) {
        slider.innerHTML = `
            <div class="banner-slide">
                <div style="width:100%;height:100%;background:var(--bg-gradient);display:flex;align-items:center;justify-content:center;">
                    <div style="text-align:center;color:var(--primary-dark);">
                        <h2>欢迎来到环生鲜</h2>
                        <p>新鲜直达 品质生活</p>
                    </div>
                </div>
            </div>
        `;
        return;
    }
    
    slider.innerHTML = state.banners.map((banner, index) => `
        <div class="banner-slide">
            <img src="${banner.imageUrl}" alt="${banner.title || '轮播图'}" 
                 onerror="this.src='${getDefaultBannerImage()}'">
            ${banner.title ? `
                <div class="banner-content">
                    <h2>${banner.title}</h2>
                </div>
            ` : ''}
        </div>
    `).join('');
    
    dots.innerHTML = state.banners.map((_, index) => `
        <div class="banner-dot ${index === 0 ? 'active' : ''}" onclick="goToBanner(${index})"></div>
    `).join('');
}

// 上一张轮播
function prevBanner() {
    state.currentBanner = (state.currentBanner - 1 + state.banners.length) % state.banners.length;
    updateBanner();
}

// 下一张轮播
function nextBanner() {
    state.currentBanner = (state.currentBanner + 1) % state.banners.length;
    updateBanner();
}

// 跳转到指定轮播
function goToBanner(index) {
    state.currentBanner = index;
    updateBanner();
}

// 更新轮播位置
function updateBanner() {
    const slider = document.getElementById('bannerSlider');
    const dots = document.querySelectorAll('.banner-dot');
    
    slider.style.transform = `translateX(-${state.currentBanner * 100}%)`;
    
    dots.forEach((dot, index) => {
        dot.classList.toggle('active', index === state.currentBanner);
    });
}

// 自动播放
function startBannerAutoPlay() {
    if (state.bannerTimer) clearInterval(state.bannerTimer);
    state.bannerTimer = setInterval(nextBanner, 5000);
}

// ========================================
// 分类
// ========================================

// 加载分类
async function loadCategories() {
    try {
        const categories = await request('/categories/top');
        state.categories = categories;
        renderCategories();
        renderCategoryFilter();
    } catch (error) {
        console.error('加载分类失败:', error);
    }
}

// 渲染分类
function renderCategories() {
    const grid = document.getElementById('categoriesGrid');
    
    if (state.categories.length === 0) {
        grid.innerHTML = '<p style="text-align:center;color:var(--text-secondary);grid-column:1/-1;">暂无分类</p>';
        return;
    }
    
    grid.innerHTML = state.categories.map(cat => `
        <div class="category-item" onclick="filterByCategory(${cat.id})">
            <div class="category-icon">${cat.icon || '📦'}</div>
            <div class="category-name">${cat.name}</div>
        </div>
    `).join('');
}

// 渲染分类筛选器
function renderCategoryFilter() {
    const select = document.getElementById('categoryFilter');
    
    select.innerHTML = '<option value="">全部分类</option>' + 
        state.categories.map(cat => `<option value="${cat.id}">${cat.name}</option>`).join('');
}

// 按分类筛选
function filterByCategory(categoryId) {
    document.getElementById('categoryFilter').value = categoryId;
    state.currentPage = 1;
    loadAllProducts();
    
    // 滚动到商品区域
    document.getElementById('products').scrollIntoView({ behavior: 'smooth' });
}

// ========================================
// 商品
// ========================================

// 加载热销商品
async function loadHotProducts() {
    try {
        const products = await request('/products/hot?limit=8');
        renderProducts('hotProductsGrid', products);
    } catch (error) {
        console.error('加载热销商品失败:', error);
        document.getElementById('hotProductsGrid').innerHTML = 
            '<p style="text-align:center;color:var(--text-secondary);grid-column:1/-1;">加载失败</p>';
    }
}

// 加载新品
async function loadNewProducts() {
    try {
        const products = await request('/products/new?limit=8');
        renderProducts('newProductsGrid', products);
    } catch (error) {
        console.error('加载新品失败:', error);
        document.getElementById('newProductsGrid').innerHTML = 
            '<p style="text-align:center;color:var(--text-secondary);grid-column:1/-1;">加载失败</p>';
    }
}

// 加载全部商品
async function loadAllProducts() {
    const categoryId = document.getElementById('categoryFilter').value;
    const keyword = document.getElementById('searchInput').value;
    
    let url = `/products?page=${state.currentPage}&size=12`;
    if (categoryId) url += `&categoryId=${categoryId}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;
    
    try {
        const data = await request(url);
        state.totalPages = data.pages;
        renderProducts('allProductsGrid', data.records);
        renderPagination();
    } catch (error) {
        console.error('加载商品失败:', error);
        document.getElementById('allProductsGrid').innerHTML = 
            '<p style="text-align:center;color:var(--text-secondary);grid-column:1/-1;">加载失败</p>';
    }
}

// 渲染商品列表
function renderProducts(containerId, products) {
    const container = document.getElementById(containerId);
    
    if (!products || products.length === 0) {
        container.innerHTML = '<p style="text-align:center;color:var(--text-secondary);grid-column:1/-1;">暂无商品</p>';
        return;
    }
    
    container.innerHTML = products.map(product => `
        <div class="product-card" onclick="showProductDetail(${product.id})">
            <div class="product-image">
                <img src="${getProductDisplayImage(product)}" alt="${product.name}"
                     onerror="this.src='${createPlaceholderImage('商品')}'">
                ${product.isHot ? '<span class="product-tag">热销</span>' : ''}
                ${product.isNew ? '<span class="product-tag new">新品</span>' : ''}
                ${product.isRecommend && !product.isHot && !product.isNew ? '<span class="product-tag recommend">推荐</span>' : ''}
            </div>
            <div class="product-info">
                <div class="product-name">${product.name}</div>
                <div class="product-origin">${product.origin || '产地直供'}</div>
                <div class="product-price">
                    <span class="price-current"><span>¥</span>${formatPrice(product.price)}</span>
                    ${product.originalPrice ? `<span class="price-original">¥${formatPrice(product.originalPrice)}</span>` : ''}
                    <span style="margin-left:5px;font-size:12px;color:var(--text-secondary)">/${product.unit || '斤'}</span>
                </div>
                <div class="product-footer">
                    <span class="product-sales">已售 ${product.sales || 0}</span>
                    <button class="add-cart-btn" onclick="event.stopPropagation();addToCart(${product.id})">
                        <i class="fas fa-plus"></i>
                    </button>
                </div>
            </div>
        </div>
    `).join('');
}

// 渲染分页
function renderPagination() {
    const pagination = document.getElementById('pagination');
    
    if (state.totalPages <= 1) {
        pagination.innerHTML = '';
        return;
    }
    
    let html = '';
    
    // 上一页
    html += `<button class="page-btn" ${state.currentPage === 1 ? 'disabled' : ''} onclick="goToPage(${state.currentPage - 1})">
        <i class="fas fa-chevron-left"></i>
    </button>`;
    
    // 页码
    for (let i = 1; i <= state.totalPages; i++) {
        if (i === 1 || i === state.totalPages || (i >= state.currentPage - 2 && i <= state.currentPage + 2)) {
            html += `<button class="page-btn ${i === state.currentPage ? 'active' : ''}" onclick="goToPage(${i})">${i}</button>`;
        } else if (i === state.currentPage - 3 || i === state.currentPage + 3) {
            html += `<span class="page-btn">...</span>`;
        }
    }
    
    // 下一页
    html += `<button class="page-btn" ${state.currentPage === state.totalPages ? 'disabled' : ''} onclick="goToPage(${state.currentPage + 1})">
        <i class="fas fa-chevron-right"></i>
    </button>`;
    
    pagination.innerHTML = html;
}

// 跳转页面
function goToPage(page) {
    if (page < 1 || page > state.totalPages) return;
    state.currentPage = page;
    loadAllProducts();
    document.getElementById('products').scrollIntoView({ behavior: 'smooth' });
}

// 搜索商品
function searchProducts() {
    state.currentPage = 1;
    loadAllProducts();
    document.getElementById('products').scrollIntoView({ behavior: 'smooth' });
}

// 筛选商品
function filterProducts() {
    state.currentPage = 1;
    loadAllProducts();
}

// 显示商品详情
async function showProductDetail(productId) {
    try {
        const product = await request(`/products/${productId}`);
        renderProductDetail(product);
        showModal('productModal');
    } catch (error) {
        showToast('加载商品详情失败', 'error');
    }
}

// 渲染商品详情
function renderProductDetail(product) {
    const container = document.getElementById('productDetail');
    
    container.innerHTML = `
        <div class="detail-image">
            <img src="${getProductDisplayImage(product)}" alt="${product.name}"
                 onerror="this.src='${createPlaceholderImage('商品')}'">
        </div>
        <div class="detail-info">
            <h2>${product.name}</h2>
            <div class="detail-price">
                <span>¥${formatPrice(product.price)}</span>
                ${product.originalPrice ? `<span style="font-size:16px;color:var(--text-light);text-decoration:line-through;margin-left:10px;">¥${formatPrice(product.originalPrice)}</span>` : ''}
                <span style="font-size:14px;color:var(--text-secondary);margin-left:5px;">/${product.unit || '斤'}</span>
            </div>
            <div class="detail-meta">
                <div><i class="fas fa-map-marker-alt"></i> 产地：${product.origin || '产地直供'}</div>
                <div><i class="fas fa-clock"></i> 保质期：${product.shelfLife || '请参考包装'}</div>
                <div><i class="fas fa-box"></i> 存储方式：${product.storageMethod || '冷藏保存'}</div>
                <div><i class="fas fa-shopping-cart"></i> 已售：${product.sales || 0}件</div>
                <div><i class="fas fa-cubes"></i> 库存：${product.stock || 0}件</div>
                <div><i class="fas fa-truck-field"></i> 供应商：${product.supplierName || '平台直供'}</div>
            </div>
            <div class="detail-desc">
                ${product.description || '暂无商品描述'}
            </div>
            <div class="detail-quantity">
                <span>数量：</span>
                <button class="quantity-btn" onclick="changeDetailQuantity(-1)">
                    <i class="fas fa-minus"></i>
                </button>
                <span class="quantity-value" id="detailQuantity">1</span>
                <button class="quantity-btn" onclick="changeDetailQuantity(1)">
                    <i class="fas fa-plus"></i>
                </button>
            </div>
            <div class="detail-actions">
                <button class="btn-secondary" onclick="addToCartFromDetail(${product.id})">
                    <i class="fas fa-cart-plus"></i> 加入购物车
                </button>
                <button class="btn-primary" onclick="buyNow(${product.id})">
                    <i class="fas fa-bolt"></i> 立即购买
                </button>
            </div>
        </div>
    `;
}

// 修改详情页数量
function changeDetailQuantity(delta) {
    const el = document.getElementById('detailQuantity');
    let quantity = parseInt(el.textContent) + delta;
    if (quantity < 1) quantity = 1;
    if (quantity > 99) quantity = 99;
    el.textContent = quantity;
}

// 从详情页加入购物车
function addToCartFromDetail(productId) {
    const quantity = parseInt(document.getElementById('detailQuantity').textContent);
    addToCart(productId, quantity);
}

// 立即购买
async function buyNow(productId) {
    if (!state.user) {
        showToast('请先登录', 'warning');
        showModal('authModal');
        return;
    }
    
    const quantity = parseInt(document.getElementById('detailQuantity').textContent);
    const added = await addToCart(productId, quantity);
    if (added) {
        closeModal('productModal');
        openCart();
    }
}

// ========================================
// 购物车
// ========================================

// 加载购物车
async function loadCart() {
    if (!state.user) return;
    
    try {
        const cart = await request('/cart');
        state.cart = cart;
        updateCartUI();
    } catch (error) {
        console.error('加载购物车失败:', error);
    }
}

// 加入购物车
async function addToCart(productId, quantity = 1) {
    if (!state.user) {
        showToast('请先登录', 'warning');
        showModal('authModal');
        return false;
    }
    
    try {
        await request('/cart', {
            method: 'POST',
            body: JSON.stringify({ productId, quantity })
        });
        
        showToast('已加入购物车');
        await loadCart();
        return true;
    } catch (error) {
        showToast(error.message, 'error');
        return false;
    }
}

// 更新购物车数量
async function updateCartQuantity(productId, quantity) {
    if (quantity < 1) {
        removeFromCart(productId);
        return;
    }
    
    try {
        await request(`/cart/${productId}`, {
            method: 'PUT',
            body: JSON.stringify({ quantity })
        });
        
        loadCart();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 从购物车移除
async function removeFromCart(productId) {
    try {
        await request(`/cart/${productId}`, {
            method: 'DELETE'
        });
        
        showToast('已移除');
        loadCart();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// 更新购物车界面
function updateCartUI() {
    const countEl = document.getElementById('cartCount');
    const itemsEl = document.getElementById('cartItems');
    const totalEl = document.getElementById('cartTotal');
    
    // 更新数量
    const totalCount = state.cart.reduce((sum, item) => sum + item.quantity, 0);
    countEl.textContent = totalCount;
    
    // 更新列表
    if (state.cart.length === 0) {
        itemsEl.innerHTML = `
            <div class="cart-empty">
                <i class="fas fa-shopping-basket"></i>
                <p>购物车是空的</p>
            </div>
        `;
    } else {
        itemsEl.innerHTML = state.cart.map(item => `
            <div class="cart-item">
                <div class="cart-item-image">
                    <img src="${item.product ? getProductDisplayImage(item.product) : ''}" alt="${item.product?.name || ''}">
                </div>
                <div class="cart-item-info">
                    <div class="cart-item-name">${item.product?.name || '未知商品'}</div>
                    <div class="cart-item-price">¥${formatPrice(item.product?.price || 0)}</div>
                    <div class="cart-item-quantity">
                        <button class="quantity-btn" onclick="updateCartQuantity(${item.productId}, ${item.quantity - 1})">
                            <i class="fas fa-minus"></i>
                        </button>
                        <span class="quantity-value">${item.quantity}</span>
                        <button class="quantity-btn" onclick="updateCartQuantity(${item.productId}, ${item.quantity + 1})">
                            <i class="fas fa-plus"></i>
                        </button>
                    </div>
                </div>
                <button class="cart-item-remove" onclick="removeFromCart(${item.productId})">
                    <i class="fas fa-trash-alt"></i>
                </button>
            </div>
        `).join('');
    }
    
    // 更新总价
    const total = state.cart.reduce((sum, item) => {
        return sum + (item.product?.price || 0) * item.quantity;
    }, 0);
    totalEl.textContent = `¥${formatPrice(total)}`;
}

// 打开购物车
function openCart() {
    document.getElementById('cartSidebar').classList.add('show');
    document.getElementById('overlay').classList.add('show');
}

// 关闭购物车
function closeCart() {
    document.getElementById('cartSidebar').classList.remove('show');
    document.getElementById('overlay').classList.remove('show');
}

// 结算
async function checkout() {
    if (!state.user) {
        showToast('请先登录', 'warning');
        closeCart();
        showModal('authModal');
        return;
    }
    
    if (state.cart.length === 0) {
        showToast('购物车是空的', 'warning');
        return;
    }

    try {
        const addresses = await request('/addresses');
        state.addresses = addresses;
        const defaultAddress = addresses.find(item => item.isDefault === 1) || addresses[0];
        if (!defaultAddress) {
            closeCart();
            showToast('请先添加收货地址', 'warning');
            showAddresses();
            return;
        }

        const order = await request('/orders', {
            method: 'POST',
            body: JSON.stringify({
                addressId: defaultAddress.id,
                remark: '',
                cartItemIds: state.cart.map(item => item.id)
            })
        });

        await request(`/orders/${order.id}/pay`, {
            method: 'PUT'
        });

        document.getElementById('paymentAmount').textContent = `¥${formatPrice(order.payAmount || order.totalAmount || 0)}`;
        document.getElementById('paymentOrderNo').textContent = order.orderNo || '--';
        document.getElementById('paymentTime').textContent = order.createdAt || new Date().toLocaleString('zh-CN');

        await loadCart();
        closeCart();
        setTimeout(() => {
            showModal('paymentSuccessModal');
        }, 300);
    } catch (error) {
        showToast(error.message || '下单失败', 'error');
    }
}

// 关闭支付成功弹窗
function closePaymentSuccess() {
    closeModal('paymentSuccessModal');
}

// ========================================
// 数据分析
// ========================================

// 加载数据概览
async function loadAnalytics() {
    try {
        const overview = await request('/analytics/overview');
        
        document.getElementById('totalSales').textContent = formatPrice(overview.totalSales || 0);
        document.getElementById('totalOrders').textContent = overview.totalOrders || 0;
        document.getElementById('totalProducts').textContent = overview.totalProducts || 0;
        document.getElementById('totalUsers').textContent = overview.totalUsers || 0;
    } catch (error) {
        console.error('加载数据概览失败:', error);
    }
}

// 加载反馈统计
async function loadFeedbackStats() {
    try {
        const stats = await request('/analytics/feedback-stats');
        
        const avgRating = stats.avgRating || 0;
        document.getElementById('avgRating').textContent = avgRating.toFixed(1);
        
        // 显示星星
        const starsEl = document.getElementById('ratingStars');
        let starsHtml = '';
        for (let i = 1; i <= 5; i++) {
            if (i <= avgRating) {
                starsHtml += '<i class="fas fa-star"></i>';
            } else if (i - 0.5 <= avgRating) {
                starsHtml += '<i class="fas fa-star-half-alt"></i>';
            } else {
                starsHtml += '<i class="far fa-star"></i>';
            }
        }
        starsEl.innerHTML = starsHtml;
        
        // 显示各评分数量
        const countsEl = document.getElementById('feedbackCounts');
        const ratingStats = stats.ratingStats || {};
        countsEl.innerHTML = Object.entries(ratingStats).map(([rating, count]) => `
            <div class="feedback-count-item">
                <span>${rating}星</span>
                <span style="font-weight:bold;color:var(--warning-color)">${count}</span>
            </div>
        `).join('');
    } catch (error) {
        console.error('加载反馈统计失败:', error);
    }
}

async function loadSupplierInsights() {
    try {
        const [suppliers, insights] = await Promise.all([
            request('/suppliers'),
            request('/analytics/supplier-insights')
        ]);
        state.suppliers = suppliers;
        state.supplierInsights = insights;
        renderSupplierInsights();
    } catch (error) {
        console.error('加载供应商数据反馈失败:', error);
        document.getElementById('supplierInsightsGrid').innerHTML = `
            <div class="supplier-empty">供应商数据反馈加载失败，请稍后重试</div>
        `;
    }
}

function renderSupplierInsights() {
    const container = document.getElementById('supplierInsightsGrid');
    if (!state.supplierInsights.length) {
        container.innerHTML = '<div class="supplier-empty">暂无供应商反馈数据</div>';
        return;
    }

    container.innerHTML = state.supplierInsights.map(item => `
        <div class="supplier-card">
            <div class="supplier-card-head">
                <div>
                    <h3>${item.supplierName}</h3>
                    <p>${item.contactPerson || '待补充联系人'} · ${item.address || '待补充地址'}</p>
                </div>
                <span class="supplier-sync-tag">已同步 ${item.syncedAt}</span>
            </div>
            <div class="supplier-metrics">
                <div class="supplier-metric">
                    <strong>${item.salesStats?.totalSalesCount || 0}</strong>
                    <span>累计销量</span>
                </div>
                <div class="supplier-metric">
                    <strong>${item.feedbackStats?.pendingFeedback || 0}</strong>
                    <span>待处理反馈</span>
                </div>
                <div class="supplier-metric">
                    <strong>${(item.feedbackStats?.avgRating || 0).toFixed ? item.feedbackStats.avgRating.toFixed(1) : formatPrice(item.feedbackStats?.avgRating || 0)}</strong>
                    <span>平均评分</span>
                </div>
            </div>
            <div class="supplier-block">
                <div class="supplier-block-title">热销商品</div>
                <div class="supplier-tags">
                    ${(item.hotProducts || []).map(name => `<span>${name}</span>`).join('') || '<span>暂无热销商品</span>'}
                </div>
            </div>
            <div class="supplier-block">
                <div class="supplier-block-title">平台反馈建议</div>
                <ul class="supplier-recommendations">
                    ${(item.recommendations || []).map(text => `<li>${text}</li>`).join('')}
                </ul>
            </div>
        </div>
    `).join('');
}

// ========================================
// 弹窗控制
// ========================================

function showModal(modalId) {
    document.getElementById(modalId).classList.add('show');
    document.getElementById('overlay').classList.add('show');
}

function closeModal(modalId) {
    document.getElementById(modalId).classList.remove('show');
    document.getElementById('overlay').classList.remove('show');
}

function closeAllModals() {
    document.querySelectorAll('.modal').forEach(modal => modal.classList.remove('show'));
    document.getElementById('cartSidebar').classList.remove('show');
    document.getElementById('overlay').classList.remove('show');
    document.getElementById('userDropdown').classList.remove('show');
}

function toggleMobileMenu() {
    document.getElementById('mobileMenu').classList.toggle('show');
}

// ========================================
// 用户相关页面（示例）
// ========================================

async function showOrders() {
    if (!state.user) {
        showModal('authModal');
        return;
    }
    try {
        state.orders = await request('/orders');
        const list = state.orders.records || state.orders.list || state.orders;
        renderAccountModal('我的订单', `
            <div class="account-section">
                ${(list || []).length ? (list || []).map(order => `
                    <div class="account-card">
                        <div><strong>订单号：</strong>${order.orderNo || '--'}</div>
                        <div><strong>状态：</strong>${order.status || '--'}</div>
                        <div><strong>金额：</strong>¥${formatPrice(order.payAmount || order.totalAmount || 0)}</div>
                        <div class="account-actions">
                            ${order.status === 'PENDING' ? `<button class="btn-secondary" onclick="payUserOrder(${order.id})">立即支付</button>` : ''}
                            ${order.status === 'PENDING' ? `<button class="btn-secondary" onclick="cancelUserOrder(${order.id})">取消订单</button>` : ''}
                            ${order.status === 'SHIPPED' ? `<button class="btn-primary" onclick="confirmUserOrder(${order.id})">确认收货</button>` : ''}
                        </div>
                    </div>
                `).join('') : '<p class="account-empty">暂无订单记录</p>'}
            </div>
        `);
    } catch (error) {
        showToast(error.message, 'error');
    }
    document.getElementById('userDropdown').classList.remove('show');
}

async function showAddresses() {
    if (!state.user) {
        showModal('authModal');
        return;
    }
    try {
        state.addresses = await request('/addresses');
        renderAccountModal('收货地址', `
            <div class="account-section">
                <div class="account-form">
                    <input id="addressReceiver" placeholder="收货人">
                    <input id="addressPhone" placeholder="手机号">
                    <input id="addressProvince" placeholder="省">
                    <input id="addressCity" placeholder="市">
                    <input id="addressDistrict" placeholder="区">
                    <input id="addressDetail" placeholder="详细地址">
                    <label style="display:flex;align-items:center;gap:8px;">
                        <input style="width: 20px;" type="checkbox" id="addressDefault"> 设为默认地址
                    </label>
                    <button class="btn-primary" onclick="submitAddress()">保存地址</button>
                </div>
                <div class="account-list">
                    ${state.addresses.length ? state.addresses.map(address => `
                        <div class="account-card">
                            <div><strong>${address.receiverName}</strong> ${address.receiverPhone}</div>
                            <div>${address.province}${address.city}${address.district}${address.detailAddress}</div>
                            <div class="account-actions">
                                ${address.isDefault === 1 ? '<span class="badge-default">默认地址</span>' : `<button class="btn-secondary" onclick="setDefaultAddress(${address.id})">设为默认</button>`}
                                <button class="btn-secondary" onclick="deleteAddress(${address.id})">删除</button>
                            </div>
                        </div>
                    `).join('') : '<p class="account-empty">暂无地址，请先添加</p>'}
                </div>
            </div>
        `);
    } catch (error) {
        showToast(error.message, 'error');
    }
    document.getElementById('userDropdown').classList.remove('show');
}

async function showFeedback() {
    if (!state.user) {
        showModal('authModal');
        return;
    }
    try {
        const feedbackPage = await request('/feedbacks/user');
        state.feedbacks = feedbackPage.records || feedbackPage.list || [];
        renderAccountModal('我的反馈', `
            <div class="account-section">
                <div class="account-form">
                    <input id="feedbackProductId" placeholder="商品ID（可选）">
                    <input id="feedbackOrderId" placeholder="订单ID（可选）">
                    <select id="feedbackRating">
                        <option value="5">5 星</option>
                        <option value="4">4 星</option>
                        <option value="3">3 星</option>
                        <option value="2">2 星</option>
                        <option value="1">1 星</option>
                    </select>
                    <textarea id="feedbackContent" placeholder="请输入反馈内容"></textarea>
                    <button class="btn-primary" onclick="submitFeedback()">提交反馈</button>
                </div>
                <div class="account-list">
                    ${state.feedbacks.length ? state.feedbacks.map(item => `
                        <div class="account-card">
                            <div><strong>${item.productName || '平台反馈'}</strong> · ${item.rating || '-'} 星</div>
                            <div>${item.content || ''}</div>
                            ${item.reply ? `<div class="account-reply">商家回复：${item.reply}</div>` : ''}
                        </div>
                    `).join('') : '<p class="account-empty">暂无反馈记录</p>'}
                </div>
            </div>
        `);
    } catch (error) {
        showToast(error.message, 'error');
    }
    document.getElementById('userDropdown').classList.remove('show');
}

async function showSupplierWorkbench() {
    if (!state.user) {
        showModal('authModal');
        return;
    }
    if (!isSupplierUser()) {
        showToast('当前账号不是供应商账号', 'warning');
        return;
    }

    try {
        state.supplierDashboard = await request('/suppliers/me/dashboard');
        const supplier = state.supplierDashboard.supplier || {};
        const feedbacks = state.supplierDashboard.feedbacks || [];
        const recommendations = state.supplierDashboard.recommendations || [];
        const salesStats = state.supplierDashboard.salesStats || {};
        const feedbackStats = state.supplierDashboard.feedbackStats || {};
        const products = state.supplierDashboard.products || [];

        renderAccountModal('供应商工作台', `
            <div class="account-section supplier-workbench">
                <div class="supplier-panel">
                    <h3>供应商资料</h3>
                    <div class="account-form">
                        <input id="supplierContactPerson" placeholder="联系人" value="${supplier.contactPerson || ''}">
                        <input id="supplierContactPhone" placeholder="联系电话" value="${supplier.contactPhone || ''}">
                        <input id="supplierAddress" placeholder="联系地址" value="${supplier.address || ''}">
                        <textarea id="supplierDescription" placeholder="供应商介绍">${supplier.description || ''}</textarea>
                        <button class="btn-primary" onclick="saveSupplierProfile()">保存资料</button>
                    </div>
                </div>
                <div class="supplier-panel">
                    <h3>数据反馈摘要</h3>
                    <div class="supplier-summary-grid">
                        <div class="account-card"><strong>${salesStats.totalSalesCount || 0}</strong><span>累计销量</span></div>
                        <div class="account-card"><strong>¥${formatPrice(salesStats.totalSalesAmount || 0)}</strong><span>销售额</span></div>
                        <div class="account-card"><strong>${feedbackStats.pendingFeedback || 0}</strong><span>待处理反馈</span></div>
                        <div class="account-card"><strong>${(feedbackStats.avgRating || 0).toFixed ? feedbackStats.avgRating.toFixed(1) : formatPrice(feedbackStats.avgRating || 0)}</strong><span>平均评分</span></div>
                    </div>
                    <div class="supplier-tags" style="margin-top:16px;">
                        ${products.map(item => `<span>${item.name}</span>`).join('') || '<span>暂无在售商品</span>'}
                    </div>
                    <ul class="supplier-recommendations supplier-recommendations-inline">
                        ${recommendations.map(text => `<li>${text}</li>`).join('')}
                    </ul>
                </div>
                <div class="supplier-panel">
                    <h3>消费者反馈处理</h3>
                    <div class="account-list">
                        ${feedbacks.length ? feedbacks.map(item => `
                            <div class="account-card">
                                <div><strong>${item.productName || '未关联商品'}</strong> · ${item.rating || '-'} 星</div>
                                <div>${item.content || '暂无反馈内容'}</div>
                                ${item.reply ? `<div class="account-reply">已回复：${item.reply}</div>` : `
                                    <div class="supplier-reply-box">
                                        <textarea id="supplierReply-${item.id}" placeholder="针对这条反馈给出回复和改进措施"></textarea>
                                        <button class="btn-secondary" onclick="replySupplierFeedback(${item.id})">提交回复</button>
                                    </div>
                                `}
                            </div>
                        `).join('') : '<p class="account-empty">暂无需要处理的消费者反馈</p>'}
                    </div>
                </div>
            </div>
        `);
    } catch (error) {
        showToast(error.message, 'error');
    }

    document.getElementById('userDropdown').classList.remove('show');
}

async function saveSupplierProfile() {
    try {
        await request('/suppliers/me', {
            method: 'PUT',
            body: JSON.stringify({
                contactPerson: document.getElementById('supplierContactPerson').value,
                contactPhone: document.getElementById('supplierContactPhone').value,
                address: document.getElementById('supplierAddress').value,
                description: document.getElementById('supplierDescription').value
            })
        });
        showToast('供应商资料已更新');
        await loadSupplierInsights();
        showSupplierWorkbench();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function replySupplierFeedback(feedbackId) {
    try {
        const input = document.getElementById(`supplierReply-${feedbackId}`);
        const reply = input?.value?.trim();
        if (!reply) {
            showToast('请输入回复内容', 'warning');
            return;
        }

        await request(`/suppliers/me/feedbacks/${feedbackId}/reply`, {
            method: 'PUT',
            body: JSON.stringify({ reply })
        });

        showToast('反馈已同步回复');
        await loadSupplierInsights();
        showSupplierWorkbench();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

function renderAccountModal(title, bodyHtml) {
    const container = document.getElementById('accountModalContent');
    container.innerHTML = `
        <div class="account-modal">
            <h2>${title}</h2>
            ${bodyHtml}
        </div>
    `;
    showModal('accountModal');
}

async function payUserOrder(orderId) {
    try {
        await request(`/orders/${orderId}/pay`, { method: 'PUT' });
        showToast('支付成功');
        showOrders();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function cancelUserOrder(orderId) {
    try {
        await request(`/orders/${orderId}/cancel`, { method: 'PUT' });
        showToast('订单已取消');
        showOrders();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function confirmUserOrder(orderId) {
    try {
        await request(`/orders/${orderId}/receive`, { method: 'PUT' });
        showToast('已确认收货');
        showOrders();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function submitAddress() {
    try {
        await request('/addresses', {
            method: 'POST',
            body: JSON.stringify({
                receiverName: document.getElementById('addressReceiver').value,
                receiverPhone: document.getElementById('addressPhone').value,
                province: document.getElementById('addressProvince').value,
                city: document.getElementById('addressCity').value,
                district: document.getElementById('addressDistrict').value,
                detailAddress: document.getElementById('addressDetail').value,
                isDefault: document.getElementById('addressDefault').checked ? 1 : 0
            })
        });
        showToast('地址已保存');
        showAddresses();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function setDefaultAddress(addressId) {
    try {
        await request(`/addresses/${addressId}/default`, { method: 'PUT' });
        showToast('已设为默认地址');
        showAddresses();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function deleteAddress(addressId) {
    try {
        await request(`/addresses/${addressId}`, { method: 'DELETE' });
        showToast('地址已删除');
        showAddresses();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function submitFeedback() {
    try {
        await request('/feedbacks', {
            method: 'POST',
            body: JSON.stringify({
                productId: document.getElementById('feedbackProductId').value || null,
                orderId: document.getElementById('feedbackOrderId').value || null,
                type: 'PRODUCT',
                rating: Number(document.getElementById('feedbackRating').value),
                content: document.getElementById('feedbackContent').value
            })
        });
        showToast('反馈已提交');
        showFeedback();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

// ========================================
// 表单提交
// ========================================

document.getElementById('loginForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('loginUsername').value;
    const password = document.getElementById('loginPassword').value;
    await login(username, password);
});

document.getElementById('registerForm').addEventListener('submit', async (e) => {
    e.preventDefault();
    const username = document.getElementById('regUsername').value;
    const password = document.getElementById('regPassword').value;
    const nickname = document.getElementById('regNickname').value;
    const phone = document.getElementById('regPhone').value;
    await register(username, password, nickname, phone);
});

// 搜索框回车事件
document.getElementById('searchInput').addEventListener('keypress', (e) => {
    if (e.key === 'Enter') {
        searchProducts();
    }
});

// 点击外部关闭下拉菜单
document.addEventListener('click', (e) => {
    if (!e.target.closest('.user-menu')) {
        document.getElementById('userDropdown').classList.remove('show');
    }
});

// ========================================
// 页面初始化
// ========================================

async function init() {
    checkAuth();
    
    // 并行加载数据
    await Promise.all([
        loadBanners(),
        loadCategories(),
        loadHotProducts(),
        loadNewProducts(),
        loadAllProducts(),
        loadAnalytics(),
        loadFeedbackStats(),
        loadSupplierInsights()
    ]);
}

// 页面加载完成后初始化
document.addEventListener('DOMContentLoaded', init);
