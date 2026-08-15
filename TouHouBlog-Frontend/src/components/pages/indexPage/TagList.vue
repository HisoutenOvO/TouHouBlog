<template>
  <div class="glass-card p-4 relative">
    <div class="flex justify-between items-center mb-3">
      <h3 class="font-bold text-gray-900">🏷️ 标签</h3>
      <button v-if="isAdmin" @click="showManager = !showManager" class="text-xs text-blue-500 hover:underline">管理</button>
    </div>
    <div v-if="loading" class="text-sm text-gray-500">加载中...</div>
    <div v-else-if="tags.length" class="flex flex-wrap gap-2">
      <span
          v-for="tag in tags"
          :key="tag.id"
          class="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-600 cursor-pointer hover:bg-gray-200 transition-colors"
          @click="goToTag(tag.id)"
      >
        {{ tag.name }}
      </span>
    </div>
    <div v-else class="text-sm text-gray-500">暂无标签</div>

    <!-- 原地管理面板 -->
    <div v-if="showManager" class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg p-3 z-50">
      <div class="text-xs text-gray-500 mb-2">管理标签</div>
      <div class="space-y-2 max-h-48 overflow-y-auto">
        <div v-for="tag in tags" :key="tag.id" class="flex items-center justify-between text-sm">
          <template v-if="editingTagId === tag.id">
            <input v-model="editTagName" @keyup.enter="updateTag(tag.id)" @blur="cancelEdit" class="flex-1 px-2 py-1 border border-gray-200 rounded text-xs" />
          </template>
          <template v-else>
            <span class="text-gray-700">{{ tag.name }}</span>
            <span class="text-xs text-gray-400 ml-2">{{ tag.articleCount || 0 }} 篇</span>
          </template>
          <div class="flex gap-1 ml-2">
            <button v-if="editingTagId !== tag.id" @click="startEdit(tag)" class="text-xs text-blue-500 hover:underline">编辑</button>
            <button @click="deleteTag(tag.id)" class="text-xs text-red-500 hover:underline">删除</button>
          </div>
        </div>
      </div>
      <div class="flex gap-1 mt-2 flex-nowrap">
        <input v-model="newTagName" @keyup.enter="addTag" placeholder="新标签" class="flex-1 min-w-0 px-2 py-1 text-xs border border-gray-200 rounded" />
        <button @click="addTag" :disabled="!newTagName.trim()" class="px-2 py-1 text-xs bg-gray-800 text-white rounded">新增</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../../../utils/request.js'
import { getUserFromToken } from '../../../utils/auth.js'
import { navigate } from 'astro:transitions/client'

const tags = ref([])
const loading = ref(true)
const isAdmin = ref(false)

const showManager = ref(false)
const editingTagId = ref(null)
const editTagName = ref('')
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

const openManager = () => {
  showManager.value = true
}

const startEdit = (tag) => {
  editingTagId.value = tag.id
  editTagName.value = tag.name
}

const cancelEdit = () => {
  editingTagId.value = null
  editTagName.value = ''
}

const updateTag = async (id) => {
  if (!editTagName.value.trim()) return
  try {
    await request.put(`/api/tags/${id}`, { name: editTagName.value.trim() })
    editingTagId.value = null
    await loadTags()
  } catch (e) {}
}

const deleteTag = async (id) => {
  const confirmed = confirm('确定删除该标签吗？')
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