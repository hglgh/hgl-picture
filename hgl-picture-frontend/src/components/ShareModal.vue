<template>
  <a-modal v-model:visible="visible" title="分享图片" :footer="false" @cancel="closeModal">
    <h4>复制分享链接</h4>
    <a-typography-link copyable>
      {{ link }}
    </a-typography-link>
    <div style="margin-bottom: 16px" />
    <h4>手机扫码查看</h4>
    <a-qrcode :value="link"/>
  </a-modal>
</template>

<script setup lang="ts">
import { defineProps, ref, withDefaults, defineExpose } from 'vue'

/**
 * 定义组件属性类型
 */
interface Props {
  title: string
  link: string
}

/**
 * 给组件指定初始值
 */
const props = withDefaults(defineProps<Props>(), {
  title: () => '分享',
  link: () => 'https://laoyujianli.com/share/yupi',
})

const visible = ref(false)
// 打开模态框
const openModal = () => {
  visible.value = true
}
// 关闭模态框
const closeModal = () => {
  visible.value = false
}
// 暴露方法给父组件调用
defineExpose({
  openModal,
})
</script>
