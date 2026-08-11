<template>
  <div class="max-w-4xl mx-auto py-8">
    <div v-if="!isAdmin" class="text-center py-20 text-gray-500">无权限访问</div>
    <div v-else class="space-y-4">
      <!-- 标题 -->
      <input
          v-model="title"
          type="text"
          placeholder="文章标题"
          class="w-full px-4 py-3 text-2xl font-bold border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-300"
      />

      <!-- ByteMD 编辑器 -->
      <Editor
          :value="content"
          :plugins="plugins"
          :upload-images="uploadImages"
          @change="handleChange"
      />

      <!-- 分类下拉 + 新增分类按钮 -->
      <div class="flex items-center gap-2">
        <select v-model="categoryId" class="border border-gray-200 rounded px-3 py-2">
          <option value="">选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
        <button @click="showAddCategory = true" class="px-2 py-1 text-xs bg-gray-100 rounded hover:bg-gray-200">＋</button>

        <!-- 新增分类弹窗 -->
        <div v-if="showAddCategory" class="flex gap-2 items-center">
          <input v-model="newCategoryName" @keyup.enter="addCategory" placeholder="新分类名" class="px-2 py-1 text-xs border border-gray-200 rounded" />
          <button @click="addCategory" :disabled="!newCategoryName.trim()" class="px-2 py-1 text-xs bg-gray-800 text-white rounded">确定</button>
          <button @click="showAddCategory = false" class="px-2 py-1 text-xs border border-gray-200 rounded">取消</button>
        </div>
      </div>
      <!-- 封面图上传 -->
      <div class="mb-4">
        <p class="text-sm text-gray-500 mb-1">封面图</p>
        <div class="flex items-center gap-3">
          <div class="w-32 h-20 bg-gray-100 border border-gray-200 rounded flex items-center justify-center overflow-hidden">
            <img v-if="editCoverUrl" :src="editCoverUrl" class="w-full h-full object-cover" />
            <span v-else class="text-gray-400 text-xs">无封面</span>
          </div>
          <input type="file" accept="image/*" @change="uploadCover" class="text-xs" />
          <button v-if="editCoverUrl" @click="editCoverUrl = ''" class="text-xs text-red-500">移除</button>
        </div>
      </div>

      <!-- 标签区域 -->
      <div class="border border-gray-200 rounded-lg p-3">
        <p class="text-sm font-medium text-gray-700 mb-2">🏷️ 标签</p>
        <!-- 已有标签云：多选 -->
        <div class="flex flex-wrap gap-2 mb-3">
          <span
              v-for="tag in allTags"
              :key="tag.id"
              class="px-2.5 py-1 text-xs rounded-full cursor-pointer transition-colors"
              :class="selectedTags.includes(tag.id) ? 'bg-gray-800 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
              @click="toggleTag(tag.id)"
          >
            {{ tag.name }}
            <button @click.stop="deleteTag(tag.id)" class="ml-1 text-red-500 hover:text-red-700" title="删除标签">×</button>
          </span>
        </div>
        <!-- 新增标签 -->
        <div class="flex gap-2">
          <input
              v-model="newTagName"
              type="text"
              placeholder="输入新标签名"
              class="flex-1 px-3 py-1.5 text-sm border border-gray-200 rounded focus:outline-none focus:ring-1 focus:ring-gray-300"
              @keyup.enter="createTag"
          />
          <button
              @click="createTag"
              :disabled="!newTagName.trim() || creatingTag"
              class="px-3 py-1.5 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 disabled:opacity-50"
          >
            {{ creatingTag ? '...' : '新增' }}
          </button>
        </div>
      </div>

      <!-- 保存按钮 -->
      <button
          @click="saveArticle"
          class="px-6 py-2 bg-gray-800 text-white rounded-lg hover:bg-gray-900 transition-colors"
      >
        {{ isEdit ? '更新文章' : '发布文章' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Editor } from '@bytemd/vue-next'
import 'bytemd/dist/index.css'
import { getUserFromToken } from '../../utils/auth'
import request from '../../utils/request'
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

// 标签相关
const allTags = ref([])           // 所有标签列表
const selectedTags = ref([])      // 选中的标签 ID
const newTagName = ref('')        // 新标签输入
const creatingTag = ref(false)    // 是否正在创建标签

// 新增分类
const showAddCategory = ref(false)
const newCategoryName = ref('')

const plugins = []
let ossClient = null

const editCoverUrl = ref('')

const uploadCover = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  if (!ossClient) {
    const res = await request.get('/api/oss/signature')
    const data = res.data.data
    ossClient = new OSS({ region: data.region, accessKeyId: data.accessKeyId, accessKeySecret: data.accessKeySecret, bucket: data.bucket })
  }
  const key = `blog-covers/${Date.now()}_${file.name}`
  const result = await ossClient.put(key, file)
  editCoverUrl.value = result.url
}
// 图片上传（OSS）
const uploadImages = async (files) => {
  if (!ossClient) {
    const res = await request.get('/api/oss/signature')
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

const handleChange = (v) => { content.value = v }

// 加载分类
const loadCategories = async () => {
  const res = await request.get('/api/categories/list?page=1&pageSize=999')
  categories.value = res.data.data.records
}

// 加载所有标签
const loadTags = async () => {
  const res = await request.get('/api/tags/list?page=1&pageSize=999')
  allTags.value = res.data.data.records
}

// 切换标签选中状态
const toggleTag = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index === -1) {
    selectedTags.value.push(tagId)
  } else {
    selectedTags.value.splice(index, 1)
  }
}

