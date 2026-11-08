<template>
  <div class="agent-page" v-loading="pageLoading">
    <!-- 页头：标题与操作左右一行（不单独占一行），工作方式一行；两种模式的详细说明走 hover -->
    <div class="agent-howto">
      <div class="agent-howto__top">
        <h1 class="agent-howto__title">智能体管理</h1>
        <p class="agent-howto__sub">
          一个智能体 = 某个场景下的一个 AI 助手，前台按「编码」调用，按这里的配置返回结果。
        </p>
        <div class="agent-howto__spacer" />
        <el-popover
          placement="bottom-end"
          :width="420"
          trigger="hover"
          :show-after="120"
          popper-class="agent-howto-pop"
        >
          <template #reference>
            <el-icon class="agent-howto__info"><QuestionFilled /></el-icon>
          </template>
          <div class="agent-howto-pop__body">
            <p class="agent-howto-pop__item">
              <b>提示词模式</b>（大多数场景用这个）：选一个模型 + 写一段提示语就够了，
              前台一问一答直接出结果，改提示语就等于改效果，最快。
            </p>
            <p class="agent-howto-pop__item">
              <b>工作流模式</b>（要多步决策才用）：先在画布上把「判断 → 调用 → 分支」连成一条流程，
              再回到这里把流程绑到智能体上。
            </p>
          </div>
        </el-popover>
        <div class="agent-howto__actions">
          <el-tooltip content="刷新列表" placement="bottom">
            <el-button class="agent-icon-btn" plain @click="loadAll">
              <el-icon><Refresh /></el-icon>
            </el-button>
          </el-tooltip>
          <el-button type="primary" @click="openCreate">
            <el-icon><Plus /></el-icon>
            新增智能体
          </el-button>
        </div>
      </div>
      <div class="agent-howto__bar">
        <span class="agent-howto__mode">
          提示词模式
          <span class="agent-howto__badge">大多数场景用这个</span>
        </span>
        <span class="agent-howto__divider" />
        <span class="agent-howto__mode">工作流模式</span>
        <el-button link type="primary" class="agent-howto__link" @click="router.push('/ai/workflows')">
          先去画布编排一条流程
          <el-icon class="agent-howto__arrow"><ArrowRight /></el-icon>
        </el-button>
        <!-- 概览：不单独占一行，贴在工作方式行右侧 -->
        <div class="agent-stats">
          <div class="stat-card">
            <span class="stat-card__label">{{ activeTerminalMeta.label }}智能体</span>
            <span class="stat-card__value">{{ stats.total }}</span>
          </div>
          <div class="stat-card" :title="`停用 ${stats.disabled}`">
            <span class="stat-card__label">已启用</span>
            <span class="stat-card__value stat-card__value--on">{{ stats.enabled }}</span>
          </div>
          <div class="stat-card" title="未绑定模型无法启用">
            <span class="stat-card__label">待配置模型</span>
            <span class="stat-card__value stat-card__value--warn">{{ stats.pending }}</span>
          </div>
          <div class="stat-card">
            <span class="stat-card__label">覆盖场景</span>
            <span class="stat-card__value">{{ stats.scenes }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 端切换：商家端 / 前端 / 管理后台，各自维护自己的一批智能体 -->
    <el-card class="agent-terminals-card" shadow="never">
      <el-tabs v-model="activeTerminal" class="agent-terminals">
        <el-tab-pane v-for="t in TERMINALS" :key="t.value" :name="t.value" :label="t.label" />
      </el-tabs>
      <p class="agent-terminals__desc">{{ activeTerminalMeta.desc }}</p>
    </el-card>

    <!-- 筛选 -->
    <el-card class="agent-panel" shadow="never">
      <div class="agent-toolbar">
        <el-select v-model="filters.scene" placeholder="全部场景" clearable style="width: 150px">
          <el-option v-for="s in scenes" :key="s.value" :label="s.label" :value="s.value" />
        </el-select>
        <el-select v-model="filters.status" placeholder="全部状态" clearable style="width: 130px">
          <el-option label="已启用" :value="1" />
          <el-option label="已停用" :value="0" />
        </el-select>
        <el-input
          v-model="filters.keyword"
          placeholder="搜索名称或编码"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
        />
        <div class="agent-toolbar__spacer" />
        <el-button-group class="agent-view-switch">
          <el-button
            :type="viewMode === 'card' ? 'primary' : ''"
            :title="'卡片展示'"
            @click="setViewMode('card')"
          >
            <el-icon><Grid /></el-icon>
          </el-button>
          <el-button
            :type="viewMode === 'table' ? 'primary' : ''"
            :title="'表格展示'"
            @click="setViewMode('table')"
          >
            <el-icon><List /></el-icon>
          </el-button>
        </el-button-group>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 卡片视图（默认） -->
      <div v-if="list.length && viewMode === 'card'" class="agent-cards">
        <div
          v-for="row in list"
          :key="row.id"
          class="agent-card"
          :class="{ 'agent-card--off': row.status !== 1 }"
        >
          <div class="agent-card__head">
            <span class="agent-cell__icon" :class="`agent-cell__icon--${row.scene}`">
              <el-icon><component :is="sceneIcon(row.scene)" /></el-icon>
            </span>
            <div class="agent-card__title">
              <div class="agent-card__name" :title="row.name">{{ row.name }}</div>
              <div class="agent-card__code">{{ row.code }}</div>
            </div>
            <el-switch
              v-model="row.__on"
              size="small"
              :loading="row.__switching"
              @change="(val) => onToggle(row, val)"
            />
          </div>

          <div class="agent-card__tags">
            <el-tag size="small" :type="sceneTag(row.scene)" effect="light">{{ row.sceneLabel }}</el-tag>
            <el-tag size="small" type="info" effect="plain">{{ triggerLabel(row.triggerType) }}</el-tag>
            <el-tag size="small" type="info" effect="plain">{{ scopeLabel(row.scopeType) }}</el-tag>
            <el-tooltip
              v-if="row.mode === 'workflow'"
              content="点一下看这条流程长什么样"
              placement="top"
            >
              <el-tag
                size="small"
                type="warning"
                effect="plain"
                class="agent-tag--link"
                @click="openFlow(row)"
              >
                工作流{{ row.workflowCode ? ' · ' + row.workflowCode : '' }}
              </el-tag>
            </el-tooltip>
            <el-tag v-if="row.builtIn === 1" size="small" type="success" effect="plain">内置</el-tag>
            <el-tooltip
              v-if="isContractOutput(row)"
              content="提示词里约定了 JSON 键名，给前台程序解析用。试用对话会按字段展开，不用读原文"
              placement="top"
            >
              <el-tag size="small" type="primary" effect="plain">结构化输出</el-tag>
            </el-tooltip>
          </div>

          <p class="agent-card__desc">{{ row.description || '暂无能力说明' }}</p>

          <div v-if="row.pcPage || row.h5Page || row.usagePage" class="agent-card__usage">
            <div class="usage-row">
              <span class="usage-row__tag usage-row__tag--pc">PC 商城</span>
              <span
                class="usage-row__text"
                :class="{ 'usage-row__text--empty': !row.pcPage }"
                :title="row.pcPage || '还没接入'"
              >
                {{ row.pcPage || '还没接入' }}
              </span>
              <el-button v-if="row.pcPath" link type="primary" size="small" @click="openFront(row.pcPath)">
                去看看
              </el-button>
            </div>
            <div class="usage-row">
              <span class="usage-row__tag usage-row__tag--h5">H5 / 小程序</span>
              <span
                class="usage-row__text"
                :class="{ 'usage-row__text--empty': !row.h5Page }"
                :title="row.h5Page || '还没接入'"
              >
                {{ row.h5Page || '还没接入' }}
              </span>
              <el-button v-if="row.h5Path" link type="primary" size="small" @click="openFront(row.h5Path)">
                去看看
              </el-button>
            </div>
          </div>

          <div class="agent-card__model">
            <span class="agent-card__model-label">模型</span>
            <span v-if="row.modelCode" class="agent-model">{{ row.modelCode }}</span>
            <span v-else class="agent-model agent-model--empty">未配置</span>
          </div>

          <div class="agent-card__actions">
            <el-button link type="primary" size="small" @click="openChat(row)">试用对话</el-button>
            <el-button link type="primary" size="small" @click="openConfig(row)">配置</el-button>
            <el-button link type="primary" size="small" @click="openLogs(row)">调用记录</el-button>
            <div class="agent-card__spacer" />
            <el-button
              link
              type="danger"
              size="small"
              :disabled="row.builtIn === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </div>
        </div>
      </div>

      <!-- 表格视图 -->
      <el-table
        v-else-if="list.length"
        :data="list"
        class="agent-table"
        size="small"
        style="width: 100%"
        row-class-name="agent-row"
      >
        <el-table-column label="智能体" min-width="220">
          <template #default="{ row }">
            <div class="agent-cell">
              <span class="agent-cell__icon" :class="`agent-cell__icon--${row.scene}`">
                <el-icon><component :is="sceneIcon(row.scene)" /></el-icon>
              </span>
              <span class="agent-cell__text">
                <span class="agent-cell__name">{{ row.name }}</span>
                <span class="agent-cell__code">{{ row.code }}</span>
              </span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="场景" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="sceneTag(row.scene)" effect="light">{{ row.sceneLabel }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="description" label="能力说明" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="agent-desc">{{ row.description || '—' }}</span>
          </template>
        </el-table-column>

        <el-table-column label="在哪儿用（PC / H5）" min-width="260">
          <template #default="{ row }">
            <div class="agent-usage">
              <div class="agent-usage__line">
                <span class="usage-row__tag usage-row__tag--pc">PC</span>
                <span v-if="row.pcPage" class="agent-usage__text">{{ row.pcPage }}</span>
                <span v-else class="agent-usage--empty">还没接入</span>
                <el-button v-if="row.pcPath" link type="primary" size="small" @click="openFront(row.pcPath)">
                  去看看
                </el-button>
              </div>
              <div class="agent-usage__line">
                <span class="usage-row__tag usage-row__tag--h5">H5</span>
                <span v-if="row.h5Page" class="agent-usage__text">{{ row.h5Page }}</span>
                <span v-else class="agent-usage--empty">还没接入</span>
                <el-button v-if="row.h5Path" link type="primary" size="small" @click="openFront(row.h5Path)">
                  去看看
                </el-button>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="模型" width="150">
          <template #default="{ row }">
            <span v-if="row.modelCode" class="agent-model">{{ row.modelCode }}</span>
            <span v-else class="agent-model agent-model--empty">未配置</span>
          </template>
        </el-table-column>

        <el-table-column label="触发 / 范围" width="150">
          <template #default="{ row }">
            <div class="agent-meta">
              <span>{{ triggerLabel(row.triggerType) }}</span>
              <span class="agent-meta__sub">{{ scopeLabel(row.scopeType) }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="96" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.__on"
              size="small"
              :loading="row.__switching"
              @change="(val) => onToggle(row, val)"
            />
          </template>
        </el-table-column>

        <el-table-column label="操作" width="236" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openChat(row)">试用对话</el-button>
            <el-button link type="primary" size="small" @click="openConfig(row)">配置</el-button>
            <el-button link type="primary" size="small" @click="openLogs(row)">调用记录</el-button>
            <el-button
              link
              type="danger"
              size="small"
              :disabled="row.builtIn === 1"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-else class="agent-empty">
        <el-icon class="agent-empty__icon"><MagicStick /></el-icon>
        <p class="agent-empty__title">还没有智能体</p>
        <p class="agent-empty__desc">点击右上角「新增智能体」创建第一个场景能力，或从内置场景模板中选择</p>
        <el-button type="primary" plain @click="openCreate">新增智能体</el-button>
      </div>

      <div v-if="pagination.total" class="agent-pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next"
          :current-page="pagination.page"
          :page-size="pagination.limit"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          @current-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </el-card>

    <!-- 配置 / 新增 -->
    <el-drawer
      v-model="configVisible"
      :title="isCreate ? '新增智能体' : `配置 · ${current?.name || ''}`"
      size="520px"
      destroy-on-close
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="96px" class="agent-form">
        <el-form-item label="编码" prop="code">
          <el-input v-model="form.code" placeholder="如 goods_recommend，前台按此调用" :disabled="!isCreate" />
          <div v-if="isCreate" class="agent-form__tip">
            前台喊它用的名字（小写英文 + 下划线，如 goods_recommend），创建后不可修改
          </div>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="如 商品智能推荐" />
        </el-form-item>
        <el-form-item label="场景" prop="scene">
          <el-select v-model="form.scene" placeholder="选择场景" style="width: 100%">
            <el-option v-for="s in scenes" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="所属端" prop="terminal">
          <el-select v-model="form.terminal" placeholder="选择端" style="width: 100%">
            <el-option v-for="t in TERMINALS" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
          <div class="agent-form__tip">决定这个智能体归在哪个端的选项卡下，配置后可调整</div>
        </el-form-item>
        <el-form-item label="能力说明">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="一句话说明这个智能体做什么" />
        </el-form-item>
        <el-form-item label="PC 商城">
          <el-input v-model="form.pcPage" placeholder="在哪个页面哪个位置，例：商品详情页 → 猜你喜欢推荐位" />
          <el-input
            v-model="form.pcPath"
            placeholder="访问路径，例：/pc/product/list（填了才能一键打开）"
            style="margin-top: 6px"
          />
        </el-form-item>
        <el-form-item label="H5 / 小程序">
          <el-input v-model="form.h5Page" placeholder="在哪个页面哪个位置，例：商品详情页 → 猜你喜欢推荐位" />
          <el-input
            v-model="form.h5Path"
            placeholder="访问路径，例：/h5/pages/index/index（填了才能一键打开）"
            style="margin-top: 6px"
          />
        </el-form-item>
        <el-form-item label="其它入口">
          <el-input
            v-model="form.usagePage"
            type="textarea"
            :rows="2"
            placeholder="商家后台等其他调用位置，例：商品管理 → 商品编辑页「AI 生成文案」"
          />
          <div class="agent-form__tip">
            PC / H5 两行会直接显示在卡片上，右边带「去看看」，点一下就能打开对应页面看真实效果；
            还没接入的端留空即可，卡片上显示「还没接入」
          </div>
        </el-form-item>
        <el-form-item label="执行模式">
          <el-radio-group v-model="form.mode">
            <el-radio-button value="prompt">提示词</el-radio-button>
            <el-radio-button value="workflow">工作流</el-radio-button>
          </el-radio-group>
          <div class="agent-form__tip">
            <template v-if="form.mode === 'prompt'">
              一问一答：前台传进来的问题，交给下面选的模型 + 提示语直接出结果。想调效果就改提示语。
            </template>
            <template v-else>
              多步流程：问题交给你在画布上编排的那条流程去跑，下面的提示语不参与执行。
              <el-button link type="primary" @click="openDesigner">还没编过流程？去画布编一条</el-button>
            </template>
          </div>
        </el-form-item>
        <el-form-item v-if="form.mode === 'workflow'" label="关联工作流">
          <el-select v-model="form.workflowCode" placeholder="选择工作流" clearable filterable style="width: 100%">
            <el-option v-for="w in workflows" :key="w.code" :label="`${w.name} · ${w.code}`" :value="w.code" />
          </el-select>
          <div class="agent-form__tip">
            没有合适的？<el-button link type="primary" @click="openDesigner">去编排一个</el-button>
          </div>
        </el-form-item>

        <el-divider content-position="left">{{ form.mode === 'workflow' ? '兜底模型（画布节点未指定模型时用）' : '模型与提示词' }}</el-divider>

        <el-form-item label="绑定模型">
          <el-select
            v-model="form.modelCode"
            placeholder="选择模型"
            clearable
            filterable
            style="width: 100%"
          >
            <el-option v-for="m in models" :key="m.code || m.name" :label="modelLabel(m)" :value="m.code || m.name" />
          </el-select>
          <div class="agent-form__tip">未绑定模型的智能体无法启用</div>
        </el-form-item>
        <el-form-item v-if="form.mode === 'workflow'" label=" ">
          <el-alert
            type="info"
            :closable="false"
            show-icon
            title="工作流模式下由画布决定提示词，这里的系统提示词不参与执行（留作说明即可）"
          />
        </el-form-item>
        <el-form-item label="系统提示词">
          <el-input
            v-model="form.prompt"
            type="textarea"
            :rows="6"
            placeholder="定义角色、约束与输出格式，例如：你是电商导购助手，只根据给定商品列表回答……"
          />
        </el-form-item>

        <el-divider content-position="left">触发条件</el-divider>

        <el-form-item label="触发方式">
          <el-select v-model="form.triggerType" style="width: 100%">
            <el-option label="手动触发" value="manual" />
            <el-option label="自动触发" value="auto" />
            <el-option label="事件触发" value="event" />
            <el-option label="定时触发" value="schedule" />
          </el-select>
        </el-form-item>
        <el-form-item label="触发配置">
          <el-input v-model="form.triggerConfig" type="textarea" :rows="2" placeholder='JSON，如 {"events":["view","cart"]}' />
        </el-form-item>

        <el-divider content-position="left">生效范围</el-divider>

        <el-form-item label="范围类型">
          <el-select v-model="form.scopeType" style="width: 100%">
            <el-option label="全站" value="all" />
            <el-option label="指定租户" value="tenant" />
            <el-option label="指定店铺" value="shop" />
            <el-option label="指定品类" value="category" />
          </el-select>
        </el-form-item>
        <el-form-item label="范围配置">
          <el-input v-model="form.scopeConfig" type="textarea" :rows="2" placeholder='JSON，如 {"tenantIds":[1,2]}' />
        </el-form-item>

        <el-divider content-position="left">其他参数</el-divider>

        <el-form-item label="参数">
          <el-input v-model="form.params" type="textarea" :rows="3" placeholder='JSON，如 {"temperature":0.4,"maxTokens":800}' />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :step="10" controls-position="right" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="configVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-drawer>

    <!-- 调用记录 -->
    <el-drawer v-model="logsVisible" :title="`调用记录 · ${current?.name || ''}`" size="760px" destroy-on-close>
      <div class="agent-toolbar agent-toolbar--sub">
        <el-select v-model="logFilters.status" placeholder="全部结果" clearable style="width: 130px">
          <el-option label="成功" :value="1" />
          <el-option label="失败" :value="0" />
        </el-select>
        <div class="agent-toolbar__spacer" />
        <el-button type="primary" plain @click="loadLogs">查询</el-button>
      </div>

      <el-table v-if="logs.length" :data="logs" size="small" style="width: 100%">
        <el-table-column prop="createTime" label="调用时间" width="160" />
        <el-table-column prop="bizId" label="业务单号" width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.bizId || '—' }}</template>
        </el-table-column>
        <el-table-column prop="modelName" label="模型" width="140" show-overflow-tooltip>
          <template #default="{ row }">{{ row.modelName || '—' }}</template>
        </el-table-column>
        <el-table-column prop="inputSummary" label="入参摘要" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.inputSummary || '—' }}</template>
        </el-table-column>
        <el-table-column label="Tokens" width="110" align="right">
          <template #default="{ row }">
            {{ (row.promptTokens || 0) + ' / ' + (row.completionTokens || 0) }}
          </template>
        </el-table-column>
        <el-table-column prop="latencyMs" label="耗时" width="90" align="right">
          <template #default="{ row }">{{ row.latencyMs != null ? row.latencyMs + ' ms' : '—' }}</template>
        </el-table-column>
        <el-table-column label="结果" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 1 ? 'success' : 'danger'">{{ row.statusText }}</el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div v-else class="agent-empty agent-empty--sm">
        <el-icon class="agent-empty__icon"><Clock /></el-icon>
        <p class="agent-empty__title">暂无调用记录</p>
        <p class="agent-empty__desc">智能体启用并被前台调用后，这里会记录每一次调用的时间、模型与耗时</p>
      </div>

      <div v-if="logPagination.total" class="agent-pager">
        <el-pagination
          background
          layout="total, prev, pager, next"
          :current-page="logPagination.page"
          :page-size="logPagination.limit"
          :total="logPagination.total"
          @current-change="handleLogPageChange"
        />
      </div>
    </el-drawer>

    <!-- 试用对话：按当前配置直接调 /web/ai/chat，验证提示词 / 工作流效果 -->
    <AgentChatDrawer v-model="chatVisible" :agent="chatAgent" />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Refresh,
  Plus,
  MagicStick,
  Clock,
  Goods,
  HomeFilled,
  ChatDotRound,
  Tickets,
  EditPen,
  Aim,
  WarnTriangleFilled,
  Service,
  Shop,
  Monitor,
  Setting,
  ChatDotSquare,
  Grid,
  List,
  Share,
  QuestionFilled,
  ArrowRight
} from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { appUrl } from '@/utils/appBase'
import { isContractOutput } from '@/utils/aiContractOutput.js'
import AgentChatDrawer from './AgentChatDrawer.vue'
import { agentApi } from '@/api/ai/agent'
import { workflowApi } from '@/api/ai/workflow'
import { aiApi } from '@/api/ai/ai'

