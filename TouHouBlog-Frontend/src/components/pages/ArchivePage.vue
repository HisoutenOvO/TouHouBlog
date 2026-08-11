<template>
  <RecentArticles
      v-if="isFilterReady"
      :page-size="8"
      :search="searchKeyword"
      :category-id="categoryId"
      :tag-id="tagId"
  />
  <div v-else class="text-center text-gray-400 py-20">加载中...</div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import RecentArticles from './indexPage/RecentArticles.vue'

const searchKeyword = ref('')
const categoryId = ref(null)
const tagId = ref(null)
const isFilterReady = ref(false)

const initFilterParams = () => {
  const urlParams = new URLSearchParams(window.location.search)
  const cat = urlParams.get('categoryId')
  const tag = urlParams.get('tagId')
  categoryId.value = (cat && cat !== 'null') ? cat : null
  tagId.value = (tag && tag !== 'null') ? tag : null
  isFilterReady.value = true
}

if (typeof window !== 'undefined') {
  window.addEventListener('search-change', (e) => {
    searchKeyword.value = e.detail.keyword
    fetchTotal() // 搜索时也更新总数
  })
}

const fetchTotal = async () => {
  try {
    const params = { page: 1, pageSize: 1 }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (categoryId.value) params.categoryId = categoryId.value
    if (tagId.value) params.tagId = tagId.value
    const res = await axios.get('/api/articles/list', { params })
    window.dispatchEvent(new CustomEvent('article-total', {
      detail: { total: res.data.data.total || 0 }
    }))
  } catch (e) {}
}

onMounted(() => {
  initFilterParams()
  fetchTotal()
})
</script>