import axios from 'axios'

const service = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
    timeout: 15000,
})

service.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    (error) => Promise.reject(error)
)

service.interceptors.response.use(
    (response) => {
        const res = response.data
        const newToken = response.headers['x-refresh-token']
        if (newToken) localStorage.setItem('token', newToken)
        if (res.code !== 200) {
            return Promise.reject(new Error(res.message || '请求失败'))
        }
        return res
    },
    (error) => {
        if (error.response?.status === 401) {
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            window.location.href = 'http://localhost:5172/login'
            return Promise.reject(new Error('登录已过期'))
        }
        return Promise.reject(error)
    }
)

export default service