const router = useRouter()

const pageLoading = ref(false)
const saving = ref(false)
const list = ref([])
const scenes = ref([])
const models = ref([])
const workflows = ref([])

/** 四个端：商家端（bcenter）/ 前端（C 端 PC+H5+小程序）/ 管理后台（平台运营）/ IM（客服与消息通知） */
const TERMINALS = [
  {
    value: 'merchant',
    label: '商家端',
    icon: Shop,
    desc: '商家后台使用：客服回复、商品文案、营销策划、库存预警、评论分析、售后工单'
  },
  {
    value: 'frontend',
    label: '前端',
    icon: Monitor,
    desc: 'C 端 PC / H5 / 小程序使用：商品推荐、首页个性化、意图识别、智能下单'
  },
  {
    value: 'admin',
    label: '管理后台',
    icon: Setting,
    desc: '平台运营使用：经营数据解读、运营策略建议、商品内容合规审核'
  },
  {
    value: 'im',
    label: 'IM 客服',
    icon: ChatDotSquare,
    desc: 'IM 会话与消息中心使用：会话路由、话术辅助、会话质检、通知编排、消息模板'
  }
]
const activeTerminal = ref('merchant')
const activeTerminalMeta = computed(() => TERMINALS.find((t) => t.value === activeTerminal.value) || TERMINALS[0])

