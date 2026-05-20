## 1. 规范的样式目录推荐在 
Vite 项目中，建议将全局样式统一放在 src/assets/styles/ 下。这样可以享受 Vite 对静态资源的深度优化（如自动资产哈希、内联优化）：

~~~
src/
├── assets/
│   └── styles/
│       ├── index.scss       # 样式总主控文件（引入 reset、common 等）
│       ├── reset.scss       # 样式重置（如 normalize.scss）
│       ├── common.scss      # 全局公共类（如 .flex-between, .text-ellipsis）
│       ├── variables.scss   # 纯 SCSS 变量（主题色、字号）
│       └── mixins.scss      # SCSS 混入（清除浮动、媒体查询宏）
~~~

## 2、局部组件样式：
利用 SFC 
`<style scoped>`
对于 `views/ `和 `components/` 下的 Vue 单文件组件（SFC），样式直接写在组件内部。
强制添加 scoped：通过 Vite 的插件系统，`<style scoped>` 会被自动编译并带上独一无二的 `data-v-xxxxxx` 属性，防止组件间样式污染。使用预处理器：在单文件组件中直接声明 `lang="scss"`，Vite 会自动识别。
```html
<!-- src/components/MyButton.vue -->
   <template>
   <button class="my-btn">点击</button>
   </template>

<style lang="scss" scoped>
.my-btn {
  // 可以直接使用下文配置的全局变量
  background-color: $primary-color; 
  padding: 10px 20px;
}
</style>

```


# 3. Vite 核心配置：自动注入全局变量
如果在组件里频繁手动 
`@import "@/assets/styles/variables.scss"`，代码会变得非常冗余。通过修改根目录下的 `vite.config.js`（或 `vite.config.ts`），可以实现变量与 Mixin 的全局自动注入：
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
plugins: [vue()],
resolve: {
alias: {
// 设置路径别名，方便引入样式
'@': path.resolve(__dirname, './src')
}
},
css: {
preprocessorOptions: {
scss: {
// 自动将全局变量和混入注入到每一个 Vue 组件的 <style> 中
// 注意末尾的分号 ; 不能省略！
additionalData: `
          @use "@/assets/styles/variables.scss" as *;
          @use "@/assets/styles/mixins.scss" as *;
        `
}
}
}
})

```
# 4. 项目入口引入全局基础样式
对于不需要变量注入、但需要全局生效的样式（如重置样式、公共类），在 `src/main.js`（或 `main.ts`）中直接引入：
```javascript
import { createApp } from 'vue'
import App from './App.vue'

// 引入全局主控样式（内部 @import 了 reset.scss 和 common.scss）
import './assets/styles/index.scss'

createApp(App).mount('#app')

```
# 一键换肤功能

在现代前端开发中，最推荐、性能最好的方案是：SCSS 变量负责定义结构 + CSS 原生变量（CSS Variables）负责控制颜色。
### 1. 修改样式目录与文件
   在 src/assets/styles/ 下新建或修改以下三个文件：
* 文件 1：_themes.scss (定义不同主题的颜色)
```scss
// 使用 CSS 原生变量，方便运行时通过 JS 动态修改
[data-theme="light"] {
  --background-color: #ffffff;
  --text-color: #333333;
  --primary-color: #409eff;
}

[data-theme="dark"] {
  --background-color: #121212;
  --text-color: #eeeeee;
  --primary-color: #66b1ff;
}
```
* 文件 2：variables.scss (映射全局 SCSS 变量)

```scss
// 将 SCSS 变量指向 CSS 原生变量
// 这样在 Vue 组件中写 $bg-color，就能自动响应主题切换
$bg-color: var(--background-color);
$text-color: var(--text-color);
$primary-color: var(--primary-color);

```

* 文件 3：index.scss (全局主控文件)
```scss
@import "./themes.scss";
@import "./variables.scss";
@import "./common.scss";

// 初始化默认主题
html {
  @extend [data-theme="light"];
} 

```
### 3. 在 Vue 组件中编写一键切换逻辑
在你的控制组件（如导航栏）中，通过 JS 修改 `html `标签的` `data-theme` 属性：

```vue
<template>
  <div class="theme-box">
    <p>当前文本颜色会自动跟随主题</p>
    <button @click="toggleTheme">切换到 {{ theme === 'light' ? '暗黑' : '明亮' }} 模式</button>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const theme = ref('light')

const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dark' : 'light'
  // 核心：动态修改 html 标签的自定义属性
  document.documentElement.setAttribute('data-theme', theme.value)
  // 可选：持久化存储到本地，下次打开页面自动应用
  localStorage.setItem('user-theme', theme.value)
}

onMounted(() => {
  // 初始化时读取本地缓存
  const savedTheme = localStorage.getItem('user-theme') || 'light'
  theme.value = savedTheme
  document.documentElement.setAttribute('data-theme', savedTheme)
})
</script>

<style lang="scss" scoped>
.theme-box {
  // 直接使用在 variables.scss 中映射好的 SCSS 变量
  background-color: $bg-color;
  color: $text-color;
  padding: 20px;
  transition: all 0.3s ease; // 加上过渡动画，让换肤更丝滑
}
</style>

```
