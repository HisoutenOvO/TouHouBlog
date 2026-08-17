<template>
  <div v-if="!isAdmin" class="fade-in flex items-center justify-center h-screen text-gray-500">无权限访问</div>
  <div v-else class="edit-page-container fade-in">
    <div class="edit-layout">
      <!-- 左侧写作区 -->
      <div class="edit-main">
        <input
            v-model="title"
            type="text"
            placeholder="文章标题"
            class="title-input"
        />
        <div class="editor-wrapper">
          <Editor
              :value="content"
              :plugins="plugins"
              :upload-images="uploadImages"
              locale="zh-Hans"
              @change="handleChange"
          />
        </div>
      </div>

      <!-- 右侧设置面板 -->
      <div class="edit-settings">
        <!-- 分类 -->
        <div class="setting-group">
          <label class="setting-label">分类</label>
          <div class="flex gap-2">
            <select v-model="categoryId" class="setting-select">
              <option value="">未分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
            <button @click="showAddCategory = true" class="setting-add-btn">＋</button>
          </div>
          <div v-if="showAddCategory" class="flex gap-2 mt-2">
            <input
                v-model="newCategoryName"
                @keyup.enter="addCategory"
                placeholder="新分类名"
                class="setting-input"
            />
            <button @click="addCategory" :disabled="!newCategoryName.trim()" class="setting-confirm-btn">确定</button>
            <button @click="showAddCategory = false" class="setting-cancel-btn">取消</button>
          </div>
        </div>

        <!-- 封面图 -->
        <div class="setting-group">
          <label class="setting-label">封面图</label>
          <div class="cover-upload" @click="triggerCoverInput">
            <img v-if="editCoverUrl" :src="editCoverUrl" class="cover-image" />
            <div v-else class="cover-placeholder">
              <span class="text-3xl">＋</span>
              <span class="text-xs">添加封面</span>
            </div>
          </div>
          <input ref="coverInputRef" type="file" accept="image/*" @change="uploadCover" class="hidden" />
          <button v-if="editCoverUrl" @click="editCoverUrl = ''" class="text-xs text-red-500 mt-1">移除封面</button>
        </div>

        <!-- 标签 -->
        <div class="setting-group">
          <label class="setting-label">标签</label>
          <div class="flex flex-wrap gap-1.5">
            <span
                v-for="tag in allTags"
                :key="tag.id"
                class="tag-item"
                :class="{ active: selectedTags.includes(tag.id) }"
                @click="toggleTag(tag.id)"
            >
              {{ tag.name }}
            </span>
          </div>
          <div class="flex gap-2 mt-2">
            <input
                v-model="newTagName"
                type="text"
                placeholder="新增标签"
                class="setting-input"
                @keyup.enter="createTag"
            />
            <button @click="createTag" :disabled="!newTagName.trim() || creatingTag" class="setting-confirm-btn">
              {{ creatingTag ? '...' : '新增' }}
            </button>
          </div>
        </div>

        <!-- 操作按钮组 -->
        <div class="flex gap-2 mt-auto">
          <button @click="cancelEdit" class="cancel-btn">取消</button>
          <button @click="saveDraft" class="draft-btn">保存草稿</button>
          <button @click="publishArticle" class="publish-btn">发布</button>
        </div>
      </div>
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
import gfm from '@bytemd/plugin-gfm'
import { navigate } from 'astro:transitions/client'

const plugins = [gfm()]

const props = defineProps({
  articleId: { type: String, default: '' }
})

const isAdmin = ref(false)
const title = ref('')
const content = ref('')
const categoryId = ref('')
const categories = ref([])

// 当前文章ID：空字符串表示新建，有值表示编辑
const currentArticleId = ref(props.articleId || '')

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
  const confirmed = await window.$confirm(`确定要删除标签「${name}」吗？`)
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
    currentArticleId.value = props.articleId
    const res = await request.get(`/api/articles/${props.articleId}`)
    const article = res.data.data
    title.value = article.title
    content.value = article.content
    categoryId.value = article.categoryId
    selectedTags.value = article.tags ? article.tags.map(t => t.id) : []
    editCoverUrl.value = article.coverUrl || ''
  }
}

// 检查是否有未发布的草稿
const checkDraft = async () => {
  // 只在新建模式（没有 articleId）时检查
  if (props.articleId) return
  try {
    const res = await request.get('/api/articles/draft')
    const draft = res.data.data
    if (draft && draft.id) {
      const shouldContinue = await window.$confirm('检测到未完成的草稿，是否继续编辑？\n')
      if (shouldContinue) {
        // 继续编辑草稿：加载内容，并将 currentArticleId 设为草稿ID
        currentArticleId.value = String(draft.id)
        title.value = draft.title || ''
        content.value = draft.content || ''
        categoryId.value = draft.categoryId || ''
        selectedTags.value = draft.tags ? draft.tags.map(t => t.id) : []
        editCoverUrl.value = draft.coverUrl || ''
      } else {
        // 删除草稿并新建
        await request.delete(`/api/articles/${draft.id}`)
        // 不加载任何内容，保持空白新建
      }
    }
  } catch (e) {
    console.error('检查草稿失败', e)
  }
}

// 取消：弹出确认框，确认后跳转
const cancelEdit = async () => {
  const confirmed = await window.$confirm('确定要退出编辑吗？未保存的修改将丢失。')
  if (!confirmed) return
  if (currentArticleId.value) {
    navigate(`/article/${currentArticleId.value}`)
  } else {
    navigate('/archive')
  }
}

