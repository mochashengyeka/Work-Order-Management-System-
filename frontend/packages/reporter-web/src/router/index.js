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
        redirect: '/order/my',
    },
    {
        path: '/order/create',
        name: 'CreateOrder',
        component: () => import('@/views/order/CreateOrder.vue'),
        meta: { title: '创建工单', requiresAuth: true },
    },
    {
        path: '/order/my',
        name: 'MyOrders',
        component: () => import('@/views/order/MyOrders.vue'),
        meta: { title: '我的工单', requiresAuth: true },
    },
    {
        path: '/order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/OrderDetail.vue'),
        meta: { title: '工单详情', requiresAuth: true },
    },
    {
        path: '/profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', requiresAuth: true },
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes,
})

router.beforeEach((to, from, next) => {
    if (to.query.token && to.query.userInfo) {
        localStorage.setItem('token', to.query.token)
        localStorage.setItem('userInfo', decodeURIComponent(to.query.userInfo))
        next({ path: to.path, replace: true })
        return
    }

    const token = localStorage.getItem('token')
    const userInfoStr = localStorage.getItem('userInfo')
    const userInfo = userInfoStr ? JSON.parse(userInfoStr) : null

    if (to.meta.requiresAuth && !token) {
        next('/login')
        return
    }

    if (token && userInfo && userInfo.role !== 1) {
        const urls = { 2: 'http://localhost:5174', 3: 'http://localhost:5175' }
        window.location.href = urls[userInfo.role]
        return
    }

    next()
})

export default router
