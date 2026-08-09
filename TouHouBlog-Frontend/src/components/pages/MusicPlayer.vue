<template>
  <div ref="apContainer" style="display:none"></div>

  <!-- 留声机全屏模式 -->
  <div v-if="currentMode === 'full'" class="full-mode">
    <div class="record-player">
      <div class="turntable">
        <div class="tonearm" :class="{ playing: isPlaying }">
          <div class="tonearm-base"></div>
          <div class="tonearm-arm"></div>
          <div class="tonearm-head"></div>
        </div>
        <div class="vinyl-record" :class="{ spinning: isPlaying }" @click="togglePlay">
          <div class="vinyl-grooves"></div>
          <div class="vinyl-label">
            <img :src="currentCover" class="label-cover" />
          </div>
          <div class="play-overlay">
            <span class="text-4xl text-white">{{ isPlaying ? '⏸' : '▶' }}</span>
          </div>
        </div>
      </div>

      <div class="song-info">
        <h3>{{ currentName || '未选择' }}</h3>
        <p>{{ currentArtist }}</p>
      </div>

      <div class="progress-bar">
        <div class="bg-gray-200 h-1 rounded-full overflow-hidden">
          <div class="bg-gray-800 h-1 rounded-full transition-all duration-200" :style="{ width: progress + '%' }"></div>
        </div>
        <div class="flex justify-between text-xs text-gray-400 mt-1">
          <span>{{ formatTime(currentTime) }}</span>
          <span>{{ formatTime(duration) }}</span>
        </div>
      </div>

      <div class="controls">
        <button @click="changeMode" class="ctrl-btn mode-btn" :title="modeTitle">
          {{ modeIcon }}
        </button>
        <button @click="prevTrack" class="ctrl-btn" title="上一首">⏮</button>
        <button @click="togglePlay" class="ctrl-btn play-btn" :title="isPlaying ? '暂停' : '播放'">
          {{ isPlaying ? '⏸' : '▶' }}
        </button>
        <button @click="nextTrack" class="ctrl-btn" title="下一首">⏭</button>
        <button @click="togglePlaylist" class="ctrl-btn" title="歌单">📋</button>
      </div>

      <div class="volume-control">
        <span class="text-sm mr-2">🔊</span>
        <input type="range" min="0" max="1" step="0.01"
               v-model="sliderValue"
               @input="onSliderChange"
               class="volume-slider" />
      </div>

      <div v-if="showPlaylist" class="playlist-panel">
        <div class="playlist-header">
          <span>📄 歌单 ({{ playlistSongs.length }})</span>
          <button @click="showPlaylist = false" class="text-gray-400 hover:text-gray-600">✕</button>
        </div>
        <ul class="playlist-list">
          <li
              v-for="(song, index) in playlistSongs"
              :key="index"
              class="playlist-item"
              :class="{ active: index === currentIndex }"
              @click="playSong(index)"
          >
            <img :src="song.cover || defaultCover" class="playlist-item-cover" />
            <div class="playlist-item-info">
              <p class="text-sm font-medium truncate">{{ song.name }}</p>
              <p class="text-xs text-gray-400 truncate">{{ song.artist }}</p>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </div>

  <!-- 迷你卡片模式 -->
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
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  mode: { type: String, default: 'mini' }
})

const apContainer = ref(null)
let ap = null

const currentMode = ref(props.mode)

const currentName = ref('')
const currentArtist = ref('')
const currentCover = ref('')
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const progress = computed(() => duration.value ? (currentTime.value / duration.value) * 100 : 0)

// 播放模式管理
const PLAYLIST_MODE_KEY = 'music_play_mode'
const getStoredMode = () => {
  const mode = localStorage.getItem(PLAYLIST_MODE_KEY)
  return (mode === 'single' || mode === 'random') ? mode : 'list'
}
const playMode = ref(getStoredMode())

const showPlaylist = ref(false)
const playlistSongs = ref([])
const currentIndex = ref(0)
const defaultCover = 'https://picsum.photos/200?random=music'

// 模式图标与标题
const modeIcon = computed(() => {
  switch (playMode.value) {
    case 'single': return '🔂'
    case 'random': return '🔀'
    default: return '🔁'
  }
})
const modeTitle = computed(() => {
  switch (playMode.value) {
    case 'single': return '单曲循环'
    case 'random': return '随机播放'
    default: return '列表循环'
  }
})

// 【关键】应用播放模式到 APlayer，使用 ap.mode 即时生效
const applyPlayMode = (mode) => {
  if (!ap) return
  // 将内部标识符映射为 APlayer 支持的模式值
  const aplayerModeMap = {
    list: 'normal',
    single: 'single',
    random: 'random'
  }
  const targetMode = aplayerModeMap[mode] || 'normal'

  // 使用官方方法 setMode（如果存在，否则回退到直接赋值）
  if (typeof ap.setMode === 'function') {
    ap.setMode(targetMode)
  } else {
    ap.mode = targetMode
  }

  // 随机模式需要配合 order = 'random' 打乱列表顺序
  if (mode === 'random') {
    ap.order = 'random'
  } else {
    ap.order = 'list'
  }
  console.log('[Player] 模式已切换:', mode, '→ aplayer mode:', targetMode, 'order:', ap.order)
}

// 切换播放模式
const changeMode = () => {
  const modeOrder = ['list', 'single', 'random']
  const currentIdx = modeOrder.indexOf(playMode.value)
  const nextMode = modeOrder[(currentIdx + 1) % modeOrder.length]
  playMode.value = nextMode
  localStorage.setItem(PLAYLIST_MODE_KEY, nextMode)
  applyPlayMode(nextMode)   // 立即生效
}

