<template>
  <div id="batchEditPictureModal">
    <a-modal v-model:visible="visible" title="批量编辑图片" :footer="false" @cancel="closeModal">
      <a-typography-paragraph type="secondary">* 仅对当前页面的图片进行批量编辑</a-typography-paragraph>
      <!--    搜索表单-->
      <a-form
        :model="formData"
        layout="vertical"
        autocomplete="off"
        @finish="handleSubmit"
      >
        <a-form-item label="分类">
          <a-auto-complete
            v-model:value="formData.category"
            style="min-width: 180px"
            :options="categoryOptions"
            placeholder="请输入分类"
            allowClear
          />
        </a-form-item>
        <a-form-item label="标签">
          <a-select
            v-model:value="formData.tags"
            mode="tags"
            placeholder="请输入标签"
            :options="tagOptions"
            style="min-width: 180px"
            allow-clear
          />
        </a-form-item>
        <a-form-item label="命名规则" name="nameRule">
          <a-input v-model:value="formData.nameRule" placeholder="请输入命名规则，输入 {序号} 可动态生成" />
        </a-form-item>
        <a-form-item>
          <a-space>
            <a-button type="primary" html-type="submit">操作</a-button>
            <a-button html-type="reset" @click="doClear">重置</a-button>
          </a-space>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>
<script setup lang="ts">
import { defineExpose, onMounted, reactive, ref } from 'vue'
import { editPicutreByBatchUsingPost, listPictureTagCategoryUsingGet } from '@/api/pictureController'
import { message } from 'ant-design-vue'

// 定义组件属性类型
interface Props {
  pictureList: API.PictureVO[]
  spaceId: number
  onSuccess: () => void
}

// 给组件指定初始值
const props = withDefaults(defineProps<Props>(), {})

const formData = reactive<API.PictureEditByBatchRequest>({})
const tagOptions = ref<{ value: string; label: string }[]>([])
const categoryOptions = ref<{ value: string; label: string }[]>([])


const handleSubmit = async () => {
  if (!props.pictureList){
    message.error('当前页面没有图片')
    return
  }
  formData.pictureIdList = props.pictureList.map(data => data.id)
  formData.spaceId = props.spaceId
  const res = await editPicutreByBatchUsingPost(formData)
  if (res.data.code === 0 && res.data.data) {
    message.success('操作成功')
    closeModal()
    props.onSuccess?.()
  }else {
    message.error('操作失败，' + res.data.message)
  }
}

// 获取标签和分类选项
const getTagCategoryOptions = async () => {
  const res = await listPictureTagCategoryUsingGet()
  if (res.data.code === 0 && res.data.data) {
    // 转换成下拉选项组件接受的格式
    tagOptions.value = (res.data.data.tagList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
    categoryOptions.value = (res.data.data.categoryList ?? []).map((data: string) => {
      return {
        value: data,
        label: data,
      }
    })
  } else {
    message.error('加载选项失败，' + res.data.message)
  }
}

onMounted(() => {
  getTagCategoryOptions()
})

const doClear = () => {
  Object.keys(formData).forEach((key) => {
    formData[key] = undefined
  })
}

const visible = ref(false)
// 打开模态框
const openModal = () => {
  visible.value = true
}
// 关闭模态框
const closeModal = () => {
  visible.value = false
}
// 暴露方法给父组件调用
defineExpose({
  openModal,
})
</script>

<style scoped></style>
