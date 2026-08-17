<template>
  <div class="sakura-container" aria-hidden="true">
    <div
        v-for="(petal, index) in petals"
        :key="index"
        class="sakura-petal"
        :style="petal.style"
    ></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const petals = ref([])

const createPetals = () => {
  const count = 50   // 增加到 35 片
  const temp = []
  for (let i = 0; i < count; i++) {
    const size = 8 + Math.random() * 10   // 8px ~ 18px，稍大
    const left = Math.random() * 100
    const delay = Math.random() * 8       // 延迟更分散
    const duration = 6 + Math.random() * 8 // 6~14s，稍快
    const sway = -40 + Math.random() * 80  // 摆动幅度 -40px ~ 40px
    const opacityStart = 0.6 + Math.random() * 0.3 // 0.6~0.9，更清晰
    const rotate = Math.random() * 360

    temp.push({
      style: {
        left: `${left}%`,
        width: `${size}px`,
        height: `${size}px`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`,
        opacity: opacityStart,
        transform: `rotate(${rotate}deg)`,
        '--sway': `${sway}px`,
        '--opacity-start': opacityStart
      }
    })
  }
  petals.value = temp
}

onMounted(() => {
  createPetals()
})
</script>

<style scoped>
.sakura-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -5;
  overflow: hidden;
}

.sakura-petal {
  --sway: 0px;
  --opacity-start: 0.8;
  position: absolute;
  top: -20px;
  background: linear-gradient(135deg, #f9a8d4, #f472b6); /* 颜色更饱和 */
  border-radius: 50% 0 50% 50%;
  box-shadow: 0 0 6px rgba(244, 114, 182, 0.8); /* 阴影更明显 */
  animation: petal-fall linear infinite;
}

@keyframes petal-fall {
  0% {
    transform: translate(0, -10vh) rotate(0deg);
    opacity: 0;
  }
  5% {
    opacity: var(--opacity-start);
  }
  100% {
    transform: translate(var(--sway), 110vh) rotate(360deg);
    opacity: 0.3;
  }
}
</style>