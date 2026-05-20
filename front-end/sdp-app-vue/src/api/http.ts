import axios, { type AxiosError, type AxiosResponse } from 'axios'
import type { ApiEnvelope } from './types'

const http = axios.create({
  baseURL: '',
  timeout: 60000,
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  const headerName = localStorage.getItem('tokenHeaderName') || 'satoken'
  if (token && headerName) {
    config.headers = config.headers ?? {}
    ;(config.headers as Record<string, string>)[headerName] = token
  }
  return config
})

http.interceptors.response.use(
  (res) => res,
  (err: AxiosError<ApiEnvelope<unknown>>) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('token')
      localStorage.removeItem('tokenHeaderName')
      localStorage.removeItem('user')
      const path = window.location.pathname || ''
      if (!path.endsWith('/login') && path !== '/login') {
        window.location.href = '/login'
      }
    }
    return Promise.reject(err)
  },
)

export function unwrap<T>(res: AxiosResponse<ApiEnvelope<T>>): T {
  const body = res.data
  if (body.code !== 0) {
    throw new Error(body.message || '请求失败')
  }
  return body.data as T
}

export default http
