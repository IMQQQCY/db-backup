import request from './request.js'

export const pageHistory = (params) => request.get('/history/page', { params })
export const deleteHistory = (id) => request.delete(`/history/${id}`)
