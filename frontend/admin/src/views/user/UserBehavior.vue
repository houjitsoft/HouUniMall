<template>
  <div class="user-behavior-container">
    <!-- 数据完整度：如实说明哪些维度有真实数据、哪些尚未采集，缺失不补 0 -->
    <el-alert
      v-if="dataQuality"
      type="warning"
      :closable="false"
      show-icon
      title="数据口径（实时聚合真实库表，非模拟数据）"
      class="dq-alert"
    >
      <div class="dq-line">
        会员 <b>{{ summary?.memberTotal ?? 0 }}</b> 人 ·
        订单记录 <b>{{ dataQuality.orderRows }}</b> 笔（有效 <b>{{ dataQuality.validOrderRows }}</b> / 待付款 <b>{{ summary?.pendingPayOrders ?? 0 }}</b> / 已取消 <b>{{ cancelledOrderRows }}</b>）·
        登录数据 <b>{{ dataQuality.loginAvailable ? '已接入' : '缺失' }}</b> ·
        商品足迹 <b>{{ dataQuality.browseAvailable ? dataQuality.footprintRows + ' 条' : '未采集' }}</b> ·
        运营手工标签 <b>{{ dataQuality.tagAvailable ? dataQuality.tagRows + ' 条' : '未打标' }}</b>
      </div>
      <div class="dq-line dq-sub">
        生成时间 {{ dataQuality.generatedAt }}。缺失维度不会补 0，画像中会明确标注「未采集 / 未打标」。
      </div>
    </el-alert>

    <el-tabs v-model="activeTab" class="portrait-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="画像概览" name="overview">

    <!-- 统计卡片：全部取自真实库表，缺失维度显示 0 并在上方口径条说明 -->
    <el-row :gutter="20" class="stats-cards">
      <el-col :span="6">
        <el-card class="stat-card total-visits">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><User /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ summary?.memberTotal ?? '-' }}</div>
              <div class="stat-label">会员总数</div>
              <div class="stat-sub">有登录记录 {{ summary?.loginMemberCount ?? 0 }} 人</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card today-visits">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><View /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ summary?.orderedMemberCount ?? '-' }}</div>
              <div class="stat-label">有下单会员</div>
              <div class="stat-sub">已成交 {{ summary?.orderMemberCount ?? 0 }} 人</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card active-users">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ summary?.pendingPayOrders ?? '-' }}</div>
              <div class="stat-label">待付款订单（笔）</div>
              <div class="stat-sub">待收 {{ formatMoney(summary?.pendingPayAmount) }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card avg-duration">
          <div class="stat-content">
            <div class="stat-icon"><el-icon><Warning /></el-icon></div>
            <div class="stat-info">
              <div class="stat-value">{{ summary?.highRiskCount ?? '-' }}</div>
              <div class="stat-label">高流失风险</div>
              <div class="stat-sub">核心 {{ summary?.coreCount ?? 0 }} / 增长 {{ summary?.growthCount ?? 0 }} / 边缘 {{ summary?.edgeCount ?? 0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 真实数据图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">近 30 天下单趋势</span>
            <el-tag size="small" type="info">柱=有效/待付款/已取消三段堆叠，合计为下单数 / 线=有效支付金额</el-tag>
          </div>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <div class="chart-header">
            <span class="chart-title">会员价值分层分布</span>
            <el-button size="small" @click="refreshAll">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
          <div ref="layerChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

      </el-tab-pane>

      <!-- ==================== 用户行为与留存分析：行为 / 留存 / 活跃 / 转化 ==================== -->
      <el-tab-pane label="行为与留存分析" name="analytics">
        <el-alert
          type="warning"
          :closable="false"
          show-icon
          class="dq-alert"
          title="四维度实时聚合：行为 / 留存 / 活跃 / 转化（全部来自真实库表，非模拟数据）"
        >
          <div class="dq-line">
            时间范围 <b>{{ analyticsRangeText }}</b> · 区间事件 <b>{{ analyticsData?.dataQuality?.rangeEventRows ?? 0 }}</b> 条 ·
            已采集 <b>{{ collectedTypeText }}</b> · 未采集 <b>{{ missingTypeText }}</b>
          </div>
          <div class="dq-line dq-sub">
            {{ analyticsData?.dataQuality?.note }} 生成时间 {{ analyticsData?.dataQuality?.generatedAt || '-' }}。
            未采集维度显示「未采集」，不补 0、不编数。
          </div>
        </el-alert>

        <el-card class="analytics-filter" shadow="never" v-loading="analyticsLoading">
          <el-form :inline="true" class="analytics-form">
            <el-form-item label="时间范围">
              <el-date-picker
                v-model="analyticsDateRange"
                type="daterange"
                value-format="YYYY-MM-DD"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                :clearable="false"
                style="width: 250px"
              />
            </el-form-item>
            <el-form-item>
              <el-button-group>
                <el-button size="small" @click="setAnalyticsRange(7)">近 7 天</el-button>
                <el-button size="small" @click="setAnalyticsRange(30)">近 30 天</el-button>
                <el-button size="small" @click="setAnalyticsRange(90)">近 90 天</el-button>
              </el-button-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="loadAnalytics">查询</el-button>
              <el-button @click="resetAnalyticsRange">重置</el-button>
              <el-button type="success" :disabled="!analyticsData" @click="exportAnalytics">
                <el-icon><Download /></el-icon> 导出
              </el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <el-row :gutter="16" class="stats-cards">
          <el-col :span="6">
            <el-card class="stat-card active-users"><div class="stat-content">
              <div class="stat-icon"><el-icon><TrendCharts /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ analyticsData?.active?.summary?.latestDau ?? '-' }}</div>
                <div class="stat-label">DAU（当日活跃）</div>
                <div class="stat-sub">区间日均 {{ analyticsData?.active?.summary?.avgDau ?? '-' }} · 峰值 {{ analyticsData?.active?.summary?.peakDau ?? '-' }}</div>
              </div>
            </div></el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card total-visits"><div class="stat-content">
              <div class="stat-icon"><el-icon><User /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ analyticsData?.active?.summary?.latestWau ?? '-' }}</div>
                <div class="stat-label">WAU（近 7 日活跃）</div>
                <div class="stat-sub">滚动窗口去重会员数</div>
              </div>
            </div></el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card today-visits"><div class="stat-content">
              <div class="stat-icon"><el-icon><View /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ analyticsData?.active?.summary?.latestMau ?? '-' }}</div>
                <div class="stat-label">MAU（近 30 日活跃）</div>
                <div class="stat-sub">区间活跃会员 {{ analyticsData?.active?.summary?.rangeActiveUsers ?? '-' }} 人</div>
              </div>
            </div></el-card>
          </el-col>
          <el-col :span="6">
            <el-card class="stat-card avg-duration"><div class="stat-content">
              <div class="stat-icon"><el-icon><PriceTag /></el-icon></div>
              <div class="stat-info">
                <div class="stat-value">{{ retentionD1Text }}</div>
                <div class="stat-label">次日留存</div>
                <div class="stat-sub">{{ retentionCohort === 'byActive' ? '活跃用户口径' : '新增注册用户口径' }} · 观察期未满记「观察中」</div>
              </div>
            </div></el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="chart-row">
          <el-col :span="12">
            <el-card class="chart-card">
              <div class="chart-header">
                <span class="chart-title">行为频次趋势</span>
                <el-tag size="small" type="info">仅绘制已采集维度</el-tag>
              </div>
              <div ref="behaviorTrendChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <div class="chart-header"><span class="chart-title">DAU / WAU / MAU 活跃趋势</span></div>
              <div ref="activeTrendChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="chart-row">
          <el-col :span="12">
            <el-card class="chart-card">
              <div class="chart-header">
                <span class="chart-title">留存分析（次日 / 7 日 / 30 日）</span>
                <el-select v-model="retentionCohort" size="small" style="width: 150px" @change="renderRetentionChart">
                  <el-option label="活跃用户留存" value="byActive" />
                  <el-option label="新增注册留存" value="byRegister" />
                </el-select>
              </div>
              <div ref="retentionChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card class="chart-card">
              <div class="chart-header"><span class="chart-title">注册到首购转化漏斗</span></div>
              <div ref="funnelChartRef" class="chart-container"></div>
            </el-card>
          </el-col>
        </el-row>

        <el-card class="analytics-table-card" shadow="never">
          <div class="chart-header">
            <span class="chart-title">行为频次汇总</span>
            <span class="analytics-hint">频次＝事件次数；人数＝去重会员；占比＝占区间全部事件</span>
          </div>
          <el-table :data="behaviorSummary" size="small" border stripe>
            <el-table-column prop="label" label="行为类型" width="110" />
            <el-table-column prop="count" label="频次" width="110" />
            <el-table-column prop="users" label="涉及会员" width="110" />
            <el-table-column label="人均频次" width="110">
              <template #default="{ row }">{{ row.avgPerUser ?? '-' }}</template>
            </el-table-column>
            <el-table-column label="占比" width="110">
              <template #default="{ row }">{{ row.share === null || row.share === undefined ? '-' : row.share + '%' }}</template>
            </el-table-column>
            <el-table-column label="采集状态">
              <template #default="{ row }">
                <el-tag size="small" :type="row.collected ? 'success' : 'info'">{{ row.collected ? '已采集' : '未采集' }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="analytics-table-card" shadow="never">
          <div class="chart-header">
            <span class="chart-title">留存分群明细（按基准日）</span>
            <span class="analytics-hint">{{ analyticsData?.retention?.rule }}</span>
          </div>
          <el-table :data="retentionCohorts" size="small" border stripe max-height="320">
            <el-table-column prop="date" label="基准日" width="120" />
            <el-table-column prop="baseUsers" label="基准人数" width="100" />
            <el-table-column label="次日留存">
              <template #default="{ row }">{{ retentionCell(row, 1) }}</template>
            </el-table-column>
            <el-table-column label="7 日留存">
              <template #default="{ row }">{{ retentionCell(row, 7) }}</template>
            </el-table-column>
            <el-table-column label="30 日留存">
              <template #default="{ row }">{{ retentionCell(row, 30) }}</template>
            </el-table-column>
          </el-table>
        </el-card>

        <el-card class="analytics-table-card" shadow="never">
          <div class="chart-header">
            <span class="chart-title">注册到首购漏斗明细</span>
            <span class="analytics-hint">{{ analyticsData?.funnel?.rule }}</span>
          </div>
          <el-table :data="funnelLevels" size="small" border stripe>
            <el-table-column prop="name" label="环节" width="160" />
            <el-table-column label="人数">
              <template #default="{ row }">{{ row.collected ? row.count : '未采集' }}</template>
            </el-table-column>
            <el-table-column label="上一级转化">
              <template #default="{ row }">{{ row.stepRate === null || row.stepRate === undefined ? '-' : row.stepRate + '%' }}</template>
            </el-table-column>
            <el-table-column label="注册整体转化">
              <template #default="{ row }">{{ row.totalRate === null || row.totalRate === undefined ? '-' : row.totalRate + '%' }}</template>
            </el-table-column>
          </el-table>
          <div class="analytics-foot">
            区间新增注册 <b>{{ analyticsData?.funnel?.newRegistered ?? 0 }}</b> 人 · 区间下单 <b>{{ analyticsData?.funnel?.rangeOrderRows ?? 0 }}</b> 笔 ·
            有效成交 <b>{{ analyticsData?.funnel?.rangePaidRows ?? 0 }}</b> 笔 · 注册到首购平均 <b>{{ analyticsData?.funnel?.avgDaysToFirstOrder ?? '-' }}</b> 天
          </div>
        </el-card>
      </el-tab-pane>

      <!-- ==================== RFM 八分群 ==================== -->
      <el-tab-pane label="RFM 八分群" name="rfm">
        <el-alert type="info" :closable="false" show-icon class="dq-alert"
          :title="rfmData?.rule || 'R/F/M 各按固定阈值打 1-5 分，以 3 分为高/低分界组合成八象限'">
          <div class="dq-line">
            共 <b>{{ rfmData?.total ?? 0 }}</b> 名会员，其中有成交 <b>{{ rfmData?.tradedTotal ?? 0 }}</b> 人进入八象限；
            没有有效订单的会员不强行塞进八象限，按「下单未成交（待付款或已取消）」与「未成交会员」单独成档。
          </div>
          <div class="dq-line dq-sub">
            分档阈值：R（最近活跃≤7天=5分，≤30=4，≤60=3，≤90=2，更久=1）；F（1单=1分，2-3单=2，4-6单=3，7-15单=4，15单以上=5）；
            M（消费&lt;100=1分，&lt;500=2，&lt;2000=3，&lt;5000=4，≥5000=5）。
          </div>
        </el-alert>

        <el-row :gutter="16" class="rfm-grid" v-loading="rfmLoading">
          <el-col v-for="seg in rfmSegments" :key="seg.segment" :span="6">
            <el-card
              shadow="hover"
              class="rfm-card"
              :class="{ 'rfm-card-active': activeSegment && activeSegment.segment === seg.segment }"
              @click="selectRfmSegment(seg)"
            >
              <div class="rfm-card-head">
                <span class="rfm-card-title">{{ seg.label }}</span>
                <el-tag size="small" effect="plain">{{ seg.count }} 人</el-tag>
              </div>
              <div class="rfm-card-meta">人均消费 {{ formatMoney(seg.avgAmount) }} · 累计 {{ seg.totalOrders }} 单</div>
              <div class="rfm-card-advice">{{ seg.advice }}</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card v-if="activeSegment" shadow="never" class="table-card">
          <div class="table-header">
            <span class="table-title">{{ activeSegment.label }} · 成员（{{ activeSegment.members.length }}）</span>
            <el-button size="small" @click="activeSegment = null">收起</el-button>
          </div>
          <el-table :data="activeSegment.members" stripe border size="small" max-height="420">
            <el-table-column label="会员" min-width="160">
              <template #default="{ row }">
                <el-button link type="primary" @click="openPortrait(row.userId)">{{ row.nickname || '未设昵称' }}</el-button>
                <div class="cell-sub">{{ row.userId }}</div>
              </template>
            </el-table-column>
            <el-table-column label="有效订单" width="90" prop="orderCount" />
            <el-table-column label="累计消费" width="120">
              <template #default="{ row }">{{ formatMoney(row.orderAmount) }}</template>
            </el-table-column>
            <el-table-column label="待付款" width="160">
              <template #default="{ row }">
                <span v-if="row.pendingPayCount">{{ row.pendingPayCount }} 笔 / {{ formatMoney(row.pendingPayAmount) }}</span>
                <span v-else class="cell-muted">无</span>
              </template>
            </el-table-column>
            <el-table-column label="最近活跃" width="110">
              <template #default="{ row }">{{ row.lastActiveDays == null ? '-' : row.lastActiveDays + ' 天前' }}</template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>

      <!-- ==================== 智能分群 ==================== -->
      <el-tab-pane label="智能分群" name="segment">
        <el-row :gutter="16">
          <el-col :span="9">
            <el-card shadow="never" class="search-card">
              <div class="table-header"><span class="table-title">圈人条件</span></div>
              <el-form :model="segment" label-width="88px" size="small">
                <el-form-item label="关键词">
                  <el-input v-model="segment.keyword" placeholder="昵称 / 手机号 / 用户ID" clearable />
                </el-form-item>
                <el-form-item label="价值分层">
                  <el-select v-model="segment.layer" placeholder="不限" clearable style="width: 100%">
                    <el-option v-for="l in layerOptions" :key="l.value" :label="l.label" :value="l.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="RFM 分群">
                  <el-select v-model="segment.rfmSegment" placeholder="不限" clearable style="width: 100%">
                    <el-option v-for="s in rfmSegments" :key="s.segment" :label="`${s.label}（${s.count}）`" :value="s.segment" />
                  </el-select>
                </el-form-item>
                <el-form-item label="生命周期">
                  <el-select v-model="segment.lifecycle" placeholder="不限" clearable style="width: 100%">
                    <el-option v-for="l in lifecycleOptions" :key="l.value" :label="l.label" :value="l.value" />
                  </el-select>
                </el-form-item>
                <el-form-item label="有效订单">
                  <div class="range-row">
                    <el-input-number v-model="segment.minOrders" :min="0" :max="9999" :controls="false" placeholder="最少" />
                    <span class="range-sep">-</span>
                    <el-input-number v-model="segment.maxOrders" :min="0" :max="9999" :controls="false" placeholder="最多" />
                  </div>
                </el-form-item>
                <el-form-item label="累计消费">
                  <div class="range-row">
                    <el-input-number v-model="segment.minAmount" :min="0" :max="9999999" :controls="false" placeholder="最低" />
                    <span class="range-sep">-</span>
                    <el-input-number v-model="segment.maxAmount" :min="0" :max="9999999" :controls="false" placeholder="最高" />
                  </div>
                </el-form-item>
                <el-form-item label="待付款≥">
                  <el-input-number v-model="segment.minPending" :min="0" :max="999" :controls="false" />
                  <span class="form-hint">笔（催付场景填 1）</span>
                </el-form-item>
                <el-form-item label="未活跃≥">
                  <el-input-number v-model="segment.inactiveDays" :min="0" :max="3650" :controls="false" />
                  <span class="form-hint">天（召回场景填 30/60/90）</span>
                </el-form-item>
                <el-form-item label="下单情况">
                  <el-radio-group v-model="segment.hasOrder">
                    <el-radio :value="null">不限</el-radio>
                    <el-radio :value="true">有订单</el-radio>
                    <el-radio :value="false">从未下单</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="打标情况">
                  <el-radio-group v-model="segment.hasTag">
                    <el-radio :value="null">不限</el-radio>
                    <el-radio :value="true">已打标</el-radio>
                    <el-radio :value="false">未打标</el-radio>
                  </el-radio-group>
                </el-form-item>
                <el-form-item label="命中标签">
                  <el-select v-model="segment.tagIds" multiple collapse-tags placeholder="不限" style="width: 100%">
                    <el-option v-for="t in tagList" :key="t.tagId" :label="`${t.tagName}（${t.memberCount}）`" :value="t.tagId" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="runSegment">
                    <el-icon><Search /></el-icon>
                    预估人数
                  </el-button>
                  <el-button @click="resetSegment">重置</el-button>
                </el-form-item>
              </el-form>
              <div class="segment-rule">当前条件：{{ segmentResult?.ruleText || '尚未执行' }}</div>
              <div class="segment-save">
                <el-input v-model="crowdForm.crowdName" placeholder="人群包名称" size="small" style="width: 150px" />
                <el-input v-model="crowdForm.remark" placeholder="备注（可选）" size="small" style="width: 150px" />
                <el-button type="success" size="small" @click="handleSaveCrowd">保存人群包</el-button>
              </div>
            </el-card>
          </el-col>
          <el-col :span="15">
            <el-card shadow="never" class="table-card">
              <div class="table-header">
                <span class="table-title">
                  命中 <b class="hit-num">{{ segmentResult?.total ?? 0 }}</b> 人
                  <span class="cell-muted">/ 全量 {{ segmentResult?.allMemberTotal ?? summary?.memberTotal ?? 0 }} 人</span>
                </span>
                <el-button size="small" :disabled="!segmentRows.length" @click="exportSegment">
                  <el-icon><Download /></el-icon>
                  导出命中名单
                </el-button>
              </div>
              <el-table :data="segmentRows" v-loading="segmentLoading" stripe border size="small" max-height="430">
                <el-table-column label="会员" min-width="150">
                  <template #default="{ row }">
                    <el-button link type="primary" @click="openPortrait(row.userId)">{{ row.nickname || '未设昵称' }}</el-button>
                  </template>
                </el-table-column>
                <el-table-column label="分层" width="100">
                  <template #default="{ row }">
                    <el-tag size="small" :type="layerTagType(row.layer)">{{ row.layerLabel }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="RFM 分群" width="120" prop="rfmLabel" />
                <el-table-column label="生命周期" width="96" prop="lifecycleLabel" />
                <el-table-column label="有效订单" width="90" prop="orderCount" />
                <el-table-column label="累计消费" width="110">
                  <template #default="{ row }">{{ formatMoney(row.orderAmount) }}</template>
                </el-table-column>
                <el-table-column label="待付款" width="80" prop="pendingPayCount" />
                <el-table-column label="风险" width="90">
                  <template #default="{ row }">
                    <el-tag size="small" :type="riskTagType(row.riskScore)">{{ row.riskScore }}</el-tag>
                  </template>
                </el-table-column>
              </el-table>
            </el-card>

            <el-card shadow="never" class="table-card">
              <div class="table-header">
                <span class="table-title">已保存人群包（{{ crowdList.length }}）</span>
                <el-button size="small" @click="refreshCrowds">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
              <el-table :data="crowdList" stripe border size="small" v-loading="crowdLoading">
                <el-table-column label="名称" min-width="130" prop="crowdName" />
                <el-table-column label="人数" width="80" prop="memberCount" />
                <el-table-column label="规则快照" min-width="240">
                  <template #default="{ row }"><span class="cell-sub">{{ row.ruleText }}</span></template>
                </el-table-column>
                <el-table-column label="创建时间" width="160" prop="createTime" />
                <el-table-column label="操作" width="80" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="danger" size="small" @click="handleDeleteCrowd(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="!crowdList.length" class="empty-hint">
                还没有保存过人群包。设置条件后点「保存人群包」，会连同当时的成员快照一起落库，之后可复盘当时的筛选口径。
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ==================== 标签体系 ==================== -->
      <el-tab-pane label="标签体系" name="tag">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-card shadow="never" class="search-card">
              <div class="table-header"><span class="table-title">新建标签</span></div>
              <el-form :model="tagForm" label-width="64px" size="small">
                <el-form-item label="名称">
                  <el-input v-model="tagForm.tagName" placeholder="如：高复购潜力" maxlength="40" show-word-limit />
                </el-form-item>
                <el-form-item label="权重">
                  <el-input-number v-model="tagForm.tagWeight" :min="0" :max="999" :controls="false" />
                  <span class="form-hint">越大越靠前</span>
                </el-form-item>
                <el-form-item label="说明">
                  <el-input v-model="tagForm.tagDescribe" type="textarea" :rows="2" placeholder="这个标签用来圈什么人" maxlength="200" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSaveTag">保存标签</el-button>
                </el-form-item>
              </el-form>
              <div class="segment-rule">
                标签字典存 <code>hou_user_tag_info</code>，打标关系存 <code>hou_base_tag_class</code>（tag_id 逗号串），
                与后台既有标签体系同一套表，不另起一套。
              </div>
            </el-card>
          </el-col>
          <el-col :span="16">
            <el-card shadow="never" class="table-card">
              <div class="table-header">
                <span class="table-title">
                  标签字典（{{ tagList.length }} 个，已覆盖会员的 {{ tagData?.usedTagCount ?? 0 }} 个）
                </span>
                <el-button size="small" @click="refreshTags">
                  <el-icon><Refresh /></el-icon>
                  刷新
                </el-button>
              </div>
              <el-table :data="tagList" stripe border size="small" v-loading="tagLoading">
                <el-table-column label="标签" min-width="140">
                  <template #default="{ row }">
                    <el-tag size="small" effect="plain" :style="{ color: row.tagColor || '#409EFF', borderColor: row.tagColor || '#409EFF' }">{{ row.tagName }}</el-tag>
                    <span v-if="row.tagIsactive === 0" class="cell-muted">（已停用）</span>
                  </template>
                </el-table-column>
                <el-table-column label="覆盖人数" width="100" prop="memberCount" />
                <el-table-column label="权重" width="70" prop="tagWeight" />
                <el-table-column label="说明" min-width="180">
                  <template #default="{ row }">{{ row.tagDescribe || '-' }}</template>
                </el-table-column>
                <el-table-column label="创建时间" width="160" prop="createTime" />
                <el-table-column label="操作" width="140" fixed="right">
                  <template #default="{ row }">
                    <el-button link type="primary" size="small" @click="viewTagMembers(row)">看成员</el-button>
                    <el-button link type="danger" size="small" @click="handleDeleteTag(row)">删除</el-button>
                  </template>
                </el-table-column>
              </el-table>
              <div v-if="!tagList.length" class="empty-hint">
                标签字典目前为空（<code>hou_user_tag_info</code> 0 行）。先在左侧新建标签，再到「会员列表」勾选会员批量打标。
              </div>
            </el-card>
          </el-col>
        </el-row>
      </el-tab-pane>

      <!-- ==================== 会员列表 ==================== -->
      <el-tab-pane label="会员列表" name="member">

    <!-- 搜索筛选 -->
    <el-card class="search-card">
      <el-form :model="query" :inline="true" class="search-form">
        <el-form-item label="会员">
          <el-input
            v-model="query.keyword"
            placeholder="昵称 / 手机号 / 用户ID"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="分层">
          <el-select v-model="query.layer" placeholder="全部分层" clearable style="width: 140px">
            <el-option v-for="l in layerOptions" :key="l.value" :label="l.label" :value="l.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="最少订单">
          <el-input-number v-model="query.minOrders" :min="0" :max="9999" controls-position="right" style="width: 120px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作 -->
    <el-card class="action-card">
      <el-row :gutter="10">
        <el-col :span="24">
          <el-button type="primary" @click="openLayerDrawer">
            <el-icon><TrendCharts /></el-icon>
            会员画像分层
          </el-button>
          <el-button type="success" @click="exportCsv">
            <el-icon><Download /></el-icon>
            导出当前页
          </el-button>
          <span class="batch-tag">
            <span class="batch-tag-label">批量打标（已选 {{ selectedRows.length }} 人）：</span>
            <el-select v-model="batchTagId" placeholder="选择标签" size="small" style="width: 160px" clearable>
              <el-option v-for="t in tagList" :key="t.tagId" :label="`${t.tagName}（${t.memberCount}）`" :value="t.tagId" />
            </el-select>
            <el-button size="small" type="primary" plain @click="handleBatchTag(true)">
              <el-icon><PriceTag /></el-icon>
              打标
            </el-button>
            <el-button size="small" plain @click="handleBatchTag(false)">取消打标</el-button>
            <el-button v-if="!tagList.length" link type="primary" size="small" @click="activeTab = 'tag'">先去创建标签</el-button>
          </span>
          <span class="table-hint">
            口径：有效订单（order_status NOT IN (1,7)）；R/F/M、分群与分层由后端按固定规则计算
          </span>
        </el-col>
      </el-row>
    </el-card>

    <!-- 会员画像列表 -->
    <el-card class="table-card">
      <div class="table-header">
        <span class="table-title">会员画像列表</span>
        <div class="table-controls">
          <el-button size="small" @click="refreshAll">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </div>
      </div>

      <el-table :data="records" v-loading="loading" stripe border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" />
        <el-table-column label="会员" min-width="180">
          <template #default="{ row }">
            <div class="cell-main">{{ row.nickname || '未设昵称' }}</div>
            <div class="cell-sub">{{ row.userId }}</div>
          </template>
        </el-table-column>
        <el-table-column label="手机号" width="130">
          <template #default="{ row }">{{ maskMobile(row.mobile) }}</template>
        </el-table-column>
        <el-table-column label="累计登录" width="150">
          <template #default="{ row }">
            <div class="cell-main">{{ row.loginCount ?? 0 }} 次</div>
            <div class="cell-sub">{{ row.lastLoginTime || '无记录' }}</div>
          </template>
        </el-table-column>
        <el-table-column label="订单" width="120">
          <template #default="{ row }">
            <div class="cell-main">有效 {{ row.orderCount || 0 }}</div>
            <div class="cell-sub" :class="{ 'cell-muted': !row.pendingPayCount }">待付款 {{ row.pendingPayCount || 0 }}</div>
          </template>
        </el-table-column>
        <el-table-column label="累计消费" width="130">
          <template #default="{ row }">{{ formatMoney(row.orderAmount) }}</template>
        </el-table-column>
        <el-table-column label="最近下单" width="170">
          <template #default="{ row }">
            <span :class="{ 'cell-muted': !row.lastOrderTime }">{{ row.lastOrderTime || '无订单' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="分层" width="110">
          <template #default="{ row }">
            <el-tag :type="layerTagType(row.layer)" size="small">{{ row.layerLabel }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="流失风险" width="130">
          <template #default="{ row }">
            <el-tag :type="riskTagType(row.riskScore)" size="small">{{ row.riskLabel }} {{ row.riskScore }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="手工标签" min-width="150">
          <template #default="{ row }">
            <template v-if="row.manualTags && row.manualTags.length">
              <el-tag v-for="t in row.manualTags" :key="t" size="small" effect="plain" class="mr4">{{ t }}</el-tag>
            </template>
            <span v-else class="cell-muted">未打标</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openPortrait(row.userId)">查看画像</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        class="pagination"
      />
    </el-card>

      </el-tab-pane>
    </el-tabs>

    <!-- 会员画像详情：全部字段来自真实聚合 -->
    <el-dialog v-model="portraitVisible" title="会员画像（真实数据聚合）" width="1080px" top="5vh">
      <div v-if="portrait" class="portrait-content">
        <div class="portrait-hero">
          <div class="hero-left">
            <el-avatar :size="52" class="hero-avatar">{{ (portrait.nickname || '?').slice(0, 1) }}</el-avatar>
            <div>
              <div class="hero-name">
                {{ portrait.nickname || '未设昵称' }}
                <el-tag :type="layerTagType(portrait.layer)" size="small" effect="dark">{{ portrait.layerLabel }}</el-tag>
                <el-tag size="small" type="success" effect="plain">{{ portrait.rfmLabel }}</el-tag>
                <el-tag size="small" type="info" effect="plain">{{ portrait.lifecycleLabel }}</el-tag>
                <el-tag size="small" :type="riskTagType(portrait.riskScore)">{{ portrait.riskLabel }} {{ portrait.riskScore }}</el-tag>
                <el-tag v-if="!portrait.hasOrderData" size="small" type="info" effect="plain">无有效成交</el-tag>
                <el-tag v-if="!portrait.hasBrowseData" size="small" type="info" effect="plain">浏览未采集</el-tag>
                <el-tag v-if="!portrait.hasManualTag" size="small" type="info" effect="plain">未打标</el-tag>
              </div>
              <div class="hero-sub">
                {{ maskMobile(portrait.mobile) }} · {{ portrait.userId }} · {{ portrait.gradeName || '无等级' }} · 注册 {{ portrait.registerTime || '-' }} · 终端 {{ portrait.registerTerminal || '-' }}
              </div>
            </div>
          </div>
          <div class="hero-metrics">
            <div class="hero-metric">
              <span class="hm-value">{{ formatMoney(portrait.orderAmount) }}</span>
              <span class="hm-label">累计消费</span>
            </div>
            <div class="hero-metric">
              <span class="hm-value">{{ portrait.orderCount || 0 }}</span>
              <span class="hm-label">有效订单</span>
            </div>
            <div class="hero-metric">
              <span class="hm-value">{{ portrait.loginCount || 0 }}</span>
              <span class="hm-label">累计登录</span>
            </div>
            <div class="hero-metric">
              <span class="hm-value">{{ lastActiveText(portrait) }}</span>
              <span class="hm-label">最近活跃</span>
            </div>
          </div>
        </div>

        <el-row :gutter="16" class="portrait-block">
          <el-col :span="12">
            <el-card shadow="never" class="portrait-card">
              <h4>RFM 价值分层</h4>
              <div v-for="item in rfmRows" :key="item.key" class="rfm-row">
                <span class="rfm-label">{{ item.label }}</span>
                <el-progress
                  :percentage="item.score"
                  :stroke-width="10"
                  :color="item.color"
                  :show-text="false"
                  class="rfm-bar"
                />
                <span class="rfm-score">{{ item.score }}</span>
              </div>
              <div class="rfm-legend">{{ rfmLegend }}</div>
              <div class="rfm-segment-line">
                <span class="rfm-seg-label">RFM 分群</span>
                <el-tag size="small" type="success" effect="dark">{{ portrait.rfmLabel }}</el-tag>
                <span class="rfm-seg-score">R{{ portrait.rScore5 ?? 0 }} · F{{ portrait.fScore5 ?? 0 }} · M{{ portrait.mScore5 ?? 0 }}</span>
              </div>
              <div class="rfm-legend">{{ portrait.rfmAdvice }}</div>
            </el-card>
          </el-col>
          <el-col :span="12">
            <el-card shadow="never" class="portrait-card">
              <h4>流失风险与挽留</h4>
              <div class="risk-head">
                <span class="risk-score" :style="{ color: riskColor(portrait.riskScore) }">{{ portrait.riskScore }}</span>
                <div class="risk-head-right">
                  <el-tag :type="riskTagType(portrait.riskScore)" size="small">{{ portrait.riskLabel }}</el-tag>
                  <div class="risk-reason">
                    {{ portrait.riskReasons && portrait.riskReasons.length ? portrait.riskReasons.join('；') : '暂无风险成因' }}
                  </div>
                </div>
              </div>
              <ul class="advice-list">
                <li v-for="(a, i) in portrait.advice" :key="i">{{ a }}</li>
              </ul>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="portrait-block">
          <el-col :span="8">
            <el-card shadow="never" class="portrait-card">
              <h4>会员信息（hou_user_info）</h4>
              <el-descriptions :column="1" size="small" border>
                <el-descriptions-item label="会员ID">{{ portrait.userId }}</el-descriptions-item>
                <el-descriptions-item label="昵称">{{ portrait.nickname || '-' }}</el-descriptions-item>
                <el-descriptions-item label="手机号">{{ maskMobile(portrait.mobile) }}</el-descriptions-item>
                <el-descriptions-item label="注册时间">{{ portrait.registerTime || '-' }}</el-descriptions-item>
                <el-descriptions-item label="注册终端">{{ portrait.registerTerminal || '-' }}</el-descriptions-item>
                <el-descriptions-item label="账号类型">{{ portrait.isAdmin === 1 ? '后台账号' : '普通会员' }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="portrait-card">
              <h4>交易画像（hou_order_base）</h4>
              <el-descriptions :column="1" size="small" border>
                <el-descriptions-item label="有效订单">{{ portrait.orderCount || 0 }} 笔</el-descriptions-item>
                <el-descriptions-item label="累计消费">{{ formatMoney(portrait.orderAmount) }}</el-descriptions-item>
                <el-descriptions-item label="客单价">{{ formatMoney(portrait.avgOrderAmount) }}</el-descriptions-item>
                <el-descriptions-item label="下单天数">{{ portrait.orderDays || 0 }} 天</el-descriptions-item>
                <el-descriptions-item label="最近下单">{{ portrait.lastOrderTime || '无订单' }}</el-descriptions-item>
                <el-descriptions-item label="待付款">
                  {{ portrait.pendingPayCount || 0 }} 笔 / {{ formatMoney(portrait.pendingPayAmount) }}
                </el-descriptions-item>
                <el-descriptions-item label="偏好商品">{{ portrait.favGoodsName || '暂无' }}</el-descriptions-item>
                <el-descriptions-item label="偏好店铺">{{ portrait.favShopName || '暂无' }}</el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card shadow="never" class="portrait-card">
              <h4>登录与浏览</h4>
              <el-descriptions :column="1" size="small" border>
                <el-descriptions-item label="累计登录">{{ portrait.loginCount || 0 }} 次</el-descriptions-item>
                <el-descriptions-item label="最后登录">{{ portrait.lastLoginTime || '无记录' }}</el-descriptions-item>
                <el-descriptions-item label="最后登录IP">{{ portrait.lastLoginIp || '-' }}</el-descriptions-item>
                <el-descriptions-item label="浏览足迹">
                  <span v-if="portrait.hasBrowseData">{{ portrait.browseCount }} 条</span>
                  <span v-else class="cell-muted">未采集</span>
                </el-descriptions-item>
                <el-descriptions-item label="浏览商品">
                  <template v-if="portrait.browseGoods && portrait.browseGoods.length">
                    {{ portrait.browseGoods.join('、') }}
                  </template>
                  <span v-else class="cell-muted">未采集</span>
                </el-descriptions-item>
              </el-descriptions>
            </el-card>
          </el-col>
        </el-row>

        <el-row :gutter="16" class="portrait-block">
          <el-col :span="10">
            <el-card shadow="never" class="portrait-card">
              <h4>购买偏好（hou_order_goods）</h4>
              <div class="pref-block">
                <div class="pref-title">常购商品 TOP</div>
                <div v-if="portrait.prefGoods && portrait.prefGoods.length" class="pref-list">
                  <div v-for="(g, i) in portrait.prefGoods" :key="i" class="pref-item">
                    <span class="pref-name">{{ i + 1 }}. {{ g.goodsName }}</span>
                    <span class="pref-val">{{ g.cnt }} 件 · {{ formatMoney(g.amount) }}</span>
                  </div>
                </div>
                <div v-else class="cell-muted">暂无成交商品</div>
              </div>
              <div class="pref-block">
                <div class="pref-title">常购店铺 TOP</div>
                <div v-if="portrait.prefShop && portrait.prefShop.length" class="pref-list">
                  <div v-for="(s, i) in portrait.prefShop" :key="i" class="pref-item">
                    <span class="pref-name">{{ i + 1 }}. {{ s.shopName }}</span>
                    <span class="pref-val">{{ s.cnt }} 件 · {{ formatMoney(s.amount) }}</span>
                  </div>
                </div>
                <div v-else class="cell-muted">暂无成交店铺</div>
              </div>
              <div class="pref-block">
                <div class="pref-title">价格带分布</div>
                <div v-if="portrait.priceBands && portrait.priceBands.length" class="pref-list">
                  <div v-for="b in portrait.priceBands" :key="b.band" class="pref-item">
                    <span class="pref-name">{{ b.band }} 元</span>
                    <span class="pref-val">{{ b.cnt }} 件 · {{ formatMoney(b.amount) }}</span>
                  </div>
                </div>
                <div v-else class="cell-muted">暂无成交商品</div>
                <div class="pref-note">
                  品类维度：{{ portrait.classAvailable ? '已采集' : '未采集（hou_order_goods.goods_class_id 全为空）' }}
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :span="14">
            <el-card shadow="never" class="portrait-card">
              <h4>行为时间线（登录 + 下单，最近 50 条）</h4>
              <el-timeline v-if="portrait.timeline && portrait.timeline.length" class="timeline-box">
                <el-timeline-item
                  v-for="(t, i) in portrait.timeline"
                  :key="i"
                  :timestamp="t.eventTime"
                  :type="t.eventType === 'order' ? 'warning' : 'primary'"
                >
                  <span class="tl-title">{{ t.title }}</span>
                  <span class="tl-detail">{{ t.detail }}</span>
                </el-timeline-item>
              </el-timeline>
              <div v-else class="cell-muted">该会员没有登录或下单记录</div>
            </el-card>
          </el-col>
        </el-row>

        <el-card shadow="never" class="portrait-card portrait-block">
          <h4>运营手工标签（hou_base_tag_class）</h4>
          <div class="tag-row">
            <template v-if="portrait.manualTags && portrait.manualTags.length">
              <el-tag v-for="t in portrait.manualTags" :key="t" class="interest-tag" effect="plain">{{ t }}</el-tag>
            </template>
            <span v-else class="cell-muted">该会员尚未被运营打标签（库中无有效标签关联记录）</span>
          </div>
          <h4 class="mt">画像结论</h4>
          <ul class="advice-list">
            <li v-for="(c, i) in portraitConclusion" :key="i">{{ c }}</li>
          </ul>
        </el-card>
      </div>
      <el-skeleton v-else :rows="6" animated />
    </el-dialog>

    <!-- 分层抽屉：规模与成员全部来自真实分层结果 -->
    <el-drawer v-model="layerDrawerVisible" title="会员画像分层（真实分层结果）" size="760px">
      <div class="layer-summary">
        <div
          v-for="item in layerCards"
          :key="item.key"
          class="layer-summary-item"
          :style="{ borderColor: item.color }"
        >
          <div class="ls-value" :style="{ color: item.color }">{{ item.count }}</div>
          <div class="ls-label">{{ item.label }}</div>
          <div class="ls-sub">占比 {{ item.ratio }}%</div>
        </div>
      </div>

      <el-card
        v-for="layer in layerCards"
        :key="layer.key"
        shadow="never"
        class="layer-card"
        :style="{ borderTop: `3px solid ${layer.color}` }"
      >
        <div class="layer-head">
          <span class="layer-title">{{ layer.label }}</span>
          <el-tag size="small" effect="plain" :style="{ color: layer.color, borderColor: layer.color }">
            {{ layer.count }} 人 · {{ layer.ratio }}%
          </el-tag>
        </div>
        <div class="layer-meta">
          人均消费 {{ formatMoney(layer.avgAmount) }} · 人均订单 {{ layer.avgOrders }} 笔 · 高风险 {{ layer.riskCount }} 人
        </div>
        <div class="layer-meta">分层规则：{{ layer.rule }}</div>
        <div class="layer-users">
          <span class="layer-users-label">该层会员：</span>
          <el-button
            v-for="u in layer.users"
            :key="u.userId"
            link
            type="primary"
            size="small"
            @click="openPortrait(u.userId)"
          >
            {{ u.nickname || u.userId }}
          </el-button>
          <span v-if="!layer.users.length" class="layer-empty">暂无</span>
        </div>
      </el-card>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, TrendCharts, Download, View, User, Warning, PriceTag } from '@element-plus/icons-vue'
import {
  fetchMemberPortraitPage,
  fetchMemberPortraitDetail,
  fetchMemberPortraitRfm,
  previewMemberSegment,
  saveMemberCrowd,
  fetchMemberCrowdList,
  deleteMemberCrowd,
  fetchMemberTagList,
  saveMemberTag,
  deleteMemberTag,
  attachMemberTag,
  detachMemberTag
} from '@/api/user/portrait'
import { fetchBehaviorAnalytics } from '@/api/user/behaviorAnalytics'

const activeTab = ref('overview')
const loading = ref(false)
const records = ref([])
const summary = ref(null)

// ==================== RFM 八分群 ====================
const rfmLoading = ref(false)
const rfmData = ref(null)
const rfmSegments = computed(() => rfmData.value?.segments || [])
const activeSegment = ref(null)

// ==================== 智能分群 / 人群包 ====================
const lifecycleOptions = [
  { value: 'active', label: '活跃期' },
  { value: 'newbie', label: '新客期' },
  { value: 'growing', label: '成长期' },
  { value: 'silent', label: '沉默期' },
  { value: 'churn', label: '流失期' },
  { value: 'inactive', label: '未激活' }
]

const emptySegment = () => ({
  keyword: '',
  layer: '',
  rfmSegment: '',
  lifecycle: '',
  minOrders: null,
  maxOrders: null,
  minAmount: null,
  maxAmount: null,
  minPending: null,
  inactiveDays: null,
  hasOrder: null,
  hasTag: null,
  tagIds: []
})

const segment = reactive(emptySegment())
const segmentResult = ref(null)
const segmentRows = computed(() => segmentResult.value?.records || [])
const segmentLoading = ref(false)
const crowdForm = reactive({ crowdName: '', remark: '' })
const crowdList = ref([])
const crowdLoading = ref(false)

// ==================== 标签 ====================
const tagLoading = ref(false)
const tagData = ref(null)
const tagList = computed(() => tagData.value?.list || [])
const tagForm = reactive({ tagName: '', tagWeight: 0, tagDescribe: '' })

const dataQuality = computed(() => summary.value?.dataQuality || {})

// 已取消 = 订单总数 - 有效 - 待付款，与后端「有效＝排除 1/7」口径对齐，
// 让「30 笔订单却 0 笔有效」这种真实状态在页面上能自我解释。
const cancelledOrderRows = computed(() => {
  const rest =
    (dataQuality.value.orderRows || 0) -
    (dataQuality.value.validOrderRows || 0) -
    (summary.value?.pendingPayOrders || 0)
  return rest > 0 ? rest : 0
})

const query = reactive({
  keyword: '',
  layer: '',
  minOrders: null
})

const pagination = reactive({
  page: 1,
  size: 20,
  total: 0
})

const layerOptions = [
  { value: 'core', label: '核心用户', color: '#f56c6c' },
  { value: 'growth', label: '增长用户', color: '#e6a23c' },
  { value: 'edge', label: '边缘/沉睡', color: '#909399' }
]

const LAYER_RULES = {
  core: '近 30 天有下单，且有效订单 ≥3、累计消费 ≥1000 元',
  growth: '近 60 天有下单，或近 30 天有登录且累计登录 ≥5 次',
  edge: '不满足核心/增长条件（无订单或长期未活跃）'
}

// ==================== 数据加载 ====================

const isSuccess = (res) => res && (res.code === 1 || res.code === 200)

const fetchData = async () => {
  loading.value = true
  try {
    const res = await fetchMemberPortraitPage({
      keyword: query.keyword || undefined,
      layer: query.layer || undefined,
      minOrders: query.minOrders === null || query.minOrders === undefined ? undefined : query.minOrders,
      page: pagination.page,
      size: pagination.size
    })
    if (!isSuccess(res)) {
      ElMessage.error(res?.message || '加载会员画像失败')
      return
    }
    const data = res.data || {}
    records.value = data.records || []
    pagination.total = data.total || 0
    summary.value = data.summary || null
    await nextTick()
    renderCharts()
  } catch (e) {
    ElMessage.error('加载会员画像失败：' + (e?.message || '网络异常'))
  } finally {
    loading.value = false
  }
}

const refreshAll = () => fetchData()

// ==================== Tab 切换 ====================

const handleTabChange = (name) => {
  if (name === 'analytics' && !analyticsData.value) loadAnalytics()
  if (name === 'rfm' && !rfmData.value) loadRfm()
  if (name === 'segment' && !crowdList.value.length) refreshCrowds()
  if (['tag', 'segment', 'member'].includes(name)) refreshTags()
}

// ==================== RFM 八分群 ====================

const loadRfm = async () => {
  rfmLoading.value = true
  try {
    const res = await fetchMemberPortraitRfm()
    if (isSuccess(res)) {
      rfmData.value = res.data
      activeSegment.value = null
    } else {
      ElMessage.error(res?.message || 'RFM 分群加载失败')
    }
  } catch (e) {
    ElMessage.error('RFM 分群加载失败')
  } finally {
    rfmLoading.value = false
  }
}

const selectRfmSegment = (seg) => {
  activeSegment.value = activeSegment.value && activeSegment.value.segment === seg.segment ? null : seg
}

// ==================== 智能分群 / 人群包 ====================

const runSegment = async () => {
  segmentLoading.value = true
  try {
    const res = await previewMemberSegment({ ...segment, page: 1, size: 200 })
    if (isSuccess(res)) {
      segmentResult.value = res.data
    } else {
      ElMessage.error(res?.message || '圈人失败')
    }
  } catch (e) {
    ElMessage.error('圈人失败')
  } finally {
    segmentLoading.value = false
  }
}

const resetSegment = () => {
  Object.assign(segment, emptySegment())
  segmentResult.value = null
}

const handleSaveCrowd = async () => {
  if (!crowdForm.crowdName.trim()) {
    ElMessage.warning('请填写人群包名称')
    return
  }
  const res = await saveMemberCrowd({
    ...segment,
    crowdName: crowdForm.crowdName.trim(),
    remark: crowdForm.remark
  })
  if (isSuccess(res)) {
    ElMessage.success(`已保存，命中 ${res.data?.memberCount ?? 0} 人`)
    crowdForm.crowdName = ''
    crowdForm.remark = ''
    refreshCrowds()
  } else {
    ElMessage.error(res?.message || '保存失败')
  }
}

const refreshCrowds = async () => {
  crowdLoading.value = true
  try {
    const res = await fetchMemberCrowdList()
    if (isSuccess(res)) crowdList.value = res.data || []
  } catch (e) {
    // 人群包表未初始化时静默，不阻塞页面
  } finally {
    crowdLoading.value = false
  }
}

const handleDeleteCrowd = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除人群包「${row.crowdName}」？`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  const res = await deleteMemberCrowd(row.id)
  if (isSuccess(res)) {
    ElMessage.success('已删除')
    refreshCrowds()
  } else {
    ElMessage.error(res?.message || '删除失败')
  }
}

const exportSegment = () => {
  const rows = segmentRows.value
  if (!rows.length) return
  const header = ['会员ID', '昵称', '手机号', '分层', 'RFM分群', '生命周期', '有效订单', '累计消费', '待付款笔数', '待付款金额', '风险分', '风险等级']
  const lines = rows.map((r) => [
    r.userId, r.nickname || '', r.mobile || '', r.layerLabel || '', r.rfmLabel || '', r.lifecycleLabel || '',
    r.orderCount || 0, r.orderAmount || 0, r.pendingPayCount || 0, r.pendingPayAmount || 0,
    r.riskScore || 0, r.riskLabel || ''
  ])
  const csv = '\ufeff' + [header, ...lines]
    .map((arr) => arr.map((v) => `"${String(v).replace(/"/g, '""')}"`).join(','))
    .join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `会员分群_${new Date().toISOString().slice(0, 10)}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

// ==================== 标签体系 ====================

const refreshTags = async () => {
  tagLoading.value = true
  try {
    const res = await fetchMemberTagList()
    if (isSuccess(res)) tagData.value = res.data
  } catch (e) {
    ElMessage.error('标签加载失败，请刷新重试；不能将接口失败视为没有标签')
  } finally {
    tagLoading.value = false
  }
}

const handleSaveTag = async () => {
  if (!tagForm.tagName.trim()) {
    ElMessage.warning('请填写标签名称')
    return
  }
  const res = await saveMemberTag({
    tagName: tagForm.tagName.trim(),
    tagWeight: tagForm.tagWeight,
    tagDescribe: tagForm.tagDescribe
  })
  if (isSuccess(res)) {
    ElMessage.success('标签已保存')
    tagForm.tagName = ''
    tagForm.tagWeight = 0
    tagForm.tagDescribe = ''
    refreshTags()
  } else {
    ElMessage.error(res?.message || '保存失败')
  }
}

const handleDeleteTag = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.tagName}」？删除后不再出现在可选列表，历史打标关系保留。`, '提示', { type: 'warning' })
  } catch (e) {
    return
  }
  const res = await deleteMemberTag(row.tagId)
  if (isSuccess(res)) {
    ElMessage.success('已删除')
    refreshTags()
  } else {
    ElMessage.error(res?.message || '删除失败')
  }
}

const viewTagMembers = async (row) => {
  activeTab.value = 'segment'
  Object.assign(segment, emptySegment())
  segment.tagIds = [row.tagId]
  await runSegment()
}

// ==================== 批量打标 ====================

const selectedRows = ref([])
const batchTagId = ref(null)

const handleSelectionChange = (rows) => {
  selectedRows.value = rows
}

const handleBatchTag = async (attach) => {
  if (!batchTagId.value) {
    ElMessage.warning('请先选择标签')
    return
  }
  if (!selectedRows.value.length) {
    ElMessage.warning('请先勾选会员')
    return
  }
  const ids = selectedRows.value.map((r) => r.userId)
  const res = attach
    ? await attachMemberTag(batchTagId.value, ids)
    : await detachMemberTag(batchTagId.value, ids)
  if (isSuccess(res)) {
    ElMessage.success(`${attach ? '打标' : '取消打标'}完成，影响 ${res.data?.affected ?? 0} 人`)
    fetchData()
    refreshTags()
  } else {
    ElMessage.error(res?.message || '操作失败')
  }
}

const handleSearch = () => {
  pagination.page = 1
  fetchData()
}

const handleReset = () => {
  query.keyword = ''
  query.layer = ''
  query.minOrders = null
  pagination.page = 1
  fetchData()
}

const handleSizeChange = () => {
  pagination.page = 1
  fetchData()
}

const handleCurrentChange = () => fetchData()

// ==================== 画像详情 ====================

const portraitVisible = ref(false)
const portrait = ref(null)

const openPortrait = async (userId) => {
  portraitVisible.value = true
  portrait.value = null
  try {
    const res = await fetchMemberPortraitDetail(userId)
    if (!isSuccess(res)) {
      ElMessage.error(res?.message || '加载会员画像失败')
      return
    }
    portrait.value = res.data
  } catch (e) {
    ElMessage.error('加载会员画像失败：' + (e?.message || '网络异常'))
  }
}

const rfmRows = computed(() => {
  const p = portrait.value
  if (!p) return []
  return [
    { key: 'R', label: 'R 最近活跃', score: p.rScore || 0, color: '#f56c6c' },
    { key: 'F', label: 'F 消费频次', score: p.fScore || 0, color: '#e6a23c' },
    { key: 'M', label: 'M 消费金额', score: p.mScore || 0, color: '#409eff' }
  ]
})

const rfmLegend = computed(() => {
  const p = portrait.value
  if (!p) return ''
  return `R：最近活跃 ${lastActiveText(p)}；F：${p.orderCount || 0} 笔订单 + ${p.loginCount || 0} 次登录；M：累计消费 ${formatMoney(p.orderAmount)}`
})

const portraitConclusion = computed(() => {
  const p = portrait.value
  if (!p) return []
  const list = []
  list.push(
    `${p.nickname || p.userId} 当前属于「${p.layerLabel}」：有效订单 ${p.orderCount || 0} 笔、累计消费 ${formatMoney(p.orderAmount)}、累计登录 ${p.loginCount || 0} 次、最近活跃 ${lastActiveText(p)}。`
  )
  if (!p.hasOrderData) {
    list.push('该会员在 hou_order_base 中没有有效订单，无法计算真实客单价与购买偏好；不要把「0 元」解读为消费力低。')
  }
  if (p.pendingPayCount > 0) {
    list.push(`另有 ${p.pendingPayCount} 笔待付款订单（应付 ${formatMoney(p.pendingPayAmount)}），未支付不计入消费，建议优先催付。`)
  }
  if (!p.hasBrowseData) {
    list.push('商品浏览足迹表当前没有该会员记录，浏览偏好维度不可用。')
  }
  if (!p.hasManualTag) {
    list.push('该会员尚未被运营打标签，标签维度为空。')
  }
  return list
})

// ==================== 分层抽屉 ====================

const layerDrawerVisible = ref(false)
const openLayerDrawer = () => {
  layerDrawerVisible.value = true
}

const layerCards = computed(() => {
  const stats = summary.value?.layerStats || []
  return stats.map((s) => {
    const meta = layerOptions.find((o) => o.value === s.layer) || {}
    return {
      key: s.layer,
      label: meta.label || s.layer,
      color: meta.color || '#909399',
      rule: LAYER_RULES[s.layer] || '',
      count: s.count || 0,
      ratio: s.ratio || 0,
      avgAmount: s.avgAmount,
      avgOrders: s.avgOrders,
      riskCount: s.riskCount || 0,
      users: s.members || []
    }
  })
})

// ==================== 用户行为与留存分析（行为 / 留存 / 活跃 / 转化） ====================

const EVENT_LABELS = { login: '登录', view: '浏览', search: '搜索', cart: '加购', order: '下单', share: '分享' }
const EVENT_COLORS = { login: '#409eff', view: '#67c23a', search: '#e6a23c', cart: '#909399', order: '#f56c6c', share: '#9b59b6' }
const EVENT_TYPES = Object.keys(EVENT_LABELS)
const FUNNEL_COLORS = ['#409eff', '#67c23a', '#e6a23c', '#f56c6c', '#9b59b6', '#16a085']

const dateStr = (d) => `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
const defaultAnalyticsRange = () => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 29)
  return [dateStr(start), dateStr(end)]
}

const analyticsDateRange = ref(defaultAnalyticsRange())
const analyticsData = ref(null)
const analyticsLoading = ref(false)
const retentionCohort = ref('byActive')

const behaviorTrendChartRef = ref()
const activeTrendChartRef = ref()
const retentionChartRef = ref()
const funnelChartRef = ref()
let behaviorTrendChart = null
let activeTrendChart = null
let retentionChart = null
let funnelChart = null

const analyticsRangeText = computed(() => (analyticsDateRange.value || []).join(' ~ '))
const collectedTypeText = computed(() => (analyticsData.value?.dataQuality?.collectedTypes || []).map((t) => EVENT_LABELS[t] || t).join('、') || '无')
const missingTypeText = computed(() => (analyticsData.value?.dataQuality?.missingTypes || []).map((t) => EVENT_LABELS[t] || t).join('、') || '无')
const behaviorSummary = computed(() => analyticsData.value?.behavior?.summary || [])
const behaviorTrend = computed(() => analyticsData.value?.behavior?.trend || [])
const activeTrend = computed(() => analyticsData.value?.active?.trend || [])
const currentRetention = computed(() => analyticsData.value?.retention?.[retentionCohort.value] || null)
const retentionCohorts = computed(() => currentRetention.value?.cohorts || [])
const funnelLevels = computed(() => analyticsData.value?.funnel?.levels || [])
const retentionD1Text = computed(() => {
  const cell = currentRetention.value?.summary?.d1
  if (!cell || cell.eligible === 0) return '观察中'
  return cell.rate === null || cell.rate === undefined ? '-' : `${cell.rate}%`
})

const setAnalyticsRange = (days) => {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - (days - 1))
  analyticsDateRange.value = [dateStr(start), dateStr(end)]
  loadAnalytics()
}

const resetAnalyticsRange = () => {
  analyticsDateRange.value = defaultAnalyticsRange()
  loadAnalytics()
}

const loadAnalytics = async () => {
  analyticsLoading.value = true
  try {
    const [startDate, endDate] = analyticsDateRange.value || defaultAnalyticsRange()
    const res = await fetchBehaviorAnalytics({ startDate, endDate })
    if (!isSuccess(res)) {
      ElMessage.error(res?.message || '行为分析加载失败')
      return
    }
    analyticsData.value = res.data || null
    await nextTick()
    renderAnalyticsCharts()
  } catch (e) {
    ElMessage.error('行为分析加载失败：' + (e?.message || '网络异常'))
  } finally {
    analyticsLoading.value = false
  }
}

const retentionCell = (row, n) => {
  if (row[`d${n}Pending`]) return '观察中'
  const rate = row[`d${n}Rate`]
  if (rate === null || rate === undefined) return '-'
  return `${row[`d${n}Users`]} 人 / ${rate}%`
}

const renderBehaviorTrendChart = () => {
  if (!behaviorTrendChartRef.value) return
  if (!behaviorTrendChart) behaviorTrendChart = echarts.init(behaviorTrendChartRef.value)
  const rows = behaviorTrend.value
  const collected = new Set(analyticsData.value?.dataQuality?.collectedTypes || [])
  const series = EVENT_TYPES.filter((t) => collected.has(t)).map((t) => ({
    name: EVENT_LABELS[t],
    type: 'line',
    smooth: true,
    showSymbol: false,
    itemStyle: { color: EVENT_COLORS[t] },
    data: rows.map((r) => Number(r[t]) || 0)
  }))
  behaviorTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: series.map((s) => s.name), bottom: 0, left: 'center' },
    grid: { left: 48, right: 20, top: 26, bottom: 56 },
    xAxis: {
      type: 'category',
      data: rows.map((r) => r.date.slice(5)),
      axisLabel: { interval: Math.max(0, Math.floor(rows.length / 8)) }
    },
    yAxis: { type: 'value', name: '次数', minInterval: 1 },
    graphic: series.length === 0
      ? [{ type: 'text', left: 'center', top: 'middle', style: { text: '暂无已采集行为数据', fill: '#909399' } }]
      : [],
    series
  }, true)
}

const renderActiveTrendChart = () => {
  if (!activeTrendChartRef.value) return
  if (!activeTrendChart) activeTrendChart = echarts.init(activeTrendChartRef.value)
  const rows = activeTrend.value
  const line = (name, key, color) => ({
    name,
    type: 'line',
    smooth: true,
    showSymbol: false,
    itemStyle: { color },
    data: rows.map((r) => Number(r[key]) || 0)
  })
  activeTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['DAU', 'WAU', 'MAU'], bottom: 0, left: 'center' },
    grid: { left: 48, right: 20, top: 26, bottom: 56 },
    xAxis: {
      type: 'category',
      data: rows.map((r) => r.date.slice(5)),
      axisLabel: { interval: Math.max(0, Math.floor(rows.length / 8)) }
    },
    yAxis: { type: 'value', name: '去重会员', minInterval: 1 },
    series: [line('DAU', 'dau', '#409eff'), line('WAU', 'wau', '#67c23a'), line('MAU', 'mau', '#e6a23c')]
  }, true)
}

const renderRetentionChart = () => {
  if (!retentionChartRef.value) return
  if (!retentionChart) retentionChart = echarts.init(retentionChartRef.value)
  const summary = currentRetention.value?.summary || {}
  const keys = ['d1', 'd7', 'd30']
  const labels = ['次日留存', '7 日留存', '30 日留存']
  const colors = ['#409eff', '#67c23a', '#e6a23c']
  const rates = keys.map((k) => summary[k]?.rate ?? 0)
  const eligible = keys.map((k) => summary[k]?.eligible ?? 0)
  retentionChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 56, right: 20, top: 30, bottom: 38 },
    xAxis: { type: 'category', data: labels },
    yAxis: { type: 'value', name: '留存率 %', min: 0, max: 100 },
    series: [{
      type: 'bar',
      barMaxWidth: 54,
      label: {
        show: true,
        position: 'top',
        formatter: (p) => (eligible[p.dataIndex] > 0 ? `${p.value}%` : '观察中')
      },
      data: rates.map((v, i) => ({ value: v, itemStyle: { color: eligible[i] > 0 ? colors[i] : '#c0c4cc' } }))
    }]
  }, true)
}

const renderFunnelChart = () => {
  if (!funnelChartRef.value) return
  if (!funnelChart) funnelChart = echarts.init(funnelChartRef.value)
  const levels = funnelLevels.value
  funnelChart.setOption({
    tooltip: { trigger: 'item', formatter: (p) => `${p.name}：${p.data.display} 人` },
    series: [{
      type: 'funnel',
      left: '6%',
      top: 18,
      bottom: 18,
      width: '88%',
      min: 0,
      max: Math.max(1, Number(levels[0]?.count) || 1),
      minSize: '20%',
      maxSize: '100%',
      sort: 'descending',
      gap: 2,
      label: { show: true, position: 'inside', formatter: (p) => `${p.name} ${p.data.display}` },
      data: levels.map((l, i) => ({
        name: l.name,
        value: l.collected ? Number(l.count) || 0 : 0,
        display: l.collected ? l.count : '未采集',
        itemStyle: { color: FUNNEL_COLORS[i % FUNNEL_COLORS.length] }
      }))
    }]
  }, true)
}

const renderAnalyticsCharts = () => {
  renderBehaviorTrendChart()
  renderActiveTrendChart()
  renderRetentionChart()
  renderFunnelChart()
}

const exportAnalytics = () => {
  const d = analyticsData.value
  if (!d) return
  const wb = XLSX.utils.book_new()
  const sheet = (rows, name) => {
    XLSX.utils.book_append_sheet(wb, XLSX.utils.json_to_sheet(rows.length ? rows : [{ 提示: '暂无数据' }]), name)
  }
  sheet([
    { 项目: '时间范围', 值: `${d.range.start} ~ ${d.range.end}` },
    { 项目: '生成时间', 值: d.dataQuality?.generatedAt || '' },
    { 项目: '区间事件数', 值: d.dataQuality?.rangeEventRows || 0 },
    { 项目: '已采集维度', 值: collectedTypeText.value },
    { 项目: '未采集维度', 值: missingTypeText.value },
    { 项目: '数据口径', 值: d.dataQuality?.note || '' },
    { 项目: '活跃口径', 值: d.active?.rule || '' },
    { 项目: '留存口径', 值: d.retention?.rule || '' },
    { 项目: '漏斗口径', 值: d.funnel?.rule || '' }
  ], '口径')
  sheet(behaviorSummary.value.map((r) => ({
    行为类型: r.label,
    频次: r.count,
    涉及会员: r.users,
    人均频次: r.avgPerUser ?? '-',
    占比: r.share === null || r.share === undefined ? '-' : `${r.share}%`,
    采集状态: r.collected ? '已采集' : '未采集'
  })), '行为频次')
  sheet(behaviorTrend.value.map((r) => {
    const row = { 日期: r.date }
    EVENT_TYPES.forEach((t) => { row[EVENT_LABELS[t]] = r[t] })
    row.合计 = r.total
    return row
  }), '行为趋势')
  sheet(activeTrend.value.map((r) => ({ 日期: r.date, DAU: r.dau, WAU: r.wau, MAU: r.mau })), '活跃趋势')
  sheet(retentionCohorts.value.map((r) => ({
    基准日: r.date,
    基准人数: r.baseUsers,
    次日留存: retentionCell(r, 1),
    '7日留存': retentionCell(r, 7),
    '30日留存': retentionCell(r, 30)
  })), '留存明细')
  sheet(funnelLevels.value.map((l) => ({
    环节: l.name,
    人数: l.collected ? l.count : '未采集',
    上一级转化: l.stepRate === null || l.stepRate === undefined ? '-' : `${l.stepRate}%`,
    注册整体转化: l.totalRate === null || l.totalRate === undefined ? '-' : `${l.totalRate}%`
  })), '转化漏斗')
  XLSX.writeFile(wb, `用户行为与留存分析_${d.range.start}_${d.range.end}.xlsx`)
  ElMessage.success('已导出六张明细表（口径 / 行为频次 / 行为趋势 / 活跃趋势 / 留存明细 / 转化漏斗）')
}

// ==================== 图表 ====================

const trendChartRef = ref()
const layerChartRef = ref()
let trendChart = null
let layerChart = null

const renderTrendChart = () => {
  if (!trendChartRef.value) return
  if (!trendChart) trendChart = echarts.init(trendChartRef.value)
  const data = summary.value?.trend || []
  // 当日订单按三种真实状态分段堆叠：有效 / 待付款 / 已取消。
  // 三段之和＝当日下单数，既不会把已取消算成待付款，也不会让「有下单但全取消」的曲线变成 0 线。
  const paidOrders = data.map((i) => i.paid ?? i.orders ?? 0)
  // 待付款只认 order_status=1；不能再用「下单数 - 有效数」倒推，
  // 否则已取消(7)会被算成待付款，和卡片上的待付款口径自相矛盾。
  const pendingOrders = data.map((i) => Number(i.pending_count ?? i.pendingCount) || 0)
  const cancelledOrders = data.map((i) => Number(i.cancelled_count ?? i.cancelledCount) || 0)
  trendChart.setOption(
    {
      tooltip: { trigger: 'axis' },
      // 图例放到底部：顶部右侧要留给「口径说明」标签，四段图例挤在上方会互相压住
      legend: { data: ['有效订单', '待付款订单', '已取消订单', '有效支付金额'], bottom: 0, left: 'center' },
      grid: { left: 52, right: 66, top: 32, bottom: 62 },
      xAxis: {
        type: 'category',
        data: data.map((i) => i.date.slice(5)),
        axisLabel: { interval: 4 }
      },
      yAxis: [
        { type: 'value', name: '订单数', minInterval: 1 },
        {
          type: 'value',
          name: '金额',
          axisLabel: {
            formatter: (v) => (v >= 10000 ? (v / 10000).toFixed(1) + '万' : v)
          }
        }
      ],
      series: [
        {
          name: '有效订单',
          type: 'bar',
          stack: 'order',
          barMaxWidth: 16,
          itemStyle: { color: '#409eff' },
          data: paidOrders
        },
        {
          name: '待付款订单',
          type: 'bar',
          stack: 'order',
          barMaxWidth: 16,
          itemStyle: { color: '#e6a23c' },
          data: pendingOrders
        },
        {
          name: '已取消订单',
          type: 'bar',
          stack: 'order',
          barMaxWidth: 16,
          itemStyle: { color: '#c0c4cc' },
          data: cancelledOrders
        },
        {
          name: '有效支付金额',
          type: 'line',
          yAxisIndex: 1,
          smooth: true,
          itemStyle: { color: '#f56c6c' },
          data: data.map((i) => Number(i.amount) || 0)
        }
      ]
    },
    true
  )
}

const renderLayerChart = () => {
  if (!layerChartRef.value) return
  if (!layerChart) layerChart = echarts.init(layerChartRef.value)
  const cards = layerCards.value
  const total = cards.reduce((sum, c) => sum + c.count, 0)
  layerChart.setOption(
    {
      tooltip: { trigger: 'item', formatter: '{b}: {c} 人（{d}%）' },
      legend: { orient: 'vertical', right: 10, top: 'center' },
      graphic: total === 0
        ? [{ type: 'text', left: 'center', top: 'middle', style: { text: '暂无分层数据', fill: '#909399' } }]
        : [],
      series: [
        {
          type: 'pie',
          radius: ['45%', '68%'],
          center: ['40%', '50%'],
          label: { formatter: '{b}\n{c} 人' },
          data: cards.map((c) => ({
            name: c.label,
            value: c.count,
            itemStyle: { color: c.color }
          }))
        }
      ]
    },
    true
  )
}

const renderCharts = () => {
  renderTrendChart()
  renderLayerChart()
}

const handleResize = () => {
  if (trendChart) trendChart.resize()
  if (layerChart) layerChart.resize()
  if (behaviorTrendChart) behaviorTrendChart.resize()
  if (activeTrendChart) activeTrendChart.resize()
  if (retentionChart) retentionChart.resize()
  if (funnelChart) funnelChart.resize()
}

// ==================== 导出 ====================

const exportCsv = () => {
  const rows = records.value
  if (!rows.length) {
    ElMessage.warning('当前页没有可导出的数据')
    return
  }
  const header = ['会员ID', '昵称', '手机号', '累计登录', '最后登录', '有效订单', '待付款', '待付款金额', '累计消费', '最近下单', '分层', '风险分', '风险等级', '手工标签']
  const lines = rows.map((r) => [
    r.userId,
    r.nickname || '',
    maskMobile(r.mobile),
    r.loginCount ?? 0,
    r.lastLoginTime || '',
    r.orderCount ?? 0,
    r.pendingPayCount ?? 0,
    Number(r.pendingPayAmount || 0).toFixed(2),
    Number(r.orderAmount || 0).toFixed(2),
    r.lastOrderTime || '',
    r.layerLabel || '',
    r.riskScore ?? '',
    r.riskLabel || '',
    (r.manualTags || []).join('|')
  ])
  const csv = '\ufeff' + [header, ...lines]
    .map((cols) => cols.map((v) => `"${String(v ?? '').replace(/"/g, '""')}"`).join(','))
    .join('\r\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `会员画像_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success(`已导出当前页 ${rows.length} 条（共 ${pagination.total} 条）`)
}

