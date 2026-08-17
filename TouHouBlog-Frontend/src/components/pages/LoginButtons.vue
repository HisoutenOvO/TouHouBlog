<template>
  <div class="login-buttons">
    <a
        href="http://localhost:8080/oauth2/authorization/github"
        class="login-btn github-btn"
    >
      <Icon icon="simple-icons:github" class="w-5 h-5" />
      <span>使用 GitHub 登录</span>
    </a>

    <a
        href="http://localhost:8080/oauth2/authorization/gitee"
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
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.github-btn {
  background: rgba(30, 30, 30, 0.75);
  color: #fff;
}
.github-btn:hover {
  background: rgba(30, 30, 30, 0.9);
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.gitee-btn {
  background: rgba(201, 62, 62, 0.75);
  color: #fff;
}
.gitee-btn:hover {
  background: rgba(201, 62, 62, 0.9);
  box-shadow: 0 4px 14px rgba(201, 62, 62, 0.25);
  transform: translateY(-2px);
}
</style>