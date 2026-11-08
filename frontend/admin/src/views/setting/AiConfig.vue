<template>
  <div class="ai-config-page">
    <!-- 统计卡片 -->
    <el-row :gutter="16" class="stats-row">
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-total">
          <div class="stat-icon"><el-icon><Connection /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">配置总数</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-enabled">
          <div class="stat-icon"><el-icon><CircleCheck /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.enabled }}</div>
            <div class="stat-label">已启用</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div
          class="stat-card stat-default stat-card-clickable"
          role="button"
          tabindex="0"
          @click="openDefaultModelDialog"
          @keyup.enter="openDefaultModelDialog"
        >
          <div class="stat-icon"><el-icon><Star /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">默认模型</div>
            <div class="stat-label">点击查看</div>
          </div>
        </div>
      </el-col>
      <el-col :xs="12" :sm="6">
        <div class="stat-card stat-providers">
          <div class="stat-icon"><el-icon><Grid /></el-icon></div>
          <div class="stat-body">
            <div class="stat-value">{{ stats.providers }}</div>
            <div class="stat-label">服务商数</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主内容区 -->
    <el-card class="main-card" shadow="never">
      <div class="catalog-layout">
        <!-- 左侧：能力分类导航 -->
        <aside class="category-sidebar">
          <button
            v-for="cat in categoryNavItems"
            :key="cat.key"
            type="button"
            class="category-nav-item"
            :class="{ active: activeCategory === cat.key, 'is-empty': cat.count === 0 && cat.key !== 'all' }"
            @click="switchCategory(cat.key)"
          >
            <span class="category-nav-icon">
              <el-icon><component :is="categoryIconMap[cat.key]" /></el-icon>
            </span>
            <span class="category-nav-label">{{ cat.label }}</span>
            <span class="category-nav-count">{{ cat.count }}</span>
          </button>
        </aside>

        <div class="catalog-main">
          <!-- 搜索栏 -->
          <div class="filter-bar">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索模型名称或服务商"
              clearable
              :prefix-icon="Search"
              class="filter-input"
              @keyup.enter="searchAiConfig"
            />
            <el-select v-model="searchEnabled" placeholder="启用状态" clearable class="filter-select">
              <el-option label="已启用" :value="1" />
              <el-option label="已禁用" :value="0" />
            </el-select>
            <el-button type="primary" :icon="Search" @click="searchAiConfig">查询</el-button>
            <el-button :icon="RefreshLeft" @click="resetSearch">重置</el-button>
            <div class="filter-actions">
              <el-button type="primary" :icon="Plus" @click="showAddDialog">新增配置</el-button>
              <el-button :icon="Refresh" @click="refreshList">刷新</el-button>
            </div>
          </div>

          <!-- 模型目录：服务商级别卡片 -->
          <div v-loading="tableLoading" class="grouped-catalog">
            <el-empty
              v-if="!providerGroups.length"
              description="暂无匹配的模型，可切换分类或新增配置"
              :image-size="80"
            />

            <div class="provider-card-list">
              <article
                v-for="group in providerGroups"
                :key="group.key"
                class="provider-card"
                :class="{ 'has-configured': group.configured > 0 }"
              >
                <div class="provider-card-header" @click="toggleProvider(group.key)">
                  <div class="provider-card-left">
                    <span class="provider-card-badge" :style="{ background: group.color }">{{ group.label.charAt(0) }}</span>
                    <div class="provider-card-info">
                      <h3 class="provider-card-title">
                        {{ group.label }}
                        <el-tag
                          v-for="t in (group.typeLabels || [])"
                          :key="t"
                          size="small"
                          effect="plain"
                          :type="group.isAvatar ? 'warning' : 'info'"
                          class="provider-type-tag"
                        >{{ t }}</el-tag>
                      </h3>
                      <p class="provider-card-meta">
                        <span class="meta-item">共 {{ group.total }} 个模型</span>
                        <el-divider direction="vertical" />
                        <span class="meta-item meta-configured">{{ group.configured }} 已配置</span>
                        <el-divider direction="vertical" />
                        <span class="meta-item meta-enabled">{{ group.enabled }} 已启用</span>
                      </p>
                    </div>
                  </div>
                  <div class="provider-card-right">
                    <el-tag v-if="group.apiKeyConfigured" type="success" size="small" effect="plain">API 已配置</el-tag>
                    <el-tag v-else type="info" size="small" effect="plain">未配置</el-tag>
                    <el-icon class="expand-icon" :class="{ expanded: expandedProviders.has(group.key) }">
                      <ArrowRight />
                    </el-icon>
                  </div>
                </div>

                <!-- 展开区域：只读模型清单 + 品牌操作区 -->
                <transition name="el-zoom-in-top">
                  <div v-if="expandedProviders.has(group.key)" class="provider-card-body">
                    <!-- 只读模型清单（共享同一密钥） -->
                    <div class="brand-model-section">
                      <div class="brand-section-header">
                        <span class="section-label">模型清单（共享同一密钥）</span>
                        <span v-if="group.apiEndpoint" class="brand-endpoint">{{ group.apiEndpoint }}</span>
                      </div>
                      <div class="brand-model-chips">
                        <span
                          v-for="m in group.models"
                          :key="`${m.providerCode}-${m.modelName}`"
                          class="brand-model-chip"
                          :class="{ configured: m.configured, enabled: m.config?.isEnabled === 1, 'is-default': m.config?.isDefault === 1 }"
                        >
                          <code>{{ m.modelName }}</code>
                          <el-tag v-if="m.config?.isDefault === 1" type="warning" size="small" effect="dark">默认</el-tag>
                          <el-icon v-else-if="m.config?.isEnabled === 1" class="chip-on"><CircleCheck /></el-icon>
                        </span>
                      </div>
                    </div>

                    <!-- 品牌操作区 -->
                    <div class="brand-action-bar">
                      <el-button
                        v-if="getProviderApplicationUrl(group.providerCode)"
                        tag="a"
                        :href="getProviderApplicationUrl(group.providerCode)"
                        target="_blank"
                        rel="noopener noreferrer"
                        :icon="Link"
                      >申请密钥</el-button>
                      <el-button type="primary" :icon="Key" @click="openCredentialDialog(group)">
                        {{ group.apiKeyConfigured ? '更新密钥' : '配置密钥' }}
                      </el-button>
                      <el-button :icon="CircleCheck" :disabled="!group.configured" @click="setGroupEnabled(group, 1)">启用全部</el-button>
                      <el-button :icon="CircleClose" :disabled="!group.configured" @click="setGroupEnabled(group, 0)">停用全部</el-button>
                      <el-button :icon="Connection" :disabled="!group.representativeConfig" @click="testConnection(group.representativeConfig)">测试连接</el-button>
                      <el-button type="danger" plain :icon="Delete" :disabled="!group.configured" @click="clearGroupConfig(group)">清除配置</el-button>
                    </div>

                    <!-- 默认模型（按功能类型独立生效） -->
                    <div v-if="group.configured" class="brand-default-bar">
                      <span class="brand-default-label">默认模型</span>
                      <el-select
                        v-model="groupDefaultSelection[group.key]"
                        placeholder="选择该功能类型的默认模型"
                        size="small"
                        class="brand-default-select"
                        filterable
                      >
                        <el-option
                          v-for="m in group.models"
                          :key="`${m.providerCode}::${m.modelName}`"
                          :label="m.modelName"
                          :value="`${m.providerCode}::${m.modelName}`"
                        />
                      </el-select>
                      <el-button size="small" type="warning" plain :disabled="!groupDefaultSelection[group.key]" @click="setGroupDefault(group)">设为默认</el-button>
                      <span class="brand-default-hint">按功能类型（文本/视觉/语音/数字人等）各自生效，不影响其它类型的默认</span>
                    </div>
                  </div>
                </transition>
              </article>
            </div>
          </div>
        </div>
      </div>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="760px"
      class="config-dialog"
      destroy-on-close
      @opened="focusSelectedTemplate"
      @close="resetForm"
    >
      <!-- 模板选择 -->
      <div class="template-section">
        <div class="section-title">
          <el-icon><MagicStick /></el-icon>
          选择配置模板
          <el-tag v-if="currentDefaultModel" type="warning" size="small" effect="dark" style="margin-left: 12px">
            当前使用：{{ currentDefaultModel }}
          </el-tag>
        </div>
        <p class="section-desc">从预设模板快速填充，或选择「自定义模板」手动填写全部参数</p>

        <!-- 模板搜索 -->
        <div class="template-search-bar">
          <el-input
            v-model="templateSearchKeyword"
            placeholder="搜索服务商名称、服务商代码、模型名称、模型版本"
            clearable
            :prefix-icon="Search"
            @keyup.enter.prevent
          />
          <span v-if="templateSearchKeyword" class="template-search-hint">
            当前 Tab 匹配 {{ filteredTemplates.length }} 个模板
          </span>
        </div>
        
        <!-- 按服务商分 Tab 列出全部模型模板 -->
        <div class="template-filter">
          <el-tabs v-model="templateProviderTab" class="template-provider-tabs">
            <el-tab-pane
              v-for="tab in TEMPLATE_PROVIDER_TABS"
              :key="tab.name"
              :name="tab.name"
            >
              <template #label>
                <span class="tab-label">
                  <span v-if="tab.color" class="tab-dot" :style="{ background: tab.color }" />
                  {{ tab.label }}
                  <span class="tab-count">({{ templateTabCount(tab.name) }})</span>
                </span>
              </template>
            </el-tab-pane>
          </el-tabs>
        </div>
        
        <div ref="templateGridRef" class="template-grid">
          <div
            v-if="!filteredTemplates.length"
            class="template-empty"
          >
            {{ templateSearchKeyword ? '未找到匹配的模型模板，请调整关键词或切换 Tab' : '该分类下暂无模型模板' }}
          </div>
          <div
            v-for="tpl in filteredTemplates"
            :key="tpl.id"
            :data-template-id="tpl.id"
            class="template-card"
            :class="{ 
              selected: selectedTemplateId === tpl.id, 
              custom: tpl.isCustom,
              'is-default': isDefaultModel(tpl)
            }"
            @click="selectTemplate(tpl.id)"
          >
            <div class="tpl-header">
              <span
                class="tpl-dot"
                :style="{ background: tpl.color }"
              />
              <span class="tpl-name">{{ tpl.name }}</span>
              <el-icon v-if="selectedTemplateId === tpl.id" class="tpl-check"><CircleCheck /></el-icon>
              <el-tag v-if="isDefaultModel(tpl)" type="warning" size="small" effect="dark" class="default-badge">当前使用</el-tag>
            </div>
            <p class="tpl-desc">{{ tpl.description }}</p>
            <div v-if="!tpl.isCustom" class="tpl-meta">
              <el-tag size="small" effect="plain">{{ tpl.providerCode }}</el-tag>
              <span class="tpl-model">{{ tpl.modelName }}</span>
              <el-tag v-if="tpl.modelVersion && tpl.modelVersion !== 'latest'" size="small" type="info" effect="plain">{{ tpl.modelVersion }}</el-tag>
            </div>
            <div v-else class="tpl-meta">
              <el-tag size="small" type="info" effect="plain">自由配置</el-tag>
            </div>
          </div>
        </div>
      </div>

      <el-divider />

      <el-form :model="configForm" :rules="configRules" ref="configFormRef" label-width="110px">
        <el-tabs v-model="activeFormTab" class="form-tabs">
          <el-tab-pane label="基本信息" name="basic">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="服务商代码" prop="providerCode">
                  <el-input v-model="configForm.providerCode" placeholder="如：openai, qwen, custom" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="服务商名称" prop="providerName">
                  <el-input v-model="configForm.providerName" placeholder="如：OpenAI, 阿里云通义千问" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="模型名称" prop="modelName">
                  <el-input v-model="configForm.modelName" placeholder="如：gpt-4-turbo, qwen-max" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="模型版本">
                  <el-input v-model="configForm.modelVersion" placeholder="如：0125, latest" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="模型描述">
              <el-input
                v-model="configForm.description"
                type="textarea"
                :rows="3"
                placeholder="模型的功能特点说明"
              />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="API 配置" name="api">
            <el-form-item :label="isAvatarConfig ? 'Access Key (AK)' : 'API 密钥'" prop="apiKey">
              <el-input
                v-model="configForm.apiKey"
                type="password"
                show-password
                :placeholder="isAvatarConfig ? '请输入供应商 Access Key' : '请输入 API Key'"
              />
            </el-form-item>
            <el-form-item :label="isAvatarConfig ? 'Secret Key (SK)' : 'API 密钥 2'">
              <el-input
                v-model="configForm.apiSecret"
                type="password"
                show-password
                :placeholder="getSecretKeyPlaceholder(isAvatarConfig, configForm.apiSecretConfigured)"
              />
              <div v-if="configForm.apiSecretConfigured && !configForm.apiSecret" class="credential-saved-hint">
                Secret Key 已安全保存，只有重新输入时才会更新
              </div>
            </el-form-item>
            <el-form-item label="API 端点">
              <el-input v-model="configForm.apiEndpoint" placeholder="如：https://api.openai.com/v1" />
            </el-form-item>
            <el-form-item v-if="providerApplicationUrl" label="业务入口">
              <el-link :href="providerApplicationUrl" target="_blank" type="primary" :underline="false">
                前往 {{ configForm.providerName || '服务商' }} 业务控制台
              </el-link>
            </el-form-item>
            <el-form-item label="应用 ID">
              <el-input v-model="configForm.appId" placeholder="部分服务商需要（可选）" />
            </el-form-item>
            <template v-if="isAvatarConfig">
              <el-form-item label="Region">
                <el-input v-model="configForm.region" placeholder="如：cn-north-1" />
              </el-form-item>
              <el-form-item label="默认形象 ID">
                <el-input v-model="avatarOptions.avatarId" placeholder="视频工作台未指定形象时使用" />
              </el-form-item>
              <el-form-item label="默认音色 ID">
                <el-input v-model="avatarOptions.voiceId" placeholder="视频工作台未指定音色时使用" />
              </el-form-item>
              <el-form-item v-if="configForm.providerCode === 'aliyun-avatar'" label="播报模板 ID" required>
                <el-input v-model="avatarOptions.templateId" placeholder="在阿里云数字人视频创作控制台复制模板 ID" />
              </el-form-item>
              <el-form-item v-if="configForm.providerCode === 'aliyun-avatar'" label="音频变量名">
                <el-input v-model="avatarOptions.audioVariableName" placeholder="默认：slide_audio" />
              </el-form-item>
              <el-form-item v-if="configForm.providerCode === 'aliyun-avatar'" label="互动 License">
                <el-input v-model="avatarOptions.chatLicense" type="password" show-password placeholder="互动会话未单独填写时使用" autocomplete="new-password" />
                <div v-if="avatarOptions.chatLicenseConfigured && !avatarOptions.chatLicense" class="credential-saved-hint">互动 License 已安全保存；留空则保持原值</div>
                <div v-else class="credential-saved-hint">仅保存到服务端扩展配置；创建互动会话时不会回显或写入任务日志</div>
              </el-form-item>
              <el-form-item label="回调地址">
                <el-input v-model="avatarOptions.callbackUrl" placeholder="https://域名/geo/public/video-callbacks/供应商代码" />
              </el-form-item>
              <el-alert type="info" :closable="false" show-icon title="回调令牌由每个视频任务自动生成，无需在这里填写" />
            </template>
            <el-form-item v-if="configForm.providerCode === 'dify'" label="扩展配置">
              <el-input
                v-model="configForm.configJson"
                type="textarea"
                :rows="4"
                placeholder='如：{"appType":"workflow","fileInputKey":"file","descriptionInputKey":"description"}'
              />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="模型参数" name="params">
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="最大 Token">
                  <el-input-number v-model="configForm.maxTokens" :min="1" :max="200000" :step="1000" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="温度参数">
                  <el-input-number v-model="configForm.temperature" :min="0" :max="2" :precision="2" :step="0.1" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="Top P">
                  <el-input-number v-model="configForm.topP" :min="0" :max="1" :precision="2" :step="0.1" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="超时(秒)">
                  <el-input-number v-model="configForm.timeout" :min="1" :max="300" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="重试次数">
              <el-input-number v-model="configForm.retryCount" :min="0" :max="10" style="width: 200px" />
            </el-form-item>
          </el-tab-pane>

          <el-tab-pane label="功能与成本" name="features">
            <el-row :gutter="16">
              <el-col :span="8">
                <el-form-item label="流式输出">
                  <el-switch v-model="configForm.supportStream" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="函数调用">
                  <el-switch v-model="configForm.supportFunction" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item label="视觉理解">
                  <el-switch v-model="configForm.supportVision" :active-value="1" :inactive-value="0" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="输入成本/1K">
                  <el-input-number v-model="configForm.costPer1kInput" :min="0" :precision="6" :step="0.0001" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="输出成本/1K">
                  <el-input-number v-model="configForm.costPer1kOutput" :min="0" :precision="6" :step="0.0001" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-row :gutter="16">
              <el-col :span="12">
                <el-form-item label="优先级">
                  <el-input-number v-model="configForm.priority" :min="0" :max="100" style="width: 100%" />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="排序">
                  <el-input-number v-model="configForm.sortOrder" :min="0" :max="9999" style="width: 100%" />
                </el-form-item>
              </el-col>
            </el-row>
            <el-form-item label="启用状态">
              <el-switch v-model="configForm.isEnabled" :active-value="1" :inactive-value="0" />
            </el-form-item>
          </el-tab-pane>
        </el-tabs>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saveLoading" @click="saveConfig">保存配置</el-button>
      </template>
    </el-dialog>

    <!-- 品牌凭证对话框 -->
    <el-dialog
      v-model="credentialDialogVisible"
      :title="`配置 ${credentialForm.providerName || '品牌'} 凭证`"
      width="560px"
      class="credential-dialog"
      destroy-on-close
      @close="resetCredentialForm"
    >
      <el-alert
        type="info"
        :closable="false"
        show-icon
        class="credential-tip"
        title="该品牌下的所有模型共享同一把密钥"
        :description="`保存后将下发到 ${credentialForm.models.length} 个模型`"
      />
      <el-form :model="credentialForm" :rules="credentialRules" ref="credentialFormRef" label-width="120px" class="credential-form">
        <el-form-item label="服务商">
          <el-input :model-value="credentialForm.providerName" disabled />
        </el-form-item>
        <el-form-item label="API 端点">
          <el-input v-model="credentialForm.apiEndpoint" placeholder="如：https://api.openai.com/v1" />
        </el-form-item>
        <el-form-item :label="credentialForm.isAvatar ? 'Access Key (AK)' : 'API 密钥'" prop="apiKey">
          <el-input
            v-model="credentialForm.apiKey"
            type="password"
            show-password
            :placeholder="getSecretKeyPlaceholder(credentialForm.isAvatar, credentialForm.apiKeyConfigured)"
          />
          <div v-if="credentialForm.apiKeyConfigured && !credentialForm.apiKey" class="credential-saved-hint">
            密钥已安全保存，留空则保持原值
          </div>
        </el-form-item>
        <el-form-item :label="credentialForm.isAvatar ? 'Secret Key (SK)' : 'API 密钥2 / Secret'">
          <el-input
            v-model="credentialForm.apiSecret"
            type="password"
            show-password
            :placeholder="getSecretKeyPlaceholder(credentialForm.isAvatar, credentialForm.apiSecretConfigured)"
          />
          <div v-if="credentialForm.apiSecretConfigured && !credentialForm.apiSecret" class="credential-saved-hint">
            Secret 已安全保存，留空则保持原值
          </div>
        </el-form-item>
        <el-form-item label="应用 ID">
          <el-input v-model="credentialForm.appId" placeholder="部分服务商需要（可选）" />
        </el-form-item>
        <template v-if="credentialForm.isAvatar">
          <el-form-item label="Region">
            <el-input v-model="credentialForm.region" placeholder="如：cn-north-1" />
          </el-form-item>
          <el-form-item label="默认形象 ID">
            <el-input v-model="avatarOptions.avatarId" placeholder="视频工作台未指定形象时使用" />
          </el-form-item>
          <el-form-item label="默认音色 ID">
            <el-input v-model="avatarOptions.voiceId" placeholder="视频工作台未指定音色时使用" />
          </el-form-item>
          <el-form-item v-if="credentialForm.providerCode === 'aliyun-avatar'" label="播报模板 ID">
            <el-input v-model="avatarOptions.templateId" placeholder="在阿里云数字人视频创作控制台复制模板 ID" />
          </el-form-item>
          <el-form-item v-if="credentialForm.providerCode === 'aliyun-avatar'" label="音频变量名">
            <el-input v-model="avatarOptions.audioVariableName" placeholder="默认：slide_audio" />
          </el-form-item>
          <el-form-item v-if="credentialForm.providerCode === 'aliyun-avatar'" label="互动 License">
            <el-input v-model="avatarOptions.chatLicense" type="password" show-password placeholder="互动会话未单独填写时使用" autocomplete="new-password" />
            <div v-if="avatarOptions.chatLicenseConfigured && !avatarOptions.chatLicense" class="credential-saved-hint">互动 License 已安全保存；留空则保持原值</div>
          </el-form-item>
          <el-form-item label="回调地址">
            <el-input v-model="avatarOptions.callbackUrl" placeholder="https://域名/geo/public/video-callbacks/供应商代码" />
          </el-form-item>
        </template>
        <el-form-item label="启用状态">
          <el-switch v-model="credentialForm.isEnabled" :active-value="1" :inactive-value="0" />
          <span class="credential-switch-hint">下发到该品牌全部模型</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="credentialDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="credentialSaveLoading" @click="saveProviderCredential">保存并下发</el-button>
      </template>
    </el-dialog>

    <!-- 默认模型分配概览 -->
    <el-dialog
      v-model="defaultModelDialogVisible"
      title="默认模型分配"
      width="680px"
      destroy-on-close
    >
      <el-alert
        type="info"
        show-icon
        :closable="false"
        title="各能力分类当前使用的已启用模型"
        description="默认模型按功能类型各自独立设置：优先取该分类下显式设为默认的模型，否则取该分类下已启用且优先级最高的模型。修改默认请在品牌卡片上选择模型后点击「设为默认」。"
        class="default-model-tip"
      />
      <el-table
        :data="categoryDefaultRows"
        stripe
        max-height="420"
        class="default-model-table"
      >
        <el-table-column label="能力分类" prop="label" width="120" />
        <el-table-column label="当前模型" min-width="200">
          <template #default="{ row }">
            <template v-if="row.configured">
              <code class="model-name">{{ row.modelName }}</code>
              <el-tag
                v-if="row.isDefault"
                type="warning"
                size="small"
                effect="dark"
                class="default-tag"
              >默认</el-tag>
            </template>
            <span v-else class="text-muted">未配置</span>
          </template>
        </el-table-column>
        <el-table-column label="服务商" prop="providerName" min-width="140">
          <template #default="{ row }">
            {{ row.configured ? row.providerName : '—' }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="defaultModelDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Refresh, Search, RefreshLeft, Connection, CircleCheck, Star, Grid, MagicStick,
  Document, View, Picture, Box, Reading, VideoCamera, Microphone, Headset,
  Share, Warning, OfficeBuilding, UserFilled, ArrowRight, InfoFilled,
  Key, Delete, CircleClose, Link
} from '@element-plus/icons-vue'
import request from '@/utils/request'
import {
  AI_PROVIDER_TEMPLATES,
  CUSTOM_TEMPLATE_ID,
  TEMPLATE_PROVIDER_TABS,
  filterTemplatesByProviderTab,
  filterDialogTemplates,
  countTemplatesForTab,
  buildGroupedModelCatalog,
  buildProviderCredentialGroups,
  buildCategoryNav,
  buildCategoryDefaultAssignments,
  inferModelCategory,
  matchTemplateByConfig,
  applyTemplateToForm
} from '@/constants/aiProviderTemplates'
import { parseProviderOptions, updateProviderOptions } from '@/constants/aiProviderConfigJson'
import { focusTemplateCard, getProviderApplicationUrl } from '@/constants/aiProviderExperience'
import { buildCredentialPayload, getSecretKeyPlaceholder } from '@/constants/aiCredentialState'

