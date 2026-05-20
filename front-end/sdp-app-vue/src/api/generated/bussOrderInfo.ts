import http, { unwrap } from '../http'
import type { ApiEnvelope } from '../types'

export interface BussOrderInfo {
  id?: string
  creator?: string
  createTime?: string
  updater?: string
  updateTime?: string
  orderNo?: string
}

export async function fetchBussOrderInfoList(): Promise<BussOrderInfo[]> {
  return unwrap(await http.get<ApiEnvelope<BussOrderInfo[]>>('/api/admin/buss_order_info'))
}

export async function fetchBussOrderInfoDetail(id: string | number): Promise<BussOrderInfo> {
  return unwrap(await http.get<ApiEnvelope<BussOrderInfo>>(`/api/admin/buss_order_info/${id}`))
}

export async function createBussOrderInfo(body: BussOrderInfo): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/buss_order_info', body))
}

export async function updateBussOrderInfo(id: string | number, body: BussOrderInfo): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/buss_order_info/${id}`, body))
}

export async function deleteBussOrderInfo(id: string | number): Promise<void> {
  unwrap(await http.delete<ApiEnvelope<null>>(`/api/admin/buss_order_info/${id}`))
}
