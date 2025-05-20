<template>
  <div id="basicLayout">
    <a-layout style="min-height: 100vh">
      <a-layout-header class="header">
        <GlobalHeader/>
      </a-layout-header>
      <a-layout>
        <GlobalSider class="sider"/>
        <a-layout-content class="content">
          <router-view />
        </a-layout-content>
      </a-layout>
      <a-layout-footer class="footer">
        <a href="https://www.codefather.cn" target="_blank">
          小扈严选 by 请别把我整破防
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">

import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalSider from '@/components/GlobalSider.vue'
</script>

<style scoped>
#basicLayout .header{
  background: #ffffff;
  color: unset;
  padding-inline: 20px;
  margin-bottom: 1px;
}
#basicLayout .content {
  padding: 28px;
  background: linear-gradient(to right, #fefefe, #ffffff);
  margin-bottom: 40px;
}
#basicLayout .footer {
  background: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}
#basicLayout .sider{
  padding-top: 20px;
  background: #ffffff;
  border-right: 0.5px solid #eee;
}
#basicLayout :deep(.ant-menu-root){
  border-bottom: none !important;
  border-inline-end: none !important;
}
</style>
