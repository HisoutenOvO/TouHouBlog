<template>
  <div class="max-w-3xl mx-auto px-4 py-8">
    <div class="text-center fade-in-simple mb-12">
      <h1 class="archive-title">开发者日志</h1>
      <p class="archive-subtitle">记录博客每个重要版本的迭代与思考</p>
    </div>

    <!-- 管理员新增按钮 -->
    <div v-if="isAdmin" class="flex justify-center gap-3 mb-8">
      <button class="devlog-btn" @click="openEditor()">
        <Icon icon="lucide:plus" class="w-4 h-4" />
        新增日志
      </button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="text-center text-gray-500 py-20">加载中...</div>

    <!-- 日志时间线 -->
    <div v-else-if="groupedLogs.length" class="space-y-10">
      <div v-for="yearGroup in groupedLogs" :key="yearGroup.year" class="year-section">
        <!-- 年份标题 -->
        <div class="flex items-center gap-3 mb-5">
          <span class="year-text">{{ yearGroup.year }}</span>
          <span class="text-sm text-gray-400">{{ yearGroup.items.length }} 条日志</span>
        </div>

        <!-- 该年份下的日志时间线 -->
        <div class="devlog-timeline">
          <div
              v-for="(log, index) in yearGroup.items"
              :key="log.id"
              class="timeline-item"
              :style="{ animationDelay: `${index * 80}ms` }"
          >
            <div class="timeline-dot"></div>

            <div class="glass-card p-6 timeline-card">
              <div class="flex items-center justify-between flex-wrap gap-2 mb-2">
                <div class="flex items-center gap-3">
                  <span class="version-badge">{{ log.version }}</span>
                  <h3 class="text-lg font-bold text-gray-900">{{ log.title }}</h3>
                </div>
                <span class="text-xs text-gray-400">{{ formatDate(log.createTime) }}</span>
              </div>

              <!-- 全文显示，无折叠 -->
              <p class="text-gray-600 leading-relaxed whitespace-pre-wrap mt-2">{{ log.content }}</p>

              <!-- 管理员操作按钮 -->
              <div v-if="isAdmin" class="flex gap-2 mt-4 justify-end">
                <button class="devlog-mini-btn" @click="openEditor(log)">
                  <Icon icon="lucide:pencil" class="w-3.5 h-3.5" />
                  编辑
                </button>
                <button class="devlog-mini-btn danger" @click="deleteLog(log.id)">
                  <Icon icon="lucide:trash-2" class="w-3.5 h-3.5" />
                  删除
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else class="text-center text-gray-500 py-20">暂无日志</div>

    <!-- 编辑器模态框 -->
    <Transition name="modal">
      <div
          v-if="editorVisible"
          class="fixed inset-0 z-[10000] flex items-center justify-center bg-black/30 backdrop-blur-sm p-4"
          @click.self="closeEditor"
      >
        <div class="glass-card w-full max-w-2xl p-6" @click.stop>
          <h3 class="text-lg font-bold text-gray-900 mb-4">
            {{ editingId ? '编辑日志' : '新增日志' }}
          </h3>
          <div class="space-y-3">
            <input
                v-model="editVersion"
                type="text"
                placeholder="版本号，如 v1.2.0"
                class="w-full px-3 py-2 border border-gray-200 rounded text-sm"
            />
            <input
                v-model="editTitle"
                type="text"
                placeholder="日志标题"
                class="w-full px-3 py-2 border border-gray-200 rounded text-sm"
            />
            <textarea
                v-model="editContent"
                rows="6"
                placeholder="日志内容（支持 Markdown）"
                class="w-full px-3 py-2 border border-gray-200 rounded text-sm"
            ></textarea>
          </div>
          <div class="flex justify-end gap-2 mt-4">
            <button class="cancel-btn" @click="closeEditor">取消</button>
            <button class="publish-btn" @click="saveLog">保存</button>
          </div>
        </div>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../utils/request'
import { getUserFromToken } from '../../utils/auth'

const logs = ref([])
const loading = ref(true)
const isAdmin = ref(false)

const editorVisible = ref(false)
const editingId = ref(null)
const editVersion = ref('')
const editTitle = ref('')
const editContent = ref('')

