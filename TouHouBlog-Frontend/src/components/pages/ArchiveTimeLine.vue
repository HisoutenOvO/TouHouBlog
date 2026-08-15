<template>
  <div>
    <div v-if="loading" class="text-center py-10 text-gray-500">加载中...</div>

    <div v-else-if="groupedYears.length" class="archive-timeline glass-card">
      <!-- 年份循环 -->
      <div v-for="yearGroup in groupedYears" :key="yearGroup.year" class="year-section">
        <!-- 年份标题 -->
        <div class="year-header">
          <span class="year-text">{{ yearGroup.year }}</span>
          <span class="year-count">{{ yearGroup.total }} 篇</span>
        </div>

        <!-- 月份列表 -->
        <div class="months-container">
          <div v-for="month in yearGroup.months" :key="month.month" class="month-block">
            <!-- 圆点 + 月份标题 -->
            <div class="month-header" @click="toggleMonth(month.month)">
              <span class="month-dot"></span>
              <span class="month-title">{{ formatMonth(month.month) }}</span>
              <span class="month-count">{{ month.articles.length }} 篇</span>
              <span class="month-arrow">{{ isMonthOpen(month.month) ? '▲' : '▼' }}</span>
            </div>

            <!-- 文章列表（折叠时隐藏） -->
            <ul v-if="isMonthOpen(month.month)" class="article-list">
              <li
                  v-for="article in month.articles"
                  :key="article.id"
                  class="article-item"
                  @click="goArticle(article.id)"
              >
                <span class="article-title">{{ article.title }}</span>
                <span class="article-date">{{ formatDate(article.createTime) }}</span>
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
.archive-timeline {
  padding: 1.5rem;
  border-radius: 1rem;
}

.year-section {
  margin-bottom: 2rem;
}

.year-header {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.year-text {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 3rem;
  font-weight: 700;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.12));
}

.year-count {
  font-size: 0.9rem;
  color: #6b7280;
}

.months-container {
  position: relative;
  padding-left: 2rem;
  border-left: 2px solid rgba(255, 255, 255, 0.8);
}

.month-block {
  position: relative;
  margin-bottom: 1.5rem;
}

.month-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  cursor: pointer;
  user-select: none;
  transition: background 0.2s;
  padding: 0.25rem 0.5rem;
  border-radius: 6px;
}

.month-header:hover {
  background: rgba(255, 255, 255, 0.5);
}

.month-dot {
  position: absolute;
  left: -2.5rem;                 /* 2rem(竖线距离内容区) + 0.5rem(圆点半径) */
  top: 0.5rem;
  width: 1rem;
  height: 1rem;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #b388eb;
  box-shadow: 0 0 6px rgba(179, 136, 235, 0.5);
}

.month-title {
  font-size: 1.25rem;
  font-weight: 600;
  color: #4b5563;
}

.month-count {
  font-size: 0.8rem;
  color: #9ca3af;
}

.month-arrow {
  font-size: 0.7rem;
  color: #9ca3af;
  margin-left: auto;
}

.article-list {
  list-style: none;
  padding-left: 1.5rem;
  margin-top: 0.5rem;
}

.article-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.6rem 0.8rem;
  margin-bottom: 0.25rem;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}

.article-item:hover {
  background: rgba(255, 255, 255, 0.6);
  transform: translateX(4px);
}

.article-title {
  font-size: 0.95rem;
  color: #374151;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.article-date {
  font-size: 0.8rem;
  color: #9ca3af;
  margin-left: 1rem;
  flex-shrink: 0;
}
</style>