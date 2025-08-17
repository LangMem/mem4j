import { defineStore } from 'pinia'

export const userStore = defineStore({

    id: 'user',

    // 数据
    state: () => {
        return {
            // 如果token有就获取，没有赋值为 ‘’
            token: localStorage.getItem('token') || '',
            userInfo: localStorage.getItem('userInfo') || {}
        }
    },

    // 方法
    actions: {
        setUserInfo(data: any) {
            this.token = data.token
            this.userInfo = data.user_info
            localStorage.setItem('token', this.token)
            localStorage.setIntem('userInfo', JSON.stringify(this.userInfo))
        }
    }
})