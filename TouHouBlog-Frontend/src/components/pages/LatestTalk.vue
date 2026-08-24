<template>
  <div class="glass-card p-4">
    <div class="flex justify-between items-center mb-3">
      <h3 class="latest-talk-title">最近的杂谈～</h3>
      <a href="/talks" class="text-sm text-[var(--text-secondary)] hover:text-[var(--text-primary)] no-underline">查看全部 →</a>
    </div>

    <div v-if="loading" class="flex items-center justify-center h-32 text-[var(--text-secondary)]">
      <LoadingSkeleton />
    </div>

    <div v-else-if="talks.length" class="space-y-2">
      <div
          v-for="talk in talks"
          :key="talk.id"
          class="rounded-lg p-3 bg-[var(--input-bg)] hover:bg-[var(--btn-primary-hover-bg)] transition-colors cursor-pointer"
          @click="goTalk(talk.id)"
      >
        <p class="text-sm text-[var(--text-primary)] leading-relaxed line-clamp-2">
          {{ talk.content }}
        </p>
        <div class="text-xs text-[var(--text-muted)] mt-1">
          {{ talk.createTime }}
        </div>
      </div>
    </div>

    <div v-else class="flex items-center justify-center h-32 text-[var(--text-secondary)]">
      暂无杂谈。
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../utils/request'
import { navigate } from 'astro:transitions/client'
import LoadingSkeleton from "../common/LoadingSkeleton.vue";

const talks = ref([])
const loading = ref(true)

const goTalk = (id) => {
  navigate(`/talk/${id}`)
}

onMounted(async () => {
  try {
    const res = await request.get('/api/talks/list', { params: { page: 1, pageSize: 4 } })
    talks.value = res.data.data.records || []
  } catch (e) {
    console.error('获取杂谈失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.latest-talk-title {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 1.5rem;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.02em;
}
</style>