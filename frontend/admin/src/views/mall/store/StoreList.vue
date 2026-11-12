<template>
  <div class="store-list-page">
    <nav class="store-tabs" aria-label="店铺管理页签">
      <button class="store-tab is-active" type="button" aria-current="page">店铺设置</button>
      <button v-for="tab in pendingTabs" :key="tab" class="store-tab" type="button" disabled :title="`${tab}待补充参考截图和交互规则`">{{ tab }}</button>
    </nav>

    <div class="store-list-content">
      <el-form class="store-search" :model="queryParams" label-width="70px" @submit.prevent="handleSearch">
        <div class="store-search-fields">
          <el-form-item label="店主账号：">
            <el-input v-model="queryParams.ownerAccount" placeholder="请输入店主账号" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="店铺名称：">
            <el-input v-model="queryParams.name" placeholder="请输入店铺名称" clearable @keyup.enter="handleSearch" />
          </el-form-item>
          <el-form-item label="店铺类型：">
            <el-select v-model="queryParams.entryType" placeholder="请选择" clearable>
              <el-option label="个人入驻" :value="1" />
              <el-option label="企业入驻" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item label="店铺状态：">
            <el-select v-model="queryParams.shop_status" placeholder="全部">
              <el-option label="全部" value="" />
              <el-option label="正常" :value="1" />
              <el-option label="冻结" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item label="店铺等级：">
            <el-select v-model="queryParams.shopLevelId" placeholder="请选择" clearable :loading="levelLoading">
              <el-option v-for="level in levels" :key="level.id" :label="level.storeLevel || level.levelName" :value="level.id" />
            </el-select>
          </el-form-item>
        </div>
        <div class="store-search-actions">
          <el-button type="primary" size="small" native-type="submit" :loading="loading">查询</el-button>
          <el-button type="primary" size="small" @click="handleReset">重置</el-button>
        </div>
      </el-form>

      <div class="store-toolbar">
        <el-button type="primary" link :icon="Plus" @click="handleAdd">创建店铺</el-button>
        <el-button type="primary" link :icon="Download" :loading="exporting" @click="handleExport">导出</el-button>
        <el-button type="primary" link :icon="Refresh" :loading="loading" @click="fetchList">刷新</el-button>
      </div>
      <el-alert v-if="listError" class="store-list-error" type="error" :title="listError" :closable="false" show-icon />
      <el-table v-loading="loading" :data="list" class="store-table" empty-text="暂无店铺数据" :row-style="{ height: '76px' }" :header-row-style="{ height: '44px' }">
        <el-table-column label="操作" width="160" fixed="left" align="center">
          <template #default="{ row }">
            <div class="store-row-actions">
              <el-button type="primary" link @click="handleView(row)">查看</el-button>
              <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button type="primary" link @click="handleEdit(row)">修改</el-button>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="店铺id" prop="id" min-width="80" align="center" />
        <el-table-column label="店主账号" min-width="140" align="center">
          <template #default="{ row }">{{ maskStoreAccount(row.account?.loginAccount) }}</template>
        </el-table-column>
        <el-table-column label="店铺名称" prop="name" min-width="140" align="center" show-overflow-tooltip />
        <el-table-column label="店铺logo" min-width="110" align="center">
          <template #default="{ row }">
            <el-image v-if="row.logo" :src="row.logo" fit="contain" class="store-logo">
              <template #error><span class="store-empty-value">—</span></template>
            </el-image>
            <span v-else class="store-empty-value">—</span>
          </template>
        </el-table-column>
        <el-table-column label="店铺类型" prop="entry_type_text" min-width="120" align="center" />
        <el-table-column label="店铺等级" prop="level_name" min-width="120" align="center" />
        <el-table-column label="商家电话" min-width="120" align="center">
          <template #default="{ row }">{{ row.mobile || '—' }}</template>
        </el-table-column>
        <el-table-column label="开店时间" min-width="120" align="center">
          <template #default="{ row }">{{ formatStoreDate(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="店铺曾用名" min-width="140" align="center" show-overflow-tooltip>
          <template #default="{ row }">{{ row.previousName || '—' }}</template>
        </el-table-column>
        <el-table-column label="审核状态" min-width="130" align="center">
          <template #default="{ row }">
            <span class="store-audit-state" :class="`is-${storeAuditState(row.auditStatus).tone}`">
              <i aria-hidden="true" />{{ storeAuditState(row.auditStatus).label }}
            </span>
          </template>
        </el-table-column>
      </el-table>
      <div class="store-pagination">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 15, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSearch"
          @current-change="fetchList"
        />
      </div>
    </div>

    <el-dialog
      v-model="createVisible"
      title="新增店铺"
      width="680px"
      destroy-on-close
      @open="loadAccounts"
      @closed="resetCreateForm"
    >
      <el-alert
        class="mb-5"
        type="info"
        :closable="false"
        title="店铺资料与店铺管理主账号将一次性创建"
        description="请妥善告知店铺登录账号与初始密码；系统不会在创建后显示密码。"
        show-icon
      />
      <el-form
        ref="createFormRef"
        :model="createForm"
        :rules="createRules"
        label-width="100px"
        status-icon
      >
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12">
            <el-form-item label="店铺名称" prop="name">
              <el-input v-model="createForm.name" maxlength="100" placeholder="请输入店铺名称" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="店铺类型" prop="type">
              <el-select v-model="createForm.type" class="w-full">
                <el-option label="商家入驻" :value="1" />
                <el-option label="官方自营" :value="0" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="入驻类型" prop="entryType">
              <el-select v-model="createForm.entryType" class="w-full" clearable placeholder="请选择">
                <el-option label="个人入驻" :value="1" />
                <el-option label="企业入驻" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="店铺等级" prop="shopLevelId">
              <el-select v-model="createForm.shopLevelId" class="w-full" clearable placeholder="请选择" :loading="levelLoading">
                <el-option v-for="level in levels" :key="level.id" :label="level.storeLevel || level.levelName" :value="level.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="主营类目" prop="shopCategoryId">
              <el-select v-model="createForm.shopCategoryId" class="w-full" clearable :loading="shopCategoryLoading" placeholder="可选">
                <el-option
                  v-for="item in shopCategoryList"
                  :key="item.id"
                  :label="item.name"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="联系人" prop="contact">
              <el-input v-model="createForm.contact" maxlength="30" placeholder="请输入联系人" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="联系手机" prop="mobile">
              <el-input v-model="createForm.mobile" maxlength="11" placeholder="请输入11位手机号" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item label="店铺状态" prop="shopStatus">
              <el-radio-group v-model="createForm.shopStatus">
                <el-radio :value="1">正常</el-radio>
                <el-radio :value="0">冻结</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="详细地址" prop="addressInfo">
              <el-input v-model="createForm.addressInfo" maxlength="255" placeholder="选填">
                <template #append>
                  <el-button @click="openMapPicker">地图选点</el-button>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="账号来源" prop="accountMode">
              <el-radio-group v-model="createForm.accountMode" @change="createFormRef?.clearValidate(['platformUserId', 'accountIdentifier', 'accountPassword', 'accountPasswordAgain'])">
                <el-radio value="existing">绑定已有账号</el-radio>
                <el-radio value="new">手机号或邮箱新增</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col v-if="createForm.accountMode === 'existing'" :span="24">
            <el-form-item label="已有账号" prop="platformUserId">
              <el-select v-model="createForm.platformUserId" filterable remote clearable class="w-full" placeholder="搜索手机号或邮箱" :remote-method="searchAccounts" :loading="accountLoading">
                <el-option v-for="item in accounts" :key="item.userId" :label="`${item.displayName} (${item.loginAccount})`" :value="item.userId">
                  <div class="account-option">
                    <span>{{ item.displayName }}</span>
                    <span class="minor">{{ item.loginAccount }}<template v-if="item.grantedMerchantName"> · 已绑定：{{ item.grantedMerchantName }}</template></span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <template v-else>
            <el-col :xs="24" :sm="12">
              <el-form-item label="手机号或邮箱" prop="accountIdentifier">
                <el-input v-model="createForm.accountIdentifier" autocomplete="off" placeholder="注册后可用于登录" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="登录密码" prop="accountPassword">
                <el-input v-model="createForm.accountPassword" type="password" maxlength="32" autocomplete="new-password" show-password placeholder="6至32个字符" />
              </el-form-item>
            </el-col>
            <el-col :xs="24" :sm="12">
              <el-form-item label="确认密码" prop="accountPasswordAgain">
                <el-input v-model="createForm.accountPasswordAgain" type="password" maxlength="32" autocomplete="new-password" show-password placeholder="请再次输入密码" />
              </el-form-item>
            </el-col>
          </template>
          <el-col :xs="24" :sm="12">
            <el-form-item label="营业状态" prop="businessStatus">
              <el-radio-group v-model="createForm.businessStatus">
                <el-radio :value="1">营业中</el-radio>
                <el-radio :value="0">暂停营业</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">确认创建</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mapVisible" title="地图选点" width="760px" destroy-on-close @open="initMapPicker" @closed="destroyMapPicker">
      <div class="map-picker-toolbar">
        <el-input v-model="mapKeyword" clearable placeholder="输入地址后搜索，或直接在地图上点击" @keyup.enter="searchMapAddress" />
        <el-button type="primary" :loading="mapSearching" @click="searchMapAddress">搜索</el-button>
      </div>
      <div ref="mapContainer" class="map-picker-container">
        <el-empty v-if="mapError" :description="mapError" />
      </div>
      <div class="map-picker-meta">
        <span v-if="pickedLocation">已选位置：{{ pickedLocation.address || '地图位置' }}</span>
        <span v-if="pickedLocation">经度 {{ pickedLocation.lng.toFixed(6) }}，纬度 {{ pickedLocation.lat.toFixed(6) }}</span>
        <span v-else>点击地图选择店铺位置</span>
      </div>
      <template #footer>
        <el-button @click="mapVisible = false">取消</el-button>
        <el-button type="primary" :disabled="!pickedLocation" @click="applyMapLocation">使用此位置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { Download, Plus, Refresh } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { mallShopLevelOptions } from '@/api/mall/shopAdmin'
