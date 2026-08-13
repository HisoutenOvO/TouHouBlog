<template>
  <div>
    <h2 class="text-3xl font-bold text-center py-10">杂谈与思考</h2>
    <p class="text-center text-base text-gray-400 -mt-6 mb-8">
      把日常里值得记住的一瞬，沿着时间慢慢收进这里
    </p>

    <!-- 发布框（仅管理员可见） -->
    <div v-if="isAdmin" class="max-w-3xl mx-auto mb-8 px-4">
      <div class="bg-white rounded-lg shadow-sm border border-gray-100 p-4">
        <textarea
            v-model="newContent"
            rows="3"
            placeholder="写点什么..."
            class="w-full border border-gray-200 rounded-lg p-3 text-sm resize-none focus:outline-none focus:ring-1 focus:ring-gray-300"
        ></textarea>
        <div class="flex items-center gap-3 mt-3">
          <!-- 图片上传 -->
          <div
              class="w-12 h-12 border border-dashed border-gray-300 rounded-lg flex items-center justify-center cursor-pointer hover:border-gray-400 relative overflow-hidden"
              @click="triggerImageInput"
          >
            <img v-if="newImage" :src="newImage" class="w-full h-full object-cover rounded-lg" />
            <span v-else class="text-2xl text-gray-300">+</span>
          </div>
          <input ref="imageInput" type="file" accept="image/*" @change="uploadNewImage" class="hidden" />
          <button
              @click="publishTalk"
              :disabled="!newContent.trim() || publishing"
              class="ml-auto px-4 py-2 bg-gray-800 text-white text-sm rounded-lg hover:bg-gray-900 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ publishing ? '发布中...' : '发布' }}
          </button>
        </div>
        <p v-if="errorMsg" class="text-red-500 text-xs mt-2">{{ errorMsg }}</p>
      </div>
    </div>

    <!-- 杂谈列表 -->
    <div v-if="loading" class="text-center text-gray-500 py-20">加载中...</div>

    <div v-else-if="talks.length" class="max-w-3xl mx-auto space-y-5 px-4">
      <div
          v-for="talk in talks"
          :key="talk.id"
          class="bg-white rounded-lg shadow-sm border border-gray-100 p-6 hover:shadow-md transition-shadow cursor-pointer"
          @click="goTalk(talk.id)"
      >
        <div class="flex justify-between items-center mb-3">
          <span class="font-bold text-gray-900">Hisouten</span>
          <span class="text-sm text-gray-400">{{ formatDate(talk.createTime) }}</span>
        </div>
        <p class="text-gray-700 whitespace-pre-wrap leading-relaxed mb-4">{{ talk.content }}</p>
        <div v-if="talk.picture" class="mb-4">
          <img :src="talk.picture" alt="杂谈图片" class="w-24 h-24 object-cover rounded-lg border border-gray-100" />
        </div>
        <div class="flex justify-between items-center text-sm text-gray-400">
          <span>{{ formatTime(talk.createTime) }}</span>
          <div class="flex gap-3">
            <button class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50">
              ❤️ <span class="text-xs">{{ talk.likeCount || 0 }}</span>
            </button>
            <button class="flex items-center gap-1 px-2 py-1 border border-gray-200 rounded hover:bg-gray-50">
              💬 <span class="text-xs">{{ talk.commentCount || 0 }}</span>
            </button>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div v-if="total > pageSize" class="flex justify-center gap-2 pt-6">
        <button
            @click="changePage(pageNum - 1)"
            :disabled="pageNum === 1"
            class="px-3 py-1 text-sm rounded border border-gray-200 bg-white disabled:opacity-30 disabled:cursor-not-allowed hover:bg-gray-50"
        >
          上一页
        </button>
        <span class="px-3 py-1 text-sm text-gray-500">{{ pageNum }} / {{ totalPages }}</span>
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
import { ref, onMounted, computed } from 'vue'
import request from '../../utils/request'
import OSS from 'ali-oss'
import { getUserFromToken } from '../../utils/auth'
import { navigate } from 'astro:transitions/client'

const talks = ref([])
const loading = ref(true)
const pageNum = ref(1)
const pageSize = 5
const total = ref(0)
const totalPages = computed(() => Math.ceil(total.value / pageSize))

// 发布相关
const isAdmin = ref(false)
const newContent = ref('')
const newImage = ref('')
const imageInput = ref(null)
const publishing = ref(false)
const errorMsg = ref('')

let ossClient = null

const triggerImageInput = () => {
  imageInput.value?.click()
}

const uploadNewImage = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (!ossClient) {
    const res = await request.get('/api/oss/signature')
    const data = res.data.data
    ossClient = new OSS({
      region: data.region,
      endpoint: data.endpoint,
      accessKeyId: data.accessKeyId,
      accessKeySecret: data.accessKeySecret,
      bucket: data.bucket,
    })
  }
  const key = `talk-images/${Date.now()}_${file.name}`
  try {
    const result = await ossClient.put(key, file)
    newImage.value = result.url
  } catch (e) {
    errorMsg.value = '图片上传失败'
  }
}

const publishTalk = async () => {
  if (!newContent.value.trim()) return
  publishing.value = true
  errorMsg.value = ''
  try {
    await request.post('/api/talks', {
      content: newContent.value.trim(),
      picture: newImage.value || null
    })
    newContent.value = ''
    newImage.value = ''
    pageNum.value = 1
    await fetchTalks()
  } catch (e) {
    // 错误由全局拦截器处理
  } finally {
    publishing.value = false
  }
}

const fetchTalks = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/talks/list', {
      params: { page: pageNum.value, pageSize }
    })
    talks.value = res.data.data.records
    total.value = res.data.data.total
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

const goTalk = (id) => {
  navigate(`/talk/${id}`)
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return d.toLocaleDateString('zh-CN')
}

const formatTime = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return d.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)
  await fetchTalks()
})
</script>