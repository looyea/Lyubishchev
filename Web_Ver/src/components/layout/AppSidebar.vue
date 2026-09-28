<template>
  <div class="app-sidebar">
    <div class="sidebar-logo">
      <h3>柳比歇夫</h3>
      <span>时间统计系统</span>
    </div>
    <el-scrollbar class="sidebar-scroll">
      <el-menu
        :default-active="activeMenu"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/">
          <el-icon><Odometer /></el-icon>
          <span>概览</span>
        </el-menu-item>

        <el-menu-item index="/entry">
          <el-icon><EditPen /></el-icon>
          <span>时间记录</span>
        </el-menu-item>

        <el-sub-menu index="reports">
          <template #title>
            <el-icon><DataAnalysis /></el-icon>
            <span>统计报告</span>
          </template>
          <el-menu-item index="/report/weekly">周报</el-menu-item>
          <el-menu-item index="/report/archive">历史周结</el-menu-item>
          <el-menu-item index="/report/monthly">月报</el-menu-item>
          <el-menu-item index="/report/year">年报</el-menu-item>
          <el-menu-item index="/report/distribution">时间分布</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="data">
          <template #title>
            <el-icon><Folder /></el-icon>
            <span>数据管理</span>
          </template>
          <el-menu-item index="/records">全部记录</el-menu-item>
          <el-menu-item index="/import">导入历史数据</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Odometer, EditPen, DataAnalysis, Folder } from '@element-plus/icons-vue'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped>
.sidebar-logo {
  height: 56px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}
.sidebar-logo h3 {
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  line-height: 1.2;
}
.sidebar-logo span {
  font-size: 11px;
  color: #8a9bb2;
}
.sidebar-scroll {
  flex: 1;
}
.el-menu {
  border-right: none;
}
</style>
