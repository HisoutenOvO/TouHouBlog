<template>
  <div class="max-w-7xl mx-auto px-4 py-8">
    <div v-if="loading" class="text-center py-20 text-gray-500">加载中...</div>

    <div v-else-if="article" class="flex gap-6">
      <!-- 左侧内容 -->
      <div class="flex-1 min-w-0">
        <h1 class="text-3xl font-extrabold text-gray-900 mb-3">{{ article.title }}</h1>
        <div class="flex items-center text-sm text-gray-400 space-x-4 mb-6">
          <span>发布于 {{ formatDate(article.createTime) }}</span>
          <span v-if="article.updateTime">最后修改于 {{ formatDate(article.updateTime) }}</span>
          <span v-if="article.categoryName" class="bg-gray-100 px-2 py-0.5 rounded">{{ article.categoryName }}</span>
          <LikeButton :article-id="articleId" />
        </div>

        <!-- 正文渲染为 Markdown -->
        <div class="bg-white border border-gray-200 rounded-lg p-6 shadow-sm">
          <div class="prose max-w-none" v-html="renderedContent"></div>
        </div>

        <!-- 标签 -->
        <div v-if="article.tags && article.tags.length" class="flex flex-wrap gap-2 mt-6">
          <span v-for="tag in article.tags" :key="tag.id"
                class="px-3 py-1 text-xs rounded-full bg-gray-100 text-gray-600">
            {{ tag.name }}
          </span>
        </div>
        <CommentSection :article-id="articleId" />
        <div class="mt-8">
          <a href="/archive" class="text-sm text-gray-400 hover:text-gray-600 no-underline">← 返回归档</a>
        </div>
      </div>

      <!-- 右侧侧边栏 -->
      <aside class="w-72 flex-shrink-0 space-y-4">
        <div class="sticky top-24 space-y-4">
          <HomeIntro />
          <MusicPlayer client:only="vue" />
          <TableOfContents :headings="headings" />
        </div>
      </aside>
    </div>

    <div v-else class="text-center py-20 text-gray-500">文章不存在。</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import MarkdownIt from 'markdown-it'
import markdownItAnchor from 'markdown-it-anchor'
import HomeIntro from './indexPage/HomeIntro.vue'
import TableOfContents from './TableOfContents.vue'
import CommentSection from './CommentSection.vue'
import LikeButton from './LikeButton.vue'
import MusicPlayer from "./MusicPlayer.vue";

const props = defineProps({
  articleId: String
})

const article = ref(null)
const loading = ref(true)
const headings = ref([])

// 初始化 markdown-it
const md = new MarkdownIt({
  html: true,        // 允许 HTML 标签
  breaks: true,      // 转换换行符
  linkify: true      // 自动转换链接
}).use(markdownItAnchor, {
  level: [1, 2, 3],
  slugify: (s) => {
    // 简单的 slugify，去除空格和标点，转为小写
    return s.toLowerCase().replace(/[\s,，。？！：；""''（）—《》【】]+/g, '-').replace(/^-+|-+$/g, '')
  }
})

const renderedContent = computed(() => {
  if (!article.value) return ''
  return md.render(article.value.content)
})

// 提取标题生成目录
const extractHeadings = (html) => {
  const parser = new DOMParser()
  const doc = parser.parseFromString(html, 'text/html')
  const hElements = doc.querySelectorAll('h1, h2, h3')
  const result = []
  hElements.forEach((el) => {
    result.push({
      level: parseInt(el.tagName.charAt(1)),
      text: el.textContent,
      id: el.id
    })
  })
  return result
}

const fetchArticle = async () => {
  try {
    const res = await axios.get(`/api/articles/${props.articleId}`)
    article.value = res.data.data
    // 渲染后提取标题
    const html = md.render(article.value.content)
    headings.value = extractHeadings(html)
  } catch (e) {
    console.error('获取文章详情失败', e)
    article.value = null
  } finally {
    loading.value = false
  }
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  return new Date(datetime).toLocaleDateString('zh-CN')
}



onMounted(fetchArticle)
</script>