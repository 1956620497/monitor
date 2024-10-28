<script setup>
import {Delete, Lock, Plus, Refresh, Switch} from "@element-plus/icons-vue";
import {computed, reactive, ref} from "vue";
import {get, logout, post} from "@/net";
import {ElMessage} from "element-plus";
import router from "@/router";
import CreateSubAccount from "@/components/CreateSubAccount.vue";
import {useStore} from "@/store";


//持久数据
const store = useStore()
//表单数据
const formRef = ref()
//验证是否通过
const valid = ref(false)
//如果验证通过，激昂valid设置为true，可以点击提交按钮
const onValidate = (prop,isValid) => valid.value = isValid

//修改密码表单
const form = reactive({
  password:'',
  new_password:'',
  new_password_repeat:''
})

//对比密码方法
const validatePassword = (rule,value,callback) => {
  if (value === ''){
    callback(new Error("请再次输入密码"))
  }else if (value !== form.new_password){
    callback(new Error("两次输入的密码不一致"))
  }else {
    callback()
  }
}

//验证邮箱
const emailForm = reactive({
  email:store.user.email,
  code:''
})

//设置请求验证码的冷却时间
const coldTime = ref(0)
//判断邮箱是否正确
const isEmailValid = ref(true)

//判断邮箱是否正确
const onEmailValidate = (prop,isValid) => {
  if (prop === 'email'){
    isEmailValid.value = isValid
  }
}

//修改邮箱成功的方法
function modifyEmail(){
  post('/api/user/modify-email',emailForm,() => {
    ElMessage.success('邮件修改成功')
    logout(() => router.push('/'))
  })
}

//发送邮箱方法
const validateEmail = () => {
  coldTime.value = 60
  let handle;
  get(`/api/auth/ask-code?email=${emailForm.email}&type=modify`,()=>{
    ElMessage.success(`验证码已发送到邮箱:${emailForm.email},请注意查收`)
    handle = setInterval(() => {
      coldTime.value--
      if (coldTime.value === 0){
        clearInterval(handle)
      }
    },1000)
  },(message) => {
    ElMessage.warning(message)
    coldTime.value = 0
  })
}

//规则
const rules = {
  password:[
    {required:true,message:'请输入原来的密码',trigger:'blur'}
  ],
  new_password : [
    {required:true,message:'请输入新的密码',trigger:'blur'},
    {min:6,max:16,message:'密码的长度必须在6-16个字符之间',trigger: ['blur']}
  ],
  new_password_repeat: [
    {required:true,message:'请重复输入新的密码',trigger:'blur'},
    {validator:validatePassword,trigger: ['blur','change']}
  ],
  email:[
    {required:true,message:'输入邮件地址',trigger:'blur'},
    {type:'email',message:'请输入合法的电子邮件地址',trigger: ['blur','change']}
  ]
}

//提交事件
function resetPassword() {
  formRef.value.validate(isValid => {
    if (isValid){
      post('/api/user/change-password',form,() => {
        ElMessage.success('密码修改成功，请重新登录！')
        //修改密码成功之后直接退出当前用户
        logout(() => router.push("/"))
      })
    }
  })
}

//主机信息列表
const simpleList = ref([])

//如果是子账户，就不需要获取服务器列表
if (store.isAdmin){
  //获取简单主机信息列表
  get('/api/monitor/simple-list',list => {
    simpleList.value = list
    initSubAccounts()
  })
}



//子账户列表
const accounts = ref([])

//获取所有的子账户列表
const initSubAccounts = () =>
    get('/api/user/sub/list',list => accounts.value = list)

//是否显示创建子账户弹窗
const createAccount = ref(false)

//删除子账户
function deleteAccount(id) {
  get(`/api/user/sub/delete?uid=${id}`,() => {
    ElMessage.success('子账户删除成功')
    //重新初始化子账户列表
    initSubAccounts()
  })
}


</script>
<!--安全页面-->
<template>
<div style="display: flex;gap:10px ">
  <div style="flex: 50%">
