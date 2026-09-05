# TouHouBlog

一个以东方 Project 风格为主题的个人博客，包含文章、杂谈、图集、音乐播放器、开发者日志、OAuth2 登录、夜间模式等功能。前端基于 Astro 4 + Vue 3，后端基于 Spring Boot 3.2，数据库使用 MySQL 8，部署于阿里云 ECS，支持 Docker、Nginx 和 HTTPS。

---

## ✨ 功能特性

- 📝 **文章系统**：Markdown 编辑、分类、标签、搜索、归档时间线、草稿、点赞
- 💬 **杂谈模块**：短内容发布，支持多图
- 🖼️ **图集模块**：瀑布流展示、灯箱预览、后台上传（阿里云 OSS 直传）
- 🎵 **音乐播放器**：黑胶唱片 UI、歌词高亮、歌单切换、播放模式切换、跨页面连续播放
- 🧾 **开发者日志**：独立时间线，记录迭代过程
- 🔐 **登录认证**：Gitee / GitHub OAuth2 登录，JWT 鉴权，站长身份识别
- 🌙 **明暗主题**：全局 CSS 变量 + `data-theme` 切换，毛玻璃与粉紫蓝渐变风格
- 🌸 **东方风格视觉**：樱花飘落、粒子背景、开屏动画、自定义光标、加载骨架屏
- 🚀 **生产部署**：Nginx 反向代理、HTTPS（Let's Encrypt）、systemd 服务守护、MySQL 容器化

---

## 🧰 技术栈

### 后端
- Java 21
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- PageHelper 2.1.1
- Spring Security + OAuth2 Client
- JWT (jjwt 0.12.3)
- Knife4j (OpenAPI 3)
- OkHttp 3
- 阿里云 OSS SDK（前端直传）
- Lombok

### 前端
- Astro 4
- Vue 3
- Tailwind CSS 4
- `@iconify/vue`（Lucide 图标）
- `md-editor-v3`（Markdown 编辑器）
- `ali-oss`（前端直传）
- APlayer（动态导入）
- 自定义毛玻璃、动画与特效

### 数据库与部署
- MySQL 8.0（Docker 容器）
- Nginx 1.18
- Docker & Docker Compose
- Certbot（HTTPS 证书）
- systemd（服务守护）

---

## 📁 项目结构

```
TouHouBlog/
├── TouHouBlog-Backend/                # 后端
│   ├── src/main/java/BlogBack/
│   │   ├── common/                    # 通用返回、异常、常量
│   │   ├── config/                    # Security、MyBatis 配置
│   │   ├── controller/                # 控制器
│   │   ├── dto/                       # 数据传输对象
│   │   ├── entity/                    # 实体类
│   │   ├── mapper/                    # MyBatis-Plus Mapper
│   │   ├── service/                   # 服务层
│   │   ├── utils/                     # JWT、OSS 等工具
│   │   └── filter/                    # JWT 过滤器
│   └── src/main/resources/
│       ├── application.yml            # 主配置
│       └── application-dev.yml        # 开发/生产环境配置
│
└── TouHouBlog-Frontend/               # 前端
    ├── src/
    │   ├── components/
    │   │   ├── common/                # 全局组件（樱花、粒子等）
    │   │   └── pages/                 # 页面组件
    │   ├── layouts/
    │   │   └── Layout.astro           # 全局布局
    │   ├── pages/                     # Astro 页面路由
    │   ├── styles/                    # 全局样式（global.css 等）
    │   ├── utils/                     # 请求封装、鉴权等
    │   └── public/                    # 静态资源（图片等）
    ├── astro.config.mjs
    └── package.json
```

---

## 🚀 本地开发

### 后端

1. 确保已安装 JDK 21、MySQL 8。
2. 创建数据库 `touhoublog`，并导入备份 SQL（可选）。
3. 修改 `application-dev.yml` 中的数据库连接、OAuth、OSS 等配置。
4. 在 IDE 中运行 `TouHouBlogApplication`，或使用 Maven：
   ```bash
   mvn spring-boot:run
   ```
   后端默认运行在 `http://localhost:8080`。

### 前端

1. 确保已安装 Node.js 18+ 和 npm。
2. 进入前端目录并安装依赖：
   ```bash
   cd TouHouBlog-Frontend
   npm install
   ```
3. 启动开发服务器：
   ```bash
   npm run dev
   ```
   前端默认运行在 `http://localhost:4321`，并通过 Vite 代理转发 `/api` 到后端。

---

## 🌐 生产部署

以下步骤基于本项目已部署的阿里云 ECS（Ubuntu 22.04）环境，供参考。

### 1. 环境准备

- 服务器安装 Docker、Nginx、JDK 21、Node.js 20。
- 设置虚拟内存（可选，2G 内存建议）：
  ```bash
  fallocate -l 2G /swapfile
  chmod 600 /swapfile
  mkswap /swapfile
  swapon /swapfile
  echo '/swapfile none swap sw 0 0' | tee -a /etc/fstab
  ```

### 2. 启动 MySQL 容器

```bash
docker run -d \
  --name touhou-mysql \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD=你的密码 \
  -e MYSQL_DATABASE=touhoublog \
  -v /opt/touhoublog/mysql:/var/lib/mysql \
  --restart=always \
  mysql:8.0
```

导入数据（可选）：

