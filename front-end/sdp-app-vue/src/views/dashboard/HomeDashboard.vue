<script setup lang="ts">
import { Folder, Monitor, Ship, TrendCharts, WarningFilled } from '@element-plus/icons-vue'
import { onMounted, ref } from 'vue'
import { fetchDashboardOverview, type DashboardOverview } from '@/api/dashboard'

const loading = ref(true)
const data = ref<DashboardOverview | null>(null)

onMounted(async () => {
  loading.value = true
  try {
    data.value = await fetchDashboardOverview()
  } finally {
    loading.value = false
  }
})

function donutGradient(items: Array<{ value: number; color: string }>) {
  const sum = items.reduce((s, i) => s + i.value, 0) || 1
  let acc = 0
  const parts = items.map((i) => {
    const start = (acc / sum) * 360
    acc += i.value
    const end = (acc / sum) * 360
    return `${i.color} ${start}deg ${end}deg`
  })
  return `conic-gradient(${parts.join(', ')})`
}

function sparkPath(points: number[]) {
  if (!points.length) return ''
  const max = Math.max(...points, 1)
  const w = 88
  const h = 28
  return points
    .map((p, i) => {
      const x = (i / (points.length - 1 || 1)) * w
      const y = h - (p / max) * h
      return `${i === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)}`
    })
    .join(' ')
}
</script>

