<template>
  <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
    <div v-for="i in 9" :key="i" class="bg-white rounded-lg border border-gray-200 overflow-hidden shadow-sm">
      <div class="h-48 bg-gray-100 flex items-center justify-center">
        <span class="text-gray-400 text-lg">图片 {{ i }}</span>
      </div>
      <div class="p-3 text-sm text-gray-500 text-center">
        图片标题占位
      </div>
    </div>
  </div>
</template>