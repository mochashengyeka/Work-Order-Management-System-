import request from './request'

export const userApi = {
    getList: (params) => request.get('/admin/user/list', { params }),
    updateStatus: (id, status) => request.put(`/admin/user/status/${id}?status=${status}`),
    add: (data) => request.post('/admin/user/add', data),
    update: (data) => request.put('/admin/user/update', data),
    delete: (id) => request.delete(`/admin/user/delete/${id}`),
}
