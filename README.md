# AI-Gateway

自研 **Spring Boot 3** 网关：对外 **OpenAI Chat Completions** 兼容（`POST /v1/chat/completions`），按路由将请求转发到 **OpenAI**、**DeepSeek**（OpenAI 兼容透传）、**Anthropic Messages** 或 **Google Gemini generateContent**；客户端使用网关颁发的 **Bearer**（`GATEWAY_CLIENT_TOKENS`），上游密钥仅在网关侧配置。

- 仓库：`https://github.com/leaviiiiing/AI-Gateway.git`
- **设计与架构说明**：[docs/网关项目说明.md](docs/网关项目说明.md)（[文档索引](docs/README.md)）
- 运维 Agent（a1）为独立项目；通过 `OPENAI_BASE_URL` 指向本服务、`OPENAI_API_KEY` 使用网关客户端令牌；可选 `OPSAGENT_LLM_OPENAI_ROUTE_PROVIDER` 发送 `X-Route-Provider`。

## 路由

| 方式 | 说明 |
|------|------|
| `X-Route-Provider` 请求头 | `openai` \| `deepseek` \| `anthropic` \| `gemini`（优先级最高） |
| `model` 推断 | `deepseek-*` → DeepSeek；`claude*` / 含 `anthropic` → Anthropic；`gemini*` / `google/*` → Gemini；否则 → OpenAI |

## 环境变量

| 变量 | 说明 |
|------|------|
| `GATEWAY_CLIENT_TOKENS` | 逗号分隔的客户端 Bearer（**空** = 开发模式放行所有请求并打 WARN） |
| `UPSTREAM_OPENAI_API_KEY` / `UPSTREAM_OPENAI_BASE_URL` | OpenAI 上游 |
| `UPSTREAM_DEEPSEEK_API_KEY` / `UPSTREAM_DEEPSEEK_BASE_URL` | DeepSeek 上游（默认 `https://api.deepseek.com`） |
| `UPSTREAM_ANTHROPIC_API_KEY` / `UPSTREAM_ANTHROPIC_BASE_URL` | Anthropic |
| `UPSTREAM_ANTHROPIC_VERSION` | 默认 `2023-06-01` |
| `UPSTREAM_GEMINI_API_KEY` / `UPSTREAM_GEMINI_BASE_URL` | Gemini（Google AI Studio） |
| `GATEWAY_RATE_LIMIT_PER_MINUTE` | Resilience4j 全局限流（默认 `300` / 分钟） |
| `SERVER_PORT` | 默认 `8090` |

## 运行

### 本地 Maven

```bash
mvn spring-boot:run
```

### 容器（本仓库内）

在 **仓库根目录**（与 `pom.xml` 同级）执行：

```bash
docker compose -f deploy/docker-compose.yml up --build
```

默认暴露 **8090**。上游与客户端令牌可通过环境变量或 `.env` 传入（见上表与 `deploy/docker-compose.yml`）。

根目录 **`Dockerfile`** 为多阶段构建（Maven 打包 + JRE 运行）；**`.dockerignore`** 已排除 `target/` 等。

健康检查：`GET http://localhost:8090/actuator/health`  
指标：`GET http://localhost:8090/actuator/prometheus`

## 限制

- **`stream: true`**：当前返回 `400 stream_not_supported`（非透传流式后续迭代）。
- **Gemini**：当前实现针对 **Generative Language API + API key**；Vertex 需扩展 `GeminiDriver` 鉴权分支。

## 与 ops-agent 联调示例

```bash
export OPENAI_BASE_URL=http://localhost:8090
export OPENAI_API_KEY=<gateway-client-token>
export OPSAGENT_LLM_PROVIDER=openai
export OPSAGENT_LLM_OPENAI_ROUTE_PROVIDER=deepseek
```

`opsagent.llm.openai.model` 设为与路由一致的模型名（如 `deepseek-chat`、`gpt-4o-mini`、`gemini-1.5-flash`、`claude-3-5-sonnet-20240620`）。
