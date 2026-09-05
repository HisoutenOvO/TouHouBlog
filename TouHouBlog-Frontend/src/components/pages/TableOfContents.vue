<template>
  <div class="glass-card p-4">
    <h3 class="toc-title mb-3 flex items-center gap-1.5">
      <Icon icon="lucide:list" class="w-4 h-4" />
      目录
    </h3>

    <div v-if="!headings.length" class="text-sm text-[var(--text-muted)]">
      暂无目录
    </div>

    <nav v-else class="toc-nav">
      <a
          v-for="(heading, index) in headings"
          :key="index"
          :href="`#${heading.id}`"
          class="toc-item"
          :class="[
          `toc-level-${heading.level}`,
          { active: activeId === heading.id }
        ]"
          @click="handleItemClick(heading.id)"
      >
        <span class="toc-dot"></span>
        <span class="toc-text">{{ heading.text }}</span>
      </a>
    </nav>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  headings: {
    type: Array,
    default: () => []
  }
})

const activeId = ref('')
let observer = null
let clickLock = false

const handleItemClick = (id) => {
  activeId.value = id
  clickLock = true
  setTimeout(() => {
    clickLock = false
  }, 1000)
}

const setupScrollListener = () => {
  const contentEl = document.querySelector('.article-content-card')
  if (!contentEl) return

  const headingEls = contentEl.querySelectorAll('h1, h2, h3')
  if (!headingEls.length) return

  let ticking = false

  const updateActive = () => {
    ticking = false
    const scrollTop = window.scrollY || document.documentElement.scrollTop
    let currentId = ''
    let minDistance = Infinity

    headingEls.forEach(el => {
      const top = el.getBoundingClientRect().top
      // 只考虑位于滚动区域上方或接近顶部的标题
      if (top <= 120 && (120 - top) < minDistance) {
        minDistance = 120 - top
        currentId = el.id || ''
      }
    })

    if (currentId) {
      activeId.value = currentId
    } else {
      // 如果所有标题都在顶部以下，高亮第一个
      activeId.value = headingEls[0].id || ''
    }
  }

  const onScroll = () => {
    if (!ticking) {
      window.requestAnimationFrame(() => {
        updateActive()
        ticking = false
      })
      ticking = true
    }
  }

  window.addEventListener('scroll', onScroll, { passive: true })
  updateActive()

  // 保存清理函数
  cleanupFns.push(() => {
    window.removeEventListener('scroll', onScroll)
  })
}

const cleanupFns = []

onMounted(() => {
  setTimeout(setupScrollListener, 300)
})

onBeforeUnmount(() => {
  cleanupFns.forEach(fn => fn())
})
</script>

<style scoped>
/* 原有样式保留 */
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

.toc-item {
  display: flex;
  align-items: center;
  padding: 0.4rem 0.5rem;
  border-radius: 0.375rem;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 0.85rem;
  line-height: 1.4;
  cursor: pointer;
  /* transition: all 0.2s ease;  删除或改为 none */
}

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

/* 激活状态 */
.toc-item.active {
  background: var(--btn-primary-hover-bg) !important;
  color: #7c3aed !important;
  font-weight: 600;
  /* transition: none; */
}

.toc-item.active .toc-text {
  color: #7c3aed !important;
}

.toc-item.active .toc-dot {
  background: #7c3aed !important;
  box-shadow: 0 0 8px rgba(124, 58, 237, 0.8) !important;
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

.toc-text {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  flex: 1;
}

/* 暗色模式 */
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

[data-theme="dark"] .toc-item.active {
  background: rgba(139, 92, 246, 0.35) !important;
}

[data-theme="dark"] .toc-item.active .toc-text {
  color: #ffffff !important;
}
.toc-nav {
  position: relative;
  max-height: calc(100vh - 192px);  /* 顶部和底部都留 96px */
  overflow-y: auto;
  padding-right: 0.25rem;
  /* 可以移除之前设置的 padding-bottom，或保留一个很小值 */
}

.toc-nav::-webkit-scrollbar {
  width: 5px;
}

.toc-nav::-webkit-scrollbar-thumb {
  background: rgba(136, 136, 136, 0.5);
  border-radius: 3px;
}

.toc-nav::-webkit-scrollbar-thumb:hover {
  background: rgba(100, 100, 100, 0.7);
}
</style>