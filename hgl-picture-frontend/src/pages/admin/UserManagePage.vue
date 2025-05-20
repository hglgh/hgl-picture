<template>
  <div id="userManagePage">
    <!--    搜索表单-->
    <a-form
      :model="queryParams"
      name="horizontal_login"
      layout="inline"
      autocomplete="off"
      @finish="doQuery"
    >
      <a-form-item label="账号">
        <a-input v-model:value="queryParams.userAccount" placeholder="输入账号" allow-clear />
      </a-form-item>

      <a-form-item label="用户名">
        <a-input v-model:value="queryParams.userName" placeholder="输入用户名" allow-clear />
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
    >
      <template #bodyCell="{ column, record }">
        <template v-if="column.dataIndex === 'userAvatar'">
          <a-image :src="record.userAvatar" width="120px" :alt="record.userName" />
        </template>
        <template v-else-if="column.dataIndex === 'userRole'">
          <div v-if="record.userRole === 'admin'">
            <a-tag color="green">管理员</a-tag>
          </div>
          <div v-else>
            <a-tag color="blue">普通用户</a-tag>
          </div>
        </template>
        <template v-if="column.dataIndex === 'createTime'">
          {{ dayjs(record.createTime).format('YYYY-MM-DD HH:mm:ss') }}
        </template>
        <template v-else-if="column.key === 'action'">
          <a-button danger @click="doDelete(record.id)">删除</a-button>
        </template>
      </template>
    </a-table>
  </div>
</template>
<script lang="ts" setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { deleteUserUsingDelete, listUserByPageUsingPost } from '@/api/yonghujiekou'
import { message } from 'ant-design-vue'
import dayjs from 'dayjs'
const columns = [
  {
    title: 'id',
    dataIndex: 'id',
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
  },
  {
    title: '用户名',
    dataIndex: 'userName',
  },
  {
    title: '头像',
    dataIndex: 'userAvatar',
  },
  {
    title: '简介',
    dataIndex: 'userProfile',
  },
  {
    title: '用户角色',
    dataIndex: 'userRole',
  },
  {
    title: '创建时间',
    dataIndex: 'createTime',
  },
  {
    title: '操作',
    key: 'action',
  },
]
//搜索条件
const queryParams = reactive<API.UserQueryRequest>({
  current: 1,
  pageSize: 2,
})

//定义数据
const dataList = ref<API.UserVO[]>([])
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
  const res = await listUserByPageUsingPost({ ...queryParams })
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
 * 搜索用户
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
  const res = await deleteUserUsingDelete({ id })
  if (res.data.code === 0 && res.data.data) {
    message.success('删除成功')
    await fetchData()
  } else {
    message.error('删除失败')
  }
}
//====================================function End====================================
</script>
