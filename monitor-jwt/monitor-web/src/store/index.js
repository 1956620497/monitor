import {defineStore} from "pinia";

//持久化处理
export const useStore = defineStore('general',{
    state:() => {
        return {
            user: {
                role:'',
                username:'',
                email:''
            }
        }
    },
    getters : {
        isAdmin(){
            return this.user.role === 'admin'
        }
    },
    persist:true
})
