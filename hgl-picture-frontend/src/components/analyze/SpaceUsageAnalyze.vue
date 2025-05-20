<template>
  <div class="space-user-analyze">
    <a-flex gap="middle">
      <a-card title="存储空间" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>
            {{ formatSize(data.usedSize) }} /{{
              data.maxSize ? formatSize(data.maxSize) : '无限制'
            }}
          </h3>
          <a-progress type="dashboard" :percent="data.sizeUsageRatio ?? 0" />
        </div>
      </a-card>
      <a-card title="图片数量" style="width: 50%">
        <div style="height: 320px; text-align: center">
          <h3>{{ data.usedCount }} /{{ data.maxCount ?? '无限制' }}</h3>
          <a-progress type="dashboard" :percent="data.countUsageRatio ?? 0" />
        </div>
      </a-card>
    </a-flex>
  </div>
</template>

<script setup lang="ts">
import { ref, watchEffect } from 'vue'
import { getSpaceUsageAnalyzeUsingPost } from '@/api/spaceAnalyzeController'
import { message } from 'ant-design-vue'
import { formatSize } from '../../utils'

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
const data = ref<API.SpaceUsageAnalyzeResponse>({})
const loading = ref<boolean>(false)

//获取数据
const fetchData = async () => {
  loading.value = true
  const res = await getSpaceUsageAnalyzeUsingPost({
    spaceId: props.spaceId,
    queryAll: props.queryAll,
    queryPublic: props.queryPublic,
  })
  if (res.data.code === 0 && res.data.data) {
    data.value = res.data.data
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