import {
  mallShopLists,
  mallShopProvisionAccountOptions,
  mallShopProvisionCreate,
  mallShopProvisionOptions
} from '@/api/mall/shopAdmin'
import {
  buildCreateStorePayload,
  buildStoreListParams,
  buildStoreExportCsv,
  formatStoreDate,
  maskStoreAccount,
  normalizeStorePage,
  storeAuditState
} from './storeListModel'

const router = useRouter()
const pendingTabs = ['审核店铺名', '审核资质信息', '结算管理', '审核经营类目']
const levels = ref([])
const levelLoading = ref(false)
const exporting = ref(false)
const listError = ref('')
let listRequestId = 0

/** 加载状态 */
const loading = ref(false)
/** 列表数据 */
const list = ref([])
/** 经营类目列表 */
const shopCategoryList = ref([])
const shopCategoryLoading = ref(false)
const accounts = ref([])
const accountLoading = ref(false)
const createVisible = ref(false)
const creating = ref(false)
const createFormRef = ref()
const mapVisible = ref(false)
const mapContainer = ref()
const mapKeyword = ref('')
const mapSearching = ref(false)
const mapError = ref('')
const pickedLocation = ref(null)
let tencentMap = null
let tencentMarkerLayer = null
let tencentMapClickHandler = null
const TENCENT_MAP_KEY = 'A34BZ-FT5K6-DTPSC-E6RVP-JOHHV-WQB27'

