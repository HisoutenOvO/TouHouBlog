<template>
  <div ref="apContainer" style="display:none"></div>

  <!-- 留声机全屏模式 -->
  <div v-if="currentMode === 'full'" class="full-mode">
    <div class="record-player">
      <!-- 黑胶唱片 + 唱针区域 -->
      <div class="turntable">
        <!-- 唱针臂 -->
        <div class="tonearm" :class="{ playing: isPlaying }">
          <div class="tonearm-base"></div>
          <div class="tonearm-arm"></div>
          <div class="tonearm-head"></div>
        </div>

        <!-- 唱片盘 -->
        <div class="vinyl-record" :class="{ spinning: isPlaying }" @click="togglePlay">
          <!-- 唱片纹理 -->
          <div class="vinyl-grooves"></div>
          <!-- 中心标签（封面） -->
          <div class="vinyl-label">
            <img :src="currentCover" class="label-cover" />
          </div>
          <!-- 播放/暂停图标覆盖层 -->
          <div class="play-overlay">
            <span class="text-4xl text-white">{{ isPlaying ? '⏸' : '▶' }}</span>
          </div>
        </div>
      </div>

      <!-- 歌曲信息 -->
      <div class="song-info">
        <h3>{{ currentName || '未选择' }}</h3>
        <p>{{ currentArtist }}</p>
      </div>

      <!-- 进度条 -->
      <div class="progress-bar">
        <div class="bg-gray-200 h-1 rounded-full overflow-hidden">
          <div class="bg-gray-800 h-1 rounded-full transition-all duration-200" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="flex justify-between text-xs text-gray-400 mt-1">
          <span>{{ formatTime(currentTime) }}</span>
          <span>{{ formatTime(duration) }}</span>
        </div>
      </div>

      <!-- 控制栏（优化后） -->
      <div class="controls">
        <button @click="prevTrack" class="ctrl-btn" title="上一首">⏮</button>
        <button @click="togglePlay" class="ctrl-btn play-btn" :title="isPlaying ? '暂停' : '播放'">
          {{ isPlaying ? '⏸' : '▶' }}
        </button>
        <button @click="nextTrack" class="ctrl-btn" title="下一首">⏭</button>
      </div>

      <!-- 音量控制 -->
      <div class="volume-control">
        <span class="text-sm mr-2">🔊</span>
        <input type="range" min="0" max="1" step="0.01"
               v-model="sliderValue"
               @input="onSliderChange(sliderValue)"
               class="volume-slider" />
      </div>
    </div>
  </div>

  <!-- 迷你卡片模式（保持不变） -->
  <div v-else-if="currentMode === 'mini'" class="mini-bar">
    <img :src="currentCover" class="w-10 h-10 rounded" />
    <div class="flex-1 min-w-0">
      <p class="text-sm font-medium truncate">{{ currentName }}</p>
      <p class="text-xs text-gray-400 truncate">{{ currentArtist }}</p>
    </div>
    <div class="flex items-center gap-2">
      <button @click="prevTrack" class="mini-ctrl">⏮</button>
      <button @click="togglePlay" class="mini-ctrl">{{ isPlaying ? '⏸' : '▶' }}</button>
      <button @click="nextTrack" class="mini-ctrl">⏭</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, computed, watch } from 'vue'

const props = defineProps({
  mode: { type: String, default: 'mini' }
})

const apContainer = ref(null)
let ap = null

const currentMode = ref(props.mode)

// 歌曲信息
const currentName = ref('')
const currentArtist = ref('')
const currentCover = ref('')
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const progress = computed(() => duration.value ? (currentTime.value / duration.value) * 100 : 0)

// 音量映射
const sliderValue = ref(0.5)
const actualVolume = computed(() => {
  const val = sliderValue.value
  if (val <= 0.5) return val * 0.2
  return 0.1 + (val - 0.5) * 1.8
})
const volumeToSlider = (vol) => {
  if (vol <= 0.1) return vol / 0.2
  return 0.5 + (vol - 0.1) / 1.8
}
const onSliderChange = () => {
  const vol = actualVolume.value
  if (ap) {
    ap.volume(vol)
    const saved = JSON.parse(localStorage.getItem(PROGRESS_KEY)) || {}
    saved.volume = vol
    localStorage.setItem(PROGRESS_KEY, JSON.stringify(saved))
  }
}

const INSTANCE_KEY = '__MUSIC_PLAYER_INSTANCE__'
const PLAYLIST_CACHE_KEY = 'touhou_music_playlist_cache'
const PROGRESS_KEY = 'music-progress'