// ==================== 格式化 ====================

const maskMobile = (m) => {
  if (!m) return '-'
  const s = String(m)
  return s.length === 11 ? s.slice(0, 3) + '****' + s.slice(7) : s
}

const formatMoney = (v) => {
  if (v === null || v === undefined || v === '') return '¥0.00'
  const n = Number(v)
  if (Number.isNaN(n)) return '¥0.00'
  return '¥' + n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const layerTagType = (layer) => {
  if (layer === 'core') return 'danger'
  if (layer === 'growth') return 'warning'
  return 'info'
}

const riskTagType = (score) => {
  const n = Number(score) || 0
  if (n >= 60) return 'danger'
  if (n >= 30) return 'warning'
  return 'success'
}

const riskColor = (score) => {
  const n = Number(score) || 0
  if (n >= 60) return '#f56c6c'
  if (n >= 30) return '#e6a23c'
  return '#67c23a'
}

const lastActiveText = (row) => {
  const d = row?.lastActiveDays
  if (d === null || d === undefined) return '无活跃记录'
  if (d === 0) return '今天'
  if (d === 1) return '昨天'
  return `${d} 天前`
}

// ==================== 生命周期 ====================

onMounted(() => {
  fetchData()
  // 标签供「批量打标」下拉使用，首次进入即静默加载；RFM 矩阵延后到切页时再取
  refreshTags()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChart) {
    trendChart.dispose()
    trendChart = null
  }
  if (layerChart) {
    layerChart.dispose()
    layerChart = null
  }
  if (behaviorTrendChart) { behaviorTrendChart.dispose(); behaviorTrendChart = null }
  if (activeTrendChart) { activeTrendChart.dispose(); activeTrendChart = null }
  if (retentionChart) { retentionChart.dispose(); retentionChart = null }
  if (funnelChart) { funnelChart.dispose(); funnelChart = null }
})
</script>

