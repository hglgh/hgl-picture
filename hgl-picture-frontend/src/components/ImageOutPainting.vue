<template>
  <a-modal
    class="image-out-painting"
    v-model:visible="visible"
    title="AI 扩图"
    :footer="false"
    @cancel="closeModal"
  >
    <a-row gutter="16">
      <a-col span="12">
        <h4>原始图片</h4>
        <img :src="picture?.url" :alt="picture?.name" style="max-width: 100%" />
      </a-col>
      <a-col span="12">
        <h4>扩图结果</h4>
        <img
          v-if="resultImageUrl"
          :src="resultImageUrl"
          :alt="picture?.name"
          style="max-width: 100%"
        />
      </a-col>
    </a-row>
    <div style="margin-bottom: 16px" />
    <a-flex gap="16" justify="center">
      <a-button type="primary" ghost @click="createTask">生成图片</a-button>
      <a-button type="primary" @click="handleUpload">应用结果</a-button>
    </a-flex>
  </a-modal>
</template>

<script setup lang="ts">
import { onUnmounted, ref } from 'vue'
import {
  createPictureOutPaintingTaskUsingPost,
  getPictureOutPaintingTaskUsingGet,
  uploadPictureByUrlUsingPost,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess?: (newPicture: API.PictureVO) => void
}

const props = defineProps<Props>()

// 是否可见
const visible = ref(false)

// 打开弹窗
const openModal = () => {
  visible.value = true
}

// 关闭弹窗
const closeModal = () => {
  visible.value = false
}

// 暴露函数给父组件
defineExpose({
  openModal,
})

const resultImageUrl = ref<string>()
// 任务 id
let taskId = ref<string>()

/**
 * 创建任务
 */
const createTask = async () => {
  if (!props.picture?.id) {
    return
  }
  const res = await createPictureOutPaintingTaskUsingPost({
    pictureId: props.picture.id,
    // 可以根据需要设置扩图参数
    parameters: {
      xScale: 2,
      yScale: 2,
    },
  })
  if (res.data.code === 0 && res.data.data) {
    message.success('创建任务成功，请耐心等待，不要退出界面')
    console.log(res.data.data.output?.taskId)
    taskId.value = res.data.data.output?.taskId
    // 开启轮询
    startPolling()
  } else {
    message.error('创建任务失败，' + res.data.message)
  }
}
// 轮询定时器
let pollingTimer: NodeJS.Timeout = null

// 清理轮询定时器
const clearPolling = () => {
  if (pollingTimer) {
    clearInterval(pollingTimer)
    pollingTimer = null
    taskId.value = ''
  }
}

/**
 * 开始轮询任务状态
 */
const startPolling = () => {
  if (!taskId.value) return

  pollingTimer = setInterval(checkTaskStatus, 3000)
}


/**
 * 轮询任务状态检查
 */
const checkTaskStatus = async () => {
  try {
    const res = await getPictureOutPaintingTaskUsingGet({
      taskId: taskId.value,
    })
    handleTaskResponse(res)
  } catch (error) {
    handlePollingError(error)
  }
}

/**
 * 处理任务响应结果
 */
const handleTaskResponse = (res: any) => {
  if (res.data.code !== 0 || !res.data.data) {
    return
  }

  const taskResult = res.data.data.output
  if (taskResult?.taskStatus === 'SUCCEEDED') {
    handleTaskSuccess(taskResult)
  } else if (taskResult?.taskStatus === 'FAILED') {
    handleTaskFailure()
  }
}

/**
 * 处理任务成功
 */
const handleTaskSuccess = (taskResult: any) => {
  message.success('扩图任务成功')
  resultImageUrl.value = taskResult.outputImageUrl
  clearPolling()
}

/**
 * 处理任务失败
 */
const handleTaskFailure = () => {
  message.error('扩图任务失败')
  clearPolling()
}

/**
 * 处理轮询错误
 */
const handlePollingError = (error: any) => {
  console.error('轮询任务状态失败', error)
  message.error('检测任务状态失败，请稍后重试')
  clearPolling()
}


// 清理定时器，避免内存泄漏
onUnmounted(() => {
  clearPolling()
})
const uploadLoading = ref<boolean>(false)

const handleUpload = async () => {
  uploadLoading.value = true
  try {
    const params: API.PictureUploadRequest = {
      fileUrl: resultImageUrl.value,
      spaceId: props.spaceId,
    }
    if (props.picture) {
      params.id = props.picture.id
    }
    const res = await uploadPictureByUrlUsingPost(params)
    if (res.data.code === 0 && res.data.data) {
      message.success('图片上传成功')
      // 将上传成功的图片信息传递给父组件
      props.onSuccess?.(res.data.data)
      // 关闭弹窗
      closeModal()
    } else {
      message.error('图片上传失败，' + res.data.message)
    }
  } catch (error) {
    message.error('图片上传失败')
  } finally {
    uploadLoading.value = false
  }
}
</script>

<style scoped>
.image-out-painting {
  text-align: center;
}
</style>
