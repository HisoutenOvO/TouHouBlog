<template>
  <div class="glass-card p-6 relative overflow-hidden">
    <!-- 装饰光晕 -->
    <div class="absolute -top-8 -left-8 w-32 h-32 bg-pink-200/30 rounded-full blur-2xl pointer-events-none"></div>
    <div class="absolute -bottom-8 -right-8 w-32 h-32 bg-violet-200/30 rounded-full blur-2xl pointer-events-none"></div>

    <div class="relative z-10 flex flex-col items-center text-center">
      <!-- 可点击头像，跳转关于页 -->
      <div
          class="avatar-ring mb-3 cursor-pointer"
          @click="goAbout"
          title="前往关于页"
      >
        <img
            src="/images/head.jpg"
            alt="Hisouten"
            class="w-full h-full object-cover rounded-full"
        />
      </div>

      <!-- 渐变名字 -->
      <h2 class="intro-name mb-1">Hisouten</h2>

      <!-- 分隔线 -->
      <div class="intro-divider my-2"></div>

      <!-- 简介文字 -->
      <p class="intro-desc text-sm text-gray-500 leading-relaxed">
        全栈开发练习生<br/>
        东方Project爱好者
      </p>

      <!-- 社交链接 -->
      <div class="flex gap-3 mt-4">
        <a href="https://github.com/HisoutenOvO" target="_blank" rel="noopener"
           class="intro-social-link" title="GitHub">
          <Icon icon="lucide:github" class="w-4 h-4" />
        </a>
        <a href="https://gitee.com/shi-changyu123" target="_blank" rel="noopener"
           class="intro-social-link" title="Gitee">
          <Icon icon="simple-icons:gitee" class="w-4 h-4" />
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'
import { navigate } from 'astro:transitions/client'

const goAbout = () => {
  navigate('/about')
}
</script>

<style scoped>
/* 头像圆环 */
.avatar-ring {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(255, 255, 255, 0.8);
  box-shadow: 0 4px 12px rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden; /* 让图片被圆形裁剪 */
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

/* 可点击头像悬停效果 */
.avatar-ring:hover {
  transform: scale(1.08);
  box-shadow: 0 6px 20px rgba(0,0,0,0.1);
}

/* 渐变名字 */
.intro-name {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 1.8rem;
  font-weight: 600;
  background: linear-gradient(135deg, #ff8ac2, #f472b6, #e879f9, #a78bfa, #60a5fa);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 0 8px rgba(244, 114, 182, 0.5));
}

/* 分隔线 */
.intro-divider {
  width: 3rem;
  height: 2px;
  background: linear-gradient(135deg, #f9a8d4, #c084fc);
  border-radius: 999px;
}

/* 简介文字 */
.intro-desc {
  letter-spacing: 0.02em;
}

/* 社交链接按钮 */
.intro-social-link {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2rem;
  height: 2rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #6b7280;
  transition: all 0.25s ease;
}
.intro-social-link:hover {
  background: rgba(255, 255, 255, 0.95);
  color: #111827;
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}
</style>