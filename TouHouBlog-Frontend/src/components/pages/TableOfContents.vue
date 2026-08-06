<template>
  <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100">
    <h3 class="font-bold text-gray-900 mb-2">📑 目录</h3>
    <div v-if="headings.length === 0" class="text-sm text-gray-400">
      暂无目录
    </div>
    <nav v-else class="space-y-1">
      <a
          v-for="(heading, index) in headings"
          :key="index"
          :href="`#${heading.id}`"
          :class="`block text-sm hover:text-gray-900 transition-colors truncate ${
          heading.level === 1
            ? 'pl-0 font-semibold text-gray-700'
            : heading.level === 2
            ? 'pl-3 text-gray-600'
            : 'pl-6 text-gray-500'
        }`"
      >
        {{ heading.text }}
      </a>
    </nav>
  </div>
</template>

<script setup>
defineProps({
  headings: {
    type: Array,
    default: () => []
  }
})
</script>