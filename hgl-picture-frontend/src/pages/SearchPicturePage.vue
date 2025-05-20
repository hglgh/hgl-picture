<template>
  <div id="searchPicturePage">
    <h2 style="margin-bottom: 16px">以图搜图</h2>
    <h3 style="margin-bottom: 16px">原图</h3>
    <a-card hoverable style="width: 240px">
      <template #cover>
        <img
          :alt="picture.name"
          :src="picture.thumbnailUrl ?? picture.url"
          style="height: 180px; object-fit: cover"
        />
      </template>
    </a-card>
    <h3 style="margin: 16px 0">搜索结果</h3>
    <!--    图片结果列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <a :href="picture.imgUrl" target="_blank">
            <!--          单张图片-->
            <a-card hoverable>
              <template #cover>
                <img
                  :alt="picture.title"
                  :src="picture.imgUrl"
                  style="height: 180px; object-fit: cover"
                />
              </template>
            </a-card>
          </a>
        </a-list-item>
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  getPictureVoByIdUsingGet,
  searchPictureByPictureIsSoUsingPost,
} from '@/api/pictureController'
import { useRoute } from 'vue-router'

const route = useRoute()


const loading = ref(true)
const picture = ref<API.PictureVO>({})
const pictureId = computed(() => route.query?.pictureId)
const dataList = ref<API.SoImageSearchResult[]>([])

//获取图片详情
const fetchPictureDetail = async () => {
  try {
    const response = await getPictureVoByIdUsingGet({
      id: pictureId.value,
    })
    if (response.data.code === 0 && response.data.data) {
      picture.value = response.data.data
    } else {
      message.error('获取图片详情失败，' + response.data.message)
    }
  } catch (e: any) {
    message.error('获取图片详情失败，' + e.message)
  }
}
onMounted(() => {
  fetchPictureDetail()
})
//获取搜索结果
const fetchResultData = async () => {
  loading.value = true
  try {
    const response = await searchPictureByPictureIsSoUsingPost({
      pictureId: pictureId.value,
    })
    if (response.data.code === 0 && response.data.data) {
      dataList.value = response.data.data ?? []
    } else {
      message.error('获取数据失败，' + response.data.message)
    }
  } catch (e: any) {
    message.error('获取数据失败，' + e.message)
  }
  loading.value = false
}
onMounted(() => {
  fetchResultData()
})
</script>

<style scoped>
#searchPicturePage {
  margin-bottom: 16px;
}
</style>
