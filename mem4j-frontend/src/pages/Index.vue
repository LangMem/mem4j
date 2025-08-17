<template >
  <div class="common-layout">
    <el-container>
      <hr>
      <el-main>
        <!-- 子界面路由入口 -->
        <router-view></router-view>
      </el-main>
      <hr>
    </el-container>
  </div>
</template>

<script setup lang="ts">
</script>

<style lang="sass">
html,
body,
#app,
.el-container,
.common-layout
  margin: 0
  height: 100%

.el-header
  background: linear-gradient(rgb(178, 255, 172), rgb(149, 255, 186))

.el-main
  background: linear-gradient(rgb(149, 255, 186), rgb(62, 255, 225))

.el-footer
  background: linear-gradient(rgb(62, 255, 225), rgb(19, 255, 247))
  height: 28vh
</style>

