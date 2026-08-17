<template>
  <div class="click-ripple-container" aria-hidden="true">
    <div
        v-for="ripple in ripples"
        :key="ripple.id"
        class="click-ripple"
        :style="{ left: ripple.x + 'px', top: ripple.y + 'px' }"
    ></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const ripples = ref([])
let idCounter = 0

const addRipple = (e) => {
  const x = e.clientX
  const y = e.clientY
  const id = ++idCounter
  ripples.value.push({ id, x, y })

  setTimeout(() => {
    ripples.value = ripples.value.filter(r => r.id !== id)
  }, 700)
}

onMounted(() => {
  document.addEventListener('click', addRipple)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', addRipple)
})
</script>

<style scoped>
.click-ripple-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 999;
}

.click-ripple {
  position: absolute;
  width: 120px;          /* 增大尺寸 */
  height: 120px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(192, 132, 252, 0.9) 0%, rgba(249, 213, 229, 0.6) 50%, transparent 70%);
  transform: translate(-50%, -50%) scale(0);
  animation: click-ripple-anim 0.7s ease-out forwards;
}

@keyframes click-ripple-anim {
  from {
    transform: translate(-50%, -50%) scale(0);
    opacity: 1;
  }
  to {
    transform: translate(-50%, -50%) scale(1.5); /* 扩散更大 */
    opacity: 0;
  }
}
</style>