<style scoped>
.user-behavior-container {
  padding: 20px;
}

.stats-cards {
  margin-bottom: 20px;
}

.chart-row {
  margin-bottom: 20px;
}

.stat-card, .chart-card {
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  font-size: 40px;
  margin-right: 15px;
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
}

.total-visits .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.today-visits .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
  color: white;
}

.active-users .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  color: white;
}

.avg-duration .stat-icon {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #b1b3b8;
  line-height: 1.3;
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.chart-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.chart-container {
  height: 300px;
  width: 100%;
  background: transparent;
}

.search-card, .action-card, .table-card {
  margin-bottom: 20px;
}

.search-form .el-form-item {
  margin-bottom: 0;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.table-title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.pagination {
  margin-top: 20px;
  text-align: right;
}

.behavior-detail {
  margin-bottom: 20px;
}

.extended-info {
  margin-top: 30px;
}

.extended-info h4 {
  margin-bottom: 15px;
  color: #303133;
}

.user-portrait {
  min-height: 400px;
}

.portrait-form {
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
}

.portrait-content {
  margin-top: 20px;
}

.portrait-card {
  margin-bottom: 20px;
}

.portrait-card h4 {
  margin-bottom: 15px;
  color: #303133;
  font-size: 16px;
}

.portrait-card p {
  margin: 8px 0;
  color: #606266;
  font-size: 14px;
}

/* ===== 用户画像 ===== */
.portrait-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.portrait-hint {
  font-size: 12px;
  color: #909399;
}

.portrait-hero {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
  padding: 14px 18px;
  margin-bottom: 16px;
  border-radius: 8px;
  background: #f5f7fa;
}

.hero-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hero-avatar {
  background: #409eff;
  color: #fff;
  font-size: 20px;
}

.hero-name {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.hero-sub {
  margin-top: 4px;
  font-size: 12px;
  color: #909399;
}

.hero-metrics {
  display: flex;
  gap: 26px;
}

.hero-metric {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.hm-value {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.hm-label {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}

.portrait-block {
  margin-bottom: 16px;
}

.rfm-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}

.rfm-label {
  width: 82px;
  font-size: 13px;
  color: #606266;
}

.rfm-bar {
  flex: 1;
}

.rfm-score {
  width: 32px;
  text-align: right;
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.rfm-legend {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.risk-head {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  margin-bottom: 10px;
}

.risk-score {
  font-size: 34px;
  font-weight: 700;
  line-height: 1;
}

.risk-head-right {
  flex: 1;
}

.risk-reason {
  margin-top: 6px;
  font-size: 12px;
  color: #909399;
}

.advice-list {
  margin: 0;
  padding-left: 18px;
}

.advice-list li {
  margin-bottom: 6px;
  font-size: 13px;
  line-height: 1.6;
  color: #606266;
}

.tag-row {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.interest-tag {
  margin: 0;
}

.mt {
  margin-top: 16px;
}

.layer-summary {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}

.layer-summary-item {
  flex: 1;
  padding: 12px;
  text-align: center;
  border: 1px solid #ebeef5;
  border-left-width: 4px;
  border-radius: 6px;
  background: #fafafa;
}

.ls-value {
  font-size: 24px;
  font-weight: 700;
}

.ls-label {
  margin-top: 4px;
  font-size: 13px;
  color: #303133;
}

.ls-sub {
  margin-top: 2px;
  font-size: 12px;
  color: #909399;
}

.layer-card {
  margin-bottom: 14px;
}

.layer-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.layer-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.layer-meta {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

.layer-slogan {
  margin-top: 6px;
  font-size: 13px;
  color: #606266;
}

.layer-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin: 10px 0;
}

.layer-tag {
  margin: 0;
}

.layer-users {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.layer-users-label {
  font-size: 12px;
  color: #909399;
}

.layer-empty {
  font-size: 12px;
  color: #c0c4cc;
}

.persona-detail h4 {
  margin: 18px 0 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.pd-slogan {
  margin-bottom: 12px;
  font-size: 15px;
  font-weight: 600;
  color: #409eff;
}

.pd-background {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #606266;
}
</style>

<style scoped>
.dq-alert {
  margin-bottom: 16px;
}
.dq-line {
  line-height: 1.9;
  font-size: 13px;
}
.dq-sub {
  color: #909399;
  font-size: 12px;
}
.cell-main {
  font-size: 13px;
  color: #303133;
}
.cell-sub {
  font-size: 12px;
  color: #909399;
}
.cell-muted {
  color: #c0c4cc;
}
.mr4 {
  margin-right: 4px;
}
.table-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

/* ==================== Tab 与新增区块 ==================== */
.portrait-tabs {
  margin-top: 4px;
}

.rfm-grid {
  margin-bottom: 16px;
}

.rfm-card {
  cursor: pointer;
  margin-bottom: 16px;
  min-height: 132px;
  transition: all 0.2s;
}

.rfm-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
}

.rfm-card-active {
  border: 1px solid #409eff;
  box-shadow: 0 0 0 1px #409eff inset;
}

.rfm-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.rfm-card-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.rfm-card-meta {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
}

.rfm-card-advice {
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.range-row {
  display: flex;
  align-items: center;
  gap: 6px;
  width: 100%;
}

.range-row .el-input-number {
  flex: 1;
}

.range-sep {
  color: #c0c4cc;
}

.form-hint {
  margin-left: 6px;
  font-size: 12px;
  color: #909399;
}

.segment-rule {
  margin-top: 8px;
  padding: 8px 10px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #606266;
  line-height: 1.5;
}

.segment-save {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  align-items: center;
}

.hit-num {
  color: #409eff;
  font-size: 16px;
}

.empty-hint {
  margin-top: 10px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 4px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}

.batch-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  margin-left: 8px;
}

.batch-tag-label {
  font-size: 12px;
  color: #606266;
}

/* 单客 360 新增块 */
.rfm-segment-line {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 10px;
  padding-top: 10px;
  border-top: 1px dashed #ebeef5;
}

.rfm-seg-label {
  font-size: 13px;
  color: #606266;
}

.rfm-seg-score {
  font-size: 12px;
  color: #909399;
}

.pref-block {
  margin-bottom: 14px;
}

.pref-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.pref-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.pref-item {
  display: flex;
  justify-content: space-between;
  gap: 10px;
  font-size: 12px;
  color: #606266;
}

.pref-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pref-val {
  color: #909399;
  white-space: nowrap;
}

.pref-note {
  margin-top: 6px;
  font-size: 12px;
  color: #b1b3b8;
}

.timeline-box {
  max-height: 420px;
  overflow-y: auto;
  padding-right: 6px;
}

.tl-title {
  font-size: 13px;
  color: #303133;
  margin-right: 8px;
}

.tl-detail {
  font-size: 12px;
  color: #909399;
}

.analytics-filter {
  margin-bottom: 16px;
}

.analytics-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.analytics-form :deep(.el-form-item) {
  margin-bottom: 8px;
  margin-right: 12px;
}

.analytics-table-card {
  margin-bottom: 16px;
}

.analytics-hint {
  font-size: 12px;
  color: #909399;
  max-width: 62%;
  line-height: 1.4;
  text-align: right;
}

.analytics-foot {
  margin-top: 12px;
  font-size: 13px;
  color: #606266;
}

.analytics-foot b {
  color: #303133;
}
</style>
