<template>
  <div class="todo-page max-w-3xl mx-auto px-4 py-8">
    <div class="text-center mb-6 fade-in-simple">
      <h1 class="archive-title">待办清单</h1>
      <p class="archive-subtitle">记录每日任务与长期目标</p>
    </div>

    <div class="flex justify-center items-center gap-4 mb-8">
      <button class="add-btn" @click="handleAdd" title="新增">
        <Icon icon="lucide:plus" class="w-5 h-5" />
      </button>

      <div class="archive-view-switch w-64">
        <div
            class="archive-view-indicator"
            :style="{ transform: currentTab === 'stage' ? 'translateX(100%)' : 'translateX(0)' }"
        ></div>
        <button class="archive-view-btn" :class="{ active: currentTab === 'daily' }" @click="currentTab = 'daily'">每日待办</button>
        <button class="archive-view-btn" :class="{ active: currentTab === 'stage' }" @click="currentTab = 'stage'">阶段性任务</button>
      </div>

      <button class="toggle-completed-btn" @click="showCompleted = !showCompleted">
        <Icon icon="lucide:list-check" class="w-4 h-4" />
        {{ showCompleted ? '返回' : '查看已完成' }}
      </button>
    </div>

    <div class="glass-card p-5">
      <h2 class="section-title">{{ showCompleted ? '已完成' : '待完成' }}</h2>

      <!-- 整个列表区域：根据 currentTab 和 showCompleted 使用 key 触发整体淡入淡出 -->
      <Transition name="fade" mode="out-in">
        <div :key="currentTab + '-' + showCompleted" class="mt-4">
          <!-- 每日待办，未完成 -->
          <TransitionGroup
              v-if="currentTab === 'daily' && !showCompleted"
              key="daily-incomplete"
              name="list"
              tag="div"
              class="flex flex-col gap-3"
          >
            <TodoItem
                v-if="showDailyAdd"
                key="daily-add"
                :todo="emptyTodo"
                :adding="true"
                @save="saveDailyAdd"
                @cancel="showDailyAdd = false"
                @priority="updateDailyDraftPriority"
            />
            <TodoItem
                v-for="item in filteredDaily"
                :key="item.id"
                :todo="item"
                @toggle="toggleDaily(item)"
                @edit="editDaily(item, $event)"
                @delete="deleteDaily(item.id)"
                @priority="changePriority(item, $event)"
            />
            <div v-if="!filteredDaily.length && !showDailyAdd" key="empty" class="text-center text-sm text-[var(--text-muted)] py-8">
              暂无待办任务
            </div>
          </TransitionGroup>

          <!-- 每日待办，已完成 -->
          <TransitionGroup
              v-else-if="currentTab === 'daily' && showCompleted"
              key="daily-completed"
              name="list"
              tag="div"
              class="flex flex-col gap-3"
          >
            <TodoItem
                v-for="item in filteredDaily"
                :key="item.id"
                :todo="item"
                @toggle="toggleDaily(item)"
                @edit="editDaily(item, $event)"
                @delete="deleteDaily(item.id)"
                @priority="changePriority(item, $event)"
            />
            <div v-if="!filteredDaily.length" key="empty" class="text-center text-sm text-[var(--text-muted)] py-8">
              暂无已完成任务
            </div>
          </TransitionGroup>

          <!-- 阶段性任务，未完成 -->
          <TransitionGroup
              v-else-if="currentTab === 'stage' && !showCompleted"
              key="stage-incomplete"
              name="list"
              tag="div"
              class="flex flex-col gap-4"
          >
            <StageTaskItem
                v-if="showStageAdd"
                key="stage-add"
                :task="emptyStageTask"
                :adding="true"
                @save="saveStageAdd"
                @cancel="showStageAdd = false"
                @priority="updateStageDraftPriority"
            />
            <StageTaskItem
                v-for="item in filteredStage"
                :key="item.id"
                :task="item"
                @toggle-main="toggleMain(item)"
                @add-subtask="addSubtask(item, $event)"
                @toggle-subtask="toggleSubtask(item, $event)"
                @edit-main="editStage(item, $event)"
                @delete-main="deleteStage(item.id)"
                @edit-subtask="editSubtask(item, $event.subtask, $event.title)"
                @delete-subtask="deleteSubtask(item, $event)"
                @priority="changePriority(item, $event)"
            />
            <div v-if="!filteredStage.length && !showStageAdd" key="empty" class="text-center text-sm text-[var(--text-muted)] py-8">
              暂无阶段性任务
            </div>
          </TransitionGroup>

          <!-- 阶段性任务，已完成 -->
          <TransitionGroup
              v-else-if="currentTab === 'stage' && showCompleted"
              key="stage-completed"
              name="list"
              tag="div"
              class="flex flex-col gap-4"
          >
            <StageTaskItem
                v-for="item in filteredStage"
                :key="item.id"
                :task="item"
                @toggle-main="toggleMain(item)"
                @add-subtask="addSubtask(item, $event)"
                @toggle-subtask="toggleSubtask(item, $event)"
                @edit-main="editStage(item, $event)"
                @delete-main="deleteStage(item.id)"
                @edit-subtask="editSubtask(item, $event.subtask, $event.title)"
                @delete-subtask="deleteSubtask(item, $event)"
                @priority="changePriority(item, $event)"
            />
            <div v-if="!filteredStage.length" key="empty" class="text-center text-sm text-[var(--text-muted)] py-8">
              暂无已完成任务
            </div>
          </TransitionGroup>
        </div>
      </Transition>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../utils/request'
