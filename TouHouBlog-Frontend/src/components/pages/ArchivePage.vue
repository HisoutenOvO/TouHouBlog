<template>
  <div>
    <!-- 顶部标题区（不变） -->
    <div class="text-center py-12">
      <h1 class="text-6xl font-extrabold text-gray-900 tracking-widest">归档与探索</h1>
      <p class="mt-4 text-base text-gray-400">共 {{ totalArticles }} 篇研究</p>
      <div class="mt-6 flex justify-center">
        <input
            v-model="inputText"
            type="text"
            placeholder="搜索文章..."
            class="w-80 px-4 py-2.5 border border-gray-200 rounded-full text-sm text-gray-700 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent bg-white shadow-sm"
            @keyup.enter="handleSearch"
        />
      </div>
    </div>

    <div class="flex gap-6 max-w-7xl mx-auto px-4">
      <aside class="w-56 flex-shrink-0">
        <div class="sticky top-24"><CategoryList /></div>
      </aside>

      <main class="flex-1 max-w-[60%]">
        <!-- 关键：只有参数就绪后才渲染文章列表 -->
        <RecentArticles
            v-if="isFilterReady"
            :page-size="8"
            :search="searchKeyword"
            :category-id="categoryId"
            :tag-id="tagId"
        />
        <div v-else class="text-center text-gray-400 py-20">加载中...</div>
      </main>

      <aside class="w-56 flex-shrink-0">
        <div class="sticky top-24 space-y-4">
          <TagList />
          <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100">
            <h3 class="font-bold text-gray-900 mb-2">🎵 音乐</h3>
            <p class="text-sm text-gray-500">音乐功能开发中...</p>
          </div>
        </div>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'
import RecentArticles from './indexPage/RecentArticles.vue'
import CategoryList from './indexPage/CategoryList.vue'
import TagList from './indexPage/TagList.vue'

const totalArticles = ref(0)
const inputText = ref('')
const searchKeyword = ref('')
const categoryId = ref(null)
const tagId = ref(null)
const isFilterReady = ref(false)   // 新增标志位

const initFilterParams = () => {
  const urlParams = new URLSearchParams(window.location.search)
  const cat = urlParams.get('categoryId')
  const tag = urlParams.get('tagId')
  categoryId.value = (cat && cat !== 'null') ? cat : null
  tagId.value = (tag && tag !== 'null') ? tag : null
  isFilterReady.value = true   // 参数解析完毕，允许渲染列表
}

const fetchTotal = async () => {
  try {
    const params = { page: 1, pageSize: 1 }
    if (searchKeyword.value) params.keyword = searchKeyword.value
    if (categoryId.value) params.categoryId = categoryId.value
    if (tagId.value) params.tagId = tagId.value
    const res = await axios.get('/api/articles/list', { params })
    totalArticles.value = res.data.data.total || 0
  } catch (e) {
    console.error('获取文章总数失败', e)
  }
}

const handleSearch = () => {
  searchKeyword.value = inputText.value.trim()
}

watch(searchKeyword, () => {
  fetchTotal()
})

onMounted(() => {
  initFilterParams()   // 先解析参数
  fetchTotal()         // 再获取总数（此时参数已就绪）
})
</script>