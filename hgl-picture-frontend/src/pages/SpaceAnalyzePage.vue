<template>
  <div id="spaceAnalyzePage">
    <h2>
      空间图库分析 -
      <span v-if="queryAll">全部空间</span>
      <span v-if="queryPublic">公共图库</span>
      <span v-if="spaceId">
        <a :href="`/space/${spaceId}`" target="_blank"> 空间Id:{{ spaceId }}</a>
      </span>
    </h2>
    <div style="margin-bottom: 16px"/>
    <a-row :gutter="[16, 16]">
<!--      空间使用分析-->
      <a-col :xs="24" :md="12">
        <SpaceUsageAnalyze :query-all="queryAll" :space-id="spaceId" :query-public="queryPublic"/>
      </a-col>
<!--      空间分类分析-->
      <a-col :xs="24" :md="12">
        <SpaceCategoryAnalyze :query-all="queryAll" :space-id="spaceId" :query-public="queryPublic"/>
      </a-col>
<!--      空间标签分析-->
      <a-col :xs="24" :md="12">
        <SpaceTagAnalyze :query-all="queryAll" :space-id="spaceId" :query-public="queryPublic"/>
      </a-col>
<!--      空间大小分析-->
      <a-col :xs="24" :md="12">
        <SpaceSizeAnalyze :query-all="queryAll" :space-id="spaceId" :query-public="queryPublic"/>
      </a-col>
<!--      用户上传行为分析-->
      <a-col :xs="24" :md="12">
        <SpaceUserAnalyze :query-all="queryAll" :space-id="spaceId" :query-public="queryPublic"/>
      </a-col>
<!--      空间使用排行分析-->
      <a-col :xs="24" :md="12">
        <SpaceRankAnalyze v-if="isAdmin" :query-all="queryAll" :space-id="spaceId" :query-public="queryPublic"/>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
import SpaceUsageAnalyze from '@/components/analyze/SpaceUsageAnalyze.vue'
import SpaceCategoryAnalyze from '@/components/analyze/SpaceCategoryAnalyze.vue'
import SpaceTagAnalyze from '@/components/analyze/SpaceTagAnalyze.vue'
import SpaceSizeAnalyze from '@/components/analyze/SpaceSizeAnalyze.vue'
import SpaceUserAnalyze from '@/components/analyze/SpaceUserAnalyze.vue'
import SpaceRankAnalyze from '@/components/analyze/SpaceRankAnalyze.vue'
import { useRoute } from 'vue-router'
import { computed } from 'vue'
import { useLoginUserStore } from '@/stores/useLoginUserStore'

const route = useRoute()
const spaceId = computed(() => route.query?.spaceId as string)
const queryAll = computed(() => !!route.query?.queryAll)
const queryPublic = computed(() => !!route.query?.queryPublic)

// 判断是否为管理员
const loginUserStroe = useLoginUserStore()
const loginUser = loginUserStroe.loginUser
const isAdmin = computed(() => {
  return loginUser.userRole === 'admin'
})
</script>

<style scoped>
#spaceAnalyzePage {
}
</style>