// 音量控制（不变）
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

// 切歌函数
const switchToIndex = (index) => {
  if (!ap || !ap.list.audios.length) return
  const total = ap.list.audios.length
  let target = index
  if (target < 0) target = total - 1
  if (target >= total) target = 0
  ap.list.switch(target)
  ap.play()
}
const nextTrack = () => ap?.skipForward()
const prevTrack = () => ap?.skipBack()

// UI 同步
const syncUI = () => {
  if (!ap) return
  isPlaying.value = !ap.audio.paused
  currentTime.value = ap.audio.currentTime || 0
  duration.value = ap.audio.duration || 0

  if (ap.list && ap.list.audios.length > 0) {
    const idx = ap.list.index >= 0 && ap.list.index < ap.list.audios.length ? ap.list.index : 0
    currentIndex.value = idx
    const track = ap.list.audios[idx]
    if (track) {
      currentName.value = track.name || '未知歌曲'
      currentArtist.value = track.artist || '未知歌手'
      currentCover.value = track.cover || defaultCover
    }
    playlistSongs.value = ap.list.audios.map(s => ({
      name: s.name,
      artist: s.artist,
      cover: s.cover || defaultCover
    }))
  }

  sliderValue.value = volumeToSlider(ap.audio.volume || 0.1)
}

const togglePlay = () => ap?.toggle()
const togglePlaylist = () => { showPlaylist.value = !showPlaylist.value }
const playSong = (index) => {
  if (!ap) return
  switchToIndex(index)
  showPlaylist.value = false
}
const formatTime = (s) => {
  if (isNaN(s)) return '0:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec < 10 ? '0' : ''}${sec}`
}

// 路由切换
if (typeof window !== 'undefined') {
  watch(
      () => window.location.pathname,
      (newPath) => {
        currentMode.value = newPath === '/' ? 'full' : 'mini'
      },
      { immediate: true }
  )
}

let uiTimer = null
let saveTimer = null

onMounted(async () => {
  if (window[INSTANCE_KEY]) {
    ap = window[INSTANCE_KEY]
    console.log('[Player] 复用已有实例')
    const savedMode = getStoredMode()
    playMode.value = savedMode
    applyPlayMode(savedMode)   // 重新应用模式
    syncUI()
  } else {
    console.log('[Player] 创建新实例')
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
    console.log('[Player] 歌单加载完成，共', songs.length, '首')

    // 初始化模式
    const initMode = getStoredMode()
    const aplayerInitMode = initMode === 'single' ? 'single' : initMode === 'random' ? 'random' : 'normal'
    const aplayerInitOrder = initMode === 'random' ? 'random' : 'list'

    ap = new APlayer({
      container: apContainer.value,
      fixed: false,
      mini: false,
      autoplay: false,
      theme: '#b7b7b7',
      mode: aplayerInitMode,
      order: aplayerInitOrder,   // 必须添加 order 参数
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
    playMode.value = initMode
    console.log('[Player] 新实例已创建，初始模式:', initMode)

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
        console.log('[Player] 已恢复进度和音量')
      } catch (e) {}
    }
    syncUI()
  }

  uiTimer = setInterval(syncUI, 300)
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
})
</script>

<style scoped>
/* ========== 全屏模式 ========== */
.full-mode {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px 10px 10px;
  overflow: visible;
}

.record-player {
  text-align: center;
  max-width: 320px;
  width: 100%;
  position: relative;
}

.turntable {
  position: relative;
  width: 230px;
  height: 230px;
  margin: 0 auto 1.5rem;
}

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

.vinyl-record.spinning {
  animation: spin 20s linear infinite;
}
@keyframes spin {
  from { transform: translate(-50%, -50%) rotate(0deg); }
  to { transform: translate(-50%, -50%) rotate(360deg); }
}

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

/* 唱针系统 */
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

/* 歌曲信息 */
.song-info { margin-top: 0.5rem; }
.song-info h3 { font-weight: bold; margin: 0; font-size: 0.95rem; }
.song-info p { color: #666; margin: 0.2rem 0 0; font-size: 0.75rem; }

.progress-bar {
  width: 220px;
  margin: 0.5rem auto 0;
}

/* 控制栏 */
.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  margin-top: 0.75rem;
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
.mode-btn {
  font-size: 1.2rem;
  color: #666;
}

/* 音量控制 */
.volume-control {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 0.75rem;
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

/* 歌单面板 */
.playlist-panel {
  position: absolute;
  top: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
  width: 260px;
  background: white;
  border: 1px solid #e5e7eb;
  border-radius: 0.5rem;
  padding: 0.75rem;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  z-index: 50;
  text-align: left;
}
.playlist-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8rem;
  font-weight: bold;
  color: #374151;
  margin-bottom: 0.5rem;
}
.playlist-list {
  list-style: none;
  padding: 0;
  margin: 0;
  max-height: 200px;
  overflow-y: auto;
}
.playlist-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0;
  border-bottom: 1px solid #f3f4f6;
  cursor: pointer;
  transition: background 0.2s;
}
.playlist-item:hover {
  background: #f9fafb;
}
.playlist-item.active {
  background: #f3f4f6;
}
.playlist-item-cover {
  width: 28px;
  height: 28px;
  border-radius: 4px;
  object-fit: cover;
}
.playlist-item-info {
  flex: 1;
  min-width: 0;
}

/* 迷你模式 */
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