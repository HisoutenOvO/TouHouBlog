<template>
  <div class="glass-card p-4">
    <div class="flex justify-between items-center mb-3">
      <h3 class="font-bold text-gray-900 flex items-center gap-1.5">
        <Icon icon="lucide:folder" class="w-5 h-5 text-gray-600" />
        分类
      </h3>
      <button
          v-if="isAdmin"
          @click="showManager = !showManager"
          class="manager-toggle-btn"
      >
        {{ showManager ? '完成' : '管理' }}
      </button>
    </div>

    <!-- 普通状态：展示分类列表 -->
    <div v-if="loading" class="text-sm text-gray-500">
      <LoadingSkeleton />
    </div>
    <template v-else>
      <!-- 管理状态 -->
      <div v-if="showManager" class="space-y-2">
        <div v-for="cat in categories" :key="cat.id" class="flex items-center justify-between text-sm">
          <template v-if="editingCatId === cat.id">
            <input
                v-model="editCatName"
                @keyup.enter="updateCategory(cat.id)"
                @keyup.esc="cancelEdit"
                class="manager-input flex-1 min-w-0"
            />
            <div class="flex gap-1 ml-2">
              <button @click="updateCategory(cat.id)" class="manager-btn primary">保存</button>
              <button @click="cancelEdit" class="manager-btn">取消</button>
            </div>
          </template>
          <template v-else>
            <span class="text-gray-700">{{ cat.name }}</span>
            <div class="flex items-center gap-2">
              <button @click="startEdit(cat)" class="manager-btn">
                <Icon icon="lucide:pencil" class="w-3.5 h-3.5" />
                编辑
              </button>
              <button @click="deleteCategory(cat.id)" class="manager-btn danger">
                <Icon icon="lucide:trash-2" class="w-3.5 h-3.5" />
                删除
              </button>
            </div>
          </template>
        </div>

        <!-- 新增分类 -->
        <div class="flex gap-2 pt-2 border-t border-gray-100">
          <input
              v-model="newCatName"
              @keyup.enter="addCategory"
              placeholder="新增分类"
              class="manager-input flex-1 min-w-0"
          />
          <button @click="addCategory" :disabled="!newCatName.trim()" class="manager-btn primary">
            <Icon icon="lucide:plus" class="w-3.5 h-3.5" />
            新增
          </button>
        </div>
      </div>

      <!-- 普通状态：分类列表 -->
      <div v-else-if="categories.length" class="space-y-2">
        <div
            v-for="cat in categories"
            :key="cat.id"
            class="category-item flex justify-between text-sm cursor-pointer"
            @click="goToCategory(cat.id)"
        >
          <span class="text-gray-700 category-name">{{ cat.name }}</span>
          <span class="text-gray-400 category-count">{{ cat.articleCount || 0 }}</span>
        </div>
      </div>
      <div v-else class="text-sm text-gray-500">暂无分类</div>
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../utils/request'
import { getUserFromToken } from '../../utils/auth'
import { navigate } from 'astro:transitions/client'
import LoadingSkeleton from "../common/LoadingSkeleton.vue";

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
  const confirmed = await window.$confirm(`删除「${cat?.name || '该分类'}」后，其下文章将归入“其他”分类，确定删除吗？`)
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

<style scoped>
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
.manager-btn.danger {
  color: #ef4444;
}
.manager-btn.danger:hover {
  background: #fee2e2;
}

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
.category-item {
  padding: 0.35rem 0.5rem;
  border-radius: 0.5rem;
  transition: transform 0.25s ease, background 0.25s ease, box-shadow 0.25s ease;
}

.category-item:hover {
  background: rgba(255, 255, 255, 0.6);
  transform: translateX(6px);
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.12);
}

.category-item:hover .category-name {
  color: #1f2937;
}

.category-item:hover .category-count {
  color: #7c3aed;
}
</style>