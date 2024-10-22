import {createRouter, createWebHistory} from "vue-router";
import {unauthorized} from "@/net";

const router = createRouter({
    history:createWebHistory(import.meta.env.BASE_URL),
    routes:[
        {
            path:'/',
            name:'welcome',
            component:() => import('@/views/WelcomeView.vue'),
            children:[
                {
                    path:'',
                    name:'welcome-login',
                    component:()=>import('@/views/welcome/LoginPage.vue')
                },
                {
                    path:'reset',
                    name:'welcome-reset',
                    component:() => import('@/views/welcome/ResetPage.vue')
                }
            ]
        },{
            path:'/index',
            name:'index',
            component: () => import('@/views/indexView.vue'),
            children:[
                {
                    path: '',
                    name: 'manage',
                    component: () => import('@/views/main/Manage.vue')
                },
                {
                    path: 'security',
                    name: 'security',
                    component: () => import('@/views/main/Security.vue')
                }
            ]
        }
    ]
})

//配置一下路由守卫
router.beforeEach((to,from,next) => {
    const isUnauthorized = unauthorized()
    //要访问welcome-的页面并且已登录
    if (to.name.startsWith('welcome-') && !isUnauthorized){
        //直接跳转到index页面
        next('/index')
        //用户要访问index页面并且没有登录
    }else if (to.fullPath.startsWith('/index') && isUnauthorized){
        //跳转到登录界面
        next('/')
    }else {
        next()
    }
})

export default router
