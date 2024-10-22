<script setup>
import PreviewCard from "@/components/PreviewCard.vue";
import {reactive, ref} from "vue";
import {get} from "@/net";
import ClientDetails from "@/components/ClientDetails.vue";

//主机数据存放
const list = ref([])
//获取数据请求
const updateList = () => get('/api/monitor/list',data => {
  list.value = data
  console.log(list.value)
})
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

</script>
<!--管理页面-->
<template>
  <div class="manage-main">
    <div class="title">
      <i class="fa-solid fa-server"></i>
      管理主机列表
    </div>
    <div class="desc">在这里管理所有已经注册的主机实例，实时监控主机运行状态，快速进行管理和操作。</div>
    <el-divider style="margin: 10px 0" />
<!--    主机卡片-->
    <div class="card-list">
      <preview-card v-for="item in list" :data="item" :update="updateList"
                    @click="displayClientDeatils(item.id)"/>
    </div>
<!--    主机详细信息-->
<!--    v-if="list.length" 列表没初始化完成时，不加载这个东西-->
    <el-drawer size="520" :show-close="false" v-model="detail.show"
               :with-header="false" v-if="list.length" @close="detail.id = -1">
      <client-details :id="detail.id" :update="updateList" />
    </el-drawer>
  </div>
</template>

<style scoped>
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
