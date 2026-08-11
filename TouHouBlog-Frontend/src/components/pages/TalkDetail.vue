<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div v-if="loading" class="text-center py-20 text-gray-500">加载中...</div>

    <div v-else-if="talk" class="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
      <!-- 昵称 + 日期 -->
      <div class="flex justify-between items-center mb-3">
        <span class="font-bold text-gray-900">Hisouten</span>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-400">{{ formatDate(talk.createTime) }}</span>
          <button v-if="isAdmin" @click="deleteTalk" class="text-xs text-red-500 hover:underline">删除</button>
        </div>
      </div>

      <!-- 正文 -->
      <p class="text-gray-700 whitespace-pre-wrap leading-relaxed mb-4">{{ talk.content }}</p>

      <!-- 图片（点击放大） -->
      <div v-if="talk.picture" class="mb-4">
        <img v-if="talk.picture" :src="talk.picture" class="w-full rounded-lg border border-gray-100 cursor-pointer" @click="showImageOverlay = true" />
      </div>

      <!-- 互动行：时间、点赞、评论按钮 -->
      <div class="flex justify-between items-center text-sm text-gray-400 border-t pt-3">
        <span>{{ formatTime(talk.createTime) }}</span>
        <div class="flex gap-3">
          <button
              @click="toggleLike"
              :disabled="likeLoading"
              class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50 transition-colors"
              :class="{ 'bg-red-50 border-red-200': liked }"
          >
            <span :class="liked ? 'text-red-500' : 'text-gray-400'">❤️</span>
            <span class="text-xs" :class="liked ? 'text-red-500' : 'text-gray-500'">{{ likes }}</span>
          </button>
          <button class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50">
            💬 <span class="text-xs">{{ commentTotal }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20 text-gray-500">杂谈不存在。</div>

    <!-- 评论区 -->
    <div v-if="talk" class="mt-6">
      <h3 class="text-lg font-bold text-gray-900 mb-4">💬 评论 ({{ commentTotal }})</h3>
      <TalkCommentSection :talk-id="talkId" @total-change="commentTotal = $event" />
    </div>

    <div class="mt-6 text-center">
      <a href="/talks" class="text-sm text-gray-400 hover:text-gray-600 no-underline">← 返回杂谈</a>
    </div>

    <!-- 图片放大遮罩 -->
    <div v-if="showImageOverlay" class="fixed inset-0 z-50 bg-black bg-opacity-80 flex items-center justify-center p-4" @click="showImageOverlay = false">
      <img :src="talk.picture" class="max-w-full max-h-full rounded-lg shadow-2xl" @click.stop />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { getUserFromToken } from '../../utils/auth'
import TalkCommentSection from './TalkCommentSection.vue'


const props = defineProps({ talkId: String })

const talk = ref(null)
const loading = ref(true)
const likes = ref(0)
const liked = ref(false)
const likeLoading = ref(false)
const showImageOverlay = ref(false)
const commentTotal = ref(0)

const fetchTalk = async () => {
  try {
    const res = await request.get(`/api/talks/${props.talkId}`)
    talk.value = res.data.data
    // 获取点赞状态
    await fetchLikeStatus()
  } catch (e) {
    talk.value = null
  } finally {
    loading.value = false
  }
}

const fetchLikeStatus = async () => {
  const user = getUserFromToken()
  if (!user) return
  try {
    const res = await request.get(`/api/talks/${props.talkId}/like`, {
      params: { userId: user.id }
    })
    likes.value = res.data.data.likes
    liked.value = res.data.data.liked
  } catch (e) {}
}

const toggleLike = async () => {
  const user = getUserFromToken()
  if (!user) {
    window.location.href = '/manage'
    return
  }
  likeLoading.value = true
  try {
    const res = await request.post(`/api/talks/${props.talkId}/like`, {
      userId: user.id
    })
    likes.value = res.data.data.likes
    liked.value = res.data.data.liked
  } catch (e) {}
  finally {
    likeLoading.value = false
  }
}

const isAdmin = ref(false)

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)
  await fetchTalk()
})

const deleteTalk = async () => {
  const confirmed = confirm('确定要删除这条杂谈吗？')
  if (!confirmed) return
  try {
    await request.delete(`/api/talks/${props.talkId}`)
    window.location.href = '/talks'
  } catch (e) {}
}
const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
const formatTime = (d) => d ? new Date(d).toLocaleTimeString('zh-CN', { hour:'2-digit', minute:'2-digit' }) : ''

onMounted(fetchTalk)
</script>