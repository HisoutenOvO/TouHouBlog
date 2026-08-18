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
  '/images/bg6.jpg',
  '/images/bgn1.jpg',
  '/images/bgn2.jpg',
  '/images/bgn3.jpg',
  '/images/bgn4.jpg',
  '/images/bgn5.jpg',
])

const currentIndex = ref(0)
let timer = null

const startCarousel = () => {
  if (timer) clearInterval(timer)
  timer = setInterval(() => {
    nextImage()
  }, 5000)
}

const nextImage = () => {
  currentIndex.value = (currentIndex.value + 1) % images.value.length
  // 保存到全局，即使组件重建也能恢复
  if (typeof window !== 'undefined') {
    window.__bgCurrentIndex = currentIndex.value
  }
}

const stopCarousel = () => {
  if (timer) clearInterval(timer)
  timer = null
}

onMounted(() => {
  // 如果之前有保存的索引，恢复
  if (typeof window !== 'undefined' && window.__bgCurrentIndex !== undefined) {
    currentIndex.value = window.__bgCurrentIndex % images.value.length
  }
  startCarousel()
})

onBeforeUnmount(() => {
  stopCarousel()
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

</style>