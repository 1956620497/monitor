<script setup>
import {logout} from "@/net";
import router from "@/router";
import {Back, Moon, Sunny} from "@element-plus/icons-vue";
import {useDark} from "@vueuse/core";
import {ref} from "vue";
import TabItem from "@/components/TabItem.vue";
import {useRoute} from "vue-router";

function userLogout(){
  logout(()=> router.push('/'))
}

//管理导航栏
const tabs = [
  //如果路由是该路由的话，将自动激活active
  {id:1,name:'管理',route:'manage'},
  {id:2,name:'安全',route: 'security'}
]

//拿到当前路由
const route = useRoute()
//通过当前路由来得到默认的index
const defaultIndex = () => {
  for (let tab of tabs) {
    if (route.name === tab.route)
      return tab.id
  }
  return 1
}

//主题模式切换
const dark = ref(useDark())

//默认选择管理页面
const tab = ref(defaultIndex())

//点击跳转路由
function changePage(item){
  tab.value = item.id
  router.push({name: item.route})
}



</script>

<template>
  <el-container class="main-container">
    <el-header  class="main-header">
<!--      Logo-->
      <el-image style="height: 30px"
                src="http://element-plus.org/images/element-plus-logo.svg" />
      <div class="tabs">
        <tab-item v-for="item in tabs" :name="item.name"
                  :active="item.id === tab" @click="changePage(item)" />
<!--        切换颜色主题的-->
        <el-switch style="margin: 0 20px" v-model="dark"
                   active-color="#424242" :active-action-icon="Moon"
                   :inactive-action-icon="Sunny" />
<!--        头像-->
        <el-dropdown>
          <el-avatar class="avatar"
                     src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png"/>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="userLogout">
                <el-icon><Back /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <el-main class="main-content">
<!--      配置主页面路由-->
      <router-view v-slot="{ Component }">
        <transition name="el-fade-in-linear" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>

    </el-main>
  </el-container>
</template>

<style scoped>
.main-container{
  height:100vh;
  width: 100vw;

  .main-header{
    height: 55px;
    background-color: var(--el-bg-color);
    border-bottom: solid 1px var(--el-border-color);
    display: flex;
    align-items: center;

    .tabs {
      height: 55px;
      gap: 10px;
      /* 将外层flex占满 */
      flex: 1px;
      display: flex;
      align-items: center;
      /* 元素全部居右 */
      justify-content: right;
    }
  }

  .main-content{
    height: 100%;
    background-color: #f5f5f5;
  }

}

/* 设置深色模式 */
.dark .main-container .main-content {
  background-color: #232323;
}

</style>