/** 展示形态：卡片（默认）/ 表格，记住上次选择 */
const VIEW_MODE_KEY = 'ai-agent-view-mode'
const viewMode = ref(localStorage.getItem(VIEW_MODE_KEY) === 'table' ? 'table' : 'card')
const setViewMode = (mode) => {
  viewMode.value = mode
  try {
    localStorage.setItem(VIEW_MODE_KEY, mode)
  } catch {
    /* 隐私模式下写不了，忽略 */
  }
}

const filters = reactive({ scene: '', status: '', keyword: '' })
const pagination = reactive({ page: 1, limit: 20, total: 0 })

const configVisible = ref(false)
const isCreate = ref(false)
const current = ref(null)
const formRef = ref(null)
const form = reactive({
  id: null,
  code: '',
  name: '',
  scene: '',
  terminal: 'merchant',
  description: '',
  usagePage: '',
  pcPage: '',
  pcPath: '',
  h5Page: '',
  h5Path: '',
  modelCode: '',
  mode: 'prompt',
  workflowCode: '',
  prompt: '',
  triggerType: 'manual',
  triggerConfig: '',
  scopeType: 'all',
  scopeConfig: '',
  params: '',
  sort: 100
})
const rules = {
  code: [{ required: true, message: '请输入智能体编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入智能体名称', trigger: 'blur' }],
  scene: [{ required: true, message: '请选择场景', trigger: 'change' }]
}

