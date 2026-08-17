<template>
  <div class="fixed inset-0 -z-10 overflow-hidden">
    <!-- 背景项：淡入淡出切换，每项使用渐变 + 清晰背景图 -->
    <div
        v-for="(img, idx) in images"
        :key="idx"
        class="absolute inset-0 transition-opacity duration-1000 ease-in-out"
        :class="idx === currentIndex ? 'opacity-100' : 'opacity-0'"
        :style="{
        backgroundImage: `linear-gradient(135deg, rgba(252, 228, 236, 0.75), rgba(232, 234, 246, 0.75), rgba(237, 231, 246, 0.75)), url(${img})`,
        backgroundSize: 'cover',
        backgroundPosition: 'center'
      }"
    ></div>
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

const startCarousel = () => {
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % images.value.length
  }, 5000)
}

onMounted(() => {
  startCarousel()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>