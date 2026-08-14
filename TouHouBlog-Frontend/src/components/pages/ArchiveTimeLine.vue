<template>
  <div>
    <div v-if="loading" class="text-center py-10 text-gray-500">加载中...</div>

    <!-- 整个时间线容器：只有一条连续竖线 -->
    <div v-else-if="groupedYears.length" class="relative pl-10 border-l border-gray-300">
      <!-- 年份和月份都放在同一条竖线内 -->
      <div v-for="yearGroup in groupedYears" :key="yearGroup.year" class="relative mb-16 last:mb-0">
        <!-- 年份标题（靠近竖线，对齐） -->
        <div class="flex items-center gap-3 mb-8">
          <span class="year-title">{{ yearGroup.year }}</span>
          <span class="text-base text-gray-400">{{ yearGroup.total }} 篇</span>
        </div>

        <!-- 月份列表 -->
        <div class="space-y-12">
          <div v-for="month in yearGroup.months" :key="month.month" class="relative">
            <!-- 圆点：精确定位在竖线上 -->
            <span class="absolute top-2 w-4 h-4 rounded-full bg-white border-2 border-gray-400 dot"></span>

            <!-- 月份标题（可折叠） -->
            <div class="flex items-center gap-3 ml-6 cursor-pointer select-none" @click="toggleMonth(month.month)">
              <h3 class="month-title">{{ formatMonth(month.month) }}</h3>
              <span class="text-sm text-gray-400">{{ month.articles.length }} 篇</span>
              <span class="text-gray-400 text-sm">{{ isMonthOpen(month.month) ? '▲' : '▼' }}</span>
            </div>

            <!-- 文章列表 -->
            <ul v-if="isMonthOpen(month.month)" class="space-y-4 ml-6 mt-3">
              <li
                  v-for="article in month.articles"
                  :key="article.id"
                  class="flex items-center justify-between text-base cursor-pointer hover:text-gray-900 transition-colors"
                  @click="goArticle(article.id)"
              >
                <span class="text-gray-700">{{ article.title }}</span>
                <span class="text-sm text-gray-400">{{ formatDate(article.createTime) }}</span>
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-10 text-gray-500">暂无文章</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'
import { navigate } from 'astro:transitions/client'

const months = ref([])
const loading = ref(true)
const collapsedMonths = ref(new Set())

const fetchArchive = async () => {
  try {
    const res = await request.get('/api/articles/archive')
    months.value = res.data.data || []
  } catch (e) {
    console.error('获取归档时间线失败', e)
  } finally {
    loading.value = false
  }
}

const groupedYears = computed(() => {
  const yearMap = new Map()
  months.value.forEach(month => {
    const year = month.month.split('-')[0]
    if (!yearMap.has(year)) {
      yearMap.set(year, { year, months: [], total: 0 })
    }
    const group = yearMap.get(year)
    group.months.push(month)
    group.total += month.articles.length
  })
  return Array.from(yearMap.values()).sort((a, b) => b.year.localeCompare(a.year))
})

const toggleMonth = (month) => {
  if (collapsedMonths.value.has(month)) {
    collapsedMonths.value.delete(month)
  } else {
    collapsedMonths.value.add(month)
  }
  collapsedMonths.value = new Set(collapsedMonths.value)
}

const isMonthOpen = (month) => {
  return !collapsedMonths.value.has(month)
}

const goArticle = (id) => {
  navigate(`/article/${id}`)
}

const formatMonth = (month) => {
  const [year, mon] = month.split('-')
  return `${parseInt(mon)}月`
}

const formatDate = (datetime) => {
  const d = new Date(datetime)
  return d.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

onMounted(fetchArchive)
</script>

<style scoped>
.year-title {
  font-size: 2.5rem;          /* 年份更大 */
  font-weight: 800;
  line-height: 1;
}

.month-title {
  font-size: 1.75rem;       /* 月份更小 */
  font-weight: 600;
}

.dot {
  left: -2.5rem;            /* 对应容器 pl-10 的 2.5rem */
  transform: translateX(-50%);
}
</style>