<template>
  <div>
    <div style="width: 100vw;height: 100vh;overflow: hidden;display: flex">
      <div style="flex: 1;background-color:black">
        <el-image style="width: 100%;height: 100%" fit="cover"
            src="/images/118282448_p0_scale.png"/>
      </div>
      <div class="welcome-title">
        <div style="font-size:30px;font-weight: bold">
          欢迎来到本小站
        </div>
        <div style="font-size:20px;margin-top: 10px">
          欢迎访问捏
        </div>
        <div style="font-size:20px;margin-top: 5px">
          欢迎喵！欢迎喵！
        </div>
      </div>
      <div class="right-card">
<!--        登录页面-->

        <router-view v-slot="{ Component }">
          <transition name="el-fade-in-linear"  mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>

</script>


<style scoped>
.right-card{
  width: 400px;
  z-index: 1;
  background-color: var(--el-bg-color)
}

.welcome-title{
  position: absolute;
  bottom: 30px;
  left: 30px;
  color: white;
  text-shadow: 0 0 10px black;
}
</style>
<!--绝对布局      position: absolute;-->