<template>
  <div v-loading="loading" class="dash-page sdp-page">
    <template v-if="data">
      <el-row :gutter="16" class="dash-row">
        <el-col :xs="24" :lg="16">
          <div class="dash-hero sdp-dash-animate" style="animation-delay: 0s">
            <div class="dash-hero-grid">
              <div class="dash-metric">
                <span class="dash-metric-label">系统数</span>
                <span class="dash-metric-num">{{ data.summary.systems }}</span>
                <span class="dash-metric-delta">昨日 +{{ data.summary.deltas.systems.yesterday }}</span>
              </div>
              <div class="dash-metric">
                <span class="dash-metric-label">数据库</span>
                <span class="dash-metric-num">{{ data.summary.databases }}</span>
                <span class="dash-metric-delta">昨日 +{{ data.summary.deltas.databases.yesterday }}</span>
              </div>
              <div class="dash-metric">
                <span class="dash-metric-label">表数量</span>
                <span class="dash-metric-num">{{ data.summary.tables }}</span>
                <span class="dash-metric-delta">昨日 +{{ data.summary.deltas.tables.yesterday }}</span>
              </div>
              <div class="dash-metric">
                <span class="dash-metric-label">数据项</span>
                <span class="dash-metric-num">{{ data.summary.items }}</span>
                <span class="dash-metric-delta">昨日 +{{ data.summary.deltas.items.yesterday }}</span>
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="24" :lg="8">
          <el-card class="sdp-panel dash-side sdp-dash-animate" shadow="never" style="animation-delay: 0.08s">
            <div class="dash-side-inner">
              <div>
                <p class="dash-side-label">上架表</p>
                <p class="dash-side-num">{{ data.listing.tables }}</p>
                <p class="dash-side-y">昨日 +{{ data.listing.deltas.tables.yesterday }}</p>
              </div>
              <div>
                <p class="dash-side-label">上架数据项</p>
                <p class="dash-side-num">{{ data.listing.items }}</p>
                <p class="dash-side-y">昨日 +{{ data.listing.deltas.items.yesterday }}</p>
              </div>
              <div class="dash-ring-wrap">
                <el-progress type="dashboard" :percentage="data.listing.rate" :width="120" color="var(--primary-color)">
                  <template #default="{ percentage }">
                    <span class="dash-ring-text">{{ percentage }}%</span>
                    <span class="dash-ring-sub">上架率</span>
                  </template>
                </el-progress>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-card class="sdp-panel dash-mid sdp-dash-animate" shadow="never" style="animation-delay: 0.12s">
        <el-row :gutter="20">
          <el-col :xs="24" :md="8">
            <h3 class="dash-block-title">系统分布</h3>
            <div class="dash-sys-grid">
              <div
                v-for="(s, i) in data.systems"
                :key="s.id"
                class="dash-sys-cell sdp-dash-animate"
                :style="{ animationDelay: `${0.05 * i}s` }"
              >
                <el-icon class="dash-sys-ic"><Monitor /></el-icon>
                <span class="dash-sys-name">{{ s.name }}</span>
                <span class="dash-sys-count">{{ s.count }}</span>
              </div>
            </div>
          </el-col>
          <el-col :xs="24" :md="8">
            <h3 class="dash-block-title">数据库类型</h3>
            <div class="dash-donut-row">
              <div class="dash-donut" :style="{ background: donutGradient(data.dbTypes) }">
                <div class="dash-donut-hole">
                  <span class="dash-donut-total">{{ data.summary.databases }}</span>
                  <span class="dash-donut-sub">总量</span>
                </div>
              </div>
              <ul class="dash-legend">
                <li v-for="d in data.dbTypes" :key="d.name">
                  <i class="dash-dot" :style="{ background: d.color }" /> {{ d.name }} · {{ d.value }}%
                </li>
              </ul>
            </div>
          </el-col>
          <el-col :xs="24" :md="8">
            <h3 class="dash-block-title">表类型</h3>
            <div class="dash-donut-row">
              <div class="dash-donut" :style="{ background: donutGradient(data.tableTypes) }">
                <div class="dash-donut-hole">
                  <span class="dash-donut-total">{{ data.summary.tables }}</span>
                  <span class="dash-donut-sub">表合计</span>
                </div>
              </div>
              <ul class="dash-legend">
                <li v-for="t in data.tableTypes" :key="t.name">
                  <i class="dash-dot" :style="{ background: t.color }" /> {{ t.name }} · {{ t.value }}%
                </li>
              </ul>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <el-row :gutter="16" class="dash-row">
        <el-col :xs="24" :lg="8">
          <el-card class="sdp-panel sdp-dash-animate" shadow="never" style="animation-delay: 0.16s">
            <template #header>
              <span class="sdp-panel-title">目录 · {{ data.folders.length }}</span>
            </template>
            <ul class="dash-folder-list">
              <li v-for="(f, i) in data.folders" :key="f.id" class="sdp-dash-animate" :style="{ animationDelay: `${0.04 * i}s` }">
                <el-icon class="dash-folder-ic"><Folder /></el-icon>
                <span class="dash-folder-label">{{ f.label }}</span>
                <span class="dash-folder-count">{{ f.count }}</span>
              </li>
            </ul>
          </el-card>
        </el-col>
        <el-col :xs="24" :lg="16">
          <el-card class="sdp-panel sdp-dash-animate" shadow="never" style="animation-delay: 0.2s">
            <template #header>
              <span class="sdp-panel-title">接口信息</span>
            </template>
            <div class="dash-api-grid">
              <div class="dash-api-status">
                <div class="dash-st dash-st-blue">
                  <el-icon><TrendCharts /></el-icon>
                  <div>
                    <p class="dash-st-label">接口总数</p>
                    <p class="dash-st-num">{{ data.interfaces.total }}</p>
                  </div>
                </div>
                <div class="dash-st dash-st-green">
                  <el-icon><Ship /></el-icon>
                  <div>
                    <p class="dash-st-label">运行正常</p>
                    <p class="dash-st-num">{{ data.interfaces.ok }}</p>
                  </div>
                </div>
                <div class="dash-st dash-st-red">
                  <el-icon><WarningFilled /></el-icon>
                  <div>
                    <p class="dash-st-label">异常</p>
                    <p class="dash-st-num">{{ data.interfaces.err }}</p>
                  </div>
                </div>
              </div>
              <div class="dash-trends">
                <p class="dash-trends-title">调用量趋势 TOP</p>
                <div v-for="t in data.trends" :key="t.rank" class="dash-trend-row">
                  <span class="dash-tr-rank">{{ t.rank }}</span>
                  <div class="dash-tr-main">
                    <span class="dash-tr-label">{{ t.label }}</span>
                    <span class="dash-tr-total">{{ t.total.toLocaleString() }}</span>
                    <span class="dash-tr-y">昨日 +{{ t.yesterday.toLocaleString() }}</span>
                  </div>
                  <svg class="dash-spark" viewBox="0 0 88 28" preserveAspectRatio="none">
                    <path :d="sparkPath(t.spark)" fill="none" stroke="var(--primary-color)" stroke-width="1.5" />
                  </svg>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<style lang="scss" scoped>
