<template>
  <div class="glass-card p-4">
    <h3 class="font-bold text-gray-900 mb-3 flex items-center gap-1.5">
      <Icon icon="lucide:list" class="w-4 h-4 text-gray-600" />
      目录
    </h3>

    <!-- 空状态 -->
    <div v-if="!headings.length" class="text-sm text-gray-400">
      暂无目录
    </div>

    <!-- 目录列表 -->
    <nav v-else class="space-y-1">
      <a
          v-for="(heading, index) in headings"
          :key="index"
          :href="`#${heading.id}`"
          class="toc-item"
          :class="{
          'pl-2': heading.level === 1,
          'pl-5': heading.level === 2,
          'pl-8': heading.level === 3,
        }"
      >
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
.toc-item {
  display: block;
  padding: 0.35rem 0.5rem;
  border-radius: 0.375rem;
  color: #4b5563;
  text-decoration: none;
  font-size: 0.85rem;
  transition: all 0.2s ease;
  position: relative;
}

.toc-item:hover {
  background: rgba(255, 255, 255, 0.6);
  color: #1f2937;
  transform: translateX(2px);
}

.toc-item:hover::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0.35rem;
  bottom: 0.35rem;
  width: 3px;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  border-radius: 999px;
}

.toc-text {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}
</style>