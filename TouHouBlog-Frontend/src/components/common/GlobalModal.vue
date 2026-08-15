<template>
  <Transition name="modal" appear>
    <div
        v-if="visible"
        class="fixed inset-0 z-[999] flex items-center justify-center bg-black/30 backdrop-blur-sm p-4"
        @click.self="handleCancel"
    >
      <div class="glass-card w-full max-w-sm p-6 text-center" @click.stop>
        <div class="text-4xl mb-3" v-if="type === 'confirm'">❓</div>
        <div class="text-4xl mb-3" v-else>💬</div>

        <p class="text-gray-800 text-sm leading-relaxed mb-6">{{ message }}</p>

        <div v-if="type === 'confirm'" class="flex gap-3 justify-center">
          <button class="modal-btn cancel" @click="handleCancel">取消</button>
          <button class="modal-btn confirm" @click="handleConfirm">确定</button>
        </div>
        <div v-else class="flex justify-center">
          <button class="modal-btn confirm" @click="handleConfirm">知道了</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const visible = ref(false)
const type = ref('alert')
const message = ref('')
let resolvePromise = null

const showAlert = (msg) => {
  type.value = 'alert'
  message.value = msg
  visible.value = true
  return new Promise((resolve) => {
    resolvePromise = resolve
  })
}

const showConfirm = (msg) => {
  type.value = 'confirm'
  message.value = msg
  visible.value = true
  return new Promise((resolve) => {
    resolvePromise = resolve
  })
}

const handleConfirm = () => {
  visible.value = false
  if (resolvePromise) {
    resolvePromise(true)
    resolvePromise = null
  }
}

const handleCancel = () => {
  visible.value = false
  if (resolvePromise) {
    resolvePromise(false)
    resolvePromise = null
  }
}

const onAlert = (e) => {
  const { message, resolve } = e.detail
  showAlert(message).then(resolve)
}

const onConfirm = (e) => {
  const { message, resolve } = e.detail
  showConfirm(message).then(resolve)
}

onMounted(() => {
  window.addEventListener('global-alert', onAlert)
  window.addEventListener('global-confirm', onConfirm)
})

onBeforeUnmount(() => {
  window.removeEventListener('global-alert', onAlert)
  window.removeEventListener('global-confirm', onConfirm)
})
</script>

<style scoped>
.modal-btn {
  padding: 0.5rem 1.5rem;
  border-radius: 9999px;
  font-size: 0.9rem;
  cursor: pointer;
  transition: background 0.2s, transform 0.2s;
}
.modal-btn:active {
  transform: scale(0.95);
}
.modal-btn.cancel {
  background: rgba(255,255,255,0.7);
  color: #4b5563;
  border: 1px solid rgba(255,255,255,0.8);
}
.modal-btn.cancel:hover {
  background: rgba(255,255,255,0.9);
}
.modal-btn.confirm {
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  color: white;
  border: none;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}
.modal-btn.confirm:hover {
  box-shadow: 0 6px 14px rgba(0,0,0,0.15);
}

/* 弹窗过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-active .glass-card,
.modal-leave-active .glass-card {
  transition: transform 0.25s ease, opacity 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .glass-card,
.modal-leave-to .glass-card {
  transform: scale(0.9);
  opacity: 0;
}
</style>