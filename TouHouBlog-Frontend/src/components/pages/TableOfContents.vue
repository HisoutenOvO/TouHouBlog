<template>
  <div class="glass-card p-4">
    <!-- 标题 -->
    <h3 class="toc-title mb-3 flex items-center gap-1.5">
      <Icon icon="lucide:list" class="w-4 h-4" />
      目录
    </h3>

    <!-- 空状态 -->
    <div v-if="!headings.length" class="text-sm text-[var(--text-muted)]">
      暂无目录
    </div>

    <!-- 目录列表 -->
    <nav v-else class="toc-nav">
      <a
          v-for="(heading, index) in headings"
          :key="index"
          :href="`#${heading.id}`"
          class="toc-item"
          :class="`toc-level-${heading.level}`"
      >
        <span class="toc-dot"></span>
        <span class="toc-text">{{ heading.text }}</span>
      </a>
    </nav>
  </div>
</template>

<script setup>
import { Icon } from '@iconify/vue'

defineProps({
  headings: {
    type: Array,
    default: () => []
  }
})
</script>

<style scoped>
/* 标题 */
.toc-title {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}

.toc-nav {
  position: relative;
}

/* 目录项通用样式 */
.toc-item {
  display: flex;
  align-items: center;
  padding: 0.4rem 0.5rem;
  border-radius: 0.375rem;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.85rem;
  transition: all 0.2s ease;
  line-height: 1.4;
}

/* 层级缩进：增加左内边距，圆点跟随移动 */
.toc-level-1 {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--text-primary);
  padding-left: 0.75rem;
}

.toc-level-2 {
  padding-left: 2rem;
}

.toc-level-3 {
  padding-left: 3.25rem;
  font-size: 0.8rem;
}

/* 圆点样式：作为 flex 子元素，间距固定 */
.toc-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--input-focus-border);
  flex-shrink: 0;
  margin-right: 0.5rem;
  transition: background 0.2s ease;
}

.toc-level-1 .toc-dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  box-shadow: 0 0 6px rgba(192, 132, 252, 0.5);
}

.toc-level-3 .toc-dot {
  width: 5px;
  height: 5px;
  background: var(--text-muted);
}

/* 悬停效果 */
.toc-item:hover {
  background: var(--btn-primary-hover-bg);
  color: var(--text-primary);
  transform: translateX(3px);
}

.toc-item:hover .toc-dot {
  background: var(--btn-primary-text);
}

/* 文字截断 */
.toc-text {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  flex: 1;
}

/* 暗色模式额外增强层次 */
[data-theme="dark"] .toc-item {
  color: var(--text-secondary);
}

[data-theme="dark"] .toc-level-1 {
  color: #f3f4f6;
}

[data-theme="dark"] .toc-item:hover {
  background: rgba(139, 92, 246, 0.25);
  color: #ffffff;
}
</style>