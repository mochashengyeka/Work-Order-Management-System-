import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/views/layout/index.vue'

const routes = [
    {
        path: '/login',
        name: 'Login',
        component: () => import('@/views/login/index.vue'),
        meta: { title: '登录' },
    },
    {
        path: '/',
        component: Layout,
        redirect: '/dashboard',
        children: [
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/index.vue'),
                meta: { title: '仪表盘', icon: 'DataLine' },
            },
            {
                path: 'order',
                name: 'OrderManage',
                redirect: '/order/list',
                meta: { title: '工单管理', icon: 'Document' },
                children: [
                    {
                        path: 'list',
                        name: 'OrderList',
                        component: () => import('@/views/order/OrderList.vue'),
                        meta: { title: '工单列表' },
                    },
                    {
                        path: 'detail/:id',
                        name: 'OrderDetail',
                        component: () => import('@/views/order/OrderDetail.vue'),
                        meta: { title: '工单详情', hidden: true },
                    },
                ],
            },
            {
                path: 'user',
                name: 'UserManage',
                component: () => import('@/views/user/UserList.vue'),
                meta: { title: '用户管理', icon: 'User' },
            },
            {
                path: 'part',
                name: 'PartManage',
                component: () => import('@/views/part/PartList.vue'),
                meta: { title: '备件管理', icon: 'Box' },
            },
        ],
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

    if (!token) {
        next('/login')
        return
    }

    if (userInfo && userInfo.role !== 3) {
        const urls = { 1: 'http://localhost:5173', 2: 'http://localhost:5174' }
        window.location.href = urls[userInfo.role]
        return
    }

    next()
})

export default router
