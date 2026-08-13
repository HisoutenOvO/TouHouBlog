<template>
  <div
      class="w-full h-full relative overflow-hidden rounded-lg shadow-sm border border-gray-100 cursor-pointer group"
      @click="goToGallery"
  >
    <!-- 轮播图片 -->
    <transition name="fade">
      <img
          :key="currentIndex"
          :src="images[currentIndex]"
          alt="图集预览"
          class="absolute inset-0 w-full h-full object-cover transition-opacity duration-700"
      />
    </transition>

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
    <div class="absolute inset-0 flex items-center justify-center bg-black/0 group-hover:bg-black/30 transition-colors z-10">
      <span class="text-white text-sm opacity-0 group-hover:opacity-100 transition-opacity">
        点击进入图集 →
      </span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { navigate } from 'astro:transitions/client'

// 占位图，后续替换为真实图集数据（从后端获取）
const images = ref([
  'https://images.unsplash.com/photo-1528360983277-13d401cdc186?w=800&h=800&fit=crop', // 日式鸟居
  'https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?w=800&h=800&fit=crop', // 樱花和灯笼
  'https://images.unsplash.com/photo-1524413840807-0c3cb6fa808d?w=800&h=800&fit=crop', // 富士山
  'https://images.unsplash.com/photo-1478436127897-769e1b3f0f36?w=800&h=800&fit=crop', // 日本庭院
  'https://images.unsplash.com/photo-1503899036084-c55cdd92da26?w=800&h=800&fit=crop'  // 秋叶
])

const currentIndex = ref(0)
let timer = null

const startCarousel = () => {
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % images.value.length
  }, 3000) // 3秒切换
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
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.7s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>