<template>
  <div id="mySpacePage">
  </div>
</template>

<script setup lang="ts">

import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { listSpaceVoByPageUsingPost } from '@/api/spaceController'
import { message } from 'ant-design-vue'
import { onMounted } from 'vue'
import { SPACE_TYPE_ENUM } from '@/constants/space'

const router = useRouter()
const loginUserStore = useLoginUserStore()

/**
 * 检查用户是否有个人空间
 */
const checkUserSpace = async () => {
  const loginUser = loginUserStore.loginUser
  if (!loginUser?.id) {
    router.replace('user/login')
    return
  }
  const response = await listSpaceVoByPageUsingPost({
    current: 1,
    pageSize: 1,
    userId: loginUser.id,
    spaceType: SPACE_TYPE_ENUM.PRIVATE
  })
  if (response.data.code === 0) {
    if (response.data.data?.records?.length > 0){
      const space = response.data.data.records[0]
      //跳转到空间页面
      router.replace(`/space/${space.id}`)
    }else {
      //创建个人空间
      router.replace('/add_space')
      message.warn('您还没有个人空间，请先创建一个')
    }
  }else {
    message.error('获取空间失败,' + response.data.message)
  }
}
onMounted(()=>{
  checkUserSpace()
})
</script>
<style scoped>
</style>
