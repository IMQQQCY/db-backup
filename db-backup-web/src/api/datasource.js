import request from './request.js'

export const pageDataSource = (params) => request.get('/datasource/page', { params })
export const listDataSource = () => request.get('/datasource/list')
export const getDataSource = (id) => request.get(`/datasource/${id}`)
export const saveDataSource = (data) => request.post('/datasource', data)
export const updateDataSource = (data) => request.put('/datasource', data)
export const deleteDataSource = (id) => request.delete(`/datasource/${id}`)
export const testConnection = (data) => request.post('/datasource/test', data)
export const getTableList = (id) => request.get(`/datasource/${id}/tables`)
export const getTableInfoPage = (id, params) => request.get(`/datasource/${id}/tables/page`, { params })
