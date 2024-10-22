import { createApp } from 'vue'
import App from './App.vue'
import router from "@/router";
import axios from "axios";
//element-plus样式
// import 'element-plus/dist/index.css'
//暗黑模式class
import 'element-plus/theme-chalk/dark/css-vars.css'

import 'flag-icon-css/css/flag-icons.min.css'

import '@/assets/css/element.less'

//设置axios全局请求的主地址
axios.defaults.baseURL = 'http://127.0.0.1:8080'

const app = createApp(App)

app.use(router)


// createApp(App).mount('#app')

app.mount('#app')
