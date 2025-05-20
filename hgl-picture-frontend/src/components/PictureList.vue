<template>
  <div class="picture-list">
    <!--    图片列表-->
    <a-list
      :grid="{ gutter: 16, xs: 1, sm: 2, md: 3, lg: 4, xl: 5, xxl: 6 }"
      :data-source="dataList"
      :loading="loading"
    >
      <template #renderItem="{ item: picture }">
        <a-list-item style="padding: 0">
          <!--          单张图片-->
          <a-card hoverable @click="doClickPicture(picture)">
            <template #cover>
              <img
                :alt="picture.name"
                :src="picture.thumbnailUrl ?? picture.url"
                style="height: 180px; object-fit: cover"
              />
            </template>
            <a-card-meta :title="picture.name">
              <template #description>
                <a-flex>
                  <a-tag color="green">
                    {{ picture.category ?? '默认' }}
                  </a-tag>
                  <a-tag v-for="tag in picture.tags" :key="tag">
                    {{ tag }}
                  </a-tag>
                </a-flex>
              </template>
            </a-card-meta>
            <template #actions v-if="showOpation">
              <a-space @click="e=>doSearch(picture,e)">
                <search-outlined/>
                搜索
              </a-space>
              <a-space @click="e=>doShare(picture,e)">
                <shareAlt-outlined/>
                分享
              </a-space>
              <a-space v-if="canEdit" @click="e=>doEdit(picture,e)">
                <edit-outlined/>
                编辑
              </a-space>
              <a-space v-if="canDelete" @click="e=>doDelete(picture,e)">
                <delete-outlined/>
                删除
              </a-space>
            </template>
          </a-card>
        </a-list-item>
      </template>
    </a-list>
    <ShareModal ref="shareModalRef" :link="shareLink"/>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  DeleteOutlined,
  EditOutlined,
  SearchOutlined,
  ShareAltOutlined,
} from '@ant-design/icons-vue'
import { deletePictureUsingPost } from '@/api/pictureController'
import { message } from 'ant-design-vue'
import ShareModal from '@/components/ShareModal.vue'
import { ref } from 'vue'
//父组件传递给子组件的属性
interface Props {
  dataList?: API.PictureVO[]
  loading?: boolean
  showOpation?: boolean
  onReload?: () => void
  canEdit?: boolean
  canDelete?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  dataList: () => [],
  loading: false,
  showOpation: false,
  onReload: () => {},
  canEdit: false,
  canDelete: false,
})
const router = useRouter()

/**
 * 点击图片跳转到图片详情页
 * @param picture
 */
const doClickPicture = (picture: API.PictureVO) => {
  router.push({
    path: `/picture/${picture.id}`,
    // query: {
    //   id: picture.id,
    // },
  })
}
const doEdit = (picture,e) => {
  // 阻止事件冒泡
  e.stopPropagation()
  router.push({
    path: `/add_picture`,
    query: {
      id: picture.id,
      spaceId: picture.spaceId
    }
  })
}
const doDelete = async (picture,e) => {
  // 阻止事件冒泡
  e.stopPropagation()
  const id = picture.id
  if (!id) {
    return
  }
  const res = await deletePictureUsingPost({ id })
  if (res.data.code === 0 && res.data.data) {
    message.success('删除成功')
    props.onReload?.()
  } else {
    message.error('删除失败')
  }
}

  const doSearch = (picture,e) => {
    // 阻止事件冒泡
    e.stopPropagation()
    //打开新窗口
    window.open(`/search_picture?pictureId=${picture.id}`)
  }
  // 分享
const shareModalRef = ref()
// 分享链接
const shareLink = ref<string>()
  const doShare = (picture,e) => {
    // 阻止事件冒泡
    e.stopPropagation()
    shareLink.value = `${window.location.protocol}//${window.location.host}/picture/${picture.id}`
    if (shareModalRef.value){
      shareModalRef.value.openModal()
    }
  }
</script>

<style scoped></style>
