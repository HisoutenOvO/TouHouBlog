<template>
  <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100">
    <h3 class="font-bold text-gray-900 mb-3">🏷️ 标签</h3>
    <div v-if="loading" class="text-sm text-gray-500">加载中...</div>
    <div v-else-if="tags.length" class="flex flex-wrap gap-2">
      <span
          v-for="tag in tags"
          :key="tag.id"
          class="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600 cursor-pointer hover:bg-gray-200 transition-colors"
          @click="goToTag(tag.id)"
      >
        {{ tag.name }}
      </span>
    </div>
    <div v-else class="text-sm text-gray-500">暂无标签</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../../utils/request'
import { navigate } from 'astro:transitions/client'   // 新增导入

const tags = ref([])
const loading = ref(true)

const goToTag = (id) => {
  navigate(`/archive?tagId=${id}`)   // 替换原来 window.location.href
}

onMounted(async () => {
  try {
    const res = await request.get('/api/tags/list?page=1&pageSize=999')
    tags.value = res.data.data.records
  } catch (err) {
    console.error(err)
  } finally {
    loading.value = false
  }
})
</script>