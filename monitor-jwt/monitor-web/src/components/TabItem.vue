<script setup>
defineProps({
  // 导航栏名字
  name: String,
  //是否选中当前导航栏
  active: Boolean
})
</script>

<!--自己写的导航栏组件-->
<template>
  <div :class="`tab-item ${active ? 'active' : ''}`">
    {{name}}
  </div>
</template>

<style scoped>
.tab-item{
  font-size: 15px;
  width: 55px;
  height: 55px;
  text-align: center;
  line-height: 55px;
  box-sizing: border-box;
  transition: color .3s;

  &:hover{
    cursor:pointer;
    color: var(--el-color-primary);
  }
}

.active {
  border-bottom: solid 2px var(--el-color-primary);
}
</style>
