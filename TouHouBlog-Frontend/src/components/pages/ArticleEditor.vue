<template>
  <div class="max-w-4xl mx-auto py-8">
    <div v-if="!isAdmin" class="text-center py-20 text-gray-500">无权限访问</div>
    <div v-else class="space-y-4">
      <input
          v-model="title"
          type="text"
          placeholder="文章标题"
          class="w-full px-4 py-3 text-2xl font-bold border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-300"
      />
      <Editor
          :value="content"
          :plugins="plugins"
          :upload-images="uploadImages"
          @change="handleChange"
      />
      <div class="flex gap-4 items-center">
        <select v-model="categoryId" class="border border-gray-200 rounded px-3 py-2">
          <option value="">选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
        <button
            @click="saveArticle"
            class="px-6 py-2 bg-gray-800 text-white rounded-lg hover:bg-gray-900 transition-colors"
        >
          {{ isEdit ? '更新文章' : '发布文章' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Editor } from '@bytemd/vue-next'
import 'bytemd/dist/index.css'
import { getUserFromToken } from '../../utils/auth'
import axios from 'axios'
import OSS from 'ali-oss'

const props = defineProps({
  articleId: { type: String, default: '' }
})

const isAdmin = ref(false)
const title = ref('')
const content = ref('')
const categoryId = ref('')
const categories = ref([])
const isEdit = ref(false)
const plugins = []

let ossClient = null

const uploadImages = async (files) => {
  if (!ossClient) {
    const res = await axios.get('/api/oss/signature')
    const data = res.data.data
    ossClient = new OSS({
      region: data.region,
      accessKeyId: data.accessKeyId,
      accessKeySecret: data.accessKeySecret,
      bucket: data.bucket,
    })
  }
  const urls = []
  for (const file of files) {
    const key = `blog-images/${Date.now()}_${file.name}`
    const result = await ossClient.put(key, file)
    urls.push({ url: result.url })
  }
  return urls
}

const handleChange = (v) => {
  content.value = v
}

const loadCategories = async () => {
  try {
    const res = await axios.get('/api/categories/list?page=1&pageSize=999')
    categories.value = res.data.data.records
  } catch (e) {}
}

const loadArticle = async () => {
  if (props.articleId) {
    isEdit.value = true
    try {
      const res = await axios.get(`/api/articles/${props.articleId}`)
      const article = res.data.data
      title.value = article.title
      content.value = article.content
      categoryId.value = article.categoryId
    } catch (e) {
      alert('加载文章失败')
    }
  }
}

const saveArticle = async () => {
  const payload = {
    title: title.value,
    content: content.value,
    categoryId: categoryId.value || null,
    tagIds: []
  }
  try {
    if (isEdit.value) {
      await axios.put(`/api/articles/${props.articleId}`, payload)
    } else {
      await axios.post('/api/articles', payload)
    }
    window.location.href = '/archive'
  } catch (e) {
    alert('保存失败')
  }
}

onMounted(async () => {
  const user = getUserFromToken()
  if (user && user.role === 1) {
    isAdmin.value = true
    await loadCategories()
    await loadArticle()
  }
})
</script>