const categoryIconMap = {
  all: Grid,
  text: Document,
  vision: View,
  image: Picture,
  '3d': Box,
  ocr: Reading,
  video: VideoCamera,
  asr: Microphone,
  tts: Headset,
  embedding: Share,
  risk: Warning,
  industry: OfficeBuilding,
  avatar: UserFilled
}

const activeFormTab = ref('basic')
const activeCategory = ref('all')
const searchKeyword = ref('')
const searchEnabled = ref(null)
const tableLoading = ref(false)
const selectedTemplateId = ref(CUSTOM_TEMPLATE_ID)
const templateProviderTab = ref('all')
const templateSearchKeyword = ref('')
const templateGridRef = ref(null)
const currentDefaultModel = ref('')
const defaultModelNames = ref([])
const expandedProviders = ref(new Set())

const defaultModelDialogVisible = ref(false)
const categoryDefaultRows = ref([])

const aiConfigList = ref([])
const allConfigsCache = ref([])
const pagination = reactive({ page: 1, limit: 500, total: 0 })

const categoryNavItems = computed(() =>
  buildCategoryNav({
    templates: AI_PROVIDER_TEMPLATES,
    configs: allConfigsCache.value,
    keyword: searchKeyword.value,
    enabledFilter: searchEnabled.value
  })
)