const createDefaults = () => ({
  name: '',
  type: 1,
  shopCategoryId: null,
  entryType: null,
  shopLevelId: null,
  contact: '',
  mobile: '',
  addressInfo: '',
  lat: '',
  lng: '',
  shopStatus: 1,
  businessStatus: 1,
  accountMode: 'existing',
  platformUserId: '',
  accountIdentifier: '',
  accountPassword: '',
  accountPasswordAgain: ''
})
const createForm = reactive(createDefaults())
const validatePasswordAgain = (_rule, value, callback) => {
  if (value !== createForm.accountPassword) callback(new Error('两次输入的密码不一致'))
  else callback()
}

/** 查询参数 */
const createRules = {
  name: [{ required: true, message: '请输入店铺名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择店铺类型', trigger: 'change' }],
  contact: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  mobile: [
    { required: true, message: '请输入联系手机', trigger: 'blur' },
    { pattern: /^1\d{10}$/, message: '请输入正确的11位手机号', trigger: 'blur' }
  ],
  accountMode: [{ required: true, message: '请选择账号来源', trigger: 'change' }],
  platformUserId: [{ required: true, message: '请选择已有账号', trigger: 'change' }],
  accountIdentifier: [
    { required: true, message: '请输入手机号或邮箱', trigger: 'blur' },
    { pattern: /^(1\d{10}|[^\s@]+@[^\s@]+\.[^\s@]+)$/, message: '请输入正确的手机号或邮箱', trigger: 'blur' }
  ],
  accountPassword: [
    { required: true, min: 6, max: 32, message: '初始密码长度须为6至32个字符', trigger: 'blur' }
  ],
  accountPasswordAgain: [
    { required: true, message: '请确认初始密码', trigger: 'blur' },
    { validator: validatePasswordAgain, trigger: 'blur' }
  ]
}

