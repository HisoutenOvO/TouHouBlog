<template>
  <div ref="apContainer" style="display:none"></div>

  <!-- 留声机全屏模式 -->
  <div v-if="currentMode === 'full'" class="full-mode">
    <div class="record-player">
      <Transition name="fade" mode="out-in">
      <!-- 黑胶形态 -->
      <div v-if="currentView === 'player'" class="player-content">
        <!-- 左上角歌词切换按钮 -->
        <button class="view-switch-btn top-left" @click="currentView = 'lyric'">
          <Icon icon="lucide:mic-2" class="w-6 h-6" />
        </button>
        <!-- 右上角歌单切换按钮 -->
        <button class="view-switch-btn top-right" @click="currentView = 'playlist'">
          <Icon icon="lucide:list-music" class="w-6 h-6" />
        </button>

        <!-- 上半部分：黑胶唱片居中 -->
        <div class="turntable">
          <!-- 唱针 -->
          <div class="tonearm" :class="{ playing: isPlaying, switching: isSwitching }">
            <div class="tonearm-base"></div>
            <div class="tonearm-arm"></div>
            <div class="tonearm-head"></div>
          </div>

          <!-- 唱片外层：定位与切换动画 -->
          <Transition :name="discTransitionName" mode="out-in" @after-enter="onDiscEnter">
            <div
                :key="currentIndex"
                class="vinyl-record"
            >
              <!-- 唱片内层：纹理、标签和旋转动画 -->
              <div class="vinyl-disc" :class="{ spinning: isPlaying }">
                <div class="vinyl-grooves"></div>
                <div class="vinyl-label">
                  <img :src="currentCover" class="label-cover" />
                </div>
              </div>
            </div>
          </Transition>
        </div>

        <!-- 下半部分：信息与控制 -->
        <div class="player-bottom">
          <!-- 歌曲信息居中 -->
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

          <!-- 控制栏和音量放在同一行 -->
          <div class="controls-area">
            <div class="controls">
              <!-- 模式切换：直接用三元表达式映射图标 -->
              <button @click="changeMode" class="ctrl-btn mode-btn" :title="modeTitle">
                <Icon :icon="playMode === 'list' ? 'lucide:repeat' : playMode === 'single' ? 'lucide:repeat-1' : 'lucide:shuffle'" class="w-5 h-5" />
              </button>
              <button @click="prevTrack" class="ctrl-btn" title="上一首">
                <Icon icon="lucide:skip-back" class="w-5 h-5" />
              </button>
              <button @click="togglePlay" class="ctrl-btn play-btn" :title="isPlaying ? '暂停' : '播放'">
                <Icon :icon="isPlaying ? 'lucide:pause' : 'lucide:play'" class="w-6 h-6" />
              </button>
              <button @click="nextTrack" class="ctrl-btn" title="下一首">
                <Icon icon="lucide:skip-forward" class="w-5 h-5" />
              </button>
              <button @click="refreshPlaylist" class="ctrl-btn" title="刷新歌单">
                <Icon icon="lucide:refresh-cw" class="w-5 h-5" />
              </button>
            </div>

            <div class="volume-control">
              <Icon icon="lucide:volume-2" class="text-sm mr-2" />
              <input type="range" min="0" max="1" step="0.01"
                     v-model="sliderValue"
                     @input="onSliderChange"
                     class="volume-slider" />
            </div>
          </div>
        </div>
      </div>

      <!-- 歌词形态 -->
      <div v-else-if="currentView === 'lyric'" class="lyric-container">
        <div class="lyric-top">
          <img :src="currentCover" class="lyric-cover" />
          <div class="lyric-song-info">
            <p class="lyric-song-name">{{ currentName || '未选择' }}</p>
            <p class="lyric-song-artist">{{ currentArtist }}</p>
          </div>
          <button @click="currentView = 'player'" class="lyric-back-btn">← 返回</button>
        </div>

        <div class="lyric-scroll" ref="lyricScrollRef" @scroll="onLyricScroll">
          <p v-if="!lyricLines.length" class="lyric-empty">暂无歌词</p>
          <div
              v-for="(line, index) in lyricLines"
              :key="index"
              class="lyric-line"
              :class="{ active: index === currentLyricIndex }"
          >
            <p class="lyric-main">{{ line.text }}</p>
            <p v-if="line.translation" class="lyric-trans">{{ line.translation }}</p>
          </div>
        </div>

        <div class="lyric-bottom">
          <button @click="prevTrack" class="mini-ctrl">
            <Icon icon="lucide:skip-back" class="w-5 h-5" />
          </button>
          <button @click="togglePlay" class="mini-ctrl">
            <Icon :icon="isPlaying ? 'lucide:pause' : 'lucide:play'" class="w-5 h-5" />
          </button>
          <button @click="nextTrack" class="mini-ctrl">
            <Icon icon="lucide:skip-forward" class="w-5 h-5" />
          </button>
        </div>
      </div>

      <!-- 歌单形态 -->
      <div v-else-if="currentView === 'playlist'" class="playlist-view">
        <div class="playlist-view-top">
          <button @click="currentView = 'player'" class="lyric-back-btn">← 返回</button>
          <span class="playlist-title">歌单</span>
          <span class="playlist-count">{{ playlistSongs.length }} 首</span>
        </div>

        <div class="playlist-view-list">
          <div
              v-for="(song, index) in playlistSongs"
              :key="index"
              class="playlist-view-item"
              :class="{ active: index === currentIndex }"
              @click="playSong(index)"
          >
            <img :src="song.cover || defaultCover" class="playlist-view-cover" />
            <div class="playlist-view-info">
              <p class="playlist-view-name">{{ song.name }}</p>
              <p class="playlist-view-artist">{{ song.artist }}</p>
            </div>
            <span v-if="index === currentIndex" class="playlist-playing-icon">
              <Icon icon="lucide:play" class="w-4 h-4" />
            </span>
          </div>
        </div>
      </div>
      </Transition>
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
      <button @click="prevTrack" class="mini-ctrl">
        <Icon icon="lucide:skip-back" class="w-5 h-5" />
      </button>
      <button @click="togglePlay" class="mini-ctrl">
        <Icon :icon="isPlaying ? 'lucide:pause' : 'lucide:play'" class="w-5 h-5" />
      </button>
      <button @click="nextTrack" class="mini-ctrl">
        <Icon icon="lucide:skip-forward" class="w-5 h-5" />
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, onBeforeUnmount } from 'vue'
import request from '../../utils/request'
import { Icon } from '@iconify/vue'

