import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import 'element-plus/dist/index.css'
import 'element-plus/theme-chalk/dark/css-vars.css'
import '@/assets/styles/index.scss'
import App from './App.vue'
import router from './router'
import { registerPermissionDirective } from '@/directives/permission'

const cachedTheme = localStorage.getItem('sdp-theme')
const initialTheme = cachedTheme === 'light' || cachedTheme === 'ocean' || cachedTheme === 'dark' ? cachedTheme : 'dark'
document.documentElement.classList.remove('dark')
document.documentElement.setAttribute('data-theme', initialTheme)

const app = createApp(App)
const pinia = createPinia()
app.use(pinia).use(router).use(ElementPlus, { locale: zhCn })
registerPermissionDirective(app)
app.mount('#app')