// 保存草稿：保存后回到归档页
const saveDraft = async () => {
  if (!title.value.trim()) {
    await window.$alert('请输入文章标题')
    return
  }
  const payload = {
    title: title.value.trim(),
    content: content.value,
    categoryId: categoryId.value || null,
    tagIds: selectedTags.value,
    coverUrl: editCoverUrl.value || null,
    status: 0   // 草稿
  }
  try {
    if (currentArticleId.value) {
      await request.put(`/api/articles/${currentArticleId.value}`, payload)
    } else {
      await request.post('/api/articles', payload)
    }
    await window.$alert('草稿已保存')
    navigate('/archive')   // 保存后返回归档页
  } catch (e) {}
}

// 发布：保存并发布，跳转详情页
const publishArticle = async () => {
  if (!title.value.trim()) {
    await window.$alert('请输入文章标题')
    return
  }
  if (!categoryId.value) {
    await window.$alert('请选择文章分类')
    return
  }
  if (selectedTags.value.length === 0) {
    await window.$alert('请至少选择一个标签')
    return
  }
  const payload = {
    title: title.value.trim(),
    content: content.value,
    categoryId: categoryId.value || null,
    tagIds: selectedTags.value,
    coverUrl: editCoverUrl.value || null,
    status: 1   // 已发布
  }
  try {
    if (currentArticleId.value) {
      await request.put(`/api/articles/${currentArticleId.value}`, payload)
      await window.$alert('文章已发布')
      navigate(`/article/${currentArticleId.value}`)
    } else {
      const res = await request.post('/api/articles', payload)
      const newId = res.data.data
      await window.$alert('文章已发布')
      navigate(`/article/${newId}`)
    }
  } catch (e) {}
}

onMounted(async () => {
  const user = getUserFromToken()
  if (user && user.role === 1) {
    isAdmin.value = true
    await loadCategories()
    await loadTags()
    await loadArticle()
    await checkDraft()   // 检查草稿
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
  background: linear-gradient(135deg, #fce4ec 0%, #e8eaf6 40%, #ede7f6 100%);
  overflow: hidden;
}

/* 伪元素：模糊背景图叠加，并设置透明度让渐变透出 */
.edit-page-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: url('/images/bg.jpg') center/cover no-repeat;
  filter: blur(6px);
  opacity: 0.5;
  z-index: -1;
}

.edit-layout {
  display: flex;
  height: 100%;
}

.edit-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  padding: 1.5rem;
  gap: 1rem;
}

.title-input {
  font-size: 2rem;
  font-weight: 700;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 8px;
  padding: 0.5rem 1rem;
  outline: none;
  transition: border-color 0.2s, background 0.2s;
  color: #111827;
}
.title-input:focus {
  border-color: #111827;
  background: rgba(255, 255, 255, 0.8);
}

.editor-wrapper {
  flex: 1;
  min-height: 0;
  border-radius: 10px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 4px 20px rgba(31, 38, 135, 0.08);
}

.edit-settings {
  width: 320px;
  flex-shrink: 0;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(12px) saturate(150%);
  -webkit-backdrop-filter: blur(12px) saturate(150%);
  border-left: 1px solid rgba(255, 255, 255, 0.5);
  padding: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  overflow-y: auto;
}

.setting-group {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.setting-label {
  font-size: 0.85rem;
  font-weight: 600;
  color: #374151;
}

.setting-select {
  flex: 1;
  padding: 0.5rem 0.75rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  background: #fff;
  font-size: 0.9rem;
}

.setting-input {
  flex: 1;
  padding: 0.45rem 0.65rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 0.85rem;
}

.setting-add-btn,
.setting-confirm-btn {
  padding: 0.5rem 0.8rem;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}

.setting-cancel-btn {
  padding: 0.5rem 0.8rem;
  background: #f3f4f6;
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}

.cover-upload {
  width: 100%;
  height: 120px;
  background: #f9fafb;
  border: 1px dashed #d1d5db;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
}

.cover-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #9ca3af;
}

.tag-item {
  padding: 0.25rem 0.75rem;
  border-radius: 999px;
  background: #f3f4f6;
  color: #4b5563;
  font-size: 0.8rem;
  cursor: pointer;
  transition: background 0.2s, color 0.2s;
}
.tag-item.active {
  background: #111827;
  color: #fff;
}

.publish-btn {
  padding: 0.7rem 1rem;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.95rem;
  cursor: pointer;
  transition: background 0.2s;
}
.publish-btn:hover {
  background: #1f2937;
}

.draft-btn {
  padding: 0.7rem 1rem;
  background: rgba(255, 255, 255, 0.7);
  color: #4b5563;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: background 0.2s;
}
.draft-btn:hover {
  background: rgba(255, 255, 255, 0.9);
}

.cancel-btn {
  padding: 0.7rem 1rem;
  background: transparent;
  color: #9ca3af;
  border: 1px solid transparent;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95rem;
  transition: all 0.2s;
}
.cancel-btn:hover {
  color: #111827;
  border-color: #e5e7eb;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: scale(0.98);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.fade-in {
  animation: fadeIn 0.3s ease-out;
}
</style>

<style>
/* ByteMD 高度链和滚动修复 */
.editor-wrapper {
  height: 100%;
  min-height: 0;
  display: flex;
  flex-direction: column;
}

.editor-wrapper .bytemd {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.editor-wrapper .bytemd-body {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.editor-wrapper .bytemd-editor,
.editor-wrapper .bytemd-preview {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  display: flex;
}

.editor-wrapper .bytemd-editor .CodeMirror {
  flex: 1;
  min-height: 0;
  height: auto !important;
}

.editor-wrapper .bytemd-editor .CodeMirror-scroll {
  height: auto !important;
  max-height: 100%;
  overflow-y: scroll !important;
}

.editor-wrapper .bytemd-preview {
  overflow-y: auto !important;
}
</style>