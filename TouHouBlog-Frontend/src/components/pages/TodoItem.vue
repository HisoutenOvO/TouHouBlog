<template>
  <div class="todo-item glass-card p-3 flex items-center gap-3" :class="{ 'completed': todo.status === 1 }">
    <button v-if="!adding" class="check-circle" @click="$emit('toggle')">
      <Transition name="check" mode="out-in">
        <Icon v-if="todo.status === 0" key="circle" icon="lucide:circle" class="w-5 h-5" />
        <Icon v-else key="check" icon="lucide:check-circle" class="w-5 h-5 text-green-500" />
      </Transition>
    </button>
    <span v-else class="check-circle">
      <Icon icon="lucide:circle" class="w-5 h-5" />
    </span>

    <Transition name="fade" mode="out-in">
      <template v-if="adding">
        <input
            key="add-input"
            ref="editInput"
            v-model="editValue"
            class="edit-input flex-1"
            placeholder="输入待办内容，回车保存"
            @keydown.enter="handleSave"
            @keydown.esc="handleCancel"
        />
      </template>
      <template v-else-if="!editing">
        <span key="text" class="todo-text flex-1" @dblclick="startEdit">{{ todo.title }}</span>
      </template>
      <template v-else>
        <input
            key="edit-input"
            ref="editInput"
            v-model="editValue"
            class="edit-input flex-1"
            @blur="finishEdit"
            @keydown.enter.prevent="finishEdit"
        />
      </template>
    </Transition>

    <div class="flex items-center gap-2 relative">
      <div class="relative">
        <button
            class="priority-dot"
            :style="{ background: displayPriorityColor }"
            @click.stop="showPriorityPicker = !showPriorityPicker"
            title="设置重要程度"
        ></button>
        <Transition name="fade">
          <div v-if="showPriorityPicker" ref="priorityPickerRef" class="priority-picker glass-card p-1.5">
            <button v-for="level in 5" :key="level" class="priority-option" :style="{ background: priorityColors[level - 1] }" @click="selectPriority(level)"></button>
          </div>
        </Transition>
      </div>

      <template v-if="adding">
        <button class="save-btn" @click="handleSave" title="保存">
          <Icon icon="lucide:check" class="w-4 h-4" />
        </button>
        <button class="cancel-btn" @click="handleCancel" title="取消">
          <Icon icon="lucide:x" class="w-4 h-4" />
        </button>
      </template>
      <template v-else>
        <button class="mini-btn" @click="startEdit" title="编辑">
          <Icon icon="lucide:pencil" class="w-4 h-4" />
        </button>
        <button class="mini-btn" @click="$emit('delete')" title="删除">
          <Icon icon="lucide:trash-2" class="w-4 h-4" />
        </button>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { Icon } from '@iconify/vue'

const props = defineProps({
  todo: { type: Object, default: () => ({ id: null, title: '', priority: 3, status: 0 }) },
  adding: { type: Boolean, default: false }
})
const emit = defineEmits(['toggle', 'edit', 'delete', 'priority', 'save', 'cancel'])

const editing = ref(false)
const editValue = ref('')
const editInput = ref(null)
const showPriorityPicker = ref(false)
const priorityPickerRef = ref(null)

const priorityColors = ['#10b981', '#3b82f6', '#8b5cf6', '#f59e0b', '#dc2626']

const displayPriorityColor = computed(() => {
  if (!props.adding && props.todo.status === 1) return '#ffffff'
  return priorityColors[(props.todo.priority - 1) || 0]
})

const priorityColorForLevel = (level) => priorityColors[level - 1]

const selectPriority = (level) => {
  showPriorityPicker.value = false
  if (props.adding) {
    emit('priority', level)
  } else if (level !== props.todo.priority) {
    emit('priority', level)
  }
}

const handleSave = () => {
  const title = editValue.value.trim()
  if (!title) return
  emit('save', { title, priority: props.todo.priority })
}

const handleCancel = () => {
  emit('cancel')
}

const handleOutsideClick = (event) => {
  if (showPriorityPicker.value && priorityPickerRef.value && !priorityPickerRef.value.contains(event.target)) {
    showPriorityPicker.value = false
  }
}

onMounted(() => {
  if (props.adding) {
    nextTick(() => editInput.value?.focus())
  }
  document.addEventListener('click', handleOutsideClick)
})
onBeforeUnmount(() => document.removeEventListener('click', handleOutsideClick))

const startEdit = async () => {
  editValue.value = props.todo.title
  editing.value = true
  await nextTick()
  editInput.value?.focus()
}
const finishEdit = () => {
  editing.value = false
  if (editValue.value.trim() && editValue.value !== props.todo.title) {
    emit('edit', editValue.value.trim())
  }
}
</script>

<style scoped>
.todo-item {
  transition: all 0.3s ease;
}
.todo-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 16px rgba(31, 38, 135, 0.12);
}
.todo-item.completed {
  opacity: 0.6;
}
.check-circle {
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
  color: var(--text-secondary);
  display: flex;
  align-items: center;
  justify-content: center;
}
.check-circle:hover { color: var(--text-primary); }
.todo-text {
  font-size: 0.95rem;
  color: var(--text-primary);
  outline: none;
  cursor: default;
}
.edit-input {
  background: transparent;
  border: none;
  outline: none;
  color: var(--text-primary);
  font-size: 0.95rem;
  padding: 0;
}
.edit-input::placeholder { color: var(--text-muted); }
.priority-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  border: none;
  cursor: pointer;
  flex-shrink: 0;
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
  transition: all 0.15s ease;
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
/* 过渡动画 */
.fade-enter-active, .fade-leave-active { transition: opacity 0.15s ease; }
.fade-enter-from, .fade-leave-to { opacity: 0; }
.check-enter-active, .check-leave-active { transition: all 0.2s ease; }
.check-enter-from, .check-leave-to { transform: scale(0.5); opacity: 0; }
</style>