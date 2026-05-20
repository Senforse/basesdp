/**
 * 首页仪表盘数据（预留 HTTP，当前为占位 Mock，后续替换为真实接口）。
 */
export interface DashboardDelta {
  yesterday: number
}

export interface DashboardOverview {
  summary: {
    systems: number
    databases: number
    tables: number
    items: number
    deltas: Record<'systems' | 'databases' | 'tables' | 'items', DashboardDelta>
  }
  listing: {
    tables: number
    items: number
    rate: number
    deltas: Record<'tables' | 'items', DashboardDelta>
  }
  systems: Array<{ id: string; name: string; count: number }>
  dbTypes: Array<{ name: string; value: number; color: string }>
  tableTypes: Array<{ name: string; value: number; color: string }>
  folders: Array<{ id: string; label: string; count: number }>
  interfaces: { total: number; ok: number; err: number }
  trends: Array<{ rank: number; label: string; total: number; yesterday: number; spark: number[] }>
}

const MOCK: DashboardOverview = {
  summary: {
    systems: 63,
    databases: 111,
    tables: 17939,
    items: 2458012,
    deltas: {
      systems: { yesterday: 2 },
      databases: { yesterday: 3 },
      tables: { yesterday: 128 },
      items: { yesterday: 9024 },
    },
  },
  listing: {
    tables: 8200,
    items: 1200400,
    rate: 40,
    deltas: { tables: { yesterday: 55 }, items: { yesterday: 3100 } },
  },
  systems: [
    { id: '1', name: '业务系统 A', count: 12 },
    { id: '2', name: '业务系统 B', count: 9 },
    { id: '3', name: '支撑平台', count: 18 },
    { id: '4', name: '开放平台', count: 7 },
    { id: '5', name: '运维监控', count: 11 },
    { id: '6', name: '数据分析', count: 6 },
  ],
  dbTypes: [
    { name: 'Oracle', value: 38, color: '#38bdf8' },
    { name: 'MySQL', value: 42, color: '#22d3ee' },
    { name: 'DM', value: 21, color: '#818cf8' },
    { name: '其他', value: 10, color: '#94a3b8' },
  ],
  tableTypes: [
    { name: '业务表', value: 62, color: '#3b82f6' },
    { name: '备份表', value: 18, color: '#64748b' },
    { name: '日志表', value: 14, color: '#22c55e' },
    { name: '临时表', value: 6, color: '#f59e0b' },
  ],
  folders: [
    { id: 'f1', label: '标准模型目录', count: 42 },
    { id: 'f2', label: '主题域目录', count: 28 },
    { id: 'f3', label: '接口资产目录', count: 16 },
    { id: 'f4', label: '临时归档', count: 7 },
  ],
  interfaces: { total: 14, ok: 14, err: 0 },
  trends: [
    { rank: 1, label: '核心交换接口', total: 13234144, yesterday: 12040, spark: [12, 14, 13, 18, 22, 20, 25] },
    { rank: 2, label: '订阅分发接口', total: 8210332, yesterday: 8031, spark: [8, 9, 8, 11, 10, 12, 14] },
    { rank: 3, label: '查询服务接口', total: 5100299, yesterday: 4022, spark: [20, 18, 17, 19, 21, 22, 24] },
    { rank: 4, label: '文件传输接口', total: 2100888, yesterday: 910, spark: [5, 6, 5, 7, 8, 7, 9] },
    { rank: 5, label: '异步回调接口', total: 980044, yesterday: 440, spark: [3, 4, 4, 5, 5, 6, 5] },
  ],
}

/** 后续改为 http.get('/api/dashboard/overview') */
export async function fetchDashboardOverview(): Promise<DashboardOverview> {
  await new Promise((r) => setTimeout(r, 120))
  return JSON.parse(JSON.stringify(MOCK)) as DashboardOverview
}
