# AI-Gateway 部署

在 **本仓库根目录**（`pom.xml` 所在目录）执行：

```bash
docker compose -f deploy/docker-compose.yml up --build
```

镜像由根目录 `Dockerfile` 构建；环境变量见根 `README.md` 与 `docker-compose.yml`。
