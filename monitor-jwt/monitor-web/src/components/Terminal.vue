<script setup>

import {onBeforeUnmount, onMounted, ref} from "vue";
import {ElMessage} from "element-plus";
import {AttachAddon} from "xterm-addon-attach/src/AttachAddon";
import {Terminal} from "xterm";
import "xterm/css/xterm.css";

const props = defineProps({
  id:Number
})

//获取组件DOM
const terminalRef = ref()

//创建一个webScoket     这个地址是直接连接到服务端的地址
const socket = new WebSocket(`ws://127.0.0.1:8080/terminal/${props.id}`)

//关闭时或连接失败时通知一下用户
socket.onclose = evt => {
  if (evt.code !== 1000){
    ElMessage.warning(`连接失败:${evt.reason}`)
  }else {
    ElMessage.success('远程SSH连接已断开')
  }
}

//注册插件，这样就可以把Socket附加到命令框上面了
const attachAddon = new AttachAddon(socket);

//组件提供的命令窗口相关配置
const term = new Terminal({
  lineHeight:1.2,
  rows:20,
  fontSize:13,
  fontFamily:"Monaco,Menlo,Consolas,'Courier New',monospace",
  fontWeight:"bold",
  theme:{
    background:'#000000'
  },
  //光标闪烁
  cursorBlink:true,
  cursorStyle:'underline',
  scrollback:100,
  tabStopWidth:4
});

//加载一下
term.loadAddon(attachAddon);

//组件初始化之后执行
onMounted(() => {
  //打开终端
  term.open(terminalRef.value)
  //让光标输入集中到窗口中
  term.focus()
})

//关闭时关闭连接
//在组件卸载之前,将连接断开，将插件也断开
onBeforeUnmount(() => {
  socket.close();
  term.dispose();
})


</script>

<!--命令窗口-->

<template>
  <div ref="terminalRef" class="xterm" />
</template>

<style scoped>
:deep(.xterm .xterm-viewport){
  overflow-y: auto;
}
</style>
