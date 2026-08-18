<template>
  <div class="max-w-5xl mx-auto px-4 py-8 space-y-6">
    <!-- 顶部欢迎卡片 -->
    <div class="glass-card p-8 flex flex-col md:flex-row items-center gap-8 relative overflow-hidden reveal-item">
      <div class="absolute -top-16 -left-16 w-48 h-48 bg-pink-200/30 rounded-full blur-3xl pointer-events-none"></div>
      <div class="absolute -bottom-16 -right-16 w-48 h-48 bg-violet-200/30 rounded-full blur-3xl pointer-events-none"></div>

      <div class="relative z-10 w-32 h-32 rounded-full overflow-hidden border-4 border-white/60 shadow-lg flex-shrink-0">
        <img src="/images/head.jpg" alt="Hisouten" class="w-full h-full object-cover" />
      </div>

      <div class="relative z-10 text-center md:text-left flex-1">
        <h1 class="about-name mb-2">Hisouten</h1>
        <p class="text-lg text-gray-600 italic">欢迎来到我的幻想世界</p>
        <p class="text-sm text-gray-400 mt-2">后端开发练习生 · 东方Project爱好者</p>
      </div>
    </div>

    <!-- 关于我 -->
    <div class="glass-card p-6 reveal-item">
      <h2 class="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
        <Icon icon="lucide:user" class="w-5 h-5 text-gray-600" />
        关于我
      </h2>

      <div class="space-y-4">
        <p class="text-gray-600 leading-relaxed">
          你好，我是 <span class="font-semibold text-gray-800">Hisouten</span>，一名热爱技术的高校学生。
          目前主要学习 Java 后端开发，同时对前端和人工智能领域也充满兴趣。
        </p>

        <div class="border-l-4 border-pink-300 pl-4 italic text-gray-500">
          最喜欢的原曲是「绯想天」（也是我名字的由来），最喜欢的同人曲是 senya 的「花鸟风月」。
        </div>

        <p class="text-gray-600 leading-relaxed">
          我喜欢东方Project，也喜欢把学到的东西记录下来。这个博客是我自己的小小避风港，
          用来存放学习笔记、幻想和日常。
        </p>

        <div class="bg-white/40 rounded-lg p-4 text-sm text-gray-600 leading-relaxed">
          准确来说我也不算是个车万人，只是喜欢听听车万的音乐，喜欢红白小巫女。
          车万的旋律在我重要的时候一直陪在我身边，而我对车万的人物和情节却还是在逐步探索的阶段，
          可以说我基本算个云车万，所以只敢自居东方爱好者，但我真的很喜欢车万的音乐和同人曲，所以这样子也挺好的嘛。
        </div>
      </div>

      <div class="mt-5 pt-4 border-t border-gray-100">
        <h3 class="text-sm font-semibold text-gray-700 mb-2">技能树</h3>
        <div class="flex flex-wrap gap-2">
          <span v-for="skill in skills" :key="skill" class="skill-tag">{{ skill }}</span>
        </div>
      </div>
    </div>

    <!-- 关于本站 -->
    <div class="glass-card p-6 reveal-item">
      <h2 class="text-xl font-bold text-gray-900 mb-4 flex items-center gap-2">
        <Icon icon="lucide:info" class="w-5 h-5 text-gray-600" />
        关于本站
      </h2>

      <div class="space-y-4">
        <p class="text-gray-600 leading-relaxed">
          TouHouBlog 是一个个人技术博客与生活记录站。这里主要发布后端开发、前端学习、
          东方Project相关的内容。
        </p>

        <div class="bg-white/40 rounded-lg p-4 text-sm text-gray-600 leading-relaxed">
          首页的黑胶唱片播放器收藏了我喜欢的音乐，所有的歌都是我自己精挑细选、非常值得一听的曲子。
          车万的曲子简直一绝，从 22 年开始这个歌单听了四五年了。
        </div>

        <p class="text-gray-600 leading-relaxed">
          其实把东方作为博客主题还是有点太大胆了，这个博客充其量算是个东方风味或者云东方主题。
          不过我的技术力目前只能到这了，这个小站算是我的避风港，可能从始至终也只有我一个人光顾。
          但这样也挺好，就像一个人包揽了整个幻想乡一样。
        </p>
      </div>

      <div class="mt-5 pt-4 border-t border-gray-100">
        <h3 class="text-sm font-semibold text-gray-700 mb-2">站点模块</h3>
        <div class="space-y-2 text-sm text-gray-600">
          <div class="flex items-center gap-2">
            <Icon icon="lucide:music" class="w-4 h-4 text-gray-500" />
            <span>音乐模块：支持歌词、歌单切换</span>
          </div>
          <div class="flex items-center gap-2">
            <Icon icon="lucide:file-text" class="w-4 h-4 text-gray-500" />
            <span>文章模块：支持搜索、分类/标签筛选、时间线</span>
          </div>
          <div class="flex items-center gap-2">
            <Icon icon="lucide:image" class="w-4 h-4 text-gray-500" />
            <span>图集模块：收藏喜欢的插画</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="grid grid-cols-2 md:grid-cols-5 gap-4">
      <div v-for="(stat, index) in stats" :key="stat.label" class="glass-card p-4 text-center reveal-item" :style="{ transitionDelay: `${index * 80}ms` }">
        <div class="text-3xl font-bold text-gray-900">{{ stat.value }}</div>
        <div class="text-xs text-gray-400 mt-1">{{ stat.label }}</div>
      </div>
    </div>

    <!-- 联系方式 -->
    <div class="glass-card p-6 text-center reveal-item">
      <h2 class="text-lg font-bold text-gray-900 mb-4">找到我</h2>
      <div class="flex justify-center gap-4">
        <a href="https://github.com/HisoutenOvO" target="_blank" rel="noopener"
           class="contact-icon" title="GitHub">
          <Icon icon="lucide:github" class="w-5 h-5" />
        </a>
        <a href="https://gitee.com/shi-changyu123" target="_blank" rel="noopener"
           class="contact-icon" title="Gitee">
          <Icon icon="simple-icons:gitee" class="w-5 h-5" />
        </a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { Icon } from '@iconify/vue'
