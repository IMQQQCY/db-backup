import request from './request.js'

export const pageTask = (params) => request.get('/task/page', { params })
export const getTask = (id) => request.get(`/task/${id}`)
export const saveTask = (data) => request.post('/task', data)
export const updateTask = (data) => request.put('/task', data)
export const deleteTask = (id) => request.delete(`/task/${id}`)
export const toggleTask = (id, enabled) => request.post(`/task/${id}/toggle?enabled=${enabled}`)
export const executeTask = (id) => request.post(`/task/${id}/execute`)
export const cronNextTimes = (expression, times = 5) => request.get('/task/cron/next', { params: { expression, times } })
