<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <!-- 页面标题 -->
    <div class="text-center fade-in-simple mb-12">
      <h1 class="archive-title">开发者日志</h1>
      <p class="archive-subtitle">记录博客每个重要版本的迭代与思考</p>
    </div>

    <!-- 找不到分类时的提示 -->
    <div v-if="!loading && categoryMissing" class="glass-card p-6 text-center">
      <p class="text-gray-600 mb-4">开发者日志分类不存在，请先创建该分类。</p>
      <button v-if="isAdmin" class="create-cat-btn" @click="createDevCategory">
        <Icon icon="lucide:plus" class="w-4 h-4" />
        创建开发者日志分类
      </button>
    </div>

    <!-- 正常加载 -->
    <div v-else-if="loading" class="text-center text-gray-500 py-20">加载中...</div>

    <div v-else-if="logs.length" class="space-y-6">
      <div v-for="(log, index) in logs" :key="log.id" class="glass-card p-6 reveal-item" :style="{ transitionDelay: `${index * 60}ms` }">
        <div class="flex items-center gap-3 mb-2">
          <span class="text-sm font-semibold text-gray-800">{{ formatDate(log.createTime) }}</span>
          <span class="px-2 py-0.5 text-xs rounded-full bg-pink-100 text-pink-800">v{{ getVersionFromTitle(log.title) }}</span>
        </div>
        <h2 class="text-xl font-bold text-gray-900 mb-2">{{ log.title }}</h2>
        <p class="text-gray-600 leading-relaxed line-clamp-3">{{ log.content }}</p>
        <a :href="`/article/${log.id}`" class="inline-block mt-3 text-sm text-blue-500 hover:underline">查看详情 →</a>
      </div>
    </div>
    <div v-else class="text-center text-gray-500 py-20">暂无日志</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../utils/request'
import { getUserFromToken } from '../../utils/auth'

const logs = ref([])
const loading = ref(true)
const isAdmin = ref(false)
const categoryMissing = ref(false)

const createDevCategory = async () => {
  try {
    await request.post('/api/categories', { name: '开发者日志' })
    categoryMissing.value = false
    await fetchLogs()
  } catch (e) {
    console.error('创建开发者日志分类失败', e)
  }
}

const fetchLogs = async () => {
  loading.value = true
  categoryMissing.value = false
  try {
    const catRes = await request.get('/api/categories/list', { params: { page: 1, pageSize: 999 } })
    const categories = catRes.data.data.records || []
    const devCat = categories.find(cat => cat.name === '开发者日志')

    if (!devCat) {
      categoryMissing.value = true
      logs.value = []
      return
    }

    const artRes = await request.get('/api/articles/list', {
      params: { page: 1, pageSize: 999, categoryId: devCat.id }
    })
    logs.value = artRes.data.data.records || []
  } catch (e) {
    console.error('获取开发者日志失败', e)
  } finally {
    loading.value = false
  }
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
}

const getVersionFromTitle = (title) => {
  const match = title.match(/v?(\d+\.\d+(?:\.\d+)?)/i)
  return match ? match[1] : '1.0'
}

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)
  await fetchLogs()
})
</script>

<style scoped>
.create-cat-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.6rem 1.2rem;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.25s ease;
}
.create-cat-btn:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  transform: translateY(-2px);
}

.reveal-item {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}
.reveal-item.is-visible {
  opacity: 1;
  transform: translateY(0);
}

@media (prefers-reduced-motion: reduce) {
  .reveal-item {
    opacity: 1;
    transform: none;
    transition: none;
  }
}
</style>