const groupedLogs = computed(() => {
  const yearMap = new Map()
  logs.value.forEach(log => {
    const year = new Date(log.createTime).getFullYear()
    if (!yearMap.has(year)) {
      yearMap.set(year, { year, items: [] })
    }
    yearMap.get(year).items.push(log)
  })
  return Array.from(yearMap.values()).sort((a, b) => b.year - a.year)
})

const fetchLogs = async () => {
  loading.value = true
  try {
    const res = await request.get('/api/devlogs/list')
    logs.value = res.data.data || []
  } catch (e) {
    console.error('获取日志失败', e)
  } finally {
    loading.value = false
  }
}

const openEditor = (log = null) => {
  editingId.value = log ? log.id : null
  editVersion.value = log ? log.version : ''
  editTitle.value = log ? log.title : ''
  editContent.value = log ? log.content : ''
  editorVisible.value = true
}

const closeEditor = () => {
  editorVisible.value = false
  editingId.value = null
  editVersion.value = ''
  editTitle.value = ''
  editContent.value = ''
}

const saveLog = async () => {
  if (!editVersion.value.trim() || !editTitle.value.trim()) {
    await window.$alert('请填写版本号和标题')
    return
  }
  const payload = {
    version: editVersion.value.trim(),
    title: editTitle.value.trim(),
    content: editContent.value
  }
  try {
    if (editingId.value) {
      await request.put(`/api/devlogs/${editingId.value}`, payload)
    } else {
      await request.post('/api/devlogs', payload)
    }
    closeEditor()
    await fetchLogs()
  } catch (e) {}
}

const deleteLog = async (id) => {
  const confirmed = await window.$confirm('确定要删除这条日志吗？')
  if (!confirmed) return
  try {
    await request.delete(`/api/devlogs/${id}`)
    await fetchLogs()
  } catch (e) {}
}

const formatDate = (datetime) => {
  if (!datetime) return ''
  const d = new Date(datetime)
  return d.toLocaleDateString('zh-CN', { month: 'long', day: 'numeric' })
}

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)
  await fetchLogs()
})
</script>

<style scoped>
.devlog-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.25s ease;
}
.devlog-btn:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  transform: translateY(-2px);
}

/* 年份文字 */
.year-text {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 2.5rem;
  font-weight: 700;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.12));
}

/* 时间线容器 */
.devlog-timeline {
  position: relative;
  padding-left: 2.5rem;
  border-left: 2px solid rgba(255, 255, 255, 0.8);
}

.timeline-item {
  position: relative;
  margin-bottom: 1.5rem;
  animation: timelineIn 0.6s ease-out both;
}

.timeline-dot {
  position: absolute;
  left: -2.5rem;
  top: 0.75rem;
  width: 1rem;
  height: 1rem;
  border-radius: 50%;
  background: #fff;
  border: 2px solid #b388eb;
  box-shadow: 0 0 6px rgba(179, 136, 235, 0.5);
  transform: translateX(-50%);   /* 新增这一行 */
}

/* 卡片 */
.timeline-card {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.version-badge {
  display: inline-block;
  padding: 0.2rem 0.7rem;
  border-radius: 9999px;
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  font-size: 0.75rem;
  font-weight: 600;
}

.devlog-mini-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.25rem;
  padding: 0.3rem 0.6rem;
  border: none;
  border-radius: 0.4rem;
  font-size: 0.75rem;
  cursor: pointer;
  background: rgba(255, 255, 255, 0.8);
  color: #4b5563;
  transition: all 0.2s;
}
.devlog-mini-btn:hover {
  background: #e5e7eb;
}
.devlog-mini-btn.danger {
  color: #ef4444;
}
.devlog-mini-btn.danger:hover {
  background: #fee2e2;
}

.publish-btn,
.cancel-btn {
  padding: 0.5rem 1rem;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.2s ease;
}
.publish-btn {
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  color: white;
  border: none;
}
.publish-btn:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}
.cancel-btn {
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid #e5e7eb;
  color: #4b5563;
}
.cancel-btn:hover {
  background: rgba(255, 255, 255, 0.9);
}

/* 入场动画 */
@keyframes timelineIn {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>