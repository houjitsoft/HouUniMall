<template>
    <div class="address-section">
        <div class="section-header">
            <span class="section-title">{{ title }}</span>
            <span v-if="!valetOrderId" class="add-address" @click="handleAddAddress">+ {{ $t('添加新地址') }}</span>
        </div>
        <div class="address-list">
            <!-- 空地址显示 -->
            <EmptyState v-if="addresses.length === 0" :text="$t('您还没有添加收货地址')" compact />

            <!-- 地址列表 -->
            <template v-else>
                <div v-for="(address, displayIndex) in displayedAddresses" :key="address.userAddressId || displayIndex"
                    class="address-item" :class="{ active: selectedAddressIndex === getOriginalIndex(displayIndex) }"
                    @click="handleSelectAddress(getOriginalIndex(displayIndex))">
                    <div class="address-content">
                        <div class="contact-info">
                            <div class="contact-name">
                                {{ address.userAddressContact }}
                                <span v-if="address.userAddressDefault === 1" class="default-badge">{{ $t('默认')
                                    }}</span>
                            </div>
                            <div class="contact-phone">{{ address.internationalDialingCode }}-{{
                                address.userAddressPhone }}</div>
                        </div>
                        <div class="address-detail">{{ address.countryName }}-{{ address.userAddressArea || '' }} {{
                            address.userAddressAddress }}</div>
                    </div>
                    <div v-if="!valetOrderId" class="address-actions">
                        <span class="action-link" @click.stop="handleDeleteAddress(getOriginalIndex(displayIndex))">{{
                            $t('删除') }}</span>
                        <span class="action-link" @click.stop="handleEditAddress(getOriginalIndex(displayIndex))">{{
                            $t('修改') }}</span>
                    </div>
                    <div class="address-tag" v-if="selectedAddressIndex === getOriginalIndex(displayIndex)">
                        ✓
                    </div>
                </div>
                <!-- 展开/收起按钮 -->
                <div v-if="addresses.length > 3" class="expand-toggle" @click="toggleExpand">
                    <span class="expand-text">{{ isExpanded ? $t('收起') : $t('展开更多') }}({{ addresses.length - 3
                        }})</span>
                    <span class="expand-icon" :class="{ expanded: isExpanded }">▼</span>
                </div>
            </template>
        </div>

        <!-- 弹窗容器 -->
        <div class="modal-overlay" v-if="showModal" @click="handleCloseModal">
            <div class="modal-content" @click.stop>
                <!-- 弹窗头部 -->
                <div class="modal-header">
                    <div class="modal-title">{{ editingIndex === -1 ? $t('新建地址') : $t('编辑地址') }}</div>
                    <div class="modal-close" @click="handleCloseModal">×</div>
                </div>
                <!-- 表单内容 -->
                <div class="modal-body">
                    <!-- 收件人 -->
                    <div class="form-item">
                        <label class="form-label"><span class="required">*</span> {{ $t('收件人') }}:</label>
                        <input type="text" class="form-input" v-model="formData.name" :placeholder="$t('请输入收件人姓名')">
                    </div>

                    <!-- 手机号 -->
                    <div class="form-item">
                        <label class="form-label"><span class="required">*</span> {{ $t('手机') }}:</label>
                        <div class="phone-input-wrapper">
                            <div class="phone-code-selector" @click="showAreaCodeModal = true">
                                {{ formData.phoneCode }}
                                <span class="arrow-down">▼</span>
                            </div>
                            <input type="tel" class="form-input phone-number" v-model="formData.phone"
                                :placeholder="$t('请输入手机号码')" maxlength="11">
                        </div>
                    </div>

                    <!-- 所在地区（仅中国显示） -->
                    <div v-if="formData.phoneCode === '+86'" class="form-item">
                        <label class="form-label"><span class="required">*</span> {{ $t('所在地区') }}:</label>
                        <AddressLinkage v-model="formData.areaNames" :return-type="'name'"
                            @update:modelValue="handleAreaNameChange" />
                    </div>

                    <!-- 详细地址 -->
                    <div class="form-item">
                        <label class="form-label"><span class="required">*</span> {{ $t('详细地址') }}:</label>
                        <textarea class="form-textarea" v-model="formData.address"
                            :placeholder="$t('请输入详细地址')"></textarea>
                    </div>

                    <!-- 设置为默认地址 -->
                    <div class="form-item checkbox-item">
                        <label class="checkbox-wrapper">
                            <input type="checkbox" v-model="formData.isDefault">
                            <span class="checkmark"></span>
                            <span class="checkbox-label">{{ $t('设置为默认收货地址') }}</span>
                        </label>
                    </div>
                </div>
                <!-- 弹窗底部 -->
                <div class="modal-footer">
                    <button class="submit-btn" @click="handleSubmit">{{ $t('提交') }}</button>
                </div>
            </div>
        </div>

        <!-- 国家/地区代码选择弹窗 -->
        <AreaCode v-if="showAreaCodeModal" @select="handleAreaCodeSelect" @close="showAreaCodeModal = false" />
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, onMounted, computed } from 'vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import AreaCode from '../Address/AreaCode.vue'
import AddressLinkage from '../Address/AddressLinkage.vue'
import { getAddresses, addAddress, updateAddress, deleteAddress } from '~/services/userms.service'
import type { UserAddress, AddAddressParams, UpdateAddressParams } from '~/types/user'
import { ConfirmModal } from '~/composables/useConfirmModal'
import { queryDistrictAll } from '~/services/base.service'
import { Toast } from '~/composables/useToast'

