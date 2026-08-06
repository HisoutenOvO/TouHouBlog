// 临时用户管理，用于评论/点赞的 userId
// 等登录做完后，替换为从 JWT 解析

const USER_KEY = 'currentUserId'

export function getCurrentUserId() {
    return localStorage.getItem(USER_KEY) || '6'  // 默认用 guest1（博丽灵梦）
}

export function setCurrentUserId(id) {
    localStorage.setItem(USER_KEY, id)
    window.location.reload()
}