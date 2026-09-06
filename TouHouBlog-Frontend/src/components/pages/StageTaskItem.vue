<template>
  <div class="stage-task glass-card p-4" :class="{ 'completed': task.status === 1 }">
    <div class="flex items-center gap-3">
      <button v-if="!adding" class="progress-circle" @click="$emit('toggle-main')">
        <svg viewBox="0 0 36 36" class="w-9 h-9">
          <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="var(--text-muted)" stroke-width="3"/>
          <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" :stroke="progressColor" stroke-width="3" :stroke-dasharray="`${progress}, 100`" stroke-linecap="round" transform="rotate(-90 18 18)"/>
        </svg>
        <Icon v-if="task.status === 1" icon="lucide:check" class="absolute text-green-500 w-4 h-4" />
      </button>
      <span v-else class="progress-circle">
        <svg viewBox="0 0 36 36" class="w-9 h-9">
          <path d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831" fill="none" stroke="var(--text-muted)" stroke-width="3"/>
        </svg>
      </span>

      <Transition name="fade" mode="out-in">
        <template v-if="adding">
          <input key="add-main" ref="mainEditInput" v-model="editMainValue" class="edit-input flex-1" placeholder="输入阶段性任务名称，回车保存" @keydown.enter="handleSave" @keydown.esc="handleCancel" />
        </template>
        <template v-else-if="!editingMain">
          <span key="main-text" class="stage-title flex-1" @dblclick="startEditMain">{{ task.title }}</span>
        </template>
        <template v-else>
          <input key="edit-main" ref="mainEditInput" v-model="editMainValue" class="edit-input flex-1" @blur="finishEditMain" @keydown.enter.prevent="finishEditMain" />
        </template>
      </Transition>

      <button v-if="!adding" class="mini-btn" @click="expanded = !expanded" title="展开/折叠">
        <Icon :icon="expanded ? 'lucide:chevron-down' : 'lucide:chevron-right'" class="w-5 h-5" />
      </button>

      <div class="relative">
        <button class="priority-dot" :style="{ background: displayPriorityColor }" @click.stop="showPriorityPicker = !showPriorityPicker" title="设置重要程度"></button>
        <Transition name="fade">
          <div v-if="showPriorityPicker" ref="priorityPickerRef" class="priority-picker glass-card p-1.5">
            <button v-for="level in 5" :key="level" class="priority-option" :style="{ background: priorityColorForLevel(level) }" @click="selectPriority(level)"></button>
          </div>
        </Transition>
      </div>

      <template v-if="adding">
        <button class="save-btn" @click="handleSave" title="保存"><Icon icon="lucide:check" class="w-4 h-4" /></button>
        <button class="cancel-btn" @click="handleCancel" title="取消"><Icon icon="lucide:x" class="w-4 h-4" /></button>
      </template>
      <template v-else>
        <button class="mini-btn" @click="startEditMain" title="编辑"><Icon icon="lucide:pencil" class="w-4 h-4" /></button>
        <button class="mini-btn" @click="$emit('delete-main')" title="删除"><Icon icon="lucide:trash-2" class="w-4 h-4" /></button>
      </template>
    </div>

    <Transition name="fold">
      <div v-if="expanded && !adding" class="subtasks mt-3">
        <TransitionGroup name="subtask" tag="div">
          <div v-for="sub in task.subtasks" :key="sub.id" class="subtask-item flex items-center gap-2 py-1.5">
            <button class="check-circle" @click="$emit('toggle-subtask', sub)">
              <Transition name="check" mode="out-in">
                <Icon v-if="sub.status === 0" key="sub-circle" icon="lucide:circle" class="w-5 h-5" />
                <Icon v-else key="sub-check" icon="lucide:check-circle" class="w-5 h-5 text-green-500" />
              </Transition>
            </button>

            <Transition name="fade" mode="out-in">
              <template v-if="editingSubtaskId !== sub.id">
                <span key="sub-text" class="subtask-text flex-1" :class="{ done: sub.status === 1 }" @dblclick="startEditSubtask(sub)">{{ sub.title }}</span>
              </template>
              <template v-else>
                <input key="sub-edit" ref="subtaskEditInput" v-model="editSubtaskValue" class="edit-input flex-1" @blur="finishEditSubtask(sub)" @keydown.enter.prevent="finishEditSubtask(sub)" />
              </template>
            </Transition>

            <button class="mini-btn" @click="startEditSubtask(sub)" title="编辑"><Icon icon="lucide:pencil" class="w-4 h-4" /></button>
            <button class="mini-btn" @click="$emit('delete-subtask', sub)" title="删除"><Icon icon="lucide:trash-2" class="w-4 h-4" /></button>
          </div>

          <div v-if="showSubtaskInput" key="sub-add" class="subtask-item flex items-center gap-2 py-1.5">
            <span class="check-circle"><Icon icon="lucide:circle" class="w-4 h-4" /></span>
            <input ref="subtaskAddInput" v-model="newSubtaskTitle" class="edit-input flex-1" placeholder="输入小任务，回车保存" @keydown.enter="submitSubtask" @keydown.esc="showSubtaskInput = false" />
            <button class="save-btn" @click="submitSubtask" title="保存"><Icon icon="lucide:check" class="w-3.5 h-3.5" /></button>
            <button class="cancel-btn" @click="showSubtaskInput = false" title="取消"><Icon icon="lucide:x" class="w-3.5 h-3.5" /></button>
          </div>

          <button v-if="!showSubtaskInput" key="add-subtask-btn" class="add-subtask-btn" @click="startSubtaskInput">
            <Icon icon="lucide:plus" class="w-4 h-4" /> 添加小任务
          </button>
        </TransitionGroup>
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  task: { type: Object, default: () => ({ id: null, title: '', priority: 3, status: 0, subtasks: [] }) },
  adding: { type: Boolean, default: false }
})
const emit = defineEmits(['toggle-main', 'add-subtask', 'toggle-subtask', 'edit-main', 'delete-main', 'edit-subtask', 'delete-subtask', 'priority', 'save', 'cancel'])

