import { defineStore } from 'pinia'
import { ref } from 'vue'
import { authApi } from '@shared/api/auth'

export const useUserStore = defineStore('user', () => {
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(null)

    const setToken = (newToken) => {
        token.value = newToken
        localStorage.setItem('token', newToken)
    }

    const logout = () => {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
    }

    const getUserInfo = async () => {
        if (!token.value) return null
        try {
            const res = await authApi.getInfo()
            userInfo.value = res.data
            localStorage.setItem('userInfo', JSON.stringify(res.data))
            return res.data
        } catch {
            logout()
            return null
        }
    }

    return { token, userInfo, setToken, logout, getUserInfo }
})
