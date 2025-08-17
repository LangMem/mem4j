import { createApp } from 'vue'
import '@/style/style.scss'
import 'virtual:windi.css'
import store from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from "./router"
import App from './App.vue'

const app = createApp(App)

app.use(ElementPlus)
app.use(router)
app.use(store)

app.mount('#app')
