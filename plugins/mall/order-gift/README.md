# 满赠（order-gift）

> 宿主 `order`，PF4J embedded 插件。表前缀 `hou_plugin_order_gift_*`。
> 后台菜单：插件应用 → 满赠。

## 一、能力

- 满额送赠品：门槛按订单商品金额判定，范围支持全场 / 类目 / 指定商品
- 赠品支持「固定赠品」与「多选一赠品池」，可设总库存，赠出实时扣减
- 后台可看赠出记录、按订单号或会员筛选，带预检器

## 二、数据表

| 表 | 用途 |
|---|---|
| `hou_plugin_order_gift_rule` | 规则：门槛金额、赠品方式、每单最多几种、优先级、时间窗 |
| `hou_plugin_order_gift_item` | 赠品池：商品/SKU、每单赠送数量、总库存与已赠出 |
| `hou_plugin_order_gift_record` | 赠出记录，`uk_order_item` 保证同单同赠品不重复登记 |

## 三、接口

| 端 | 路径 | 说明 |
|---|---|---|
| 后台 | `GET /admin/rules` | 规则 + 概览 |
| 后台 | `POST /admin/rules` | 保存规则（含赠品池整组覆盖） |
| 后台 | `POST /admin/rules/delete` | 删除规则 |
| 后台 | `GET /admin/records` | 赠出记录分页 |
| 后台 | `POST /admin/evaluate` | 预检（输入金额看能送什么） |
| C 端 | `POST /front/evaluate` | 结算页取可赠清单（amount / goodsIds / categoryIds） |
| C 端 | `POST /front/claim` | 下单成功后登记赠出（**幂等**：orderNo + itemId，重复调用返回 `alreadyClaimed=true`，不重复扣库存） |

## 四、边界与限制

**只做「规则 + 判定 + 登记」，不写订单表、不改订单金额。**

**已接 C 端（1.0.1）**：uniapp 结算页（`general/sureorder/sureorder.vue`）算价成功后调 `front/evaluate`，
命中就在合计区显示「满赠 / 满 X 元已达成」+ 可选赠品（多选一池可点选）；下单成功后自动调 `front/claim` 登记赠出。
赠品**不进订单明细行**，但会写进订单主表 `hou_order_base.giveaway_info`（形如「赠 定制马克杯 ×1」），
买家在订单详情、商家在发货单都能看到；商家仍按后台「满赠 → 赠出记录」核对发货。

赠品要真正进订单，需要买家在自己的下单流程里把赠品作为 0 元商品加入订单明细，并在下单成功后调
`/front/claim` 扣赠品库存。插件不替你做这件事 —— 因为赠品进订单会牵动发货、退款、库存扣减，
必须由下单系统统一处理。

## 五、验收记录

2026-10-29 生产验收：配「满 200 送定制马克杯（库存 100）」→ 预检 250 元命中，返回赠品 `定制马克杯 ×1 / remaining=100`。

## 六、版本记录

- **1.0.1**（2026-10-29）：`front/claim` 幂等化（重复登记返回 `alreadyClaimed=true` 而不是抛唯一键异常）；接 uniapp 结算页展示与登记。
- **1.0.0**：规则 + 赠品池 + 预检 + 赠出记录。

### 1.0.2 补充
- `front/claim` 登记赠出后，把赠品写回 `hou_order_base.giveaway_info`（按 `payment_number` 归组的多张子单一起写）；幂等命中时也补写，避免订单行晚于登记落库导致漏写。
