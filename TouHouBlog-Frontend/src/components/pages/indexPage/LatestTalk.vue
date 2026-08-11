<template>
  <div class="h-full flex flex-col">
    <div class="flex justify-between items-center mb-3">
      <h3 class="font-bold text-gray-900 text-lg">💬 最新杂谈</h3>
      <a href="/talks" class="text-sm text-gray-400 hover:text-gray-600 no-underline">查看全部 →</a>
    </div>

    <div v-if="loading" class="flex-1 flex items-center justify-center text-gray-500">
      加载中...
    </div>

    <div v-else-if="talks.length" class="flex-1 flex flex-col gap-3">
      <div
          v-for="talk in talks"
          :key="talk.id"
          class="bg-white p-4 rounded-lg shadow-sm border border-gray-100"
      >
        <p class="text-sm text-gray-700 leading-relaxed line-clamp-2">
          {{ talk.content }}
        </p>
        <div class="text-xs text-gray-400 mt-2">
          {{ talk.createTime }}
        </div>
      </div>
    </div>

    <div v-else class="flex-1 flex items-center justify-center text-gray-500">
      暂无杂谈。
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import request from '../../../utils/request'

const talks = ref([]);
const loading = ref(true);

onMounted(async () => {
  try {
    const res = await request.get('/api/talks/list', { params: { page: 1, pageSize: 3 } });
    talks.value = res.data.data.records || [];
  } catch (e) {
  } finally {
    loading.value = false;
  }
});
</script>