import type { DistrictItem } from '~/types/address'

const props = withDefaults(
    defineProps<{
        /** 代客下单ID，携带时隐藏添加/修改/删除地址 */
        valetOrderId?: string
        /** 区块标题，默认「我的地址」 */
        title?: string
    }>(),
    { title: '我的地址' }
)

const emits = defineEmits<{
    'addressChange': [address: UserAddress | null]
}>()

// 地址列表
const addresses = ref<UserAddress[]>([])
// 当前选中的地址索引
const selectedAddressIndex = ref(0)
// 是否展开地址列表
const isExpanded = ref(false)

// 计算要显示的地址列表
const displayedAddresses = computed(() => {
    if (isExpanded.value || addresses.value.length <= 3) {
        return addresses.value
    }
    // 折叠状态下只显示前3个地址
    return addresses.value.slice(0, 3)
})

// 切换展开/收起
const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
}

// 获取原始索引（displayedAddresses 中的索引转换为 addresses 中的索引）
const getOriginalIndex = (displayIndex: number): number => {
    const displayedAddress = displayedAddresses.value[displayIndex]
    if (!displayedAddress) return displayIndex

    // 在原始地址数组中找到对应的索引
    const originalIndex = addresses.value.findIndex((addr: UserAddress) =>
        addr.userAddressId === displayedAddress.userAddressId
    )
    return originalIndex !== -1 ? originalIndex : displayIndex
}

/**
 * 统一更新选中地址并通知父组件
 */
const updateSelectedAddress = (index: number) => {
    selectedAddressIndex.value = index
    emits('addressChange', addresses.value[index] || null)
}

// 弹窗显示状态
const showModal = ref(false)
// 国家/地区代码选择弹窗显示状态
const showAreaCodeModal = ref(false)
// 当前编辑的地址索引
const editingIndex = ref(-1)

// 表单数据
const formData = reactive<{
    name: string
    phoneCode: string
    phone: string
    area: [string, string, string] // [provinceId, cityId, areaId]
    areaNames: [string, string, string] // [provinceName, cityName, areaName] 用于显示
    address: string
    isDefault: boolean
}>({
    name: '',
    phoneCode: '+86',
    phone: '',
    area: ['', '', ''],
    areaNames: ['', '', ''],
    address: '',
    isDefault: false
})

// 处理国家/地区代码选择
const handleAreaCodeSelect = (area: any) => {
    formData.phoneCode = area.code
    showAreaCodeModal.value = false
}

// 地区数据缓存
const districtDataCache = ref<DistrictItem[]>([])

// 加载地区数据
const loadDistrictData = async () => {
    try {
        const res = await queryDistrictAll({})
        if (res?.data) {
            districtDataCache.value = res.data as DistrictItem[]
        }
    } catch (error) {
        console.error('加载地区数据失败:', error)
    }
}

// 根据名称查找ID
const findDistrictIdByName = (name: string, parentList: DistrictItem[]): string => {
    const item = parentList.find(item => item.districtName === name)
    return item ? String(item.districtId) : ''
}

