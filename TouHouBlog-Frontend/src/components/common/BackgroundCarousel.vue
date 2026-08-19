<template>
  <div class="fixed inset-0 -z-10 overflow-hidden">
    <!-- 背景层：只负责淡入淡出 -->
    <div
        v-for="(img, idx) in images"
        :key="idx"
        class="absolute inset-0 transition-opacity duration-1500 ease-out"
        :class="idx === currentIndex ? 'opacity-100' : 'opacity-0'"
    >
      <!-- 图片层：只负责背景图和漂移动画（文章页不加漂移动画） -->
      <div
          class="w-full h-full bg-cover bg-center"
          :class="{ 'bg-drift': !isArticlePage }"
          :style="{ backgroundImage: `url(${img})` }"
      ></div>
      <!-- 滤镜层：使用全局 CSS 变量，随主题切换自动变化 -->
      <div class="absolute inset-0 bg-carousel-filter"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

// 亮色模式图片列表
const lightImages = [
  '/images/bg.jpg',
  '/images/bg2.jpg',
  '/images/bg3.jpg',
  '/images/bg4.jpg',
  '/images/bg5.jpg',
  '/images/bg6.jpg',
]

// 暗色模式图片列表
const darkImages = [
  '/images/bgn5.jpg',
  '/images/bgn2.jpg',
  '/images/bgn3.jpg',
  '/images/bgn4.jpg',
  '/images/bgn1.jpg',
]

const currentTheme = ref('light')
const images = ref(lightImages)
const currentIndex = ref(0)
const isArticlePage = ref(false)  // 是否为文章详情页

// 全局状态键名
const INDEX_KEY = '__bgCurrentIndex'
const THEME_KEY = '__bgCurrentTheme'
const TIMER_KEY = '__bgCarouselTimer'
const IMAGES_KEY = '__bgImages'

// 初始化全局图片列表（只需一次）
if (typeof window !== 'undefined' && !window[IMAGES_KEY]) {
  window[IMAGES_KEY] = {
    light: lightImages,
    dark: darkImages
  }
}

const getGlobalTheme = () => {
  return window[THEME_KEY] || document.documentElement.getAttribute('data-theme') || 'light'
}

const getGlobalIndex = () => {
  return typeof window[INDEX_KEY] === 'number' ? window[INDEX_KEY] : 0
}

// 更新本地状态（文章页强制索引为 0）
const updateLocalState = () => {
  const theme = getGlobalTheme()
  if (theme !== currentTheme.value) {
    currentTheme.value = theme
    images.value = theme === 'dark' ? darkImages : lightImages
  }

  if (isArticlePage.value) {
    // 文章页固定显示第一张背景图，不跟随全局轮播
    currentIndex.value = 0
  } else {
    const idx = getGlobalIndex() % images.value.length
    currentIndex.value = idx
  }
}

// 启动全局轮播（仅非文章页且全局定时器不存在时启动）
const startGlobalCarousel = () => {
  if (isArticlePage.value) return
  if (window[TIMER_KEY]) return

  const tick = () => {
    const theme = window[THEME_KEY] || 'light'
    const list = window[IMAGES_KEY][theme] || lightImages
    let idx = window[INDEX_KEY] || 0
    idx = (idx + 1) % list.length
    window[INDEX_KEY] = idx
    window.dispatchEvent(new CustomEvent('bg-index-change', { detail: { index: idx } }))
  }

  window[TIMER_KEY] = setInterval(tick, 5000)
}

// 监听全局索引变化事件（文章页忽略）
const handleIndexChange = (event) => {
  if (isArticlePage.value) return  // 关键：文章页不响应轮播事件
  currentIndex.value = event.detail.index % images.value.length
}

// 监听主题变化
const handleThemeChange = () => {
  const newTheme = document.documentElement.getAttribute('data-theme') || 'light'
  if (newTheme !== currentTheme.value) {
    window[THEME_KEY] = newTheme
    currentTheme.value = newTheme
    images.value = newTheme === 'dark' ? darkImages : lightImages

    if (isArticlePage.value) {
      // 文章页依然固定第一张
      currentIndex.value = 0
    } else {
      let idx = window[INDEX_KEY] || 0
      idx = idx % images.value.length
      window[INDEX_KEY] = idx
      currentIndex.value = idx
    }
  }
}

onMounted(() => {
  isArticlePage.value = window.location.pathname.startsWith('/article/')

  if (typeof window[INDEX_KEY] !== 'number') {
    window[INDEX_KEY] = 0
  }
  if (!window[THEME_KEY]) {
    window[THEME_KEY] = document.documentElement.getAttribute('data-theme') || 'light'
  }

  updateLocalState()
  startGlobalCarousel()

  // 文章页不监听全局索引变化事件，确保背景不切换
  if (!isArticlePage.value) {
    window.addEventListener('bg-index-change', handleIndexChange)
  }

  const observer = new MutationObserver(handleThemeChange)
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  })
  window.__bgObserver = observer
})

onBeforeUnmount(() => {
  window.removeEventListener('bg-index-change', handleIndexChange)
  if (window.__bgObserver) {
    window.__bgObserver.disconnect()
    window.__bgObserver = null
  }
})
</script>

<style scoped>
/* 背景漂移动画：持续向右缓慢移动（仅当 .bg-drift 类存在时生效） */
@keyframes bgDrift {
  0%, 100% {
    transform: scale(1.1) translateX(0);
  }
  50% {
    transform: scale(1.1) translateX(15px);
  }
}

.bg-drift {
  animation: bgDrift 12s ease-in-out infinite;
}

/* 滤镜层：使用全局变量，自动响应亮暗主题 */
.bg-carousel-filter {
  background-image: var(--bg-image);
  pointer-events: none;
}
</style>