.dash-page {
  padding-bottom: 24px;
}
.dash-row {
  margin-bottom: 16px;
}
.dash-hero {
  border-radius: 12px;
  padding: 20px 24px;
  background: var(--hero-gradient);
  box-shadow: var(--hero-shadow);
  min-height: 148px;
}
.dash-hero-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
@media (max-width: 992px) {
  .dash-hero-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.dash-metric-label {
  display: block;
  font-size: 13px;
  color: var(--on-primary);
  opacity: 0.85;
}
.dash-metric-num {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: var(--on-primary);
  letter-spacing: -0.5px;
}
.dash-metric-delta {
  font-size: 12px;
  color: var(--on-primary);
  opacity: 0.9;
}
.dash-side :deep(.el-card__body) {
  padding: 16px;
}
.dash-side-inner {
  display: grid;
  grid-template-columns: 1fr 1fr auto;
  gap: 12px;
  align-items: center;
}
@media (max-width: 576px) {
  .dash-side-inner {
    grid-template-columns: 1fr;
  }
}
.dash-side-label {
  margin: 0;
  font-size: 13px;
  color: var(--text-muted);
}
.dash-side-num {
  margin: 4px 0 0;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-color);
}
.dash-side-y {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--danger-color);
}
.dash-ring-wrap {
  justify-self: end;
}
.dash-ring-text {
  display: block;
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
}
.dash-ring-sub {
  font-size: 12px;
  color: var(--text-muted);
}
.dash-mid :deep(.el-card__body) {
  padding: 16px 20px 20px;
}
.dash-block-title {
  margin: 0 0 12px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-color);
}
.dash-sys-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
@media (max-width: 576px) {
  .dash-sys-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
.dash-sys-cell {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  padding: 12px;
  border-radius: 10px;
  background: var(--card-soft-bg);
  border: 1px solid var(--primary-soft);
  transition:
    transform 0.2s,
    box-shadow 0.2s;
}
.dash-sys-cell:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 24px var(--primary-soft);
}
.dash-sys-ic {
  font-size: 20px;
  color: var(--primary-color);
}
.dash-sys-name {
  font-size: 13px;
  color: var(--text-weak);
}
.dash-sys-count {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-color);
}
.dash-donut-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}
.dash-donut {
  width: 132px;
  height: 132px;
  border-radius: 50%;
  flex-shrink: 0;
  padding: 14px;
  box-sizing: border-box;
}
.dash-donut-hole {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: var(--ring-hole-bg);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}
.dash-donut-total {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-color);
}
.dash-donut-sub {
  font-size: 11px;
  color: var(--text-muted);
}
.dash-legend {
  list-style: none;
  margin: 0;
  padding: 0;
  font-size: 13px;
  color: var(--text-weak);
  line-height: 1.7;
}
.dash-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 6px;
}
.dash-folder-list {
  list-style: none;
  margin: 0;
  padding: 0;
}
.dash-folder-list li {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 4px;
  border-bottom: 1px solid var(--primary-soft);
  transition: background 0.15s;
}
.dash-folder-list li:last-child {
  border-bottom: none;
}
.dash-folder-list li:hover {
  background: var(--primary-soft);
  border-radius: 8px;
}
.dash-folder-ic {
  color: var(--primary-color);
  font-size: 20px;
}
.dash-folder-label {
  flex: 1;
  color: var(--text-weak);
  font-size: 14px;
}
.dash-folder-count {
  font-weight: 600;
  color: var(--text-color);
}
.dash-api-grid {
  display: grid;
  grid-template-columns: 200px 1fr;
  gap: 20px;
}
@media (max-width: 768px) {
  .dash-api-grid {
    grid-template-columns: 1fr;
  }
}
.dash-api-status {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.dash-st {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 10px;
  border: 1px solid var(--panel-border);
}
.dash-st .el-icon {
  font-size: 22px;
}
.dash-st-blue {
  background: var(--primary-soft);
  color: var(--primary-color);
}
.dash-st-green {
  background: color-mix(in srgb, var(--success-color) 15%, transparent);
  color: var(--success-color);
}
.dash-st-red {
  background: color-mix(in srgb, var(--danger-color) 15%, transparent);
  color: var(--danger-color);
}
.dash-st-label {
  margin: 0;
  font-size: 12px;
  color: var(--text-muted);
}
.dash-st-num {
  margin: 2px 0 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-color);
}
.dash-trends-title {
  margin: 0 0 12px;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-color);
}
.dash-trend-row {
  display: grid;
  grid-template-columns: 24px 1fr 92px;
  align-items: center;
  gap: 10px;
  padding: 10px 0;
  border-bottom: 1px solid var(--primary-soft);
}
.dash-trend-row:last-child {
  border-bottom: none;
}
.dash-tr-rank {
  font-weight: 700;
  color: var(--primary-color);
}
.dash-tr-main {
  display: flex;
  flex-wrap: wrap;
  align-items: baseline;
  gap: 8px;
}
.dash-tr-label {
  color: var(--text-weak);
  font-size: 13px;
}
.dash-tr-total {
  font-weight: 600;
  color: var(--text-color);
  font-size: 14px;
}
.dash-tr-y {
  font-size: 12px;
  color: var(--danger-color);
}
.dash-spark {
  width: 88px;
  height: 28px;
  opacity: 0.95;
}

@keyframes sdp-dash-float {
  0%,
  100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-5px);
  }
}
.sdp-dash-animate {
  animation: sdp-dash-float 5.5s ease-in-out infinite;
}
</style>
