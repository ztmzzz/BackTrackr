<template>
  <el-row
      justify="center"
      align="middle"
      :style="{ height: 'calc(100vh - 100px)' }">
    <el-col :span="8" >
      <el-input
          v-model="searchText"
          clearable
          placeholder=""
          @keyup.enter="search"
      />
    </el-col>
    <el-col :span="2" >
      <el-button type="primary" @click="search" >搜索</el-button>
    </el-col>
  </el-row>

</template>
<script>


export default {
  name: 'SearchBox',
  data() {
    return {
      searchText: '',
    };
  },
  methods: {
    search() {
      this.$router.push({path: '/video', query: {searchText: this.searchText}})
    }
  }
}
</script>
<style scoped>

</style>