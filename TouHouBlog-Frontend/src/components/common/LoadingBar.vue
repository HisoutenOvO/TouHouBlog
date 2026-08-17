<template>
  <div v-if="loading" class="loading-bar"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const loading = ref(false)
let hideTimer = null

const startLoading = () => {
  loading.value = true
  if (hideTimer) clearTimeout(hideTimer)
  // 如果页面加载超过 1.5 秒仍未结束，自动隐藏，避免一直显示
  hideTimer = setTimeout(() => {
    loading.value = false
  }, 1500)
}

const stopLoading = () => {
  // 延迟一点隐藏，让进度条走完动画
  setTimeout(() => {
    loading.value = false
  }, 300)
}

onMounted(() => {
  document.addEventListener('astro:before-preparation', startLoading)
  document.addEventListener('astro:after-swap', stopLoading)
  document.addEventListener('astro:page-load', stopLoading)
})

onBeforeUnmount(() => {
  document.removeEventListener('astro:before-preparation', startLoading)
  document.removeEventListener('astro:after-swap', stopLoading)
  document.removeEventListener('astro:page-load', stopLoading)
  if (hideTimer) clearTimeout(hideTimer)
})
</script>

<style scoped>
.loading-bar {
  position: fixed;
  top: 0;
  left: 0;
  height: 3px;
  width: 100%;
  z-index: 9999;
  background: linear-gradient(90deg, #7c3aed, #ec4899, #3b82f6);
  animation: loading-progress 0.6s ease-out forwards;
  box-shadow: 0 0 10px rgba(124, 58, 237, 0.4);
}

@keyframes loading-progress {
  0% {
    width: 0%;
  }
  100% {
    width: 100%;
  }
}
</style>