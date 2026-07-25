<template>
  <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100">
    <h3 class="font-bold text-gray-900 mb-3">📂 分类</h3>
    <div class="text-sm text-gray-500" v-if="loading">加载中...</div>
    <div v-else-if="categories.length" class="space-y-2">
      <div v-for="cat in categories" :key="cat.id" class="lex justify-between text-sm">
        <span class="text-gray-700">{{ cat.name }}</span>
        <span class="text-gray-400">{{ cat.articleCount || 0 }}</span>
      </div>
    </div>
    <div v-else class="text-sm text-gray-500">暂无分类</div>
  </div>
</template>

<script setup>
import {ref,onMounted} from 'vue'
import axios from 'axios'

const categories = ref([])
const loading = ref(true)

onMounted(async () => {
  try{
    const res = await axios.get('/api/categories/list?page=1&pageSize=999')
    categories.value = res.data.data.records
  } catch (e) {
    console.error('获取分类失败', e);
  } finally {
    loading.value = false;
  }
})
</script>