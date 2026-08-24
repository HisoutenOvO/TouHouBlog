<template>
  <Transition name="splash">
    <div v-if="visible" class="splash-overlay" @click="dismiss">
      <!-- 流动渐变背景 -->
      <div class="splash-bg-animated"></div>

      <!-- 旋转光环 -->
      <div class="magic-circle"></div>
      <div class="magic-circle small"></div>

      <!-- 樱花层 -->
      <div class="sakura-layer" ref="sakuraLayer">
        <div
            v-for="(petal, index) in petals"
            :key="index"
            class="sakura-petal"
            :style="petal.style"
        ></div>
      </div>

      <!-- 上升粒子层 -->
      <div class="particle-layer">
        <div
            v-for="(particle, index) in particles"
            :key="index"
            class="rising-particle"
            :style="particle.style"
        ></div>
      </div>

      <!-- 标题文字：站长与游客不同 -->
      <h1 class="splash-welcome">
        <span
            v-for="(char, index) in (isAdmin ? adminChars : welcomeChars)"
            :key="index"
            class="char"
            :style="{ animationDelay: `${index * 0.3}s` }"
        >{{ char }}</span>
      </h1>
      <p class="splash-blog-name">{{ isAdmin ? 'Hisouten 站长～' : 'TouHouBlog' }}</p>
      <p class="splash-desc">{{ isAdmin ? '今天也请继续书写属于您与幻想乡的故事吧～' : '只属于你我的幻想世界' }}</p>
      <p class="splash-hint">点击任意处进入</p>
    </div>
  </Transition>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { getUserFromToken } from '../../utils/auth'

const visible = ref(false)
const canDismiss = ref(false)
const welcomeChars = ['欢', '迎', '来', '到']
const adminChars = ['欢', '迎', '回', '家']
const petals = ref([])
const particles = ref([])
const sakuraLayer = ref(null)
let dismissTimer = null
let mouseX = 0
let mouseY = 0

const user = ref(null)
const isAdmin = computed(() => user.value?.role === 1)

