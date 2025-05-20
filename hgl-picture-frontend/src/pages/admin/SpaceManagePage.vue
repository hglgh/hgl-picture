<template>
  <div id="spaceManagePage">
    <a-flex justify="space-between">
      <h2>空间管理</h2>
      <a-space>
        <a-button type="primary" href="/add_space" target="_blank">+ 创建空间</a-button>
        <a-button type="primary" ghost href="/space_analyze?queryPublic=1" target="_blank">分析公共图库</a-button>
        <a-button type="primary" ghost href="/space_analyze?queryAll=1" target="_blank">分享全部空间</a-button>
      </a-space>
    </a-flex>
    <div style="margin-bottom: 16px" />
    <!--    搜索表单-->
    <a-form
      :model="queryParams"
      name="horizontal_login"
      layout="inline"
      autocomplete="off"
      @finish="doQuery"
    >
      <a-form-item label="空间名称">
        <a-input
          v-model:value="queryParams.spaceName"
          placeholder="请输入空间名称"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="用户Id">
        <a-input
          v-model:value="queryParams.userId"
          placeholder="请输入用户Id"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="空间级别" name="spaceLevel">
        <a-select
          v-model:value="queryParams.spaceLevel"
          :options="SPACE_LEVEL_OPTIONS"
          placeholder="请选择空间级别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item label="空间类别" name="spaceType">
        <a-select
          v-model:value="queryParams.spaceType"
          :options="SPACE_TYPE_OPTIONS"
          placeholder="请选择空间类别"
          style="min-width: 180px"
          allow-clear
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" html-type="submit">搜索</a-button>
      </a-form-item>
    </a-form>
    <div style="margin-bottom: 16px" />
    <!--    表格-->
    <a-table
      :columns="columns"
      :data-source="dataList"
      :pagination="pagination"
      @change="doTableChange"
      :scroll="{ x: 'max-content' }"
    >
      <template #bodyCell="{ column, record }">
        <!--        审核信息-->
        <template v-if="column.dataIndex === 'spaceLevel'">
          <div>{{ SPACE_LEVEL_MAP[record.spaceLevel] }}</div>
        </template>
        <!--        空间类别-->
        <template v-if="column.dataIndex === 'spaceType'">
          <a-tag>{{ SPACE_TYPE_MAP[record.spaceType] }}</a-tag>
        </template>
        <!--        空间信息-->
        <template v-if="column.dataIndex === 'spaceUseInfo'">
          <div>大小：{{formatSize(record.totalSize)}} / {{formatSize(record.maxSize)}}</div>
          <div>数量：{{ record.totalCount }} / {{record.maxCount}}</div>
        </template>
        <template v-else-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.dataIndex === 'editTime'">
          {{ dayjs(record.editTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-space wrap>
            <a-button type="link" :href="`/space_analyze?spaceId=${record.id}`" target="_blank"
              >分析
            </a-button>
            <a-button type="link" :href="`/add_space?id=${record.id}`" target="_blank"
            >编辑
            </a-button>
            <a-button danger @click="doDelete(record.id)">删除</a-button>
          </a-space>
        </template>
      </template>
    </a-table>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
import { listSpaceByPageUsingPost } from '@/api/spaceController'
import { SPACE_LEVEL_MAP, SPACE_LEVEL_OPTIONS, SPACE_TYPE_MAP, SPACE_TYPE_OPTIONS } from '@/constants/space'
import { formatSize } from '@/utils'


const columns = [
  {
    title: 'id',
    dataIndex: 'id',
    width: 80,
  },
  {
    title: '空间名称',
    dataIndex: 'spaceName',
  },
  {
    title: '空间级别',
    dataIndex: 'spaceLevel',
  },
  {
    title: '空间类别',
    dataIndex: 'spaceType',
  },
  {
    title: '使用情况',
    dataIndex: 'spaceUseInfo',
  },
  {
    title: '用户 id',
    dataIndex: 'userId',
    width: 80,
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '编辑时间',
    dataIndex: 'editTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
//搜索条件
const queryParams = reactive<API.SpaceQueryRequest>({
  current: 1,
  pageSize: 10,
  sortField: 'createTime',
  sortOrder: 'descend',
})

//定义数据
const dataList = ref<API.Space[]>([])
const total = ref<number>(0)

//分页参数
const pagination = computed(() => {
  return {
    current: queryParams.current,
    pageSize: queryParams.pageSize,
    total: total.value,
    showSizeChanger: true,
    showTotal: (total: number) => `共 ${total} 条数据`,
  }
})
//====================================function Start====================================

//获取数据
const fetchData = async () => {
  const res = await listSpaceByPageUsingPost({ ...queryParams })
  if (res.data.code === 0 && res.data.data) {
    dataList.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    message.error('获取数据失败')
  }
}

const doTableChange = (page: any) => {
  queryParams.current = page.current
  queryParams.pageSize = page.pageSize
  fetchData()
}
//页面加载时请求一次获取数据
onMounted(() => {
  fetchData()
})

/**
 * 搜索空间
 */
const doQuery = () => {
  //重置页码
  queryParams.current = 1
  fetchData()
}

const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteSpaceUsingPost({ id })
  if (res.data.code === 0 && res.data.data) {
    message.success('删除成功')
    await fetchData()
  } else {
    message.error('删除失败')
  }
}
//====================================function End====================================
</script>
