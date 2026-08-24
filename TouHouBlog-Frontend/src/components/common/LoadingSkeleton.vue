<template>
  <div class="skeleton-card">
    <div class="skeleton-header">
      <span class="skeleton-title">{{ text }}</span>
    </div>
    <div class="skeleton-lines">
      <div class="skeleton-line" style="width: 85%"></div>
      <div class="skeleton-line" style="width: 70%"></div>
      <div class="skeleton-line" style="width: 75%"></div>
    </div>
  </div>
</template>

<script setup>
defineProps({
  text: {
    type: String,
    default: '少女祈祷中'
  }
})
</script>

<style scoped>
.skeleton-card {
  background: rgba(255, 255, 255, 0.35);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-radius: 1rem;
  padding: 1rem 1.25rem;
  width: 100%;
  box-sizing: border-box;
}

.skeleton-header {
  margin-bottom: 0.8rem;
}

.skeleton-title {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 0.9rem;
  letter-spacing: 0.08em;
  color: #7c3aed;
  text-shadow: 0 1px 4px rgba(124, 58, 237, 0.25);
}

.skeleton-lines {
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}

.skeleton-line {
  height: 0.85rem;
  border-radius: 999px;
  background: linear-gradient(
      90deg,
      rgba(255, 255, 255, 0.2) 25%,
      rgba(255, 255, 255, 0.6) 50%,
      rgba(255, 255, 255, 0.2) 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.2s ease-in-out infinite;
}

@keyframes shimmer {
  0% { background-position: 200% 0; }
  100% { background-position: -200% 0; }
}
</style>