<template>
  <div id="addPictureBatchPage">
    <h2 style="margin-bottom: 16px">批量修改图片</h2>
    <a-form layout="vertical" :model="formData" name="formData" @finish="handleSubmit">
      <a-form-item label="关键词" name="searchText">
        <a-input v-model:value="formData.searchText" placeholder="请输入关键词" allow-clear />
      </a-form-item>

      <a-form-item label="抓取数量" name="count">
        <a-input-number
          v-model:value="formData.count"
          placeholder="输入数量"
          style="min-width: 180px"
          :min="1"
          :max="30"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="名称前缀" name="namePrefix">
        <a-input
          v-model:value="formData.namePrefix"
          placeholder="请输入名称前缀，会自动补充序号"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%" :loading="loading">执行任务</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet, uploadPictureByBatchUsingPost
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'

const formData = ref<API.PictureUploadByBatchRequest>({
  count: 10,
})
//提交任务状态
const loading = ref(false)

const router = useRouter()
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  loading.value = true
  const response = await uploadPictureByBatchUsingPost({
    ...formData.value,
  })
  if (response.data.code === 0 && response.data.data) {
    message.success(`创建成功，共${response.data.data} 条`)
    await router.push({
      path: `/`,
      replace: true,
    })
  } else {
    message.error('创建失败,' + response.data.message)
  }
  loading.value = false
}
</script>
<style scoped>
#addPictureBatchPage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
