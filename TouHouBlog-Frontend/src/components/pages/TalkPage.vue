<template>
  <div>
  <h2 class="text-2xl font-bold mb-6">杂谈列表</h2>
  <div v-if="loading" class="text-gray-500">加载中</div>
  <div v-else-if="talks.length" class="space-y-4">
    <div v-for="talk in talks" :key="talk.id" class="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
      <h3 class="text-lg font-semibold mb-2">{{ talk.content }}</h3>
      <p class="text-sm text-gray-400">{{talk.createTime}}</p>
  </div>
  </div>
    <div v-else class="text-gray-500">暂无杂谈。</div>
  </div>
</template>
<script setup>
import { ref,onMounted} from 'vue'
import axios from 'axios'

const talks = ref([]);
const loading = ref(true);
onMounted(async () => {
  try{
    const res = await axios.get('/api/talks/list?page=1&pageSize=10');
    talks.value = res.data.data.records;
  }catch(e){
    console.log(e)
  }finally{
    loading.value = false
  }
})

</script>