let wasPlayingBeforeSwitch = false
const onDiscEnter = () => {
  // 唱片归位，放下唱针
  isSwitching.value = false

  // 如果切歌前正在播放，则继续播放
  if (wasPlayingBeforeSwitch && ap) {
    ap.play()
    wasPlayingBeforeSwitch = false
  }

  // 清除过渡名，避免后续非切歌触发动画
  discTransitionName.value = ''
}
const props = defineProps({
  mode: { type: String, default: 'mini' }
})
const discDirection = ref('next') // 'next' 或 'prev'
const isSwitching = ref(false)

const apContainer = ref(null)
let ap = null
let APlayerClass = null
const discTransitionName = ref('')   // 初始为空，无动画
let switchTimer = null               // 唱针动作定时器


const currentMode = ref(props.mode)

const currentName = ref('')
const currentArtist = ref('')
const currentCover = ref('')
const isPlaying = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const progress = computed(() => duration.value ? (currentTime.value / duration.value) * 100 : 0)

// 播放模式
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

// 歌词相关
const currentView = ref('player')           // 'player' | 'lyric'
const lyricLines = ref([])
const currentLyricIndex = ref(-1)
const lyricScrollRef = ref(null)
const currentSongId = ref('')
let lyricScrollLock = false

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

const refreshPlaylist = async () => {
  localStorage.removeItem(PLAYLIST_CACHE_KEY)
  try {
    const res = await fetch('/api/music/playlist')
    const json = await res.json()
    const songs = json.data || []
    if (songs.length) {
      localStorage.setItem(PLAYLIST_CACHE_KEY, JSON.stringify(songs))
      ap.list.clear()
      ap.list.add(songs.map(s => ({
        name: s.name || '未知歌曲',
        artist: s.artist || '未知歌手',
        url: s.url,
        cover: s.cover || '',
        id: s.id || ''
      })))
      ap.list.switch(0)
      syncUI()
    }
  } catch (e) {
    console.error('刷新歌单失败', e)
  }
}

