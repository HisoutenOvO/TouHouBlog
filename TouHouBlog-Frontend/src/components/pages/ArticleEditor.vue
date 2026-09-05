<template>
  <div v-if="!isAdmin" class="fade-in flex items-center justify-center h-screen text-gray-500">无权限访问</div>
  <div v-else class="edit-page-container fade-in">
    <div class="edit-layout" :class="{ 'is-fullscreen': isFullscreen }">
      <!-- 左侧写作区 -->
      <div class="edit-main">
        <div class="flex items-center gap-3">
          <input
              v-model="title"
              type="text"
              placeholder="文章标题"
              class="title-input"
          />
          <button class="setting-add-btn" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏编辑'">
            <Icon :icon="isFullscreen ? 'lucide:minimize-2' : 'lucide:maximize-2'" class="w-4 h-4" />
          </button>
        </div>

        <div class="editor-wrapper">
          <MdEditor
              v-model="content"
              :onUploadImg="uploadImages"
              :toolbars="toolbars"
              :footers="[]"
              :theme="isDark ? 'dark' : 'light'"
              preview-theme="default"
              @onSave="saveArticle"
          />
        </div>
      </div>

      <!-- 右侧设置面板 -->
      <div class="edit-settings">
        <!-- 分类 -->
        <div class="setting-group">
          <label class="setting-label">分类</label>
          <div class="flex gap-2 items-center">
            <select v-model="categoryId" class="setting-select">
              <option value="">未分类</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
            <button @click="showAddCategory = true" class="setting-add-btn">
              <Icon icon="lucide:plus" class="w-4 h-4" />
            </button>
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
              <Icon icon="lucide:image-plus" class="w-8 h-8 text-gray-400" />
              <span class="text-xs text-gray-400 mt-1">添加封面</span>
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
              <Icon icon="lucide:plus" class="w-3.5 h-3.5" />
              新增
            </button>
          </div>
        </div>

        <!-- 操作按钮组 -->
        <div class="mt-auto flex flex-col gap-2">
          <button @click="publishArticle" class="publish-btn w-full">
            <Icon icon="lucide:send" class="w-4 h-4" />
            发布
          </button>
          <div class="flex gap-2">
            <button @click="saveDraft" class="draft-btn flex-1">
              <Icon icon="lucide:save" class="w-4 h-4" />
              保存草稿
            </button>
            <button @click="cancelEdit" class="cancel-btn flex-1">
              <Icon icon="lucide:x" class="w-4 h-4" />
              取消
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import { getUserFromToken } from '../../utils/auth'
import request from '../../utils/request'
import OSS from 'ali-oss'
import { navigate } from 'astro:transitions/client'
import { Icon } from '@iconify/vue'

const props = defineProps({
  articleId: { type: String, default: '' }
})

// 主题响应式处理
const theme = ref(
    typeof document !== 'undefined'
        ? document.documentElement.getAttribute('data-theme') || 'light'
        : 'light'
)
let themeObserver = null
const isDark = computed(() => theme.value === 'dark')

const isAdmin = ref(false)
const title = ref('')
const content = ref('')
const categoryId = ref('')
const categories = ref([])

// 当前文章ID：空字符串表示新建，有值表示编辑
const currentArticleId = ref(props.articleId || '')

// 全屏状态
const isFullscreen = ref(false)
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

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

// 初始化 OSS 客户端
const initOssClient = async () => {
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
  return ossClient
}

// 上传封面
const uploadCover = async (e) => {
  const file = e.target.files[0]
  if (!file) return
  try {
    const client = await initOssClient()
    const key = `blog-covers/${Date.now()}_${file.name}`
    const result = await client.put(key, file)
    editCoverUrl.value = result.url
  } catch (err) {
    console.error('封面上传失败', err)
    await window.$alert('封面上传失败，请重试')
  }
}

// md-editor-v3 图片上传回调
const uploadImages = async (files, callback) => {
  try {
    const client = await initOssClient()
    const urls = []
    for (const file of files) {
      const key = `blog-images/${Date.now()}_${file.name || 'image.png'}`
      const result = await client.put(key, file)
      let url = result.url
      if (url && !url.startsWith('http')) {
        url = 'https://' + url
      }
      urls.push(url)
    }
    callback(urls)
  } catch (err) {
    console.error('图片上传失败', err)
    await window.$alert('图片上传失败，请重试')
  }
}

