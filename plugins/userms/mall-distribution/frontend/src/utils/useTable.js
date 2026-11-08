import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'

/**
 * 列表页取数：统一 loading / 列表 / 总数 / 分页，兼容 IPage 与裸数组两种返回。
 *
 * 商城内核接口返回 ResponseResult<IPage<T>>，经 BFF 直通与插件 callCoreApi 归一化后
 * 页面拿到的是 { code, message, data }，data 为 { records, total, ... }。
 * 老接口偶有直接返回数组的情况，这里一并兜住，避免每个页面各写一遍判空。
 *
 * <b>分页参数名不统一</b>：rebate 四个接口读 {@code pageNum/pageSize}，
 * distribution 与 points 读 {@code page/pageSize}。因此这里把分页键做成参数，
 * 由调用方按后端实际取值传入，不要想当然写成 page/size（否则永远停在第 1 页）。
 *
 * @param {(params: object) => Promise<object>} fetcher 取数函数
 * @param {object} options 配置
 * @param {object} options.defaultQuery 额外查询条件默认值
 * @param {string[]} options.pageKeys [页码键, 每页条数键]
 */
export function useTable(fetcher, options = {}) {
  const { defaultQuery = {}, pageKeys = ['pageNum', 'pageSize'] } = options
  const [pageKey, sizeKey] = pageKeys
  const loading = ref(false)
  const list = ref([])
  const total = ref(0)
  const query = reactive({ [pageKey]: 1, [sizeKey]: 20, ...defaultQuery })

  async function load() {
    loading.value = true
    try {
      const res = await fetcher({ ...query })
      const payload = res?.data ?? res ?? {}
      list.value = payload.records ?? payload.list ?? (Array.isArray(payload) ? payload : [])
      total.value = Number(payload.total ?? list.value.length ?? 0)
    } catch (error) {
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  function search() {
    query[pageKey] = 1
    return load()
  }

  function reset(extra = {}) {
    Object.assign(query, { [pageKey]: 1 }, extra)
    return load()
  }

  function onCurrentChange(value) {
    query[pageKey] = value
    return load()
  }

  function onSizeChange(value) {
    query[sizeKey] = value
    query[pageKey] = 1
    return load()
  }

  return { loading, list, total, query, pageKey, sizeKey, load, search, reset, onCurrentChange, onSizeChange }
}

/**
 * 触发一次操作并刷新列表：成功后提示，失败也提示（不静默）。
 *
 * @param {() => Promise<object>} action 操作
 * @param {() => Promise<void>} reload 刷新
 * @param {string} successText 成功提示
 */
export async function runAction(action, reload, successText = '操作成功') {
  try {
    const res = await action()
    const code = res?.code
    if (code === undefined || code === 1 || code === 200 || code === 0) {
      ElMessage.success(successText)
      await reload?.()
      return true
    }
    ElMessage.error(res?.message || '操作失败')
    return false
  } catch (error) {
    ElMessage.error(error?.message || '操作失败')
    return false
  }
}
