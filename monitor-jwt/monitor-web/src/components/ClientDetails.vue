<!--主机详细信息-->

<script setup>
import {computed, reactive, watch} from "vue";
import {get, post} from "@/net";
import {copyIp, cpuNameToImage, fitByUnit, osNameToIcon, percentageToStatus, rename} from "@/tools";
import {ElMessage, ElMessageBox} from "element-plus";
import RuntimeHistory from "@/components/RuntimeHistory.vue";
import {Delete} from "@element-plus/icons-vue";

//组件传递信息
const props =  defineProps({
  id: Number,
  update:Function
})

//删除主机后通知父组件刷新
const emits = defineEmits(['delete'])

//改名之后的重新初始化
function updateDetails(){
  //刷新上一级页面
  props.update()
  //重新初始化
  init(props.id)
}

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

//页面数据
const details = reactive({
  //基本信息
  base:{},
  //运行时数据
  runtime:{
    list: []
  },
  //是否处于编辑节点信息状态
  editNode:false
})

//节点提交信息
const nodeEdit = reactive({
  name:'',
  location:''
})

//节点信息编辑
const enableNodeEdit = () => {
  details.editNode = true
  nodeEdit.name = details.base.node
  nodeEdit.location = details.base.location
}

//初始化方法
const init = id => {
  if (id !== -1){
    //清除原本的数据
    details.base = {}
    details.runtime = { list: [] }
    //请求页面接口数据
    get(`/api/monitor/details?clientId=${id}`,
        data => Object.assign(details.base,data))
    //请求性能历史记录数据,过去一小时的数据
    get(`/api/monitor/runtime-history?clientId=${id}`,
        data => Object.assign(details.runtime,data))
  }
}

//获取最新的性能数据
setInterval(() => {
  //props.id=-1的情况是没有打开这个组件的时候，没打开就不需要获取最新的数据
  if (props.id !== -1 && details.runtime){
    //每隔十秒拉取一次最新的数据
    get(`/api/monitor/runtime-now?clientId=${props.id}`,data =>{
      //删除数组中的最后一个元素
      // details.runtime.list.splice(details.runtime.list.length -1)
      //在数组的开头插入这条数据
      // details.runtime.list.unshift(data)

      //判断一下，看请求得来的数据有没有一个小时的
      if(details.runtime.list.length  >= 360)
        //先将最前面的数据删除
        details.runtime.list.splice(0,1)
      //最后面的数据才是最新的一条数据
      details.runtime.list.push(data)
    })
  }
  //每10秒钟执行一次
},10000)

//计算属性
const now = computed(() => details.runtime.list[details.runtime.list.length - 1])

//监听id属性是否变化，有变化就请求数据    {immediate: true}表示一开始就执行一次
watch(() => props.id , init,{immediate: true})

//节点编辑完成后的保存按钮
const submitNodeEdit = () => {
  post('/api/monitor/node',{
    id:props.id,
    node:nodeEdit.name,
    location: nodeEdit.location
  },() => {
    details.editNode = false
    updateDetails()
    ElMessage.success('节点信息已更新')
  })
}

//删除主机
function deleteCLient() {
  ElMessageBox.confirm('删除此主机后所有统计数据都将丢失，您确定要这么做吗？','删除主机',{
    confirmButtonText: '确定',
    cancelButtonText:'取消',
    type:'warning'
  }).then(() => {
    get(`/api/monitor/delete?clientId=${props.id}`,() => {
      //删除之后要通知一下父组件及时更新
      emits('delete')
      props.update()
      ElMessage.success('主机已成功移除')
    })
  }).catch(() => {})
}

