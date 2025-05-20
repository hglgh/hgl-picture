<template>
  <div class="space-tag-analyze">
    <a-card title="空间图片标签分析">
      <v-chart :option="options" style="height: 320px; max-width: 100%" :loading="loading" />
    </a-card>
  </div>
</template>

<script setup lang="ts">
import VChart from 'vue-echarts'
import 'echarts'
import "echarts-wordcloud"
import { computed, ref, watchEffect } from 'vue'
import { getSpaceCategoryAnalyzeUsingPost, getSpaceTagAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
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

//图表数据
const dataList = ref<API.SpaceCategoryAnalyzeResponse>([])
const options = computed(() => {
  const tagData = dataList.value.map((item) => ({
    name: item.tag,
    value: item.count,
  }))

  return {
    tooltip: {
      trigger: 'item',
      formatter: (params: any) => `${params.name}: ${params.value} 次`,
    },
    series: [
      {
        type: 'wordCloud',
        gridSize: 10,
        sizeRange: [12, 50], // 字体大小范围
        rotationRange: [-90, 90],
        shape: 'circle',
        textStyle: {
          color: () =>
            `rgb(${Math.round(Math.random() * 255)}, ${Math.round(
              Math.random() * 255,
            )}, ${Math.round(Math.random() * 255)})`, // 随机颜色
        },
        data: tagData,
      },
    ],
  }
})


const loading = ref<boolean>(false)

//获取数据
const fetchData = async () => {
  loading.value = true
  const res = await getSpaceTagAnalyzeUsingPost({
    spaceId: props.spaceId,
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
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
