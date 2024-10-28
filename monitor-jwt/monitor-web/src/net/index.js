import axios from  'axios'
import {ElMessage} from "element-plus";
import {useStore} from "@/store";


const authItemName = "access_token"

//默认的失败
const defaultFailure = (message,code,url) => {
    console.warn(`请求地址:${url},状态码:${code},错误信息:${message}`)
    ElMessage.warning(message)
}

const defaultError = (err) => {
    console.error(err)
    ElMessage.warning('发生了一些错误请联系管理员')
}

//拿到Token
function takeAccessToken(){
    //取出token
    const str = localStorage.getItem(authItemName) || sessionStorage.getItem(authItemName)
    //判断一下有没有拿到，没拿到直接返回空
    if (!str) return null;
    //如果拿到了，重新封回
    const authObj = JSON.parse(str)
    //判断一下有没有过期
    if (authObj.expire <= new Date()){
        //如果已经过期了
        deleteAccessToken()
        ElMessage.warning('登录状态已过期，请重新登录')
        return null;
    }
    return authObj.token
}

//保存token      remember-是否勾选记住我   expire-token过期时间
function storeAccessToken(token,remember,expire){
    //封装成一个对象
    const authObj = { token:token,expire:expire }

    //如果用户勾选了记住我
    const str = JSON.stringify(authObj)

    if (remember)
        //如果勾选了记住我,就存储到本地存储空间中
        localStorage.setItem(authItemName,str)
    else
        //如果没有勾选记住我，就存到会话存储空间中
        sessionStorage.setItem(authItemName,str)
}

//删除对象域中的Token
function deleteAccessToken(){
    localStorage.removeItem(authItemName)
    sessionStorage.removeItem(authItemName)
}

//用于生成请求头
function accessHeader(){
    //拿到token
    const token = takeAccessToken();
    //判断一下有没有token
    return token ? {
        //如果有就返回
        'Authorization':`Bearer ${takeAccessToken()}`
        //如果没有就返回空
    } : {}
}

//封装post请求     内部使用的封装请求
function internalPost(url,data,header,success,failure,error = defaultError){
    axios.post(url,data,{
        headers:header
    }).then(({data}) => {
        if (data.code === 200){
            //代表请求成功
            success(data.data)
        }else{
            failure(data.message,data.code,url)
        }
    }).catch(err => error(err))
}


//封装GET请求    内部使用
function internalGet(url,header,success,failure,error = defaultError){
    axios.get(url,{ headers:header }).then(({data}) => {
        if (data.code === 200){
            //代表请求成功
            success(data.data)
        }else{
            failure(data.message,data.code,url)
        }
    }).catch(err => error(err))
}

//再次封装，封装一个外部使用的请求接口
function get(url,success,failure = defaultFailure){
    internalGet(url,accessHeader(),success,failure)
}

function post(url,data,success,failure = defaultFailure){
    internalPost(url,data,accessHeader(),success,failure)
}

//登录请求
function login(username,password,remember,success,failure = defaultFailure){
    internalPost('/api/auth/login',{
        //请求数据
        username:username,
        password:password
    },{
        //改成json格式数据
        'Content-Type':'application/x-www-form-urlencoded'
    },(data) => {
        //存储AccessToken
        storeAccessToken(data.token,remember,data.expire)
        //存储到持久化插件中
        const store = useStore()
        store.user.role = data.role
        store.user.username = data.username
        store.user.email = data.email
        //成功回调
        ElMessage.success(`登录成功,欢迎${data.username}来到系统`)
        success(data)
    },failure)
}

//退出登录
function logout(success,failure = defaultFailure){
    get('/api/auth/logout',() => {
        deleteAccessToken()
        ElMessage.success('退出登录成功,欢迎您再次使用')
        success()
    },failure)
}

//判断一下是否未验证（登录）
function unauthorized(){
    //如果这里不是空的，就是已登录,返回true
    return !takeAccessToken()
}


//暴露出去
export {login,logout,get,post,unauthorized}
