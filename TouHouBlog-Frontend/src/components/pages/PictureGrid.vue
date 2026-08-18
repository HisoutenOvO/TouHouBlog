<template>
  <div class="gallery-wrapper" ref="galleryContainer">
    <!-- 管理员操作区 -->
    <div v-if="isAdmin" class="upload-area mb-4">
      <!-- 非编辑模式：显示“编辑模式”按钮 -->
      <button v-if="!editMode" class="upload-btn" @click="editMode = true">
        <Icon icon="lucide:pencil" class="w-5 h-5" />
        编辑模式
      </button>

      <!-- 编辑模式：显示“添加图片”和“完成”按钮 -->
      <template v-else>
        <button class="upload-btn" @click="triggerUpload">
          <Icon icon="lucide:plus" class="w-5 h-5" />
          添加图片
        </button>
        <button class="upload-btn done-btn" @click="editMode = false">
          <Icon icon="lucide:check" class="w-5 h-5" />
          完成
        </button>
      </template>

      <input
          ref="fileInput"
          type="file"
          accept="image/*"
          multiple
          @change="handleUpload"
          class="hidden"
      />
    </div>

    <!-- 瀑布流 -->
    <div v-if="columns.length" class="gallery-masonry">
      <div v-for="(column, colIndex) in columns" :key="colIndex" class="gallery-column">
        <div
            v-for="item in column"
            :key="item.src"
            class="gallery-item"
            :style="item.delay !== undefined ? { animationDelay: `${item.delay}ms` } : {}"
            @click="openLightbox(item.src)"
        >
          <img :src="item.src" alt="图集图片" />

          <!-- 编辑模式下的删除按钮 -->
          <button
              v-if="editMode"
              class="delete-btn"
              @click.stop="deleteImage(item.id)"
              title="删除图片"
          >
            <Icon icon="lucide:x" class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
    <div v-else class="text-center text-gray-500 py-20">暂无图片</div>

    <!-- 灯箱 -->
    <Transition name="lightbox">
      <div v-if="lightboxVisible" class="lightbox-overlay" @click.self="closeLightbox">
        <button class="lightbox-close" @click="closeLightbox">
          <Icon icon="lucide:x" class="w-6 h-6" />
        </button>
        <img :src="currentImage" class="lightbox-image" @click.stop />
      </div>
    </Transition>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../utils/request'
import OSS from 'ali-oss'
import { getUserFromToken } from '../../utils/auth'

const galleryContainer = ref(null)
const columns = ref([])
const columnCount = ref(4)
const isAdmin = ref(false)
const editMode = ref(false)
const fileInput = ref(null)
let resizeObserver = null
let isInitialLayout = true
let ossClient = null

const lightboxVisible = ref(false)
const currentImage = ref('')

const openLightbox = (src) => {
  currentImage.value = src
  lightboxVisible.value = true
}

const closeLightbox = () => {
  lightboxVisible.value = false
}

const triggerUpload = () => {
  fileInput.value?.click()
}

const handleUpload = async (e) => {
  const files = e.target.files
  if (!files.length) return

  if (!ossClient) {
    const res = await request.get('/api/oss/signature')
    const data = res.data.data
    ossClient = new OSS({
      region: data.region,
      endpoint: data.endpoint,
      accessKeyId: data.accessKeyId,
      accessKeySecret: data.accessKeySecret,
      bucket: data.bucket,
    })
  }

  for (const file of files) {
    const key = `gallery-images/${Date.now()}_${file.name}`
    try {
      const result = await ossClient.put(key, file)
      await request.post('/api/gallery', { url: result.url })
    } catch (err) {
      console.error('图片上传失败', err)
    }
  }

  await fetchImages()
  e.target.value = ''
}

const deleteImage = async (id) => {
  const confirmed = await window.$confirm('确定要删除这张图片吗？')
  if (!confirmed) return
  try {
    await request.delete(`/api/gallery/${id}`)
    await fetchImages()
  } catch (e) {
    // 全局拦截器已提示
  }
}

const fetchImages = async () => {
  try {
    const res = await request.get('/api/gallery/list')
    const images = res.data.data.map(item => ({
      id: item.id,
      url: item.url
    }))
    await layoutImages(images)
  } catch (e) {
    console.error('获取图集失败', e)
  }
}

