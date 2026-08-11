<template>
  <div class="bg-white p-4 rounded-lg shadow-sm border border-gray-100 relative">
    <div class="flex justify-between items-center mb-3">
      <h3 class="font-bold text-gray-900">📂 分类</h3>
      <button v-if="isAdmin" @click="showManager = !showManager" class="text-xs text-blue-500 hover:underline">管理</button>
    </div>
    <div class="text-sm text-gray-500" v-if="loading">加载中...</div>
    <div v-else-if="categories.length" class="space-y-2">
      <div
          v-for="cat in categories"
          :key="cat.id"
          class="flex justify-between text-sm cursor-pointer hover:text-gray-900 transition-colors"
          @click="goToCategory(cat.id)"
      >
        <span class="text-gray-700">{{ cat.name }}</span>
        <span class="text-gray-400">{{ cat.articleCount || 0 }}</span>
      </div>
    </div>
    <div v-else class="text-sm text-gray-500">暂无分类</div>

    <!-- 原地管理面板 -->
    <div v-if="showManager" class="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-200 rounded-lg shadow-lg p-3 z-50">
      <div class="text-xs text-gray-500 mb-2">管理分类</div>
      <div class="space-y-2 max-h-48 overflow-y-auto">
        <div v-for="cat in categories" :key="cat.id" class="flex items-center justify-between text-sm">
          <template v-if="editingCatId === cat.id">
            <input v-model="editCatName" @keyup.enter="updateCategory(cat.id)" @blur="cancelEdit" class="flex-1 px-2 py-1 border border-gray-200 rounded text-xs" />
          </template>
          <template v-else>
            <span class="text-gray-700">{{ cat.name }}</span>
            <span class="text-xs text-gray-400 ml-2">{{ cat.articleCount || 0 }} 篇</span>
          </template>
          <div class="flex gap-1 ml-2">
            <button v-if="editingCatId !== cat.id" @click="startEdit(cat)" class="text-xs text-blue-500 hover:underline">编辑</button>
            <button @click="deleteCategory(cat.id)" class="text-xs text-red-500 hover:underline">删除</button>
          </div>
        </div>
      </div>
      <div class="flex gap-1 mt-2 flex-nowrap">
        <input v-model="newCatName" @keyup.enter="addCategory" placeholder="新分类" class="flex-1 min-w-0  px-2 py-1 text-xs border border-gray-200 rounded" />
        <button @click="addCategory" :disabled="!newCatName.trim()" class="px-2 py-1 text-xs bg-gray-800 text-white rounded">新增</button>
      </div>
    </div>
  </div>
</template>


<script setup>
import { ref, onMounted } from 'vue'
import request from '../../../utils/request'
import { getUserFromToken } from '../../../utils/auth'
import { navigate } from 'astro:transitions/client'

const categories = ref([])
const loading = ref(true)
const isAdmin = ref(false)

const showManager = ref(false)
const editingCatId = ref(null)
const editCatName = ref('')
const newCatName = ref('')

const goToCategory = (id) => {
  navigate(`/archive?categoryId=${id}`)
}

const loadCategories = async () => {
  try {
    const res = await request.get('/api/categories/list?page=1&pageSize=999')
    categories.value = res.data.data.records
  } catch (e) {} finally {
    loading.value = false
  }
}

const openManager = () => {
  showManager.value = true
}

const startEdit = (cat) => {
  editingCatId.value = cat.id
  editCatName.value = cat.name
}

const cancelEdit = () => {
  editingCatId.value = null
  editCatName.value = ''
}

const updateCategory = async (id) => {
  if (!editCatName.value.trim()) return
  try {
    await request.put(`/api/categories/${id}`, { name: editCatName.value.trim() })
    editingCatId.value = null
    await loadCategories()
  } catch (e) {}
}

const deleteCategory = async (id) => {
  const cat = categories.value.find(c => c.id === id)
  const name = cat?.name || '该分类'
  const confirmed = confirm(`删除「${name}」后，其下文章将归入“其他”分类，确定删除吗？`)
  if (!confirmed) return
  try {
    await request.delete(`/api/categories/${id}`)
    await loadCategories()
  } catch (e) {}
}

const addCategory = async () => {
  const name = newCatName.value.trim()
  if (!name) return
  try {
    await request.post('/api/categories', { name })
    newCatName.value = ''
    await loadCategories()
  } catch (e) {}
}

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)
  await loadCategories()
})
</script>