const providerGroups = computed(() =>
  buildProviderCredentialGroups({
    templates: AI_PROVIDER_TEMPLATES,
    configs: allConfigsCache.value,
    category: activeCategory.value,
    keyword: searchKeyword.value,
    enabledFilter: searchEnabled.value
  })
)

const groupDefaultSelection = reactive({})

const configuredModels = (group) => group.models.filter(m => m.configured && m.config?.id)

const stats = reactive({ total: 0, enabled: 0, providers: 0 })

const dialogVisible = ref(false)
const dialogTitle = computed(() => (configForm.id ? '编辑 AI 配置' : '新增 AI 配置'))
const configFormRef = ref()
const saveLoading = ref(false)

const configForm = reactive({
  id: null,
  providerCode: '',
  providerName: '',
  modelName: '',
  modelVersion: '',
  description: '',
  apiKey: '',
  apiSecret: '',
  apiKeyConfigured: false,
  apiSecretConfigured: false,
  apiEndpoint: '',
  appId: '',
  region: '',
  configJson: '',
  maxTokens: 2000,
  temperature: 0.7,
  topP: 1,
  timeout: 60,
  retryCount: 3,
  supportStream: 1,
  supportFunction: 0,
  supportVision: 0,
  costPer1kInput: 0,
  costPer1kOutput: 0,
  priority: 1,
  sortOrder: 0,
  isEnabled: 1
})

