<template>
  <!-- 加载状态 -->
  <div v-if="loading" class="text-center py-20 text-gray-500">加载中...</div>

  <!-- 编辑/查看模式切换过渡 -->
  <div v-else>
    <Transition name="edit" mode="out-in">
      <!-- 编辑模式 -->
      <div v-if="isEditing" key="editor" class="edit-page-container">
        <div class="edit-layout" :class="{ 'is-fullscreen': isFullscreen }">
          <!-- 左侧写作区 -->
          <div class="edit-main">
            <div class="flex items-center gap-3">
              <input
                  v-model="editTitle"
                  type="text"
                  placeholder="文章标题"
                  class="title-input"
              />
              <!-- 全屏切换按钮 -->
              <button class="setting-add-btn" @click="toggleFullscreen" :title="isFullscreen ? '退出全屏' : '全屏编辑'">
                <Icon :icon="isFullscreen ? 'lucide:minimize-2' : 'lucide:maximize-2'" class="w-4 h-4" />
              </button>
            </div>
            <div class="editor-wrapper">
              <MdEditor
                  v-model="editContent"
                  :onUploadImg="uploadImages"
                  :toolbars="toolbars"
                  :footers="[]"
                  preview-theme="github"
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
                <select v-model="editCategoryId" class="setting-select">
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
                    :class="{ active: editSelectedTags.includes(tag.id) }"
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

            <!-- 操作按钮：仅编辑模式 -->
            <div class="mt-auto flex flex-col gap-2">
              <button @click="saveArticle" class="publish-btn w-full">
                <Icon icon="lucide:save" class="w-4 h-4" />
                保存修改
              </button>
              <button @click="cancelEdit" class="cancel-btn w-full">
                <Icon icon="lucide:x" class="w-4 h-4" />
                取消
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 查看模式 -->
      <div v-else-if="article" key="viewer" class="max-w-7xl mx-auto px-4 py-8">
        <div class="flex gap-6">
          <!-- 左侧内容区 -->
          <div class="flex-1 min-w-0">
            <!-- 封面图 -->
            <div v-if="article.coverUrl" class="mb-6">
              <div class="w-full" style="aspect-ratio: 16 / 5; overflow: hidden; border-radius: 0.5rem;">
                <img :src="article.coverUrl" class="w-full h-full object-cover" />
              </div>
            </div>

            <div class="article-content-card">
              <!-- 标题行 -->
              <div class="flex items-center gap-3 mb-2">
                <h1 class="text-3xl font-extrabold text-gray-900">{{ article.title }}</h1>
                <button v-if="isAdmin" @click="enterEditMode"
                        class="admin-action-btn edit">
                  <Icon icon="lucide:pencil" class="w-4 h-4" />
                  编辑
                </button>
                <button v-if="isAdmin" @click="deleteArticle"
                        class="admin-action-btn delete">
                  <Icon icon="lucide:trash-2" class="w-4 h-4" />
                  删除
                </button>
              </div>

              <!-- 日期、分类、点赞 -->
              <div class="flex items-center text-sm text-gray-500 space-x-4 pb-4 mb-4 border-b border-gray-100">
                <span>发布于 {{ formatDate(article.createTime) }}</span>
                <span v-if="article.updateTime">最后修改于 {{ formatDate(article.updateTime) }}</span>
                <a
                    v-if="article.categoryName"
                    @click="goCategory(article.categoryId)"
                    class="category-chip"
                    title="查看该分类下的文章"
                >
                  <Icon icon="lucide:folder" class="w-3.5 h-3.5" />
                  {{ article.categoryName }}
                </a>
                <LikeButton :article-id="articleId" />
              </div>

              <!-- 正文 -->
              <div class="prose prose-lg max-w-none ..." v-html="renderedContent"></div>
            </div>

            <!-- 标签 -->
            <div v-if="article.tags && article.tags.length" class="flex flex-wrap gap-2 mt-6">
              <a
                  v-for="tag in article.tags"
                  :key="tag.id"
                  @click="goTag(tag.id)"
                  class="tag-chip"
                  title="查看该标签下的文章"
              >
                <Icon icon="lucide:tag" class="w-3.5 h-3.5" />
                {{ tag.name }}
              </a>
            </div>

            <!-- 评论区 -->
            <CommentSection :article-id="articleId" />

            <div class="mt-8">
              <a href="/archive" class="text-sm text-gray-400 hover:text-gray-600 no-underline">← 返回归档</a>
            </div>
          </div>

          <!-- 右侧侧边栏 -->
          <aside class="w-72 flex-shrink-0 space-y-4">
            <div class="sticky top-24 space-y-4">
              <HomeIntro />
              <MusicPlayer />
              <TableOfContents :headings="headings" />
            </div>
          </aside>
        </div>
      </div>

      <!-- 文章不存在 -->
      <div v-else key="notfound" class="text-center py-20 text-gray-500">文章不存在。</div>
    </Transition>
  </div>
</template>

<script setup>
// @ts-nocheck
import { ref, computed, onMounted, nextTick } from 'vue'
import request from '../../utils/request'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import hljs from 'highlight.js'
import { MdEditor } from 'md-editor-v3'
import 'md-editor-v3/lib/style.css'
import OSS from 'ali-oss'
import HomeIntro from './indexPage/HomeIntro.vue'
import TableOfContents from './TableOfContents.vue'
import LikeButton from './LikeButton.vue'
import CommentSection from './CommentSection.vue'
import MusicPlayer from './MusicPlayer.vue'
import { getUserFromToken } from '../../utils/auth.js'
import { Icon } from '@iconify/vue'
import { navigate } from 'astro:transitions/client'

