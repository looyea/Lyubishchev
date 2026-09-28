import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
  },
  build: {
    // element-plus 为全量引入，单 chunk 较大属预期；如需瘦身可改为 unplugin-vue-components 按需引入
    chunkSizeWarningLimit: 1000,
    rollupOptions: {
      output: {
        manualChunks: {
          vue: ['vue', 'vue-router', 'pinia'],
          echarts: ['echarts', 'vue-echarts'],
          'element-plus': ['element-plus'],
          export: ['html2canvas', 'jspdf'],
          xlsx: ['xlsx'],
        },
      },
    },
  },
})
