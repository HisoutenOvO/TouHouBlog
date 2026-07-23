<template>
  <div>
    <h2 class="text-2xl font-bold mb-6">文章列表</h2>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else-if="articles.length" class="space-y-4">
      <div
          v-for="article in articles"
          :key="article.id"
          class="bg-white p-6 rounded-lg shadow-sm border border-gray-100"
      >
        <h3 class="text-lg font-semibold mb-2">{{ article.title }}</h3>
        <p class="text-sm text-gray-400">{{ article.createTime }}</p>
      </div>
    </div>
    <div v-else class="text-gray-500">暂无文章。</div>
  </div>
</template>
<script setup>
import {ref,onMounted} from 'vue'
import axios from 'axios'

const articles = ref([]);
const loading = ref(true);
onMounted(async () => {
  try{
    const res = await axios.get('/api/articles/list?page=1&pageSize=10');
    articles.value = res.data.data.records;
  }catch (e) {
    console.error('获取文章失败', e);
  } finally {
    loading.value = false;
  }
});
</script>