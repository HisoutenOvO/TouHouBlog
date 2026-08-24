<template>
  <div v-if="loading" class="loading-wrapper">
    <div class="loading-bar"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const loading = ref(false)
let hideTimer = null

const startLoading = () => {
  loading.value = true
  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => {
    loading.value = false
  }, 1800)
}

const stopLoading = () => {
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
.loading-wrapper {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  z-index: 10001;
  pointer-events: none;
}

.loading-bar {
  height: 3px;
  width: 100%;
  background: linear-gradient(90deg, #7c3aed, #ec4899, #3b82f6);
  box-shadow: 0 0 15px rgba(124, 58, 237, 0.8);
  animation: loading-progress 0.8s ease-out forwards;
}

.loading-text {
  position: absolute;
  top: 8px;
  right: 16px;
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 0.8rem;
  letter-spacing: 0.1em;
  color: #6b4b6b;
  background: rgba(255, 255, 255, 0.75);
  backdrop-filter: blur(8px);
  padding: 0.2rem 0.8rem;
  border-radius: 999px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}

@keyframes loading-progress {
  0% { width: 0%; opacity: 0; }
  10% { opacity: 1; }
  100% { width: 100%; opacity: 0.6; }
}
</style>