<template>
  <div class="max-w-2xl mx-auto relative pl-8">
    <!-- 竖线 -->
    <div class="absolute left-3 top-0 bottom-0 w-0.5 bg-gray-200"></div>

    <div v-for="(item, index) in logs" :key="index" class="relative mb-10">
      <!-- 圆点 -->
      <div class="absolute -left-5 top-1 w-3 h-3 bg-white border-2 border-gray-300 rounded-full"></div>
      <!-- 内容 -->
      <div class="bg-white rounded-lg border border-gray-200 p-4 shadow-sm">
        <div class="flex justify-between items-center mb-2">
          <span class="text-sm text-gray-500">{{ item.date }}</span>
          <span class="px-2 py-0.5 text-xs rounded-full bg-gray-100 text-gray-600">{{ item.version }}</span>
        </div>
        <p class="text-gray-700">{{ item.desc }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
const logs = [
  { date: '2026-06', version: 'v0.2.0', desc: '第二阶段：前端骨架搭建完成，页面路由全部贯通' },
  { date: '2026-05', version: 'v0.1.0', desc: '第一阶段：后端基础接口实现，文章、分类、标签、杂谈增删改查完成' },
  { date: '2026-04', version: 'v0.0.1', desc: '项目初始化，技术栈选型与数据库设计' }
]
</script>