// 从 APlayer 实例同步所有 UI 状态（无事件，直接读取）
const syncUI = () => {
  if (!ap) return
  // 播放状态
  isPlaying.value = !ap.audio.paused
  // 进度
  currentTime.value = ap.audio.currentTime || 0
  duration.value = ap.audio.duration || 0
  // 当前歌曲信息
  if (ap.list && ap.list.audios.length > 0) {
    const idx = ap.list.index >= 0 && ap.list.index < ap.list.audios.length ? ap.list.index : 0
    const track = ap.list.audios[idx]
    if (track) {
      currentName.value = track.name || '未知歌曲'
      currentArtist.value = track.artist || '未知歌手'
      currentCover.value = track.cover || 'https://picsum.photos/200?random=music'
    }
  }
  // 音量滑块同步（从 ap 实际音量反算）
  sliderValue.value = volumeToSlider(ap.audio.volume || 0.1)
}

const togglePlay = () => ap?.toggle()
const prevTrack = () => ap?.skipBack()
const nextTrack = () => ap?.skipForward()
const formatTime = (s) => {
  if (isNaN(s)) return '0:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec < 10 ? '0' : ''}${sec}`
}

// 路由模式切换
if (typeof window !== 'undefined') {
  watch(
      () => window.location.pathname,
      (newPath) => {
        currentMode.value = newPath === '/' ? 'full' : 'mini'
      },
      { immediate: true }
  )
}

// 定时器 ID
let uiTimer = null
let saveTimer = null

onMounted(async () => {
  // 1. 尝试复用全局实例
  if (window[INSTANCE_KEY]) {
    ap = window[INSTANCE_KEY]
    syncUI()  // 立即同步一次
  } else {
    // 2. 创建新实例（同之前的逻辑）
    const [APlayerModule] = await Promise.all([
      import('aplayer'),
      new Promise((resolve) => {
        if (!document.querySelector('link[href*="APlayer.min.css"]')) {
          const link = document.createElement('link')
          link.rel = 'stylesheet'
          link.href = 'https://cdn.jsdelivr.net/npm/aplayer@1.10.1/dist/APlayer.min.css'
          link.onload = resolve
          document.head.appendChild(link)
        } else resolve()
      })
    ])
    const APlayer = APlayerModule.default

    let songs = []
    const cached = localStorage.getItem(PLAYLIST_CACHE_KEY)
    if (cached) {
      try { songs = JSON.parse(cached) } catch (e) {}
    }
    if (!songs.length) {
      try {
        const res = await fetch('/api/music/playlist')
        const json = await res.json()
        songs = json.data || []
        if (songs.length) {
          localStorage.setItem(PLAYLIST_CACHE_KEY, JSON.stringify(songs))
        }
      } catch (e) {
        console.error('获取歌单失败', e)
      }
    }

    ap = new APlayer({
      container: apContainer.value,
      fixed: false,
      mini: false,
      autoplay: false,
      theme: '#b7b7b7',
      loop: 'all',
      order: 'list',
      preload: 'auto',
      volume: 0.1,
      audio: songs.map(s => ({
        name: s.name || '未知歌曲',
        artist: s.artist || '未知歌手',
        url: s.url,
        cover: s.cover || ''
      }))
    })

    window[INSTANCE_KEY] = ap

    // 恢复进度和音量
    const saved = JSON.parse(localStorage.getItem(PROGRESS_KEY))
    if (saved) {
      try {
        const idx = (saved.index >= 0 && saved.index < ap.list.audios.length) ? saved.index : 0
        if (ap.list.audios.length > 0) {
          ap.list.switch(idx)
          if (saved.currentTime) ap.seek(saved.currentTime)
        }
        const vol = saved.volume ?? 0.1
        ap.volume(vol)
      } catch (e) {}
    }
    syncUI()
  }

  // 3. 启动 UI 轮询器（每 300ms 同步一次）
  uiTimer = setInterval(syncUI, 300)

  // 4. 定时保存进度到 localStorage（保留原有功能）
  saveTimer = setInterval(() => {
    if (!ap) return
    const old = JSON.parse(localStorage.getItem(PROGRESS_KEY)) || {}
    localStorage.setItem(PROGRESS_KEY, JSON.stringify({
      index: ap.list.index,
      currentTime: ap.audio.currentTime,
      volume: old.volume ?? ap.audio.volume
    }))
  }, 2000)
})

onBeforeUnmount(() => {
  if (uiTimer) clearInterval(uiTimer)
  if (saveTimer) clearInterval(saveTimer)
  // 注意：不要销毁 ap，因为是全局单例
})
</script>

<style scoped>
/* ========== 全屏模式 ========== */
.full-mode {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 10px 10px;
  overflow: hidden;
}

.record-player {
  text-align: center;
  max-width: 300px;
  width: 100%;
}

/* 转盘容器，稍微放大以容纳唱针 */
.turntable {
  position: relative;
  width: 230px;
  height: 230px;
  margin: 0 auto 1.5rem;
}