// 创建樱花花瓣
const createPetals = () => {
  const temp = []
  for (let i = 0; i < 30; i++) {
    const size = 8 + Math.random() * 14
    const left = Math.random() * 100
    const delay = Math.random() * 5
    const duration = 6 + Math.random() * 6
    const sway = -60 + Math.random() * 120
    const opacity = 0.5 + Math.random() * 0.4
    const rotate = Math.random() * 360
    temp.push({
      style: {
        left: `${left}%`,
        width: `${size}px`,
        height: `${size}px`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`,
        opacity: opacity,
        transform: `rotate(${rotate}deg)`,
        '--sway': `${sway}px`
      }
    })
  }
  petals.value = temp
}

// 创建上升粒子
const createParticles = () => {
  const temp = []
  for (let i = 0; i < 20; i++) {
    const size = 2 + Math.random() * 5
    const left = Math.random() * 100
    const delay = Math.random() * 4
    const duration = 4 + Math.random() * 5
    const opacity = 0.3 + Math.random() * 0.5
    const hue = Math.random() > 0.5 ? '#f9a8d4' : '#c084fc'
    temp.push({
      style: {
        left: `${left}%`,
        width: `${size}px`,
        height: `${size}px`,
        animationDelay: `${delay}s`,
        animationDuration: `${duration}s`,
        opacity: opacity,
        background: hue,
        boxShadow: `0 0 ${size * 3}px ${hue}`
      }
    })
  }
  particles.value = temp
}

// 鼠标视差效果
const handleMouseMove = (e) => {
  mouseX = (e.clientX / window.innerWidth - 0.5) * 2
  mouseY = (e.clientY / window.innerHeight - 0.5) * 2

  if (sakuraLayer.value) {
    sakuraLayer.value.style.transform = `translate(${mouseX * 15}px, ${mouseY * 15}px)`
  }

  const circles = document.querySelectorAll('.magic-circle')
  circles.forEach((circle, index) => {
    const factor = index === 0 ? 10 : 20
    circle.style.transform = `translate(${mouseX * factor}px, ${mouseY * factor}px) rotate(${index === 0 ? '0deg' : '0deg'})`
  })
}


const showSplash = () => {
  if (window.location.pathname === '/' && !visible.value) {
    visible.value = true;
    canDismiss.value = false;
    window.__splashDone = false;   // 关键：每次显示前重置
    if (dismissTimer) clearTimeout(dismissTimer);
    dismissTimer = setTimeout(() => {
      canDismiss.value = true;
    }, 3800);
  } else if (window.location.pathname !== '/') {
    visible.value = false;
  }
};

const dismiss = () => {
  if (!canDismiss.value) return;
  visible.value = false;
  setTimeout(() => {
    window.dispatchEvent(new CustomEvent('splash-finished'));
    window.__splashDone = true;    // 开屏结束后才标记完成
  }, 500);
  const ap = window.__MUSIC_PLAYER_INSTANCE__;
  if (ap && ap.audio && ap.audio.paused) {
    ap.play();
  }
};

const handleRoute = () => {
  showSplash()
}
onMounted(() => {
  user.value = getUserFromToken()
  createPetals()
  createParticles()
  showSplash()
  window.addEventListener('mousemove', handleMouseMove)
})

onBeforeUnmount(() => {
  document.removeEventListener('astro:page-load', handleRoute)
  window.removeEventListener('mousemove', handleMouseMove)
  if (dismissTimer) clearTimeout(dismissTimer)
})
</script>

<style scoped>
.splash-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10000;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  cursor: pointer;
  overflow: hidden;
  background-color: #fce4ec;
}

/* 流动渐变背景 */
.splash-bg-animated {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image:
      linear-gradient(135deg, rgba(252, 228, 236, 0.8), rgba(232, 234, 246, 0.8), rgba(237, 231, 246, 0.8)),
      url('/images/bg.jpg');
  background-size: cover, 100% 100%;
  background-position: center;
  z-index: -2;
  animation: bgGradientShift 8s ease-in-out infinite;
}

@keyframes bgGradientShift {
  0%, 100% { filter: hue-rotate(0deg) brightness(1); }
  25% { filter: hue-rotate(-10deg) brightness(1.05); }
  50% { filter: hue-rotate(10deg) brightness(0.95); }
  75% { filter: hue-rotate(-5deg) brightness(1.02); }
}

/* 旋转魔法阵 */
.magic-circle {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 500px;
  height: 500px;
  margin-left: -250px;
  margin-top: -250px;
  border: 2px solid rgba(244, 114, 182, 0.3);
  border-radius: 50%;
  z-index: 0;
  pointer-events: none;
  animation: circleSpin 20s linear infinite;
}

.magic-circle::before {
  content: '';
  position: absolute;
  top: -2px;
  left: 50%;
  width: 4px;
  height: 4px;
  background: #f472b6;
  border-radius: 50%;
  box-shadow: 0 0 10px #f472b6;
}

.magic-circle.small {
  width: 350px;
  height: 350px;
  margin-left: -175px;
  margin-top: -175px;
  border-color: rgba(192, 132, 252, 0.2);
  animation-duration: 15s;
  animation-direction: reverse;
}

@keyframes circleSpin {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

.sakura-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
  transition: transform 0.1s ease-out;
}

.sakura-petal {
  --sway: 0px;
  position: absolute;
  top: -20px;
  background: linear-gradient(135deg, #ff8ac2, #f472b6);
  border-radius: 50% 0 50% 50%;
  box-shadow: 0 0 6px rgba(244, 114, 182, 0.6);
  animation: petal-fall linear infinite;
}

@keyframes petal-fall {
  0% { transform: translate(0, -10vh) rotate(0deg); opacity: 0; }
  10% { opacity: 0.9; }
  100% { transform: translate(var(--sway), 110vh) rotate(360deg); opacity: 0.3; }
}

/* 上升粒子 */
.particle-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
}

.rising-particle {
  position: absolute;
  bottom: -10px;
  border-radius: 50%;
  animation: particleRise ease-in-out infinite;
}

@keyframes particleRise {
  0% { transform: translateY(0) scale(1); opacity: 0; }
  10% { opacity: 0.8; }
  100% { transform: translateY(-100vh) scale(0.3); opacity: 0; }
}

.splash-welcome {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 6rem;
  font-weight: 200;
  margin: 0;
  display: flex;
  gap: 1.5rem;
  letter-spacing: 0.2em;
  transform: translateY(-5vh);
  z-index: 2;
}

.char {
  display: inline-block;
  background: linear-gradient(135deg, #ff8ac2, #f472b6, #f9a8d4, #ffb3d9, #e879f9);
  background-size: 200% 200%;
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 0 15px rgba(244, 114, 182, 0.9)) drop-shadow(0 2px 8px rgba(0,0,0,0.5));
  animation: charReveal 1s ease-out both;
}

.char::after {
  content: '';
  position: absolute;
  inset: -10px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(244, 114, 182, 0.4), transparent 70%);
  opacity: 0;
  animation: charGlow 1.5s ease-out 0.5s both;
}

@keyframes charGlow {
  0% { opacity: 0; transform: scale(0.5); }
  30% { opacity: 1; }
  100% { opacity: 0; transform: scale(1.5); }
}

.splash-blog-name {
  font-family: "Georgia", "Times New Roman", serif;
  font-size: 2.8rem;
  background: linear-gradient(135deg, #ff8ac2, #f472b6, #e879f9);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  letter-spacing: 0.08em;
  margin-top: -2rem;
  z-index: 2;
  opacity: 0;
  animation: fadeUp 1s ease-out 1.8s forwards;
}

.splash-desc {
  font-size: 1rem;
  color: #ffffff;
  margin-top: 0.4rem;
  z-index: 2;
  opacity: 0;
  animation: fadeUp 1s ease-out 2.2s forwards;
  text-shadow: 0 1px 8px rgba(0,0,0,0.6);
}

.splash-hint {
  font-size: 0.85rem;
  color: #ffe4ef;
  margin-top: 1.8rem;
  z-index: 2;
  opacity: 0;
  animation: fadeUp 1s ease-out 2.8s forwards;
}

@keyframes charReveal {
  from { opacity: 0; transform: translateY(40px) scale(0.7); filter: blur(12px); }
  to { opacity: 1; transform: translateY(0) scale(1); filter: blur(0); }
}

@keyframes fadeUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

.splash-enter-active,
.splash-leave-active {
  transition: opacity 0.5s ease;
}
.splash-enter-from {
  opacity: 1;
}
.splash-leave-to {
  opacity: 0;
}
</style>