const queryDefaults = () => ({ ownerAccount: '', name: '', entryType: '', shop_status: '', shopLevelId: '' })
const queryParams = reactive(queryDefaults())

/** 分页信息 */
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })

/**
 * 获取店铺列表数据
 */
const fetchList = async () => {
  const requestId = ++listRequestId
  loading.value = true
  listError.value = ''
  try {
    const res = await mallShopLists(buildStoreListParams(queryParams, pagination))
    if (requestId !== listRequestId) return
    if (res?.code !== 1 || !res.data) throw new Error(res?.message || res?.msg || '店铺列表加载失败')
    const page = normalizeStorePage(res.data)
    list.value = page.list
    pagination.total = page.total
  } catch (error) {
    if (requestId !== listRequestId) return
    list.value = []
    pagination.total = 0
    listError.value = error?.message || '店铺列表加载失败，请刷新重试'
  } finally {
    if (requestId === listRequestId) loading.value = false
  }
}

const fetchLevels = async () => {
  levelLoading.value = true
  try {
    const res = await mallShopLevelOptions()
    if (res?.code !== 1 || !Array.isArray(res.data)) throw new Error(res?.message || '店铺等级加载失败')
    levels.value = res.data
  } catch (error) {
    ElMessage.error(error?.message || '店铺等级加载失败')
  } finally {
    levelLoading.value = false
  }
}

// 与当前筛选保持一致，逐页导出，绝不把当前页冒充全部结果。
const handleExport = async () => {
  if (exporting.value) return
  exporting.value = true
  const filters = { ...queryParams }
  try {
    const rows = []
    let total = null
    for (let pageNo = 1; ; pageNo++) {
      const res = await mallShopLists(buildStoreListParams(filters, { pageNo, pageSize: 100 }))
      if (res?.code !== 1 || !res.data) throw new Error(res?.message || res?.msg || '导出失败')
      const page = normalizeStorePage(res.data)
      if (total === null) total = page.total
      if (page.total !== total) throw new Error('导出期间店铺数量发生变化，请重试')
      rows.push(...page.list)
      if (rows.length >= total) break
      if (!page.list.length || pageNo >= 100) throw new Error('结果过多或分页数据不完整，请缩小筛选范围后导出')
    }
    if (!rows.length) { ElMessage.info('当前筛选条件下没有可导出的店铺'); return }
    const url = URL.createObjectURL(new Blob([buildStoreExportCsv(rows)], { type: 'text/csv;charset=utf-8;' }))
    const anchor = document.createElement('a')
    anchor.href = url
    anchor.download = `店铺列表-${new Date().toISOString().slice(0, 10)}.csv`
    document.body.appendChild(anchor)
    anchor.click()
    anchor.remove()
    setTimeout(() => URL.revokeObjectURL(url), 1000)
    ElMessage.success(`已导出 ${rows.length} 家店铺`)
  } catch (error) {
    ElMessage.error(error?.message || '导出失败，请重试')
  } finally {
    exporting.value = false
  }
}

