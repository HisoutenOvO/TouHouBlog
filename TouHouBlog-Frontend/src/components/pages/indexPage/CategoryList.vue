<template>
  <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100">
    <h3 class="font-bold text-gray-900 mb-3">📂 分类</h3>
    <div class="text-sm text-gray-500" v-if="loading">加载中...</div>
    <div v-else-if="categories.length" class="space-y-2">
      <div
          v-for="cat in categories"
          :key="cat.id"
          class="flex justify-between text-sm cursor-pointer hover:text-gray-900 transition-colors"
          @click="goToCategory(cat.id)"
      >
        <span class="text-gray-700">{{ cat.name }}</span>
        <span class="text-gray-400">{{ cat.articleCount || 0 }}</span>
      </div>
    </div>
    <div v-else class="text-sm text-gray-500">暂无分类</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../../utils/request'
import { navigate } from 'astro:transitions/client'   // 新增导入

const categories = ref([])
const loading = ref(true)

const goToCategory = (id) => {
  navigate(`/archive?categoryId=${id}`)   // 替换原来 window.location.href
}

onMounted(async () => {
  try {
    const res = await request.get('/api/categories/list?page=1&pageSize=999')
    categories.value = res.data.data.records
  } catch (e) {
  } finally {
    loading.value = false
  }
})
</script>