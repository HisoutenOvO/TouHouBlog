<template>
  <div class="login-buttons">
    <a
        href="/oauth2/authorization/github"
        class="login-btn github-btn"
    >
      <Icon icon="simple-icons:github" class="w-5 h-5" />
      <span>使用 GitHub 登录</span>
    </a>

    <a
        href="/oauth2/authorization/gitee"
        class="login-btn gitee-btn"
    >
      <Icon icon="simple-icons:gitee" class="w-5 h-5" />
      <span>使用 Gitee 登录</span>
    </a>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'
</script>

<style scoped>
.login-buttons {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
}

/* 通用按钮样式：毛玻璃 + 渐变边框 */
.login-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.6rem;
  padding: 0.75rem 1.5rem;
  border-radius: 9999px;
  font-size: 0.95rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.25s ease;
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  position: relative;
  overflow: hidden;
}

/* GitHub 按钮：深色毛玻璃 */
.github-btn {
  background: rgba(30, 30, 30, 0.75);
  color: #fff;
}
.github-btn:hover {
  background: rgba(30, 30, 30, 0.9);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
  transform: translateY(-2px);
}

/* Gitee 按钮：红色毛玻璃 */
.gitee-btn {
  background: rgba(201, 62, 62, 0.75);
  color: #fff;
}
.gitee-btn:hover {
  background: rgba(201, 62, 62, 0.9);
  box-shadow: 0 6px 20px rgba(201, 62, 62, 0.3);
  transform: translateY(-2px);
}

/* 暗色模式按钮适配 */
[data-theme="dark"] .github-btn {
  background: rgba(30, 30, 30, 0.6);
  border-color: rgba(255, 255, 255, 0.2);
}
[data-theme="dark"] .github-btn:hover {
  background: rgba(30, 30, 30, 0.8);
}

[data-theme="dark"] .gitee-btn {
  background: rgba(201, 62, 62, 0.6);
  border-color: rgba(255, 255, 255, 0.2);
}
[data-theme="dark"] .gitee-btn:hover {
  background: rgba(201, 62, 62, 0.8);
}
</style>