const logsVisible = ref(false)
const chatVisible = ref(false)
const chatAgent = ref(null)
const logs = ref([])
const logFilters = reactive({ status: '' })
const logPagination = reactive({ page: 1, limit: 10, total: 0 })

const stats = computed(() => {
  const rows = list.value
  const sceneSet = new Set(rows.map((r) => r.scene).filter(Boolean))
  return {
    total: pagination.total || rows.length,
    enabled: rows.filter((r) => r.status === 1).length,
    disabled: rows.filter((r) => r.status !== 1).length,
    pending: rows.filter((r) => !r.modelCode).length,
    scenes: sceneSet.size
  }
})

const SCENE_ICONS = {
  recommend: Goods,
  service: ChatDotRound,
  order: Tickets,
  content: EditPen,
  marketing: MagicStick,
  insight: Aim,
  supply: WarnTriangleFilled,
  aftersale: Service
}

const sceneIcon = (scene) => SCENE_ICONS[scene] || (scene === 'recommend' ? HomeFilled : MagicStick)

const SCENE_TAGS = {
  recommend: 'primary',
  service: 'success',
  order: 'warning',
  content: 'info',
  marketing: 'danger',
  insight: 'info',
  supply: 'warning',
  aftersale: 'success'
}
const sceneTag = (scene) => SCENE_TAGS[scene] || 'info'