const avatarOptions = reactive({ avatarId: '', voiceId: '', callbackUrl: '', projectId: '', templateId: '', audioVariableName: '', chatLicense: '', chatLicenseConfigured: false })
const isAvatarConfig = computed(() => /avatar|digital[- ]?human/i.test(`${configForm.providerCode} ${configForm.modelName}`))
const providerApplicationUrl = computed(() => getProviderApplicationUrl(configForm.providerCode))

const focusSelectedTemplate = () => {
  focusTemplateCard(templateGridRef.value, selectedTemplateId.value)
}

const syncAvatarOptions = () => {
  const options = parseProviderOptions(configForm.configJson)
  Object.assign(avatarOptions, {
    avatarId: options.avatarId || '',
    voiceId: options.voiceId || '',
    callbackUrl: options.callbackUrl || '',
    projectId: options.projectId || '',
    templateId: options.templateId || '',
    audioVariableName: options.audioVariableName || '',
    chatLicense: options.chatLicense || ''
  })
}

const configRules = {
  providerCode: [{ required: true, message: '请输入服务商代码', trigger: 'blur' }],
  providerName: [{ required: true, message: '请输入服务商名称', trigger: 'blur' }],
  modelName: [{ required: true, message: '请输入模型名称', trigger: 'blur' }],
  apiKey: [{ required: true, message: '请输入 API 密钥', trigger: 'blur' }]
}

// 模板按服务商 Tab 筛选
const filteredTemplates = computed(() =>
  filterDialogTemplates(AI_PROVIDER_TEMPLATES, templateProviderTab.value, templateSearchKeyword.value)
)

const templateTabCount = (tabName) =>
  countTemplatesForTab(AI_PROVIDER_TEMPLATES, tabName, templateSearchKeyword.value)

// 判断是否为当前默认模型（每种功能类型可各自有一个默认，故可能多个）
const isDefaultModel = (template) => {
  if (!template.modelName) return false
  return defaultModelNames.value.includes(template.modelName)
}

