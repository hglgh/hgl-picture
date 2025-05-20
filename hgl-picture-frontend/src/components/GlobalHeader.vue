<template>
  <div id="globalHeader">
    <a-row :wrap="false">
      <a-col flex="200px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.png" alt="logo" />
            <div class="title">小扈图库</div>
          </div>
        </router-link>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <!--用户信息展示栏-->
      <a-col flex="150px">
        <div class="user-login-status">
          <div v-if="loginUserStroe.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStroe.loginUser.userAvatar" />
                {{ loginUserStroe.loginUser.userName ?? '无名' }}
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item>
                    <router-link to="/my_space">
                      <UserOutlined/>
                      我的空间
                    </router-link>
                  </a-menu-item>
                  <a-menu-item @click="doLoginout">
                    <LoginOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { computed, h, ref } from 'vue'
import {
  HomeOutlined,
  LoginOutlined,
  UserOutlined
} from '@ant-design/icons-vue'
import { MenuProps, message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { userLogoutUsingPost } from '@/api/yonghujiekou'

const loginUserStroe = useLoginUserStore()
const current = ref<string[]>(['/'])

//未经过滤的菜单项
const originItems = [
  {
    key: '/',
    icon: h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/add_picture',
    label: '创建图片',
    title: '创建图片',
  },
  {
    key: '/admin/pictureManage',
    label: '图片管理',
    title: '图片管理',
  },
  {
    key: '/admin/spaceManage',
    label: '空间管理',
    title: '空间管理',
  },
  {
    key: 'others',
    label: h('a', { href: 'https://www.codefather.cn', target: '_blank' }, '编程导航'),
    title: '编程导航',
  },
]

//根据权限过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    if (menu?.key?.startsWith('/admin')) {
      const loginUser = loginUserStroe.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}
// 展示在菜单的路由数组
const items = computed<MenuProps['items']>(() => filterMenus(originItems))
const router = useRouter()
//路由跳转事件
const doMenuClick = ({ key }) => {
  router.push({
    path: key,
  })
}
//路由监听(路由跳转后更新菜单选中状态——当前要高亮的菜单项)
router.afterEach((to, from, next) => {
  current.value = [to.path]
})

//用户注销
const doLoginout = async () => {
  const response = await userLogoutUsingPost()
  try {
    if (response.data.code === 0) {
      message.success('退出登录成功')
      loginUserStroe.setLoginUser({
        userName: '未登录',
      })
      await router.push({
        path: '/user/login',
      })
    } else {
      message.error('退出登录失败' + response.data.message)
    }
  } catch (e: any) {
    message.error('退出登录失败' + e.message)
  }
}
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}
.title {
  color: black;
  font-size: 20px;
  margin-left: 16px;
}
.logo {
  height: 50px;
}
</style>