// 音量
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

const switchToIndex = (index) => {
  if (!ap || !ap.list.audios.length) return
  const total = ap.list.audios.length
  let target = index
  if (target < 0) target = total - 1
  if (target >= total) target = 0
  ap.list.switch(target)
  ap.play()
}
const prevTrack = () => {
  if (!ap) return
  if (switchTimer) clearTimeout(switchTimer)
  wasPlayingBeforeSwitch = !ap.audio.paused
  ap.pause()                       // 先暂停，等动画完成再播放
  discTransitionName.value = 'disc-prev'
  isSwitching.value = true         // 立即抬起唱针
  ap.skipBack()                    // 触发唱片切换动画
}

const nextTrack = () => {
  if (!ap) return
  if (switchTimer) clearTimeout(switchTimer)
  wasPlayingBeforeSwitch = !ap.audio.paused
  ap.pause()
  discTransitionName.value = 'disc-next'
  isSwitching.value = true
  ap.skipForward()
}


// 加载歌词
const loadLyric = async (songId) => {
  if (!songId || songId === currentSongId.value) return
  currentSongId.value = songId
  lyricLines.value = []
  currentLyricIndex.value = -1
  try {
    const res = await request.get(`/api/music/lyric?songId=${songId}`)
    lyricLines.value = res.data.data || []
  } catch (e) {
    lyricLines.value = []
  }
}

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
      if (track.id && track.id !== currentSongId.value) {
        loadLyric(track.id)
      }
    }
    playlistSongs.value = ap.list.audios.map(s => ({
      name: s.name,
      artist: s.artist,
      cover: s.cover || defaultCover
    }))
  }

  // 歌词高亮
  if (lyricLines.value.length) {
    let newIndex = -1
    for (let i = 0; i < lyricLines.value.length; i++) {
      if (currentTime.value >= lyricLines.value[i].time) {
        newIndex = i
      } else {
        break
      }
    }
    if (newIndex !== currentLyricIndex.value) {
      currentLyricIndex.value = newIndex
      if (!lyricScrollLock && lyricScrollRef.value) {
        const activeEl = lyricScrollRef.value.querySelector('.lyric-line.active')
        if (activeEl) {
          activeEl.scrollIntoView({ behavior: 'smooth', block: 'center' })
        }
      }
    }
  }

  sliderValue.value = volumeToSlider(ap.audio.volume || 0.1)
}

const onLyricScroll = () => {
  lyricScrollLock = true
  clearTimeout(window.__lyricScrollTimer)
  window.__lyricScrollTimer = setTimeout(() => {
    lyricScrollLock = false
  }, 2000)
}

const togglePlay = () => ap?.toggle()
const togglePlaylist = () => { showPlaylist.value = !showPlaylist.value }
const playSong = (index) => {
  if (!ap) return
  if (switchTimer) clearTimeout(switchTimer)
  wasPlayingBeforeSwitch = !ap.audio.paused
  ap.pause()
  discTransitionName.value = index > currentIndex.value ? 'disc-next' : 'disc-prev'
  isSwitching.value = true
  switchToIndex(index)
  showPlaylist.value = false
  currentView.value = 'player'
}
const formatTime = (s) => {
  if (isNaN(s)) return '0:00'
  const m = Math.floor(s / 60)
  const sec = Math.floor(s % 60)
  return `${m}:${sec < 10 ? '0' : ''}${sec}`
}

