<template>
  <div class="welcome-hero glass-card h-full w-full relative overflow-hidden">
    <!-- 装饰光晕 -->
    <div class="glow-circle absolute -top-10 -left-10 w-40 h-40 bg-pink-200/30 rounded-full blur-2xl pointer-events-none"></div>
    <div class="glow-circle absolute -bottom-10 -right-10 w-40 h-40 bg-violet-200/30 rounded-full blur-2xl pointer-events-none"></div>

    <div class="relative z-10 h-full flex flex-col justify-center items-center text-center p-6">
      <!-- ========== 站长模式 ========== -->
      <template v-if="isAdmin">
        <h1 class="welcome-title">
          <span
              v-for="(char, index) in adminChars"
              :key="index"
              class="welcome-char"
              :class="{ 'animate-char': animationStarted }"
              :style="{ animationDelay: `${index * 0.15}s` }"
          >{{ char }}</span>
        </h1>
        <p class="welcome-blog-name mt-2" :class="{ 'animate-blog': animationStarted }">Hisouten站长</p>

        <div class="welcome-divider my-3" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 1.4s;"></div>

        <!-- 站长模式的小字 -->
        <div class="welcome-content flex-1 w-full flex items-center justify-center">
          <div class="overview-content">
            <p class="welcome-intro" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 1.6s;">
              👋 欢迎回到 <span class="font-semibold intro-highlight">TouHouBlog👋</span><br/>
              这里永远是独属于您的避风港～
            </p>
            <p class="welcome-intro" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 2.2s;">
              今天也请继续收集属于您与幻想乡的故事与日常生活吧～
            </p>
            <p class="welcome-intro" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 2.5s;">
              幻想乡与您同在，尽管向前迈步吧～
            </p>
            <p class="overview-hint text-sm mt-2" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 2.8s;">
              尽情书写属于您的华章吧～
            </p>
          </div>
        </div>

        <!-- 站长快捷按钮区 -->
        <div class="welcome-nav flex gap-4 mt-4">
          <button
              class="nav-icon-btn"
              :class="{ 'animate-fade-up': animationStarted }"
              style="animation-delay: 3.2s;"
              @click="go('/todo')"
              title="清理待办"
          >
            <Icon icon="lucide:list-todo" class="w-5 h-5" />
            <span class="nav-label">清理待办</span>
          </button>
          <button
              class="nav-icon-btn"
              :class="{ 'animate-fade-up': animationStarted }"
              style="animation-delay: 3.4s;"
              @click="go('/write')"
              title="写文章"
          >
            <Icon icon="lucide:pencil" class="w-5 h-5" />
            <span class="nav-label">写写文章</span>
          </button>
          <button
              class="nav-icon-btn"
              :class="{ 'animate-fade-up': animationStarted }"
              style="animation-delay: 3.6s;"
              @click="go('/talks')"
              title="发杂谈"
          >
            <Icon icon="lucide:message-circle" class="w-5 h-5" />
            <span class="nav-label">聊聊杂谈</span>
          </button>
          <button
              class="nav-icon-btn"
              :class="{ 'animate-fade-up': animationStarted }"
              style="animation-delay: 3.8s;"
              @click="go('/picture')"
              title="看图集"
          >
            <Icon icon="lucide:image" class="w-5 h-5" />
            <span class="nav-label">看看图集</span>
          </button>
        </div>
      </template>

      <!-- ========== 游客模式（保持原有内容） ========== -->
      <template v-else>
        <h1 class="welcome-title">
          <span
              v-for="(char, index) in welcomeChars"
              :key="index"
              class="welcome-char"
              :class="{ 'animate-char': animationStarted }"
              :style="{ animationDelay: `${index * 0.15}s` }"
          >{{ char }}</span>
        </h1>
        <p class="welcome-blog-name mt-2" :class="{ 'animate-blog': animationStarted }">TouHouBlog</p>

        <div class="welcome-divider my-3" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 1.4s;"></div>

        <div class="welcome-content flex-1 w-full flex items-center justify-center">
          <Transition name="fade" mode="out-in">
            <!-- 默认概览 -->
            <div v-if="activeTab === 'overview'" key="overview" class="overview-content">
              <p class="welcome-intro" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 1.6s;">
                👋 你好，我是 <span class="font-semibold intro-highlight">Hisouten👋</span><br/>
                欢迎来到我的幻想世界～
              </p>
              <p class="welcome-intro" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 1.9s;">
                这里致力于收集幻想乡的旋律与景色，并记录日常生活～
              </p>
              <p class="welcome-intro" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 2.2s;">
                本站若有不完善的地方请多多包涵～
              </p>
              <p class="overview-hint text-sm mt-2" :class="{ 'animate-fade-up': animationStarted }" style="animation-delay: 2.5s;">
                点击下方图标，探索各个模块
              </p>
            </div>

            <!-- 音乐介绍 -->
            <div v-else-if="activeTab === 'music'" key="music" class="module-content">
              <h3 class="module-title">🎵 关于音乐</h3>
              <p class="module-desc">
                温馨提示：音乐调节非常敏感，想提高音量要非常轻的滑动哦～
                左侧的黑胶唱片播放器，左上角查看歌词，右上角查看歌单～～
                下方最左侧可切换播放模式，中间可抓取最新歌单，右侧可调节音量，尽情享受来自幻想乡的旋律吧，都是值得一听的精品哦～
              </p>
            </div>

            <!-- 文章介绍 -->
            <div v-else-if="activeTab === 'articles'" key="articles" class="module-content">
              <h3 class="module-title">📝 关于文章</h3>
              <p class="module-desc">
                归档页收录了我的学习笔记、技术总结和东方杂谈。
                你可以搜索、按分类标签筛选，或切换到时间线浏览。
              </p>
            </div>

            <!-- 图集介绍 -->
            <div v-else-if="activeTab === 'gallery'" key="gallery" class="module-content">
              <h3 class="module-title">🖼️ 关于图集</h3>
              <p class="module-desc">
                图集页展示我收藏的东方Project插画与摄影。
                点击任意图片可以放大欣赏，后续还会支持多图浏览。
              </p>
            </div>
          </Transition>
        </div>

        <div class="welcome-nav flex gap-4 mt-4">
          <button
              class="nav-icon-btn"
              :class="{ 'animate-fade-up': animationStarted, active: activeTab === 'music' }"
              @click="activeTab = 'music'"
              title="关于音乐"
              style="animation-delay: 2.8s;"
          >
            <Icon icon="lucide:music" class="w-5 h-5" />
            <span class="nav-label">音乐</span>
          </button>
          <button
              class="nav-icon-btn"
              :class="{ 'animate-fade-up': animationStarted, active: activeTab === 'articles' }"
              @click="activeTab = 'articles'"
              title="关于文章"
              style="animation-delay: 3.0s;"
          >
            <Icon icon="lucide:file-text" class="w-5 h-5" />
            <span class="nav-label">文章</span>
          </button>
          <button
              class="nav-icon-btn"
              :class="{ 'animate-fade-up': animationStarted, active: activeTab === 'gallery' }"
              @click="activeTab = 'gallery'"
              title="关于图集"
              style="animation-delay: 3.2s;"
          >
            <Icon icon="lucide:image" class="w-5 h-5" />
            <span class="nav-label">图集</span>
          </button>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Icon } from '@iconify/vue'
