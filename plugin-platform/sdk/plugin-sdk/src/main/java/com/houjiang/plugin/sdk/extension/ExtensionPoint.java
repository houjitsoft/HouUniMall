package com.houjiang.plugin.sdk.extension;

/**
 * 扩展点基接口。
 *
 * <p>这是插件与宿主之间唯一的运行期契约：插件实现宿主在 <code>{module}-api</code> 中定义的扩展点，
 * 宿主在业务代码中通过 <code>ExtensionInvoker</code> 回调。接口刻意只保留一个方法——
 * 插件开发者学会这一个方法，就能接入任意业务扩展点。
 *
 * <p>执行顺序由 {@link com.houjiang.plugin.sdk.annotation.HouExtension#priority()} 决定，
 * 不在接口中声明，避免每个实现都要重写 <code>order()</code>。
 *
 * <p>契约（由宿主 <code>ExtensionInvoker</code> 保证，插件无需自行处理）：
 * <ul>
 *   <li>插件抛出的异常与超时都会被<b>隔离</b>，不向主流程传播；是否中断主链路由该扩展点的
 *       <b>降级策略</b>决定（引擎策略表 {@code houcloud.plugin.degrade.{point}}）：
 *       {@code IGNORE}（记录并继续，默认）、{@code FALLBACK}（回落官方取值）、
 *       {@code ABORT}（fail-closed，锁定集如金额 / 库存 / 支付，故障时中断主链路）</li>
 *   <li>单个扩展点有执行超时（默认 200ms），超时按故障处理并被跳过</li>
 *   <li>扩展点运行在宿主的隔离线程上，因此<b>不得依赖调用线程绑定的事务</b>；需要写库请走宿主 API</li>
 *   <li><code>before*</code> 类扩展点只允许校验与中断，不改写核心入参</li>
 *   <li>无插件时调用为零开销，扩展点类不会被加载</li>
 * </ul>
 *
 * <p>示例：
 * <pre>{@code
 * @HouExtension(point = OrderCreateBeforeExtension.class, priority = 10)
 * public class CouponOrderExtension implements OrderCreateBeforeExtension {
 *     @Override
 *     public void execute(OrderCreateContext ctx) {
 *         if (ctx.getIn().getAmount() > limit) {
 *             ctx.abort("超过单笔限额");
 *         }
 *     }
 * }
 * }</pre>
 *
 * @param <C> 上下文类型
 * @author HouCloud
 */
public interface ExtensionPoint<C extends ExtensionContext> {

    /**
     * 执行扩展逻辑。
     *
     * @param context 上下文，可读入参；<code>after*</code> 类扩展点可写结果；可调用 {@link ExtensionContext#abort(String)} 中断
     */
    void execute(C context);
}
