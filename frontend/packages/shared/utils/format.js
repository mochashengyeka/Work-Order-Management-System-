function pad(n) {
    return String(n).padStart(2, '0')
}

export function formatTime(time) {
    if (!time) return '-'
    const d = new Date(time)
    if (isNaN(d.getTime())) return '-'
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

export function formatDate(time) {
    if (!time) return '-'
    const d = new Date(time)
    if (isNaN(d.getTime())) return '-'
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function getPriorityText(priority) {
    const map = { 1: '紧急', 2: '高', 3: '中', 4: '低' }
    return map[priority] || '未知'
}

export function getPriorityType(priority) {
    const map = { 1: 'danger', 2: 'warning', 3: 'primary', 4: 'info' }
    return map[priority] || 'info'
}

export function getStatusText(status) {
    const map = { 1: '待派单', 2: '待接单', 3: '处理中', 4: '待验收', 5: '已完成', 6: '已取消' }
    return map[status] || '未知'
}

export function getStatusType(status) {
    const map = { 1: 'info', 2: 'warning', 3: 'primary', 4: 'warning', 5: 'success', 6: 'danger' }
    return map[status] || 'info'
}

export function getRoleText(role) {
    const map = { 1: '报修人', 2: '工程师', 3: '管理员' }
    return map[role] || '未知'
}
