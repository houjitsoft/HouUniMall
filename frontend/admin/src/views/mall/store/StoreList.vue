<template>
  <div class="goods-lists">
    <el-card class="!border-none" shadow="never">
      <el-alert
        type="info"
        title="新增店铺会同步开通店铺管理主账号，创建后可直接登录店铺管理端"
        :closable="false"
        show-icon
      />

      <el-form ref="formRef" class="mb-[-16px] mt-[16px]" :model="queryParams" :inline="true">
        <el-form-item label="店铺名称">
          <el-input
            class="w-[280px]"
            v-model="queryParams.name"
            placeholder="请输入店铺名称"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="店铺类型">
          <el-select class="w-[280px]" v-model="queryParams.type" placeholder="选择">
            <el-option label="全部" value="" />
            <el-option label="官方自营" value="1" />
            <el-option label="商家入驻" value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="主营类目">
          <el-select class="w-[320px]" v-model="queryParams.shop_category_id">
            <el-option label="全部" value="" />
            <el-option
              v-for="item in shopCategoryList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="营业状态">
          <el-select class="w-[280px]" v-model="queryParams.business_status" placeholder="选择">
            <el-option label="全部" value="" />
            <el-option label="营业中" value="1" />
            <el-option label="暂停营业" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="店铺状态">
          <el-select class="w-[280px]" v-model="queryParams.shop_status" placeholder="选择">
            <el-option label="全部" value="" />
            <el-option label="正常" value="1" />
            <el-option label="冻结" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="!border-none mt-4" :body-style="{ 'padding-top': '10px' }" shadow="never">
      <!-- 新增店铺 -->
      <el-button type="primary" class="mt-2" @click="handleAdd">+ 新增店铺</el-button>

      <!-- 表格 -->
      <el-table size="large" v-loading="loading" :data="list" class="mt-4">
        <el-table-column type="selection" width="55" />
        <el-table-column label="ID" prop="id" width="55" show-overflow-tooltip />
        <el-table-column label="店铺名称" min-width="200">
          <template #default="{ row }">
            <div class="store-name-cell">
              <el-image
                v-if="row.logo"
                fit="cover"
                :src="row.logo"
                class="store-logo flex-none"
                @error="row.logo = ''"
              />
              <div v-else class="store-logo store-logo-placeholder flex-none">
                {{ row.name?.trim().charAt(0) || '商' }}
              </div>
              <div class="store-name-cell__text">
                <el-tooltip effect="dark" :content="row.name" placement="top">
                  <div class="store-name-cell__name">{{ row.name }}</div>
                </el-tooltip>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="统一账号" prop="account.loginAccount" min-width="120" />
        <el-table-column label="主营类目" prop="shop_category.name" min-width="90" />
        <el-table-column label="店铺类型" prop="type_text" min-width="90" />
        <el-table-column label="营业状态" min-width="90">
          <template #default="{ row }">
            <el-tag v-if="row?.business_status == 1" type="success">营业中</el-tag>
            <el-tag type="info" v-else>暂停营业</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="店铺状态" min-width="90">
          <template #default="{ row }">
            <el-switch
              v-model="row.shop_status"
              :active-value="1"
              :inactive-value="0"
              @change="handleChangeStatus($event, row.id)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="primary" link @click="handleAccount(row)">账号管理</el-button>
            <el-button type="danger" link @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="flex justify-end mt-4">
        <el-pagination
          v-model:current-page="pagination.pageNo"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 15, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchList"
          @current-change="fetchList"
        />
      </div>
    </el-card>

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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import request from '@/utils/request'
import {
  mallShopLists,
  mallShopProvisionAccountOptions,
  mallShopProvisionCreate,
  mallShopProvisionOptions,
  mallShopStatus
} from '@/api/mall/shopAdmin'
import {
  buildCreateStorePayload,
  buildStoreListParams,
  normalizeStorePage
} from './storeListModel'

const router = useRouter()

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

const queryParams = reactive({
  name: '',
  type: '',
  shop_category_id: '',
  business_status: '',
  shop_status: ''
})

/** 分页信息 */
const pagination = reactive({ pageNo: 1, pageSize: 15, total: 0 })

/**
 * 获取店铺列表数据
 */
const fetchList = async () => {
  loading.value = true
  try {
    const res = await mallShopLists(buildStoreListParams(queryParams, pagination))
    if (res && res.code === 1 && res.data) {
      const page = normalizeStorePage(res.data)
      list.value = page.list
      pagination.total = page.total
    } else {
      list.value = []
      pagination.total = 0
    }
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
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
  queryParams.name = ''
  queryParams.type = ''
  queryParams.shop_category_id = ''
  queryParams.business_status = ''
  queryParams.shop_status = ''
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

/**
 * 账号管理
 * @param {object} row - 当前店铺行
 */
const handleAccount = (row) => {
  ElMessage.info('账号管理：' + row.name)
}

/**
 * 删除店铺
 * @param {number} id - 店铺ID
 */
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除？', '提示', { type: 'warning' })
    const res = await request.post('/web/mall/admin/shop/del', { id })
    if (res && res.code === 1) {
      ElMessage.success('删除成功')
      fetchList()
    } else {
      ElMessage.error(res?.message || '删除失败')
    }
  } catch (e) {
    // 取消操作
  }
}

/**
 * 切换店铺状态（正常/冻结）
 * @param {number} status - 状态值 1/0
 * @param {number} id - 店铺ID
 */
const handleChangeStatus = async (status, id) => {
  try {
    const res = await mallShopStatus({ id, status })
    if (res && res.code === 1) {
      ElMessage.success('状态已更新')
    } else {
      ElMessage.error(res?.msg || res?.message || '操作失败')
      fetchList()
    }
  } catch (e) {
    fetchList()
  }
}

onMounted(() => {
  fetchList()
  fetchShopCategoryList()
})

onBeforeUnmount(destroyMapPicker)
</script>

<style scoped>
.store-name-cell {
  display: flex;
  align-items: center;
  min-width: 0;
  gap: 12px;
}

.store-logo {
  width: 58px;
  height: 58px;
  border-radius: 8px;
  overflow: hidden;
}

.store-logo-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--el-color-primary);
  font-size: 22px;
  font-weight: 600;
  background: var(--el-color-primary-light-9);
  border: 1px solid var(--el-color-primary-light-7);
}

.store-name-cell__text {
  min-width: 0;
  flex: 1;
  overflow: hidden;
}

.store-name-cell__name {
  overflow: hidden;
  color: var(--el-text-color-primary);
  font-size: 14px;
  line-height: 22px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

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