/**
 * 获取经营类目列表（用于搜索下拉）
 */
const fetchShopCategoryList = async () => {
  shopCategoryLoading.value = true
  try {
    const res = await mallShopProvisionOptions()
    if (res && res.code === 1 && res.data) {
      const rows = Array.isArray(res.data) ? res.data : (res.data.records || res.data.lists || [])
      shopCategoryList.value = rows
        .map((item) => ({ id: item.id ?? item.categoryId, name: item.name ?? item.categoryName }))
        .filter((item) => item.id !== undefined && item.id !== null && item.name)
    }
  } catch (e) {
    console.error('获取类目失败', e)
  } finally {
    shopCategoryLoading.value = false
  }
}

/**
 * 查询（重置到第一页）
 */
const handleSearch = () => {
  pagination.pageNo = 1
  fetchList()
}

/**
 * 重置查询参数
 */
const handleReset = () => {
  Object.assign(queryParams, queryDefaults())
  handleSearch()
}

/**
 * 新增店铺
 */
const handleAdd = () => {
  if (!shopCategoryList.value.length) fetchShopCategoryList()
  createVisible.value = true
}

const loadTencentMapScript = () => {
  if (window.TMap) return Promise.resolve(window.TMap)
  const scriptId = 'houcloud-tencent-map-gl'
  const existing = document.getElementById(scriptId)
  if (existing) {
    return new Promise((resolve, reject) => {
      existing.addEventListener('load', () => resolve(window.TMap), { once: true })
      existing.addEventListener('error', reject, { once: true })
    })
  }
  return new Promise((resolve, reject) => {
    const script = document.createElement('script')
    script.id = scriptId
    script.src = `https://map.qq.com/api/gljs?v=1.exp&key=${TENCENT_MAP_KEY}`
    script.onload = () => resolve(window.TMap)
    script.onerror = reject
    document.head.appendChild(script)
  })
}

const setMapMarker = (lat, lng, address = '') => {
  if (!window.TMap || !tencentMarkerLayer) return
  const position = new window.TMap.LatLng(lat, lng)
  tencentMarkerLayer.updateGeometries([{ id: 'selected', styleId: 'selected', position }])
  tencentMap?.setCenter(position)
  pickedLocation.value = { lat, lng, address }
}

const reverseGeocode = async (lat, lng) => {
  try {
    const query = new URLSearchParams({ location: `${lat},${lng}`, key: TENCENT_MAP_KEY, get_poi: '1' })
    const res = await fetch(`https://apis.map.qq.com/ws/geocoder/v1/?${query.toString()}`)
    const data = await res.json()
    return data?.status === 0 ? (data.result?.formatted_addresses?.recommend || data.result?.address || '') : ''
  } catch {
    return ''
  }
}