const expanded = ref(false)
const editingMain = ref(false)
const editMainValue = ref('')
const mainEditInput = ref(null)
const editingSubtaskId = ref(null)
const editSubtaskValue = ref('')
const subtaskEditInput = ref(null)
const showPriorityPicker = ref(false)
const priorityPickerRef = ref(null)
const showSubtaskInput = ref(false)
const newSubtaskTitle = ref('')
const subtaskAddInput = ref(null)

const priorityColors = ['#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#dc2626']

const progress = computed(() => {
  if (!props.task.subtasks || props.task.subtasks.length === 0) return 0
  const done = props.task.subtasks.filter(s => s.status === 1).length
  return Math.round((done / props.task.subtasks.length) * 100)
})
const displayPriorityColor = computed(() => {
  if (!props.adding && props.task.status === 1) return '#ffffff'
  return priorityColors[(props.task.priority - 1) || 0]
})
const priorityColorForLevel = (level) => priorityColors[level - 1]
const progressColor = computed(() => props.task.status === 1 ? '#10b981' : '#7c3aed')

const selectPriority = (level) => {
  showPriorityPicker.value = false
  if (props.adding) {
    emit('priority', level)
  } else if (level !== props.task.priority) {
    emit('priority', level)
  }
}
const handleSave = () => {
  const title = editMainValue.value.trim()
  if (!title) return
  emit('save', { title, priority: props.task.priority })
}
const handleCancel = () => emit('cancel')
const handleOutsideClick = (event) => {
  if (showPriorityPicker.value && priorityPickerRef.value && !priorityPickerRef.value.contains(event.target)) {
    showPriorityPicker.value = false
  }
}

onMounted(() => {
  if (props.adding) nextTick(() => mainEditInput.value?.focus())
  document.addEventListener('click', handleOutsideClick)
})
onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick))

