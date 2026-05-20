<template>
  <div class="sdp-page">
    <el-card class="sdp-panel biz-card" shadow="never">
      <template #header>
        <span class="sdp-panel-title">生产工单</span>
      </template>
      <p class="biz-desc">生产工单业务模块占位页，可与后端业务接口对接后扩展列表与流程。</p>
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
