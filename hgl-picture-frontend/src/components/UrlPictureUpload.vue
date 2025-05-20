<template>
  <div class="url-picture-upload">
    <a-input-group compact>
      <a-input v-model:value="fileUrl" style="width: calc(100% - 120px)" placeholder="请输入图片地址" />
      <a-button type="primary" style="width: 120px" :loading="loading" @click="handleUpload">提交</a-button>
    </a-input-group>
    <div class="img-wrapper">
      <img v-if="picture?.url" :src="picture?.url" alt="avatar" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { PlusOutlined, LoadingOutlined } from '@ant-design/icons-vue'
import { message } from 'ant-design-vue'
import type { UploadChangeParam, UploadProps } from 'ant-design-vue'
import { uploadPictureByUrlUsingPost, uploadPictureUsingPost } from '@/api/pictureController'

interface Props {
  picture?: API.PictureVO
  spaceId?: number
  onSuccess: (newPicture: API.PictureVO) => void
}

// 定义props
const props = defineProps<Props>()

const fileUrl = ref<string>()

/**
 * 上传图片
 * @param file
 */
const handleUpload = async () => {
  loading.value = true
  try {
    const params: API.PictureUploadRequest = {fileUrl: fileUrl.value}
    params.spaceId = props.spaceId
    if (props.picture){
      params.id = props.picture.id
    }
    const response = await uploadPictureByUrlUsingPost(params)
    if (response.data.code === 0 && response.data.data) {
      message.success('图片上传成功')
      //将上传成功的图片信息返回给父组件
      props.onSuccess?.(response.data.data)
    } else {
      message.error('图片上传失败' + response.data.message)
    }
  } catch (e: any) {
    message.error('图片上传失败' + e.message)
  }
  loading.value = false
}

const loading = ref<boolean>(false)

</script>

<style scoped>
.url-picture-upload {
margin-bottom: 16px;
}
.url-picture-upload img {
  max-width: 100%;
  max-height: 480px;
}
.url-picture-upload .img-wrapper {
  text-align: center;
  margin-top: 16px;
}
</style>
