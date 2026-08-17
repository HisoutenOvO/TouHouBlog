<template>
  <div>
    <div v-if="loading" class="text-gray-500">加载中...</div>
    <TransitionGroup
        v-else-if="articles.length"
        name="article-list"
        tag="div"
        class="space-y-3"
        appear
    >
      <RevealOnScroll
          v-for="(article, index) in articles"
          :key="article.id"
          :delay="index * 60"
      >
        <a
            :href="`/article/${article.id}`"
            class="glass-card flex article-card-hover cursor-pointer min-h-[12rem]"
            :style="{ transitionDelay: `${index * 80}ms` }"
        >
          <!-- 左侧封面 -->
          <div class="w-1/2 bg-gray-100 rounded-l-lg flex items-center justify-center overflow-hidden relative">
            <img
                v-if="article.coverUrl"
                :src="article.coverUrl"
                class="absolute inset-0 w-full h-full object-cover rounded-l-lg"
            />
            <span v-else class="text-gray-400 text-lg">封面占位</span>
          </div>
          <!-- 右侧文字 -->
          <div class="w-1/2 p-5 flex flex-col justify-between">
            <div>
              <h3 class="text-lg font-bold text-gray-900 mb-1">{{ article.title }}</h3>
              <div class="text-sm text-gray-400">
                {{ article.categoryName }} · {{ article.createTime }}
              </div>
            </div>
            <p class="text-sm text-gray-600 leading-relaxed line-clamp-2 my-2">{{ article.content }}</p>
            <div class="flex flex-wrap gap-1.5">
        <span v-for="tag in article.tags" :key="tag.id" class="tag-chip-list">
          {{ tag.name }}
        </span>
            </div>
          </div>
        </a>
      </RevealOnScroll>
    </TransitionGroup>
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
import request from '../../../utils/request'
import RevealOnScroll from "../../common/RevealOnScroll.vue";

const props = defineProps({
  pageSize: { type: Number, default: 5 },
  search: { type: String, default: '' },
  categoryId: { type: [String, Number], default: null },
  tagId: { type: [String, Number], default: null }
})

const articles = ref([])
const loading = ref(true)
const pageNum = ref(1)
const total = ref(0)
const totalPages = ref(0)

const fetchArticles = async () => {
  loading.value = true
  try {
    const params = { page: pageNum.value, pageSize: props.pageSize }
    if (props.search) params.keyword = props.search
    if (props.categoryId) params.categoryId = props.categoryId
    if (props.tagId) params.tagId = props.tagId

    const res = await request.get('/api/articles/list', { params })
    articles.value = res.data.data.records
    total.value = res.data.data.total
    totalPages.value = Math.ceil(total.value / props.pageSize)
  } catch (e) {
    console.error('获取文章失败', e)
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  pageNum.value = page
  fetchArticles()
}

watch(
    () => [props.search, props.categoryId, props.tagId],
    () => {
      pageNum.value = 1
      fetchArticles()
    }
)

onMounted(() => {
  fetchArticles()
})
</script>

<style scoped>
/* 文章卡片逐个上浮动画 */
.article-list-enter-active {
  transition: opacity 0.6s ease, transform 0.6s ease;
}
.article-list-enter-from {
  opacity: 0;
  transform: translateY(40px);
}
.article-list-leave-active {
  transition: opacity 0.3s ease;
}
.article-list-leave-to {
  opacity: 0;
}
/* 文章卡片悬停特效 */
.article-card-hover {
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.article-card-hover:hover {
  transform: translateY(-6px);
  box-shadow: 0 16px 32px rgba(124, 58, 237, 0.18), 0 6px 12px rgba(0, 0, 0, 0.06);
}
</style>