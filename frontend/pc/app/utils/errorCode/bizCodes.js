/**
 * pc 端业务码具名常量
 *
 * 解决的问题：下单/算价接口过去直接写 `response.code === 15030` 判断「商品无货」，
 * 裸数字散落在页面里，看不出语义、改不动、也无法随后端切换新码。
 *
 * ## 15030 的真身不是无货（本轮已核实）
 *
 * - `BizCodeEnume`：`EXCEEDED_MAXIMUM_DELIVERY_DISTANCE(15030, "超出最大配送距离")`
 * - `backend/` 下**零处**业务代码用 15030 表示无货，该码在全站只存在于枚举定义中
 * - 下单真正的无货信号是 `OrderCreateServiceImpl` 里 `ResponseResult.fail(goodsId)`：
 *   返回默认失败码、无货商品 ID 列表塞在 `data` 里，**从来没有定义过码值**
 *
 * 因此历史上 `=== 15030` 判无货是**从未命中的死分支**——用户下单遇到无货，
 * 看到的是一句通用失败提示，根本不知道是哪件商品没货。
 *
 * 现在收敛到统一新码 **40801 GOODS_OUT_OF_STOCK**（该码不认领任何旧码），
 * 码值从生成映射表按名称取，后端同步改为返回 40801 后前端即可真正命中。
 */
import { codeOfName } from './index.js'

export const GOODS_OUT_OF_STOCK = codeOfName('GOODS_OUT_OF_STOCK')