const toolbars = [
  'bold',
  'underline',
  'italic',
  'strikeThrough',
  'sub',
  'sup',
  'quote',
  'unorderedList',
  'orderedList',
  'task',
  'codeRow',
  'code',
  'link',
  'image',
  'table',
  'revoke',
  'next',
  'save',
  'preview',
  'htmlPreview',
  'catalog',
  'github'
]

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

const checkDraft = async () => {
  if (props.articleId) return
  try {
    const res = await request.get('/api/articles/draft')
    const draft = res.data.data
    if (draft && draft.id) {
      const shouldContinue = await window.$confirm('检测到未完成的草稿，是否继续编辑？\n')
      if (shouldContinue) {
        currentArticleId.value = String(draft.id)
        title.value = draft.title || ''
        content.value = draft.content || ''
        categoryId.value = draft.categoryId || ''
        selectedTags.value = draft.tags ? draft.tags.map(t => t.id) : []
        editCoverUrl.value = draft.coverUrl || ''
      } else {
        await request.delete(`/api/articles/${draft.id}`)
      }
    }
  } catch (e) {
    console.error('检查草稿失败', e)
  }
}

const cancelEdit = async () => {
  const confirmed = await window.$confirm('确定要退出编辑吗？未保存的修改将丢失。')
  if (!confirmed) return
  if (currentArticleId.value) {
    navigate(`/article/${currentArticleId.value}`)
  } else {
    navigate('/archive')
  }
}

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
    status: 0
  }
  try {
    if (currentArticleId.value) {
      await request.put(`/api/articles/${currentArticleId.value}`, payload)
    } else {
      await request.post('/api/articles', payload)
    }
    await window.$alert('草稿已保存')
    navigate('/archive')
  } catch (e) {}
}

const saveArticle = () => {
  saveDraft()
}

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
    status: 1
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
  themeObserver = new MutationObserver(() => {
    theme.value = document.documentElement.getAttribute('data-theme') || 'light'
  })
  themeObserver.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  })

  const user = getUserFromToken()
  if (user && user.role === 1) {
    isAdmin.value = true
    await loadCategories()
    await loadTags()
    await loadArticle()
    await checkDraft()
  }
})

onBeforeUnmount(() => {
  if (themeObserver) {
    themeObserver.disconnect()
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

.edit-layout.is-fullscreen .edit-settings {
  display: none;
}

.edit-layout.is-fullscreen .edit-main {
  padding: 0;
}

.edit-layout.is-fullscreen .editor-wrapper {
  border-radius: 0;
  height: 100vh;
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
  border: 1px solid #e8d5f5;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.6);
  font-size: 0.9rem;
  appearance: none;
  -webkit-appearance: none;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='12' height='12' viewBox='0 0 12 12'%3E%3Cpath fill='%236b7280' d='M6 8L1 3h10z'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 0.75rem center;
  padding-right: 2rem;
  cursor: pointer;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.setting-select:focus {
  border-color: #d8b4e8;
  box-shadow: 0 0 0 2px rgba(216, 180, 232, 0.2);
  outline: none;
}

.setting-add-btn {
  width: 32px;
  height: 32px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 50%;
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s ease;
  flex-shrink: 0;
}

.setting-add-btn:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  transform: scale(1.1);
}

.setting-input {
  flex: 1;
  padding: 0.45rem 0.65rem;
  border: 1px solid #e8d5f5;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.6);
  font-size: 0.85rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}

.setting-input:focus {
  border-color: #d8b4e8;
  box-shadow: 0 0 0 2px rgba(216, 180, 232, 0.2);
  outline: none;
}

.setting-confirm-btn {
  padding: 0.5rem 0.8rem;
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  transition: all 0.2s ease;
}

.setting-confirm-btn:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  color: #523b52;
}

.setting-cancel-btn {
  padding: 0.5rem 0.8rem;
  background: rgba(255, 255, 255, 0.6);
  color: #374151;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85rem;
}

.cover-upload {
  width: 100%;
  height: 120px;
  background: rgba(255, 255, 255, 0.5);
  border: 1.5px dashed #d8b4e8;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;
  transition: border-color 0.25s, box-shadow 0.25s, background 0.25s;
}

.cover-upload:hover {
  border-color: #c084fc;
  box-shadow: 0 0 0 3px rgba(192, 132, 252, 0.15);
  background: rgba(255, 255, 255, 0.7);
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
  background: rgba(255, 255, 255, 0.6);
  color: #4b5563;
  font-size: 0.8rem;
  cursor: pointer;
  border: 1px solid rgba(255, 255, 255, 0.6);
  transition: all 0.2s ease;
}

.tag-item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.8);
}