import { getUserFromToken } from '../../../utils/auth'
import { navigate } from 'astro:transitions/client'

const welcomeChars = ['欢', '迎', '来', '到']
const adminChars = ['欢', '迎', '回', '家']
const activeTab = ref('overview')
const animationStarted = ref(false)

const user = ref(null)
const isAdmin = computed(() => user.value?.role === 1)

const startAnimation = () => {
  animationStarted.value = true
}

const go = (path) => {
  navigate(path)
}

onMounted(() => {
  user.value = getUserFromToken()
  if (window.__splashDone) {
    startAnimation()
  } else {
    window.addEventListener('splash-finished', startAnimation, { once: true })
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('splash-finished', startAnimation)
})
</script>

<style scoped>
/* 外层容器：过渡包含背景、边框、阴影、毛玻璃 */
.welcome-hero {
  transition: background 0.3s ease, border-color 0.3s ease, box-shadow 0.3s ease, backdrop-filter 0.3s ease;
}

/* 装饰光晕过渡 */
.glow-circle {
  transition: background 0.3s ease, opacity 0.3s ease;
}

.welcome-title {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 3.5rem;
  font-weight: 200;
  display: flex;
  gap: 0.6rem;
  letter-spacing: 0.15em;
  margin: 0;
}

.welcome-char {
  display: inline-block;
  background: var(--title-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 0 6px rgba(0, 0, 0, 0.25)) drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
  transition: background 0.3s ease, filter 0.3s ease;
  opacity: 0; /* 初始隐藏，防止动画前闪烁 */
}

.animate-char {
  animation: welcomeCharReveal 0.8s ease-out both;
}

.welcome-blog-name {
  font-family: "Georgia", "Times New Roman", serif;
  font-size: 1.5rem;
  background: var(--title-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  letter-spacing: 0.08em;
  transition: background 0.3s ease, filter 0.3s ease;
  opacity: 0; /* 初始隐藏 */
}

.animate-blog {
  animation: fadeUp 0.8s ease-out 1.2s both; /* 延迟1.2s，等待欢迎字符动画完成 */
}

.welcome-divider {
  width: 3rem;
  height: 2px;
  background: linear-gradient(135deg, #f9a8d4, #c084fc);
  border-radius: 999px;
  opacity: 0;
}

/* 通用淡入上浮动画类 */
.animate-fade-up {
  animation: fadeUp 0.8s ease-out both;
}

@keyframes fadeUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-content {
  min-height: 100px;
}

.overview-content,
.module-content {
  width: 100%;
}

/* 所有文字颜色使用变量，并添加过渡 */
.welcome-intro {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.7;
  letter-spacing: 0.03em;
  transition: color 0.3s ease;
  opacity: 0; /* 初始隐藏 */
}

.intro-highlight {
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.overview-hint {
  font-size: 0.8rem;
  color: var(--text-muted);
  transition: color 0.3s ease;
  opacity: 0; /* 初始隐藏 */
}

.module-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  background: var(--title-gradient);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  transition: background 0.3s ease, filter 0.3s ease;
}

.module-desc {
  font-size: 0.9rem;
  color: var(--text-secondary);
  line-height: 1.6;
  letter-spacing: 0.02em;
  transition: color 0.3s ease;
}

.welcome-nav {
  display: flex;
  justify-content: center;
  align-items: center;
}

.nav-icon-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.25rem;
  padding: 0.5rem 0.75rem;
  border-radius: 0.75rem;
  background: var(--input-bg);
  border: 1px solid var(--card-border);
  color: var(--text-secondary);
  cursor: pointer;
  transition: background 0.3s ease, border-color 0.3s ease, color 0.3s ease, transform 0.25s ease, box-shadow 0.3s ease;
  opacity: 0; /* 初始隐藏，通过动画显示 */
}

.nav-icon-btn:hover {
  background: var(--btn-primary-hover-bg);
  color: var(--btn-primary-text);
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.06);
}

.nav-icon-btn.active {
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  border-color: var(--card-border);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}

.nav-label {
  font-size: 0.7rem;
  white-space: nowrap;
}

/* 内容切换动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}
.fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}
.fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}

@keyframes welcomeCharReveal {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.8);
    filter: blur(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}
</style>