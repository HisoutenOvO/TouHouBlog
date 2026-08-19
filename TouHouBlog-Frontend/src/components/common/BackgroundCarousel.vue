<template>
  <div class="fixed inset-0 -z-10 overflow-hidden">
    <!-- 背景层：只负责淡入淡出 -->
    <div
        v-for="(img, idx) in images"
        :key="idx"
        class="absolute inset-0 transition-opacity duration-1500 ease-out"
        :class="idx === currentIndex ? 'opacity-100' : 'opacity-0'"
    >
      <!-- 图片层：只负责背景图和漂移动画 -->
      <div
          class="w-full h-full bg-cover bg-center bg-drift"
          :style="{ backgroundImage: `url(${img})` }"
      ></div>
      <!-- 滤镜层：使用全局 CSS 变量，随主题切换自动变化 -->
      <div class="absolute inset-0 bg-carousel-filter"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

// 亮色模式图片列表（根据实际文件名调整）
const lightImages = [
  '/images/bg.jpg',
  '/images/bg2.jpg',
  '/images/bg3.jpg',
  '/images/bg4.jpg',
  '/images/bg5.jpg',
  '/images/bg6.jpg',
]

// 暗色模式图片列表（根据实际文件名调整）
const darkImages = [
  '/images/bgn1.jpg',
  '/images/bgn2.jpg',
  '/images/bgn3.jpg',
  '/images/bgn4.jpg',
  '/images/bgn5.jpg',
]

const currentTheme = ref('light')
const images = ref(lightImages)
const currentIndex = ref(0)

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

// 获取当前主题（优先从全局获取，避免不同步）
const getGlobalTheme = () => {
  return window[THEME_KEY] || document.documentElement.getAttribute('data-theme') || 'light'
}

// 获取当前索引
const getGlobalIndex = () => {
  return typeof window[INDEX_KEY] === 'number' ? window[INDEX_KEY] : 0
}

// 更新本地状态（由全局事件触发）
const updateLocalState = () => {
  const theme = getGlobalTheme()
  if (theme !== currentTheme.value) {
    currentTheme.value = theme
    images.value = theme === 'dark' ? darkImages : lightImages
  }
  const idx = getGlobalIndex() % images.value.length
  currentIndex.value = idx
}

// 启动全局轮播（仅首次调用时有效）
const startGlobalCarousel = () => {
  if (window[TIMER_KEY]) return

  const tick = () => {
    const theme = window[THEME_KEY] || 'light'
    const list = window[IMAGES_KEY][theme] || lightImages
    let idx = window[INDEX_KEY] || 0
    idx = (idx + 1) % list.length
    window[INDEX_KEY] = idx
    // 派发事件，通知所有活跃的背景组件更新显示
    window.dispatchEvent(new CustomEvent('bg-index-change', { detail: { index: idx } }))
  }

  window[TIMER_KEY] = setInterval(tick, 5000)
}

// 监听全局索引变化事件，更新本地状态
const handleIndexChange = (event) => {
  currentIndex.value = event.detail.index % images.value.length
}

// 监听主题变化，更新图片列表和索引（不强制重置索引）
const handleThemeChange = () => {
  const newTheme = document.documentElement.getAttribute('data-theme') || 'light'
  if (newTheme !== currentTheme.value) {
    window[THEME_KEY] = newTheme
    currentTheme.value = newTheme
    images.value = newTheme === 'dark' ? darkImages : lightImages
    // 调整索引到新列表的有效范围
    const list = images.value
    let idx = window[INDEX_KEY] || 0
    idx = idx % list.length
    window[INDEX_KEY] = idx
    currentIndex.value = idx
    // 不派发事件，让定时器继续工作
  }
}

onMounted(() => {
  // 初始化全局索引和主题
  if (typeof window[INDEX_KEY] !== 'number') {
    window[INDEX_KEY] = 0
  }
  if (!window[THEME_KEY]) {
    window[THEME_KEY] = document.documentElement.getAttribute('data-theme') || 'light'
  }

  updateLocalState()
  startGlobalCarousel()

  // 监听全局索引变化
  window.addEventListener('bg-index-change', handleIndexChange)

  // 监听主题变化
  const observer = new MutationObserver(handleThemeChange)
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['data-theme']
  })
  window.__bgObserver = observer
})

onBeforeUnmount(() => {
  // 移除事件监听，但不停止全局定时器
  window.removeEventListener('bg-index-change', handleIndexChange)
  if (window.__bgObserver) {
    window.__bgObserver.disconnect()
    window.__bgObserver = null
  }
})
</script>

<style scoped>
/* 背景漂移动画：持续向右缓慢移动 */
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