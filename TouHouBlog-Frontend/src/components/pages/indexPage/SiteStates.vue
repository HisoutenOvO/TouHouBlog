<template>
  <div class="glass-card p-4">
    <div class="flex justify-around text-center">
      <div>
        <div class="text-2xl font-bold text-gray-900">{{ stats.articleCount }}</div>
        <div class="text-xs text-gray-500">文章</div>
      </div>
      <div>
        <div class="text-2xl font-bold text-gray-900">{{ stats.categoryCount }}</div>
        <div class="text-xs text-gray-500">分类</div>
      </div>
      <div>
        <div class="text-2xl font-bold text-gray-900">{{ stats.tagCount }}</div>
        <div class="text-xs text-gray-500">标签</div>
      </div>
      <div>
        <div class="text-2xl font-bold text-gray-900">{{ stats.runningDays }}</div>
        <div class="text-xs text-gray-500">运行天数</div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../../utils/request'
const stats = ref({
  articleCount: 0,
  categoryCount: 0,
  tagCount: 0,
  runningDays: 0
});

onMounted(async () => {
  try {
    const [articleRes, categoryRes, tagRes] = await Promise.all([
      request.get('/api/articles/list?page=1&pageSize=5'),
      request.get('/api/categories/list?page=1&pageSize=5'),
      request.get('/api/tags/list?page=1&pageSize=5')
    ]);

    stats.value.articleCount = articleRes.data.data.total;
    stats.value.categoryCount = categoryRes.data.data.total;
    stats.value.tagCount = tagRes.data.data.total;
  } catch (e) {
    console.error('获取统计数据失败', e);
  }

  const startDate = new Date('2026-07-01');
  const today = new Date();
  stats.value.runningDays = Math.floor((today - startDate) / (1000 * 60 * 60 * 24));
});
</script>