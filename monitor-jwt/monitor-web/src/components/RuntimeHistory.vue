<!--运行时图表展示-->


<script setup>
import {onMounted, watch} from "vue";
import * as echarts from "echarts";
import {defaultOption, doubleSeries, singleSeries} from "@/echarts";

//获取历史信息
const props = defineProps({
  data: Array
})

//获取所有demo节点
const charts = []

//创建一个时间轴的List的数据,x轴
const loaclTimeLine = list =>　list.map(item => new  Date(item.timestamp).toLocaleString())

//更新CPU的使用率
function updateCpuUsage(list){
  //拿到对应的图表
  const chart = charts[0]
  //拿到CPU的使用率
  let data = list.map(item => (item.cpuUsage * 100).toFixed(1))
  //echarts有个option属性，这个属性就是图表各项展示的参数
  const option = defaultOption('CPU(%)',loaclTimeLine(list))
  singleSeries(option,'CPU使用率(%)',data,['#72c4fe','#72d5fe','#2b6fd733'])
  chart.setOption(option)
}

//更新内存的使用率
function updateMemoryUsage(list) {
  const chart = charts[1]
  let data = list.map(item => (item.memoryUsage * 1024).toFixed(1));
  const option = defaultOption('内存(MB)',loaclTimeLine(list))
  singleSeries(option,'内存使用(MB)',data,['#6be3a3','#bbfad4','#A5FFD033'])
  chart.setOption(option);
}

//网络使用率
function updateNetworkUsage(list){
  const chart = charts[2]
  let data = [
      list.map(item => item.networkUpload),
      list.map(item => item.networkDownload)
  ]
  const option = defaultOption('网络(KB/S)',loaclTimeLine(list))
  doubleSeries(option,['上传(KB/s)','下载(KB/s)'],data,[
      ['#f6b66e','#ffd29c','#fddfc033'],
      ['#79c7ff','#3cabf3','rgba(192,242,253,0.2)']
  ])
  chart.setOption(option)
}

//磁盘使用率
function updateDiskUsage(list){
  const chart = charts[3]
  let data = [
      list.map(item => item.diskRead.toFixed(1)),
      list.map(item => item.diskWrite.toFixed(1))
  ]
  const option = defaultOption('磁盘(MB/s)',loaclTimeLine(list))
  doubleSeries(option,['读取(MB/s)','写入(MB/s)'],data,[
      ['#d2d2d2','#d5d5d5','rgba(199,199,199,0.2)'],
      ['#757575','#7c7c7c','rgba(94,94,94,0.2)']
  ])
  chart.setOption(option)
}



//初始化方法
function initCharts(){
  //获取到所有节点的dome
  const chartList = [
    document.getElementById('cpuUsage'),
    document.getElementById('memoryUsage'),
    document.getElementById('networkUsage'),
    document.getElementById('diskUsage'),
  ]
  //循环用echarts
  for (let i = 0 ; i < chartList.length ; i++){
    const chart = chartList[i]
    charts[i] = echarts.init(chart)
  }
}

//初始化echarts,要在加载成功之后才能去初始化
onMounted(() => {
  //先初始化好
  initCharts()
  //更新使用情况
  watch(() => props.data,list => {
    //更新CPU的使用率,绘制图表
    updateCpuUsage(list)
    //内存使用率
    updateMemoryUsage(list)
    //网络上传下载
    updateNetworkUsage(list)
    //磁盘读写
    updateDiskUsage(list)
    //immediate: true先加载一次,deep:true作用是当数据发生更改时，也可以获取到
  },{ immediate: true,deep:true })
})

</script>

<!--运行时图表展示-->

<template>
<div class="charts">
  <div id="cpuUsage" style="width: 100%;height: 250px"></div>
  <div id="memoryUsage" style="width: 100%;height: 250px"></div>
  <div id="networkUsage" style="width: 100%;height: 250px"></div>
  <div id="diskUsage" style="width: 100%;height: 250px"></div>

</div>
</template>

<style scoped>
.charts {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-gap: 1px;
}
</style>
