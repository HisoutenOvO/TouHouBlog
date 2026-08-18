<template>
  <div>
    <div class="text-center fade-in-simple">
      <h1 class="talks-title">杂谈与思考</h1>
      <p class="talks-subtitle">把日常里值得记住的一瞬，沿着时间慢慢收进这里</p>
    </div>

    <!-- 发布框（仅管理员可见） -->
    <div v-if="isAdmin" class="max-w-3xl mx-auto mb-8 px-4">
      <div class="glass-card p-4">
        <textarea
            v-model="newContent"
            rows="3"
            placeholder="写点什么..."
            class="w-full border border-gray-200 rounded-lg p-3 text-sm resize-none focus:outline-none focus:ring-1 focus:ring-gray-300 bg-white/60 backdrop-blur-sm"
        ></textarea>
        <!-- 多图上传区域 -->
        <div class="flex flex-wrap gap-2 mt-3">
          <!-- 已上传图片缩略图 -->
          <div v-for="(pic, index) in newPictures" :key="index" class="relative w-16 h-16 group">
            <img :src="pic" class="w-full h-full object-cover rounded-lg" />
            <button
                @click="removeNewPic(index)"
                class="absolute -top-1 -right-1 w-4 h-4 bg-red-500 text-white rounded-full text-xs flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity"
                title="删除图片"
            >
              <Icon icon="lucide:x" class="w-3 h-3" />
            </button>
          </div>
          <!-- 上传按钮 -->
          <div
              class="w-16 h-16 border border-dashed border-gray-300 rounded-lg flex items-center justify-center cursor-pointer hover:border-gray-400 transition-colors"
              @click="triggerImageInput"
          >
            <Icon icon="lucide:plus" class="w-5 h-5 text-gray-400" />
          </div>
          <input ref="imageInput" type="file" accept="image/*" multiple @change="uploadNewImages" class="hidden" />
        </div>

        <div class="flex justify-end mt-3">
          <button
              @click="publishTalk"
              :disabled="!newContent.trim() || publishing"
              class="px-4 py-2 bg-gray-800 text-white text-sm rounded-lg hover:bg-gray-900 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            {{ publishing ? '发布中...' : '发布' }}
          </button>
        </div>
        <p v-if="errorMsg" class="text-red-500 text-xs mt-2">{{ errorMsg }}</p>
      </div>
    </div>

    <!-- 杂谈列表 -->
    <div v-if="loading" class="text-center text-gray-500 py-20">加载中...</div>

    <TransitionGroup
        v-else-if="talks.length"
        name="talk-list"
        tag="div"
        class="max-w-3xl mx-auto space-y-5 px-4"
        appear
    >
      <div
          v-for="talk in talks"
          :key="talk.id"
          class="glass-card p-6 hover:shadow-md transition-shadow cursor-pointer"
          @click="goTalk(talk.id)"
      >
        <div class="flex justify-between items-center mb-3">
          <span class="font-bold text-gray-900">Hisouten</span>
          <span class="text-sm text-gray-400">{{ formatDate(talk.createTime) }}</span>
        </div>
        <p class="text-gray-700 whitespace-pre-wrap leading-relaxed mb-4">{{ talk.content }}</p>

        <!-- 图片预览 -->
        <div v-if="getPictures(talk).length" class="mb-4">
          <div class="relative inline-block">
            <img
                :src="getPictures(talk)[0]"
                class="w-24 h-24 object-cover rounded-lg border border-gray-100"
            />
            <span
                v-if="getPictures(talk).length > 1"
                class="absolute bottom-1 right-1 bg-black/60 text-white text-xs px-1.5 py-0.5 rounded"
            >
              +{{ getPictures(talk).length - 1 }}
            </span>
          </div>
        </div>

        <div class="flex justify-between items-center text-sm text-gray-400">
          <span>{{ formatTime(talk.createTime) }}</span>
          <div class="flex gap-3">
            <button class="talk-action-btn" @click.stop>
              <Icon icon="lucide:heart" class="w-4 h-4" />
              <span>{{ talk.likeCount || 0 }}</span>
            </button>
            <button class="talk-action-btn" @click.stop>
              <Icon icon="lucide:message-circle" class="w-4 h-4" />
              <span>{{ talk.commentCount || 0 }}</span>
            </button>
          </div>
        </div>
      </div>
    </TransitionGroup>

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

</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
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

const isAdmin = ref(false)
const newContent = ref('')
const newPictures = ref([])
const imageInput = ref(null)
const publishing = ref(false)
const errorMsg = ref('')

let ossClient = null

const triggerImageInput = () => {
  imageInput.value?.click()
}

const uploadNewImages = async (e) => {
  const files = e.target.files
  if (!files.length) return
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
  for (const file of files) {
    const key = `talk-images/${Date.now()}_${file.name}`
    try {
      const result = await ossClient.put(key, file)
      newPictures.value.push(result.url)
    } catch (e) {
      errorMsg.value = '图片上传失败'
    }
  }
}

const removeNewPic = (index) => {
  newPictures.value.splice(index, 1)
}

const publishTalk = async () => {
  if (!newContent.value.trim()) return
  publishing.value = true
  errorMsg.value = ''
  try {
    await request.post('/api/talks', {
      content: newContent.value.trim(),
      pictures: newPictures.value
    })
    newContent.value = ''
    newPictures.value = []
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

const getPictures = (talk) => {
  if (!talk.pictures) return []
  if (typeof talk.pictures === 'string') {
    try {
      return JSON.parse(talk.pictures)
    } catch (e) {
      return []
    }
  }
  return talk.pictures
}

const goTalk = (id) => {
  navigate(`/talk/${id}`)
}

const changePage = (page) => {
  if (page < 1 || page > totalPages.value) return
  pageNum.value = page
  fetchTalks()
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
/* 杂谈列表卡片逐个上浮动画 */
.talk-list-enter-active {
  transition: opacity 0.6s ease, transform 0.6s ease;
}
.talk-list-enter-from {
  opacity: 0;
  transform: translateY(40px);
}
.talk-list-leave-active {
  transition: opacity 0.3s ease;
}
.talk-list-leave-to {
  opacity: 0;
}
</style>