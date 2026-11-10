<template>
  <div class="shop-level-page">
    <el-alert type="info" :closable="false" show-icon class="level-tip">
      <template #title>这里是入驻页「店铺等级」下拉与「查看店铺权益和收费标准」的唯一数据源</template>
      <div class="tip-body">
        新增或修改后立即影响新入驻商家可选的等级、开店费用与商品 / 图片 / 视频上限。等级类型为「不展示」的等级不会出现在入驻页；
        ID=4 为品牌店铺（入驻页带 * 标记），按既有口径只允许调整开店费用；ID=1 基础版是店铺默认等级，不可删除。
      </div>
    </el-alert>

    <el-card shadow="never" class="level-card">
      <template #header>
        <div class="card-header">
          <span class="card-title">店铺等级设置</span>
          <div>
            <el-button :icon="Refresh" @click="loadList" :loading="loading">刷新</el-button>
            <el-button type="primary" :icon="Plus" @click="openCreate">新增等级</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="rows" stripe border size="small">
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="levelName" label="等级名称" min-width="120">
          <template #default="{ row }">
            <span>{{ row.levelName || row.storeLevel || '-' }}</span>
            <el-tag v-if="row.id === 4" size="small" type="warning" class="ml6">品牌店铺</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="storeLevel" label="等级编码" width="100" />
        <el-table-column label="等级类型" width="110">
          <template #default="{ row }">
            <el-tag size="small" :type="row.levelType === 3 ? 'info' : row.levelType === 2 ? 'warning' : 'success'">
              {{ levelTypeText(row.levelType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="开店费用 (元)" width="130">
          <template #default="{ row }">{{ formatMoney(row.expenses) }}</template>
        </el-table-column>
        <el-table-column label="商品 / 图片 / 视频" min-width="170">
          <template #default="{ row }">
            {{ limitText(row.goodsIsLimit, row.goodsCount) }} /
            {{ limitText(row.imageIsLimit, row.imageCount) }} /
            {{ limitText(row.videoIsLimit, row.videoCount) }}
          </template>
        </el-table-column>
        <el-table-column label="优先展示" width="110">
          <template #default="{ row }">
            {{ row.shopListFirstShow === 1 ? '店铺√' : '店铺×' }}
            {{ row.goodsListFirstShow === 1 ? '商品√' : '商品×' }}
          </template>
        </el-table-column>
        <el-table-column label="营销活动" min-width="150">
          <template #default="{ row }">{{ activityText(row.marketingActivitiesList) }}</template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="165" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="openDetail(row)">查看权益</el-button>
            <el-button link type="primary" size="small" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" size="small" :disabled="row.id === 1" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && rows.length === 0" description="暂无店铺等级，点击「新增等级」创建" :image-size="80" />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑店铺等级' : '新增店铺等级'" width="760px" destroy-on-close>
      <el-alert v-if="form.id === 4" type="warning" :closable="false" class="mb12" title="品牌店铺按既有口径只允许修改开店费用，其余字段保存后不生效" />
      <el-form ref="formRef" :model="form" :rules="rules" label-width="150px">
        <el-form-item label="等级名称" prop="levelName">
          <el-input v-model="form.levelName" placeholder="如：旗舰版" maxlength="30" />
        </el-form-item>
        <el-form-item label="等级编码">
          <el-input v-model="form.storeLevel" placeholder="如：2，用于展示与排序标识" maxlength="30" />
        </el-form-item>
        <el-form-item label="等级类型">
          <el-select v-model="form.levelType" style="width: 100%">
            <el-option :value="1" label="普通店铺（入驻页可选）" />
            <el-option :value="2" label="品牌店铺（入驻页可选）" />
            <el-option :value="3" label="不展示（入驻页不出现）" />
          </el-select>
        </el-form-item>
        <el-form-item label="开店费用 (元)" prop="expenses">
          <el-input-number v-model="form.expenses" :min="0" :precision="2" :step="100" style="width: 220px" />
          <span class="form-hint">入驻页「收费标准」展示该金额</span>
        </el-form-item>
        <el-form-item label="优先展示">
          <el-checkbox v-model="form.shopListFirstShow" :true-value="1" :false-value="0">店铺列表优先展示</el-checkbox>
          <el-checkbox v-model="form.goodsListFirstShow" :true-value="1" :false-value="0">商品列表优先展示</el-checkbox>
        </el-form-item>
        <el-form-item label="可用营销活动">
          <el-checkbox-group v-model="form.marketingActivitiesList">
            <el-checkbox v-for="opt in MARKETING_OPTIONS" :key="opt.value" :value="opt.value">{{ opt.label }}</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-divider content-position="left">资源上限</el-divider>
        <el-form-item label="可发布商品数">
          <el-checkbox v-model="form.goodsIsLimit" :true-value="1" :false-value="0">限制</el-checkbox>
          <el-input-number v-model="form.goodsCount" :min="0" :step="100" :disabled="form.goodsIsLimit !== 1" style="width: 180px" />
        </el-form-item>
        <el-form-item label="可上传图片数">
          <el-checkbox v-model="form.imageIsLimit" :true-value="1" :false-value="0">限制</el-checkbox>
          <el-input-number v-model="form.imageCount" :min="0" :step="100" :disabled="form.imageIsLimit !== 1" style="width: 180px" />
        </el-form-item>
        <el-form-item label="可上传视频数">
          <el-checkbox v-model="form.videoIsLimit" :true-value="1" :false-value="0">限制</el-checkbox>
          <el-input-number v-model="form.videoCount" :min="0" :step="10" :disabled="form.videoIsLimit !== 1" style="width: 180px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="店铺权益与收费标准" width="560px">
      <el-descriptions v-if="current" :column="1" border size="small">
        <el-descriptions-item label="等级名称">{{ current.levelName || current.storeLevel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="开店费用">{{ formatMoney(current.expenses) }} 元</el-descriptions-item>
        <el-descriptions-item label="店铺列表优先展示">{{ yesNo(current.shopListFirstShow) }}</el-descriptions-item>
        <el-descriptions-item label="商品列表优先展示">{{ yesNo(current.goodsListFirstShow) }}</el-descriptions-item>
        <el-descriptions-item label="营销活动">{{ activityText(current.marketingActivitiesList) }}</el-descriptions-item>
        <el-descriptions-item label="可发布商品数">{{ limitText(current.goodsIsLimit, current.goodsCount) }}</el-descriptions-item>
        <el-descriptions-item label="可上传图片数">{{ limitText(current.imageIsLimit, current.imageCount) }}</el-descriptions-item>
        <el-descriptions-item label="可上传视频数">{{ limitText(current.videoIsLimit, current.videoCount) }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button type="primary" @click="detailVisible = false">知道了</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { shopLevelList, shopLevelSave, shopLevelDelete } from '@/api/mall/storeLevel'

const MARKETING_OPTIONS = [
  { value: '1', label: '优惠券' },
  { value: '2', label: '限时折扣' },
  { value: '3', label: '限时秒杀' },
  { value: '4', label: '满减赠折' }
]

const rows = ref([])
const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const detailVisible = ref(false)
const current = ref(null)
const formRef = ref(null)

const blankForm = () => ({
  id: null,
  levelName: '',
  storeLevel: '',
  levelType: 1,
  expenses: 0,
  shopListFirstShow: 0,
  goodsListFirstShow: 0,
  marketingActivitiesList: [],
  goodsIsLimit: 0,
  goodsCount: 1000,
  imageIsLimit: 0,
  imageCount: 1000,
  videoIsLimit: 0,
  videoCount: 100
})
const form = reactive(blankForm())

const rules = {
  levelName: [{ required: true, message: '请输入等级名称', trigger: 'blur' }],
  expenses: [{ required: true, message: '请输入开店费用', trigger: 'change' }]
}

function isOk(res) {
  return res && (res.code === 1 || res.code === 200)
}

function levelTypeText(type) {
  if (type === 2) return '品牌店铺'
  if (type === 3) return '不展示'
  return '普通店铺'
}

function formatMoney(v) {
  const n = Number(v)
  return Number.isFinite(n) ? n.toFixed(2) : '0.00'
}

function limitText(limited, count) {
  return Number(limited) === 1 ? `${count ?? 0}` : '不限'
}

function yesNo(v) {
  return Number(v) === 1 ? '是' : '否'
}

function activityText(list) {
  if (!Array.isArray(list) || list.length === 0) return '无'
  const labels = MARKETING_OPTIONS.filter((o) => list.map(String).includes(o.value)).map((o) => o.label)
  return labels.length ? labels.join('、') : '无'
}

async function loadList() {
  loading.value = true
  try {
    const res = await shopLevelList()
    if (!isOk(res)) {
      rows.value = []
      return
    }
    rows.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    // 请求拦截器已统一提示；这里保证页面不卡在 loading，也不吞掉错误对象
    rows.value = []
  } finally {
    loading.value = false
  }
}

function fillForm(row) {
  Object.assign(form, blankForm(), {
    id: row?.id ?? null,
    levelName: row?.levelName ?? '',
    storeLevel: row?.storeLevel ?? '',
    levelType: row?.levelType ?? 1,
    expenses: Number(row?.expenses ?? 0),
    shopListFirstShow: Number(row?.shopListFirstShow ?? 0),
    goodsListFirstShow: Number(row?.goodsListFirstShow ?? 0),
    marketingActivitiesList: Array.isArray(row?.marketingActivitiesList)
      ? row.marketingActivitiesList.map(String)
      : [],
    goodsIsLimit: Number(row?.goodsIsLimit ?? 0),
    goodsCount: Number(row?.goodsCount ?? 0),
    imageIsLimit: Number(row?.imageIsLimit ?? 0),
    imageCount: Number(row?.imageCount ?? 0),
    videoIsLimit: Number(row?.videoIsLimit ?? 0),
    videoCount: Number(row?.videoCount ?? 0)
  })
}

function openCreate() {
  fillForm(null)
  dialogVisible.value = true
}

function openEdit(row) {
  fillForm(row)
  dialogVisible.value = true
}

function openDetail(row) {
  current.value = row
  detailVisible.value = true
}

async function handleSave() {
  try {
    await formRef.value?.validate()
  } catch (e) {
    return
  }
  saving.value = true
  try {
    const res = await shopLevelSave({
      ...form,
      expenses: Number(form.expenses) || 0,
      goodsCount: Number(form.goodsCount) || 0,
      imageCount: Number(form.imageCount) || 0,
      videoCount: Number(form.videoCount) || 0,
      marketingActivitiesList: [...form.marketingActivitiesList]
    })
    if (!isOk(res)) return
    ElMessage.success(form.id ? '保存成功' : '新增成功')
    dialogVisible.value = false
    await loadList()
  } catch (e) {
    // 失败原因由请求拦截器统一提示，表单保留供用户修正
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确定删除等级「${row.levelName || row.storeLevel || row.id}」吗？删除后该等级不再出现在入驻页。`,
      '删除确认',
      { type: 'warning' }
    )
  } catch (e) {
    return
  }
  try {
    const res = await shopLevelDelete({ id: row.id })
    if (!isOk(res)) return
    ElMessage.success('已删除')
    await loadList()
  } catch (e) {
    // 后端「已被店铺使用」等失败原因由请求拦截器统一提示
  }
}

onMounted(loadList)
</script>

<style scoped>
.shop-level-page {
  padding: 4px;
}
.level-tip {
  margin-bottom: 12px;
}
.tip-body {
  line-height: 1.7;
  font-size: 12px;
}
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.card-title {
  font-weight: 600;
}
.level-card :deep(.el-card__body) {
  padding-top: 8px;
}
.ml6 {
  margin-left: 6px;
}
.mb12 {
  margin-bottom: 12px;
}
.form-hint {
  margin-left: 10px;
  color: #909399;
  font-size: 12px;
}
</style>
