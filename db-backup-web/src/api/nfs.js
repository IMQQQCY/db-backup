import request from './request.js'

export const pageNfs = (params) => request.get('/nfs/page', { params })
export const listNfs = () => request.get('/nfs/list')
export const getNfs = (id) => request.get(`/nfs/${id}`)
export const saveNfs = (data) => request.post('/nfs', data)
export const updateNfs = (data) => request.put('/nfs', data)
export const deleteNfs = (id) => request.delete(`/nfs/${id}`)
export const mountNfs = (id) => request.post(`/nfs/${id}/mount`)
export const unmountNfs = (id) => request.post(`/nfs/${id}/unmount`)
export const statusNfs = (id) => request.get(`/nfs/${id}/status`)
