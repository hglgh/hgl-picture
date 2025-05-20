import router from '@/router'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'

//是否为首次获取登录用户信息
let isFirstGetLoginUser = true

/**
 * 全局权限校验，每次路由跳转时（切换页面），都会执行该函数
 * router.beforeEach 函数传入的三个参数介绍如下：
 * to: RouteLocationNormalized: 表示即将要进入的目标路由对象，包含目标路径（如 to.fullPath）等信息。
 * from: RouteLocationNormalized: 表示当前导航正要离开的路由对象，可以获取当前页面路径等信息。
 * next: NavigationGuardNext: 用于控制导航行为的方法，调用 next() 表示放行到目标路由；也可以通过 next(false) 阻止导航，或通过 next('/path') 跳转到其他路由。
 */
router.beforeEach(async (to, from, next) => {
  const loginUserStroe = useLoginUserStore()
  let loginUser = loginUserStroe.loginUser
  //确保页面刷新时，首次加载时，能等待后端返回登录用户信息在校验权限
  if (isFirstGetLoginUser) {
    await loginUserStroe.fetchLoginUser()
    loginUser = loginUserStroe.loginUser
    isFirstGetLoginUser = false
  }
  const toUrl = to.fullPath
  //可以自定义权限校验逻辑，例如：管理员才能访问 /admin开头的页面
  if (toUrl.startsWith('/admin')) {
    if (!loginUser.userRole || loginUser.userRole !== 'admin') {
      message.error('没有权限')
      next(`/user/login?redirect=${to.fullPath}`)
      return
    }
  }
  next()
})
