//将网络上传下载的单位变为动态的
//value是数据，unit是单位，默认是字节kb
import {useClipboard} from "@vueuse/core";
import {ElMessage, ElMessageBox} from "element-plus";
import {post} from "@/net";

function fitByUnit(value, unit){
    const units = ['B','KB','MB','GB','TB','PB']
    let index = units.indexOf(unit)
    //判断一下，value要小于1，并且value不等于0 就进入
    //或者value的值大于1024，并且 index的单位要大于B或者要小于PB
    while (((value < 1 && value !== 0) || value >= 1024)
    && (index >= 0 || index < units.length)){
        if (value >= 1024){
            //如果大于1024，提高一次单位
            value = value / 1024
            index = index + 1
        }else{
            //如果小于1024，单位就降低一位
            value = value * 1024
            index = index - 1
        }
    }
    return `${parseInt(value)} ${units[index]}`
}

//进度条颜色判断
function percentageToStatus(percentage){
    if (percentage < 50)
        return 'success'
    else if (percentage < 80)
        return 'warning'
    else
        return 'exception'
}

//处理器icon判断
function cpuNameToImage(name){
    if (name.indexOf('Apple') >= 0)
        return 'Apple.png'
    else if (name.indexOf('AMD') >= 0)
        return 'AMD.png'
    else
        return 'Intel.png'
}

//操作系统图标判断
function osNameToIcon(name){
    if (name.indexOf('Ubuntu') >= 0)
        return {icon: 'fa-ubuntu',color: '#db4c1a'}
    else if (name.indexOf('CentOS') >= 0)
        return {icon: 'fa-centos',color: '#9dcd30'}
    else if (name.indexOf('macOS') >= 0)
        return {icon: 'fa-apple',color: 'grey'}
    else if (name.indexOf('Windows') >= 0)
        return {icon: 'fa-windows',color: '#3578b9'}
    else if (name.indexOf('Debian') >= 0)
        return {icon: 'fa-debian',color: '#a80836'}
    else
        return {icon: 'fa-linux',color: 'grey'}
}


//点击复制功能
const { copy } = useClipboard()
//点击复制事件
const copyIp = (ip) => copy(ip).then(() => ElMessage.success('成功复制IP地址到剪贴板'))

//重命名主机
function rename(id,name,after) {
    ElMessageBox.prompt('请输入新的服务器主机名称','修改名称',{
        confirmButtonText:'确认',
        cancelButtonText:'取消',
        inputValue: name,
        inputPattern:  /^[a-zA-Z0-9_\u4e00-\u9fa5]{1,10}$/,
        inputErrorMessage: '名称只能包含中英文字符、数字和下划线',
    }).then(({ value }) => post('/api/monitor/rename',{
            id:id,
            name: value
        },() => {
            ElMessage.success('主机名称已更新')
            //让外面页面传入一个函数进来，让里面页面通过调用这个函数来更新界面
            after()
        })
    )
}


export { fitByUnit , percentageToStatus,cpuNameToImage,osNameToIcon,copyIp,rename }
