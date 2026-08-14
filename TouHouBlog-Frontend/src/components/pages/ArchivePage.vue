<template>
  <div>
    <!-- 视图切换开关（由 archive.astro 控制，这里只负责显示对应视图） -->
    <template v-if="currentView === 'list'">
      <RecentArticles
          v-if="isFilterReady"
          :page-size="8"
          :search="searchKeyword"
          :category-id="categoryId"
          :tag-id="tagId"
      />
      <div v-else class="text-center text-gray-400 py-20">加载中...</div>
    </template>

    <ArchiveTimeline v-else />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import RecentArticles from './indexPage/RecentArticles.vue'
import ArchiveTimeline from '../pages/ArchiveTimeLine.vue'

const currentView = ref('list') // 默认列表视图

// 监听视图切换事件（来自 archive.astro）
if (typeof window !== 'undefined') {
  window.addEventListener('archive-view-change', (e) => {
    currentView.value = e.detail.view
  })
  // 初始化视图状态
  const initialView = window.__archiveView || 'list'
  currentView.value = initialView
}
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
    const res = await request.get('/api/articles/list', { params })
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