const TRIGGER_LABELS = { manual: '手动触发', auto: '自动触发', event: '事件触发', schedule: '定时触发' }
const SCOPE_LABELS = { all: '全站', tenant: '指定租户', shop: '指定店铺', category: '指定品类' }
const triggerLabel = (t) => TRIGGER_LABELS[t] || t || '—'
const scopeLabel = (s) => SCOPE_LABELS[s] || s || '—'
const modelLabel = (m) => (m.name ? `${m.name}${m.code ? ' · ' + m.code : ''}` : String(m.code || m))


const loadScenes = async () => {
  try {
    const res = await agentApi.getScenes()
    if (res.code === 1 && Array.isArray(res.data)) scenes.value = res.data
  } catch (e) {
    /* 字典拉取失败不阻塞页面 */
  }
}

const loadModels = async () => {
  try {
    const res = await aiApi.getEnabledModels()
    const rows = res?.data
    if (res.code === 1 && Array.isArray(rows)) models.value = rows
  } catch (e) {
    /* 模型列表失败不阻塞页面 */
  }
}

const loadList = async () => {
  pageLoading.value = true
  try {
    const params = { page: pagination.page, limit: pagination.limit, terminal: activeTerminal.value }
    if (filters.scene) params.scene = filters.scene
    if (filters.status !== '' && filters.status !== null) params.status = filters.status
    if (filters.keyword) params.keyword = filters.keyword
    const res = await agentApi.getList(params)
    if (res.code === 1 && res.data) {
      list.value = (res.data.list || []).map((r) => ({ ...r, __on: r.status === 1, __switching: false }))
      pagination.total = res.data.total || 0
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (e) {
    ElMessage.error(e.message || '加载智能体列表失败')
    list.value = []
  } finally {
    pageLoading.value = false
  }
}

const loadWorkflows = async () => {
  try {
    const res = await workflowApi.getList({ page: 1, limit: 100 })
    if (res.code === 1 && res.data) workflows.value = res.data.list || []
  } catch (e) {
    /* 工作流列表失败不阻塞智能体页面 */
  }
}

/** 新窗口打开画布：必须走 appUrl 拼部署基路径（线上是 /admin/），直接写 '/ai/...' 会落到站点根被弹回首页 */
const openDesigner = () => {
  window.open(appUrl('/ai/workflow/design'), '_blank')
}

/** 卡片上的「工作流 · code」标签可点：直接跳到这条流程的画布，方便对照智能体看它到底跑什么 */
const openFlow = (row) => {
  if (!row.workflowCode) return
  window.open(appUrl(`/ai/workflow/design?code=${encodeURIComponent(row.workflowCode)}`), '_blank')
}

/**
 * 打开智能体在商城的真实展示页面。
 *
 * 这里刻意不用 appUrl()：那个函数是给 admin 自己的子路径（线上是 /admin/）拼前缀用的，
 * 而 PC 商城（/pc/）与 H5（/h5/）是站点根下的另外两个应用，套上去会变成 /admin/pc/... 打不开。
 */
const openFront = (path) => {
  if (!path) return
  const url = /^https?:\/\//i.test(path)
    ? path
    : `${window.location.origin}${path.startsWith('/') ? '' : '/'}${path}`
  window.open(url, '_blank', 'noopener')
}

/** 试用对话：拿当前配置直接聊两句，验证提示词 / 流程改对了没 */
const openChat = (row) => {
  chatAgent.value = row
  chatVisible.value = true
}

/** 画布那边保存完会 postMessage 回来，这里把工作流下拉刷新一遍，省得手动刷页面 */
const onWorkflowSaved = (e) => {
  if (e.origin !== window.location.origin) return
  if (e.data?.type !== 'ai-workflow-saved') return
  loadWorkflows()
  ElMessage.success(`工作流「${e.data.name || e.data.code || ''}」已保存，可直接选择了`)
}
/** 兜底：新窗口关掉回到本页时，如果配置抽屉还开着，也刷一次下拉 */
const onWindowFocus = () => {
  if (configVisible.value && form.mode === 'workflow') loadWorkflows()
}

const loadAll = () => Promise.all([loadList(), loadScenes(), loadModels(), loadWorkflows()])

/** 切换端：清空筛选并回到第一页重新加载，避免把上一个端的筛选条件带过去 */
const onTerminalChange = () => {
  filters.scene = ''
  filters.status = ''
  filters.keyword = ''
  pagination.page = 1
  loadList()
}

// activeTerminal 一变就重载：不依赖 el-tabs 的 tab-change 事件（各版本行为不一致，实测未触发）
watch(activeTerminal, () => {
  onTerminalChange()
})

const handleSearch = () => {
  pagination.page = 1
  loadList()
}

const handleReset = () => {
  filters.scene = ''
  filters.status = ''
  filters.keyword = ''
  pagination.page = 1
  loadList()
}

const handlePageChange = (page) => {
  pagination.page = page
  loadList()
}

const handleSizeChange = (size) => {
  pagination.limit = size
  pagination.page = 1
  loadList()
}

/** 启用前校验：未绑定模型不允许启用 */
const onToggle = async (row, val) => {
  if (val && !row.modelCode) {
    row.__on = false
    ElMessage.warning('请先配置模型后再启用')
    openConfig(row)
    return
  }
  row.__switching = true
  try {
    const res = await agentApi.updateStatus({ code: row.code, status: val ? 1 : 0 })
    if (res.code === 1 && res.data?.success) {
      row.status = val ? 1 : 0
      ElMessage.success(val ? '已启用' : '已停用')
    } else {
      row.__on = !val
      ElMessage.error(res?.data?.message || res?.message || '状态更新失败')
    }
  } catch (e) {
    row.__on = !val
    ElMessage.error(e.message || '状态更新失败')
  } finally {
    row.__switching = false
  }
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    scene: '',
    terminal: activeTerminal.value,
    description: '',
    usagePage: '',
    pcPage: '',
    pcPath: '',
    h5Page: '',
    h5Path: '',
    modelCode: '',
    mode: 'prompt',
    workflowCode: '',
    prompt: '',
    triggerType: 'manual',
    triggerConfig: '',
    scopeType: 'all',
    scopeConfig: '',
    params: '',
    sort: 100
  })
}

