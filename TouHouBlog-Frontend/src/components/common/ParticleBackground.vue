<template>
  <div class="particle-container" aria-hidden="true">
    <div
        v-for="(particle, index) in particles"
        :key="index"
        class="particle"
        :style="particle.style"
    ></div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const particles = ref([])

const createParticles = () => {
  const count = 35   // 减少数量
  const temp = []
  for (let i = 0; i < count; i++) {
    const size = 12 + Math.random() * 14        // 12~26px
    const left = Math.random() * 100
    const top = Math.random() * 100
    const delay = Math.random() * 5
    const duration = 10 + Math.random() * 10    // 10~20s
    const driftX = -30 + Math.random() * 60
    const driftY = -20 + Math.random() * 40
    const blur = 2 + Math.random() * 4          // 2~6px，不要太大
    const opacityStart = 0.6 + Math.random() * 0.4
    const isPink = Math.random() > 0.5
    const color = isPink
        ? 'rgba(249, 168, 212, 0.9)'
        : 'rgba(192, 132, 252, 0.9)'

    temp.push({
      style: {
        left: `${left}%`,
        top: `${top}%`,
        width: `${size}px`,
        height: `${size}px`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`,
        opacity: opacityStart,
        background: `radial-gradient(circle, ${color} 0%, transparent 70%)`,
        filter: `blur(${blur}px)`,   // 静态模糊，不参与动画
        willChange: 'transform, opacity',
        '--drift-x': `${driftX}px`,
        '--drift-y': `${driftY}px`,
      }
    })
  }
  particles.value = temp
}

onMounted(() => {
  createParticles()
})
</script>

<style scoped>
.particle-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: -3;
  overflow: hidden;
}

.particle {
  --drift-x: 0px;
  --drift-y: 0px;
  position: absolute;
  border-radius: 50%;
  will-change: transform, opacity;
  animation: float-particle ease-in-out infinite alternate;
}

@keyframes float-particle {
  0% {
    transform: translate3d(0, 0, 0) scale(0.9);
    opacity: 0.3;
  }
  50% {
    transform: translate3d(calc(var(--drift-x) * 0.5), calc(var(--drift-y) * 0.5), 0) scale(1.05);
    opacity: 0.9;
  }
  100% {
    transform: translate3d(var(--drift-x), var(--drift-y), 0) scale(0.95);
    opacity: 0.5;
  }
}
</style>