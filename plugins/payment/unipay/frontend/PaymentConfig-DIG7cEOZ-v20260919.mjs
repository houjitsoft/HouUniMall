import { _ as re, r as u, a as ce, c as j, o as de, E as d, b as r, p as ue, d as K, e as p, h as o, w as s, q as pe, g as z, k as v, i as c, t as y, f as F, F as R, j as q, v as B } from "./_plugin-vue_export-helper-Bh3pUN_y-v20260919.mjs";
import { c as E } from "./paymentBizW4-_PSB1goI-v20260919.mjs";
import { p as me } from "./pay-B7ybr3qD-v20260919.mjs";
import { c as fe, f as _ } from "./channelCatalog-BlFpTUS6-v20260919.mjs";
const ge = { class: "payment-config" }, he = { class: "card-header" }, ve = { class: "header-actions" }, ye = { class: "pagination-container" }, _e = {
  __name: "PaymentConfig",
  setup(Ce) {
    const I = u(!1), T = u(!1), f = u(!1), U = u(!1), $ = u(""), P = u(null), C = u(1), b = u(10), k = u(0), M = u([]), V = u(!1), n = ce({
      id: "",
      channelCode: "",
      channelName: "",
      provider: "",
      payMethod: "",
      merchantNo: "",
      remark: "",
      status: 1,
      configMap: {}
    }), H = {
      channelCode: [{ required: !0, message: "请选择支付渠道", trigger: "change" }]
    }, W = j(() => fe({ includeStub: V.value })), N = j(() => _(n.channelCode));
    function D(t) {
      return _(t)?.maturity || "stub";
    }
    function L(t) {
      const e = {};
      (t?.fields || []).forEach((l) => {
        e[l.key] = "";
      }), n.configMap = e;
    }
    function O(t) {
      const e = _(t);
      n.channelName = e?.name || "", n.provider = e?.provider || "", n.payMethod = t, L(e);
    }
    de(() => {
      g();
    });
    async function g() {
      I.value = !0;
      try {
        const t = await E.list({
          page: C.value,
          size: b.value,
          pageNum: C.value,
          pageSize: b.value
        });
        if (t.code === 200 || t.success) {
          const e = t.data || {};
          let l = e.records || e.list || [];
          V.value || (l = l.filter((h) => D(h.channelCode) === "live")), M.value = l, k.value = e.total || l.length;
        } else
          d.error(t.message || "获取支付配置失败"), M.value = [], k.value = 0;
      } catch (t) {
        console.error(t), d.error("获取支付配置失败: " + (t.message || "网络错误")), M.value = [], k.value = 0;
      } finally {
        I.value = !1;
      }
    }
    function Y(t) {
      if (!t) return "-";
      const e = new Date(t);
      return isNaN(e.getTime()) ? "-" : e.toLocaleString("zh-CN");
    }
    function G() {
      U.value = !1, $.value = "新增支付配置", Object.assign(n, {
        id: "",
        channelCode: "WECHAT_V3",
        channelName: "",
        provider: "",
        payMethod: "",
        merchantNo: "",
        remark: "",
        status: 1,
        configMap: {}
      }), O("WECHAT_V3"), f.value = !0;
    }
    async function J(t) {
      U.value = !0, $.value = `编辑凭证 · ${t.channelCode}`, Object.assign(n, {
        id: t.id,
        channelCode: t.channelCode,
        channelName: t.channelName,
        provider: t.provider,
        payMethod: t.payMethod || t.channelCode,
        merchantNo: t.merchantNo || "",
        remark: t.remark || "",
        status: t.status,
        configMap: {}
      }), L(_(t.channelCode));
      try {
        const e = await E.get(t.id);
        if (e.code === 200 && e.data) {
          const l = e.data, h = l.configMap || l.config || {};
          Object.keys(n.configMap).forEach((i) => {
            h[i] != null && (n.configMap[i] = String(h[i]));
          }), ["appId", "mchId", "merchantId", "merchantKey", "apiV3Key"].forEach((i) => {
            l[i] != null && n.configMap[i] !== void 0 && !n.configMap[i] && (n.configMap[i] = String(l[i]));
          }), l.merchantId && n.configMap.mchId !== void 0 && !n.configMap.mchId && (n.configMap.mchId = String(l.merchantId)), l.merchantKey && n.configMap.apiV3Key !== void 0 && !n.configMap.apiV3Key && (n.configMap.apiV3Key = String(l.merchantKey));
        }
      } catch (e) {
        console.warn("load credential detail failed", e);
      }
      f.value = !0;
    }
    async function Q() {
      try {
        await P.value.validate();
      } catch {
        return;
      }
      const t = _(n.channelCode);
      if (!t) {
        d.error("未知渠道编码");
        return;
      }
      T.value = !0;
      try {
        const e = {
          id: n.id || void 0,
          channelCode: n.channelCode,
          channelName: t.name,
          provider: t.provider,
          payMethod: n.channelCode,
          merchantNo: n.merchantNo || null,
          remark: n.remark,
          status: n.status ?? 1,
          configMap: { ...n.configMap }
        }, l = await E.save(e);
        l.code === 200 || l.success ? (d.success("保存成功（已按 Strategy 渠道码写入 hou_pay_channel）"), f.value = !1, g()) : d.error(l.message || "保存失败");
      } catch (e) {
        d.error("保存失败: " + (e.message || "网络错误"));
      } finally {
        T.value = !1;
      }
    }
    async function X(t) {
      const e = t.status === 1 ? 0 : 1;
      try {
        await B.confirm(`确认${e === 1 ? "启用" : "禁用"} ${t.channelCode}？`, "提示", { type: "warning" });
        const l = await E.toggle({ id: t.id, status: e });
        l.code === 200 || l.success ? (d.success("状态已更新"), g()) : d.error(l.message || "操作失败");
      } catch (l) {
        l !== "cancel" && d.error(l.message || "操作失败");
      }
    }
    async function Z(t) {
      try {
        await B.confirm(`确认删除 ${t.channelCode} 配置？`, "警告", { type: "warning" });
        const e = await me.deletePaymentConfig({ id: t.id });
        e.code === 200 || e.success ? (d.success("删除成功"), g()) : d.error(e.message || "删除失败");
      } catch (e) {
        e !== "cancel" && d.error(e.message || "删除失败");
      }
    }
    return (t, e) => {
      const l = r("el-alert"), h = r("el-checkbox"), i = r("el-button"), m = r("el-table-column"), x = r("el-tag"), ee = r("el-table"), ae = r("el-pagination"), te = r("el-card"), ne = r("el-option"), le = r("el-select"), w = r("el-form-item"), A = r("el-input"), oe = r("el-form"), se = r("el-dialog"), ie = ue("loading");
      return p(), K("div", ge, [
        o(l, {
          type: "info",
          closable: !1,
          "show-icon": "",
          title: "支付设置与渠道凭证共用 hou_pay_channel；渠道编码必须与收银台 Strategy 一致（WECHAT_V3 / ALIPAY / STRIPE）。骨架渠道仅供联调占位，不会真扣款。",
          style: { "margin-bottom": "12px" }
        }),
        o(te, { class: "box-card" }, {
          header: s(() => [
            z("div", he, [
              e[10] || (e[10] = z(
                "span",
                null,
                "支付配置",
                -1
                /* CACHED */
              )),
              z("div", ve, [
                o(h, {
                  modelValue: V.value,
                  "onUpdate:modelValue": e[0] || (e[0] = (a) => V.value = a)
                }, {
                  default: s(() => [...e[8] || (e[8] = [
                    c(
                      "显示骨架/Mock 渠道",
                      -1
                      /* CACHED */
                    )
                  ])]),
                  _: 1
                  /* STABLE */
                }, 8, ["modelValue"]),
                o(i, {
                  type: "primary",
                  onClick: G
                }, {
                  default: s(() => [...e[9] || (e[9] = [
                    c(
                      "新增配置",
                      -1
                      /* CACHED */
                    )
                  ])]),
                  _: 1
                  /* STABLE */
                })
              ])
            ])
          ]),
          default: s(() => [
            pe((p(), v(ee, {
              data: M.value,
              style: { width: "100%" }
            }, {
              default: s(() => [
                o(m, {
                  prop: "channelName",
                  label: "支付渠道",
                  "min-width": "160"
                }),
                o(m, {
                  prop: "channelCode",
                  label: "渠道编码",
                  width: "140"
                }, {
                  default: s(({ row: a }) => [
                    o(
                      x,
                      { size: "small" },
                      {
                        default: s(() => [
                          c(
                            y(a.channelCode),
                            1
                            /* TEXT */
                          )
                        ]),
                        _: 2
                        /* DYNAMIC */
                      },
                      1024
                      /* DYNAMIC_SLOTS */
                    )
                  ]),
                  _: 1
                  /* STABLE */
                }),
                o(m, {
                  label: "成熟度",
                  width: "120"
                }, {
                  default: s(({ row: a }) => [
                    D(a.channelCode) === "live" ? (p(), v(x, {
                      key: 0,
                      type: "success",
                      size: "small"
                    }, {
                      default: s(() => [...e[11] || (e[11] = [
                        c(
                          "可真联调",
                          -1
                          /* CACHED */
                        )
                      ])]),
                      _: 1
                      /* STABLE */
                    })) : (p(), v(x, {
                      key: 1,
                      type: "warning",
                      size: "small"
                    }, {
                      default: s(() => [...e[12] || (e[12] = [
                        c(
                          "骨架/Mock",
                          -1
                          /* CACHED */
                        )
                      ])]),
                      _: 1
                      /* STABLE */
                    }))
                  ]),
                  _: 1
                  /* STABLE */
                }),
                o(m, {
                  prop: "merchantNo",
                  label: "商户号",
                  width: "140"
                }, {
                  default: s(({ row: a }) => [
                    c(
                      y(a.merchantNo || "全局"),
                      1
                      /* TEXT */
                    )
                  ]),
                  _: 1
                  /* STABLE */
                }),
                o(m, {
                  prop: "status",
                  label: "状态",
                  width: "100"
                }, {
                  default: s(({ row: a }) => [
                    o(x, {
                      type: a.status === 1 ? "success" : "danger"
                    }, {
                      default: s(() => [
                        c(
                          y(a.status === 1 ? "启用" : "禁用"),
                          1
                          /* TEXT */
                        )
                      ]),
                      _: 2
                      /* DYNAMIC */
                    }, 1032, ["type"])
                  ]),
                  _: 1
                  /* STABLE */
                }),
                o(m, {
                  prop: "updateTime",
                  label: "更新时间",
                  width: "180"
                }, {
                  default: s(({ row: a }) => [
                    c(
                      y(Y(a.updateTime || a.createTime)),
                      1
                      /* TEXT */
                    )
                  ]),
                  _: 1
                  /* STABLE */
                }),
                o(m, {
                  label: "操作",
                  width: "220",
                  fixed: "right"
                }, {
                  default: s(({ row: a }) => [
                    o(i, {
                      size: "small",
                      onClick: (S) => J(a)
                    }, {
                      default: s(() => [...e[13] || (e[13] = [
                        c(
                          "编辑凭证",
                          -1
                          /* CACHED */
                        )
                      ])]),
                      _: 1
                      /* STABLE */
                    }, 8, ["onClick"]),
                    o(i, {
                      size: "small",
                      onClick: (S) => X(a)
                    }, {
                      default: s(() => [
                        c(
                          y(a.status === 1 ? "禁用" : "启用"),
                          1
                          /* TEXT */
                        )
                      ]),
                      _: 2
                      /* DYNAMIC */
                    }, 1032, ["onClick"]),
                    o(i, {
                      size: "small",
                      type: "danger",
                      onClick: (S) => Z(a)
                    }, {
                      default: s(() => [...e[14] || (e[14] = [
                        c(
                          "删除",
                          -1
                          /* CACHED */
                        )
                      ])]),
                      _: 1
                      /* STABLE */
                    }, 8, ["onClick"])
                  ]),
                  _: 1
                  /* STABLE */
                })
              ]),
              _: 1
              /* STABLE */
            }, 8, ["data"])), [
              [ie, I.value]
            ]),
            z("div", ye, [
              o(ae, {
                "current-page": C.value,
                "onUpdate:currentPage": e[1] || (e[1] = (a) => C.value = a),
                "page-size": b.value,
                "onUpdate:pageSize": e[2] || (e[2] = (a) => b.value = a),
                "page-sizes": [10, 20, 50, 100],
                total: k.value,
                layout: "total, sizes, prev, pager, next, jumper",
                onSizeChange: g,
                onCurrentChange: g
              }, null, 8, ["current-page", "page-size", "total"])
            ])
          ]),
          _: 1
          /* STABLE */
        }),
        o(se, {
          modelValue: f.value,
          "onUpdate:modelValue": e[7] || (e[7] = (a) => f.value = a),
          title: $.value,
          width: "640px",
          "close-on-click-modal": !1,
          "destroy-on-close": ""
        }, {
          footer: s(() => [
            o(i, {
              onClick: e[6] || (e[6] = (a) => f.value = !1)
            }, {
              default: s(() => [...e[15] || (e[15] = [
                c(
                  "取消",
                  -1
                  /* CACHED */
                )
              ])]),
              _: 1
              /* STABLE */
            }),
            o(i, {
              type: "primary",
              loading: T.value,
              onClick: Q
            }, {
              default: s(() => [...e[16] || (e[16] = [
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
          default: s(() => [
            N.value?.maturity === "stub" ? (p(), v(l, {
              key: 0,
              type: "warning",
              closable: !1,
              "show-icon": "",
              title: N.value.stubReason || "该渠道仅为骨架/Mock，保存凭证也不会走真实扣款",
              style: { "margin-bottom": "12px" }
            }, null, 8, ["title"])) : F("v-if", !0),
            o(oe, {
              model: n,
              rules: H,
              ref_key: "configFormRef",
              ref: P,
              "label-width": "140px"
            }, {
              default: s(() => [
                o(w, {
                  label: "支付渠道",
                  prop: "channelCode"
                }, {
                  default: s(() => [
                    o(le, {
                      modelValue: n.channelCode,
                      "onUpdate:modelValue": e[3] || (e[3] = (a) => n.channelCode = a),
                      placeholder: "请选择支付渠道",
                      disabled: U.value,
                      style: { width: "100%" },
                      onChange: O
                    }, {
                      default: s(() => [
                        (p(!0), K(
                          R,
                          null,
                          q(W.value, (a) => (p(), v(ne, {
                            key: a.value,
                            label: a.label,
                            value: a.value
                          }, null, 8, ["label", "value"]))),
                          128
                          /* KEYED_FRAGMENT */
                        ))
                      ]),
                      _: 1
                      /* STABLE */
                    }, 8, ["modelValue", "disabled"])
                  ]),
                  _: 1
                  /* STABLE */
                }),
                o(w, { label: "商户号 merchantNo" }, {
                  default: s(() => [
                    o(A, {
                      modelValue: n.merchantNo,
                      "onUpdate:modelValue": e[4] || (e[4] = (a) => n.merchantNo = a),
                      placeholder: "留空=全局凭证；多商户时填业务商户号"
                    }, null, 8, ["modelValue"])
                  ]),
                  _: 1
                  /* STABLE */
                }),
                N.value ? (p(!0), K(
                  R,
                  { key: 0 },
                  q(N.value.fields, (a) => (p(), v(w, {
                    key: a.key,
                    label: a.label,
                    prop: "configMap." + a.key,
                    rules: a.required ? [{ required: !0, message: `请输入${a.label}`, trigger: "blur" }] : []
                  }, {
                    default: s(() => [
                      o(A, {
                        modelValue: n.configMap[a.key],
                        "onUpdate:modelValue": (S) => n.configMap[a.key] = S,
                        type: a.type === "textarea" ? "textarea" : a.sensitive ? "password" : "text",
                        "show-password": !!a.sensitive && a.type !== "textarea",
                        rows: a.type === "textarea" ? 4 : void 0,
                        placeholder: a.placeholder || "",
                        autocomplete: "off"
                      }, null, 8, ["modelValue", "onUpdate:modelValue", "type", "show-password", "rows", "placeholder"])
                    ]),
                    _: 2
                    /* DYNAMIC */
                  }, 1032, ["label", "prop", "rules"]))),
                  128
                  /* KEYED_FRAGMENT */
                )) : F("v-if", !0),
                o(w, { label: "备注" }, {
                  default: s(() => [
                    o(A, {
                      modelValue: n.remark,
                      "onUpdate:modelValue": e[5] || (e[5] = (a) => n.remark = a),
                      type: "textarea",
                      placeholder: "可选备注"
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
      ]);
    };
  }
}, Ne = /* @__PURE__ */ re(_e, [["__scopeId", "data-v-58667f4c"]]);
export {
  Ne as default
};