import request from '../../utils/request'

const skills = ['Java', 'Spring Boot', 'Vue 3', 'Astro', 'Tailwind CSS', 'MySQL', 'Docker', 'Redis']

const stats = ref([
  { label: '文章', value: 0 },
  { label: '分类', value: 0 },
  { label: '标签', value: 0 },
  { label: '杂谈', value: 0 },
  { label: '运行天数', value: 0 }
])

let observer = null

const observeRevealItems = () => {
  observer = new IntersectionObserver(
      (entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            entry.target.classList.add('is-visible')
            observer.unobserve(entry.target)
          }
        })
      },
      { threshold: 0.1, rootMargin: '0px 0px -40px 0px' }
  )

  document.querySelectorAll('.reveal-item').forEach(el => {
    observer.observe(el)
  })
}

onMounted(async () => {
  try {
    const [articleRes, categoryRes, tagRes, talkRes] = await Promise.all([
      request.get('/api/articles/list', { params: { page: 1, pageSize: 1 } }),
      request.get('/api/categories/list', { params: { page: 1, pageSize: 1 } }),
      request.get('/api/tags/list', { params: { page: 1, pageSize: 1 } }),
      request.get('/api/talks/list', { params: { page: 1, pageSize: 1 } })
    ])

    stats.value[0].value = articleRes.data.data.total || 0
    stats.value[1].value = categoryRes.data.data.total || 0
    stats.value[2].value = tagRes.data.data.total || 0
    stats.value[3].value = talkRes.data.data.total || 0

    const startDate = new Date('2026-07-01')
    const today = new Date()
    const diff = Math.floor((today - startDate) / (1000 * 60 * 60 * 24))
    stats.value[4].value = diff
  } catch (e) {
    console.error('获取统计数据失败', e)
  }

  observeRevealItems()
})

onBeforeUnmount(() => {
  if (observer) observer.disconnect()
})
</script>

<style scoped>
/* 渐变名字 */
.about-name {
  font-family: "STKaiti", "KaiTi", "楷体", "华文楷体", serif;
  font-size: 4rem;
  font-weight: 700;
  background: linear-gradient(135deg, #ff8ac2, #f472b6, #e879f9, #a78bfa, #60a5fa);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  color: transparent;
  filter: drop-shadow(0 0 15px rgba(244, 114, 182, 0.7));
}

/* 滚动进入动画 */
.reveal-item {
  opacity: 0;
  transform: translateY(24px);
  transition: opacity 0.5s ease, transform 0.5s ease;
}

.reveal-item.is-visible {
  opacity: 1;
  transform: translateY(0);
}

/* 技能标签 */
.skill-tag {
  padding: 0.2rem 0.7rem;
  border-radius: 999px;
  font-size: 0.75rem;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #4b5563;
  transition: all 0.2s ease;
}
.skill-tag:hover {
  background: linear-gradient(135deg, #f9d5e5, #e8d5f5);
  color: #6b4b6b;
  transform: translateY(-1px);
}

/* 联系方式图标 */
.contact-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 2.5rem;
  height: 2.5rem;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.7);
  border: 1px solid rgba(255, 255, 255, 0.6);
  color: #6b7280;
  transition: all 0.25s ease;
}
.contact-icon:hover {
  background: rgba(255, 255, 255, 0.95);
  color: #111827;
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0,0,0,0.08);
}
</style>