/* 唱片（位置不变） */
.vinyl-record {
  width: 160px;
  height: 160px;
  border-radius: 50%;
  background: radial-gradient(circle at center,
  #111 0%, #1a1a1a 20%, #222 22%, #111 24%,
  #222 26%, #111 28%, #222 30%, #111 32%,
  #222 34%, #111 36%, #222 38%, #111 40%,
  #1a1a1a 60%, #333 62%, #1a1a1a 65%, #111 100%);
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 0 25px rgba(0,0,0,0.5),
  0 0 0 6px rgba(20,20,20,0.8);
  cursor: pointer;
  transition: box-shadow 0.3s;
}
.vinyl-record:hover {
  box-shadow: 0 0 35px rgba(0,0,0,0.7),
  0 0 0 6px rgba(20,20,20,0.9);
}

/* 旋转动画 */
.vinyl-record.spinning {
  animation: spin 20s linear infinite;
}
@keyframes spin {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

/* 中心封面 */
.vinyl-label {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 90px;
  height: 90px;
  border-radius: 50%;
  background: #fff;
  overflow: hidden;
  border: 2px solid #ddd;
  box-shadow: 0 0 0 4px rgba(0,0,0,0.1);
  z-index: 2;
}
.label-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.play-overlay {
  position: absolute;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.3);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: background 0.3s;
  z-index: 3;
}
.vinyl-record:hover .play-overlay {
  background: rgba(0,0,0,0.5);
}

/* ========== 唱针系统 ========== */
.tonearm {
  position: absolute;
  top: 15px;
  right: 40px;
  width: 0;
  height: 0;
  z-index: 15;
  transform-origin: 100% 50%;
  transform: rotate(-110deg);
  transition: transform 0.4s ease;
  pointer-events: none;
}
.tonearm.playing {
  transform: rotate(-80deg);
}

.tonearm-arm {
  position: absolute;
  top: 50%;
  right: 0;
  width: 110px;
  height: 4px;
  background: linear-gradient(to left, #777, #333);
  transform: translateY(-50%);
  border-radius: 2px;
  transform-origin: 100% 50%;
}

.tonearm-head {
  position: absolute;
  left: -6px;
  top: 50%;
  transform: translateY(-50%);
  width: 22px;
  height: 10px;
  background: #444;
  border-radius: 2px 10px 10px 2px;
}
.tonearm-head::after {
  content: '';
  position: absolute;
  bottom: -3px;
  left: 3px;
  width: 6px;
  height: 6px;
  background: #888;
  border-radius: 50%;
}

.tonearm-base {
  position: absolute;
  top: 50%;
  right: -8px;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  background: #555;
  border-radius: 50%;
  box-shadow: 0 0 4px rgba(0,0,0,0.5);
}

/* ========== 下方控制区 ========== */
.song-info { margin-top: 0.5rem; }
.song-info h3 { font-weight: bold; margin: 0; font-size: 0.95rem; }
.song-info p { color: #666; margin: 0.2rem 0 0; font-size: 0.75rem; }

.progress-bar {
  width: 220px;
  margin: 0.5rem auto 0;
}

.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
  margin-top: 1rem;
}
.ctrl-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.3rem;
  color: #444;
  transition: color 0.2s, transform 0.2s;
  padding: 0.25rem;
}
.ctrl-btn:hover {
  color: #000;
  transform: scale(1.15);
}
.play-btn {
  font-size: 1.8rem;
  color: #222;
  background: #f3f4f6;
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
.play-btn:hover {
  background: #e5e7eb;
  transform: scale(1.1);
}

/* 优化后的音量控制 */
.volume-control {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 1.25rem;
  padding-bottom: 0.25rem;
}

.volume-slider {
  width: 120px;
  height: 6px;
  -webkit-appearance: none;
  appearance: none;
  background: #e5e7eb;
  border-radius: 3px;
  outline: none;
  cursor: pointer;
  transition: background 0.2s;
}
.volume-slider::-webkit-slider-thumb {
  -webkit-appearance: none;
  appearance: none;
  width: 16px;
  height: 16px;
  background: #374151;
  border-radius: 50%;
  box-shadow: 0 1px 4px rgba(0,0,0,0.2);
  transition: background 0.2s, transform 0.2s;
}
.volume-slider::-webkit-slider-thumb:hover {
  background: #111827;
  transform: scale(1.15);
}
.volume-slider::-moz-range-thumb {
  width: 16px;
  height: 16px;
  background: #374151;
  border-radius: 50%;
  border: none;
  box-shadow: 0 1px 4px rgba(0,0,0,0.2);
}
.volume-slider::-moz-range-track {
  height: 6px;
  background: #e5e7eb;
  border-radius: 3px;
}

/* ========== 迷你模式 ========== */
.mini-bar {
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.mini-bar img { object-fit: cover; }
.mini-ctrl {
  background: none;
  border: none;
  font-size: 1rem;
  cursor: pointer;
  color: #333;
}
.mini-ctrl:hover { color: #000; }
</style>