.tag-item.active {
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  border-color: rgba(255, 255, 255, 0.7);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
}

.publish-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem 1.2rem;
  border: none;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  color: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.2s ease;
}

.publish-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  transform: translateY(-1px);
}

.draft-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.5);
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  transition: all 0.2s ease;
}

.draft-btn:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  transform: translateY(-1px);
}

.cancel-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem 1.2rem;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  font-size: 0.9rem;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.5);
  color: #6b7280;
  transition: all 0.2s ease;
}

.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.8);
  color: #111827;
}

@keyframes fadeIn {
  from { opacity: 0; transform: scale(0.98); }
  to { opacity: 1; transform: scale(1); }
}

.fade-in {
  animation: fadeIn 0.3s ease-out;
}

/* ========== 暗色模式适配（仅编辑页面外部样式） ========== */
[data-theme="dark"] .edit-page-container {
  background: linear-gradient(135deg, #1a1025 0%, #2a1a35 40%, #1e1430 100%);
}

[data-theme="dark"] .edit-page-container::before {
  background: url('/images/bgn1.jpg') center/cover no-repeat;
  filter: blur(6px);
  opacity: 0.6;
}

[data-theme="dark"] .title-input {
  background: var(--input-bg);
  border-color: var(--card-border);
  color: var(--text-primary);
}
[data-theme="dark"] .title-input:focus {
  border-color: var(--input-focus-border);
  background: var(--input-bg);
}

[data-theme="dark"] .editor-wrapper {
  background: var(--card-bg);
  border-color: var(--card-border);
}

[data-theme="dark"] .edit-settings {
  background: var(--card-bg);
  border-left-color: var(--card-border);
}

[data-theme="dark"] .setting-label {
  color: var(--text-primary);
}

[data-theme="dark"] .setting-select,
[data-theme="dark"] .setting-input {
  background: var(--input-bg);
  border-color: var(--input-border);
  color: var(--text-primary);
}
[data-theme="dark"] .setting-select:focus,
[data-theme="dark"] .setting-input:focus {
  border-color: var(--input-focus-border);
}

[data-theme="dark"] .setting-add-btn {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  border-color: var(--card-border);
}
[data-theme="dark"] .setting-add-btn:hover {
  background: var(--btn-primary-hover-bg);
}

[data-theme="dark"] .setting-confirm-btn {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  border-color: var(--card-border);
}
[data-theme="dark"] .setting-confirm-btn:hover {
  background: var(--btn-primary-hover-bg);
}

[data-theme="dark"] .setting-cancel-btn {
  background: var(--input-bg);
  color: var(--text-secondary);
  border-color: var(--table-border);
}

[data-theme="dark"] .cover-upload {
  background: var(--input-bg);
  border-color: var(--input-border);
}
[data-theme="dark"] .cover-upload:hover {
  background: var(--btn-primary-hover-bg);
  border-color: var(--input-focus-border);
}

[data-theme="dark"] .cover-placeholder {
  color: var(--text-muted);
}

[data-theme="dark"] .tag-item {
  background: var(--input-bg);
  color: var(--text-secondary);
  border-color: var(--card-border);
}
[data-theme="dark"] .tag-item:hover {
  background: var(--btn-primary-hover-bg);
}
[data-theme="dark"] .tag-item.active {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  border-color: var(--card-border);
}

[data-theme="dark"] .publish-btn {
  background: var(--btn-gradient-bg);
  color: white;
}

[data-theme="dark"] .draft-btn {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  border-color: var(--card-border);
}
[data-theme="dark"] .draft-btn:hover {
  background: var(--btn-primary-hover-bg);
}

[data-theme="dark"] .cancel-btn {
  background: var(--input-bg);
  color: var(--text-secondary);
  border-color: var(--table-border);
}
[data-theme="dark"] .cancel-btn:hover {
  background: var(--btn-primary-hover-bg);
  color: var(--text-primary);
}
</style>

<style>
/* md-editor-v3 容器填充 */
.editor-wrapper .md-editor {
  height: 100%;
}
</style>