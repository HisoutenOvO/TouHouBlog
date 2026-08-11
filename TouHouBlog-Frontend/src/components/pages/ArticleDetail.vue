<template>
  <!-- 加载状态 -->
  <div v-if="loading" class="text-center py-20 text-gray-500">加载中...</div>

  <!-- 编辑模式：全屏 ByteMD 编辑器 -->
  <template v-else-if="isEditing">
    <div class="edit-page-container">
      <!-- 顶部工具栏：标题、分类、保存/取消 -->
      <div class="edit-toolbar">
        <div class="flex items-center gap-4">
          <input v-model="editTitle" type="text" placeholder="文章标题"
                 class="flex-1 px-4 py-2 text-xl font-bold border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-300" />
          <select v-model="editCategoryId" class="border border-gray-200 rounded px-3 py-2">
            <option value="">选择分类</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
          <button @click="saveArticle" class="px-6 py-2 bg-gray-800 text-white rounded-lg hover:bg-gray-900 transition-colors">
            保存修改
          </button>
          <button @click="cancelEdit" class="px-6 py-2 border border-gray-300 rounded-lg text-gray-600 hover:bg-gray-50">
            取消
          </button>
        </div>
      </div>

      <!-- 标签区域 -->
      <div class="edit-tags">
        <div class="flex items-center gap-2 mb-2">
          <span class="text-sm text-gray-500">🏷️ 标签</span>
          <button @click="showTagPanel = !showTagPanel" class="text-xs text-blue-500 hover:underline">
            {{ showTagPanel ? '收起' : '管理标签' }}
          </button>
        </div>
        <!-- 已选标签 -->
        <div class="flex flex-wrap gap-1.5">
          <span v-for="tagId in editSelectedTags" :key="tagId" class="px-2 py-0.5 text-xs rounded-full bg-gray-800 text-white">
            {{ getTagName(tagId) }}
            <button @click="removeTag(tagId)" class="ml-1 text-white hover:text-red-300">&times;</button>
          </span>
        </div>
        <!-- 标签管理面板（可折叠） -->
        <div v-if="showTagPanel" class="mt-2 border border-gray-200 rounded p-2 bg-white">
          <div class="flex flex-wrap gap-1 mb-2">
            <span v-for="tag in allTags" :key="tag.id"
                  class="px-2 py-0.5 text-xs rounded-full cursor-pointer transition-colors"
                  :class="editSelectedTags.includes(tag.id) ? 'bg-gray-800 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'"
                  @click="toggleTag(tag.id)">
              {{ tag.name }}
            </span>
          </div>
          <div class="flex gap-2">
            <input v-model="newTagName" type="text" placeholder="新标签名"
                   class="flex-1 px-2 py-1 text-xs border border-gray-200 rounded"
                   @keyup.enter="createTag" />
            <button @click="createTag" :disabled="!newTagName.trim()"
                    class="px-2 py-1 text-xs bg-gray-100 rounded hover:bg-gray-200">新增</button>
          </div>
        </div>
      </div>

      <!-- ByteMD 编辑器：占满剩余高度 -->
      <div class="edit-editor-full">
        <Editor :value="editContent" :plugins="plugins" :upload-images="uploadImages"
                @change="v => editContent = v" />
      </div>
    </div>
  </template>

  <!-- 查看模式：原有布局（左侧内容 + 右侧侧边栏） -->
  <div v-else-if="article" class="max-w-7xl mx-auto px-4 py-8">
    <div class="flex gap-6">
      <!-- 左侧内容区 -->
      <div class="flex-1 min-w-0">
        <!-- 标题行 -->
        <div class="flex items-center gap-4 mb-3">
          <h1 class="text-3xl font-extrabold text-gray-900">{{ article.title }}</h1>
          <button v-if="isAdmin" @click="enterEditMode"
                  class="text-sm px-3 py-1 border border-gray-300 rounded hover:bg-gray-50 text-gray-600">
            编辑
          </button>
        </div>

        <!-- 日期、分类、点赞 -->
        <div class="flex items-center text-sm text-gray-400 space-x-4 mb-6">
          <span>发布于 {{ formatDate(article.createTime) }}</span>
          <span v-if="article.updateTime">最后修改于 {{ formatDate(article.updateTime) }}</span>
          <span v-if="article.categoryName" class="bg-gray-100 px-2 py-0.5 rounded">{{ article.categoryName }}</span>
          <LikeButton :article-id="articleId" />
        </div>

        <!-- 正文 -->
        <div class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div class="prose prose-lg max-w-none ..." v-html="renderedContent"></div>
        </div>

        <!-- 标签 -->
        <div v-if="article.tags && article.tags.length" class="flex flex-wrap gap-2 mt-6">
          <span v-for="tag in article.tags" :key="tag.id"
                class="px-3 py-1 text-xs rounded-full bg-gray-100 text-gray-600">
            {{ tag.name }}
          </span>
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
  <div v-else class="text-center py-20 text-gray-500">文章不存在。</div>