const changeMode = () => {
  const modeOrder = ['list', 'single', 'random']
  const currentIdx = modeOrder.indexOf(playMode.value)
  const nextMode = modeOrder[(currentIdx + 1) % modeOrder.length]

  localStorage.setItem(PLAYLIST_MODE_KEY, nextMode)
  playMode.value = nextMode
  const APlayerCtor = APlayerClass || window.__APlayerClass
  if (APlayerClass && ap) {
    const oldIndex = ap.list.index
    const oldTime = ap.audio.currentTime
    const oldVolume = ap.audio.volume
    const wasPlaying = !ap.audio.paused

    const songs = ap.list.audios.map(s => ({
      name: s.name,
      artist: s.artist,
      url: s.url,
      cover: s.cover || '',
      id: s.id || ''
    }))

    ap.destroy()

    let newLoop, newOrder
    if (nextMode === 'single') {
      newLoop = 'one'
      newOrder = 'list'
    } else if (nextMode === 'random') {
      newLoop = 'all'
      newOrder = 'random'
    } else {
      newLoop = 'all'
      newOrder = 'list'
    }

    ap = new APlayerClass({
      container: apContainer.value,
      fixed: false,
      mini: false,
      autoplay: false,
      theme: '#b7b7b7',
      loop: newLoop,
      order: newOrder,
      preload: 'auto',
      volume: oldVolume,
      audio: songs
    })

    if (songs.length > 0) {
      const newIndex = (oldIndex >= 0 && oldIndex < songs.length) ? oldIndex : 0
      ap.list.switch(newIndex)
      if (oldTime) ap.seek(oldTime)
      if (wasPlaying) ap.play()
    }

    window[INSTANCE_KEY] = ap
    syncUI()
    console.log('[Player] 实例重建完成，新模式:', nextMode, 'loop:', newLoop, 'order:', newOrder)
  }
}

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
    APlayerClass = window.__APlayerClass || null   // 从全局恢复
    const savedMode = getStoredMode()
    playMode.value = savedMode
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
    APlayerClass = APlayer
    window.__APlayerClass = APlayer   // 新增：保存到全局，防止页面切换后丢失

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

    const initMode = getStoredMode()
    let initLoop, initOrder
    if (initMode === 'single') {
      initLoop = 'one'
      initOrder = 'list'
    } else if (initMode === 'random') {
      initLoop = 'all'
      initOrder = 'random'
    } else {
      initLoop = 'all'
      initOrder = 'list'
    }

    ap = new APlayer({
      container: apContainer.value,
      fixed: false,
      mini: false,
      autoplay: false,
      theme: '#b7b7b7',
      loop: initLoop,
      order: initOrder,
      preload: 'auto',
      volume: 0.1,
      audio: songs.map(s => ({
        name: s.name || '未知歌曲',
        artist: s.artist || '未知歌手',
        url: s.url,
        cover: s.cover || '',
        id: s.id || ''
      }))
    })
    window[INSTANCE_KEY] = ap
    playMode.value = initMode

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
  if (switchTimer) clearTimeout(switchTimer)
  if (uiTimer) clearInterval(uiTimer)
  if (saveTimer) clearInterval(saveTimer)
})
</script>

<style scoped>
   /* ========== 全屏模式 ========== */
 .full-mode {
   width: 100%;
   height: 100%;
   display: flex;
   justify-content: center;
   align-items: center;
   padding: 0;
   overflow: hidden;
   position: relative;
 }

.record-player {
  width: 100%;
  height: 100%;
  max-width: none;
  text-align: center;
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: linear-gradient(135deg, rgba(252, 228, 236, 0.7), rgba(232, 234, 246, 0.7), rgba(237, 231, 246, 0.7));
  backdrop-filter: blur(16px) saturate(160%);
  -webkit-backdrop-filter: blur(16px) saturate(160%);
  border-radius: 1.25rem;
  box-shadow: 0 8px 30px rgba(31, 38, 135, 0.12);
  padding: 1.25rem;
}

.player-content {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 1.25rem;
  width: 100%;
  height: 100%;
  position: relative;
}

.player-bottom {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
  width: 100%;
}

