import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import WindiCSS from 'vite-plugin-windicss'
import path from 'path'

// https://vitejs.dev/config/
export default defineConfig({

  resolve: {
    alias: {
      "@": path.resolve(__dirname, 'src')
    }
  },

  plugins: [
    vue(),
    WindiCSS(),
  ],

  // 解决跨域
  server: {
    // 前端服务启动地址 默认为 ‘0.0.0.0’
    // host: '59.74.224.30',
    port: 9000, // 设置服务启动端口号
    open: true, // 设置服务启动时是否自动打开浏览器

    proxy: {
      '/api': {
        // 服务器地址
        // target: 'http://59.74.224.30:8080',
        // 本地地址
        target: 'http://127.0.0.1:3000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },

})
