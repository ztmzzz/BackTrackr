<template>
  <el-container>
    <el-header>
      <el-menu
          :default-active="$route.path"
          mode="horizontal"
          router
      >
        <el-menu-item index="/search">搜索</el-menu-item>
        <el-menu-item index="/video">回想</el-menu-item>
        <el-menu-item index="/setting">设置</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <router-view></router-view>
    </el-main>
  </el-container>
</template>

<script>


export default {
  name: 'App',
  components: {},
  mounted() {
  }

}
</script>

<style>

</style>
