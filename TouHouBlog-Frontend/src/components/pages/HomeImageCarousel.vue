<template>
  <div
      class="w-full h-full relative overflow-hidden rounded-lg shadow-sm border border-gray-100 cursor-pointer group"
      @click="goToGallery"
  >
    <!-- 图片容器：使用 flex 和 transform 实现滑动 -->
    <div
        class="absolute inset-0 flex transition-transform duration-500 ease-in-out"
        :style="{ transform: `translateX(-${currentIndex * 100}%)` }"
    >
      <img
          v-for="(img, idx) in images"
          :key="idx"
          :src="img"
          alt="图集预览"
          class="w-full h-full object-cover flex-shrink-0"
      />
    </div>

    <!-- 底部指示器 -->
    <div class="absolute bottom-3 left-1/2 transform -translate-x-1/2 flex gap-2 z-10">
      <span
          v-for="(img, idx) in images"
          :key="idx"
          class="w-2 h-2 rounded-full transition-colors"
          :class="idx === currentIndex ? 'bg-white' : 'bg-white/50'"
      ></span>
    </div>

    <!-- 悬浮提示 -->
    <div
        class="absolute inset-0 flex items-center justify-center bg-black/0 group-hover:bg-black/30 transition-colors z-10"
    >
      <span class="text-white text-sm opacity-0 group-hover:opacity-100 transition-opacity">
        点击进入图集 →
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { navigate } from 'astro:transitions/client'

const images = ref([
  'https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=800&h=800&fit=crop',
  'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800&h=800&fit=crop',
  'https://images.unsplash.com/photo-1524413840807-0c3cb6fa808d?w=800&h=800&fit=crop',
  'https://images.unsplash.com/photo-1478436127897-769e1b3f0f36?w=800&h=800&fit=crop',
  'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800&h=800&fit=crop'
])

const currentIndex = ref(0)
let timer = null

const startCarousel = () => {
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % images.value.length
  }, 3000)
}

const goToGallery = () => {
  navigate('/picture')
}

onMounted(() => {
  startCarousel()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
/* 不需要额外样式，transition 已由 Tailwind 类提供 */
</style>