<!--    修改密码模块-->
    <div class="info-card">
      <div class="title"><i class="fa-solid fa-lock"></i> 修改密码</div>
      <el-divider style="margin: 10px 0" />
      <el-form @validate="onValidate" :model="form" :rules="rules"
               ref="formRef" style="margin: 20px" label-width="100">
        <el-form-item label="当前密码" prop="password">
          <el-input type="password" v-model="form.password"
                    :prefix-icon="Lock" placeholder="当前密码" maxlength="16" />
        </el-form-item>
        <el-form-item label="新密码" prop="new_password">
          <el-input type="password" v-model="form.new_password"
                    :prefix-icon="Lock" placeholder="新密码" maxlength="16" />
        </el-form-item>
        <el-form-item label="重复新密码" prop="new_password_repeat">
          <el-input type="password" v-model="form.new_password_repeat"
                    :prefix-icon="Lock" placeholder="重复新密码" maxlength="16" />
        </el-form-item>
        <div style="text-align: center">
          <el-button :icon="Switch" @click="resetPassword"
                     type="success" :disabled="!valid">立即重置密码</el-button>
        </div>
      </el-form>
    </div>
    <div class="info-card" style="margin-top: 10px">
      <div class="title"><i class="fa-regular fa-envelope"></i> 电子邮件设置</div>
      <el-divider style="margin: 10px 0" />
      <el-form :model="emailForm" label-position="top" :rules="rules"
               ref="emailFormRef" @validate="onEmailValidate" style="margin: 0 10px 10px 10px">
        <el-form-item label="电子邮件" prop="email">
          <el-input v-model="emailForm.email" />
        </el-form-item>
        <el-form-item>
          <el-row style="width: 100%" :gutter="10">
            <el-col :span="18">
              <el-input placeholder="请获取验证码" v-model="emailForm.code" />
            </el-col>
            <el-col :span="6">
              <el-button type="success" @click="validateEmail" style="width: 100%"
                         :disabled="!isEmailValid || coldTime > 0">
                {{coldTime > 0 ? '请稍后 ' + coldTime + ' 秒' : '获取验证码'}}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
        <div>
          <el-button @click="modifyEmail" :disabled="!emailForm.email"
                     :icon="Refresh" type="success">保存电子邮件</el-button>
        </div>
      </el-form>
    </div>
  </div>
<!--  子用户管理模块-->
  <div class="info-card" style="flex: 50%">
    <div class="title"><i class="fa-solid fa-users"></i> 子用户管理</div>
    <el-divider style="margin: 10px 0" />
    <div v-if="accounts.length" style="text-align: center">
      <div v-for="item in accounts" class="account-card">
        <el-avatar class="avatar" :size="30"
                   src="https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png" />
        <div style="margin-left: 15px;line-height: 18px;flex: 1">
          <div>
            <span>{{item.username}}</span>
            <span style="font-size: 13px;color: grey;margin-left: 5px">
              管理 {{item.clientList.length}} 个服务器
            </span>
          </div>
          <div style="font-size: 13px;color: grey">{{item.email}}</div>
        </div>
        <el-button type="danger" :icon="Delete"
                   @click="deleteAccount(item.id)" text>删除子账户</el-button>
      </div>
      <el-button :icon="Plus" type="primary"
                 @click="createAccount = true" plain>添加更多子账户</el-button>
    </div>
    <div v-else>
      <el-empty :image-size="100" description="还没有子用户哦" v-if="store.isAdmin">
        <el-button :icon="Plus" type="primary" @click="createAccount = true"
                   plain>添加子用户</el-button>
      </el-empty>
      <el-empty :image-size="100"  description="子账户只能由管理员账号进行操作" v-else />
    </div>
  </div>
<!--  子账户管理-->
  <el-drawer  v-model="createAccount" size="350" :with-header="false">
    <create-sub-account :clients="simpleList" @create="createAccount = false;initSubAccounts()" />
  </el-drawer>
</div>
</template>

<style scoped>

:deep(.el-drawer){
  margin: 10px;
  height: calc(100% - 20px);
  border-radius: 10px;
}

:deep(.el-drawer__body){
  padding: 0;
}

/* 子账户样式 */
.account-card {
  border-radius: 5px;
  background-color: var(--el-bg-color-page);
  padding: 10px;
  display: flex;
  align-items: center;
  text-align: left;
  margin: 10px 0;
}

/* 卡片样式 */
.info-card {
  border-radius: 7px;
  padding: 15px 20px;
  background-color: var(--el-bg-color);
  height: fit-content;

  .title {
    font-size: 18px;
    font-weight: bold;
    color: dodgerblue;
  }
}
</style>