const updateStats = (list) => {
  const catalog = buildGroupedModelCatalog({
    templates: AI_PROVIDER_TEMPLATES,
    configs: list,
    category: 'all',
    keyword: '',
    enabledFilter: null
  })
  const items = catalog.flatMap(g => g.items || [])
  stats.total = items.length
  stats.enabled = items.filter(i => i.config?.isEnabled === 1).length
  stats.providers = catalog.length
}

const loadStatsFromEnabled = async () => {
  try {
    const res = await request.post('/web/base/aiConfig/enabled')
    const enabledList = res?.data || []
    stats.enabled = enabledList.length
  } catch {
    /* 统计失败不影响主流程 */
  }
}

const loadAiConfigList = async () => {
  try {
    tableLoading.value = true
    const res = await request.post('/web/base/aiConfig/list', {
      keyword: searchKeyword.value,
      isEnabled: searchEnabled.value,
      page: pagination.page,
      limit: pagination.limit
    })
    if (res.code === 1 || res.code === 200) {
      const list = res.data?.list || res.data?.records || []
      allConfigsCache.value = list
      aiConfigList.value = list
      pagination.total = res.data?.totalCount || res.data?.total || list.length
      updateStats(list)
      
      const defaultItems = list.filter(i => i.isDefault === 1)
      defaultModelNames.value = defaultItems.map(i => i.modelName)
      // 头部“当前使用”标签取主默认（优先文本对话类，否则第一条），与后端 getDefaultConfig 一致
      const textDefault = defaultItems.find(i => inferModelCategory(i) === 'text')
      const primaryDefault = textDefault || defaultItems[0]
      currentDefaultModel.value = primaryDefault ? primaryDefault.modelName : ''
    }
  } catch (error) {
    console.error('加载 AI 配置列表失败:', error)
    ElMessage.error('加载列表失败')
  } finally {
    tableLoading.value = false
  }
}

const switchCategory = (key) => {
  activeCategory.value = key
}

const searchAiConfig = () => {
  pagination.page = 1
  loadAiConfigList()
}

const resetSearch = () => {
  searchKeyword.value = ''
  searchEnabled.value = null
  pagination.page = 1
  loadAiConfigList()
}

const refreshList = () => {
  loadAiConfigList()
  loadStatsFromEnabled()
}

const selectTemplate = (templateId) => {
  selectedTemplateId.value = templateId
  const tpl = AI_PROVIDER_TEMPLATES.find(t => t.id === templateId)
  if (!tpl) return
  syncProviderTabForTemplate(tpl)
  const keepApiKey = !!configForm.id
  Object.assign(configForm, applyTemplateToForm(tpl, keepApiKey, configForm.apiKey))
  syncAvatarOptions()
  avatarOptions.chatLicenseConfigured = false
}

const syncProviderTabForTemplate = (tpl) => {
  if (!tpl) return
  if (tpl.isCustom) {
    templateProviderTab.value = 'custom'
    return
  }
  const name = tpl.modelName || ''
  if (tpl.providerCode === 'deepseek' || /deepseek/i.test(name)) {
    templateProviderTab.value = 'deepseek'
  } else if (tpl.providerCode === 'kimi' || /kimi|moonshot/i.test(name)) {
    templateProviderTab.value = 'kimi'
  } else if (tpl.providerCode === 'chatglm' || /^glm/i.test(name)) {
    templateProviderTab.value = 'glm'
  } else if (tpl.providerCode === 'qwen') {
    templateProviderTab.value = 'qwen'
  } else if (TEMPLATE_PROVIDER_TABS.some(t => t.name === tpl.providerCode)) {
    templateProviderTab.value = tpl.providerCode
  } else {
    templateProviderTab.value = 'all'
  }
}

const showAddDialog = () => {
  resetForm()
  templateProviderTab.value = 'custom'
  selectTemplate(CUSTOM_TEMPLATE_ID)
  dialogVisible.value = true
}

const openDefaultModelDialog = async () => {
  try {
    let configs = allConfigsCache.value
    if (!configs.length) {
      const listRes = await request.post('/web/base/aiConfig/list', { page: 1, limit: 500 })
      if (listRes.code === 1 || listRes.code === 200) {
        configs = listRes.data?.list || listRes.data?.records || []
      }
    }
    categoryDefaultRows.value = buildCategoryDefaultAssignments({
      templates: AI_PROVIDER_TEMPLATES,
      configs
    })
    defaultModelDialogVisible.value = true
  } catch (error) {
    console.error('加载默认模型分配失败:', error)
    ElMessage.error('加载默认模型分配失败')
  }
}

const editConfig = (config) => {
  Object.assign(configForm, {
    id: config.id,
    providerCode: config.providerCode,
    providerName: config.providerName,
    modelName: config.modelName,
    modelVersion: config.modelVersion,
    description: config.description,
    apiKey: config.apiKey || '',
    apiSecret: '',
    apiKeyConfigured: !!config.apiKeyConfigured || !!config.apiKey,
    apiSecretConfigured: !!config.apiSecretConfigured,
    apiEndpoint: config.apiEndpoint,
    appId: config.appId,
    region: config.region || '',
    configJson: config.configJson || '',
    maxTokens: config.maxTokens,
    temperature: config.temperature,
    topP: config.topP,
    timeout: config.timeout,
    retryCount: config.retryCount,
    supportStream: config.supportStream,
    supportFunction: config.supportFunction,
    supportVision: config.supportVision,
    costPer1kInput: config.costPer1kInput,
    costPer1kOutput: config.costPer1kOutput,
    priority: config.priority,
    sortOrder: config.sortOrder,
    isEnabled: config.isEnabled
  })
  syncAvatarOptions()
  selectedTemplateId.value = matchTemplateByConfig(config)
  const matchedTpl = AI_PROVIDER_TEMPLATES.find(t => t.id === selectedTemplateId.value)
  if (matchedTpl) syncProviderTabForTemplate(matchedTpl)
  dialogVisible.value = true
}

const saveConfig = async () => {
  if (!configFormRef.value) return
  await configFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      saveLoading.value = true
      if (isAvatarConfig.value) {
        const { chatLicenseConfigured, ...providerOptions } = avatarOptions
        configForm.configJson = updateProviderOptions(configForm.configJson, providerOptions)
      }
      const res = await request.post('/web/base/aiConfig/save', buildCredentialPayload(configForm))
      if (res.code === 1 || res.code === 200) {
        ElMessage.success(configForm.id ? '更新成功' : '添加成功')
        dialogVisible.value = false
        loadAiConfigList()
        loadStatsFromEnabled()
      } else {
        ElMessage.error(res.message || '操作失败')
      }
    } catch (error) {
      console.error('保存 AI 配置失败:', error)
      ElMessage.error('操作失败')
    } finally {
      saveLoading.value = false
    }
  })
}

