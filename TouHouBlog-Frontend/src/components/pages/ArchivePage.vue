<template>
  <div class="archive-view-container min-h-[500px]">
    <div v-show="currentView === 'list'">
      <RecentArticles
          v-if="isFilterReady"
          :page-size="8"
          :search="searchKeyword"
          :category-id="categoryId"
          :tag-id="tagId"
      />
      <div v-else class="text-center text-gray-400 py-20">加载中...</div>
    </div>

    <div v-show="currentView === 'timeline'">
      <ArchiveTimeline />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import RecentArticles from './indexPage/RecentArticles.vue'
import ArchiveTimeline from './ArchiveTimeLine.vue'

const searchKeyword = ref('')
const categoryId = ref(null)
const tagId = ref(null)
const isFilterReady = ref(false)
const currentView = ref('list')

// 从 URL 读取筛选参数
const initFilterParams = () => {
  const urlParams = new URLSearchParams(window.location.search)
  const cat = urlParams.get('categoryId')
  const tag = urlParams.get('tagId')
  categoryId.value = (cat && cat !== 'null') ? cat : null
  tagId.value = (tag && tag !== 'null') ? tag : null
  isFilterReady.value = true
}

// 监听搜索和视图切换事件
if (typeof window !== 'undefined') {
  window.addEventListener('search-change', (e) => {
    searchKeyword.value = e.detail.keyword
  })
  window.addEventListener('archive-view-change', (e) => {
    currentView.value = e.detail.view
  })
  // 初始化视图状态
  currentView.value = window.__archiveView || 'list'
}

onMounted(() => {
  initFilterParams()
})
</script>

<style scoped>
.archive-view-container {
  min-height: 500px;
}
</style>