# AetherChat 问题清单

## 一、Settings 模块问题 (8项)

| # | 问题 | 严重程度 | 文件位置 |
|---|------|----------|----------|
| S1 | 搜索引擎选项不完整 - 只支持 SearXNG，缺少 Google 和 Bing | 中 | SettingsScreen.kt:351-355 |
| S2 | 缺少搜索结果数量限制设置 | 中 | SettingsUiState.kt, SettingsScreen.kt |
| S3 | STT 提供商选项不完整 - 只支持 OpenAI Whisper，缺少 Google Speech | 中 | SettingsScreen.kt:373-378 |
| S4 | STT 模型选择应为下拉选择器而非文本输入 | 低 | SettingsScreen.kt:390-394 |
| S5 | TTS 提供商选项不完整 - 只支持 OpenAI TTS，缺少 Edge TTS | 中 | SettingsScreen.kt:404-409 |
| S6 | TTS 语速滑块范围不符合需求 - 当前 0.5-2.0，需求 0.25-4.0 | 中 | SettingsScreen.kt:438 |
| S7 | WebDAV 同步间隔选项与需求不符 | 低 | SettingsScreen.kt:480-492 |
| S8 | 开源许可功能未实现 - 点击事件为空 | 低 | SettingsScreen.kt:565-570 |

## 二、Providers 模块问题 (3项)

| # | 问题 | 严重程度 | 文件位置 |
|---|------|----------|----------|
| P1 | customModels 列表未正确加载 - 没有按 isCustom 分离 | 高 | ProvidersViewModel.kt:209-216 |
| P2 | 高级设置功能未实现 - 多Key轮询、超时设置只有UI占位 | 中 | ProviderDetailScreen.kt:345-371 |
| P3 | ProviderEntity 缺少高级设置字段 (timeoutSeconds, apiKeys) | 中 | Entity.kt:14-22 |

## 三、Chat 模块问题 (6项)

| # | 问题 | 严重程度 | 文件位置 |
|---|------|----------|----------|
| C1 | 消息气泡颜色 - 用户消息应为蓝色，助手消息应有灰色背景 | 高 | ChatScreen.kt |
| C2 | Markdown 渲染不完整 - 缺少代码块语法高亮、表格渲染 | 中 | MarkdownText.kt |
| C3 | Token 计数未显示在消息气泡中 | 中 | ChatScreen.kt |
| C4 | 网络搜索功能未集成 - WebSearchProvider 未注入 ChatViewModel | 高 | ChatViewModel.kt |
| C5 | STT 语音输入未完整实现 - 需要长按录音逻辑和权限请求 | 高 | ChatViewModel.kt, ChatScreen.kt |
| C6 | ChatInputBar 语音按钮应为长按触发而非点击切换 | 中 | ChatScreen.kt |

## 四、Assistants 模块问题 (6项)

| # | 问题 | 严重程度 | 文件位置 |
|---|------|----------|----------|
| A1 | 缺少助手详情页 - 无法编辑或删除已创建的助手 | 高 | 需新建 AssistantDetailScreen.kt |
| A2 | 创建助手时缺少图标emoji选择器 | 中 | CreateAssistantScreen.kt |
| A3 | 创建助手时缺少提供商/模型选择 | 高 | CreateAssistantScreen.kt |
| A4 | 新建对话时无法选择助手 | 高 | ConversationsDrawer.kt |
| A5 | 助手设置未应用到对话 - ChatViewModel 不读取助手配置 | 高 | ChatViewModel.kt |
| A6 | 助手列表点击无响应 - onNavigateToDetail 是空实现 | 高 | AetherChatNavHost.kt:113 |

## 五、导航问题 (1项)

| # | 问题 | 严重程度 | 文件位置 |
|---|------|----------|----------|
| N1 | 缺少 AssistantDetailRoute 路由定义 | 高 | Route.kt, AetherChatNavHost.kt |

---

## 修复优先级排序

### 第一优先级（核心功能缺失）
1. **A1+A6+N1**: 完整实现助手详情页（新建文件 + 路由 + 导航）
2. **A3+A4+A5**: 实现助手选择功能（创建助手时选择提供商/模型 + 新建对话时选择助手 + 应用助手设置）
3. **C4**: 集成网络搜索功能
4. **C5+C6**: 完整实现 STT 语音输入（长按录音 + 权限请求）
5. **C1**: 修复消息气泡颜色

### 第二优先级（功能不完整）
6. **P1**: 修复 customModels 列表加载
7. **S1+S3+S5**: 补充搜索引擎/STT/TTS 提供商选项
8. **S2**: 添加搜索结果数量限制设置
9. **C2+C3**: 完善 Markdown 渲染和 Token 显示

### 第三优先级（细节优化）
10. **S4+S6+S7+S8**: UI 优化（下拉选择器、语速范围、同步间隔、开源许可）
11. **P2+P3**: 高级设置功能实现
12. **A2**: emoji 选择器

---

## 修复计划

### 阶段 1: Assistants 完整重建 (A1-A6 + N1)
- 新建 AssistantDetailScreen.kt
- 添加 AssistantDetailRoute
- 实现 AssistantDetailViewModel
- CreateAssistantScreen 添加提供商/模型选择
- ConversationsDrawer 添加助手选择入口
- ChatViewModel 应用助手设置

### 阶段 2: Chat 核心功能 (C1-C6)
- 修复消息气泡颜色
- 集成 WebSearchProvider
- 实现长按录音 STT
- 完善 Markdown 渲染
- 显示 Token 计数

### 阶段 3: Settings 完善 (S1-S8)
- 补充所有缺失选项
- 修复语速范围
- 实现开源许可页面

### 阶段 4: Providers 完善 (P1-P3)
- 修复 customModels 加载
- 实现高级设置（可选）