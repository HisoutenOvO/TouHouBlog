<template>
  <div class="fixed inset-0 -z-10 overflow-hidden">
    <!-- 背景层：只负责淡入淡出 -->
    <div
        v-for="(img, idx) in images"
        :key="idx"
        class="absolute inset-0 transition-opacity duration-1500 ease-out"
        :class="idx === currentIndex ? 'opacity-100' : 'opacity-0'"
    >
      <!-- 背景内层：负责背景图和持续漂移动画 -->
      <div
          class="w-full h-full bg-cover bg-center bg-drift"
          :style="{
          backgroundImage: `linear-gradient(135deg, rgba(252, 228, 236, 0.75), rgba(232, 234, 246, 0.75), rgba(237, 231, 246, 0.75)), url(${img})`,
        }"
      ></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const images = ref([
  '/images/bg.jpg',
  '/images/bg2.jpg',
  '/images/bg3.jpg',
  '/images/bg4.jpg',
  '/images/bg5.jpg',
  '/images/bgn1.jpg',
  '/images/bgn2.jpg',
  '/images/bgn3.jpg',
])

const currentIndex = ref(0)
let timer = null
const isArticleDetail = ref(false)   // 是否在文章详情页

const startCarousel = () => {
  if (isArticleDetail.value) return
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % images.value.length
  }, 6000)
}

onMounted(() => {
  startCarousel()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
/* 背景漂移动画：持续向右缓慢移动 */
@keyframes bgDrift {
  0%, 100% {
    transform: translateX(0);
  }
  50% {
    transform: translateX(20px);
  }
}

.bg-drift {
  animation: bgDrift 14s ease-in-out infinite;
}
</style>