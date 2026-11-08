/**
 * W4 自研支付平台扩展 API（对账/会员Biz/分账Biz/风控审核/商户补全）
 * 对应后端 BFF 路径：
 *   /web/pay/recon/**           对账清算（T3）
 *   /web/pay/merchant/**        商户补全（T4）
 *   /web/pay/member/biz/**      自研会员（T5）
 *   /web/pay/division/biz/**    自研分账（T6）
 *   /web/pay/risk/log/**        风控人工审核（T7）
 *
 * @author Amos
 * @since 2026-05
 */
import request from '@/utils/request'

/** ============== W4-T3 对账清算 ============== */
export const reconApi = {
  /** 渠道对账单拉取（T+1 批量） */
  pullChannelBill: (params) => request.post('/web/pay/recon/pullChannelBill', params),
  /** 对账差异分页 */
  diffList: (params) => request.post('/web/pay/recon/diffList', params || {}),
  /** 对账差异处理（补单/冲正/人工关闭） */
  diffHandle: (params) => request.post('/web/pay/recon/diffHandle', params),
  /** 对账报告生成 */
  reportGen: (params) => request.post('/web/pay/recon/reportGen', params),
  /** 对账核对（比对本地订单与渠道账单） */
  diffCheck: (params) => request.post('/web/pay/recon/diffCheck', params || {}),
  /** 对账报表分页 */
  reportList: (params) => request.post('/web/pay/recon/reportList', params || {}),
  /** 手动触发对账（拉取+核对） */
  trigger: (params) => request.post('/web/pay/recon/trigger', params || {})
}

/**
 * 对账中心渠道静态清单（与 ReconServiceImpl.ALL_CHANNELS 对齐）
 */
export const reconChannels = [
  { code: 'WX', name: '微信支付', channelId: 1 },
  { code: 'ALI', name: '支付宝', channelId: 2 },
  { code: 'UNION', name: '银联', channelId: 3 },
  { code: 'YSF', name: '云闪付', channelId: 4 }
]


/** ============== 平台商户入网（自研，可用；通企付 member/mch 需密钥另走 stub） ============== */
export const merchantPlatformApi = {
  /** 商户进件：mchType 1企业/2个体/3个人 */
  reg: (params) => request.post('/web/pay/merchant/reg', params),
  /** 商户列表分页 */
  list: (params) => request.post('/web/pay/merchant/list', params || {}),
  /** 商户详情 */
  detail: (params) => request.post('/web/pay/merchant/detail', params),
  /** 状态变更：0待审核 1审核中 2已生效 3已冻结 4已注销 5审核失败 */
  changeStatus: (params) => request.post('/web/pay/merchant/changeStatus', params)
}

/** ============== W4-T4 商户补全 ============== */
export const merchantBizApi = {
  /** 个人/个体工商户进件 */
  regPerson: (params) => request.post('/web/pay/merchant/regPerson', params),
  /** 商户资质补充 */
  qualSupplement: (params) => request.post('/web/pay/merchant/qualSupplement', params),
  /** 商户子账号列表 */
  subAccountList: (params) => request.post('/web/pay/merchant/subAccount/list', params || {})
}

/** ============== W4-T5 自研会员 ============== */
export const memberBizApi = {
  /** 会员信息聚合查询 */
  infoQuery: (params) => request.post('/web/pay/member/biz/info/query', params),
  /** 会员企业信息修改 */
  enterpriseModify: (params) => request.post('/web/pay/member/biz/enterprise/modify', params),
  /** 会员注册状态 */
  regStatus: (params) => request.post('/web/pay/member/biz/regStatus', params),
  /** 会员产品管理（open/close/list） */
  productManage: (params) => request.post('/web/pay/member/biz/product/manage', params)
}

/** ============== W4-T6 自研分账 ============== */
export const divisionBizApi = {
  /** 分账接收方绑定（万分比 shareRatio） */
  receiverBind: (params) => request.post('/web/pay/division/biz/receiver/bind', params),
  /** 分账接收方列表 */
  receiverList: (params) => request.post('/web/pay/division/biz/receiver/list', params || {}),
  /** 分账接收方解绑 */
  receiverUnbind: (params) => request.post('/web/pay/division/biz/receiver/unbind', params),
  /** 分账规则引擎计算 */
  ruleEngineCalc: (params) => request.post('/web/pay/division/biz/ruleEngine/calc', params)
}

/**
 * 分账中心辅助 API（平台 / 渠道 / 商户，供分步向导使用）
 * - platforms      通企付分账平台清单（chanNo 即 BatchDivisionRequest.chanNo）
 * - merchantList   自研商户列表（供选商户）
 * - channelPublic  启用中的支付渠道（供"整合对应支付"选择）
 */
export const splitCenterApi = {
  /** 通企付分账平台清单（对应 chanNo，DTO 注释固定三种） */
  platforms: () => [
    {
      chanNo: 'allinpay',
      name: '收银宝',
      provider: '通联支付',
      desc: '支持单笔分账、实时结果，分账方入金为收银宝商户号',
      multiReceivers: false,
      supportsNotify: true,
      supportsCoupon: false,
      template: false
    },
    {
      chanNo: 'yunst2isv',
      name: '特色结算',
      provider: '云商通',
      desc: '支持批量分账（最多50笔）、平台抽佣与分账模板，异步逐笔通知',
      multiReceivers: true,
      supportsNotify: true,
      supportsCoupon: true,
      template: true
    },
    {
      chanNo: 'yunst2yhzh',
      name: '银行结算账户',
      provider: '云商通',
      desc: '按银行账户（出金方商户会员）结算，需上送解冻订单金额',
      multiReceivers: true,
      supportsNotify: true,
      supportsCoupon: false,
      template: true
    }
  ],
  /** 平台商户列表 */
  merchantList: (params) => request.post('/web/pay/merchant/list', params || {}),
  /** 启用中的支付渠道（公开字段，脱敏） */
  channelPublic: (params) => request.post('/web/pay/config/public', params || {})
}

/** ============== W4-T7 风控人工审核 ============== */
export const riskLogApi = {
  /** 提交人工审核（状态 0 待审核） */
  submitReview: (params) => request.post('/web/pay/risk/log/submitReview', params),
  /** 人工审核通过/驳回（status=1/2） */
  approve: (params) => request.post('/web/pay/risk/log/approve', params)
}

/** ============== W4-SDK 渠道凭证管理（DB+AES） ============== */
export const channelConfigApi = {
  /** 渠道凭证分页（脱敏） */
  list: (params) => request.post('/web/pay/channel/config/list', params || {}),
  /** 凭证明文回显（编辑用） */
  get: (id) => request.post('/web/pay/channel/config/get', { id }),
  /** 保存/更新凭证（AES 字段级加密落库） */
  save: (params) => request.post('/web/pay/channel/config/save', params),
  /** 启停渠道 */
  toggle: (params) => request.post('/web/pay/channel/config/toggle', params),
  /** 测试连通：校验凭证完整性，缺失字段返回 CHANNEL_NOT_CONFIGURED */
  testConnect: (id) => request.post('/web/pay/channel/config/testConnect', { id })
}
