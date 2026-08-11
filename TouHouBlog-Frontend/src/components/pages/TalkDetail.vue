<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div v-if="loading" class="text-center py-20 text-gray-500">加载中...</div>

    <div v-else-if="talk" class="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
      <!-- 昵称 + 日期 -->
      <div class="flex justify-between items-center mb-3">
        <span class="font-bold text-gray-900">Hisouten</span>
        <span class="text-sm text-gray-400">{{ formatDate(talk.createTime) }}</span>
      </div>

      <!-- 正文 -->
      <p class="text-gray-700 whitespace-pre-wrap leading-relaxed mb-4">{{ talk.content }}</p>

      <!-- 图片 -->
      <img v-if="talk.picture" :src="talk.picture" alt="杂谈图片"
           class="max-w-full h-auto rounded-lg border border-gray-100 mb-4" />

      <!-- 互动 -->
      <div class="flex justify-between items-center text-sm text-gray-400 border-t pt-3">
        <span>{{ formatTime(talk.createTime) }}</span>
        <div class="flex gap-3">
          <button class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50">
            ❤️ <span class="text-xs">0</span>
          </button>
          <button class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50">
            💬 <span class="text-xs">0</span>
          </button>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20 text-gray-500">杂谈不存在。</div>

    <div class="mt-6 text-center">
      <a href="/talks" class="text-sm text-gray-400 hover:text-gray-600 no-underline">← 返回杂谈</a>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
const props = defineProps({ talkId: String })
const talk = ref(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await request.get(`/api/talks/${props.talkId}`)
    talk.value = res.data.data
  } catch (e) {
    console.error('获取杂谈详情失败', e)
    talk.value = null
  } finally {
    loading.value = false
  }
})

const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
const formatTime = (d) => d ? new Date(d).toLocaleTimeString('zh-CN', { hour:'2-digit', minute:'2-digit' }) : ''
</script>