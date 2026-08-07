<template>
  <div class="bg-white rounded-lg shadow-sm border border-gray-100">
    <h3 class="font-bold text-gray-900 p-4 pb-2">🎵 音乐</h3>
    <div ref="playerContainer"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const playerContainer = ref(null)
let ap = null

onMounted(async () => {
  // 仅在浏览器端动态加载 APlayer，服务端绝对不会碰到这些代码
  const [APlayerModule, cssModule] = await Promise.all([
    import('aplayer'),
    import('aplayer/dist/APlayer.min.css')
  ])
  const APlayer = APlayerModule.default

  ap = new APlayer({
    container: playerContainer.value,
    fixed: false,
    mini: false,
    autoplay: false,
    theme: '#b7b7b7',
    loop: 'all',
    order: 'random',
    preload: 'auto',
    volume: 0.7,
    mutex: true,
    listFolded: true,
    listMaxHeight: '200px',
    audio: [
      {
        name: 'Song 1',
        artist: 'Artist 1',
        url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
        cover: 'https://picsum.photos/200/200?random=1'
      },
      {
        name: 'Song 2',
        artist: 'Artist 2',
        url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3',
        cover: 'https://picsum.photos/200/200?random=2'
      },
      {
        name: 'Song 3',
        artist: 'Artist 3',
        url: 'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3',
        cover: 'https://picsum.photos/200/200?random=3'
      }
    ]
  })
})

onBeforeUnmount(() => {
  if (ap) ap.destroy()
})
</script>

<style>
/* 与博客风格统一的样式 */
.aplayer {
  background: transparent !important;
  border-radius: 0 0 0.5rem 0.5rem;
  margin: 0 !important;
  font-family: inherit;
  box-shadow: none !important;
}
.aplayer .aplayer-list {
  max-height: 180px !important;
}
.aplayer .aplayer-list ol li:hover {
  background: #f3f4f6;
}
.aplayer .aplayer-info .aplayer-music {
  text-align: left;
}
</style>