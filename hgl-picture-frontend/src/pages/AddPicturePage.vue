<template>
  <div id="addPicturePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改图片' : '创建图片' }}
    </h2>
    <a-typography-paragraph v-if="spaceId" type="secondary">
      保存到空间：<a :href="`/space/${spaceId}`" target="_blank">{{ spaceId }}</a>
    </a-typography-paragraph>
    <!--    选择上传方式-->
    <a-tabs v-model:activeKey="uploadType">
      <a-tab-pane key="file" tab="文件上传">
        <!--    图片上传组件-->
        <PictureUpload :picture="picture" :space-id="spaceId" :on-success="onSuccess" />
      </a-tab-pane>
      <a-tab-pane key="url" tab="URL上传" force-render>
        <!--    URL图片上传组件-->
        <UrlPictureUpload :picture="picture" :space-id="spaceId" :on-success="onSuccess" />
        <!--    图片信息表单-->
      </a-tab-pane>
    </a-tabs>
    <a-form
      v-if="picture"
      layout="vertical"
      :model="pictureForm"
      name="pictureForm"
      @finish="handleSubmit"
    >
      <div v-if="picture" class="edit-bar">
        <a-space size="middle">
          <a-button :icon="h(EditOutlined)" @click="doEditPicture">编辑图片</a-button>
          <a-button type="primary" ghost :icon="h(FullscreenOutlined)" @click="doImagePainting"
            >AI扩图</a-button
          >
        </a-space>
        <ImageCropper
          ref="imageCropperRef"
          :imageUrl="picture?.url"
          :picture="picture"
          :spaceId="spaceId"
          :space="space"
          :onSuccess="onCropSuccess"
        />
        <ImageOutPainting
          ref="imageOutPaintingRef"
          :picture="picture"
          :spaceId="spaceId"
          :onSuccess="onImageOutPaintingSuccess"
        />
      </div>

      <a-form-item label="名称" name="name">
        <a-input v-model:value="pictureForm.name" placeholder="请输入图片名称" allow-clear />
      </a-form-item>

      <a-form-item label="简介" name="introduction">
        <a-textarea
          v-model:value="pictureForm.introduction"
          placeholder="输入简介"
          :auto-size="{ minRows: 1, maxRows: 5 }"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="分类" name="category">
        <a-auto-complete
          v-model:value="pictureForm.category"
          :options="categoryOptions"
          placeholder="请输入图片分类"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="标签" name="tags">
        <a-select
          v-model:value="pictureForm.tags"
          mode="tags"
          :options="tagOptions"
          placeholder="请输入图片标签"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">创建</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>

<script setup lang="ts">
import PictureUpload from '@/components/PictureUpload.vue'
import { computed, h, onMounted, reactive, ref, watchEffect } from 'vue'
import {
  editPictureUsingPost,
  getPictureVoByIdUsingGet,
  listPictureTagCategoryUsingGet,
} from '@/api/pictureController'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import UrlPictureUpload from '@/components/UrlPictureUpload.vue'
import { EditOutlined, FullscreenOutlined } from '@ant-design/icons-vue'
import ImageCropper from '@/components/ImageCropper.vue'
import ImageOutPainting from '@/components/ImageOutPainting.vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController'

const route = useRoute()
const spaceId = computed(() => route.query?.spaceId)
const imageCropperRef = ref()
const picture = ref<API.PictureVO>()
const onSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
  pictureForm.value = {
    id: newPicture.id,
    name: newPicture.name,
    introduction: newPicture.introduction,
    category: newPicture.category,
    tags: newPicture.tags,
  }
}
const pictureForm = ref<API.PictureEditRequest>({})
const router = useRouter()
const uploadType = ref<'file' | 'url'>('file')
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const pictureId = picture.value?.id
  if (!pictureId) {
    return
  }
  const response = await editPictureUsingPost({
    id: pictureId,
    spaceId: spaceId.value,
    ...values,
  })
  if (response.data.code === 0 && response.data.data) {
    message.success('创建成功')
    router.push({
      path: `/picture/${pictureId}`,
      replace: true,
    })
  } else {
    message.error('创建失败,' + response.data.message)
  }
}
const categoryOptions = ref<string[]>([])
const tagOptions = ref<string[]>([])
/**
 * 获取标签和分类选项
 */
const getTagCategoryOpations = async () => {
  const response = await listPictureTagCategoryUsingGet()
  if (response.data.code === 0 && response.data.data) {
    tagOptions.value = (response.data.data.tagList ?? []).map((tag: string) => {
      return {
        value: tag,
        label: tag,
      }
    })
    categoryOptions.value = (response.data.data.categoryList ?? []).map((category: string) => {
      return {
        value: category,
        label: category,
      }
    })
  } else {
    message.error('获取标签和分类选项失败,' + response.data.message)
  }
}
onMounted(() => {
  getTagCategoryOpations()
})

//获取老数据
const getOldPicture = async () => {
  //获取Id
  const id = route.query?.id
  if (id) {
    const response = await getPictureVoByIdUsingGet({
      id,
    })
    if (response.data.code === 0 && response.data.data) {
      const data = response.data.data
      picture.value = data
      pictureForm.value = {
        name: data.name,
        introduction: data.introduction,
        category: data.category,
        tags: data.tags,
      }
    }
  }
}
onMounted(() => {
  getOldPicture()
})

// 编辑图片
const doEditPicture = () => {
  if (imageCropperRef.value) {
    imageCropperRef.value.openModal()
  }
}
// 编辑成功事件
const onCropSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

const imageOutPaintingRef = ref()
const doImagePainting = () => {
  if (imageOutPaintingRef.value) {
    imageOutPaintingRef.value.openModal()
  }
}
// AI扩图成功事件
const onImageOutPaintingSuccess = (newPicture: API.PictureVO) => {
  picture.value = newPicture
}

const space = ref<API.SpaceVO>()

// 获取空间信息
const fetchSpace = async () => {
  // 获取数据
  if (spaceId.value) {
    const res = await getSpaceVoByIdUsingGet({
      id: spaceId.value,
    })
    if (res.data.code === 0 && res.data.data) {
      space.value = res.data.data
    }
  }
}

watchEffect(() => {
  fetchSpace()
})
</script>
<style scoped>
#addPicturePage {
  max-width: 720px;
  margin: 0 auto;
}

#addPicturePage .edit-bar {
  text-align: center;
  margin: 16px 0;
}
</style>
