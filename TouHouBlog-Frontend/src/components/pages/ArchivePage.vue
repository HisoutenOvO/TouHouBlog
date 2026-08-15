<template>
  <div class="archive-view-container min-h-[500px]">
    <!-- 文章列表视图 -->
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

    <!-- 时间线视图 -->
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
  })
  window.addEventListener('archive-view-change', (e) => {
    currentView.value = e.detail.view
  })
  const initialView = window.__archiveView || 'list'
  currentView.value = initialView
}

onMounted(() => {
  initFilterParams()
})
</script>