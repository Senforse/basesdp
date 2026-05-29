/** 与后端 ApiResponse 一致 */
export interface ApiEnvelope<T> {
  code: number
  message?: string
  data: T
}

export interface SysOrganization {
  id?: number
  parentId: number
  orgCode: string
  orgName: string
  sortOrder?: number
  status?: number
  children?: SysOrganization[]
}

export interface SysMenu {
  id?: number
  parentId: number
  menuName: string
  path?: string | null
  component?: string | null
  perms?: string | null
  menuType?: number
  icon?: string | null
  sortOrder?: number
  visible?: number
  status?: number
  children?: SysMenu[]
}

export interface SysRole {
  id?: number
  roleCode: string
  roleName: string
  sortOrder?: number
  status?: number
}

export interface SysUser {
  id?: number
  username: string
  password?: string
  displayName?: string
  status?: number
  deptId?: number | null
}

/** 在线会话行（与后端 OnlineUserVO 一致） */
export interface OnlineUserVO {
  tokenValue: string
  displayName?: string
  username?: string
  userType?: string
  application?: string
  authType?: string
  expireTime?: string
}

export interface OnlineUserPageResult {
  records: OnlineUserVO[]
  total: number
  current: number
  size: number
}

export interface AutoCodeColumnPayload {
  columnName: string
  comment?: string
  dbType: string
  length?: number
  scale?: number
  nullable?: boolean
  primaryKey?: boolean
}

export interface AutoCodeGeneratePayload {
  tableName: string
  tableComment?: string
  modulePackage?: string
  columns: AutoCodeColumnPayload[]
}

export interface AutoCodeGenerateResult {
  createTableSql: string
  entityClassName: string
  mapperClassName: string
  serviceClassName: string
  controllerClassName: string
  generatedFiles: string[]
  frontendGeneratedFiles: string[]
  frontendZipToken?: string
  frontendZipDownloadUrl?: string
}

export interface SysDictType {
  id?: number
  dictCode: string
  dictName: string
  description?: string
  sortOrder?: number
  status?: number
}

export interface SysDictItem {
  id?: number
  dictTypeId: number
  dictCode: string
  itemLabel: string
  itemValue: string
  sortOrder?: number
  status?: number
  description?: string
}
