import axios from 'axios'
import { getToken } from './auth'

const request = axios.create({
    // 基础路径留空，因为你的 Vite 代理已经处理了 /api
    timeout: 15000
})

// ============ 请求拦截器：自动携带 Token ============
request.interceptors.request.use(
    config => {
        const token = getToken()
        if (token) {
            config.headers.Authorization = `Bearer ${token}`
        }
        return config
    },
    error => Promise.reject(error)
)

// ============ 响应拦截器：统一处理 code ============
request.interceptors.response.use(
    response => {
        const res = response.data
        // 如果后端返回的 code 不是 1（成功），则认为业务失败
        if (res.code !== 1) {
            // 弹出错误提示（你也可以换成更优雅的 Toast 组件，但目前 alert 够用）
            alert(res.msg || '请求失败')
            return Promise.reject(new Error(res.msg || '请求失败'))
        }
        // 正常返回数据
        return response
    },
    error => {
        // HTTP 错误（如 500、404、网络中断）
        const msg = error.response?.data?.msg || '网络错误，请稍后再试'
        alert(msg)
        return Promise.reject(error)
    }
)

export default request