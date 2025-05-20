import { ref } from 'vue'
import { defineStore } from 'pinia'
import { getLoginUserUsingGet } from '@/api/yonghujiekou'

/**
 * 存储登录用户信息的状态
 */
export const useLoginUserStore = defineStore('loginUser', () => {
  //定义状态的初始值
  const loginUser = ref<API.LoginUserVO>({
    userName: '未登录',
  })

  async function fetchLoginUser() {
    const res = await getLoginUserUsingGet()
    if (res.data.code === 0 && res.data.data) {
      loginUser.value = res.data.data
    }

    //测试用户登录， 3s后自动登录
    // setTimeout(() => {
    //   loginUser.value = {
    //     userName: '测试用户',
    //     id: 1,
    //   }
    // }, 3000)
  }

  /**
   * 设置登录用户信息
   * @param newLoginUser
   */
  //定义怎样修改状态
  function setLoginUser(newLoginUser: any) {
    loginUser.value = newLoginUser
  }

  //返回
  return { loginUser, fetchLoginUser, setLoginUser }
})
