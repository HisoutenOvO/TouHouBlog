<template>
  <div class="text-2xl font-bold mb-4">文章列表</div>
  <div v-if="loading" class="taxt-grey-500">加载ing...</div>
    <ul v-else-if="articles.length">
      <li v-for="article in articles" :key="article.id" class="border-b py-3">
        <h3 class="text-lg font-semibold">{{ article.title }}</h3>
        <p class="text-sm text-gray-400">{{article.createTime}}</p>
        </li>
    </ul>
  <div v-else class="text-grey-500">暂无文章</div>
</template>
<script setup>
import {ref,onMounted} from 'vue'
import axios from 'axios'

const articles = ref([]);
const loading = ref(true);
onMounted(async () => {
  try{
    const res = await axios.get('/api/articles/list?page=1&pageSize=5');
    articles.value = res.data.data.records;
  }catch (e) {
    console.error('获取文章失败', e);
  } finally {
    loading.value = false;
  }
});
</script>