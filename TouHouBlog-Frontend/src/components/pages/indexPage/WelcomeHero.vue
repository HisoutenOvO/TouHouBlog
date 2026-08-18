<template>
  <div class="welcome-hero glass-card h-full w-full relative overflow-hidden">
    <!-- 装饰光晕 -->
    <div class="absolute -top-10 -left-10 w-40 h-40 bg-pink-200/30 rounded-full blur-2xl pointer-events-none"></div>
    <div class="absolute -bottom-10 -right-10 w-40 h-40 bg-violet-200/30 rounded-full blur-2xl pointer-events-none"></div>

    <div class="relative z-10 h-full flex flex-col justify-center items-center text-center p-6">
      <!-- 标题区（始终显示） -->
      <h1 class="welcome-title">
        <span
            v-for="(char, index) in welcomeChars"
            :key="index"
            class="welcome-char"
            :style="{ animationDelay: `${index * 0.15}s` }"
        >{{ char }}</span>
      </h1>
      <p class="welcome-blog-name mt-2">TouHouBlog</p>

      <div class="welcome-divider my-3"></div>

      <!-- 内容区：根据当前 tab 切换 -->
      <div class="welcome-content flex-1 w-full flex items-center justify-center">
        <Transition name="fade" mode="out-in">
          <!-- 默认概览 -->
          <div v-if="activeTab === 'overview'" key="overview" class="overview-content">
            <p class="welcome-intro">
              👋 你好，我是 <span class="font-semibold text-gray-800">Hisouten👋</span><br/>
              欢迎来到我的幻想世界～
            </p>
            <p class="welcome-intro">这里致力于收集幻想乡的旋律与景色，并记录日常生活～</p>
            <p class="welcome-intro">本站若有不完善的地方请多多包涵～</p>
            <p class="overview-hint text-sm text-gray-500 mt-2">
              点击下方图标，探索各个模块
            </p>
          </div>

          <!-- 音乐介绍 -->
          <div v-else-if="activeTab === 'music'" key="music" class="module-content">
            <h3 class="module-title">🎵 关于音乐</h3>
            <p class="module-desc">
              这里是我的音乐小站，收藏了东方原曲与同人曲的各种精品～～
              左侧的黑胶唱片播放器，左上角查看歌词，右上角查看歌单～～
              下方最左侧可切换播放模式，中间可抓取最新歌单，右侧可调节音量，尽情享受来自幻想乡的旋律吧，全都值得一听哦～
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

      <!-- 底部三个图标按钮 -->
      <div class="welcome-nav flex gap-4 mt-4">
        <button
            class="nav-icon-btn"
            :class="{ active: activeTab === 'music' }"
            @click="activeTab = 'music'"
            title="关于音乐"
        >
          <Icon icon="lucide:music" class="w-5 h-5" />
          <span class="nav-label">音乐</span>
        </button>
        <button
            class="nav-icon-btn"
            :class="{ active: activeTab === 'articles' }"
            @click="activeTab = 'articles'"
            title="关于文章"
        >
          <Icon icon="lucide:file-text" class="w-5 h-5" />
          <span class="nav-label">文章</span>
        </button>
        <button
            class="nav-icon-btn"
            :class="{ active: activeTab === 'gallery' }"
            @click="activeTab = 'gallery'"
            title="关于图集"
        >
          <Icon icon="lucide:image" class="w-5 h-5" />
          <span class="nav-label">图集</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'

const welcomeChars = ['欢', '迎', '来', '到']
const activeTab = ref('overview')
</script>

<style scoped>
.welcome-hero {
  transition: all 0.3s ease;
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
  background: linear-gradient(135deg, #c026d3, #db2777, #7c3aed);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 0 6px rgba(0, 0, 0, 0.25)) drop-shadow(0 2px 4px rgba(0, 0, 0, 0.15));
  animation: welcomeCharReveal 0.8s ease-out both;
}
.welcome-blog-name {
  font-family: "Georgia", "Times New Roman", serif;
  font-size: 1.5rem;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  letter-spacing: 0.08em;
}

.welcome-divider {
  width: 3rem;
  height: 2px;
  background: linear-gradient(135deg, #f9a8d4, #c084fc);
  border-radius: 999px;
}

.welcome-content {
  min-height: 100px;
}

.overview-content,
.module-content {
  width: 100%;
}

.welcome-intro {
  font-size: 1rem;
  color: #4b5563;
  line-height: 1.7;
  letter-spacing: 0.03em;
}

.overview-hint {
  font-size: 0.8rem;
  color: #9ca3af;
}

.module-title {
  font-size: 1.25rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
}

.module-desc {
  font-size: 0.9rem;
  color: #4b5563;
  line-height: 1.6;
  letter-spacing: 0.02em;
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
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #6b7280;
  cursor: pointer;
  transition: all 0.25s ease;
}

.nav-icon-btn:hover {
  background: rgba(255, 255, 255, 0.85);
  color: #111827;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.06);
}

.nav-icon-btn.active {
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  border-color: rgba(255, 255, 255, 0.7);
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