const startEditMain = async () => {
  editMainValue.value = props.task.title
  editingMain.value = true
  await nextTick()
  mainEditInput.value?.focus()
}
const finishEditMain = () => {
  editingMain.value = false
  if (editMainValue.value.trim() && editMainValue.value !== props.task.title) {
    emit('edit-main', editMainValue.value.trim())
  }
}
const startEditSubtask = async (sub) => {
  editingSubtaskId.value = sub.id
  editSubtaskValue.value = sub.title
  await nextTick()
  subtaskEditInput.value?.focus()
}
const finishEditSubtask = (sub) => {
  editingSubtaskId.value = null
  if (editSubtaskValue.value.trim() && editSubtaskValue.value !== sub.title) {
    emit('edit-subtask', { subtask: sub, title: editSubtaskValue.value.trim() })
  }
}
const startSubtaskInput = async () => {
  showSubtaskInput.value = true
  await nextTick()
  subtaskAddInput.value?.focus()
}
const submitSubtask = () => {
  const title = newSubtaskTitle.value.trim()
  if (!title) return
  showSubtaskInput.value = false
  newSubtaskTitle.value = ''
  emit('add-subtask', title)
}
</script>

<style scoped>
.stage-task {
  transition: all 0.3s ease;
}
.stage-task:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(31, 38, 135, 0.15);
}
.stage-task.completed {
  opacity: 0.65;
}
.progress-circle {
  position: relative;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}
.stage-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text-primary);
  outline: none;
  cursor: default;
}
.subtask-item {
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
}
.subtask-text {
  font-size: 0.9rem;
  color: var(--text-secondary);
  cursor: default;
}
.subtask-text.done {
  color: var(--text-muted);
  text-decoration: line-through;
}
.add-subtask-btn {
  background: none;
  border: 1px dashed rgba(255, 255, 255, 0.5);
  border-radius: 6px;
  padding: 0.3rem 0.6rem;
  font-size: 0.8rem;
  color: var(--text-muted);
  cursor: pointer;
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.3rem;
  transition: background 0.2s ease, color 0.2s ease;
}
.add-subtask-btn:hover {
  background: rgba(255,255,255,0.2);
  color: var(--text-primary);
}
.priority-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  transition: transform 0.2s ease;
}
.priority-dot:hover { transform: scale(1.3); }
.priority-picker {
  position: absolute;
  bottom: 28px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 0.4rem;
  border-radius: 999px;
  padding: 0.4rem 0.6rem;
  z-index: 30;
  background: var(--input-bg);
  backdrop-filter: blur(8px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.priority-option {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  transition: transform 0.1s;
}
.priority-option:hover { transform: scale(1.3); }
.save-btn {
  background: none;
  border: none;
  color: #10b981;
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.15s ease;
}
.save-btn:hover { background: rgba(16, 185, 129, 0.15); }
.cancel-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}
.cancel-btn:hover { color: #dc2626; background: rgba(220, 38, 38, 0.1); }
.mini-btn {
  background: none;
  border: none;
  color: var(--text-muted);
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.15s ease;
}
.mini-btn:hover { color: var(--text-primary); background: rgba(255,255,255,0.3); }
.edit-input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 0.95rem;
  padding: 0;
}
.edit-input::placeholder { color: var(--text-muted); }
/* 动画 */
.fold-enter-active, .fold-leave-active {
  transition: all 0.25s ease;
  overflow: hidden;
}
.fold-enter-from, .fold-leave-to {
  opacity: 0;
  transform: translateY(-10px);
  max-height: 0;
}
.fold-enter-to, .fold-leave-from {
  max-height: 500px;
}
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.check-enter-active, .check-leave-active { transition: all 0.2s ease; }
.check-enter-from, .check-leave-to { transform: scale(0.5); opacity: 0; }
.subtask-enter-active, .subtask-leave-active { transition: all 0.25s ease; }
.subtask-enter-from { opacity: 0; transform: translateY(-8px); }
.subtask-leave-to { opacity: 0; transform: translateY(8px); }
.subtask-move { transition: transform 0.25s ease; }
</style>