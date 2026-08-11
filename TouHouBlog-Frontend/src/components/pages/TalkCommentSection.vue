<template>
  <div>
    <!-- 评论列表 -->
    <div v-if="comments.length" class="space-y-4 mb-6">
      <div v-for="item in comments" :key="item.id" class="border-b border-gray-100 pb-4 last:border-0">
        <div class="flex justify-between items-start">
          <span class="font-semibold text-gray-800">{{ item.nickname }}</span>
          <span class="text-xs text-gray-400">{{ item.createTime }}</span>
        </div>
        <p class="mt-1 text-gray-700 whitespace-pre-wrap">{{ item.content }}</p>
      </div>
    </div>
    <div v-else class="text-sm text-gray-400 mb-4">暂无评论</div>

    <!-- 分页 -->
    <div v-if="total > pageSize" class="flex justify-center gap-2 mb-4">
      <button @click="changePage(pageNum - 1)" :disabled="pageNum === 1"
              class="px-3 py-1 text-sm rounded border border-gray-200 bg-white disabled:opacity-30">上一页</button>
      <span class="px-3 py-1 text-sm text-gray-500">{{ pageNum }} / {{ totalPages }}</span>
      <button @click="changePage(pageNum + 1)" :disabled="pageNum === totalPages"
              class="px-3 py-1 text-sm rounded border border-gray-200 bg-white disabled:opacity-30">下一页</button>
    </div>

    <!-- 发表评论（登录后可见） -->
    <div v-if="isLoggedIn" class="border-t pt-4">
      <textarea v-model="content" rows="2" placeholder="写下评论..."
                class="w-full px-3 py-2 border border-gray-200 rounded text-sm focus:outline-none focus:ring-1 focus:ring-gray-300 resize-none"></textarea>
      <button @click="submitComment" :disabled="!content || submitting"
              class="mt-2 px-4 py-1.5 bg-gray-800 text-white text-sm rounded hover:bg-gray-900 disabled:opacity-50 disabled:cursor-not-allowed transition-colors">
        {{ submitting ? '提交中...' : '发表' }}
      </button>
    </div>
    <div v-else class="border-t pt-4 text-center text-sm text-gray-400">
      <a href="/manage" class="text-blue-500 hover:underline">请先登录</a> 后评论
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import request from '../../utils/request'
import { getUserFromToken } from '../../utils/auth'

const props = defineProps({ talkId: String })
const emit = defineEmits(['totalChange'])

const comments = ref([])
const content = ref('')
const submitting = ref(false)
const isLoggedIn = ref(false)

const pageNum = ref(1)
const pageSize = 5
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize))

const user = getUserFromToken()
isLoggedIn.value = !!user

const fetchComments = async () => {
  try {
    const res = await request.get(`/api/comments/talk/${props.talkId}`, {
      params: { page: pageNum.value, pageSize }
    })
    comments.value = res.data.data.records
    total.value = res.data.data.total
    emit('totalChange', total.value)
  } catch (e) {}
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  pageNum.value = page
  fetchComments()
}

const submitComment = async () => {
  if (!content.value.trim()) return
  submitting.value = true
  try {
    await request.post('/api/comments', {
      talkId: props.talkId,
      content: content.value.trim()
    })
    content.value = ''
    pageNum.value = 1
    await fetchComments()
  } catch (e) {} finally {
    submitting.value = false
  }
}

onMounted(fetchComments)
</script>