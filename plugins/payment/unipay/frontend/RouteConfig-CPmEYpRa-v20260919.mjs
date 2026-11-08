import { _ as J, c as K, r as b, a as U, o as Q, b as s, p as Z, d as E, e as g, h as t, w as a, q as ee, g as v, k as h, f as te, t as x, i as u, F as le, j as ae, s as V, E as c } from "./_plugin-vue_export-helper-Bh3pUN_y-v20260919.mjs";
import { c as ne, f as I, C as oe } from "./channelCatalog-BlFpTUS6-v20260919.mjs";
const se = { class: "route-config-page" }, ie = { class: "card-header" }, ue = { style: { "margin-top": "12px", display: "flex", "justify-content": "flex-end" } }, de = {
  __name: "RouteConfig",
  setup(re) {
    const L = K(() => ne({ includeStub: !0 })), Y = (n) => {
      const e = I(n);
      return e ? e.name : { WX: "WX（请改为 WECHAT_V3）", ALI: "ALI（请改为 ALIPAY）", UNION: "UNION（无 Strategy）", YSF: "YSF（请改为 YSF_PAY）" }[n] || n || "-";
    }, D = (n) => {
      const e = I(n);
      return e ? e.maturity === oe.STUB : !1;
    }, w = b(!1), z = b([]), d = U({ current: 1, size: 10, total: 0 }), r = b(!1), f = b(null), o = U({ routeName: "", channelCode: "WECHAT_V3", bizType: "1", priority: 10, minAmount: 0, maxAmount: 0, weight: 50 }), _ = async () => {
      w.value = !0;
      try {
        const n = await V.post("/web/pay/channel/route/list", { page: d.current, size: d.size });
        n.code === 200 && n.data && (z.value = n.data.records || n.data.list || [], d.total = n.data.total || 0);
      } catch (n) {
        console.error(n);
      } finally {
        w.value = !1;
      }
    }, O = () => {
      f.value = null, Object.assign(o, { routeName: "", channelCode: "WECHAT_V3", bizType: "1", priority: 10, minAmount: 0, maxAmount: 0, weight: 50 }), r.value = !0;
    }, W = (n) => {
      f.value = n, Object.assign(o, n), r.value = !0;
    }, F = async () => {
      const n = { ...o };
      f.value && (n.id = f.value.id);
      const e = await V.post("/web/pay/channel/route/save", n);
      e.code === 200 ? (c.success("保存成功"), r.value = !1, _()) : c.error(e.message || "操作失败");
    }, P = async (n) => {
      const e = await V.post("/web/pay/channel/route/changeStatus", { id: n.id, status: n.status === 1 ? 0 : 1 });
      e.code === 200 ? (c.success("状态变更成功"), _()) : c.error(e.message || "操作失败");
    }, H = async (n) => {
      const e = await V.post("/web/pay/channel/route/delete", { id: n.id });
      e.code === 200 ? (c.success("删除成功"), _()) : c.error(e.message || "操作失败");
    };
    return Q(() => _()), (n, e) => {
      const k = s("el-alert"), p = s("el-button"), i = s("el-table-column"), N = s("el-tag"), R = s("el-popconfirm"), j = s("el-table"), B = s("el-pagination"), M = s("el-card"), $ = s("el-input"), m = s("el-form-item"), C = s("el-option"), S = s("el-select"), y = s("el-input-number"), A = s("el-col"), X = s("el-form"), q = s("el-dialog"), G = Z("loading");
      return g(), E("div", se, [
        t(k, {
          type: "info",
          closable: !1,
          "show-icon": "",
          title: "路由目标渠道须写 PayMethodEnum / Strategy 码：WECHAT_V3、ALIPAY、STRIPE（可真联调）。旧码 WX/ALI/UNION/YSF 不会命中收银台 Strategy。",
          style: { "margin-bottom": "12px" }
        }),
        t(M, { shadow: "never" }, {
          header: a(() => [
            v("div", ie, [
              e[12] || (e[12] = v(
                "span",
                null,
                "路由规则配置",
                -1
                /* CACHED */
              )),
              t(p, {
                type: "primary",
                size: "small",
                onClick: O
              }, {
                default: a(() => [...e[11] || (e[11] = [
                  u(
                    "新增规则",
                    -1
                    /* CACHED */
                  )
                ])]),
                _: 1
                /* STABLE */
              })
            ])
          ]),
          default: a(() => [
            ee((g(), h(j, {
              data: z.value,
              stripe: ""
            }, {
              default: a(() => [
                t(i, {
                  prop: "id",
                  label: "ID",
                  width: "70"
                }),
                t(i, {
                  prop: "routeName",
                  label: "规则名称",
                  "min-width": "140"
                }),
                t(i, {
                  prop: "channelCode",
                  label: "目标渠道",
                  "min-width": "160"
                }, {
                  default: a(({ row: l }) => [
                    v(
                      "span",
                      null,
                      x(Y(l.channelCode)),
                      1
                      /* TEXT */
                    ),
                    D(l.channelCode) ? (g(), h(N, {
                      key: 0,
                      type: "warning",
                      size: "small",
                      style: { "margin-left": "6px" }
                    }, {
                      default: a(() => [...e[13] || (e[13] = [
                        u(
                          "骨架",
                          -1
                          /* CACHED */
                        )
                      ])]),
                      _: 1
                      /* STABLE */
                    })) : te("v-if", !0)
                  ]),
                  _: 1
                  /* STABLE */
                }),
                t(i, {
                  prop: "priority",
                  label: "优先级",
                  width: "80",
                  align: "center"
                }),
                t(i, {
                  prop: "bizType",
                  label: "业务类型",
                  width: "100"
                }),
                t(i, {
                  prop: "minAmount",
                  label: "最小金额",
                  width: "100",
                  align: "right"
                }),
                t(i, {
                  prop: "maxAmount",
                  label: "最大金额",
                  width: "100",
                  align: "right"
                }),
                t(i, {
                  prop: "weight",
                  label: "权重",
                  width: "70",
                  align: "center"
                }),
                t(i, {
                  prop: "status",
                  label: "状态",
                  width: "80"
                }, {
                  default: a(({ row: l }) => [
                    t(N, {
                      type: l.status === 1 ? "success" : "info",
                      size: "small"
                    }, {
                      default: a(() => [
                        u(
                          x(l.status === 1 ? "启用" : "停用"),
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
                t(i, {
                  label: "操作",
                  width: "180",
                  fixed: "right"
                }, {
                  default: a(({ row: l }) => [
                    t(p, {
                      link: "",
                      type: "primary",
                      size: "small",
                      onClick: (T) => W(l)
                    }, {
                      default: a(() => [...e[14] || (e[14] = [
                        u(
                          "编辑",
                          -1
                          /* CACHED */
                        )
                      ])]),
                      _: 1
                      /* STABLE */
                    }, 8, ["onClick"]),
                    t(p, {
                      link: "",
                      type: l.status === 1 ? "warning" : "success",
                      size: "small",
                      onClick: (T) => P(l)
                    }, {
                      default: a(() => [
                        u(
                          x(l.status === 1 ? "停用" : "启用"),
                          1
                          /* TEXT */
                        )
                      ]),
                      _: 2
                      /* DYNAMIC */
                    }, 1032, ["type", "onClick"]),
                    t(R, {
                      title: "确认删除?",
                      onConfirm: (T) => H(l)
                    }, {
                      reference: a(() => [
                        t(p, {
                          link: "",
                          type: "danger",
                          size: "small"
                        }, {
                          default: a(() => [...e[15] || (e[15] = [
                            u(
                              "删除",
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
                    }, 8, ["onConfirm"])
                  ]),
                  _: 1
                  /* STABLE */
                })
              ]),
              _: 1
              /* STABLE */
            }, 8, ["data"])), [
              [G, w.value]
            ]),
            v("div", ue, [
              t(B, {
                "current-page": d.current,
                "onUpdate:currentPage": e[0] || (e[0] = (l) => d.current = l),
                "page-size": d.size,
                "onUpdate:pageSize": e[1] || (e[1] = (l) => d.size = l),
                total: d.total,
                "page-sizes": [10, 20, 50],
                layout: "total,sizes,prev,pager,next",
                background: "",
                onChange: _
              }, null, 8, ["current-page", "page-size", "total"])
            ])
          ]),
          _: 1
          /* STABLE */
        }),
        t(q, {
          modelValue: r.value,
          "onUpdate:modelValue": e[10] || (e[10] = (l) => r.value = l),
          title: f.value ? "编辑路由规则" : "新增路由规则",
          width: "520px"
        }, {
          footer: a(() => [
            t(p, {
              onClick: e[9] || (e[9] = (l) => r.value = !1)
            }, {
              default: a(() => [...e[17] || (e[17] = [
                u(
                  "取消",
                  -1
                  /* CACHED */
                )
              ])]),
              _: 1
              /* STABLE */
            }),
            t(p, {
              type: "primary",
              onClick: F
            }, {
              default: a(() => [...e[18] || (e[18] = [
                u(
                  "保存",
                  -1
                  /* CACHED */
                )
              ])]),
              _: 1
              /* STABLE */
            })
          ]),
          default: a(() => [
            t(X, {
              model: o,
              "label-width": "100px"
            }, {
              default: a(() => [
                t(m, { label: "规则名称" }, {
                  default: a(() => [
                    t($, {
                      modelValue: o.routeName,
                      "onUpdate:modelValue": e[2] || (e[2] = (l) => o.routeName = l)
                    }, null, 8, ["modelValue"])
                  ]),
                  _: 1
                  /* STABLE */
                }),
                t(m, { label: "目标渠道" }, {
                  default: a(() => [
                    t(S, {
                      modelValue: o.channelCode,
                      "onUpdate:modelValue": e[3] || (e[3] = (l) => o.channelCode = l),
                      style: { width: "100%" },
                      filterable: ""
                    }, {
                      default: a(() => [
                        (g(!0), E(
                          le,
                          null,
                          ae(L.value, (l) => (g(), h(C, {
                            key: l.value,
                            label: l.label,
                            value: l.value
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
                t(m, { label: "业务类型" }, {
                  default: a(() => [
                    t(S, {
                      modelValue: o.bizType,
                      "onUpdate:modelValue": e[4] || (e[4] = (l) => o.bizType = l),
                      style: { width: "100%" }
                    }, {
                      default: a(() => [
                        t(C, {
                          label: "支付",
                          value: "1"
                        }),
                        t(C, {
                          label: "退款",
                          value: "2"
                        })
                      ]),
                      _: 1
                      /* STABLE */
                    }, 8, ["modelValue"])
                  ]),
                  _: 1
                  /* STABLE */
                }),
                t(m, { label: "优先级" }, {
                  default: a(() => [
                    t(y, {
                      modelValue: o.priority,
                      "onUpdate:modelValue": e[5] || (e[5] = (l) => o.priority = l),
                      min: 1,
                      max: 99
                    }, null, 8, ["modelValue"])
                  ]),
                  _: 1
                  /* STABLE */
                }),
                t(m, { label: "金额范围" }, {
                  default: a(() => [
                    t(A, { span: 11 }, {
                      default: a(() => [
                        t(y, {
                          modelValue: o.minAmount,
                          "onUpdate:modelValue": e[6] || (e[6] = (l) => o.minAmount = l),
                          min: 0,
                          placeholder: "最小",
                          style: { width: "100%" }
                        }, null, 8, ["modelValue"])
                      ]),
                      _: 1
                      /* STABLE */
                    }),
                    t(A, {
                      span: 2,
                      style: { "text-align": "center" }
                    }, {
                      default: a(() => [...e[16] || (e[16] = [
                        u(
                          "~",
                          -1
                          /* CACHED */
                        )
                      ])]),
                      _: 1
                      /* STABLE */
                    }),
                    t(A, { span: 11 }, {
                      default: a(() => [
                        t(y, {
                          modelValue: o.maxAmount,
                          "onUpdate:modelValue": e[7] || (e[7] = (l) => o.maxAmount = l),
                          min: 0,
                          placeholder: "最大",
                          style: { width: "100%" }
                        }, null, 8, ["modelValue"])
                      ]),
                      _: 1
                      /* STABLE */
                    })
                  ]),
                  _: 1
                  /* STABLE */
                }),
                t(m, { label: "权重" }, {
                  default: a(() => [
                    t(y, {
                      modelValue: o.weight,
                      "onUpdate:modelValue": e[8] || (e[8] = (l) => o.weight = l),
                      min: 1,
                      max: 100
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
}, ce = /* @__PURE__ */ J(de, [["__scopeId", "data-v-200db305"]]);
export {
  ce as default
};