const deleteConfig = (config) => {
  ElMessageBox.confirm('确定要删除该 AI 配置吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await request.post('/web/base/aiConfig/delete', null, { params: { id: config.id } })
      if (res.code === 1 || res.code === 200) {
        ElMessage.success('删除成功')
        loadAiConfigList()
        loadStatsFromEnabled()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除 AI 配置失败:', error)
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

const toggleEnabled = async (config) => {
  try {
    const res = await request.post('/web/base/aiConfig/toggle', null, {
      params: { id: config.id, isEnabled: config.isEnabled }
    })
    if (res.code === 1 || res.code === 200) {
      ElMessage.success(config.isEnabled === 1 ? '已启用' : '已禁用')
      loadStatsFromEnabled()
    } else {
      ElMessage.error(res.message || '操作失败')
      config.isEnabled = config.isEnabled === 1 ? 0 : 1
    }
  } catch (error) {
    console.error('切换状态失败:', error)
    ElMessage.error('操作失败')
    config.isEnabled = config.isEnabled === 1 ? 0 : 1
  }
}

const setAsDefault = async (config) => {
  try {
    const res = await request.post('/web/base/aiConfig/setDefault', null, { params: { id: config.id } })
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('已设置为默认配置')
      // 更新当前默认显示
      currentDefaultModel.value = config.modelName
      loadAiConfigList()
      loadStatsFromEnabled()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    console.error('设置默认失败:', error)
    ElMessage.error('操作失败')
  }
}

const testConnection = async (config) => {
  const loadingMsg = ElMessage({ message: '正在测试连接...', type: 'info', duration: 0 })
  try {
    const res = await request.post('/web/base/aiConfig/test', null, { params: { id: config.id } })
    loadingMsg.close()
    if (res.code === 1 || res.code === 200) {
      ElMessage.success('连接测试成功')
    } else {
      ElMessage.error(res.message || '连接测试失败')
    }
  } catch (error) {
    loadingMsg.close()
    console.error('测试连接失败:', error)
    ElMessage.error('连接测试失败')
  }
}

const resetForm = () => {
  Object.assign(configForm, {
    id: null,
    providerCode: '',
    providerName: '',
    modelName: '',
    modelVersion: '',
    description: '',
    apiKey: '',
    apiSecret: '',
    apiKeyConfigured: false,
    apiSecretConfigured: false,
    apiEndpoint: '',
    appId: '',
    region: '',
    configJson: '',
    maxTokens: 2000,
    temperature: 0.7,
    topP: 1,
    timeout: 60,
    retryCount: 3,
    supportStream: 1,
    supportFunction: 0,
    supportVision: 0,
    costPer1kInput: 0,
    costPer1kOutput: 0,
    priority: 1,
    sortOrder: 0,
    isEnabled: 1
  })
  Object.assign(avatarOptions, { avatarId: '', voiceId: '', callbackUrl: '', projectId: '', templateId: '', audioVariableName: '', chatLicense: '', chatLicenseConfigured: false })
  selectedTemplateId.value = CUSTOM_TEMPLATE_ID
  templateProviderTab.value = 'all'
  templateSearchKeyword.value = ''
  activeFormTab.value = 'basic'
  configFormRef.value?.clearValidate()
}

const toggleProvider = (key) => {
  const newSet = new Set(expandedProviders.value)
  if (newSet.has(key)) {
    newSet.delete(key)
  } else {
    newSet.add(key)
  }
  expandedProviders.value = newSet
}

// ========== 品牌凭证对话框 ==========
const credentialDialogVisible = ref(false)
const credentialSaveLoading = ref(false)
const credentialFormRef = ref()

const credentialForm = reactive({
  groupKey: '',
  providerCode: '',
  providerName: '',
  apiEndpoint: '',
  apiKey: '',
  apiSecret: '',
  apiKeyConfigured: false,
  apiSecretConfigured: false,
  region: '',
  appId: '',
  configJson: '',
  isEnabled: 1,
  isAvatar: false,
  models: []
})

const credentialRules = {
  apiKey: [{
    validator: (rule, value, callback) => {
      if (credentialForm.apiKeyConfigured || (value && value.trim())) {
        callback()
      } else {
        callback(new Error(credentialForm.isAvatar ? '请输入 Access Key' : '请输入 API 密钥'))
      }
    },
    trigger: 'blur'
  }]
}

const resetCredentialForm = () => {
  Object.assign(credentialForm, {
    groupKey: '', providerCode: '', providerName: '', apiEndpoint: '',
    apiKey: '', apiSecret: '', apiKeyConfigured: false, apiSecretConfigured: false,
    region: '', appId: '', configJson: '', isEnabled: 1, isAvatar: false, models: []
  })
  Object.assign(avatarOptions, { avatarId: '', voiceId: '', callbackUrl: '', projectId: '', templateId: '', audioVariableName: '', chatLicense: '', chatLicenseConfigured: false })
  credentialFormRef.value?.clearValidate()
}

const openCredentialDialog = (group) => {
  resetCredentialForm()
  const rep = group.representativeConfig || {}
  credentialForm.groupKey = group.key
  credentialForm.providerCode = group.providerCode
  credentialForm.providerName = group.label
  credentialForm.apiEndpoint = group.apiEndpoint || rep.apiEndpoint || ''
  credentialForm.apiKeyConfigured = !!(rep.apiKey || rep.apiKeyConfigured)
  credentialForm.apiSecretConfigured = !!rep.apiSecretConfigured
  credentialForm.region = rep.region || ''
  credentialForm.appId = rep.appId || ''
  credentialForm.configJson = rep.configJson || ''
  credentialForm.isEnabled = rep.isEnabled != null ? rep.isEnabled : 1
  credentialForm.isAvatar = group.isAvatar
  credentialForm.models = group.models
  if (group.isAvatar) {
    const options = parseProviderOptions(rep.configJson)
    Object.assign(avatarOptions, {
      avatarId: options.avatarId || '',
      voiceId: options.voiceId || '',
      callbackUrl: options.callbackUrl || '',
      projectId: options.projectId || '',
      templateId: options.templateId || '',
      audioVariableName: options.audioVariableName || '',
      chatLicense: '',
      chatLicenseConfigured: !!rep.chatLicenseConfigured
    })
  }
  credentialDialogVisible.value = true
}

// 从模板派生每个模型的完整参数（后端按行 upsert 时初始化模型模板参数）
const buildModelPayload = (models) => models.map((m) => {
  const tpl = m.templateId ? AI_PROVIDER_TEMPLATES.find(t => t.id === m.templateId) : null
  const cfg = m.config || {}
  return {
    providerCode: m.providerCode,
    providerName: m.providerName,
    modelName: m.modelName,
    modelVersion: m.modelVersion || tpl?.modelVersion || '',
    description: m.description || tpl?.description || '',
    apiEndpoint: m.apiEndpoint || tpl?.apiEndpoint || '',
    maxTokens: cfg.maxTokens ?? tpl?.maxTokens,
    temperature: cfg.temperature ?? tpl?.temperature,
    topP: cfg.topP ?? tpl?.topP,
    timeout: cfg.timeout ?? tpl?.timeout,
    retryTimes: cfg.retryTimes ?? tpl?.retryCount,
    supportStream: cfg.supportStream ?? tpl?.supportStream,
    supportFunction: cfg.supportFunction ?? tpl?.supportFunction,
    supportVision: cfg.supportVision ?? tpl?.supportVision,
    costPer1kInput: cfg.costPer1kInput ?? tpl?.costPer1kInput,
    costPer1kOutput: cfg.costPer1kOutput ?? tpl?.costPer1kOutput,
    priority: cfg.priority ?? tpl?.priority,
    sortOrder: cfg.sortOrder ?? tpl?.sortOrder,
    configJson: cfg.configJson || ''
  }
})

const saveProviderCredential = async () => {
  if (!credentialFormRef.value) return
  await credentialFormRef.value.validate(async (valid) => {
    if (!valid) return
    try {
      credentialSaveLoading.value = true
      // 数字人扩展项写入品牌级 configJson；非数字人不下发品牌级 configJson（保留各模型原值）
      let brandConfigJson = ''
      if (credentialForm.isAvatar) {
        const { chatLicenseConfigured, ...providerOptions } = avatarOptions
        brandConfigJson = updateProviderOptions(credentialForm.configJson || '', providerOptions)
      }
      const payload = {
        providerCode: credentialForm.providerCode,
        apiEndpoint: credentialForm.apiEndpoint,
        region: credentialForm.region,
        appId: credentialForm.appId,
        configJson: brandConfigJson,
        isEnabled: credentialForm.isEnabled,
        models: buildModelPayload(credentialForm.models)
      }
      // 密钥留空则不下发（后端保留原值）
      if (credentialForm.apiKey && credentialForm.apiKey.trim()) {
        payload.apiKey = credentialForm.apiKey
      }
      if (credentialForm.apiSecret && credentialForm.apiSecret.trim()) {
        payload.apiSecret = credentialForm.apiSecret
      }
      const res = await request.post('/web/base/aiConfig/saveProvider', payload)
      if (res.code === 1 || res.code === 200) {
        ElMessage.success('凭证已下发到该品牌全部模型')
        credentialDialogVisible.value = false
        loadAiConfigList()
        loadStatsFromEnabled()
      } else {
        ElMessage.error(res.message || '保存失败')
      }
    } catch (error) {
      console.error('保存品牌凭证失败:', error)
      ElMessage.error('保存失败')
    } finally {
      credentialSaveLoading.value = false
    }
  })
}

const setGroupEnabled = async (group, isEnabled) => {
  const targets = configuredModels(group)
  if (!targets.length) return
  try {
    await Promise.all(targets.map(m =>
      request.post('/web/base/aiConfig/toggle', null, { params: { id: m.config.id, isEnabled } })
    ))
    ElMessage.success(isEnabled === 1 ? '已启用该品牌全部模型' : '已停用该品牌全部模型')
    loadAiConfigList()
    loadStatsFromEnabled()
  } catch (error) {
    console.error('批量切换状态失败:', error)
    ElMessage.error('操作失败')
  }
}

const clearGroupConfig = (group) => {
  const targets = configuredModels(group)
  if (!targets.length) return
  ElMessageBox.confirm(`确定要清除「${group.label}」下 ${targets.length} 个模型的配置吗？此操作不可恢复。`, '清除配置', {
    confirmButtonText: '确定清除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await Promise.all(targets.map(m =>
        request.post('/web/base/aiConfig/delete', null, { params: { id: m.config.id } })
      ))
      ElMessage.success('已清除该品牌配置')
      loadAiConfigList()
      loadStatsFromEnabled()
    } catch (error) {
      console.error('清除配置失败:', error)
      ElMessage.error('清除失败')
    }
  }).catch(() => {})
}