// 处理地区名称变化，同步更新ID
const handleAreaNameChange = async (areaNames: string[]) => {
    formData.areaNames = [areaNames[0] || '', areaNames[1] || '', areaNames[2] || '']

    // 通过名称查询对应的ID
    if (districtDataCache.value.length > 0 && areaNames[0]) {
        const provinceId = findDistrictIdByName(areaNames[0], districtDataCache.value)
        let cityId = ''
        let areaId = ''

        if (provinceId && areaNames[1]) {
            const province = districtDataCache.value.find(p => String(p.districtId) === provinceId)
            if (province?.childDistrict) {
                cityId = findDistrictIdByName(areaNames[1], province.childDistrict)

                if (cityId && areaNames[2]) {
                    const city = province.childDistrict.find((c: DistrictItem) => String(c.districtId) === cityId)
                    if (city?.childDistrict) {
                        areaId = findDistrictIdByName(areaNames[2], city.childDistrict)
                    }
                }
            }
        }

        formData.area = [provinceId, cityId, areaId]
    }
}

// 选择地址
const handleSelectAddress = (index: number) => {
    // 如果选中的地址不在显示的列表中（折叠状态下选择了第4个及之后的地址），自动展开
    if (!isExpanded.value && addresses.value.length > 3 && index >= 3) {
        isExpanded.value = true
    }
    updateSelectedAddress(index)
}

// 打开添加地址弹窗
const handleAddAddress = () => {
    // 重置表单数据
    Object.assign(formData, {
        name: '',
        phoneCode: '+86',
        phone: '',
        area: ['', '', ''],
        areaNames: ['', '', ''],
        address: '',
        isDefault: false
    })
    editingIndex.value = -1
    showModal.value = true
}

// 关闭弹窗
const handleCloseModal = () => {
    showModal.value = false
    editingIndex.value = -1 // 重置编辑索引
}

// 提交表单
const handleSubmit = async () => {
    // 表单验证
    if (!formData.name.trim()) {
        Toast.warning('请输入收件人姓名')
        return
    }
    if (!formData.phone.trim()) {
        Toast.warning('请输入手机号码')
        return
    }

    // 仅当 phoneCode 是 +86 时才校验所在地区
    if (formData.phoneCode === '+86') {
        if (!formData.area[0]) {
            Toast.warning('请选择省份')
            return
        }
        if (!formData.area[1]) {
            Toast.warning('请选择城市')
            return
        }
        if (!formData.area[2]) {
            Toast.warning('请选择区县')
            return
        }
    }

    if (!formData.address.trim()) {
        Toast.warning('请输入详细地址')
        return
    }

    // 格式化地址信息（保持原始字段结构）
    let formattedArea = ''
    if (formData.phoneCode === '+86' && formData.areaNames[0]) {
        // 中国地址格式化，使用名称
        const areaParts = formData.areaNames.filter(part => part.trim())
        formattedArea = areaParts.join(' ')
    }

    // 根据模式决定是添加还是编辑地址
    if (editingIndex.value === -1) {
        // 添加新地址
        try {
            const addParams: AddAddressParams = {
                userAddressContact: formData.name.trim(),
                userAddressAddress: formData.address.trim(),
                userAddressPhone: formData.phone.trim(),
                internationalType: 2,
                internationalDialingCode: formData.phoneCode,
                countryName: formData.phoneCode === '+86' ? '中国' : '',
                countryId: 1,
                userAddressDefault: formData.isDefault ? 1 : 0,
                userAddressProvinceId: Number(formData.area[0]) || 0,
                userAddressCityId: Number(formData.area[1]) || 0,
                userAddressAreaId: Number(formData.area[2]) || 0,
                userAddressArea: formattedArea
            }

            const res = await addAddress(addParams)
            if (res.code === 1) {
                Toast.success('地址添加成功')
                // 重新获取地址列表
                await loadAddresses()
                // 如果是第一个地址或设置为默认地址，自动选中
                if (addresses.value.length === 1 || formData.isDefault) {
                    const targetIndex = addresses.value.length - 1
                    updateSelectedAddress(targetIndex)
                }
            } else {
                Toast.error(res.message || '地址添加失败')
            }
        } catch (error) {
            console.error('添加地址失败:', error)
            Toast.error('地址添加失败，请重试')
        }
    } else {
        // 编辑地址
        const editedAddress = addresses.value[editingIndex.value]
        if (editedAddress) {
            try {
                const updateParams: UpdateAddressParams = {
                    userAddressId: editedAddress.userAddressId,
                    userAddressContact: formData.name.trim(),
                    internationalType: 2,
                    internationalDialingCode: formData.phoneCode,
                    countryName: formData.phoneCode === '+86' ? '中国' : '',
                    countryId: 1,
                    userAddressProvinceId: Number(formData.area[0]) || 0,
                    userAddressCityId: Number(formData.area[1]) || 0,
                    userAddressAreaId: Number(formData.area[2]) || 0,
                    userAddressArea: formattedArea,
                    userAddressAddress: formData.address.trim(),
                    userAddressPhone: formData.phone.trim(),
                    userAddressDefault: formData.isDefault ? 1 : 0
                }

                const res = await updateAddress(updateParams)
                if (res.code === 1) {
                    Toast.success('地址更新成功')
                    // 保存当前编辑的地址ID，用于重新加载后定位
                    const editedAddressId = editedAddress.userAddressId
                    // 重新获取地址列表
                    await loadAddresses()
                    // 如果设置为默认地址，loadAddresses 会自动选中默认地址
                    // 否则，尝试找到编辑的地址并选中
                    if (!formData.isDefault && addresses.value.length > 0) {
                        const editedIndex = addresses.value.findIndex((addr: UserAddress) => addr.userAddressId === editedAddressId)
                        if (editedIndex !== -1) {
                            updateSelectedAddress(editedIndex)
                        }
                    }
                } else {
                    Toast.error(res.message || '地址更新失败')
                }
            } catch (error) {
                console.error('更新地址失败:', error)
                Toast.error('地址更新失败，请重试')
            }
        }
    }

    // 关闭弹窗
    showModal.value = false
    // 重置编辑索引
    editingIndex.value = -1
}



