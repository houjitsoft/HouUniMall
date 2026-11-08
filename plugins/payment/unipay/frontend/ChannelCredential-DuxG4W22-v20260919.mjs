import { _ as se, r as N, c as de, L as y, a as Y, o as re, b as u, d as V, e as m, F as S, f as I, g as f, h as t, w as n, i as c, A as ie, j as M, k as h, l as ue, t as g, E as v, v as B } from "./_plugin-vue_export-helper-Bh3pUN_y-v20260919.mjs";
import { W as P, B as ce, C as me, M as z, t as fe } from "./icons-vue-JpWVxlSk-v20260919.mjs";
import { c as T } from "./paymentBizW4-_PSB1goI-v20260919.mjs";
import { a as pe, C as _e } from "./channelCatalog-BlFpTUS6-v20260919.mjs";
const ye = { class: "channel-credential" }, ge = { style: { "margin-bottom": "12px", display: "flex", "justify-content": "flex-end" } }, ve = { class: "card-head" }, Ce = { class: "ch-name" }, be = { class: "card-desc" }, ke = { class: "card-fields" }, we = { class: "field-label" }, Ne = { class: "field-value" }, Ve = { class: "card-actions" }, he = {
  __name: "ChannelCredential",
  setup(Ee) {
    const L = N(!0), U = de(() => {
      const a = {
        WECHAT_V3: y(fe),
        ALIPAY: y(z),
        STRIPE: y(P),
        ALI_CREDIT: y(z),
        YSF_PAY: y(me),
        FME_PAY: y(ce),
        SHARE_PAY: y(P)
      };
      return pe.filter((e) => L.value || e.maturity === _e.LIVE).map((e) => ({
        ...e,
        icon: a[e.code] || y(P)
      }));
    }), p = Y({ merchantNo: "", channelCode: "" }), k = N({}), E = N(!1), w = N(null), $ = N(!1), A = N(null), o = Y({
      id: null,
      merchantNo: "",
      channelCode: "",
      status: 1,
      remark: "",
      config: {}
    }), O = {
      merchantNo: []
    };
    async function x() {
      try {
        const a = await T.list({
          pageNo: 1,
          pageSize: 50,
          merchantNo: p.merchantNo || void 0,
          channelCode: p.channelCode || void 0
        }), e = a?.data?.records || a?.data?.list || a?.data || [], s = {};
        e.forEach((d) => {
          const r = d.channelCode;
          (!s[r] || (d.id || 0) > (s[r].id || 0)) && (s[r] = d);
        }), k.value = s;
      } catch (a) {
        v.error("加载渠道凭证失败：" + (a?.message || a));
      }
    }
    function W() {
      p.merchantNo = "", p.channelCode = "", x();
    }
    function C(a) {
      const e = k.value[a];
      if (!e) return { configured: !1, enabled: !1, text: "未配置", tagType: "info" };
      const s = Number(e.status) === 1;
      return {
        configured: !0,
        enabled: s,
        text: s ? "已启用" : "已配置-停用",
        tagType: s ? "success" : "warning"
      };
    }
    function K(a, e) {
      const s = k.value[a];
      if (!s) return "—";
      let d = s.configJson;
      if (typeof d == "string")
        try {
          d = JSON.parse(d);
        } catch {
          d = {};
        }
      const r = d?.[e];
      if (r == null || r === "") return "—";
      const i = String(r);
      return i.length <= 8 ? i : i.slice(0, 4) + "****" + i.slice(-4);
    }
    async function j(a) {
      w.value = a;
      const e = k.value[a.code];
      if (o.id = null, o.merchantNo = p.merchantNo || "", o.channelCode = a.code, o.status = 1, o.remark = "", o.config = {}, a.fields.forEach((s) => {
        o.config[s.key] = "";
      }), e && e.id)
        try {
          const d = (await T.get(e.id))?.data || {};
          o.id = d.id, o.merchantNo = d.merchantNo || "", o.channelCode = d.channelCode, o.status = Number(d.status ?? 1), o.remark = d.remark || "";
          const r = d.configMap || {};
          a.fields.forEach((i) => {
            const R = r?.[i.key];
            o.config[i.key] = i.sensitive && R === "***" ? "" : R ?? "";
          });
        } catch {
          v.warning("回显失败，将以新建模式打开");
        }
      E.value = !0;
    }
    function q(a) {
      return o.id && a.sensitive ? [] : a.required ? [{ required: !0, message: `请输入${a.label}`, trigger: "blur" }] : [];
    }
    async function G() {
      A.value && await A.value.validate(async (a) => {
        if (a) {
          $.value = !0;
          try {
            const e = {
              id: o.id || void 0,
              merchantNo: o.merchantNo || void 0,
              channelCode: o.channelCode,
              channelName: w.value?.name,
              payMethod: o.channelCode,
              provider: o.channelCode === "STRIPE" ? "STRIPE" : void 0,
              status: o.status,
              remark: o.remark,
              configMap: o.config || {}
            };
            await T.save(e), v.success("保存成功，敏感字段已 AES 加密落库"), E.value = !1, await x();
          } catch (e) {
            v.error("保存失败：" + (e?.message || e));
          } finally {
            $.value = !1;
          }
        }
      });
    }
    async function J(a) {
      const e = k.value[a.code];
      if (!e?.id) return;
      const s = Number(e.status) === 1;
      await B.confirm(
        `确认${s ? "停用" : "启用"} ${a.name}？`,
        "提示",
        { type: "warning" }
      );
      try {
        await T.toggle({ id: e.id, status: s ? 0 : 1 }), v.success("操作成功"), await x();
      } catch (d) {
        v.error("操作失败：" + (d?.message || d));
      }
    }
    async function Q(a) {
      const e = k.value[a.code];
      if (e?.id)
        try {
          const d = (await T.testConnect(e.id))?.data || {};
          d.success ? v.success(`${a.name} 凭证完整，可进入真实 SDK 联调`) : B.alert(
            `errorCode: ${d.errorCode || "CHANNEL_NOT_CONFIGURED"}
缺失字段: ${(d.missing || []).join(", ") || "—"}`,
            `${a.name} 测试连通失败`,
            { type: "error" }
          );
        } catch (s) {
          v.error("测试连通失败：" + (s?.message || s));
        }
    }
    function X() {
      A.value && A.value.resetFields?.(), w.value = null;
    }
    return re(x), (a, e) => {
      const s = u("el-alert"), d = u("el-checkbox"), r = u("el-input"), i = u("el-form-item"), R = u("el-option"), Z = u("el-select"), b = u("el-button"), F = u("el-form"), D = u("el-card"), ee = u("el-icon"), le = u("el-tag"), oe = u("el-col"), te = u("el-row"), H = u("el-radio"), ae = u("el-radio-group"), ne = u("el-dialog");
      return m(), V(
        S,
        null,
        [
          I(`
   渠道凭证管理（W4-SDK 联调阶段）
   - 渠道卡片：WECHAT_V3 / ALIPAY / STRIPE（可真联调）+ 骨架渠道可选显示
   - 卡片显示已/未配置状态 + 启停开关 + 测试连通
   - 点编辑弹窗按 channelCode 切换字段集，敏感字段提交 AES 加密
  `),
          f("div", ye, [
            t(s, {
              type: "info",
              closable: !1,
              "show-icon": "",
              title: "渠道编码与 PayMethodEnum / Strategy#getChannel 一致。可真联调：WECHAT_V3、ALIPAY、STRIPE。骨架渠道（YSF/FME/花呗/分账支付）仅占位，createPay 仍为 Mock。",
              style: { "margin-bottom": "12px" }
            }),
            f("div", ge, [
              t(d, {
                modelValue: L.value,
                "onUpdate:modelValue": e[0] || (e[0] = (l) => L.value = l)
              }, {
                default: n(() => [...e[9] || (e[9] = [
                  c(
                    "显示骨架/Mock 渠道",
                    -1
                    /* CACHED */
                  )
                ])]),
                _: 1
                /* STABLE */
              }, 8, ["modelValue"])
            ]),
            t(s, {
              title: "渠道真实 SDK 联调说明",
              type: "info",
              "show-icon": "",
              closable: !1,
              style: { "margin-bottom": "16px" }
            }, {
              default: n(() => [...e[10] || (e[10] = [
                c(
                  " 敏感字段（私钥 / API V3 Key / AppSecret 等）以 AES-256 字段级加密落库，列表/回显默认脱敏； 点击「编辑」可重新填写，「测试连通」校验凭证完整性，缺失字段返回 ",
                  -1
                  /* CACHED */
                ),
                f(
                  "b",
                  null,
                  "CHANNEL_NOT_CONFIGURED",
                  -1
                  /* CACHED */
                ),
                c(
                  "。 ",
                  -1
                  /* CACHED */
                )
              ])]),
              _: 1
              /* STABLE */
            }),
            I(" 顶部操作栏 "),
            t(D, {
              shadow: "never",
              style: { "margin-bottom": "16px" }
            }, {
              default: n(() => [
                t(F, {
                  inline: !0,
                  model: p,
                  onSubmit: e[3] || (e[3] = ie(() => {
                  }, ["prevent"]))
                }, {
                  default: n(() => [
                    t(i, { label: "商户号" }, {
                      default: n(() => [
                        t(r, {
                          modelValue: p.merchantNo,
                          "onUpdate:modelValue": e[1] || (e[1] = (l) => p.merchantNo = l),
                          placeholder: "留空则查全局兜底配置",
                          clearable: "",
                          style: { width: "200px" }
                        }, null, 8, ["modelValue"])
                      ]),
                      _: 1
                      /* STABLE */
                    }),
                    t(i, { label: "渠道" }, {
                      default: n(() => [
                        t(Z, {
                          modelValue: p.channelCode,
                          "onUpdate:modelValue": e[2] || (e[2] = (l) => p.channelCode = l),
                          clearable: "",
                          placeholder: "全部渠道",
                          style: { width: "180px" }
                        }, {
                          default: n(() => [
                            (m(!0), V(
                              S,
                              null,
                              M(U.value, (l) => (m(), h(R, {
                                key: l.code,
                                label: l.name,
                                value: l.code
                              }, null, 8, ["label", "value"]))),
                              128
                              /* KEYED_FRAGMENT */
                            ))
                          ]),
                          _: 1
                          /* STABLE */
                        }, 8, ["modelValue"])
                      ]),
                      _: 1
                      /* STABLE */
                    }),
                    t(i, null, {
                      default: n(() => [
                        t(b, {
                          type: "primary",
                          onClick: x
                        }, {
                          default: n(() => [...e[11] || (e[11] = [
                            c(
                              "查询",
                              -1
                              /* CACHED */
                            )
                          ])]),
                          _: 1
                          /* STABLE */
                        }),
                        t(b, { onClick: W }, {
                          default: n(() => [...e[12] || (e[12] = [
                            c(
                              "重置",
                              -1
                              /* CACHED */
                            )
                          ])]),
                          _: 1
                          /* STABLE */
                        })
                      ]),
                      _: 1
                      /* STABLE */
                    })
                  ]),
                  _: 1
                  /* STABLE */
                }, 8, ["model"])
              ]),
              _: 1
              /* STABLE */
            }),
            I(" 渠道卡片 "),
            t(te, { gutter: 16 }, {
              default: n(() => [
                (m(!0), V(
                  S,
                  null,
                  M(U.value, (l) => (m(), h(
                    oe,
                    {
                      key: l.code,
                      xs: 24,
                      sm: 12,
                      md: 12,
                      lg: 8
                    },
                    {
                      default: n(() => [
                        t(
                          D,
                          {
                            class: "channel-card",
                            shadow: "hover"
                          },
                          {
                            default: n(() => [
                              f("div", ve, [
                                f("div", Ce, [
                                  t(ee, {
                                    size: 22,
                                    color: l.color
                                  }, {
                                    default: n(() => [
                                      (m(), h(ue(l.icon)))
                                    ]),
                                    _: 2
                                    /* DYNAMIC */
                                  }, 1032, ["color"]),
                                  f(
                                    "span",
                                    null,
                                    g(l.name),
                                    1
                                    /* TEXT */
                                  )
                                ]),
                                t(le, {
                                  type: C(l.code).tagType,
                                  size: "small"
                                }, {
                                  default: n(() => [
                                    c(
                                      g(C(l.code).text),
                                      1
                                      /* TEXT */
                                    )
                                  ]),
                                  _: 2
                                  /* DYNAMIC */
                                }, 1032, ["type"])
                              ]),
                              f(
                                "div",
                                be,
                                g(l.desc) + g(l.stubReason ? "（" + l.stubReason + "）" : ""),
                                1
                                /* TEXT */
                              ),
                              f("div", ke, [
                                (m(!0), V(
                                  S,
                                  null,
                                  M(l.fields, (_) => (m(), V("div", {
                                    class: "field-row",
                                    key: _.key
                                  }, [
                                    f(
                                      "span",
                                      we,
                                      g(_.label) + "：",
                                      1
                                      /* TEXT */
                                    ),
                                    f(
                                      "span",
                                      Ne,
                                      g(K(l.code, _.key)),
                                      1
                                      /* TEXT */
                                    )
                                  ]))),
                                  128
                                  /* KEYED_FRAGMENT */
                                ))
                              ]),
                              f("div", Ve, [
                                t(b, {
                                  size: "small",
                                  type: "primary",
                                  onClick: (_) => j(l)
                                }, {
                                  default: n(() => [
                                    c(
                                      g(C(l.code).configured ? "编辑" : "配置"),
                                      1
                                      /* TEXT */
                                    )
                                  ]),
                                  _: 2
                                  /* DYNAMIC */
                                }, 1032, ["onClick"]),
                                t(b, {
                                  size: "small",
                                  disabled: !C(l.code).configured,
                                  onClick: (_) => Q(l)
                                }, {
                                  default: n(() => [...e[13] || (e[13] = [
                                    c(
                                      "测试连通",
                                      -1
                                      /* CACHED */
                                    )
                                  ])]),
                                  _: 1
                                  /* STABLE */
                                }, 8, ["disabled", "onClick"]),
                                t(b, {
                                  size: "small",
                                  type: C(l.code).enabled ? "warning" : "success",
                                  disabled: !C(l.code).configured,
                                  onClick: (_) => J(l)
                                }, {
                                  default: n(() => [
                                    c(
                                      g(C(l.code).enabled ? "停用" : "启用"),
                                      1
                                      /* TEXT */
                                    )
                                  ]),
                                  _: 2
                                  /* DYNAMIC */
                                }, 1032, ["type", "disabled", "onClick"])
                              ])
                            ]),
                            _: 2
                            /* DYNAMIC */
                          },
                          1024
                          /* DYNAMIC_SLOTS */
                        )
                      ]),
                      _: 2
                      /* DYNAMIC */
                    },
                    1024
                    /* DYNAMIC_SLOTS */
                  ))),
                  128
                  /* KEYED_FRAGMENT */
                ))
              ]),
              _: 1
              /* STABLE */
            }),
            I(" 编辑弹窗（按 channelCode 切换动态表单） "),
            t(ne, {
              modelValue: E.value,
              "onUpdate:modelValue": e[8] || (e[8] = (l) => E.value = l),
              title: `${w.value?.name || ""} 凭证配置`,
              width: "640px",
              "close-on-click-modal": !1,
              onClose: X
            }, {
              footer: n(() => [
                t(b, {
                  onClick: e[7] || (e[7] = (l) => E.value = !1)
                }, {
                  default: n(() => [...e[16] || (e[16] = [
                    c(
                      "取消",
                      -1
                      /* CACHED */
                    )
                  ])]),
                  _: 1
                  /* STABLE */
                }),
                t(b, {
                  type: "primary",
                  loading: $.value,
                  onClick: G
                }, {
                  default: n(() => [...e[17] || (e[17] = [
                    c(
                      "保存",
                      -1
                      /* CACHED */
                    )
                  ])]),
                  _: 1
                  /* STABLE */
                }, 8, ["loading"])
              ]),
              default: n(() => [
                t(F, {
                  ref_key: "formRef",
                  ref: A,
                  model: o,
                  rules: O,
                  "label-width": "140px"
                }, {
                  default: n(() => [
                    t(i, {
                      label: "商户号",
                      prop: "merchantNo"
                    }, {
                      default: n(() => [
                        t(r, {
                          modelValue: o.merchantNo,
                          "onUpdate:modelValue": e[4] || (e[4] = (l) => o.merchantNo = l),
                          placeholder: "留空表示该渠道全局兜底配置"
                        }, null, 8, ["modelValue"])
                      ]),
                      _: 1
                      /* STABLE */
                    }),
                    t(i, { label: "渠道编码" }, {
                      default: n(() => [
                        t(r, {
                          value: w.value?.code,
                          disabled: ""
                        }, null, 8, ["value"])
                      ]),
                      _: 1
                      /* STABLE */
                    }),
                    (m(!0), V(
                      S,
                      null,
                      M(w.value?.fields || [], (l) => (m(), h(i, {
                        key: l.key,
                        label: l.label,
                        prop: `config.${l.key}`,
                        rules: q(l)
                      }, {
                        default: n(() => [
                          l.type === "textarea" ? (m(), h(r, {
                            key: 0,
                            modelValue: o.config[l.key],
                            "onUpdate:modelValue": (_) => o.config[l.key] = _,
                            type: "textarea",
                            rows: 4,
                            placeholder: l.placeholder || `请输入${l.label}`
                          }, null, 8, ["modelValue", "onUpdate:modelValue", "placeholder"])) : (m(), h(r, {
                            key: 1,
                            modelValue: o.config[l.key],
                            "onUpdate:modelValue": (_) => o.config[l.key] = _,
                            placeholder: l.placeholder || `请输入${l.label}`,
                            type: l.sensitive ? "password" : "text",
                            "show-password": !!l.sensitive
                          }, null, 8, ["modelValue", "onUpdate:modelValue", "placeholder", "type", "show-password"]))
                        ]),
                        _: 2
                        /* DYNAMIC */
                      }, 1032, ["label", "prop", "rules"]))),
                      128
                      /* KEYED_FRAGMENT */
                    )),
                    t(i, { label: "状态" }, {
                      default: n(() => [
                        t(ae, {
                          modelValue: o.status,
                          "onUpdate:modelValue": e[5] || (e[5] = (l) => o.status = l)
                        }, {
                          default: n(() => [
                            t(H, { label: 1 }, {
                              default: n(() => [...e[14] || (e[14] = [
                                c(
                                  "启用",
                                  -1
                                  /* CACHED */
                                )
                              ])]),
                              _: 1
                              /* STABLE */
                            }),
                            t(H, { label: 0 }, {
                              default: n(() => [...e[15] || (e[15] = [
                                c(
                                  "停用",
                                  -1
                                  /* CACHED */
                                )
                              ])]),
                              _: 1
                              /* STABLE */
                            })
                          ]),
                          _: 1
                          /* STABLE */
                        }, 8, ["modelValue"])
                      ]),
                      _: 1
                      /* STABLE */
                    }),
                    t(i, { label: "备注" }, {
                      default: n(() => [
                        t(r, {
                          modelValue: o.remark,
                          "onUpdate:modelValue": e[6] || (e[6] = (l) => o.remark = l),
                          placeholder: "可选，配置说明"
                        }, null, 8, ["modelValue"])
                      ]),
                      _: 1
                      /* STABLE */
                    })
                  ]),
                  _: 1
                  /* STABLE */
                }, 8, ["model"])
              ]),
              _: 1
              /* STABLE */
            }, 8, ["modelValue", "title"])
          ])
        ],
        2112
        /* STABLE_FRAGMENT, DEV_ROOT_FRAGMENT */
      );
    };
  }
}, Re = /* @__PURE__ */ se(he, [["__scopeId", "data-v-14a9529b"]]);
export {
  Re as default
};
