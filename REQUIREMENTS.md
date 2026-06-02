# AetherChat 完整需求清单

## Phase 1: 核心架构与基础功能

### 1.1 项目架构
- Kotlin 100%, Jetpack Compose + Material Design 3
- MVVM + ViewModel + StateFlow + UiState pattern
- Navigation Compose Type-safe Routes (@Serializable)
- Koin dependency injection
- OkHttp + Retrofit + SSE streaming
- Room + SQLCipher 加密数据库
- Android Keystore + Tink API密钥加密
- Gradle Kotlin DSL multi-module (14 modules)

### 1.2 数据模型
- Provider: id, name, type, baseUrl, apiKeyEncrypted, isEnabled, sortOrder
- ProviderType: XIAOMI_MIMO, DEEPSEEK, MISTRAL, GROQ, ALIYUN_BAILIAN, MOONSHOT, SILICONFLOW, CUSTOM_OPENAI_COMPAT, OPENAI, ANTHROPIC, GOOGLE
- ModelInfo: id, providerId, displayName, contextWindow, supportVision, supportFunctionCall, isEnabled, isCustom
- Conversation: id, title, assistantId, providerId, modelId, systemPrompt, tags, isPinned, createdAt, updatedAt
- Message: id, conversationId, parentId, role, content (List<ContentBlock>), modelId, providerId, tokens, status
- ContentBlock: Text, Image, Code, ToolCall, ToolResult
- Assistant: id, name, iconEmoji, systemPrompt, providerId, modelId, temperature, createdAt, updatedAt

### 1.3 提供商管理 (Providers)
- 提供商预设列表 (PROVIDER_PRESETS): 小米MiMo, DeepSeek, Mistral, Groq, 阿里云百炼, 月之暗面, 硅基流动, 自定义, OpenAI, Anthropic, Google
- 添加提供商: 选择类型 → 输入BaseURL → 输入API Key → 测试连接 → 保存
- 提供商详情页: 基础配置(API Key显示/隐藏、BaseURL)、模型管理(自动检索、手动添加、启用/禁用、测试)、高级设置(多Key轮询、超时)
- 测试连接: 实际调用 API 验证，显示延迟和可用模型数
- 自动检索模型: 调用 /v1/models 获取模型列表
- 手动添加模型: 输入模型ID、显示名称、contextWindow、支持视觉/函数调用

### 1.4 对话列表 (Conversations)
- 时间分组: 置顶 / 今天 / 昨天 / 更早
- 搜索对话: 实时搜索标题
- 滑动删除: SwipeToDismiss 左滑删除
- 长按菜单: 置顶/取消置顶、重命名、删除
- 下拉刷新
- 新建对话按钮
- 空状态提示

### 1.5 聊天界面 (Chat)
- SSE 流式对话: 实时显示 AI 回复，带流光动画
- 消息气泡: 用户消息右对齐蓝色，助手消息左对齐灰色
- Markdown 渲染: 代码块语法高亮、表格、列表
- 顶部栏: 提供商选择器、模型选择器（下拉菜单）
- 输入栏: 文本输入、发送按钮、停止生成按钮、语音输入按钮、网络搜索按钮
- 提供商/模型切换: 对话内可随时切换，切换后更新对话记录
- Token 计数显示: 输入/输出 token 数
- 消息持久化: 自动保存到 Room 数据库
- 新建对话时读取默认设置: 默认提供商、默认模型、网络搜索开关、STT开关

---

## Phase 2: 设置与语音功能

### 2.1 设置页面 (Settings) - 6 大分区

#### AI 对话设置
- 默认提供商选择: 从已启用提供商列表选择
- 默认模型选择: 根据默认提供商显示可用模型
- 流式响应开关: 默认开启
- Enter 发送开关: 默认开启
- Token 计数显示开关: 默认开启

