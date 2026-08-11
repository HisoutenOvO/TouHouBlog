<template>
  <div v-if="!isAdmin" class="flex items-center justify-center h-screen text-gray-500">无权限访问</div>
  <div v-else class="edit-page-container">
    <!-- 顶部工具栏：标题、分类、封面、标签、保存 -->
    <div class="edit-toolbar">
      <div class="flex items-center gap-4">
        <input v-model="title" type="text" placeholder="文章标题"
               class="flex-1 px-4 py-2 text-xl font-bold border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-300" />
        <select v-model="categoryId" class="border border-gray-200 rounded px-3 py-2">
          <option value="">选择分类</option>
          <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
        </select>
        <button @click="showAddCategory = true" class="px-2 py-1 text-xs bg-gray-100 rounded hover:bg-gray-200">＋</button>
        <button @click="saveArticle" class="px-6 py-2 bg-gray-800 text-white rounded-lg hover:bg-gray-900 transition-colors">
          {{ isEdit ? '更新文章' : '发布文章' }}
        </button>
      </div>

      <!-- 新增分类弹窗 -->
      <div v-if="showAddCategory" class="flex gap-2 mt-2 items-center">
        <input v-model="newCategoryName" @keyup.enter="addCategory" placeholder="新分类名" class="px-2 py-1 text-xs border border-gray-200 rounded" />
        <button @click="addCategory" :disabled="!newCategoryName.trim()" class="px-2 py-1 text-xs bg-gray-800 text-white rounded">确定</button>
        <button @click="showAddCategory = false" class="px-2 py-1 text-xs border border-gray-200 rounded">取消</button>
      </div>

      <!-- 封面图 -->
      <div class="mt-3">
        <p class="text-sm text-gray-500 mb-1">封面图</p>
        <div class="flex items-center gap-3">
          <div
              class="w-32 h-20 bg-gray-100 border border-gray-200 rounded flex items-center justify-center overflow-hidden cursor-pointer relative"
              @click="triggerCoverInput"
          >
            <img v-if="editCoverUrl" :src="editCoverUrl" class="w-full h-full object-cover" />
            <div v-else class="flex flex-col items-center text-gray-400">
              <span class="text-2xl">＋</span>
              <span class="text-xs">添加封面</span>
            </div>
          </div>
          <input ref="coverInputRef" type="file" accept="image/*" @change="uploadCover" class="hidden" />
          <button v-if="editCoverUrl" @click="editCoverUrl = ''" class="text-xs text-red-500">移除</button>
        </div>
      </div>

      <!-- 标签区域 -->
      <div class="mt-3 border border-gray-200 rounded-lg p-3">
        <p class="text-sm font-medium text-gray-700 mb-2">🏷️ 标签</p>
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
        <div class="flex gap-2">
          <input v-model="newTagName" type="text" placeholder="输入新标签名"
                 class="flex-1 px-3 py-1.5 text-sm border border-gray-200 rounded"
                 @keyup.enter="createTag" />
          <button @click="createTag" :disabled="!newTagName.trim() || creatingTag"
                  class="px-3 py-1.5 text-sm bg-gray-100 text-gray-700 rounded hover:bg-gray-200 disabled:opacity-50">
            {{ creatingTag ? '...' : '新增' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ByteMD 编辑器 -->
    <div class="edit-editor-full">
      <Editor :value="content" :plugins="plugins" :upload-images="uploadImages"
              @change="handleChange" />
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

// 标签
const allTags = ref([])
const selectedTags = ref([])
const newTagName = ref('')
const creatingTag = ref(false)

// 新增分类
const showAddCategory = ref(false)
const newCategoryName = ref('')

// 封面图
const editCoverUrl = ref('')
const coverInputRef = ref(null)
const triggerCoverInput = () => coverInputRef.value?.click()

const plugins = []
let ossClient = null

const uploadCover = async (e) => {
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
  const key = `blog-covers/${Date.now()}_${file.name}`
  const result = await ossClient.put(key, file)
  editCoverUrl.value = result.url
}

const uploadImages = async (files) => {
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
  const urls = []
  for (const file of files) {
    const key = `blog-images/${Date.now()}_${file.name}`
    const result = await ossClient.put(key, file)
    urls.push({ url: result.url })
  }
  return urls
}

const handleChange = (v) => { content.value = v }

const loadCategories = async () => {
  const res = await request.get('/api/categories/list?page=1&pageSize=999')
  categories.value = res.data.data.records
}

const loadTags = async () => {
  const res = await request.get('/api/tags/list?page=1&pageSize=999')
  allTags.value = res.data.data.records
}

const toggleTag = (tagId) => {
  const index = selectedTags.value.indexOf(tagId)
  if (index === -1) {
    selectedTags.value.push(tagId)
  } else {
    selectedTags.value.splice(index, 1)
  }
}

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

const createTag = async () => {
  const name = newTagName.value.trim()
  if (!name) return
  creatingTag.value = true
  try {
    await request.post('/api/tags', { name })
    newTagName.value = ''
    await loadTags()
  } catch (e) {} finally {
    creatingTag.value = false
  }
}

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

const loadArticle = async () => {
  if (props.articleId) {
    isEdit.value = true
    const res = await request.get(`/api/articles/${props.articleId}`)
    const article = res.data.data
    title.value = article.title
    content.value = article.content
    categoryId.value = article.categoryId
    selectedTags.value = article.tags ? article.tags.map(t => t.id) : []
    editCoverUrl.value = article.coverUrl || ''
  }
}

const saveArticle = async () => {
  const payload = {
    title: title.value,
    content: content.value,
    categoryId: categoryId.value || null,
    tagIds: selectedTags.value,
    coverUrl: editCoverUrl.value || null
  }
  if (!categoryId.value) {
    alert('请选择文章分类')
    return
  }
  if (selectedTags.value.length === 0) {
    alert('请至少选择一个标签')
    return
  }
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

<style scoped>
.edit-page-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200;
  display: flex;
  flex-direction: column;
  background: white;
  overflow: hidden;
}

.edit-toolbar {
  padding: 0.75rem 1rem;
  padding-top: 1rem;
  background: white;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
  box-sizing: border-box;
  overflow-y: auto;
  max-height: 55vh;
}

.edit-editor-full {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>

<style>
.edit-editor-full .bytemd {
  height: 100% !important;
  display: flex;
  flex-direction: column;
}
.edit-editor-full .bytemd-body {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
.edit-editor-full .bytemd-editor,
.edit-editor-full .bytemd-preview {
  height: 100% !important;
  overflow-y: auto !important;
}
</style>