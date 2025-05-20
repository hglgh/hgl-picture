<template>
  <div class="picture-search-form">
    <!--    搜索表单-->
    <a-form
      :model="queryParams"
      name="horizontal_login"
      layout="inline"
      autocomplete="off"
      @finish="doQuery"
    >
      <a-form-item label="关键词">
        <a-input
          v-model:value="queryParams.searchText"
          placeholder="从名称和简介中搜索"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="分类">
        <a-auto-complete
          v-model:value="queryParams.category"
          style="min-width: 180px"
          :options="categoryOptions"
          placeholder="请输入分类"
          allowClear
        />
      </a-form-item>
      <a-form-item label="标签">
        <a-select
          v-model:value="queryParams.tags"
          mode="tags"
          placeholder="请输入标签"
          :options="tagOptions"
          style="width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="日期" name="dateRange">
        <a-range-picker
          style="width: 400px"
          show-time
          v-model="dateRange"
          :placeholder="['编辑开始日期', '编辑结束时间']"
          format="YYYY/MM/DD HH:mm:ss"
          :presets="rangePresets"
          @change="onRangeChange"
        />
      </a-form-item>
      <a-form-item label="名称" name="name">
        <a-input v-model:value="queryParams.name" placeholder="请输入名称" allow-clear />
      </a-form-item>
      <a-form-item label="简介" name="introduction">
        <a-input v-model:value="queryParams.introduction" placeholder="请输入简介" allow-clear />
      </a-form-item>
      <a-form-item label="宽度" name="picWidth">
        <a-input-number v-model:value="queryParams.picWidth" min="0" />
      </a-form-item>
      <a-form-item label="高度" name="picHeight">
        <a-input-number v-model:value="queryParams.picHeight" min="0" />
      </a-form-item>
      <a-form-item label="格式">
        <!--        <a-input v-model:value="queryParams.picFormat" placeholder="请输入格式" allow-clear />-->
        <a-auto-complete
          v-model:value="queryParams.picFormat"
          style="min-width: 180px"
          :options="picFormatOptions"
          placeholder="请输入格式"
          allowClear
        />
      </a-form-item>

      <a-form-item>
        <a-space>
          <a-button type="primary" html-type="submit">搜索</a-button>
          <a-button html-type="reset" @click="doClear">重置</a-button>
        </a-space>
      </a-form-item>
    </a-form>

    <div style="margin-bottom: 16px" />
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { listPictureTagCategoryUsingGet, searchPictureByColorUsingPost } from '@/api/pictureController'
import { ColorPicker } from 'vue3-colorpicker'

//====================================argument Start====================================
interface Props {
  onSearch?: (queryParams: API.PictureQueryRequest) => void
}

const props = defineProps<Props>()

const categoryOptions = ref<any[]>([])
const tagOptions = ref<any[]>([])
const picFormatOptions = ref<any[]>([
  {
    value: 'jpg',
    label: 'jpg',
  },
  {
    value: 'jpeg',
    label: 'jpeg',
  },
  {
    value: 'png',
    label: 'png',
  },
  {
    value: 'gif',
    label: 'gif',
  },
  {
    value: 'webp',
    label: 'webp',
  },
])
const dateRange = ref<any>([])
const rangePresets = ref([
  { label: '过去 7 天', value: [dayjs().add(-7, 'd'), dayjs()] },
  { label: '过去 14 天', value: [dayjs().add(-14, 'd'), dayjs()] },
  { label: '过去 30 天', value: [dayjs().add(-30, 'd'), dayjs()] },
  { label: '过去 90 天', value: [dayjs().add(-90, 'd'), dayjs()] },
])
//搜索条件
const queryParams = reactive<API.PictureQueryRequest>({})

//====================================argument End====================================

//====================================function Start====================================

/**
 * 搜索图片
 */
const doQuery = () => {
  props.onSearch?.(queryParams)
}
/**
 * 日期范围选择器变化
 * @param dates
 * @param dateString
 */
const onRangeChange = (dates: any[], dateString: string[]) => {
  if (dates?.length >= 2) {
    queryParams.startEditTime = dates[0].toDate()
    queryParams.endEditTime = dates[1].toDate()
  } else {
    queryParams.startEditTime = undefined
    queryParams.endEditTime = undefined
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
  dateRange.value = []
  Object.keys(queryParams).forEach((key) => {
    queryParams[key] = undefined
  })
  doQuery()
}

//====================================function End====================================
</script>
<style scoped>
.picture-search-form .ant-form-item {
  margin-top: 16px;
}
</style>
