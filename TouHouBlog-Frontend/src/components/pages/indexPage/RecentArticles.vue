<template>
  <div>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <div v-else-if="articles.length" class="space-y-3">
      <a v-for="article in articles"
         :key="article.id"
         :href="`/article/${article.id}`"
         class="block no-underline">
        <div class="bg-white rounded-lg shadow-sm border border-gray-100 flex hover:shadow-md transition-shadow cursor-pointer">
          <!-- 左侧封面 -->
          <div class="w-1/2 bg-gray-100 rounded-l-lg flex items-center justify-center p-4">
            <span class="text-gray-400 text-lg">封面占位</span>
          </div>
          <!-- 右侧文字 -->
          <div class="w-1/2 p-5 flex flex-col justify-between">
            <div>
              <h3 class="text-lg font-bold text-gray-900 mb-1">{{ article.title }}</h3>
              <div class="text-sm text-gray-400">
                {{ article.categoryName }} · {{ article.createTime }}
              </div>
            </div>
            <p class="text-sm text-gray-600 leading-relaxed line-clamp-2 my-2">
              {{ article.content }}
            </p>
            <div class="flex flex-wrap gap-1.5">
        <span v-for="tag in article.tags" :key="tag.id"
              class="px-2.5 py-1 text-xs rounded-full bg-gray-100 text-gray-500">
          {{ tag.name }}
        </span>
            </div>
          </div>
        </div>
      </a>
    </div>
    <div v-else class="text-gray-500">暂无文章。</div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="flex justify-center gap-2 mt-6">
      <button
          @click="changePage(pageNum - 1)"
          :disabled="pageNum === 1"
          class="px-3 py-1 text-sm rounded border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50"
      >
        上一页
      </button>
      <span class="px-3 py-1 text-sm text-gray-500">
        {{ pageNum }} / {{ totalPages }}
      </span>
      <button
          @click="changePage(pageNum + 1)"
          :disabled="pageNum === totalPages"
          class="px-3 py-1 text-sm rounded border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50"
      >
        下一页
      </button>
    </div>
  </div>
</template>
<script setup>
import { ref, watch, onMounted } from 'vue'
import axios from 'axios'

const props = defineProps({
  pageSize: { type: Number, default: 5 },
  search: { type: String, default: '' },
  categoryId: { type: [String, Number], default: null },
  tagId: { type: [String, Number], default: null }
})

const articles = ref([])
const loading = ref(false)
const pageNum = ref(1)
const total = ref(0)
const totalPages = ref(0)

const fetchArticles = async () => {
  loading.value = true
  try {
    const params = { page: pageNum.value, pageSize: props.pageSize }
    if (props.search) params.keyword = props.search
    if (props.categoryId && props.categoryId !== 'null') params.categoryId = props.categoryId
    if (props.tagId && props.tagId !== 'null') params.tagId = props.tagId

    const res = await axios.get('/api/articles/list', { params })
    articles.value = res.data.data.records
    total.value = res.data.data.total
    totalPages.value = Math.ceil(total.value / props.pageSize)
  } catch (e) {
    console.error('获取文章失败', e)
    articles.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  pageNum.value = page
  fetchArticles()
}

// 监听筛选条件变化
watch(
    () => [props.search, props.categoryId, props.tagId],
    () => {
      pageNum.value = 1
      fetchArticles()
    }
)

// 组件挂载时请求数据（此时 props 已由父组件提供正确的值）
onMounted(() => {
  fetchArticles()
})
</script>