const openCreate = () => {
  isCreate.value = true
  current.value = null
  resetForm()
  configVisible.value = true
}

const openConfig = (row) => {
  isCreate.value = false
  current.value = row
  Object.assign(form, {
    id: row.id,
    code: row.code,
    name: row.name,
    scene: row.scene,
    terminal: row.terminal || 'frontend',
    description: row.description || '',
    usagePage: row.usagePage || '',
    pcPage: row.pcPage || '',
    pcPath: row.pcPath || '',
    h5Page: row.h5Page || '',
    h5Path: row.h5Path || '',
    modelCode: row.modelCode || '',
    mode: row.mode === 'workflow' ? 'workflow' : 'prompt',
    workflowCode: row.workflowCode || '',
    prompt: row.prompt || '',
    triggerType: row.triggerType || 'manual',
    triggerConfig: row.triggerConfig || '',
    scopeType: row.scopeType || 'all',
    scopeConfig: row.scopeConfig || '',
    params: row.params || '',
    sort: row.sort == null ? 100 : row.sort
  })
  configVisible.value = true
}

const validJson = (text, field) => {
  if (!text) return true
  try {
    JSON.parse(text)
    return true
  } catch (e) {
    ElMessage.warning(`${field} 不是合法 JSON，请检查后保存`)
    return false
  }
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  if (!validJson(form.triggerConfig, '触发配置')) return
  if (!validJson(form.scopeConfig, '范围配置')) return
  if (!validJson(form.params, '参数')) return

  saving.value = true
  try {
    const res = await agentApi.save({ ...form })
    if (res.code === 1 && res.data?.success) {
      ElMessage.success('保存成功')
      configVisible.value = false
      await loadList()
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (e) {
    ElMessage.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确认删除智能体「${row.name}」？删除后前台将无法再取到它的配置。`, '删除确认', {
      type: 'warning',
      confirmButtonText: '确认删除',
      cancelButtonText: '取消'
    })
  } catch (e) {
    return
  }
  const res = await agentApi.remove({ id: row.id })
  if (res.code === 1 && res.data?.success) {
    ElMessage.success('已删除')
    await loadList()
  } else {
    ElMessage.error(res?.data?.message || res.message || '删除失败')
  }
}

const openLogs = (row) => {
  current.value = row
  logFilters.status = ''
  logPagination.page = 1
  logs.value = []
  logsVisible.value = true
  loadLogs()
}

const loadLogs = async () => {
  if (!current.value) return
  const params = {
    page: logPagination.page,
    limit: logPagination.limit,
    agentCode: current.value.code
  }
  if (logFilters.status !== '' && logFilters.status !== null) params.status = logFilters.status
  const res = await agentApi.getInvokeLogs(params)
  if (res.code === 1 && res.data) {
    logs.value = res.data.list || []
    logPagination.total = res.data.total || 0
  } else {
    logs.value = []
    logPagination.total = 0
  }
}

const handleLogPageChange = (page) => {
  logPagination.page = page
  loadLogs()
}

onMounted(() => {
  loadAll()
  window.addEventListener('message', onWorkflowSaved)
  window.addEventListener('focus', onWindowFocus)
})

onUnmounted(() => {
  window.removeEventListener('message', onWorkflowSaved)
  window.removeEventListener('focus', onWindowFocus)
})
</script>

<style scoped>
.agent-page {
  padding: 20px;
  background: #f5f6f8;
  min-height: 100%;
}

/* 纯图标按钮：刷新这类低频操作，只留图标不抢主操作的视觉重心 */
.agent-howto__actions .agent-icon-btn {
  padding-left: 8px;
  padding-right: 8px;
}

/* 页头说明：标题与操作同一行，工作方式一行；两种模式的完整说明放 ⓘ hover 里 */
.agent-howto {
  background: #f7f8fa;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px 12px;
  margin-bottom: 12px;
}
.agent-howto__top {
  display: flex;
  align-items: center;
  gap: 10px;
  padding-bottom: 7px;
  margin-bottom: 7px;
  border-bottom: 1px dashed #e4e7ed;
}
.agent-howto__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.agent-howto__title {
  flex-shrink: 0;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2329;
  line-height: 1.4;
}
.agent-howto__sub {
  margin: 0;
  min-width: 0;
  font-size: 13px;
  color: #8a9099;
  line-height: 1.4;
}
.agent-howto__spacer {
  flex: 1;
}
.agent-howto__info {
  flex-shrink: 0;
  font-size: 15px;
  color: #409eff;
  cursor: help;
  outline: none;
}
/* 工作方式一行：两个模式名 + 画布入口，完整说明在 ⓘ 里 */
.agent-howto__bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  row-gap: 4px;
  gap: 10px;
  font-size: 13px;
}
.agent-howto__mode {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 600;
  color: #1f2329;
}
.agent-howto__divider {
  width: 1px;
  height: 12px;
  background: #dcdfe6;
}
.agent-howto__badge {
  font-weight: 400;
  font-size: 12px;
  color: #409eff;
  background: #ecf5ff;
  border-radius: 4px;
  padding: 1px 6px;
}
.agent-howto__link {
  padding-left: 0;
  font-size: 13px;
}
.agent-howto__arrow {
  margin-left: 2px;
}
.agent-tag--link {
  cursor: pointer;
}
@media (max-width: 900px) {
  .agent-howto__top {
    flex-wrap: wrap;
  }
  .agent-howto__sub {
    flex-basis: 100%;
  }
}

/* 概览：一行式小胶囊，贴在工作方式行右侧，不单独占一行 */
.agent-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
  flex-shrink: 0;
}

.stat-card {
  display: flex;
  align-items: baseline;
  gap: 6px;
  background: #fff;
  border: 1px solid #eceef2;
  border-radius: 6px;
  padding: 3px 10px;
  white-space: nowrap;
}

.stat-card__label {
  font-size: 12px;
  color: #8a9099;
}

.stat-card__value {
  font-size: 15px;
  font-weight: 600;
  color: #1f2329;
  line-height: 1.2;
}

.stat-card__value--on {
  color: #1a7f37;
}

.stat-card__value--warn {
  color: #c8811a;
}


/* 面板 */
.agent-terminals-card { margin-bottom: 16px; }
.agent-terminals { margin-bottom: 0; }
.agent-terminals :deep(.el-tabs__header) { margin-bottom: 8px; }
.agent-terminals :deep(.el-tabs__item) { font-size: 15px; font-weight: 600; }
.agent-terminals__desc { margin: 0; font-size: 13px; color: var(--el-text-color-secondary); line-height: 1.6; }
.agent-view-switch { margin-right: 8px; }

/* 卡片视图 */
.agent-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(318px, 1fr));
  gap: 14px;
  padding: 4px 0 8px;
}

.agent-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 16px;
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 10px;
  background: var(--el-bg-color);
  transition: box-shadow 0.18s, border-color 0.18s, transform 0.18s;
}

.agent-card:hover {
  border-color: var(--el-color-primary-light-5);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transform: translateY(-1px);
}

.agent-card--off {
  background: var(--el-fill-color-lighter);
}

.agent-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-card__title {
  flex: 1;
  min-width: 0;
}

.agent-card__name {
  font-size: 15px;
  font-weight: 600;
  color: var(--el-text-color-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.agent-card__code {
  margin-top: 2px;
  font-size: 12px;
  color: var(--el-text-color-placeholder);
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.agent-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.agent-card__desc {
  margin: 0;
  min-height: 40px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--el-text-color-regular);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 展示位置：卡片上 PC / H5 并排一行写清在哪个页面，右侧「去看看」直接开前台页面 */
.agent-card__usage {
  margin: 8px 0 0;
  padding: 8px 10px;
  border-radius: 6px;
  background: #f7f8fa;
  font-size: 12px;
  line-height: 1.6;
  color: #6b7280;
  display: flex;
  flex-wrap: nowrap;
  align-items: center;
  gap: 4px 14px;
}
.usage-row {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 0 1 auto;
  min-height: 22px;
}
.usage-row__tag {
  flex-shrink: 0;
  width: 62px;
  text-align: center;
  border-radius: 3px;
  padding: 0 4px;
  font-size: 11px;
  line-height: 17px;
}
/* 卡片里两端并排，标签由内容撑开且不折行（表格视图保留 62px 对齐） */
.agent-card__usage .usage-row__tag {
  width: auto;
  padding: 0 6px;
  white-space: nowrap;
}
.usage-row__tag--pc {
  color: #185fa5;
  background: #e8f1fb;
}
.usage-row__tag--h5 {
  color: #1a7f37;
  background: #e8f5ec;
}
.usage-row__text {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.usage-row__text--empty {
  color: #c0c4cc;
}

.agent-card__model {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
}

.agent-card__model-label {
  color: var(--el-text-color-secondary);
}

.agent-card__actions {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-top: 10px;
  border-top: 1px dashed var(--el-border-color-lighter);
}

.agent-card__spacer {
  flex: 1;
}

.agent-panel {
  border: 1px solid #eceef2;
  border-radius: 10px;
}

.agent-toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.agent-toolbar--sub {
  margin-bottom: 12px;
}

.agent-toolbar__spacer {
  flex: 1;
}

/* 列表单元格 */
.agent-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-cell__icon {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eef2f8;
  color: #4b5563;
  font-size: 16px;
  flex-shrink: 0;
}

.agent-cell__icon--recommend {
  background: #eaf3ff;
  color: #2f6fd0;
}

.agent-cell__icon--service,
.agent-cell__icon--aftersale {
  background: #eaf7ee;
  color: #1a7f37;
}

.agent-cell__icon--supply {
  background: #fdf3e6;
  color: #c8811a;
}

.agent-cell__icon--marketing {
  background: #fdeeee;
  color: #c94a4a;
}

.agent-cell__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.agent-cell__name {
  font-size: 13px;
  color: #1f2329;
  font-weight: 500;
  line-height: 1.4;
}

.agent-cell__code {
  font-size: 12px;
  color: #a8aeb8;
  font-family: ui-monospace, SFMono-Regular, Menlo, monospace;
}

.agent-desc {
  font-size: 13px;
  color: #6b7280;
}

.agent-usage {
  display: flex;
  flex-direction: column;
  gap: 2px;
  font-size: 12px;
  color: #6b7280;
}
.agent-usage__line {
  display: flex;
  align-items: center;
  gap: 6px;
  min-height: 20px;
}
.agent-usage__text {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.agent-usage--empty {
  color: #c0c4cc;
}

.agent-model {
  font-size: 13px;
  color: #1f2329;
}

.agent-model--empty {
  color: #c0c4cc;
}

.agent-meta {
  display: flex;
  flex-direction: column;
  font-size: 13px;
  color: #4b5563;
  line-height: 1.5;
}

.agent-meta__sub {
  font-size: 12px;
  color: #a8aeb8;
}

/* 行 hover */
:deep(.agent-row) {
  transition: background-color 0.15s ease;
}

:deep(.agent-row:hover) {
  background-color: #f8fafc;
}

/* 空状态 */
.agent-empty {
  padding: 48px 20px;
  text-align: center;
}

.agent-empty--sm {
  padding: 40px 20px;
}

.agent-empty__icon {
  font-size: 40px;
  color: #c9ced8;
}

.agent-empty__title {
  margin: 12px 0 4px;
  font-size: 14px;
  color: #4b5563;
  font-weight: 500;
}

.agent-empty__desc {
  margin: 0 0 16px;
  font-size: 13px;
  color: #a8aeb8;
  max-width: 420px;
  margin-left: auto;
  margin-right: auto;
  line-height: 1.6;
}

/* 表单 */
.agent-form__tip {
  margin-top: 6px;
  font-size: 12px;
  color: #a8aeb8;
  line-height: 1.5;
}

.agent-pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 992px) {
  /* 窄屏放不下就整行折下去，避免把工作方式行挤爆 */
  .agent-howto__bar {
    flex-wrap: wrap;
    row-gap: 6px;
  }
  .agent-stats {
    margin-left: 0;
    flex-basis: 100%;
    flex-wrap: wrap;
  }

  .agent-howto__actions {
    flex-direction: column;
    align-items: flex-end;
  }
}
</style>

<!-- popover 是 teleport 到 body 的，scoped 样式作用不到，这里单独写一份 -->
<style>
.agent-howto-pop.el-popover.el-popper {
  padding: 12px 14px;
}
.agent-howto-pop__item {
  margin: 0 0 6px;
  font-size: 12px;
  line-height: 1.7;
  color: var(--el-text-color-regular);
}
.agent-howto-pop__item:last-child {
  margin-bottom: 0;
}
.agent-howto-pop__item b {
  color: var(--el-text-color-primary);
}
</style>