const props = defineProps({ articleId: String })

const article = ref(null)
const loading = ref(true)
const headings = ref([])
const isAdmin = ref(false)
const isEditing = ref(false)

const editTitle = ref('')
const editContent = ref('')
const editCategoryId = ref('')
const categories = ref([])

// 全屏状态
const isFullscreen = ref(false)
const toggleFullscreen = () => {
  isFullscreen.value = !isFullscreen.value
}

// 标签相关
const allTags = ref([])
const editSelectedTags = ref([])
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

const md = new MarkdownIt({
  html: false, breaks: true, linkify: true,
  highlight: function (str, lang) {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return '<pre class="hljs"><code>' + hljs.highlight(str, { language: lang, ignoreIllegals: true }).value + '</code></pre>'
      } catch (__) {}
    }
    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  }
}).use(markdownItAnchor, {
  level: [1,2,3],
  slugify: s => s.toLowerCase().replace(/[\s,，。？！：；""''（）—《》【】]+/g, '-').replace(/^-+|-+$/g, '')
})

const renderedContent = computed(() => {
  const raw = isEditing.value ? editContent.value : (article.value?.content || '')
  return raw ? md.render(raw) : ''
})

const extractHeadings = (html) => {
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  return Array.from(doc.querySelectorAll('h1, h2, h3')).map(el => ({
    level: parseInt(el.tagName.charAt(1)),
    text: el.textContent,
    id: el.id
  }))
}

const goCategory = (id) => {
  navigate(`/archive?categoryId=${id}`)
}

const goTag = (id) => {
  navigate(`/archive?tagId=${id}`)
}

const updateHeadings = () => {
  if (isEditing.value) {
    headings.value = extractHeadings(renderedContent.value)
  }
}

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
    urls.push(result.url)
  }
  return urls
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
  try {
    const res = await request.get('/api/tags/list?page=1&pageSize=999')
    allTags.value = res.data.data.records
  } catch (e) {
    console.error('加载标签失败', e)
  }
}

const getTagName = (id) => {
  const tag = allTags.value.find(t => t.id == id)
  return tag ? tag.name : ''
}

const toggleTag = (tagId) => {
  const index = editSelectedTags.value.indexOf(tagId)
  if (index === -1) {
    editSelectedTags.value.push(tagId)
  } else {
    editSelectedTags.value.splice(index, 1)
  }
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
    if (newCat) editCategoryId.value = newCat.id
    showAddCategory.value = false
    newCategoryName.value = ''
  } catch (e) {}
}

const fetchArticle = async () => {
  try {
    const res = await request.get(`/api/articles/${props.articleId}`)
    article.value = res.data.data
    const html = md.render(article.value.content)
    headings.value = extractHeadings(html)
    const user = getUserFromToken()
    isAdmin.value = !!(user && user.role === 1)
  } catch (e) {
    article.value = null
  } finally {
    loading.value = false
  }
}

const enterEditMode = async () => {
  await loadCategories()
  await loadTags()
  editTitle.value = article.value.title
  editContent.value = article.value.content
  editCategoryId.value = article.value.categoryId || ''
  editSelectedTags.value = article.value.tags ? article.value.tags.map(t => t.id) : []
  editCoverUrl.value = article.value.coverUrl || ''
  isEditing.value = true
  await nextTick()
  updateHeadings()
}

const cancelEdit = () => {
  isEditing.value = false
  const html = md.render(article.value.content)
  headings.value = extractHeadings(html)
}

const saveArticle = async () => {
  if (!editTitle.value.trim()) {
    await window.$alert('请输入文章标题')
    return
  }
  if (!editCategoryId.value) {
    await window.$alert('请选择文章分类')
    return
  }
  if (editSelectedTags.value.length === 0) {
    await window.$alert('请至少选择一个标签')
    return
  }
  const payload = {
    title: editTitle.value.trim(),
    content: editContent.value,
    categoryId: editCategoryId.value,
    tagIds: editSelectedTags.value,
    coverUrl: editCoverUrl.value || null
  }
  try {
    await request.put(`/api/articles/${props.articleId}`, payload)
    await fetchArticle()
    isEditing.value = false
  } catch (e) {}
}

const deleteArticle = async () => {
  const confirmed = await window.$confirm('确定要删除这篇文章吗？删除后无法恢复。')
  if (!confirmed) return
  try {
    await request.delete(`/api/articles/${props.articleId}`)
    navigate('/archive')
  } catch (e) {}
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleDateString('zh-CN')
}

onMounted(fetchArticle)
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

/* 编辑/查看模式切换动画 */
.edit-enter-active,
.edit-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.edit-enter-from {
  opacity: 0;
  transform: scale(0.98);
}
.edit-leave-to {
  opacity: 0;
  transform: scale(0.98);
}

/* 管理员操作按钮 */
.admin-action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.4rem 1rem;
  border-radius: 9999px;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}
.admin-action-btn.edit {
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
}
.admin-action-btn.edit:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  color: #523b52;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
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

.category-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: linear-gradient(135deg, #dbeafe, #c7d2fe);
  color: #3730a3;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}
.category-chip:hover {
  background: linear-gradient(135deg, #c7d2fe, #b1bcf5);
  color: #312e81;
  box-shadow: 0 3px 8px rgba(0, 0, 0, 0.08);
  transform: translateY(-1px);
}

.tag-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s ease;
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.6);
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
}
.tag-chip:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  color: #523b52;
  box-shadow: 0 3px 8px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}
</style>

<style>
/* md-editor-v3 容器填充 */
.editor-wrapper .md-editor {
  height: 100%;
}
</style>