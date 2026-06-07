import { createPinia } from 'pinia'
import { createApp } from 'vue'
import App from './App.vue'
import router from './router/index.ts'
import './style.css'
import './styles/responsive.css'
import 'virtual:uno.css'

createApp(App)
  .use(createPinia())
  .use(router)
  .mount('#app')
