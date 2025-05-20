<template>
  <div class="space-user-analyze">
    <a-card title="用户空间图片分析">
      <v-chart :option="options" style="height: 320px; max-width: 100%" :loading="loading" />
      <template #extra>
<!--        <a-select v-model:value="timeDimension" style="width: 120px">
          <a-select-option value="day">日</a-select-option>
          <a-select-option value="week">周</a-select-option>
          <a-select-option value="month">月</a-select-option>
        </a-select>
        <a-input-search v-model:value="userId" placeholder="用户ID" style="width: 200px" />
        <a-button type="primary" @click="fetchData">查询</a-button>-->
        <a-space>
          <a-segmented v-model:value="timeDimension" :options="timeDimensionOptions" />
          <a-input-search  placeholder="请输入用户ID" enter-button="搜索用户" @search="doSearch" style="width: 200px" />
        </a-space>
      </template>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'
import { computed, ref, watchEffect } from 'vue'
import { getSpaceUserAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
import { message } from 'ant-design-vue'

interface Props {
  spaceId?: number
  queryAll?: boolean
  queryPublic?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  queryAll: false,
  queryPublic: false,
})

//时间维度 选项
const timeDimension = ref<'day' | 'week' | 'month'>('day')
//用户ID 选项
const userId = ref<string>()
//图表数据
const dataList = ref<API.SpaceUserAnalyzeResponse>([])
const timeDimensionOptions = [
  {
    label: '日',
    value: 'day',
  },
  {
    label: '周',
    value: 'week',
  },
  {
    label: '月',
    value: 'month',
  },
]

const doSearch = (value:string) => {
    userId.value = value
}
const options = computed(() => {
  const periods = dataList.value.map((item) => item.period) // 时间区间
  const counts = dataList.value.map((item) => item.count) // 上传数量

  return {
    tooltip: { trigger: 'axis' },
    xAxis: { type: 'category', data: periods, name: '时间区间' },
    yAxis: { type: 'value', name: '上传数量' },
    series: [
      {
        name: '上传数量',
        type: 'line',
        data: counts,
        smooth: true, // 平滑折线
        emphasis: {
          focus: 'series',
        },
      },
    ],
  }
})


const loading = ref<boolean>(false)

//获取数据
const fetchData = async () => {
  loading.value = true
  const res = await getSpaceUserAnalyzeUsingPost({
    spaceId: props.spaceId,
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
    timeDimension: timeDimension.value,
    userId: userId.value,
  })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data ?? []
  } else {
    message.error('获取数据失败')
  }
  loading.value = false
}
/**
 * 监听 props 变化，重新获取数据
 */
watchEffect(
  //只要fetchData()方法中的 props 属性发生变化，就会重新执行 fetchData() 方法。
  () => fetchData(),
)
</script>

<style scoped></style>
