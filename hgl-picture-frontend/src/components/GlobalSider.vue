<template>
  <div id="globalSider">
    <a-layout-sider
      width="200"
      breakpoint="lg"
      collapsed-width="0"
      v-if="loginUserStroe.loginUser.id"
    >
      <a-menu v-model:selectedKeys="current" mode="inline" :items="menuItems" @click="doMenuClick">
      </a-menu>
    </a-layout-sider>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref, watchEffect } from 'vue'
import { PictureOutlined, UserOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { SPACE_TYPE_ENUM } from '@/constants/space'
import { getSpaceUserUsingPost, listMyTeamSpaceUsingPost } from '@/api/spaceUserController'
import { message } from 'ant-design-vue'

const current = ref<string[]>(['/'])

const loginUserStroe = useLoginUserStore()
//固定的菜单列表
const fixedMenuItems = [
  {
    key: '/',
    icon: h(PictureOutlined),
    label: '公共图库',
  },
  {
    key: '/my_space',
    icon: h(UserOutlined),
    label: '我的空间',
  },
  {
    key: '/add_space?type=' + SPACE_TYPE_ENUM.TEAM,
    icon: h(TeamOutlined),
    label: '创建团队',
  },
]

const teamSpaceList = ref<API.SpaceUserVO[]>([])

const menuItems = computed(() => {
  //如果用户没有团队空间，则直接返回固定菜单列表
  if (teamSpaceList.value.length < 1) {
    return fixedMenuItems
  } else {
    // 展示团队空间分组
    const teamSpaceSubMenus = teamSpaceList.value.map((spaceUser) => {
      const space = spaceUser.space
      return {
        key: '/space/' + spaceUser.spaceId,
        label: space?.spaceName,
      }
    })
    const teamSpaceMenuGroup = {
      type: 'group',
      label: '我的团队',
      key: 'teamSpace',
      children: teamSpaceSubMenus,
    }
    return [...fixedMenuItems, teamSpaceMenuGroup]
  }
})

const router = useRouter()
//路由跳转事件
const doMenuClick = ({ key }) => {
  router.push(key)
}
//路由监听(路由跳转后更新菜单选中状态——当前要高亮的菜单项)
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

// 加载团队空间列表
const fetchTeamSpaceList = async () => {
  const response = await listMyTeamSpaceUsingPost({})
  if (response.data.code === 0 && response.data.data) {
    teamSpaceList.value = response.data.data
  }else {
    message.error('加载我的团队空间失败,' + response.data.message)
  }
}

/**
 * 监听变量，改变时触发数据的重新加载
 */
watchEffect(() => {
  // 登录才加载
  if (loginUserStroe.loginUser.id) {
    fetchTeamSpaceList()
  }
})
</script>

<style scoped>
#globalSider .ant-layout-sider {
  background: none;
}
</style>