// 删除地址
const handleDeleteAddress = async (index: number) => {
    const address = addresses.value[index]
    if (!address) return

    // 确认删除
    const confirmed = await ConfirmModal.confirm({
        title: '确认删除',
        message: '确定要删除这个收货地址吗？',
        confirmText: '删除',
        cancelText: '取消'
    })

    if (!confirmed) return

    try {
        const res = await deleteAddress({ userAddressId: address.userAddressId })
        if (res.code === 1) {
            Toast.success('地址删除成功')
            // 重新获取地址列表（会自动选中默认地址或第一个）
            await loadAddresses()
        } else {
            Toast.error(res.message || '地址删除失败')
        }
    } catch (error) {
        console.error('删除地址失败:', error)
        Toast.error('地址删除失败，请重试')
    }
}

// 编辑地址
const handleEditAddress = (index: number) => {
    const address = addresses.value[index]
    if (address) {
        // 使用原始字段
        const phoneCode = address.internationalDialingCode || '+86'
        const phone = address.userAddressPhone || ''

        // 使用ID填充地区
        const areaIds = [
            String(address.userAddressProvinceId || ''),
            String(address.userAddressCityId || ''),
            String(address.userAddressAreaId || '')
        ]

        // 解析地址区域名称用于显示
        let areaNames: [string, string, string] = ['', '', '']
        if (phoneCode === '+86' && address.userAddressArea) {
            const areaParts = address.userAddressArea.split(' ')
            if (areaParts.length >= 3) {
                areaNames[0] = areaParts[0] || ''
                areaNames[1] = areaParts[1] || ''
                areaNames[2] = areaParts[2] || ''
            } else if (areaParts.length === 2) {
                areaNames[0] = areaParts[0] || ''
                areaNames[1] = areaParts[1] || ''
            } else if (areaParts.length === 1) {
                areaNames[0] = areaParts[0] || ''
            }
        }

        // 填充表单数据
        Object.assign(formData, {
            name: address.userAddressContact || '',
            phoneCode: phoneCode,
            phone: phone,
            area: areaIds as [string, string, string],
            areaNames: areaNames,
            address: address.userAddressAddress || '',
            isDefault: address.userAddressDefault === 1
        })

        editingIndex.value = index
        showModal.value = true
    }
}

/**
 * 加载地址列表并自动选中合适的地址
 */
const loadAddresses = async () => {
    try {
        const addressRes = await getAddresses({
            platForm: 1,
            ...(props.valetOrderId ? { valetOrderId: props.valetOrderId } : {})
        })
        if (addressRes.code === 1 && addressRes.data) {
            addresses.value = addressRes.data

            // 自动选中：优先选择默认地址，否则选择第一个
            if (addressRes.data.length > 0) {
                const defaultIndex = addressRes.data.findIndex(addr => addr.userAddressDefault === 1)
                const targetIndex = defaultIndex !== -1 ? defaultIndex : 0
                updateSelectedAddress(targetIndex)
            } else {
                // 没有地址时，清空选中状态
                updateSelectedAddress(-1)
            }
        }
    } catch (error) {
        console.error('获取地址列表失败:', error)
        Toast.error('获取地址列表失败')
    }
}

