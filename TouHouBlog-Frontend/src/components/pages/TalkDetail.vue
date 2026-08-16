<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div v-if="loading" class="text-center py-20 text-gray-500">加载中...</div>

    <div v-else-if="talk" class="glass-card p-6">
      <!-- 昵称 + 日期 + 删除 -->
      <div class="flex justify-between items-center mb-3">
        <span class="font-bold text-gray-900">Hisouten</span>
        <div class="flex items-center gap-3">
          <span class="text-sm text-gray-400">{{ formatDate(talk.createTime) }}</span>
          <button v-if="isAdmin" @click="deleteTalk" class="admin-action-btn delete">
            <Icon icon="lucide:trash-2" class="w-4 h-4" />
            删除
          </button>
        </div>
      </div>

      <!-- 正文 -->
      <p class="text-gray-700 whitespace-pre-wrap leading-relaxed mb-4">{{ talk.content }}</p>

      <!-- 图片（点击放大） -->
      <div v-if="talk.picture" class="mb-4">
        <img :src="talk.picture" alt="杂谈图片"
             class="w-full rounded-lg border border-gray-100 cursor-pointer"
             @click="showImageOverlay = true" />
      </div>

      <!-- 互动行：时间、点赞、评论按钮 -->
      <div class="flex justify-between items-center text-sm text-gray-400 border-t pt-3">
        <span>{{ formatTime(talk.createTime) }}</span>
        <div class="flex gap-3">
          <button
              @click="toggleLike"
              :disabled="likeLoading"
              class="talk-action-btn"
              :class="{ active: liked }"
          >
            <Icon icon="lucide:heart" class="w-4 h-4" />
            <span>{{ likes }}</span>
          </button>
          <button class="talk-action-btn">
            <Icon icon="lucide:message-circle" class="w-4 h-4" />
            <span>{{ commentTotal }}</span>
          </button>
        </div>
      </div>
    </div>

    <div v-else class="text-center py-20 text-gray-500">杂谈不存在。</div>

    <!-- 评论区 -->
    <div v-if="talk" class="mt-6">
      <TalkCommentSection :talk-id="talkId" @total-change="commentTotal = $event" />
    </div>

    <div class="mt-6 text-center">
      <a href="/talks" class="text-sm text-gray-400 hover:text-gray-600 no-underline">← 返回杂谈</a>
    </div>

    <!-- 图片放大遮罩 -->
    <div v-if="showImageOverlay"
         class="fixed inset-0 z-50 bg-black bg-opacity-80 flex items-center justify-center p-4"
         @click="showImageOverlay = false">
      <img :src="talk.picture" class="max-w-full max-h-full rounded-lg shadow-2xl" @click.stop />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
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
const isAdmin = ref(false)

const fetchTalk = async () => {
  try {
    const res = await request.get(`/api/talks/${props.talkId}`)
    talk.value = res.data.data
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
  } catch (e) {
    // 全局拦截器已提示
  } finally {
    likeLoading.value = false
  }
}

const deleteTalk = async () => {
  const confirmed = await window.$confirm('确定要删除这条杂谈吗？')
  if (!confirmed) return
  try {
    await request.delete(`/api/talks/${props.talkId}`)
    window.location.href = '/talks'
  } catch (e) {}
}

const formatDate = (d) => d ? new Date(d).toLocaleDateString('zh-CN') : ''
const formatTime = (d) => d ? new Date(d).toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }) : ''

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)
  await fetchTalk()
})
</script>

<style scoped>
.talk-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.35rem 0.9rem;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.5);
  backdrop-filter: blur(4px);
  color: #6b7280;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
}
.talk-action-btn:hover {
  background: rgba(255, 255, 255, 0.85);
  color: #374151;
}
.talk-action-btn.active {
  background: rgba(254, 226, 226, 0.6);
  color: #b91c1c;
  border-color: rgba(254, 202, 202, 0.8);
}

/* 管理员操作按钮（删除） */
.admin-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 1rem;
  border-radius: 9999px;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}
.admin-action-btn.delete {
  background: linear-gradient(135deg, #fde2e2, #fbc4c4);
  color: #b91c1c;
}
.admin-action-btn.delete:hover {
  background: linear-gradient(135deg, #fecaca, #fca5a5);
  color: #991b1b;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}
</style>