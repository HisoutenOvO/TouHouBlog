<template>
  <div>
    <!-- 顶部标题区 -->
    <div class="text-center py-12">
      <h1 class="text-6xl font-extrabold text-gray-900 tracking-widest">
        杂谈与思考
      </h1>
      <p class="mt-4 text-base text-gray-400">
        把日常里值得记住的一瞬，沿着时间慢慢收进这里
      </p>
    </div>

    <div v-if="loading" class="text-center text-gray-500 py-20">加载中...</div>

    <div v-else-if="talks.length" class="max-w-3xl mx-auto space-y-5 px-4">
      <a v-for="talk in talks"
         :key="talk.id"
         :href="`/talk/${talk.id}`"
         class="block no-underline">
        <div class="bg-white rounded-lg shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer">
          <!-- 昵称 + 日期 -->
          <div class="flex justify-between items-center mb-3">
            <span class="font-bold text-gray-900">Hisouten</span>
            <span class="text-sm text-gray-400">{{ formatDate(talk.createTime) }}</span>
          </div>
          <!-- 正文 -->
          <p class="text-gray-700 whitespace-pre-wrap leading-relaxed mb-4">
            {{ talk.content }}
          </p>
          <!-- 图片（如果有） -->
          <div v-if="talk.picture" class="mb-4">
            <img :src="talk.picture" alt="杂谈图片"
                 class="max-w-full h-auto rounded-lg border border-gray-100" />
          </div>
          <!-- 互动按钮行 -->
          <div class="flex justify-between items-center text-sm text-gray-400">
            <span>{{ formatTime(talk.createTime) }}</span>
            <div class="flex gap-3" @click.prevent>
              <button class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50">
                ❤️ <span class="text-xs">0</span>
              </button>
              <button class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50">
                💬 <span class="text-xs">0</span>
              </button>
            </div>
          </div>
        </div>
      </a>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center gap-2 pt-6">
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

    <div v-else class="text-center text-gray-500 py-20">暂无杂谈。</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
const talks = ref([])
const loading = ref(true)
const pageNum = ref(1)
const pageSize = 5
const total = ref(0)
const totalPages = ref(0)

const fetchTalks = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/talks/list', {
      params: { page: pageNum.value, pageSize: pageSize }
    })
    talks.value = res.data.data.records
    total.value = res.data.data.total
    totalPages.value = Math.ceil(total.value / pageSize)
  } catch (e) {
    console.error('获取杂谈失败', e)
  } finally {
    loading.value = false
  }
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  pageNum.value = page
  fetchTalks()
}

// 日期格式化：yyyy-MM-dd
const formatDate = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return d.toLocaleDateString('zh-CN') // 2026/8/4 格式
}

// 时间格式化：HH:mm
const formatTime = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(() => {
  fetchTalks()
})
</script>