const initMapPicker = async () => {
  mapError.value = ''
  pickedLocation.value = createForm.lat && createForm.lng
    ? { lat: Number(createForm.lat), lng: Number(createForm.lng), address: createForm.addressInfo }
    : null
  mapKeyword.value = createForm.addressInfo || ''
  await nextTick()
  try {
    const TMap = await loadTencentMapScript()
    if (!mapContainer.value || !TMap) throw new Error('地图加载失败，请稍后重试')
    const initial = pickedLocation.value || { lat: 31.2304, lng: 121.4737 }
    tencentMap = new TMap.Map(mapContainer.value, { center: new TMap.LatLng(initial.lat, initial.lng), zoom: 13 })
    tencentMarkerLayer = new TMap.MultiMarker({
      map: tencentMap,
      styles: {
        selected: new TMap.MarkerStyle({
          width: 28,
          height: 36,
          src: 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="28" height="36" viewBox="0 0 28 36"%3E%3Cpath fill="%23f56c6c" stroke="white" stroke-width="2" d="M14 1C6.8 1 1 6.7 1 13.7 1 23.1 14 35 14 35s13-11.9 13-21.3C27 6.7 21.2 1 14 1z"/%3E%3Ccircle cx="14" cy="14" r="5" fill="white"/%3E%3C/svg%3E',
          anchor: { x: 14, y: 36 }
        })
      },
      geometries: pickedLocation.value ? [{ id: 'selected', styleId: 'selected', position: new TMap.LatLng(initial.lat, initial.lng) }] : []
    })
    tencentMapClickHandler = async (event) => {
      const lat = Number(event.latLng.lat)
      const lng = Number(event.latLng.lng)
      const address = await reverseGeocode(lat, lng)
      setMapMarker(lat, lng, address)
    }
    tencentMap.on('click', tencentMapClickHandler)
  } catch (error) {
    mapError.value = error?.message || '地图加载失败，请稍后重试'
  }
}

const searchMapAddress = async () => {
  const keyword = mapKeyword.value.trim()
  if (!keyword) return
  mapSearching.value = true
  try {
    const query = new URLSearchParams({ keyword, boundary: 'region(全国,0)', key: TENCENT_MAP_KEY, page_size: '10' })
    const res = await fetch(`https://apis.map.qq.com/ws/place/v1/search?${query.toString()}`)
    const data = await res.json()
    const first = data?.status === 0 ? data.data?.[0] : null
    if (!first?.location) throw new Error('没有找到相关地址，请直接点击地图选择')
    setMapMarker(Number(first.location.lat), Number(first.location.lng), first.address || first.title || keyword)
  } catch (error) {
    ElMessage.warning(error?.message || '地址搜索失败，请直接点击地图选择')
  } finally {
    mapSearching.value = false
  }
}

const applyMapLocation = () => {
  if (!pickedLocation.value) return
  createForm.lat = String(pickedLocation.value.lat)
  createForm.lng = String(pickedLocation.value.lng)
  if (pickedLocation.value.address) createForm.addressInfo = pickedLocation.value.address
  mapVisible.value = false
}

const openMapPicker = () => {
  mapVisible.value = true
}

const destroyMapPicker = () => {
  if (tencentMap && tencentMapClickHandler) tencentMap.off('click', tencentMapClickHandler)
  tencentMap?.destroy?.()
  tencentMap = null
  tencentMarkerLayer = null
  tencentMapClickHandler = null
}

const loadAccounts = async (keyword = '') => {
  accountLoading.value = true
  try {
    const res = await mallShopProvisionAccountOptions({ keyword })
    accounts.value = res?.code === 1 && Array.isArray(res.data) ? res.data : []
  } finally {
    accountLoading.value = false
  }
}

const searchAccounts = (keyword) => {
  loadAccounts(keyword)
}

const resetCreateForm = () => {
  Object.assign(createForm, createDefaults())
  createFormRef.value?.clearValidate()
}

const submitCreate = async () => {
  if (!createFormRef.value) return
  await createFormRef.value.validate()
  creating.value = true
  try {
    const res = await mallShopProvisionCreate(buildCreateStorePayload(createForm))
    if (res?.code === 1) {
      ElMessage.success('店铺和管理账号创建成功')
      createVisible.value = false
      pagination.pageNo = 1
      await fetchList()
    } else {
      ElMessage.error(res?.msg || '创建失败，请稍后重试')
    }
  } finally {
    creating.value = false
  }
}

/**
 * 编辑店铺
 * @param {object} row - 当前店铺行
 */
const handleEdit = (row) => {
  router.push({ path: '/mall/store/edit', query: { id: row.id } })
}