import TodoItem from './TodoItem.vue'
import StageTaskItem from './StageTaskItem.vue'

const currentTab = ref('daily')
const showCompleted = ref(false)
const dailyTodos = ref([])
const stageTasks = ref([])
const showDailyAdd = ref(false)
const showStageAdd = ref(false)

const emptyTodo = reactive({ title: '', priority: 3, status: 0 })
const emptyStageTask = reactive({ title: '', priority: 3, status: 0, subtasks: [] })

const filteredDaily = computed(() => dailyTodos.value.filter(t => t.status === (showCompleted.value ? 1 : 0)))
const filteredStage = computed(() => stageTasks.value.filter(t => t.status === (showCompleted.value ? 1 : 0)))

const loadDaily = async () => {
  try {
    const res = await request.get('/api/todo/daily')
    dailyTodos.value = res.data.data || []
  } catch (e) { console.error('加载每日待办失败', e) }
}
const loadStage = async () => {
  try {
    const res = await request.get('/api/todo/stage')
    stageTasks.value = res.data.data || []
  } catch (e) { console.error('加载阶段性任务失败', e) }
}

const handleAdd = () => {
  showCompleted.value = false
  if (currentTab.value === 'daily') {
    emptyTodo.priority = 3
    showDailyAdd.value = true
  } else {
    emptyStageTask.priority = 3
    showStageAdd.value = true
  }
}

const updateDailyDraftPriority = (level) => { emptyTodo.priority = level }
const updateStageDraftPriority = (level) => { emptyStageTask.priority = level }

const saveDailyAdd = async ({ title, priority }) => {
  try {
    await request.post('/api/todo', { type: 1, title, priority })
    showDailyAdd.value = false
    await loadDaily()
  } catch (e) { console.error('新增待办失败', e) }
}
const saveStageAdd = async ({ title, priority }) => {
  try {
    await request.post('/api/todo', { type: 2, title, priority })
    showStageAdd.value = false
    await loadStage()
  } catch (e) { console.error('新增阶段性任务失败', e) }
}

