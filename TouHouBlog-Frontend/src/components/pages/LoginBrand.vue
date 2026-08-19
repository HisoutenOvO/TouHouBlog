<template>
  <div class="login-left">
    <div class="mb-4">
      <Icon icon="lucide:music" class="w-20 h-20 login-music-icon" />
    </div>
    <h1 class="login-title mb-3">登录 TouHouBlog</h1>
    <p class="login-subtitle mb-4">选择一种方式，进入幻想世界</p>
    <div class="login-divider mb-6"></div>
    <p class="login-desc">
      欢迎来到这个小小的幻想乡。<br/>
      这里收集了东方Project的旋律与景色，<br/>
      也记录着平凡日常中的点滴灵感。
    </p>
    <div class="login-badges">
      <span class="badge-item">
        <Icon icon="lucide:music" class="w-4 h-4" />
        音乐
      </span>
      <span class="badge-item">
        <Icon icon="lucide:file-text" class="w-4 h-4" />
        文章
      </span>
      <span class="badge-item">
        <Icon icon="lucide:image" class="w-4 h-4" />
        图集
      </span>
    </div>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'
</script>

<style scoped>
.login-left {
  text-align: left;
}

.login-music-icon {
  color: #7c3aed;
  filter: drop-shadow(0 0 12px rgba(124, 58, 237, 0.5));
}

.login-title {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 3px 6px rgba(0, 0, 0, 0.12));
  letter-spacing: 0.05em;
  margin: 0;
}

.login-subtitle {
  font-style: italic;
  color: #6b7280;
  font-size: 1.1rem;
  letter-spacing: 0.05em;
  margin: 0;
}

.login-divider {
  width: 60px;
  height: 3px;
  border-radius: 999px;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
}

.login-desc {
  font-size: 0.95rem;
  color: #6b7280;
  letter-spacing: 0.03em;
  line-height: 1.6;
  margin: 0;
}

.login-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.badge-item {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.35rem 1rem;
  border-radius: 9999px;
  font-size: 0.8rem;
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  transition: all 0.2s ease;
}

.badge-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 10px rgba(0,0,0,0.08);
}
</style>