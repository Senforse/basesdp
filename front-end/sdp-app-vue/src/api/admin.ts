import http, {unwrap} from './http'
import type {
    ApiEnvelope,
    AutoCodeGeneratePayload,
    AutoCodeGenerateResult,
    OnlineUserPageResult,
    SysDictItem,
    SysDictType,
    SysMenu,
    SysOrganization,
    SysRole,
    SysUser,
} from './types'

/** GET /api/admin/orgs/tree */
export async function fetchOrgTree(): Promise<SysOrganization[]> {
  return unwrap(await http.get<ApiEnvelope<SysOrganization[]>>('/api/admin/orgs/tree'))
}

/** GET /api/admin/orgs/:id */
export async function fetchOrgDetail(id: number): Promise<SysOrganization> {
  return unwrap(await http.get<ApiEnvelope<SysOrganization>>(`/api/admin/orgs/${id}`))
}

/** POST /api/admin/orgs */
export async function createOrg(body: SysOrganization): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/orgs', body))
}

/** PUT /api/admin/orgs/:id */
export async function updateOrg(id: number, body: SysOrganization): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/orgs/${id}`, body))
}

/** DELETE /api/admin/orgs/:id */
export async function deleteOrg(id: number): Promise<void> {
  unwrap(await http.delete<ApiEnvelope<null>>(`/api/admin/orgs/${id}`))
}

/** GET /api/admin/menus/tree */
export async function fetchMenuAdminTree(): Promise<SysMenu[]> {
  return unwrap(await http.get<ApiEnvelope<SysMenu[]>>('/api/admin/menus/tree'))
}

/** POST /api/admin/menus */
export async function createMenu(body: SysMenu): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/menus', body))
}

/** PUT /api/admin/menus/:id */
export async function updateMenu(id: number, body: SysMenu): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/menus/${id}`, body))
}

/** DELETE /api/admin/menus/:id */
export async function deleteMenu(id: number): Promise<void> {
  unwrap(await http.delete<ApiEnvelope<null>>(`/api/admin/menus/${id}`))
}

/** GET /api/admin/roles */
export async function fetchRoles(): Promise<SysRole[]> {
  return unwrap(await http.get<ApiEnvelope<SysRole[]>>('/api/admin/roles'))
}

/** GET /api/admin/roles/:id */
export async function fetchRoleDetail(id: number): Promise<SysRole> {
  return unwrap(await http.get<ApiEnvelope<SysRole>>(`/api/admin/roles/${id}`))
}

/** POST /api/admin/roles */
export async function createRole(body: SysRole): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/roles', body))
}

/** PUT /api/admin/roles/:id */
export async function updateRole(id: number, body: SysRole): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/roles/${id}`, body))
}

/** DELETE /api/admin/roles/:id */
export async function deleteRole(id: number): Promise<void> {
  unwrap(await http.delete<ApiEnvelope<null>>(`/api/admin/roles/${id}`))
}

/** GET /api/admin/roles/:id/menus */
export async function fetchRoleMenuIds(id: number): Promise<number[]> {
  return unwrap(await http.get<ApiEnvelope<number[]>>(`/api/admin/roles/${id}/menus`))
}

/** PUT /api/admin/roles/:id/menus body: { menuIds } */
export async function assignRoleMenus(id: number, menuIds: number[]): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/roles/${id}/menus`, { menuIds }))
}

/** GET /api/admin/users */
export async function fetchUsers(): Promise<SysUser[]> {
  return unwrap(await http.get<ApiEnvelope<SysUser[]>>('/api/admin/users'))
}

/** GET /api/admin/users/:id */
export async function fetchUserDetail(id: number): Promise<SysUser> {
  return unwrap(await http.get<ApiEnvelope<SysUser>>(`/api/admin/users/${id}`))
}

/** POST /api/admin/users */
export async function createUser(body: SysUser): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/users', body))
}

/** PUT /api/admin/users/:id */
export async function updateUser(id: number, body: SysUser): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/users/${id}`, body))
}

/** DELETE /api/admin/users/:id */
export async function deleteUser(id: number): Promise<void> {
  unwrap(await http.delete<ApiEnvelope<null>>(`/api/admin/users/${id}`))
}

/** GET /api/admin/users/:id/roles */
export async function fetchUserRoleIds(id: number): Promise<number[]> {
  return unwrap(await http.get<ApiEnvelope<number[]>>(`/api/admin/users/${id}/roles`))
}

/** PUT /api/admin/users/:id/roles body: { roleIds } */
export async function assignUserRoles(id: number, roleIds: number[]): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/users/${id}/roles`, { roleIds }))
}