const handleView = (row) => {
  router.push({ path: '/mall/store/edit', query: { id: row.id, mode: 'view' } })
}

onMounted(() => {
  fetchList()
  fetchLevels()
})

onBeforeUnmount(destroyMapPicker)
</script>

<style scoped>
.store-list-page {
  --store-blue: #1684ff;
  background: #fff;
  border: 1px solid #dcdfe6;
  min-height: 640px;
  color: #303133;
  font-size: 14px;
}

.store-tabs {
  display: flex;
  overflow-x: auto;
  background: #f5f7fa;
  border-bottom: 1px solid #dce3ef;
  min-height: 40px;
}

.store-tab {
  flex: none;
  padding: 10px 21px;
  border: 0;
  border-right: 1px solid transparent;
  background: transparent;
  color: #909399;
  font-size: 14px;
  font-family: inherit;
  line-height: 18px;
}

.store-tab:disabled { cursor: not-allowed; }
.store-tab.is-active {
  background: #fff;
  color: var(--store-blue);
  border-right-color: #dce3ef;
}

.store-list-content { padding: 35px 35px 24px; }
.store-search { padding: 23px 15px; background: #ecf5ff; border-radius: 11px; }
.store-search-fields { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); column-gap: 44px; row-gap: 10px; }
.store-search :deep(.el-form-item) { margin: 0; align-items: center; }
.store-search :deep(.el-form-item__label) { padding-right: 0; color: #111; line-height: 28px; font-size: 14px; }
.store-search :deep(.el-input__wrapper), .store-search :deep(.el-select__wrapper) { min-height: 28px; height: 28px; border-radius: 4px; }
.store-search :deep(.el-input__inner) { height: 26px; font-size: 12px; }
.store-search :deep(.el-select) { width: 100%; font-size: 12px; }
.store-search-actions { display: flex; margin-top: 20px; gap: 15px; }
.store-search-actions :deep(.el-button) { margin: 0; min-height: 26px; padding: 5px 12px; border-radius: 2px; background: #137be0; border-color: #137be0; }
.store-toolbar { display: flex; justify-content: flex-end; gap: 28px; padding: 17px 10px 15px; }
.store-toolbar :deep(.el-button) { margin: 0; color: var(--store-blue); font-size: 14px; }
.store-list-error { margin-bottom: 12px; }
.store-table { --el-table-header-bg-color: #f7f8fa; --el-table-header-text-color: #111; --el-table-border-color: #e8edf6; --el-table-text-color: #444; }
.store-table :deep(.el-table__header th) { font-size: 14px; font-weight: 600; }
.store-table :deep(.cell) { line-height: 23px; padding: 0 10px; }
.store-row-actions { display: flex; justify-content: center; gap: 14px; }
.store-row-actions :deep(.el-button) { margin: 0; color: var(--store-blue); font-size: 14px; }
.store-logo { display: inline-flex; width: 62px; height: 52px; vertical-align: middle; background: #fff; }
.store-empty-value { color: #909399; }
.store-audit-state { display: inline-flex; gap: 5px; align-items: center; white-space: nowrap; }
.store-audit-state i { width: 6px; height: 6px; border-radius: 50%; background: #909399; }
.store-audit-state.is-success i { background: #67c23a; }
.store-audit-state.is-warning i { background: #e6a23c; }
.store-audit-state.is-danger i { background: #f56c6c; }
.store-pagination { display: flex; justify-content: flex-end; overflow-x: auto; margin-top: 20px; }
@media (max-width: 900px) { .store-search-fields { grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 12px 30px; } }
@media (max-width: 640px) { .store-list-content { padding: 20px 12px; } .store-search-fields { grid-template-columns: minmax(0, 1fr); } .store-toolbar { gap: 18px; } }

.map-picker-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.map-picker-container {
  position: relative;
  height: 420px;
  overflow: hidden;
  border: 1px solid var(--el-border-color);
  border-radius: 4px;
}

.map-picker-container :deep(.el-empty) {
  height: 100%;
}

.map-picker-meta {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-top: 10px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}
</style>
