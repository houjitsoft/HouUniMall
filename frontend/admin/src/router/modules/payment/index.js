// 支付中心是 payment 独立服务的后台页面，由宿主静态注册。
// 页面路径统一为 /payment/**，不再走插件加载器。
// 子路由用绝对 path（与 goods/ai/dcenter 一致），避免「父级 redirect + 相对 children」在部分导航时机落到 NotFound。
export default {
  path: '/payment',
  meta: { title: '支付中心', icon: 'CreditCard' },
  children: [
    {
      path: '',
      redirect: '/payment/dashboard'
    },
    // 工作台（对应数据库菜单 id=248）
    {
      path: '/payment/dashboard',
      name: 'PaymentDashboard',
      component: () => import('@/views/payment/PaymentDashboard.vue'),
      meta: { title: '支付中心工作台' }
    },
    // 支付管理综合面板（对应数据库菜单 id=282）
    {
      path: '/payment/manage',
      name: 'PaymentManage',
      component: () => import('@/views/payment/PaymentManage.vue'),
      meta: { title: '支付管理' }
    },
    // 支付统计（真实统计报表）
    {
      path: '/payment/statistics',
      name: 'PaymentStatistics',
      component: () => import('@/views/payment/PaymentStatistics.vue'),
      meta: { title: '支付统计' }
    },
    // 交易记录（对应数据库菜单 id=283）
    {
      path: '/payment/transaction',
      name: 'PaymentTransaction',
      component: () => import('@/views/payment/PaymentTransaction.vue'),
      meta: { title: '交易记录' }
    },
    // 支付订单列表
    {
      path: '/payment/orders',
      name: 'PaymentOrders',
      component: () => import('@/views/payment/PayList.vue'),
      meta: { title: '支付订单' }
    },
    // 退款管理（对应数据库菜单 id=284，优先使用增强版 PaymentRefund）
    {
      path: '/payment/refund',
      name: 'PaymentRefund',
      component: () => import('@/views/payment/PaymentRefund.vue'),
      meta: { title: '退款管理' }
    },
    // 对账管理（对应数据库菜单 id=285）
    {
      path: '/payment/reconciliation',
      name: 'PaymentReconciliation',
      component: () => import('@/views/payment/PaymentReconciliation.vue'),
      meta: { title: '对账管理' }
    },
    // 风控管理（对应数据库菜单 id=286）
    {
      path: '/payment/risk',
      name: 'PaymentRisk',
      component: () => import('@/views/payment/PaymentRisk.vue'),
      meta: { title: '风控管理' }
    },
    // 账户管理
    {
      path: '/payment/account',
      name: 'PaymentAccount',
      component: () => import('@/views/payment/AccountManage.vue'),
      meta: { title: '账户管理' }
    },
    // 充值管理
    {
      path: '/payment/recharge',
      name: 'PaymentRecharge',
      component: () => import('@/views/payment/RechargeManage.vue'),
      meta: { title: '充值管理' }
    },
    // 提现管理
    {
      path: '/payment/withdraw',
      name: 'PaymentWithdraw',
      component: () => import('@/views/payment/WithdrawManage.vue'),
      meta: { title: '提现管理' }
    },
    // 支付配置
    {
      path: '/payment/config',
      name: 'PaymentConfig',
      component: () => import('@/views/payment/PaymentConfig.vue'),
      meta: { title: '支付配置' }
    },
    // 商户资质管理（16 终端 × 多渠道）
    {
      path: '/payment/merchant-qual',
      name: 'PaymentMerchantQual',
      component: () => import('@/views/payment/MerchantQualManage.vue'),
      meta: { title: '商户资质管理' }
    },

    // ====== 三级菜单：交易管理 ======
    {
      path: '/payment/trade/unified-order',
      name: 'PaymentTradeUnifiedOrder',
      component: () => import('@/views/payment/trade/UnifiedOrder.vue'),
      meta: { title: '统一下单' }
    },
    {
      path: '/payment/trade/close-order',
      name: 'PaymentTradeCloseOrder',
      component: () => import('@/views/payment/trade/CloseOrder.vue'),
      meta: { title: '关闭订单' }
    },

    // ====== 三级菜单：退款管理 ======
    {
      path: '/payment/refund/apply',
      name: 'PaymentRefundApply',
      component: () => import('@/views/payment/refund/Apply.vue'),
      meta: { title: '退款申请' }
    },
    {
      path: '/payment/refund/log',
      name: 'PaymentRefundLog',
      component: () => import('@/views/payment/refund/Log.vue'),
      meta: { title: '退款记录' }
    },

    // ====== 三级菜单：会员管理 ======
    {
      path: '/payment/member/enterprise',
      name: 'PaymentMemberEnterprise',
      component: () => import('@/views/payment/member/EnterpriseOpen.vue'),
      meta: { title: '企业会员开户' }
    },
    {
      path: '/payment/member/personal',
      name: 'PaymentMemberPersonal',
      component: () => import('@/views/payment/member/PersonalOpen.vue'),
      meta: { title: '个人会员开户' }
    },
    {
      path: '/payment/member/query',
      name: 'PaymentMemberQuery',
      component: () => import('@/views/payment/member/Query.vue'),
      meta: { title: '会员信息查询' }
    },
    {
      path: '/payment/member/supplement',
      name: 'PaymentMemberSupplement',
      component: () => import('@/views/payment/member/Supplement.vue'),
      meta: { title: '会员资料补录' }
    },

    // ====== 三级菜单：提现管理 ======
    {
      path: '/payment/withdraw/log',
      name: 'PaymentWithdrawLog',
      component: () => import('@/views/payment/withdraw/Log.vue'),
      meta: { title: '提现记录' }
    },

    // ====== 三级菜单：分账管理 ======
    // 分账中心（按真实业务流程的分步向导：选平台→设角色→整合支付→发起→查询/撤销）
    {
      path: '/payment/split/center',
      name: 'PaymentSplitCenter',
      component: () => import('@/views/payment/split/SplitCenter.vue'),
      meta: { title: '分账中心' }
    },
    {
      path: '/payment/split/batch',
      name: 'PaymentSplitBatch',
      component: () => import('@/views/payment/split/Batch.vue'),
      meta: { title: '批量分账' }
    },
    {
      path: '/payment/split/query',
      name: 'PaymentSplitQuery',
      component: () => import('@/views/payment/split/Query.vue'),
      meta: { title: '分账查询' }
    },
    {
      path: '/payment/split/cancel',
      name: 'PaymentSplitCancel',
      component: () => import('@/views/payment/split/Cancel.vue'),
      meta: { title: '分账撤销' }
    },

    // ====== 三级菜单：商户管理 ======
    {
      path: '/payment/merchant/apply',
      name: 'PaymentMerchantApply',
      component: () => import('@/views/payment/merchant/Apply.vue'),
      meta: { title: '商户进件' }
    },
    {
      path: '/payment/merchant/list',
      name: 'PaymentMerchantList',
      component: () => import('@/views/payment/merchant/List.vue'),
      meta: { title: '商户列表' }
    },

    // ====== 三级菜单：对账管理 ======
    // 对账中心（按真实业务流程的分步向导：选渠道日期→拉取账单→触发核对→差异处理→对账报告）
    {
      path: '/payment/reconciliation/center',
      name: 'PaymentReconciliationCenter',
      component: () => import('@/views/payment/reconciliation/ReconCenter.vue'),
      meta: { title: '对账中心' }
    },
    {
      path: '/payment/reconciliation/download',
      name: 'PaymentReconciliationDownload',
      component: () => import('@/views/payment/reconciliation/DownloadFile.vue'),
      meta: { title: '对账文件下载' }
    },
    {
      path: '/payment/reconciliation/import',
      name: 'PaymentReconciliationImport',
      component: () => import('@/views/payment/reconciliation/Import.vue'),
      meta: { title: '账单导入' }
    },

    // ====== 三级菜单：账户管理 ======
    {
      path: '/payment/account/detail',
      name: 'PaymentAccountDetail',
      component: () => import('@/views/payment/account/Detail.vue'),
      meta: { title: '账户明细' }
    },

    // ====== W4 骨架页（T10 新增 12 页）======

    // --- 商户 4 页 ---
    {
      path: '/payment/merchant/detail',
      name: 'PaymentMerchantDetail',
      component: () => import('@/views/payment/merchant/Detail.vue'),
      meta: { title: '商户详情' }
    },
    {
      path: '/payment/merchant/product-config',
      name: 'PaymentMerchantProductConfig',
      component: () => import('@/views/payment/merchant/ProductConfig.vue'),
      meta: { title: '商户产品配置' }
    },
    {
      path: '/payment/merchant/fee-config',
      name: 'PaymentMerchantFeeConfig',
      component: () => import('@/views/payment/merchant/FeeConfig.vue'),
      meta: { title: '商户费率配置' }
    },
    {
      path: '/payment/merchant/status-manage',
      name: 'PaymentMerchantStatusManage',
      component: () => import('@/views/payment/merchant/StatusManage.vue'),
      meta: { title: '商户状态管理' }
    },

    // --- 会员 2 页 ---
    {
      path: '/payment/member/modify-enterprise',
      name: 'PaymentMemberModifyEnterprise',
      component: () => import('@/views/payment/member/ModifyEnterprise.vue'),
      meta: { title: '会员企业信息修改' }
    },
    {
      path: '/payment/member/reg-status',
      name: 'PaymentMemberRegStatus',
      component: () => import('@/views/payment/member/RegStatus.vue'),
      meta: { title: '会员注册状态' }
    },

    // --- 对账 2 页 ---
    {
      path: '/payment/reconciliation/diff-handle',
      name: 'PaymentReconciliationDiffHandle',
      component: () => import('@/views/payment/reconciliation/DiffHandle.vue'),
      meta: { title: '对账差异处理' }
    },
    {
      path: '/payment/reconciliation/report',
      name: 'PaymentReconciliationReport',
      component: () => import('@/views/payment/reconciliation/Report.vue'),
      meta: { title: '对账报告' }
    },

    // --- 风控 3 页 ---
    // 风控中心（按真实业务流程的分步向导：规则→评估→日志→审核→黑白名单）
    {
      path: '/payment/risk/center',
      name: 'PaymentRiskCenter',
      component: () => import('@/views/payment/risk/RiskCenter.vue'),
      meta: { title: '风控中心' }
    },
    {
      path: '/payment/risk/rule-config',
      name: 'PaymentRiskRuleConfig',
      component: () => import('@/views/payment/risk/RuleConfig.vue'),
      meta: { title: '风控规则配置' }
    },
    {
      path: '/payment/risk/log',
      name: 'PaymentRiskLog',
      component: () => import('@/views/payment/risk/RiskLog.vue'),
      meta: { title: '风控日志' }
    },
    {
      path: '/payment/risk/black-list',
      name: 'PaymentRiskBlackList',
      component: () => import('@/views/payment/risk/BlackList.vue'),
      meta: { title: '风控黑名单' }
    },

    // --- 渠道 2 页 ---
    {
      path: '/payment/channel/route-config',
      name: 'PaymentChannelRouteConfig',
      component: () => import('@/views/payment/channel/RouteConfig.vue'),
      meta: { title: '渠道路由配置' }
    },
    {
      path: '/payment/channel/credential',
      name: 'PaymentChannelCredential',
      component: () => import('@/views/payment/channel/ChannelCredential.vue'),
      meta: { title: '渠道凭证管理' }
    }
  ]
}
