import { createRouter, createWebHistory } from 'vue-router'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录' },
    },
    {
        path: '/',
        redirect: '/hall',
    },
    {
        path: '/hall',
        name: 'OrderHall',
        component: () => import('@/views/order/OrderHall.vue'),
        meta: { title: '接单大厅', requiresAuth: true },
    },
    {
        path: '/tasks',
        name: 'MyTasks',
        component: () => import('@/views/order/MyTasks.vue'),
        meta: { title: '我的工单', requiresAuth: true },
    },
    {
        path: '/order/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '工单详情', requiresAuth: true },
    },
    {
        path: '/parts',
        name: 'PartsList',
        component: () => import('@/views/parts/PartsList.vue'),
        meta: { title: '备件库存', requiresAuth: true },
    },
    {
        path: '/statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '我的统计', requiresAuth: true },
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
    // 从统一登录页(5172)跳回时，通过URL参数传递token
    if (to.query.token && to.query.userInfo) {
        localStorage.setItem('token', to.query.token)
        localStorage.setItem('userInfo', decodeURIComponent(to.query.userInfo))
        next({ path: to.path, replace: true })
        return
    }

    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')
    const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null

    // 未登录 → 显示本App的登录页（token存同源localStorage，API请求更可靠）
    if (to.meta.requiresAuth && !token) {
        next('/login')
        return
    }

    // 已登录但角色不匹配 → 重定向到对应App
    if (token && userInfo && userInfo.role !== 2) {
        const urls = { 1: 'http://localhost:5173', 3: 'http://localhost:5175' }
        window.location.href = urls[userInfo.role]
        return
    }

    next()
})

export default router