const setGroupDefault = async (group) => {
  const sel = groupDefaultSelection[group.key]
  if (!sel) return
  const target = group.models.find(m => `${m.providerCode}::${m.modelName}` === sel)
  if (!target) return
  // 已入库：直接设为默认
  if (target.config?.id) {
    setAsDefault(target.config)
    return
  }
  // 未入库：先随品牌共享密钥落库（后端会继承同品牌已有密钥），再设为默认
  try {
    const rep = group.representativeConfig || {}
    const payload = {
      providerCode: group.providerCode,
      apiEndpoint: group.apiEndpoint || '',
      region: rep.region || '',
      appId: rep.appId || '',
      configJson: '',
      isEnabled: 1,
      models: buildModelPayload([target])
    }
    const res = await request.post('/web/base/aiConfig/saveProvider', payload)
    if (!(res.code === 1 || res.code === 200)) {
      ElMessage.error(res.message || '入库失败')
      return
    }
    await loadAiConfigList()
    const saved = allConfigsCache.value.find(c => c.providerCode === target.providerCode && c.modelName === target.modelName)
    if (saved?.id) {
      setAsDefault({ id: saved.id, modelName: saved.modelName })
    } else {
      ElMessage.error('入库成功但未找到记录，请刷新后重试')
    }
  } catch (error) {
    console.error('设置默认模型失败:', error)
    ElMessage.error('操作失败')
  }
}

const maskApiKey = (key) => {
  if (!key || key.length <= 8) return '***'
  return key.substring(0, 4) + '***' + key.substring(key.length - 4)
}

onMounted(() => {
  loadAiConfigList()
  loadStatsFromEnabled()
})
</script>

<style scoped lang="scss">
.ai-config-page {
  min-height: 100%;
  background: #f0f2f5;
  padding: 16px 0 24px;
}

.stats-row {
  padding: 0 20px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 10px;
  padding: 18px 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  margin-bottom: 12px;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }

  .stat-icon {
    width: 44px;
    height: 44px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 22px;
    flex-shrink: 0;
  }

  .stat-value {
    font-size: 26px;
    font-weight: 700;
    color: #303133;
    line-height: 1.2;

    &.stat-value-sm {
      font-size: 15px;
      font-weight: 600;
    }
  }

  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-top: 2px;
  }
}

.stat-total .stat-icon { background: #ecf5ff; color: #409eff; }
.stat-enabled .stat-icon { background: #f0f9eb; color: #67c23a; }
.stat-default .stat-icon { background: #fdf6ec; color: #e6a23c; }
.stat-providers .stat-icon { background: #f4f4f5; color: #909399; }

.stat-card-clickable {
  cursor: pointer;

  &:hover {
    border: 1px solid #e6a23c;
  }

  &:focus-visible {
    outline: 2px solid #e6a23c;
    outline-offset: 2px;
  }
}

.main-card {
  margin: 0 20px;
  border-radius: 10px;
  border: none;

  :deep(.el-card__body) {
    padding: 20px;
  }
}

.catalog-layout {
  display: flex;
  gap: 0;
  min-height: 480px;
}

.category-sidebar {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid #ebeef5;
  padding: 4px 0;
  margin-right: 20px;
}

.category-nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 12px 14px;
  border: none;
  background: transparent;
  cursor: pointer;
  text-align: left;
  transition: background 0.15s;
  border-bottom: 1px solid #f2f3f5;

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: #ecf5ff;

    .category-nav-label {
      color: #409eff;
      font-weight: 600;
    }

    .category-nav-icon {
      color: #409eff;
    }
  }

  &.is-empty:not(.active) {
    .category-nav-label,
    .category-nav-count {
      color: #c0c4cc;
    }
  }
}

.category-nav-icon {
  flex-shrink: 0;
  color: #909399;
  font-size: 16px;
  display: flex;
  align-items: center;
}

.category-nav-label {
  flex: 1;
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
}

.category-nav-count {
  flex-shrink: 0;
  min-width: 28px;
  padding: 0 8px;
  height: 22px;
  line-height: 22px;
  text-align: center;
  font-size: 12px;
  color: #606266;
  background: #f0f2f5;
  border-radius: 11px;
}

.catalog-main {
  flex: 1;
  min-width: 0;
}

.filter-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  align-items: center;

  .filter-input { width: 260px; }
  .filter-select { width: 130px; }

  .filter-actions {
    margin-left: auto;
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }
}

.grouped-catalog {
  min-height: 200px;
}

.provider-card-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.provider-card {
  border: 1px solid #e4e7ed;
  border-radius: 12px;
  background: #fff;
  overflow: hidden;
  transition: border-color 0.2s, box-shadow 0.2s;

  &:hover {
    border-color: #c6e2ff;
    box-shadow: 0 2px 12px rgba(64, 158, 255, 0.08);
  }

  &.has-configured {
    border-color: #e4e7ed;
  }
}

.provider-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  cursor: pointer;
  user-select: none;
  transition: background 0.15s;

  &:hover {
    background: #fafbfc;
  }
}

