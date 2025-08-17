// createRouter 创建 router 对象  
// RouteRecordRaw 用于规范路由规则，增强路由对象类型限制
// createWebHashHistory 用于使用路由实例，指定路由的工作模式（hash）
import { createRouter, createWebHashHistory, RouteRecordRaw } from 'vue-router'
import { ElMessage } from 'element-plus'

import {
    Files
} from '@element-plus/icons-vue'

// 创建路由规则
const routes: Array<RouteRecordRaw> = [
    {
        path: '/',
        name: 'Index',
        redirect: "/index",
        component: () => import('../pages/Index.vue'),
        children: [
            {
                path: '/index',
                name: 'index',
                component: () => import('../pages/index/Index.vue'),
                meta: {
                    isShow: true,
                    icon: Files,
                    title: '首页'
                }
            },
        ]
    },

    // 用户输入的不存在的地址将会自动跳转到404页面
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('../pages/error/404.vue')
    }

]

// 创建 路由的实例对象
const router = createRouter({
    history: createWebHashHistory(),
    routes
})

router.beforeEach(async (to, from, next) => {

    // 注释登录
    // const token = localStorage.getItem("token")
    // // 没有登录强制跳转回登录页
    // if (!token && to.path !== "/login") {
    //     ElMessage({
    //         type: 'error',
    //         message: '用户登录超时,请重新登录',
    //         center: true,
    //     })
    //     return next({ path: '/login' })
    // }

    // // 防止重复登录
    // if (token && to.path === '/login') {
    //     ElMessage({
    //         type: 'error',
    //         message: '请勿重复登录',
    //         center: true,
    //     })
    //     // from.path 获取当前页面路径
    //     return next({ path: from.path ? from.path : '/login' })
    // }

    next()
})

// 导出
export default router