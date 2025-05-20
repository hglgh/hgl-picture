<template>
  <div id="spaceDetailPage">
    <a-flex justify="space-between">
      <h2>{{ space.spaceName }}({{ SPACE_TYPE_MAP[space.spaceType] }})</h2>
      <a-space size="middle">
        <a-button
          v-if="canUploadPicture"
          type="primary"
          :href="`/add_picture?spaceId=${id}`"
          target="_blank"
          >+ 创建图片
        </a-button>
        <a-button
          v-if="canManageSpaceUser"
          :icon="h(TeamOutlined)"
          type="primary"
          ghost
          :href="`/spaceUserManage/${id}`"
          target="_blank"
        >
          成员管理
        </a-button>
        <a-button
          v-if="canManageSpaceUser"
          :icon="h(BarChartOutlined)"
          type="primary"
          ghost
          :href="`/space_analyze?spaceId=${id}`"
          target="_blank"
        >
          空间分析
        </a-button>
        <a-button v-if="canEditPicture" :icon="h(EditOutlined)" @click="doBatchEdit">
          批量编辑</a-button
        >

        <a-tooltip :title="`占用空间${formatSize(space.totalSize)}/${formatSize(space.maxSize)}`">
          <a-progress
            type="circle"
            :percent="((space.totalSize * 100) / space.maxSize).toFixed(1)"
            :size="42"
          />
        </a-tooltip>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <PictureSearchForm :on-search="doSearch" />
    <BatchEditPictureModal
      ref="batchEditPictureModalRef"
      :picture-list="dataList"
      :space-id="id"
      :on-success="onBatchEditPictureSuccess"
    />
    <!-- 按颜色搜索 -->
    <a-form-item label="按颜色搜索" style="margin-top: 16px">
      <color-picker format="hex" @pureColorChange="onColorChange" />
    </a-form-item>
    <div style="margin-bottom: 16px" />
    <!--    图片列表-->
    <PictureList
      :loading="loading"
      :dataList="dataList"
      :show-opation="true"
      :on-reload="fetchData"
      :can-edit="canEditPicture"
      :can-delete="canDeletePicture"
    />
    <a-pagination
      v-model:current="queryParams.current"
      v-model:page-size="queryParams.pageSize"
      :total="total"
      style="text-align: right"
      @change="onPageChange"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref, h, watch, computed } from 'vue'
import { message } from 'ant-design-vue'
import { getSpaceVoByIdUsingGet } from '@/api/spaceController'
import { useRouter } from 'vue-router'
import {
  listPictureVoByPageUsingPost,
  searchPictureByColorUsingPost,
} from '@/api/pictureController'
import { formatSize } from '@/utils'
import PictureList from '@/components/PictureList.vue'
import PictureSearchForm from '@/components/PictureSearchForm.vue'
import 'vue3-colorpicker/style.css'
import { ColorPicker } from 'vue3-colorpicker'
import BatchEditPictureModal from '@/components/BatchEditPictureModal.vue'
import { EditOutlined, BarChartOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { SPACE_PERMISSION_ENUM, SPACE_TYPE_MAP } from '@/constants/space'

interface Props {
  id: string | number
}

const props = defineProps<Props>()
const router = useRouter()

const space = ref<API.SpaceVO>({})

//定义权限校验检查
// 通用权限检查函数
function createPermissionChecker(permission: string) {
  return computed(() => {
    return (space.value.permissionList ?? []).includes(permission)
  })
}

// 定义权限检查
const canManageSpaceUser = createPermissionChecker(SPACE_PERMISSION_ENUM.SPACE_USER_MANAGE)
const canUploadPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_UPLOAD)
const canEditPicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_EDIT)
const canDeletePicture = createPermissionChecker(SPACE_PERMISSION_ENUM.PICTURE_DELETE)

//--------------------获取空间详情--------------------------
const fetchSpaceDetail = async () => {
  try {
    const response = await getSpaceVoByIdUsingGet({
      id: props.id,
    })
    if (response.data.code === 0 && response.data.data) {
      space.value = response.data.data
    } else {
      message.error('获取空间详情失败，' + response.data.message)
    }
  } catch (e: any) {
    message.error('获取空间详情失败，' + e.message)
  }
}
onMounted(() => {
  fetchSpaceDetail()
})

//--------------------获取图片列表--------------------------
const loading = ref(true)
//定义数据
const dataList = ref<API.PictureVO[]>([])
const total = ref<number>(0)
//搜索条件
const queryParams = ref<API.PictureQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})
//分页参数
const onPageChange = (page: number, pageSize: number) => {
  queryParams.value.current = page
  queryParams.value.pageSize = pageSize
  fetchData()
}

//获取数据
const fetchData = async () => {
  loading.value = true
  //转换搜索参数
  const params = {
    spaceId: props.id,
    ...queryParams.value,
  }
  const res = await listPictureVoByPageUsingPost(params)
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败')
  }
  loading.value = false
}

//页面加载时请求一次获取数据
onMounted(() => {
  fetchData()
})
const doSearch = (newQueryParams: API.PictureQueryRequest) => {
  queryParams.value = {
    ...queryParams.value,
    ...newQueryParams,
    current: 1,
  }
  fetchData()
}
// 按颜色搜索
const onColorChange = async (color: string) => {
  loading.value = true
  const respone = await searchPictureByColorUsingPost({
    picColor: color,
    spaceId: props.id,
  })
  if (respone.data.code === 0 && respone.data.data) {
    const data = respone.data.data ?? []
    dataList.value = data
    total.value = data.length
  } else {
    message.error('获取数据失败,' + respone.data.message)
  }
  loading.value = false
}

// 分享弹窗引用
const batchEditPictureModalRef = ref()

// 批量编辑成功后，刷新数据
const onBatchEditPictureSuccess = () => {
  fetchData()
}

// 打开批量编辑弹窗
const doBatchEdit = () => {
  if (batchEditPictureModalRef.value) {
    batchEditPictureModalRef.value.openModal()
  }
}

//空间Id变化时，重新获取数据
watch(
  () => props.id,
  () => {
    fetchData()
    fetchSpaceDetail()
  },
)
</script>

<style scoped>
#spaceDetailPage {
  margin-bottom: 16px;
}
</style>
