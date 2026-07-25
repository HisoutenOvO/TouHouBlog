<template>
  <div>
    <div class="text-2xl font-bold mb-6">📝 最近文章</div>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else-if="articles.length" class="grid grid-cols-1 gap-4 md:grid-cols-2">
      <div v-for="art in articles" :key="art.id" class="bg-white p-4 rounded-lg shadow-sm border border-gray-100 hover:shadow-md transition-shadow">
        <h3 class="font-semibold text-gray-900 mb-2">{{art.title}}</h3>
        <div class="flex justify-between items-center text-xs text-gray-400">
          <span>{{art.categoryName}}</span>
          <span>{{art.createTime}}</span>
        </div>
      </div>
    </div>
    <div v-else class="text-gray-500">暂无文章</div>
  </div>
</template>

<script setup>
import {ref,onMounted} from 'vue'
import axios from 'axios'

const articles = ref([])
const loading = ref(true)
onMounted(async () => {
  try{
    const res = await axios.get('/api/articles/list?page=1&pageSize=6')
    articles.value = res.data.data.records
  }catch(error){
    console.error(error)
  }finally{
    loading.value = false
  }
});
</script>

