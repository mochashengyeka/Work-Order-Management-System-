// 常量定义 - 角色、状态、优先级枚举值

export const ROLE = {
    REPORTER: 1,
    ENGINEER: 2,
    ADMIN: 3,
}

export const ORDER_STATUS = {
    PENDING_ASSIGN: 1,
    PENDING_ACCEPT: 2,
    PROCESSING: 3,
    PENDING_CONFIRM: 4,
    COMPLETED: 5,
    CANCELLED: 6,
}

export const PRIORITY = {
    URGENT: 1,
    HIGH: 2,
    MEDIUM: 3,
    LOW: 4,
}

export const ROLE_OPTIONS = [
    { label: '报修人', value: 1, icon: 'User', desc: '创建报修工单，跟踪维修进度' },
    { label: '工程师', value: 2, icon: 'Tools', desc: '接单处理，提交维修结果' },
    { label: '管理员', value: 3, icon: 'Setting', desc: '工单管理，数据统计，系统配置' },
]
