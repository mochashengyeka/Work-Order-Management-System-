import request from './request'

export const authApi = {
    login: (data) => request.post('/auth/login', data),
    logout: () => request.post('/auth/logout'),
    getInfo: () => request.get('/auth/info'),
    register: (data) => request.post('/auth/register', data),
}
