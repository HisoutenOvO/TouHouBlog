<template>
  <Transition name="splash">
    <div v-if="visible" class="splash-overlay" @click="dismiss">
      <!-- 樱花层 -->
      <div class="sakura-layer">
        <div
            v-for="(petal, index) in petals"
            :key="index"
            class="sakura-petal"
            :style="petal.style"
        ></div>
      </div>

      <h1 class="splash-welcome">
        <span
            v-for="(char, index) in welcomeChars"
            :key="index"
            class="char"
            :style="{ animationDelay: `${index * 0.3}s` }"
        >{{ char }}</span>
      </h1>
      <p class="splash-blog-name">TouHouBlog</p>
      <p class="splash-desc">只属于你我的幻想世界</p>
      <p class="splash-hint">点击任意处进入</p>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const visible = ref(false)
const canDismiss = ref(false)
const welcomeChars = ['欢', '迎', '来', '到']
const petals = ref([])
let dismissTimer = null

const createPetals = () => {
  const temp = []
  for (let i = 0; i < 25; i++) {
    const size = 8 + Math.random() * 12
    const left = Math.random() * 100
    const delay = Math.random() * 5
    const duration = 6 + Math.random() * 6
    const sway = -40 + Math.random() * 80
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

const dismiss = () => {
  if (!canDismiss.value) return;   // 动画未结束，忽略点击
  visible.value = false;

  // 如果音乐播放器实例存在且当前未播放，则开始播放
  const ap = window.__MUSIC_PLAYER_INSTANCE__;
  if (ap && ap.audio && ap.audio.paused) {
    ap.play();
  }
}

const showSplash = () => {
  if (window.location.pathname === '/') {
    visible.value = true
    // 动画总时长约 3.8 秒，结束后才允许点击
    canDismiss.value = false
    if (dismissTimer) clearTimeout(dismissTimer)
    dismissTimer = setTimeout(() => {
      canDismiss.value = true
    }, 3800)
  } else {
    visible.value = false
  }
}

const handleRoute = () => {
  showSplash()
}

onMounted(() => {
  createPetals()
  showSplash()
  document.addEventListener('astro:page-load', handleRoute)
})

onBeforeUnmount(() => {
  document.removeEventListener('astro:page-load', handleRoute)
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

.splash-overlay::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-image: linear-gradient(135deg, rgba(252, 228, 236, 0.8), rgba(232, 234, 246, 0.8), rgba(237, 231, 246, 0.8)), url('/images/bg.jpg');
  background-size: cover;
  background-position: center;
  z-index: -1;
  animation: bgFadeIn 0.8s ease-out forwards;
}

@keyframes bgFadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.sakura-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
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