/** GET /api/admin/online-users */
export async function fetchOnlineUsersPage(params: {
  application?: string
  userCode?: string
  page: number
  size: number
}): Promise<OnlineUserPageResult> {
  return unwrap(
    await http.get<ApiEnvelope<OnlineUserPageResult>>('/api/admin/online-users', {
      params: {
        application: params.application,
        userCode: params.userCode,
        page: params.page,
        size: params.size,
      },
    }),
  )
}

/** POST /api/admin/online-users/kick */
export async function kickOnlineSession(tokenValue: string): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/online-users/kick', { tokenValue }))
}

/** POST /api/admin/codegen/create-table-and-generate */
export async function createTableAndGenerate(body: AutoCodeGeneratePayload): Promise<AutoCodeGenerateResult> {
  return unwrap(await http.post<ApiEnvelope<AutoCodeGenerateResult>>('/api/admin/codegen/create-table-and-generate', body))
}

/** GET /api/admin/codegen/frontend-packages/:token */
export async function downloadFrontendPageZip(token: string): Promise<Blob> {
  const res = await http.get(`/api/admin/codegen/frontend-packages/${token}`, {
    responseType: 'blob',
  })
  return res.data as Blob
}

/** GET /api/auth/permissions */
export async function fetchMyPermissions(): Promise<string[]> {
  return unwrap(await http.get<ApiEnvelope<string[]>>('/api/auth/permissions'))
}

/** GET /api/admin/dict/types */
export async function fetchDictTypes(): Promise<SysDictType[]> {
  return unwrap(await http.get<ApiEnvelope<SysDictType[]>>('/api/admin/dict/types'))
}

/** GET /api/admin/dict/types/:id */
export async function fetchDictTypeDetail(id: number): Promise<SysDictType> {
  return unwrap(await http.get<ApiEnvelope<SysDictType>>(`/api/admin/dict/types/${id}`))
}

/** POST /api/admin/dict/types */
export async function createDictType(body: SysDictType): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/dict/types', body))
}

/** PUT /api/admin/dict/types/:id */
export async function updateDictType(id: number, body: SysDictType): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/dict/types/${id}`, body))
}

/** DELETE /api/admin/dict/types/:id */
export async function deleteDictType(id: number): Promise<void> {
  unwrap(await http.delete<ApiEnvelope<null>>(`/api/admin/dict/types/${id}`))
}

/** GET /api/admin/dict/items?dictTypeId=xxx */
export async function fetchDictItemsByType(dictTypeId: number): Promise<SysDictItem[]> {
  return unwrap(await http.get<ApiEnvelope<SysDictItem[]>>('/api/admin/dict/items', { params: { dictTypeId } }))
}

/** GET /api/admin/dict/items/:id */
export async function fetchDictItemDetail(id: number): Promise<SysDictItem> {
  return unwrap(await http.get<ApiEnvelope<SysDictItem>>(`/api/admin/dict/items/${id}`))
}

/** POST /api/admin/dict/items */
export async function createDictItem(body: SysDictItem): Promise<void> {
  unwrap(await http.post<ApiEnvelope<null>>('/api/admin/dict/items', body))
}

/** PUT /api/admin/dict/items/:id */
export async function updateDictItem(id: number, body: SysDictItem): Promise<void> {
  unwrap(await http.put<ApiEnvelope<null>>(`/api/admin/dict/items/${id}`, body))
}

/** DELETE /api/admin/dict/items/:id */
export async function deleteDictItem(id: number): Promise<void> {
  unwrap(await http.delete<ApiEnvelope<null>>(`/api/admin/dict/items/${id}`))
}

/** GET /api/admin/dict/items/by-code/:dictCode */
export async function fetchDictByCode(dictCode: string): Promise<SysDictItem[]> {
  return unwrap(await http.get<ApiEnvelope<SysDictItem[]>>(`/api/admin/dict/items/by-code/${dictCode}`))
}
