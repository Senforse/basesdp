<template>
  <div class="sdp-page">
    <el-card class="sdp-panel biz-card" shadow="never">
      <template #header>
        <span class="sdp-panel-title">排产计划</span>
      </template>
      <p class="biz-desc">排产计划业务模块占位页，可根据实际 API 接入甘特图或日历视图。</p>
    </el-card>
  </div>
</template>

<style lang="scss" scoped>
.biz-desc {
  margin: 0;
  color: var(--text-muted);
  line-height: 1.6;
}
.biz-card {
  min-height: 200px;
}
</style>
