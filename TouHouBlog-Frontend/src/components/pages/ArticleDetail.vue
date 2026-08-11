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
          <div class="prose prose-lg max-w-none
                   prose-headings:text-gray-900
                   prose-p:text-gray-700 prose-p:leading-relaxed
                   prose-a:text-blue-500 prose-a:no-underline hover:prose-a:underline
                   prose-strong:text-gray-900 prose-strong:font-semibold
                   prose-blockquote:border-l-4 prose-blockquote:border-gray-300 prose-blockquote:pl-4 prose-blockquote:text-gray-600
                   prose-code:bg-gray-100 prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded prose-code:text-sm prose-code:font-normal
                   prose-pre:bg-gray-900 prose-pre:text-gray-100 prose-pre:rounded-lg prose-pre:shadow-lg
                   prose-ol:list-decimal prose-ul:list-disc prose-li:text-gray-700
                   prose-table:border-collapse prose-table:w-full
                   prose-th:border prose-th:border-gray-300 prose-th:bg-gray-50 prose-th:px-4 prose-th:py-2
                   prose-td:border prose-td:border-gray-300 prose-td:px-4 prose-td:py-2
                   prose-img:rounded-lg prose-img:shadow-md"
               v-html="renderedContent"></div>
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
import axios from 'axios'
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
    const res = await axios.get('/api/oss/signature')
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
  const res = await axios.get('/api/categories/list?page=1&pageSize=999')
  categories.value = res.data.data.records
}

const fetchArticle = async () => {
  try {
    const res = await axios.get(`/api/articles/${props.articleId}`)
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
  editTitle.value = article.value.title
  editContent.value = article.value.content
  editCategoryId.value = article.value.categoryId || ''
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
    tagIds: []
  }
  try {
    await axios.put(`/api/articles/${props.articleId}`, payload)
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
  z-index: 200; /* 提高层级确保覆盖导航栏 */
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