import request from './request.js'

export const getMailConfig = () => request.get('/mail')
export const saveMailConfig = (data) => request.post('/mail', data)
export const testMail = (data) => request.post('/mail/test', data)
