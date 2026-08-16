<template>
  <div class="glass-card p-4">
    <div class="flex justify-between items-center mb-3">
      <h3 class="font-bold text-gray-900 flex items-center gap-1.5">
        <Icon icon="lucide:tag" class="w-5 h-5 text-gray-600" />
        标签
      </h3>
      <button
          v-if="isAdmin"
          @click="showManager = !showManager"
          class="manager-toggle-btn"
      >
        {{ showManager ? '完成' : '管理' }}
      </button>
    </div>

    <div v-if="loading" class="text-sm text-gray-500">加载中...</div>
    <template v-else>
      <!-- 管理状态 -->
      <div v-if="showManager" class="space-y-2">
        <div class="flex flex-wrap gap-2">
          <span
              v-for="tag in tags"
              :key="tag.id"
              class="relative inline-flex items-center px-2.5 py-1 text-xs rounded-full bg-gray-100 text-gray-600"
          >
            {{ tag.name }}
            <button
                @click.stop="deleteTag(tag.id)"
                class="ml-1 w-4 h-4 flex items-center justify-center rounded-full bg-red-100 text-red-500 hover:bg-red-200 transition-colors"
                title="删除标签"
            >
              <Icon icon="lucide:x" class="w-3 h-3" />
            </button>
          </span>
        </div>

        <!-- 新增标签 -->
        <div class="flex gap-2 pt-2 border-t border-gray-100">
          <input
              v-model="newTagName"
              @keyup.enter="addTag"
              placeholder="新增标签"
              class="manager-input flex-1 min-w-0"
          />
          <button @click="addTag" :disabled="!newTagName.trim()" class="manager-btn primary">
            <Icon icon="lucide:plus" class="w-3.5 h-3.5" />
            新增
          </button>
        </div>
      </div>

      <!-- 普通状态 -->
      <div v-else-if="tags.length" class="flex flex-wrap gap-2">
        <span
            v-for="tag in tags"
            :key="tag.id"
            class="tag-chip-list cursor-pointer"
            @click="goToTag(tag.id)"
        >
          {{ tag.name }}
        </span>
      </div>
      <div v-else class="text-sm text-gray-500">暂无标签</div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../utils/request'
import { getUserFromToken } from '../../utils/auth'
import { navigate } from 'astro:transitions/client'

const tags = ref([])
const loading = ref(true)
const isAdmin = ref(false)

const showManager = ref(false)
const newTagName = ref('')

const goToTag = (id) => {
  navigate(`/archive?tagId=${id}`)
}

const loadTags = async () => {
  try {
    const res = await request.get('/api/tags/list?page=1&pageSize=999')
    tags.value = res.data.data.records
  } catch (e) {} finally {
    loading.value = false
  }
}

const deleteTag = async (id) => {
  const tag = tags.value.find(t => t.id === id)
  const confirmed = await window.$confirm(`确定要删除标签「${tag?.name || '该标签'}」吗？`)
  if (!confirmed) return
  try {
    await request.delete(`/api/tags/${id}`)
    await loadTags()
  } catch (e) {}
}

const addTag = async () => {
  const name = newTagName.value.trim()
  if (!name) return
  try {
    await request.post('/api/tags', { name })
    newTagName.value = ''
    await loadTags()
  } catch (e) {}
}

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)
  await loadTags()
})
</script>

<style scoped>
/* 管理按钮 */
.manager-toggle-btn {
  padding: 0.25rem 0.8rem;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
}
.manager-toggle-btn:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  color: #523b52;
  box-shadow: 0 3px 8px rgba(0,0,0,0.06);
}

/* 管理状态下的小按钮 */
.manager-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.3rem 0.6rem;
  border: none;
  border-radius: 0.4rem;
  font-size: 0.75rem;
  cursor: pointer;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.8);
  color: #4b5563;
}
.manager-btn:hover {
  background: #e5e7eb;
}
.manager-btn.primary {
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  border: 1px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
}
.manager-btn.primary:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  color: #523b52;
  box-shadow: 0 3px 8px rgba(0,0,0,0.06);
}

/* 输入框 */
.manager-input {
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid #e8d5f5;
  border-radius: 0.4rem;
  color: #4b5563;
  padding: 0.35rem 0.6rem;
  font-size: 0.8rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.manager-input:focus {
  border-color: #d8b4e8;
  box-shadow: 0 0 0 2px rgba(216, 180, 232, 0.2);
  outline: none;
}

/* 普通标签胶囊 */
.tag-chip-list {
  display: inline-flex;
  align-items: center;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 2px 6px rgba(0,0,0,0.04);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  transition: background 0.2s, transform 0.2s;
}
.tag-chip-list:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  color: #523b52;
  transform: translateY(-1px);
}
</style>