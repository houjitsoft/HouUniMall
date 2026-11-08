const a = {
  LIVE: "live",
  STUB: "stub"
}, t = [
  {
    code: "WECHAT_V3",
    name: "微信支付 V3",
    desc: "官方 wechatpay-java，APIv3；Strategy=WechatV3PayStrategy",
    provider: "WECHAT",
    maturity: "live",
    color: "#07C160",
    fields: [
      { key: "appId", label: "AppID", required: !0 },
      { key: "mchId", label: "商户号 mchId", required: !0 },
      { key: "apiV3Key", label: "APIv3 Key", sensitive: !0, required: !0 },
      { key: "certSerialNo", label: "商户证书序列号", required: !0 },
      {
        key: "privateKey",
        label: "商户私钥（PEM）",
        sensitive: !0,
        required: !0,
        type: "textarea",
        placeholder: "-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----"
      },
      {
        key: "notifyUrl",
        label: "支付回调 notifyUrl",
        required: !1,
        placeholder: "https://你的域名/web/pay/notify/wechat"
      }
    ]
  },
  {
    code: "ALIPAY",
    name: "支付宝",
    desc: "支付宝开放平台 RSA2；Strategy=AlipayPayStrategy（PC/WAP/APP）",
    provider: "ALIPAY",
    maturity: "live",
    color: "#1677FF",
    fields: [
      {
        key: "gatewayUrl",
        label: "网关地址",
        required: !1,
        placeholder: "https://openapi.alipay.com/gateway.do"
      },
      { key: "appId", label: "AppID", required: !0 },
      {
        key: "privateKey",
        label: "应用私钥（PEM）",
        sensitive: !0,
        required: !0,
        type: "textarea",
        placeholder: "-----BEGIN PRIVATE KEY-----\\n...\\n-----END PRIVATE KEY-----"
      },
      { key: "alipayPublicKey", label: "支付宝公钥", sensitive: !0, required: !0, type: "textarea" },
      {
        key: "notifyUrl",
        label: "支付回调 notifyUrl",
        required: !1,
        placeholder: "https://你的域名/web/pay/notify/alipay"
      }
    ]
  },
  {
    code: "STRIPE",
    name: "Stripe",
    desc: "国际卡；Strategy=StripePayStrategy（Checkout / PaymentIntent）",
    provider: "STRIPE",
    maturity: "live",
    color: "#635BFF",
    fields: [
      {
        key: "publishableKey",
        label: "Publishable Key",
        required: !0,
        placeholder: "pk_live_... 或 pk_test_..."
      },
      {
        key: "secretKey",
        label: "Secret Key",
        sensitive: !0,
        required: !0,
        placeholder: "sk_live_... 或 sk_test_..."
      },
      {
        key: "webhookSecret",
        label: "Webhook Secret",
        sensitive: !0,
        required: !1,
        placeholder: "whsec_..."
      },
      { key: "merchantName", label: "商户显示名", required: !1 }
    ]
  },
  {
    code: "YSF_PAY",
    name: "云闪付",
    desc: "YsfPayStrategy 仍返回 mock URL，未接银联 acpsdk",
    provider: "UNIONPAY",
    maturity: "stub",
    color: "#E60012",
    stubReason: "createPay 走 MOCK；填凭证也不会真扣款",
    fields: [
      { key: "merchId", label: "商户号 merchId", required: !0 },
      { key: "signKey", label: "签名密钥 signKey", sensitive: !0, required: !0 }
    ]
  },
  {
    code: "FME_PAY",
    name: "FME 职工金融",
    desc: "FmePayStrategy 骨架，合作方 API 未接",
    provider: "FME",
    maturity: "stub",
    color: "#FA8C16",
    stubReason: "TODO 真实 HttpClient；当前 mock 网关",
    fields: [
      { key: "partnerId", label: "合作方 partnerId", required: !0 },
      { key: "appKey", label: "AppKey", required: !0 },
      { key: "appSecret", label: "AppSecret", sensitive: !0, required: !0 }
    ]
  },
  {
    code: "ALI_CREDIT",
    name: "支付宝花呗",
    desc: "AliCreditPayStrategy 仍 TODO creditPay，非生产",
    provider: "ALIPAY",
    maturity: "stub",
    color: "#1677FF",
    stubReason: "未接花呗 productCode；勿当正式渠道",
    fields: [
      { key: "appId", label: "AppID", required: !0 },
      { key: "privateKey", label: "应用私钥（PEM）", sensitive: !0, required: !0, type: "textarea" },
      { key: "alipayPublicKey", label: "支付宝公钥", sensitive: !0, required: !0, type: "textarea" }
    ]
  },
  {
    code: "SHARE_PAY",
    name: "分账支付（骨架）",
    desc: "SharePayStrategy 本地拆分 mock；真分账在分账中心配置接收方",
    provider: "SHARE",
    maturity: "stub",
    color: "#722ED1",
    stubReason: "未联动微信/支付宝 profitsharing；仅骨架",
    fields: [
      {
        key: "remark",
        label: "备注（可选）",
        required: !1,
        placeholder: "分账接收方请到「分账中心」配置"
      }
    ]
  }
];
t.filter((r) => r.maturity === "live");
t.filter((r) => r.maturity === "stub");
function l(r) {
  return t.find((e) => e.code === r) || null;
}
function i({ includeStub: r = !0 } = {}) {
  return t.filter((e) => r || e.maturity === "live").map((e) => ({
    label: e.maturity === "stub" ? `${e.name}（骨架/Mock）` : e.name,
    value: e.code,
    maturity: e.maturity,
    disabled: !1
  }));
}
export {
  a as C,
  t as a,
  i as c,
  l as f
};