#### 网络搜索设置
- 启用网络搜索开关
- 搜索引擎选择: SearXNG (默认)、Google、Bing
- SearXNG 服务地址配置
- 搜索结果数量限制

#### 语音设置 - STT (语音识别)
- 启用 STT 开关
- STT 提供商选择: OpenAI Whisper (默认)、Google Speech
- API Key 输入 (加密存储)
- 服务地址配置
- 模型选择: whisper-1

#### 语音设置 - TTS (语音合成)
- 启用 TTS 开关
- TTS 提供商选择: OpenAI TTS (默认)、Edge TTS
- API Key 输入 (加密存储)
- 服务地址配置
- 语音选择: alloy, echo, fable, onyx, nova, shimmer
- 语速滑块: 0.25x - 4.0x

#### 数据同步 (WebDAV)
- 启用 WebDAV 同步开关
- 服务器地址
- 用户名
- 密码 (加密存储)
- 远程路径
- 自动同步开关
- 同步间隔选择: 每次启动、每小时、每天
- 立即同步按钮
- 测试连接按钮
- 数据导出: 导出所有对话为 JSON

#### 外观设置
- 主题模式: 跟随系统、浅色、深色
- 动态颜色开关 (Material You)
- 字体大小滑块

#### 关于
- 应用版本
- 开源许可
- 项目主页链接

### 2.2 语音功能实现
- STT 录音: 长按语音按钮录音，松开发送
- TTS 播放: 点击消息播放语音
- TTS Player Service: 后台播放服务，支持暂停/继续

---

## Phase 3: 高级功能

### 3.1 助手管理 (Assistants)
- 助手列表: 显示所有助手，点击进入对话
- 创建助手: 名称、图标emoji、系统提示词、默认提供商/模型、temperature
- 预设助手模板: 写作助手、编程助手、翻译助手、学习助手、创意助手
- 助手详情页: 编辑助手设置、删除助手
- 使用助手: 新建对话时选择助手，自动应用系统提示词和设置

### 3.2 MCP 协议 (工具调用)
- MCP 客户端连接: WebSocket 连接 MCP 服务器
- 工具发现: 获取可用工具列表
- 工具调用: AI 请求调用工具时执行并返回结果

### 3.3 RAG 知识库
- 知识库管理: 创建/删除知识库
- 文档上传: 支持 PDF、TXT、MD
- 向量存储: 使用嵌入模型生成向量
- 检索增强: 对话时检索相关知识片段

### 3.4 WebDAV 同步实现
- 上传对话: 将本地对话上传到 WebDAV
- 下载对话: 从 WebDAV 下载对话
- 冲突处理: 时间戳比较，保留较新版本
- 增量同步: 只同步变更的对话

### 3.5 引导页 (Onboarding)
- 欢迎 页: 应用介绍
- 添加提供商页: 引导添加第一个提供商
- 完成页: 开始使用

---

## 技术约束

### 数据持久化
- Room 数据库加密: SQLCipher 4.6.1
- API Key 加密: Android Keystore + Tink
- 设置持久化: SharedPreferences (名称: aetherchat_settings)

### 网络请求
- SSE 流式: OkHttp EventSource
- 超时设置: 连接 30s, 读取 60s
- 重试策略: 最多 3 次

### 导航结构
- OnboardingRoute → ConversationsRoute → ChatRoute
- ProvidersRoute → AddProviderRoute / ProviderDetailRoute
- SettingsRoute
- AssistantsRoute → CreateAssistantRoute

### Koin 模块
- coreDataModule: SharedPreferences, AetherChatDatabase, ProviderRepository
- coreCryptoModule: KeystoreEncryptor
- featureSettingsModule: SettingsViewModel, DataExporter
- featureChatModule: ChatViewModel
- featureProvidersModule: ProvidersViewModel, AddProviderViewModel, ProviderDetailViewModel
- featureAssistantsModule: AssistantsViewModel, CreateAssistantViewModel
- featureConversationsModule: ConversationsViewModel