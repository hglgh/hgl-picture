<template>
  <div id="addSpacePage">
    <h2 style="margin-bottom: 16px">
      {{ route.query?.id ? '修改' : '创建' }} {{SPACE_TYPE_MAP[spaceType]}}
    </h2>
    <a-form layout="vertical" :model="spaceForm" name="spaceForm" @finish="handleSubmit">
      <a-form-item label="空间名称" name="spaceName">
        <a-input v-model:value="spaceForm.spaceName" placeholder="请输入空间名称" allow-clear />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="spaceForm.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="请选择空间级别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit" :loading="loading" style="width: 100%"
          >提交
        </a-button>
      </a-form-item>
    </a-form>
    <!--    空间级别介绍-->
    <a-card title="空间级别介绍">
      <a-typography-paragraph>
        * 目前仅支持普通版，如需升级专业版和旗舰版空间，请联系
        <a href="https://codefather.cn" target="_blank">小扈先生</a>
      </a-typography-paragraph>
      <a-typography-paragraph v-for="spaceLevel in spaceLevelList">
        {{ spaceLevel.text }}: 大小{{ formatSize(spaceLevel.maxSize) }}, 数量{{
          spaceLevel.maxCount
        }}
      </a-typography-paragraph>
    </a-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import {
  addSpaceUsingPost,
  getSpaceVoByIdUsingGet,
  listSpaceLevelUsingGet,
  updateSpaceUsingPost,
} from '@/api/spaceController'
import { message } from 'ant-design-vue'
import { useRoute, useRouter } from 'vue-router'
import { SPACE_LEVEL_OPTIONS, SPACE_TYPE_ENUM, SPACE_TYPE_MAP } from '@/constants/space'
import { formatSize } from '@/utils'

const loading = ref(false)
const space = ref<API.SpaceVO>()
const spaceLevelList = ref<API.SpaceLevel[]>([])
const spaceForm = ref<API.SpaceAddRequest | API.SpaceEditRequest>({})
const router = useRouter()

const route = useRoute()
//空间类型 默认为私有空间
const spaceType = computed(() => {
  if (route.query?.type) {
    return Number(route.query?.type)
  } else {
    return SPACE_TYPE_ENUM.PRIVATE
  }
})

const fetchSpaceLevelList = async () => {
  const response = await listSpaceLevelUsingGet()
  if (response.data.code === 0 && response.data.data) {
    spaceLevelList.value = response.data.data
  } else {
    message.error('获取空间级别失败,' + response.data.message)
  }
}
onMounted(() => {
  fetchSpaceLevelList()
})
/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  const spaceId = space.value?.id
  loading.value = true
  let response
  if (spaceId) {
    //更新
    response = await updateSpaceUsingPost({
      id: spaceId,
      ...values,
    })
  } else {
    //新增
    response = await addSpaceUsingPost({
      ...values,
      spaceType: spaceType.value,
    })
  }
  if (response.data.code === 0 && response.data.data) {
    message.success('操作成功')
    await router.push({
      path: `/space/${spaceId}`,
      replace: true,
    })
  } else {
    message.error('操作失败,' + response.data.message)
  }
  loading.value = false
}

//获取老数据
const getOldSpace = async () => {
  //获取Id
  const id = route.query?.id
  if (id) {
    const response = await getSpaceVoByIdUsingGet({
      id,
    })
    if (response.data.code === 0 && response.data.data) {
      const data = response.data.data
      space.value = data
      spaceForm.value = {
        spaceName: data.spaceName,
        spaceLevel: data.spaceLevel,
      }
    }
  }
}
onMounted(() => {
  getOldSpace()
})
</script>
<style scoped>
#addSpacePage {
  max-width: 720px;
  margin: 0 auto;
}
</style>