const toggleDaily = async (item) => {
  const newStatus = item.status === 0 ? 1 : 0
  try {
    await request.put(`/api/todo/${item.id}`, { status: newStatus })
    item.status = newStatus
  } catch (e) { console.error('更新待办状态失败', e) }
}
const editDaily = async (item, newTitle) => {
  if (!newTitle || newTitle === item.title) return
  try {
    await request.put(`/api/todo/${item.id}`, { title: newTitle })
    item.title = newTitle
  } catch (e) { console.error('编辑待办失败', e) }
}
const deleteDaily = async (id) => {
  const confirmed = await window.$confirm('确定删除这条待办吗？')
  if (!confirmed) return
  try {
    await request.delete(`/api/todo/${id}`)
    dailyTodos.value = dailyTodos.value.filter(t => t.id !== id)
  } catch (e) { console.error('删除待办失败', e) }
}
const changePriority = async (item, level) => {
  try {
    await request.put(`/api/todo/${item.id}`, { priority: level })
    item.priority = level
    if (currentTab.value === 'daily') await loadDaily()
    else await loadStage()
  } catch (e) { console.error('修改重要程度失败', e) }
}
const toggleMain = async (item) => {
  const newStatus = item.status === 0 ? 1 : 0
  try {
    await request.put(`/api/todo/${item.id}`, { status: newStatus })
    item.status = newStatus
    if (newStatus === 1) item.subtasks.forEach(sub => sub.status = 1)
  } catch (e) { console.error('更新阶段性任务状态失败', e) }
}
const addSubtask = async (item, title) => {
  if (!title || !title.trim()) return
  try {
    await request.post('/api/todo/subtask', { mainId: item.id, title: title.trim() })
    await loadStage()
  } catch (e) { console.error('新增子任务失败', e) }
}
const toggleSubtask = async (item, subtask) => {
  const newStatus = subtask.status === 0 ? 1 : 0
  try {
    await request.put(`/api/todo/subtask/${subtask.id}`, { status: newStatus })
    await loadStage()
  } catch (e) { console.error('更新子任务状态失败', e) }
}
const editStage = async (item, newTitle) => {
  if (!newTitle || newTitle === item.title) return
  try {
    await request.put(`/api/todo/${item.id}`, { title: newTitle })
    item.title = newTitle
  } catch (e) { console.error('编辑阶段性任务失败', e) }
}
const deleteStage = async (id) => {
  const confirmed = await window.$confirm('确定删除这个阶段性任务吗？')
  if (!confirmed) return
  try {
    await request.delete(`/api/todo/${id}`)
    stageTasks.value = stageTasks.value.filter(t => t.id !== id)
  } catch (e) { console.error('删除阶段性任务失败', e) }
}
const editSubtask = async (item, subtask, newTitle) => {
  if (!newTitle || newTitle === subtask.title) return
  try {
    await request.put(`/api/todo/subtask/${subtask.id}`, { title: newTitle })
    subtask.title = newTitle
  } catch (e) { console.error('编辑子任务失败', e) }
}
const deleteSubtask = async (item, subtask) => {
  const confirmed = await window.$confirm('确定删除这个小任务吗？')
  if (!confirmed) return
  try {
    await request.delete(`/api/todo/subtask/${subtask.id}`)
    await loadStage()
  } catch (e) { console.error('删除子任务失败', e) }
}

onMounted(() => {
  loadDaily()
  loadStage()
})
</script>

<style scoped>
.todo-page { min-height: 100vh; }
.section-title {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 1.4rem;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: 0.02em;
  border-bottom: 2px solid var(--input-focus-border);
  padding-bottom: 0.5rem;
  margin-bottom: 1rem;
}
.add-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(124, 58, 237, 0.85), rgba(236, 72, 153, 0.85));
  color: white;
  border: 1px solid rgba(255,255,255,0.6);
  box-shadow: 0 4px 12px rgba(124, 58, 237, 0.3);
  backdrop-filter: blur(8px);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.25s ease;
  flex-shrink: 0;
}
.add-btn:hover { transform: scale(1.1); box-shadow: 0 6px 18px rgba(124, 58, 237, 0.4); }
.add-btn:active { transform: scale(0.95); }
.toggle-completed-btn {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  border: 1px solid var(--card-border);
  background: var(--btn-primary-bg);
  color: var(--btn-primary-text);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.25s ease;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  flex-shrink: 0;
  white-space: nowrap;
}
.toggle-completed-btn:hover { background: var(--btn-primary-hover-bg); transform: translateY(-1px); }

/* 页面级淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 列表项移动动画 */
.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
.list-leave-to {
  opacity: 0;
  transform: translateY(-10px) scale(0.95);
}
.list-move {
  transition: transform 0.3s ease;
}
</style>