const layoutImages = async (images) => {
  const containerWidth = galleryContainer.value?.clientWidth || 1200
  const count = getColumnCount()
  columnCount.value = count

  const columnHeights = new Array(count).fill(0)
  const newColumns = Array.from({ length: count }, () => [])

  const loadImage = (src) => {
    return new Promise((resolve) => {
      const img = new Image()
      img.onload = () => resolve({ id: null, src, width: img.naturalWidth, height: img.naturalHeight })
      img.onerror = () => resolve({ id: null, src, width: 1, height: 1 })
      img.src = src
    })
  }

  const loadedImages = await Promise.all(
      images.map(async (item) => {
        const imgInfo = await loadImage(item.url)
        return {
          id: item.id,
          src: item.url,
          width: imgInfo.width,
          height: imgInfo.height
        }
      })
  )

  const gap = 12
  const columnWidth = (containerWidth - (count - 1) * gap) / count
  let globalIndex = 0

  loadedImages.forEach((img) => {
    const imgHeight = (img.height / img.width) * columnWidth

    let minIndex = 0
    for (let i = 1; i < count; i++) {
      if (columnHeights[i] < columnHeights[minIndex]) {
        minIndex = i
      }
    }

    const item = {
      id: img.id,
      src: img.src,
      height: imgHeight
    }
    if (isInitialLayout) {
      item.delay = globalIndex * 60
    }

    newColumns[minIndex].push(item)
    columnHeights[minIndex] += imgHeight + gap
    globalIndex++
  })

  columns.value = newColumns
  if (isInitialLayout) isInitialLayout = false
}

const getColumnCount = () => {
  const width = galleryContainer.value?.clientWidth || 1200
  if (width < 600) return 1
  if (width < 900) return 2
  if (width < 1200) return 3
  return 4
}

onMounted(async () => {
  const user = getUserFromToken()
  isAdmin.value = !!(user && user.role === 1)

  await fetchImages()

  if (galleryContainer.value) {
    let lastWidth = galleryContainer.value.clientWidth || 0
    let resizeTimer = null

    resizeObserver = new ResizeObserver(() => {
      clearTimeout(resizeTimer)
      resizeTimer = setTimeout(() => {
        const newWidth = galleryContainer.value?.clientWidth || 0
        if (newWidth !== lastWidth) {
          lastWidth = newWidth
          const allItems = columns.value.flatMap(col => col.map(item => ({ id: item.id, url: item.src })))
          layoutImages(allItems)
        }
      }, 150)
    })

    resizeObserver.observe(galleryContainer.value)
  }
})

onBeforeUnmount(() => {
  if (resizeObserver) resizeObserver.disconnect()
})
</script>

<style scoped>
.gallery-wrapper {
  max-width: 1200px;
  margin: 0 auto;
  padding: 1rem;
}

.upload-area {
  display: flex;
  justify-content: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.6rem 1.2rem;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.5);
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  cursor: pointer;
  font-size: 0.9rem;
  transition: all 0.25s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.04);
}
.upload-btn:hover {
  background: linear-gradient(135deg, #f8c8dc, #ddc4f2);
  color: #523b52;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}
.done-btn {
  background: rgba(255, 255, 255, 0.7);
  color: #4b5563;
}
.done-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  color: #111827;
}

.gallery-masonry {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.gallery-column {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.gallery-item {
  position: relative;
  border-radius: 0.75rem;
  overflow: hidden;
  cursor: var(--cursor-pointer, pointer);
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  animation: galleryItemIn 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

.gallery-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.gallery-item img {
  width: 100%;
  height: auto;
  display: block;
}

/* 删除按钮 */
.delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.8);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s ease;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
.delete-btn:hover {
  background: rgba(255, 255, 255, 0.95);
  color: #ef4444;
}

@keyframes galleryItemIn {
  from {
    opacity: 0;
    transform: translateY(40px) scale(0.98);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.lightbox-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  z-index: 10000;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(4px);
}
.lightbox-image {
  max-width: 90vw;
  max-height: 90vh;
  border-radius: 0.75rem;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.5);
}
.lightbox-close {
  position: absolute;
  top: 1.5rem;
  right: 1.5rem;
  background: rgba(255, 255, 255, 0.2);
  border: none;
  color: white;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.2s;
}
.lightbox-close:hover {
  background: rgba(255, 255, 255, 0.35);
}
.lightbox-enter-active,
.lightbox-leave-active {
  transition: opacity 0.3s ease;
}
.lightbox-enter-from,
.lightbox-leave-to {
  opacity: 0;
}
</style>