</script>
<!--主机详细信息-->
<template>
  <el-scrollbar>
    <div class="client-details" v-loading="Object.keys(details.base).length === 0">
      <div v-if="Object.keys(details.base).length">

        <div style="display: flex;justify-content: space-between">
          <div class="title">
            <i class="fa-solid fa-server"></i>
            服务器信息
          </div>
          <el-button :icon="Delete" type="danger" plain text
                     @click="deleteCLient">删除此主机</el-button>
        </div>


        <el-divider style="margin: 10px 0" />
        <div class="details-list">
          <div>
            <span>服务器ID</span>
            <span>{{details.base.id}}</span>
          </div>
          <div>
            <span>服务器名称</span>
            <span style="margin-right: 10px;">{{details.base.name}}</span>
            <i class="fa-solid fa-pen-to-square interact-item"
               @click.stop="rename(details.base.id,details.base.name,updateDetails)"></i>
          </div>
          <div>
            <span>运行状态</span>
            <span>
          <i style="color: #18cb18" class="fa-solid fa-circle-play" v-if="details.base.online"></i>
          <i style="color: #18cb18" class="fa-solid fa-circle-stop" v-else></i>
          {{details.base.online ? '运行中' : '离线'}}
        </span>
          </div>
          <div v-if="!details.editNode">
            <span>服务器节点</span>
            <span :class="`flag-icon flag-icon-${details.base.location}`"></span>&nbsp;
            <span>{{details.base.node}}</span>&nbsp;
            <i @click.stop="enableNodeEdit"
               class="fa-solid fa-pen-to-square interact-item" />
          </div>
          <!--      服务器节点编辑框-->
          <div v-else>
            <span>服务器节点</span>
            <div style="display: inline-block;height: 15px">
              <div style="display: flex">
                <el-select v-model="nodeEdit.location" style="width: 80px" size="small">
                  <el-option v-for="item in locations" :value="item.name">
                    <span :class="`flag-icon flag-icon-${item.name}`"></span>&nbsp;
                    {{item.desc}}
                  </el-option>
                </el-select>
                <el-input v-model="nodeEdit.name" style="margin-left: 10px" size="small"
                          placeholder="请输入节点名称..." />
                <div style="margin-left: 10px">
                  <i @click.stop="submitNodeEdit" class="fa-solid fa-check interact-item" />
                </div>
              </div>
            </div>
          </div>
          <div>
            <span>公网IP地址</span>
            <span>
          {{details.base.ip}}
          <i class="fa-solid fa-copy interact-item" style="color: dodgerblue" @click.stop="copyIp(details.base.ip)"></i>
        </span>
          </div>
          <div style="display: flex">
            <span>处理器</span>
            <el-image style="height: 20px;margin-right: 10px;"
                      :src="`/cpu-icons/${cpuNameToImage(details.base.cpuName)}`" ></el-image>
            <span>{{details.base.cpuName}}</span>
          </div>
          <div>
            <span>硬件配置信息</span>
            <span>
          <i class="fa-solid fa-microchip"></i>
          <span style="margin-right: 10px;">{{` ${details.base.cpuCore} CPU 核心数  / `}}</span>
          <i class="fa-solid fa-memory"></i>
          <span>{{` ${details.base.memory.toFixed(1)} GB 内容容量`}}</span>
        </span>
          </div>
          <div>
            <span>操作系统</span>
            <i :style="{color: osNameToIcon(details.base.osName).color}"
               :class="`fa-brands ${osNameToIcon(details.base.osName).icon}`" ></i>
            <span style="margin-left: 10px">{{`${details.base.osName}  ${details.base.osVersion}`}}</span>
          </div>
        </div>
        <div class="title" style="margin-top: 20px">
          <i class="fa-solid fa-gauge-high"></i>
          实时监控
        </div>
        <el-divider style="margin: 10px 0" />
        <!--    判断服务器有没有运行  v-loading="!details.runtime.list.length"如果数组中没有数据，则不加载-->
        <div v-if="details.base.online" v-loading="!details.runtime.list.length"
             style="min-height: 200px">
          <!--      服务器运行的情况-->
          <!--    服务器各项参数-->
          <div style="display: flex" v-if="details.runtime.list.length">
            <el-progress type="dashboard" :width="100" :percentage="now.cpuUsage * 100"
                         :status="percentageToStatus(now.cpuUsage * 100)">
              <div style="font-size: 17px;font-weight: bold;color: initial">CPU</div>
              <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (now.cpuUsage * 100).toFixed(1) }} %</div>
            </el-progress>
            <el-progress style="margin-left: 20px"
                         type="dashboard" :width="100"
                         :percentage="now.memoryUsage / details.runtime.memory * 100"
                         :status="percentageToStatus(now.memoryUsage / details.runtime.memory * 100)">
              <div style="font-size: 17px;font-weight: bold;color: initial">内存</div>
              <div style="font-size: 13px;color: grey;margin-top: 5px">{{ (now.memoryUsage).toFixed(1) }} GB</div>
            </el-progress>
            <div style="flex: 1;margin-left: 30px;display: flex;flex-direction: column;height: 80px">
              <!--      实时网络速度-->
              <div style="flex: 1;font-size: 14px">
                <div>实时网络速度</div>
                <div>
                  <i style="color: orange" class="fa-solid fa-arrow-up"></i>
                  <span>{{` ${fitByUnit(now.networkUpload,'KB')} /s `}}</span>
                  <el-divider direction="vertical" />
                  <i style="color: dodgerblue" class="fa-solid fa-arrow-down"></i>
                  <span>{{` ${fitByUnit(now.networkDownload,'KB')} /s `}}</span>
                </div>
              </div>
              <!--      磁盘容量-->
              <div>
                <div style="font-size: 13px;display: flex;justify-content: space-between">
                  <div>
                    <i class="fa-solid fa-hard-drive"></i>
                    <span> 磁盘总容量 </span>
                  </div>
                  <div>{{ now.diskUsage.toFixed(1) }} GB / {{ details.runtime.disk.toFixed(1) }} GB</div>
                </div>
                <el-progress type="line" :show-text="false"
                             :status="percentageToStatus(now.diskUsage / details.runtime.disk * 100)"
                             :percentage="now.diskUsage / details.runtime.disk * 100"  />
              </div>
            </div>
          </div>
          <runtime-history style="margin-top: 20px" :data="details.runtime.list" />
        </div>
        <!--    服务器没有运行-->
        <el-empty description="服务器处于离线状态，请检查服务器是否正常运行" v-else />


      </div>
    </div>
  </el-scrollbar>

</template>

<style scoped>
/* 复制ip的按钮 */
.interact-item {
  transition: .3s;

  &:hover {
    cursor: pointer;
    scale: 1.1;
    opacity: 0.8;
  }
}

.client-details{
  height: 100%;
  padding: 20px;

  .title{
    color: dodgerblue;
    font-size: 18px;
    font-weight: bold;
  }

  .details-list{
    font-size: 14px;

    /* 为所有div标签设置 */
    & div{
      margin-bottom: 10px;


      /* 第一个标签 */
      & span:first-child {
        color: grey;
        font-size: 13px;
        font-weight: normal;
        width: 120px;
        display: inline-block;
      }

      & span {
        font-weight: bold;
      }
    }
  }

}
</style>
