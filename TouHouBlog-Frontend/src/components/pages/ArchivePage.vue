<template>
  <div>
    <!-- 顶部标题区 -->
    <div class="text-center py-12">
      <h1 class="text-6xl font-extrabold text-gray-900 tracking-widest">
        归档与探索
      </h1>
      <p class="mt-4 text-base text-gray-400">
        共 {{ totalArticles }} 篇研究
      </p>

      <!-- 搜索框（回车触发） -->
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

    <!-- 三栏布局 -->
    <div class="flex gap-6 max-w-7xl mx-auto px-4">
      <aside class="w-56 flex-shrink-0">
        <div class="sticky top-24">
          <CategoryList />
        </div>
      </aside>

      <main class="flex-1 max-w-[60%]">
        <RecentArticles :page-size="8" :search="searchKeyword" />
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
import { ref, watch } from 'vue'
import axios from 'axios'
import RecentArticles from './indexPage/RecentArticles.vue'
import CategoryList from './indexPage/CategoryList.vue'
import TagList from './indexPage/TagList.vue'

const totalArticles = ref(0)
const inputText = ref('')          // 输入框中的文本
const searchKeyword = ref('')      // 真正用于请求的关键词

// 获取文章总数（带搜索条件）
const fetchTotal = async () => {
  try {
    const params = { page: 1, pageSize: 1 }
    if (searchKeyword.value) {
      params.title = searchKeyword.value   // 假设后端参数名为 keyword
    }
    const res = await axios.get('/api/articles/list', { params })
    totalArticles.value = res.data.data.total || 0
  } catch (e) {
    console.error('获取文章总数失败', e)
  }
}

// 回车时：将输入框内容赋值给 searchKeyword，触发后续更新
const handleSearch = () => {
  searchKeyword.value = inputText.value.trim()
}

// 监听 searchKeyword 变化，重新获取总数
watch(searchKeyword, () => {
  fetchTotal()
}, { immediate: false })

// 初始加载总数
fetchTotal()
</script>