<template>
  <el-container>
    <el-header>
      <el-menu
          :default-active="$route.path"
          mode="horizontal"
          router
          style="height: 50px"
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

<style scoped>

.el-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

</style>
<style>
html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
}
</style>
