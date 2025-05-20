<template>
  <div id="userRegisterPage">
    <h2 class="title">小扈图库-用户注册</h2>
    <div class="desc">这是跟鱼皮做的第一个企业级项目</div>
    <a-form :model="formState" name="basic" autocomplete="off" @finish="handleSubmit">
      <a-form-item name="userAccount" :rules="[{ required: true, message: '请输入账号!' }]">
        <a-input v-model:value="formState.userAccount" placeholder="请输入账号" />
      </a-form-item>
      <a-form-item
        name="userPassword"
        :rules="[
          { required: true, message: '请输入密码!' },
          { min: 8, message: '密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.userPassword" placeholder="请输入密码" />
      </a-form-item>
      <a-form-item
        name="checkPassword"
        :rules="[
          { required: true, message: '请输入确认密码!' },
          { min: 8, message: '确认密码长度不能小于8位' },
        ]"
      >
        <a-input-password v-model:value="formState.checkPassword" placeholder="请输入确认密码" />
      </a-form-item>
      <div class="tips">
        已有账号？
        <router-link to="/user/login">去登录</router-link>
      </div>
      <a-form-item>
        <a-button type="primary" html-type="submit" style="width: 100%">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { reactive } from 'vue'
import { userLoginUsingPost, userRegisterUsingPost } from '@/api/yonghujiekou'
import { useLoginUserStore } from '@/stores/useLoginUserStore'
import { message } from 'ant-design-vue'
import Router from '@/router'
import router from '@/router'

/**
 * 用于接受表单输入的内容
 */
const formState = reactive<API.UserRegisterRequest>({
  userAccount: '',
  userPassword: '',
  checkPassword: '',
})

const userLoginStore = useLoginUserStore()

/**
 * 提交表单
 * @param values
 */
const handleSubmit = async (values: any) => {
  //校验密码是否一致
  if (values.userPassword !== values.checkPassword){
    message.error('两次密码不一致')
    return
  }
  const result = await userRegisterUsingPost(values)
  try {
    // 注册成功,跳转到登录页了
    if (result.data.code === 0 && result.data.data) {
      message.success('注册成功')
      router.push({
        path: '/user/lgoin',
        replace: true,
      })
    } else {
      message.error('注册失败' + result.data.message)
    }
  } catch (e) {
    message.error('注册失败' + e.message)
  }
}
</script>

<style scoped>
#userRegisterPage {
  max-width: 360px;
  margin: 0 auto;
}
.title {
  text-align: center;
  margin-bottom: 16px;
}
.desc {
  color: #bbb;
  text-align: center;
  margin-bottom: 16px;
}
.tips {
  color: #bbb;
  text-align: right;
  font-size: 13px;
  margin-bottom: 16px;
}
</style>
