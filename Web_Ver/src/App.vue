<template>
  <div class="app-container">
    <AppSidebar />
    <div class="app-main">
      <AppHeader />
      <div class="app-content">
        <router-view v-slot="{ Component }">
          <keep-alive :include="['TimeEntry']">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import AppSidebar from './components/layout/AppSidebar.vue'
import AppHeader from './components/layout/AppHeader.vue'
</script>