</template>

<script setup>
// @ts-nocheck
import { ref, computed, onMounted, nextTick } from 'vue'
import request from '../../utils/request'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import hljs from 'highlight.js'
import { Editor } from '@bytemd/vue-next'
import 'bytemd/dist/index.css'
import OSS from 'ali-oss'
import HomeIntro from './indexPage/HomeIntro.vue'
import TableOfContents from './TableOfContents.vue'
import LikeButton from './LikeButton.vue'
import CommentSection from './CommentSection.vue'
import MusicPlayer from './MusicPlayer.vue'
import { getUserFromToken } from '../../utils/auth.js'

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

// 标签相关
const allTags = ref([])            // 所有可用标签
const editSelectedTags = ref([])   // 当前编辑模式下选中的标签ID
const showTagPanel = ref(false)    // 是否显示标签管理面板
const newTagName = ref('')         // 新标签名输入

const plugins = []
let ossClient = null

const md = new MarkdownIt({
  html: true, breaks: true, linkify: true,
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

const updateHeadings = () => {
  if (isEditing.value) {
    headings.value = extractHeadings(renderedContent.value)
  }
}

const uploadImages = async (files) => {
  if (!ossClient) {
    const res = await request.get('/api/oss/signature')
    const data = res.data.data
    ossClient = new OSS({
      region: data.region,
      accessKeyId: data.accessKeyId,
      accessKeySecret: data.accessKeySecret,
      bucket: data.bucket
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

const removeTag = (tagId) => {
  editSelectedTags.value = editSelectedTags.value.filter(id => id !== tagId)
}

const createTag = async () => {
  const name = newTagName.value.trim()
  if (!name) return
  try {
    await request.post('/api/tags', { name })
    newTagName.value = ''
    await loadTags()
    // 不再自动选中新标签
  } catch (e) {
    alert('标签创建失败')
  }
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
  await loadTags() // 加载标签列表
  editTitle.value = article.value.title
  editContent.value = article.value.content
  editCategoryId.value = article.value.categoryId || ''
  editSelectedTags.value = article.value.tags ? article.value.tags.map(t => t.id) : []
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
  const payload = {
    title: editTitle.value,
    content: editContent.value,
    categoryId: editCategoryId.value || null,
    tagIds: editSelectedTags.value
  }
  try {
    await request.put(`/api/articles/${props.articleId}`, payload)
    await fetchArticle()
    isEditing.value = false
  } catch (e) {
    alert('保存失败')
  }
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleDateString('zh-CN')
}

onMounted(fetchArticle)
</script>

<style scoped>
/* ========== 编辑模式全屏布局 ========== */
.edit-page-container {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 200; /* 确保高于导航栏 */
  display: flex;
  flex-direction: column;
  background: white;
  overflow: hidden;
}

.edit-toolbar {
  padding: 0.75rem 1rem;
  padding-top: calc(64px + 0.75rem); /* 留出导航栏高度 (64px) */
  background: white;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
  box-sizing: border-box;
}

.edit-tags {
  padding: 0.5rem 1rem;
  background: #f9fafb;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.edit-editor-full {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>

<style>
/* 强制 ByteMD 占满高度并正确滚动 */
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