```bash
docker exec -i touhou-mysql mysql -uroot -p你的密码 touhoublog < backup.sql
```

### 3. 后端构建与部署

```bash
# 本地构建（IDEA 或 Maven）
cd TouHouBlog-Backend
mvn clean package -DskipTests
scp target/TouHouBlog-Backend-1.0-SNAPSHOT.jar root@你的服务器IP:/opt/touhoublog/
```

在服务器上创建 systemd 服务 `/etc/systemd/system/touhou-backend.service`：

```ini
[Unit]
Description=TouHouBlog Backend
After=network.target

[Service]
WorkingDirectory=/opt/touhoublog
ExecStart=/usr/bin/java -jar /opt/touhoublog/TouHouBlog-Backend-1.0-SNAPSHOT.jar
Restart=always
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
```

启动：

```bash
systemctl daemon-reload
systemctl enable --now touhou-backend
```

### 4. 前端构建与部署

```bash
cd TouHouBlog-Frontend
npm run build
scp -r dist root@你的服务器IP:/opt/touhoublog/
```

创建 `/etc/systemd/system/touhou-frontend.service`：

```ini
[Unit]
Description=TouHouBlog Frontend
After=network.target

[Service]
WorkingDirectory=/opt/touhoublog/dist
ExecStart=/usr/bin/node /opt/touhoublog/dist/server/entry.mjs
Restart=always
RestartSec=5
User=root

[Install]
WantedBy=multi-user.target
```

启动：

```bash
systemctl daemon-reload
systemctl enable --now touhou-frontend
```

### 5. Nginx 配置

创建 `/etc/nginx/sites-available/touhoublog`：

```nginx
server {
    listen 80;
    server_name 你的域名;

    location / {
        proxy_pass http://127.0.0.1:4321;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /api/ {
        rewrite ^/api/(.*)$ /$1 break;
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /oauth2/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /login/oauth2/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

启用并重载：

```bash
ln -s /etc/nginx/sites-available/touhoublog /etc/nginx/sites-enabled/
rm -f /etc/nginx/sites-enabled/default
nginx -t
systemctl reload nginx
```

### 6. HTTPS（Let's Encrypt）

```bash
apt install -y certbot python3-certbot-nginx
certbot --nginx -d 你的域名 -d www.你的域名
```

证书自动续期已由 certbot 定时任务处理。

---

## 🎵 歌单更新

本项目歌单已改为**本地文件读取**，不在运行时请求外部 API。歌单文件路径：

```
/opt/touhoublog/playlist.json
```

格式示例：

```json
[
  {
    "id": "4165168",
    "name": "土着神の頂点 ～Native Faith",
    "artist": "Like a rabbit"
  }
]
```

### 更新歌单的方法

1. **手动编辑**：修改 `playlist.json` 后重启后端。
2. **运行 Python 脚本**（服务器上需要能访问网易云 v6 接口）：

   ```bash
   python3 /opt/touhoublog/fetch_playlist.py
   systemctl restart touhou-backend
   ```

   脚本会通过网易云歌单 ID 自动抓取歌曲列表并覆盖 `playlist.json`。

---

## ⚙️ 配置说明

### 后端 `application-dev.yml`

需要配置以下变量：

- 数据库连接（`datasource.host`、`port`、`database`、`username`、`password`）
- Gitee OAuth（`gitee.client-id`、`gitee.client-secret`）
- GitHub OAuth（`github.client-id`、`github.client-secret`）
- JWT 密钥（`jwt.secret`）
- 站长第三方 ID（`admin.gitee.id`、`admin.github.id`）
- 阿里云 OSS（`oss.endpoint`、`oss.bucket`、`oss.access-key-id`、`oss.access-key-secret`、`oss.region`）

### 前端环境

前端默认通过 `/api` 请求后端，开发时由 Vite 代理，生产时由 Nginx 反向代理。

OAuth 登录按钮（`LoginButtons.vue`）建议使用相对路径：

```html
href="/oauth2/authorization/github"
href="/oauth2/authorization/gitee"
```

---

## 🔧 常见问题

### Q1：服务器上音乐歌单为空或超时
答：服务器访问 Meting API 可能被屏蔽。现已改为本地 `playlist.json` 文件，不再依赖外部 API。

### Q2：图片上传报 403 Preflight
答：需要在阿里云 OSS Bucket 的跨域设置中允许你的域名（如 `https://touhoublog.top`），并且允许 `PUT`、`GET` 等方法。

### Q3：HTTPS 无法访问但 HTTP 正常
答：检查安全组是否放行 443 端口，并且安全组正确绑定到 ECS 实例。

### Q4：OAuth 登录提示“无效的登录回调地址”
答：确保 Gitee/GitHub 平台配置的回调地址与后端 `redirect-uri` 完全一致，例如 `https://你的域名/login/oauth2/code/gitee`。

### Q5：前端组件在页面切换时模式错乱
答：确保所有使用 `MusicPlayer` 的页面都使用 `client:only="vue"` 并保留 `transition:persist`，同时不要将 `MusicPlayer` 做成全局布局内的多实例。

---

## 📄 许可证

本项目为个人学习与展示用途，未特别指定开源许可证。如需引用或二次开发，请保留作者信息。

---

**感谢使用 TouHouBlog，愿幻想乡的风常伴你左右。**
