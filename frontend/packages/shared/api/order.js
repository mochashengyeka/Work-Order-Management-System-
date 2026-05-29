import request from './request'

export const reporterOrderApi = {
    create: (data) => request.post('/reporter/order/create', data),
    getMyOrders: (params) => request.get('/reporter/order/my', { params }),
    getDetail: (id) => request.get(`/reporter/order/detail/${id}`),
    confirm: (id) => request.post(`/reporter/order/confirm/${id}`),
    cancel: (id, reason) => request.post(`/reporter/order/cancel/${id}`, null, { params: { reason } }),
    urge: (id) => request.post(`/reporter/order/urge/${id}`),
    feedback: (id, data) => request.post(`/reporter/order/feedback/${id}`, data),
}

export const engineerOrderApi = {
    getOrderHall: (params) => request.get('/engineer/order/hall', { params }),
    grab: (id) => request.post(`/engineer/order/grab/${id}`),
    getMyOrders: (params) => request.get('/engineer/order/my', { params }),
    start: (id) => request.post(`/engineer/order/start/${id}`),
    finish: (id, data) => request.post(`/engineer/order/finish/${id}`, data),
    getParts: (params) => request.get('/engineer/order/parts', { params }),
}

export const adminOrderApi = {
    getList: (params) => request.get('/admin/order/list', { params }),
    assign: (id, engineerId) => request.post(`/admin/order/assign/${id}`, { engineerId }),
    getEngineers: () => request.get('/admin/order/engineers'),
    forceClose: (id, reason) => request.post(`/admin/order/force-close/${id}`, null, { params: { reason } }),
    getDashboard: () => request.get('/admin/order/dashboard'),
}