// 删除标签（实体）
const deleteTag = async (tagId) => {
  const tag = allTags.value.find(t => t.id === tagId)
  const name = tag ? tag.name : '该标签'
  const confirmed = confirm(`确定要删除标签「${name}」吗？`)
  if (!confirmed) return
  try {
    await request.delete(`/api/tags/${tagId}`)
    selectedTags.value = selectedTags.value.filter(id => id !== tagId)
    await loadTags()
  } catch (e) {}
}

// 创建新标签
const createTag = async () => {
  const name = newTagName.value.trim()
  if (!name) return
  creatingTag.value = true
  try {
    await request.post('/api/tags', { name })
    newTagName.value = ''
    await loadTags()
    // 不再自动选中
  } catch (e) {} finally {
    creatingTag.value = false
  }
}

// 新增分类
const addCategory = async () => {
  const name = newCategoryName.value.trim()
  if (!name) return
  try {
    await request.post('/api/categories', { name })
    await loadCategories()
    const newCat = categories.value[categories.value.length - 1]
    if (newCat) categoryId.value = newCat.id
    showAddCategory.value = false
    newCategoryName.value = ''
  } catch (e) {}
}

// 加载已有文章数据（编辑模式）
const loadArticle = async () => {
  if (props.articleId) {
    isEdit.value = true
    const res = await request.get(`/api/articles/${props.articleId}`)
    const article = res.data.data
    title.value = article.title
    content.value = article.content
    categoryId.value = article.categoryId
    selectedTags.value = article.tags ? article.tags.map(t => t.id) : []
  }
}

// 保存文章
const saveArticle = async () => {
  const payload = {
    title: title.value,
    content: content.value,
    categoryId: categoryId.value || null,
    tagIds: selectedTags.value,
    coverUrl: editCoverUrl.value || null

  }
  console.log(payload)
  try {
    if (isEdit.value) {
      await request.put(`/api/articles/${props.articleId}`, payload)
    } else {
      await request.post('/api/articles', payload)
    }
    window.location.href = '/archive'
  } catch (e) {}
}

onMounted(async () => {
  const user = getUserFromToken()
  if (user && user.role === 1) {
    isAdmin.value = true
    await loadCategories()
    await loadTags()
    await loadArticle()
  }
})
</script>