.view-switch-btn {
  position: absolute;
  top: 0.75rem;
  background: transparent;
  border: none;
  padding: 0.5rem;
  cursor: pointer;
  font-size: 1.25rem;
  z-index: 25;
  transition: color 0.2s, transform 0.2s;
  color: #6b7280;
  display: flex;
  align-items: center;
  justify-content: center;
}
.top-left { left: 0.75rem; }
.top-right { right: 0.75rem; }
.view-switch-btn:hover { color: #111827; transform: scale(1.1); }

.turntable {
  position: relative;
  width: 230px;
  height: 230px;
  margin: 0 auto;
}

/* 唱片外层：只负责定位和切换动画 */
.vinyl-record {
  width: 160px;
  height: 160px;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  box-shadow: 0 0 25px rgba(0,0,0,0.5), 0 0 0 6px rgba(20,20,20,0.8);
  cursor: pointer;
  transition: box-shadow 0.3s;
  overflow: hidden;
}
.vinyl-record:hover {
  box-shadow: 0 0 35px rgba(0,0,0,0.7), 0 0 0 6px rgba(20,20,20,0.9);
}

/* 唱片内层：负责纹理和旋转，不再参与定位，避免 transform 冲突 */
.vinyl-disc {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: radial-gradient(circle at center,
  #111 0%, #1a1a1a 20%, #222 22%, #111 24%,
  #222 26%, #111 28%, #222 30%, #111 32%,
  #222 34%, #111 36%, #222 38%, #111 40%,
  #1a1a1a 60%, #333 62%, #1a1a1a 65%, #111 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}
.vinyl-disc.spinning {
  animation: spin 20s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
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

/* 唱针系统 */
.tonearm {
  position: absolute;
  top: 15px;
  right: 40px;
  width: 0;
  height: 0;
  z-index: 15;
  transform-origin: 100% 50%;
  transform: rotate(-115deg);
  transition: transform 0.4s ease;
  pointer-events: none;
}
.tonearm.playing { transform: rotate(-80deg); }
.tonearm.switching {
  transform: rotate(-115deg);
  transition: transform 0.3s ease;
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
.song-info {
  text-align: center;
  width: 100%;
}
.song-info h3 {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 1.25rem;
  font-weight: 700;
  background: linear-gradient(135deg, #7c3aed, #ec4899, #3b82f6);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.12));
  margin: 0;
  letter-spacing: 0.02em;
}
.song-info p {
  font-size: 0.8rem;
  color: #6b7280;
  margin-top: 0.3rem;
  letter-spacing: 0.03em;
  font-style: italic;
}

.progress-bar {
  width: 90%;
  margin: 0 auto;
}

.controls-area {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1rem;
  flex-wrap: wrap;
}
.controls {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 0.75rem;
}
.ctrl-btn {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 1.3rem;
  color: #4b5563;
  transition: color 0.2s, transform 0.2s;
  padding: 0.25rem;
}
.ctrl-btn:hover { color: #111827; transform: scale(1.15); }
.play-btn {
  font-size: 1.8rem;
  color: #111827;
  background: rgba(255, 255, 255, 0.7);
  border-radius: 50%;
  width: 44px;
  height: 44px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
}
.play-btn:hover {
  background: rgba(255, 255, 255, 0.9);
  transform: scale(1.1);
}
.mode-btn { font-size: 1.2rem; color: #6b7280; }

.volume-control {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background: transparent;
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

/* 歌词容器 */
.lyric-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 1rem 0.75rem;
  box-sizing: border-box;
  background: linear-gradient(135deg, rgba(252, 228, 236, 0.7), rgba(232, 234, 246, 0.7), rgba(237, 231, 246, 0.7));
  backdrop-filter: blur(16px) saturate(160%);
  -webkit-backdrop-filter: blur(16px) saturate(160%);
  border-radius: 1.25rem;
  box-shadow: 0 8px 30px rgba(31, 38, 135, 0.12);
}
.lyric-top {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
}
.lyric-cover {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.lyric-song-info { flex: 1; min-width: 0; }
.lyric-song-name {
  font-size: 0.85rem;
  font-weight: 600;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.lyric-song-artist {
  font-size: 0.7rem;
  color: #6b7280;
  margin-top: 2px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.lyric-back-btn {
  background: none;
  border: none;
  font-size: 0.75rem;
  color: #6b7280;
  cursor: pointer;
  white-space: nowrap;
  flex-shrink: 0;
}
.lyric-back-btn:hover { color: #111827; }
.lyric-scroll {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  margin: 1rem 0;
  scroll-behavior: smooth;
  text-align: center;
  padding: 0.5rem 0;
  scrollbar-width: none;
}
.lyric-scroll::-webkit-scrollbar { display: none; }
.lyric-empty { color: #9ca3af; font-size: 0.8rem; margin-top: 2rem; }
.lyric-line { padding: 0.5rem 0; transition: all 0.25s; }
.lyric-main,
.lyric-trans {
  width: 100%;
  box-sizing: border-box;
  overflow-wrap: anywhere;
  word-break: break-word;
  white-space: pre-wrap;
  margin: 0 auto;
}
.lyric-main {
  font-size: 0.9rem;
  color: #4b5563;
  transition: color 0.3s, font-weight 0.3s;
}
.lyric-trans {
  font-size: 0.75rem;
  color: #9ca3af;
  margin-top: 0.25rem;
  transition: color 0.3s;
}
.lyric-line.active .lyric-main {
  color: #111827;
  font-weight: bold;
  transform: scale(1.08);
}
.lyric-line.active .lyric-trans { color: #4b5563; }
.lyric-bottom {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 1.5rem;
  padding-top: 0.5rem;
  background: transparent;
}

/* 歌单形态 */
.playlist-view {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 1rem 0.75rem;
  box-sizing: border-box;
  background: linear-gradient(135deg, rgba(252, 228, 236, 0.7), rgba(232, 234, 246, 0.7), rgba(237, 231, 246, 0.7));
  backdrop-filter: blur(16px) saturate(160%);
  -webkit-backdrop-filter: blur(16px) saturate(160%);
  border-radius: 1.25rem;
  box-shadow: 0 8px 30px rgba(31, 38, 135, 0.12);
}
.playlist-view-top {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding-bottom: 0.75rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.6);
}
.playlist-title {
  font-size: 0.9rem;
  font-weight: bold;
  color: #111827;
  flex: 1;
  text-align: left;
}
.playlist-count { font-size: 0.8rem; color: #6b7280; }
.playlist-view-list {
  flex: 1;
  overflow-y: auto;
  margin-top: 0.5rem;
  scrollbar-width: none;
  background: transparent;
}
.playlist-view-list::-webkit-scrollbar { display: none; }
.playlist-view-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.4);
  cursor: pointer;
  transition: background 0.2s;
}
.playlist-view-item:hover { background: rgba(255, 255, 255, 0.4); }
.playlist-view-item.active { background: rgba(255, 255, 255, 0.6); }
.playlist-view-cover {
  width: 40px;
  height: 40px;
  border-radius: 6px;
  object-fit: cover;
  flex-shrink: 0;
}
.playlist-view-info { flex: 1; min-width: 0; }
.playlist-view-name {
  font-size: 0.85rem;
  color: #111827;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.playlist-view-artist {
  font-size: 0.75rem;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.playlist-playing-icon {
  font-size: 0.9rem;
  color: #111827;
  flex-shrink: 0;
}

/* 迷你模式 */
.mini-bar {
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(255, 255, 255, 0.6);
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
  color: #374151;
}
.mini-ctrl:hover { color: #111827; }

/* 视图切换淡入淡出 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.98);
}

/* 唱片切换动画：下一首 */
.disc-next-enter-active,
.disc-next-leave-active {
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
.disc-next-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) translateX(100%);
}
.disc-next-enter-to {
  opacity: 1;
  transform: translate(-50%, -50%) translateX(0);
}
.disc-next-leave-from {
  opacity: 1;
  transform: translate(-50%, -50%) translateX(0);
}
.disc-next-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) translateX(-100%);
}

/* 唱片切换动画：上一首 */
.disc-prev-enter-active,
.disc-prev-leave-active {
  transition: all 0.6s cubic-bezier(0.22, 1, 0.36, 1);
}
.disc-prev-enter-from {
  opacity: 0;
  transform: translate(-50%, -50%) translateX(-100%);
}
.disc-prev-enter-to {
  opacity: 1;
  transform: translate(-50%, -50%) translateX(0);
}
.disc-prev-leave-from {
  opacity: 1;
  transform: translate(-50%, -50%) translateX(0);
}
.disc-prev-leave-to {
  opacity: 0;
  transform: translate(-50%, -50%) translateX(100%);
}
</style>