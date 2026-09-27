<template>
  <div class="app-header">
    <span style="font-size: 16px; font-weight: 500; color: #303133;">
      {{ title }}
    </span>
    <div style="flex: 1" />
    <el-tag type="info" size="small">
      共 {{ store.totalCount }} 条记录
    </el-tag>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useTimeLogStore } from '@/stores/timeLog'

const route = useRoute()
const store = useTimeLogStore()
const title = computed(() => (route.meta.title as string) || '柳比歇夫时间统计')

// 头部需要展示全局记录数，故自行保证数据已加载
onMounted(() => {
  store.ensureLoaded()
})
</script>
