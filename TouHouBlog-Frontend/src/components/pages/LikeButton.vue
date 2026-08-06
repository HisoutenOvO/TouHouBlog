<template>
  <button @click="toggleLike" :disabled="loading"
          class="flex items-center gap-1 px-3 py-1 border border-gray-200 rounded hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          :class="{ 'bg-red-50 border-red-200': liked }">
    <span :class="liked ? 'text-red-500' : 'text-gray-400'">❤️</span>
    <span class="text-xs" :class="liked ? 'text-red-500' : 'text-gray-500'">{{ likes }}</span>
  </button>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { getCurrentUserId } from '../../utils/auth.js'

const props = defineProps({ articleId: String })

const likes = ref(0)
const liked = ref(false)
const loading = ref(false)

const fetchStatus = async () => {
  try {
    const res = await axios.get(`/api/articles/${props.articleId}/like`, {
      params: { userId: getCurrentUserId() }
    })
    likes.value = res.data.data.likes
    liked.value = res.data.data.liked
  } catch (e) {
    console.warn('获取点赞状态失败', e)
  }
}

const toggleLike = async () => {
  loading.value = true
  try {
    const res = await axios.post(`/api/articles/${props.articleId}/like`, {
      userId: getCurrentUserId()
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
  fetchStatus()
})
</script>