// 获取地址列表
onMounted(async () => {
    await loadDistrictData()
    await loadAddresses()
})
</script>

<style lang="scss" scoped>
@keyframes float {

    0%,
    100% {
        transform: translateY(0);
    }

    50% {
        transform: translateY(-10px);
    }
}

.address-section {
    background-color: #fff;
    border-radius: 12px;
    margin-bottom: 30px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    padding: 24px;
    transition: box-shadow 0.3s ease;

    &:hover {
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
    }

    .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 24px;
        padding-bottom: 16px;
        border-bottom: 2px solid #f0f0f0;

        .section-title {
            font-size: 18px;
            font-weight: 700;
            color: #262626;
            letter-spacing: 0.3px;
        }

        .add-address {
            color: var(--color);
            font-size: 14px;
            font-weight: 500;
            cursor: pointer;
            padding: 6px 12px;
            border-radius: 6px;
            transition: all 0.2s ease;

            &:hover {
                background-color: rgba(24, 144, 255, 0.1);
                transform: translateY(-1px);
            }
        }
    }
}

/* 弹窗样式 */
.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 1000;
}

.modal-content {
    background-color: #fff;
    border-radius: 8px;
    width: 500px;
    max-width: 90%;
    max-height: 90%;
    overflow-y: auto;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid #e8e8e8;

    .modal-title {
        font-size: 18px;
        font-weight: bold;
        color: #333;
    }

    .modal-close {
        font-size: 24px;
        color: #999;
        cursor: pointer;
        width: 32px;
        height: 32px;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 50%;
        transition: all 0.3s ease;

        &:hover {
            background-color: #f0f0f0;
            color: #333;
        }
    }
}

.modal-body {
    padding: 24px 20px;
}

.form-item {
    margin-bottom: 20px;

    .form-label {
        display: block;
        font-size: 14px;
        color: #333;
        margin-bottom: 8px;
        font-weight: 500;

        .required {
            color: #ff4d4f;
            margin-right: 4px;
        }
    }

    .form-input,
    .form-select,
    .form-textarea {
        width: 100%;
        padding: 8px 12px;
        border: 1px solid #d9d9d9;
        border-radius: 4px;
        font-size: 14px;
        color: #333;
        transition: all 0.3s ease;
        box-sizing: border-box;

        &:focus {
            outline: none;
            border-color: var(--color);
            box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
        }
    }

    .form-textarea {
        min-height: 100px;
        resize: vertical;
    }

    .area-select-wrapper {
        display: flex;
        gap: 8px;

        .area-select {
            flex: 1;
            min-width: 0;
        }
    }

    .phone-input-wrapper {
        display: flex;
        gap: 8px;

        .phone-code-selector {
            width: 80px;
            flex-shrink: 0;
            padding: 8px 12px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            font-size: 14px;
            color: #333;
            background-color: #fff;
            cursor: pointer;
            display: flex;
            justify-content: space-between;
            align-items: center;
            transition: all 0.3s ease;

            &:hover {
                border-color: var(--color);
                box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
            }

            .arrow-down {
                font-size: 12px;
                color: #999;
            }
        }

        .phone-number {
            flex: 1;
        }
    }

    &.checkbox-item {
        .checkbox-wrapper {
            display: flex;
            align-items: center;
            cursor: pointer;

            input[type="checkbox"] {
                position: absolute;
                opacity: 0;
                cursor: pointer;
            }

            .checkmark {
                position: relative;
                display: inline-block;
                width: 18px;
                height: 18px;
                border: 1px solid #d9d9d9;
                border-radius: 4px;
                background-color: #fff;
                margin-right: 8px;
                transition: all 0.3s ease;

                &::after {
                    content: '';
                    position: absolute;
                    left: 5px;
                    top: 1px;
                    width: 6px;
                    height: 10px;
                    border: solid #fff;
                    border-width: 0 2px 2px 0;
                    transform: rotate(45deg);
                    opacity: 0;
                    transition: all 0.3s ease;
                }
            }

            input[type="checkbox"]:checked+.checkmark {
                background-color: var(--color);
                border-color: var(--color);

                &::after {
                    opacity: 1;
                }
            }

            .checkbox-label {
                font-size: 14px;
                color: #333;
                user-select: none;
            }
        }
    }
}

