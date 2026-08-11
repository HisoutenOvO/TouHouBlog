<template>
  <button
      @click="toggleLike"
      :disabled="loading || !isLoggedIn"
      class="flex items-center gap-1 px-3 py-1 border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
      :class="{ 'bg-red-50 border-red-200': liked }"
      :title="isLoggedIn ? '' : '请先登录'"
  >
    <span :class="liked ? 'text-red-500' : 'text-gray-400'">❤️</span>
    <span class="text-xs" :class="liked ? 'text-red-500' : 'text-gray-500'">{{ likes }}</span>
  </button>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { getUserFromToken } from '../../utils/auth.js'

const props = defineProps({ articleId: String })

const likes = ref(0)
const liked = ref(false)
const loading = ref(false)
const isLoggedIn = ref(false)

const user = getUserFromToken()
isLoggedIn.value = !!user

const fetchLikeStatus = async () => {
  if (!isLoggedIn.value) return
  try {
    const token = localStorage.getItem('touhou_token')
    const res = await request.get(`/api/articles/${props.articleId}/like`, {
      headers: { Authorization: `Bearer ${token}` }
    })
    likes.value = res.data.data.likes
    liked.value = res.data.data.liked
  } catch (e) {
    console.error('获取点赞状态失败', e)
  }
}

const toggleLike = async () => {
  if (!isLoggedIn.value) {
    window.location.href = '/manage'
    return
  }
  loading.value = true
  try {
    const token = localStorage.getItem('touhou_token')
    const res = await request.post(`/api/articles/${props.articleId}/like`, null, {
      headers: { Authorization: `Bearer ${token}` }
    })
    likes.value = res.data.data.likes
    liked.value = res.data.data.liked
  } catch (e) {
    console.error('点赞操作失败', e)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLikeStatus()
})
</script>