.provider-card-left {
  display: flex;
  align-items: center;
  gap: 14px;
  min-width: 0;
}

.provider-card-badge {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  color: #fff;
  font-size: 18px;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.provider-card-info {
  min-width: 0;
}

.provider-card-title {
  margin: 0 0 4px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.provider-type-tag {
  font-weight: 400;
}

.provider-card-meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;

  .meta-configured { color: #67c23a; }
  .meta-enabled { color: #409eff; }
}

.provider-card-right {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.expand-icon {
  font-size: 16px;
  color: #909399;
  transition: transform 0.2s;

  &.expanded {
    transform: rotate(90deg);
  }
}

.provider-card-body {
  border-top: 1px solid #ebeef5;
  padding: 16px 20px;
  background: #fafbfc;
}

.configured-model-list {
  .configured-model-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;

    .section-label {
      font-size: 14px;
      font-weight: 600;
      color: #606266;
    }
  }
}

.configured-model-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 10px 14px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  margin-bottom: 8px;
  transition: border-color 0.15s;

  &:hover {
    border-color: #c6e2ff;
  }

  &:last-child {
    margin-bottom: 0;
  }

  &.is-default {
    border-color: #e6a23c;
    background: #fdf6ec;
  }
}

.model-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  min-width: 0;
  flex: 1;

  .model-name-code {
    font-family: 'SF Mono', 'Menlo', monospace;
    font-size: 13px;
    font-weight: 600;
    color: #303133;
  }

  .model-desc-text {
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 280px;
  }
}

.model-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.unconfigured-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;

  .unconfigured-tip {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #909399;
    font-size: 14px;
  }
}

.available-templates-hint {
  margin-top: 12px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
  text-align: center;

  .hint-text {
    font-size: 12px;
    color: #c0c4cc;
  }
}

/* 对话框模板区 */
.template-section {
  .section-title {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 15px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }

  .section-desc {
    font-size: 13px;
    color: #909399;
    margin: 0 0 14px;
  }
}

// 模板筛选器
.template-filter {
  margin-bottom: 14px;
}

.template-search-bar {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;

  .el-input {
    flex: 1;
  }

  .template-search-hint {
    flex-shrink: 0;
    font-size: 12px;
    color: #909399;
    white-space: nowrap;
  }
}

.template-provider-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
  }

  :deep(.el-tabs__nav-wrap) {
    &::after { display: none; }
  }

  :deep(.el-tabs__item) {
    padding: 0 12px;
    height: 36px;
    line-height: 36px;
    font-size: 13px;
  }

  .tab-label {
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .tab-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .tab-count {
    font-size: 11px;
    color: #909399;
  }
}

.template-empty {
  grid-column: 1 / -1;
  text-align: center;
  padding: 24px;
  color: #909399;
  font-size: 13px;
}

.mb-12 { margin-bottom: 12px; }

.default-model-tip {
  margin-bottom: 16px;
}

.default-model-table {
  .model-name {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 13px;
  }

  .default-tag {
    margin-left: 8px;
  }
}

.text-muted {
  color: #909399;
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
  max-height: 360px;
  overflow-y: auto;
  padding-right: 4px;
  margin-top: 12px;
}

.template-card {
  border: 1.5px solid #e4e7ed;
  border-radius: 8px;
  padding: 10px 12px;
  cursor: pointer;
  transition: all 0.2s;
  background: #fafafa;
  position: relative;

  &:hover {
    border-color: #409eff;
    background: #ecf5ff;
  }

  &.selected {
    border-color: #409eff;
    background: #ecf5ff;
    box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
  }
  
  &.is-default {
    border-color: #e6a23c;
    background: #fdf6ec;
    
    &:hover {
      border-color: #e6a23c;
      background: #faecd8;
    }
  }

  &.custom {
    border-style: dashed;

    &.selected {
      border-color: #909399;
      background: #f4f4f5;
      box-shadow: 0 0 0 2px rgba(144, 147, 153, 0.2);
    }
  }

  .tpl-header {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-bottom: 4px;
  }

  .tpl-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  .tpl-name {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    flex: 1;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .tpl-check {
    color: #409eff;
    font-size: 16px;
  }
  
  .default-badge {
    margin-left: 6px;
  }

  .tpl-desc {
    font-size: 11px;
    color: #909399;
    margin: 0 0 6px;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .tpl-meta {
    display: flex;
    align-items: center;
    gap: 6px;

    .tpl-model {
      font-size: 11px;
      color: #606266;
      font-family: monospace;
    }
  }
}

.form-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 16px;
  }
}

/* 品牌卡片展开区：只读模型清单 + 品牌操作区 */
.brand-model-section {
  margin-bottom: 16px;
}

.brand-section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;

  .section-label {
    font-size: 13px;
    font-weight: 600;
    color: #606266;
  }

  .brand-endpoint {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 12px;
    color: #909399;
    background: #f0f2f5;
    padding: 2px 8px;
    border-radius: 6px;
    max-width: 60%;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.brand-model-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.brand-model-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 5px 10px;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  background: #fff;
  transition: border-color 0.15s, background 0.15s;

  code {
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 12px;
    color: #909399;
  }

  &.configured code {
    color: #303133;
  }

  &.enabled {
    border-color: #b3e19d;
    background: #f0f9eb;

    code { color: #529b2e; }
  }

  &.is-default {
    border-color: #e6a23c;
    background: #fdf6ec;

    code { color: #b88230; }
  }

  .chip-on {
    color: #67c23a;
    font-size: 14px;
  }
}

.brand-action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.brand-default-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed #ebeef5;
  flex-wrap: wrap;

  .brand-default-label {
    font-size: 13px;
    color: #606266;
  }

  .brand-default-select {
    width: 240px;
  }

  .brand-default-hint {
    flex-basis: 100%;
    font-size: 12px;
    color: #909399;
  }
}

/* 品牌凭证对话框 */
.credential-tip {
  margin-bottom: 16px;
}

.credential-switch-hint {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.credential-saved-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

@media (max-width: 768px) {
  .catalog-layout {
    flex-direction: column;
  }

  .category-sidebar {
    width: 100%;
    border-right: none;
    border-bottom: 1px solid #ebeef5;
    margin-right: 0;
    margin-bottom: 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    padding-bottom: 12px;
  }

  .category-nav-item {
    width: auto;
    flex: 1 1 calc(50% - 4px);
    min-width: 140px;
    border: 1px solid #ebeef5;
    border-radius: 8px;
    border-bottom: 1px solid #ebeef5;
  }

  .filter-bar .filter-input {
    width: 100%;
  }

  .filter-bar .filter-actions {
    width: 100%;
    margin-left: 0;
    justify-content: flex-end;
  }

  .model-card-grid {
    grid-template-columns: 1fr;
  }

  .provider-card-header {
    padding: 12px 14px;
  }

  .configured-model-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .model-actions {
    flex-wrap: wrap;
  }
}
</style>
