<script setup>
import PreviewCard from "@/components/PreviewCard.vue";
import {computed, reactive, ref} from "vue";
import {get, post} from "@/net";
import ClientDetails from "@/components/ClientDetails.vue";
import RegisterCard from "@/components/RegisterCard.vue";
import {Plus} from "@element-plus/icons-vue";
import {useRoute} from "vue-router";
import {useStore} from "@/store";
import TerminalWindow from "@/components/TerminalWindow.vue";


//地区转换
const locations = [
  {name:'cn',desc:'中国大陆'},
  {name:'hk',desc:'香港'},
  {name:'jp',desc:'日本'},
  {name:'us',desc:'美国'},
  {name:'sg',desc:'新加坡'},
  {name:'kr',desc:'韩国'},
  {name:'de',desc:'德国'}
]

//存储当前选择了哪些区域
const checkedNodes = ref([])

//主机数据存放
const list = ref([])

//获取路由对象
const route = useRoute()

//获取持久化数据
const store = useStore()

//获取数据请求
const updateList = () => {
  //判断路由显示的是否是当前页面，如果是当前页面则不请求主机列表
  if (route.name === 'manage'){
    get('/api/monitor/list',data => {
      list.value = data
    })
  }

}
//设置定时器,每10秒钟执行一次
setInterval(updateList,10000)
// setInterval(updateList,3000)
updateList()

//详细信息相关
const detail = reactive({
  //是否展示
  show: false,
  id: -1
})

//主机点击事件，展示主机详细信息
const displayClientDeatils = (id) => {
  detail.show = true
  detail.id = id
}

//计算属性，
const  clientList = computed(() => {
  //如果选择区域里面什么都没有，就显示全部
  if (checkedNodes.value.length === 0){
    return list.value
    //如果里面有东西，说明选择了某些区域，只显示选中区域的主机
  }else {
    return list.value.filter(item => checkedNodes.value.indexOf(item.location) >= 0)
  }
})

//新增主机相关
const register = reactive({
  show:false,
  token:''
})

//获取最新的Token
const refreshToken = () => get('/api/monitor/register',token => register.token = token)

//打开ssh连接框
function openTerminal(id){
  //打开ssh连接页面
  terminal.show = true
  terminal.id = id
  //关闭详情页面
  detail.show = false
}

//ssh远程连接参数
const terminal = reactive({
  show:false,
  id:-1
})



</script>
<!--管理页面-->
<template>
  <div class="manage-main">
    <div style="display: flex;justify-content: space-between;align-items: end">
      <div>
        <div class="title">
          <i class="fa-solid fa-server"></i>
          管理主机列表
        </div>
        <div class="desc">在这里管理所有已经注册的主机实例，实时监控主机运行状态，快速进行管理和操作。</div>
      </div>
      <div>
        <el-button :icon="Plus" type="primary" :disabled="!store.isAdmin"
                   @click="register.show = true" plain>添加新主机</el-button>
      </div>
    </div>

    <el-divider style="margin: 10px 0" />
<!--    主机分类-->
    <div style="margin-bottom: 20px">
      <el-checkbox-group v-model="checkedNodes">
        <el-checkbox v-for="node in locations" :key="node" :label="node.name" border>
          <span :class="`flag-icon flag-icon-${node.name}`" ></span>
          <span style="font-size: 13px;margin-left: 10px" >{{node.desc}}</span>
        </el-checkbox>
      </el-checkbox-group>
    </div>
<!--    主机卡片-->
    <div class="card-list" v-if="list !== null && list.length ">
      <preview-card v-for="item in clientList" :data="item" :update="updateList"
                    @click="displayClientDeatils(item.id)"/>
    </div>
    <el-empty description="还没有任何主机,点击右上角添加" v-else />
<!--    主机详细信息-->
<!--    v-if="list.length" 列表没初始化完成时，不加载这个东西-->
    <el-drawer size="520" :show-close="false" v-model="detail.show"
               :with-header="false" v-if="list !== null && list.length" @close="detail.id = -1">
      <client-details :id="detail.id" :update="updateList" @delete="updateList"
                      @terminal="openTerminal" />
    </el-drawer>
    <el-drawer v-model="register.show" direction="ttb" :with-header="false" @open="refreshToken"
               style="width: 600px;margin: 10px auto" size="320">
      <register-card :token="register.token"   />
    </el-drawer>
<!--    ssh连接窗口-->
    <el-drawer style="width: 800px" :size="500" direction="btt" @close="terminal.id = -1"
               v-model="terminal.show" :close-on-click-modal="false">
      <template #header>
        <div>
          <div style="font-size: 18px;color: dodgerblue;font-weight: bold;">SSH远程连接</div>
          <div style="font-size: 14px">
            远程连接的建立将由服务端完成，因此在内网环境下也可以正常使用。
          </div>
        </div>
      </template>
      <terminal-window :id="terminal.id" />
    </el-drawer>
  </div>
</template>

<style scoped>
:deep(.el-checkbox-group .el-checkbox){
  margin-right: 10px;
}

/*改变主机详细信息抽屉的样式*/
:deep(.el-drawer){
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}
:deep(.el-drawer__body){
  padding: 0;
}

.manage-main{
  margin: 0 50px;

  .title{
    font-size: 22px;
    font-weight: bold;
  }

  .desc{
    font-size: 15px;
    color: grey;
  }
}

.card-list {
  display: flex;
  gap: 20px;
  flex-wrap: wrap;
}
</style>
