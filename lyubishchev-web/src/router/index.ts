import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'Dashboard',
      component: () => import('@/views/Dashboard.vue'),
      meta: { title: '概览' },
    },
    {
      path: '/entry',
      name: 'TimeEntry',
      component: () => import('@/views/TimeEntry.vue'),
      meta: { title: '时间记录' },
    },
    {
      path: '/report/weekly',
      name: 'WeekReport',
      component: () => import('@/views/WeekReport.vue'),
      meta: { title: '周报' },
    },
    {
      path: '/report/archive',
      name: 'SettlementArchive',
      component: () => import('@/views/SettlementArchive.vue'),
      meta: { title: '历史周结' },
    },
    {
      path: '/report/monthly',
      name: 'MonthlyReport',
      component: () => import('@/views/MonthlyReport.vue'),
      meta: { title: '月报' },
    },
    {
      path: '/report/year',
      name: 'YearReport',
      component: () => import('@/views/YearReport.vue'),
      meta: { title: '年报' },
    },
    {
      path: '/report/distribution',
      name: 'DistReport',
      component: () => import('@/views/DistReport.vue'),
      meta: { title: '时间分布' },
    },
    {
      path: '/records',
      name: 'LogManage',
      component: () => import('@/views/LogManage.vue'),
      meta: { title: '全部记录' },
    },
    {
      path: '/import',
      name: 'DataImport',
      component: () => import('@/views/DataImport.vue'),
      meta: { title: '数据导入' },
    },
  ],
})

router.beforeEach((to) => {
  document.title = `${to.meta.title || '柳比歇夫'} - 时间统计系统`
})

export default router