.modal-footer {
    padding: 16px 20px;
    border-top: 1px solid #e8e8e8;
    display: flex;
    justify-content: flex-end;

    .submit-btn {
        padding: 8px 24px;
        background-color: var(--color);
        color: #fff;
        border: none;
        border-radius: 4px;
        font-size: 14px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;

        &:hover {
            background-color: #ff7875;
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(255, 77, 79, 0.3);
        }

        &:active {
            transform: translateY(0);
            box-shadow: 0 1px 4px rgba(255, 77, 79, 0.3);
        }
    }
}

.address-list {
    .address-item {
        display: flex;
        align-items: center;
        justify-content: space-between;
        padding: 20px;
        background: linear-gradient(135deg, #ffffff 0%, #fafbfc 100%);
        border-radius: 8px;
        margin-bottom: 12px;
        border: 2px solid #e8e8e8;
        cursor: pointer;
        transition: all 0.3s ease;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.02);

        &:hover {
            border-color: var(--color);
            box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
            transform: translateY(-2px);
        }

        &.active {
            border-color: var(--color);
            background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
            box-shadow: 0 4px 16px rgba(24, 144, 255, 0.2);
        }

        .address-content {
            flex: 1;
            display: flex;
            flex-direction: column;
            gap: 8px;
            min-width: 0;

            .contact-info {
                display: flex;
                align-items: center;
                flex-wrap: wrap;
                gap: 12px;

                .contact-name {
                    font-size: 16px;
                    font-weight: 600;
                    color: #262626;
                    display: flex;
                    align-items: center;
                    gap: 8px;

                    .default-badge {
                        display: inline-block;
                        padding: 3px 10px;
                        font-size: 11px;
                        font-weight: 600;
                        color: #fff;
                        background: linear-gradient(135deg, var(--color) 0%, #40a9ff 100%);
                        border: none;
                        border-radius: 12px;
                        line-height: 1.4;
                        box-shadow: 0 2px 4px rgba(24, 144, 255, 0.3);
                    }
                }

                .contact-phone {
                    font-size: 14px;
                    color: #8c8c8c;
                    font-weight: 400;
                }
            }

            .address-detail {
                font-size: 14px;
                color: #595959;
                line-height: 1.6;
                margin-top: 4px;
            }
        }

        .address-actions {
            display: flex;
            align-items: center;
            gap: 16px;
            margin-left: 16px;

            .action-link {
                font-size: 13px;
                color: #595959;
                cursor: pointer;
                padding: 4px 8px;
                border-radius: 4px;
                transition: all 0.2s ease;
                font-weight: 400;

                &:hover {
                    color: var(--color);
                    background-color: rgba(24, 144, 255, 0.08);
                }
            }
        }

        .address-tag {
            width: 28px;
            height: 28px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--color) 0%, #40a9ff 100%);
            color: #fff;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 14px;
            font-weight: 600;
            margin-left: 16px;
            box-shadow: 0 2px 6px rgba(24, 144, 255, 0.3);
            flex-shrink: 0;
        }
    }

    .expand-toggle {
        display: flex;
        align-items: center;
        justify-content: center;
        padding: 14px;
        margin-top: 12px;
        background: linear-gradient(135deg, #f8f9fa 0%, #f0f2f5 100%);
        border-radius: 8px;
        cursor: pointer;
        transition: all 0.3s ease;
        user-select: none;
        border: 1px solid #e8e8e8;

        &:hover {
            background: linear-gradient(135deg, #e9ecef 0%, #e1e4e8 100%);
            border-color: var(--color);
            transform: translateY(-1px);
            box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
        }

        .expand-text {
            font-size: 14px;
            color: var(--color);
            font-weight: 600;
            margin-right: 8px;
            letter-spacing: 0.3px;
        }

        .expand-icon {
            font-size: 12px;
            color: var(--color);
            transition: transform 0.3s ease;
            font-weight: 600;

            &.expanded {
                transform: rotate(180deg);
            }
        }
    }
}

/* 响应式设计 */
@media (max-width: 768px) {
    .address-list {
        .address-item {
            flex-direction: column;
            align-items: flex-start;

            .address-actions {
                margin-top: 12px;

                .action-link {
                    margin-left: 0;
                    margin-right: 16px;
                }
            }

            .address-tag {
                position: absolute;
                top: 16px;
                right: 16px;
                margin-left: 0;
            }
        }
    }
}
</style>