<template>
    <div class="modal-overlay" v-if="visible" @click="handleClose">
        <div class="modal-content invoice-modal" @click.stop>
            <div class="modal-header">
                <div class="modal-title-wrap">
                    <span class="title-bar"></span>
                    <span class="modal-title">{{ $t('创建发票信息') }}</span>
                </div>
                <div class="modal-close" @click="handleClose">×</div>
            </div>
            <div class="modal-body modal-body--two-col">
                <div class="modal-col invoice-col">
                    <div class="invoice-section-header">{{ $t('发票信息') }}</div>
                    <div class="invoice-tip">{{ $t('温馨提示：发票的开票金额不包括优惠券部分') }}</div>

                    <!-- 发票类型 -->
                    <div class="form-item">
                        <label class="form-label">{{ $t('发票类型') }}:</label>
                        <div class="radio-group">
                            <div class="radio-option" :class="{ active: form.invoiceType === 0 }"
                                @click="form.invoiceType = 0">
                                <span class="radio-check" v-if="form.invoiceType === 0">✓</span>
                                {{ $t('普通发票') }}
                            </div>
                            <div class="radio-option" :class="{ active: form.invoiceType === 1 }"
                                @click="form.invoiceType = 1">
                                <span class="radio-check" v-if="form.invoiceType === 1">✓</span>
                                {{ $t('增值税专用发票') }}
                            </div>
                        </div>
                    </div>

                    <!-- 发票内容 -->
                    <div class="form-item">
                        <label class="form-label">{{ $t('发票内容') }}:</label>
                        <div class="radio-inline">
                            <label class="radio-label">
                                <input type="radio" v-model="form.invoiceContent" :value="0" />
                                <span>{{ $t('商品明细') }}</span>
                            </label>
                            <label class="radio-label">
                                <input type="radio" v-model="form.invoiceContent" :value="1" />
                                <span>{{ $t('商品类别') }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 抬头类型（仅普通发票显示） -->
                    <div class="form-item" v-if="form.invoiceType === 0">
                        <label class="form-label">{{ $t('抬头类型') }}:</label>
                        <div class="radio-inline">
                            <label class="radio-label">
                                <input type="radio" v-model="form.invoiceHeadType" :value="1" />
                                <span>{{ $t('个人') }}</span>
                            </label>
                            <label class="radio-label">
                                <input type="radio" v-model="form.invoiceHeadType" :value="2" />
                                <span>{{ $t('单位') }}</span>
                            </label>
                        </div>
                    </div>

                    <!-- 发票抬头/单位名称 -->
                    <div class="form-item">
                        <label class="form-label"><span class="required">*</span> {{ $t(invoiceHeadLabel || '') }}:</label>
                        <div class="input-with-btns">
                            <input type="text" class="form-input" v-model="form.invoiceHead"
                                :placeholder="invoiceHeadPlaceholder" />
                            <button type="button" class="btn-select" @click="showInvoiceModal = true">{{ $t('选择')
                                }}</button>
                            <span class="link-save" @click="handleSaveInvoiceInfo">{{ $t('保存发票信息') }}</span>
                        </div>
                    </div>

                    <!-- 普通发票选择单位时显示单位税号（必填） -->
                    <div class="form-item" v-if="form.invoiceType === 0 && form.invoiceHeadType === 2">
                        <label class="form-label"><span class="required">*</span> {{ $t('单位税号') }}:</label>
                        <input type="text" class="form-input" v-model="form.companyTaxId"
                            :placeholder="$t('请输入单位税号')" />
                    </div>

                    <!-- 增值税专用发票额外字段（无单位税号，其余必填） -->
                    <template v-if="form.invoiceType === 1">
                        <div class="form-item">
                            <label class="form-label"><span class="required">*</span> {{ $t('注册地址') }}:</label>
                            <input type="text" class="form-input" v-model="form.registerAddress"
                                :placeholder="$t('请输入注册地址')" />
                        </div>
                        <div class="form-item">
                            <label class="form-label"><span class="required">*</span> {{ $t('注册电话') }}:</label>
                            <input type="text" class="form-input" v-model="form.registerNumber"
                                :placeholder="$t('请输入注册电话')" />
                        </div>
                        <div class="form-item">
                            <label class="form-label"><span class="required">*</span> {{ $t('开户银行') }}:</label>
                            <input type="text" class="form-input" v-model="form.openingBank"
                                :placeholder="$t('请输入开户银行')" />
                        </div>
                        <div class="form-item">
                            <label class="form-label"><span class="required">*</span> {{ $t('银行帐号') }}:</label>
                            <input type="text" class="form-input" v-model="form.bankAccount"
                                :placeholder="$t('请输入银行帐号')" />
                        </div>
                    </template>
                </div>

                <!-- 收票地址（右侧，选填；若填写任一项则四项均需完整） -->
                <div class="modal-col address-col">
                    <div class="address-section-header">{{ $t('收票地址') }}</div>
                    <div class="form-item">
                        <label class="form-label">{{ $t('收票人') }}:</label>
                        <div class="input-with-btns">
                            <input type="text" class="form-input" v-model="form.userName"
                                :placeholder="$t('请输入收票人姓名')" />
                            <button type="button" class="btn-select" @click="showAddressModal = true">{{ $t('选择')
                                }}</button>
                            <span class="link-save" @click="handleSaveAddress">{{ $t('保存地址信息') }}</span>
                        </div>
                    </div>
                    <div class="form-item">
                        <label class="form-label">{{ $t('联系方式') }}:</label>
                        <input type="tel" class="form-input" v-model="form.userMobile" :placeholder="$t('请输入联系方式')" />
                    </div>
                    <div class="form-item">
                        <label class="form-label">{{ $t('所在地区') }}:</label>
                        <AddressLinkage v-model="form.areaIds" :show-area="3" return-type="id"
                            @update:modelValue="handleAreaChange" />
                    </div>
                    <div class="form-item">
                        <label class="form-label">{{ $t('详细地址') }}:</label>
                        <textarea class="form-textarea" v-model="form.addressDetail"
                            :placeholder="$t('请输入详细地址')"></textarea>
                    </div>
                    <div class="form-item">
                        <label class="form-label">{{ $t('电子邮箱') }}:</label>
                        <input type="email" class="form-input" v-model="form.userMail" :placeholder="$t('请输入电子邮箱')" />
                    </div>
                </div>
            </div>

            <!-- 选择发票信息弹窗 -->
            <div class="modal-overlay inner" v-if="showInvoiceModal" @click="showInvoiceModal = false">
                <div class="modal-content address-select-modal" @click.stop>
                    <div class="modal-header">
                        <div class="modal-title">{{ $t('选择发票信息') }}</div>
                        <div class="modal-close" @click="showInvoiceModal = false">×</div>
                    </div>
                    <div class="modal-body">
                        <div v-if="invoiceListLoading" class="empty-tip">{{ $t('加载中...') }}</div>
                        <div v-else-if="!invoiceList.length" class="empty-tip">{{ $t('暂无已保存的发票信息') }}</div>
                        <div v-else class="address-table-wrapper">
                            <table class="address-table">
                                <thead>
                                    <tr>
                                        <th>{{ $t('发票抬头') }}</th>
                                        <th>{{ $t('发票类型') }}</th>
                                        <th>{{ $t('税号/注册信息') }}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="item in invoiceList" :key="item.invoiceTmpId" class="address-row"
                                        :class="{ 'is-default': item.isDefault === 1 }"
                                        @click="handleSelectInvoice(item)">
                                        <td>
                                            <span class="address-name">{{ item.invoiceHead || '-' }}</span>
                                            <span v-if="item.isDefault === 1" class="default-tag">{{ $t('默认') }}</span>
                                        </td>
                                        <td>{{ item.invoiceType === 1 ? $t('增值税专用发票') : $t('普通发票') }}</td>
                                        <td>{{ item.companyTaxId || (item.registerAddress ? $t('已填写') : '-') }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 选择收票地址弹窗（选择已保存地址） -->
            <div class="modal-overlay inner" v-if="showAddressModal" @click="showAddressModal = false">
                <div class="modal-content address-select-modal" @click.stop>
                    <div class="modal-header">
                        <div class="modal-title">{{ $t('选择收票地址') }}</div>
                        <div class="modal-close" @click="showAddressModal = false">×</div>
                    </div>
                    <div class="modal-body">
                        <div v-if="addressListLoading" class="empty-tip">{{ $t('加载中...') }}</div>
                        <div v-else-if="!addressList.length" class="empty-tip">{{ $t('暂无已保存的收票地址') }}</div>
                        <div v-else class="address-table-wrapper">
                            <table class="address-table">
                                <thead>
                                    <tr>
                                        <th>{{ $t('名称') }}</th>
                                        <th>{{ $t('手机号') }}</th>
                                        <th>{{ $t('地址信息') }}</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <tr v-for="addr in addressList" :key="addr.addressId" class="address-row"
                                        :class="{ 'is-default': addr.isDefault === 1 }"
                                        @click="handleSelectAddress(addr)">
                                        <td>
                                            <span class="address-name">{{ $t(addr.userName || '') }}</span>
                                            <span v-if="addr.isDefault === 1" class="default-tag">{{ $t('默认') }}</span>
                                        </td>
                                        <td>{{ addr.userMobile }}</td>
                                        <td>{{ formatAddressDisplay(addr) }}</td>
                                    </tr>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="modal-footer">
                <button type="button" class="btn-cancel" @click="handleClose">{{ $t('取消') }}</button>
                <button type="button" class="btn-confirm" @click="handleConfirm">{{ $t('确认') }}</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, watch, reactive, computed } from 'vue'
import AddressLinkage from '../Address/AddressLinkage.vue'
import { Toast } from '~/composables/useToast'
import { getInvoiceAddressList, insertInvoiceAddress, insertInvoiceInfo, getInvoiceInfoList } from '~/services/order.service'
import type { InvoiceInfo, InvoiceAddress, InvoiceTemplate } from '~/types/business'

const props = withDefaults(defineProps<{
    modelValue?: boolean
    initialValue?: InvoiceInfo | null
}>(), {
    modelValue: false,
    initialValue: null
})

const emits = defineEmits<{
    'update:modelValue': [value: boolean]
    'confirm': [info: InvoiceInfo]
}>()

const visible = ref(props.modelValue)
const showAddressModal = ref(false)
const addressList = ref<InvoiceAddress[]>([])
const addressListLoading = ref(false)
const showInvoiceModal = ref(false)
const invoiceList = ref<InvoiceTemplate[]>([])
const invoiceListLoading = ref(false)

const form = reactive({
    invoiceType: 0 as number,
    invoiceContent: 0 as number,
    invoiceHeadType: 1 as number,
    invoiceHead: '',
    companyTaxId: '',
    registerAddress: '',
    registerNumber: '',
    openingBank: '',
    bankAccount: '',
    userName: '',
    userMobile: '',
    userMail: '',
    addressProvince: undefined as number | undefined,
    addressCity: undefined as number | undefined,
    addressArea: undefined as number | undefined,
    addressDetail: '',
    areaIds: [] as (string | number)[]
})

// 发票抬头/单位名称标签与占位符
const invoiceHeadLabel = computed(() => {
    if (form.invoiceType === 1) return '单位名称'
    if (form.invoiceHeadType === 2) return '单位名称'
    return '发票抬头'
})
const invoiceHeadPlaceholder = computed(() => {
    if (form.invoiceType === 1) return '请输入单位名称'
    if (form.invoiceHeadType === 2) return '请输入单位名称'
    return '请输入发票抬头'
})

watch(() => form.invoiceType, (val) => {
    if (val === 1) form.invoiceHeadType = 2
})

watch(showAddressModal, async (val) => {
    if (val) {
        addressListLoading.value = true
        addressList.value = []
        try {
            const res = await getInvoiceAddressList({})
            if (res?.code === 1 && res.data?.list) {
                addressList.value = res.data.list
            }
        } catch (e) {
            console.error('获取收票地址列表失败:', e)
            Toast.error('获取收票地址列表失败')
        } finally {
            addressListLoading.value = false
        }
    }
})

watch(showInvoiceModal, async (val) => {
    if (val) {
        invoiceListLoading.value = true
        invoiceList.value = []
        try {
            const res = await getInvoiceInfoList({})
            if (res?.code === 1 && res.data?.list) {
                invoiceList.value = res.data.list
            }
        } catch (e) {
            console.error('获取发票信息列表失败:', e)
            Toast.error('获取发票信息列表失败')
        } finally {
            invoiceListLoading.value = false
        }
    }
})

const handleSelectInvoice = (item: InvoiceTemplate) => {
    form.invoiceType = item.invoiceType ?? 0
    form.invoiceContent = item.invoiceContent ?? 0
    form.invoiceHeadType = item.invoiceHeadType ?? 1
    form.invoiceHead = item.invoiceHead ?? ''
    form.companyTaxId = item.companyTaxId ?? ''
    form.registerAddress = item.registerAddress ?? ''
    form.registerNumber = item.registerNumber ?? ''
    form.openingBank = item.openingBank ?? ''
    form.bankAccount = item.bankAccount ?? ''
    showInvoiceModal.value = false
}

const handleSaveInvoiceInfo = async () => {
    if (!form.invoiceHead?.trim()) {
        Toast.warning(`请先填写${invoiceHeadLabel.value}`)
        return
    }
    if (form.invoiceType === 0 && form.invoiceHeadType === 2 && !form.companyTaxId?.trim()) {
        Toast.warning('请填写单位税号')
        return
    }
    if (form.invoiceType === 1) {
        if (!form.registerAddress?.trim() || !form.registerNumber?.trim() || !form.openingBank?.trim() || !form.bankAccount?.trim()) {
            Toast.warning('请填写完整的增值税专用发票信息')
            return
        }
    }
    try {
        const res = await insertInvoiceInfo({
            invoiceType: form.invoiceType,
            invoiceHeadType: form.invoiceHeadType,
            invoiceHead: form.invoiceHead.trim(),
            companyTaxId: form.invoiceType === 0 && form.invoiceHeadType === 2 ? form.companyTaxId?.trim() : undefined,
            registerAddress: form.invoiceType === 1 ? form.registerAddress?.trim() : undefined,
            registerNumber: form.invoiceType === 1 ? form.registerNumber?.trim() : undefined,
            openingBank: form.invoiceType === 1 ? form.openingBank?.trim() : undefined,
            bankAccount: form.invoiceType === 1 ? form.bankAccount?.trim() : undefined,
            invoiceContent: form.invoiceContent,
            isDefault: 0
        })
        if (res?.code === 1) {
            Toast.success('发票信息已保存')
        } else {
            Toast.error(res?.message || '保存失败')
        }
    } catch (e) {
        console.error('保存发票信息失败:', e)
        Toast.error('保存失败，请重试')
    }
}

const formatAddressDisplay = (addr: InvoiceAddress): string => {
    const parts = [addr.preCompleteAddress, addr.addressDetail].filter(Boolean)
    return parts.join(' ') || '-'
}

const handleSelectAddress = (addr: InvoiceAddress) => {
    Object.assign(form, {
        userName: addr.userName ?? '',
        userMobile: addr.userMobile ?? '',
        userMail: addr.userMail ?? '',
        addressProvince: addr.addressProvince,
        addressCity: addr.addressCity,
        addressArea: addr.addressArea,
        addressDetail: addr.addressDetail ?? '',
        areaIds: [
            addr.addressProvince,
            addr.addressCity,
            addr.addressArea
        ].filter((v): v is number => v != null)
    })
    showAddressModal.value = false
}

watch(() => props.modelValue, (val) => {
    visible.value = val
    if (val && props.initialValue) {
        const init = props.initialValue
        const areaIds: (string | number)[] = []
        if (init.addressProvince != null) areaIds.push(init.addressProvince)
        if (init.addressCity != null) areaIds.push(init.addressCity)
        if (init.addressArea != null) areaIds.push(init.addressArea)
        Object.assign(form, {
            invoiceType: init.invoiceType ?? 0,
            invoiceContent: init.invoiceContent ?? 0,
            invoiceHeadType: init.invoiceHeadType ?? 1,
            invoiceHead: init.invoiceHead ?? '',
            companyTaxId: init.companyTaxId ?? '',
            registerAddress: init.registerAddress ?? '',
            registerNumber: init.registerNumber ?? '',
            openingBank: init.openingBank ?? '',
            bankAccount: init.bankAccount ?? '',
            userName: init.userName ?? '',
            userMobile: init.userMobile ?? '',
            userMail: init.userMail ?? '',
            addressProvince: init.addressProvince,
            addressCity: init.addressCity,
            addressArea: init.addressArea,
            addressDetail: init.addressDetail ?? '',
            areaIds: areaIds.length ? areaIds : []
        })
    } else if (val) {
        Object.assign(form, {
            invoiceType: 0,
            invoiceContent: 0,
            invoiceHeadType: 1,
            invoiceHead: '',
            companyTaxId: '',
            registerAddress: '',
            registerNumber: '',
            openingBank: '',
            bankAccount: '',
            userName: '',
            userMobile: '',
            userMail: '',
            addressProvince: undefined,
            addressCity: undefined,
            addressArea: undefined,
            addressDetail: '',
            areaIds: []
        })
    }
})

watch(visible, (val) => {
    emits('update:modelValue', val)
})

const handleAreaChange = (val: (string | number)[]) => {
    form.areaIds = val
    if (val.length >= 1) form.addressProvince = Number(val[0]) || undefined
    if (val.length >= 2) form.addressCity = Number(val[1]) || undefined
    if (val.length >= 3) form.addressArea = Number(val[2]) || undefined
}

const handleClose = () => {
    visible.value = false
}

const handleSaveAddress = async () => {
    // 收票地址：若填写任一项，则四项均需完整
    const hasUserName = !!form.userName?.trim()
    const hasUserMobile = !!form.userMobile?.trim()
    const hasArea = form.addressProvince != null && form.addressCity != null && form.addressArea != null
    const hasAddressDetail = !!form.addressDetail?.trim()
    const addressFieldCount = [hasUserName, hasUserMobile, hasArea, hasAddressDetail].filter(Boolean).length
    if (addressFieldCount > 0 && addressFieldCount < 4) {
        Toast.warning('请填写完整的收票地址（收票人、联系方式、所在地区、详细地址）')
        return
    }
    if (addressFieldCount === 0) {
        Toast.warning('请填写收票地址信息')
        return
    }
    try {
        const res = await insertInvoiceAddress({
            userName: form.userName?.trim(),
            userMobile: form.userMobile?.trim(),
            userMail: form.userMail?.trim() || undefined,
            addressProvince: form.addressProvince,
            addressCity: form.addressCity,
            addressArea: form.addressArea,
            addressDetail: form.addressDetail?.trim(),
            isDefault: 0
        })
        if (res?.code === 1) {
            Toast.success('地址信息已保存')
        } else {
            Toast.error(res?.message || '保存失败')
        }
    } catch (e) {
        console.error('保存收票地址失败:', e)
        Toast.error('保存失败，请重试')
    }
}

const handleConfirm = () => {
    if (!form.invoiceHead.trim()) {
        Toast.warning(`请输入${invoiceHeadLabel.value}`)
        return
    }
    // 普通发票+单位：单位税号必填
    if (form.invoiceType === 0 && form.invoiceHeadType === 2 && !form.companyTaxId.trim()) {
        Toast.warning('请输入单位税号')
        return
    }
    // 增值税专用发票：注册地址、注册电话、开户银行、银行帐号必填
    if (form.invoiceType === 1) {
        if (!form.registerAddress?.trim()) {
            Toast.warning('请输入注册地址')
            return
        }
        if (!form.registerNumber?.trim()) {
            Toast.warning('请输入注册电话')
            return
        }
        if (!form.openingBank?.trim()) {
            Toast.warning('请输入开户银行')
            return
        }
        if (!form.bankAccount?.trim()) {
            Toast.warning('请输入银行帐号')
            return
        }
    }
    // 收票地址：若填写任一项（收票人、联系方式、所在地区、详细地址），则四项均需完整
    const hasUserName = !!form.userName?.trim()
    const hasUserMobile = !!form.userMobile?.trim()
    const hasArea = form.addressProvince != null && form.addressCity != null && form.addressArea != null
    const hasAddressDetail = !!form.addressDetail?.trim()
    const addressFieldCount = [hasUserName, hasUserMobile, hasArea, hasAddressDetail].filter(Boolean).length
    if (addressFieldCount > 0 && addressFieldCount < 4) {
        Toast.warning('请填写完整的收票地址（收票人、联系方式、所在地区、详细地址）')
        return
    }
    const info: InvoiceInfo = {
        invoiceType: form.invoiceType,
        invoiceContent: form.invoiceContent,
        invoiceHeadType: form.invoiceHeadType,
        invoiceHead: form.invoiceHead.trim(),
        companyTaxId: form.invoiceType === 0 && form.invoiceHeadType === 2 ? form.companyTaxId.trim() : undefined,
        registerAddress: form.invoiceType === 1 ? form.registerAddress?.trim() : undefined,
        registerNumber: form.invoiceType === 1 ? form.registerNumber?.trim() : undefined,
        openingBank: form.invoiceType === 1 ? form.openingBank?.trim() : undefined,
        bankAccount: form.invoiceType === 1 ? form.bankAccount?.trim() : undefined,
        userName: form.userName || undefined,
        userMobile: form.userMobile || undefined,
        userMail: form.userMail || undefined,
        addressProvince: form.addressProvince,
        addressCity: form.addressCity,
        addressArea: form.addressArea,
        addressDetail: form.addressDetail || undefined
    }
    emits('confirm', info)
    visible.value = false
}
</script>

<style lang="scss" scoped>
.modal-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
    animation: fadeIn 0.2s ease;

    &.inner {
        z-index: 1001;
    }
}

@keyframes fadeIn {
    from {
        opacity: 0;
    }

    to {
        opacity: 1;
    }
}

.modal-content {
    background: #fff;
    border-radius: 12px;
    max-width: 520px;
    width: 90%;
    max-height: 90vh;
    overflow-y: auto;
    box-shadow: 0 12px 48px rgba(0, 0, 0, 0.15);
    animation: slideUp 0.25s ease;

    &.invoice-modal {
        max-width: 920px;
    }

    &.address-select-modal {
        max-width: 640px;
    }
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.modal-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 18px 24px;
    border-bottom: 1px solid #f0f0f0;

    .modal-title-wrap {
        display: flex;
        align-items: center;
        gap: 12px;

        .title-bar {
            width: 4px;
            height: 22px;
            background: var(--color);
            border-radius: 2px;
        }

        .modal-title {
            font-size: 18px;
            font-weight: 600;
            color: #262626;
            letter-spacing: 0.3px;
        }
    }

    .modal-close {
        font-size: 22px;
        color: #8c8c8c;
        cursor: pointer;
        width: 36px;
        height: 36px;
        display: flex;
        justify-content: center;
        align-items: center;
        border-radius: 50%;
        transition: all 0.2s ease;

        &:hover {
            background: #f5f5f5;
            color: #262626;
        }
    }
}

.modal-body {
    padding: 24px;

    &--two-col {
        display: flex;
        gap: 32px;
        align-items: flex-start;

        .modal-col {
            flex: 1;
            min-width: 0;

            &.address-col {
                padding-left: 32px;
                border-left: 1px solid #f0f0f0;
            }
        }
    }
}

.invoice-section-header,
.address-section-header {
    font-size: 15px;
    font-weight: 600;
    color: #595959;
    padding: 12px 0;
    margin: 0 0 20px 0;
    border-bottom: 1px solid #e8e8e8;
    background: linear-gradient(180deg, #fafafa 0%, #f5f5f5 100%);
    margin: -4px -4px 20px -4px;
    padding: 12px 16px;
    border-radius: 6px;
}

.link-save {
    font-size: 14px;
    color: var(--color);
    cursor: pointer;
    white-space: nowrap;
    flex-shrink: 0;
    transition: color 0.2s;

    &:hover {
        color: var(--color);
        opacity: 0.85;
        text-decoration: underline;
    }
}

.invoice-tip {
    font-size: 13px;
    color: #d46b08;
    margin-bottom: 20px;
    padding: 10px 14px;
    background: linear-gradient(135deg, #fffbe6 0%, #fff7e6 100%);
    border-radius: 6px;
    border-left: 3px solid #fa8c16;
}

.form-item {
    margin-bottom: 18px;

    &:last-child {
        margin-bottom: 0;
    }

    .form-label {
        display: block;
        font-size: 14px;
        color: #262626;
        margin-bottom: 8px;
        font-weight: 500;

        .required {
            color: #ff4d4f;
            margin-right: 2px;
        }
    }

    .form-input,
    .form-textarea {
        width: 100%;
        padding: 10px 14px;
        border: 1px solid #d9d9d9;
        border-radius: 6px;
        font-size: 14px;
        box-sizing: border-box;
        transition: border-color 0.2s, box-shadow 0.2s;

        &::placeholder {
            color: #bfbfbf;
        }

        &:hover {
            border-color: #bfbfbf;
        }

        &:focus {
            outline: none;
            border-color: var(--color);
            box-shadow: 0 0 0 2px color-mix(in srgb, var(--color) 15%, transparent);
        }
    }

    .form-textarea {
        min-height: 88px;
        resize: vertical;
        line-height: 1.5;
    }

    .radio-group {
        display: flex;
        gap: 12px;

        .radio-option {
            flex: 1;
            padding: 12px 20px;
            border: 1px solid #d9d9d9;
            border-radius: 6px;
            cursor: pointer;
            text-align: center;
            position: relative;
            transition: all 0.2s ease;
            font-size: 14px;
            color: #595959;

            &.active {
                border-color: var(--color);
                background: color-mix(in srgb, var(--color) 8%, white);
                color: var(--color);
                font-weight: 500;

                .radio-check {
                    position: absolute;
                    right: 12px;
                    top: 50%;
                    transform: translateY(-50%);
                    color: var(--color);
                    font-size: 14px;
                    font-weight: 600;
                }
            }

            &:hover:not(.active) {
                border-color: var(--color);
                color: var(--color);
            }
        }
    }

    .radio-inline {
        display: flex;
        gap: 32px;
        flex-wrap: wrap;

        .radio-label {
            display: flex;
            align-items: center;
            gap: 8px;
            cursor: pointer;
            font-size: 14px;
            color: #595959;
            transition: color 0.2s;

            &:hover {
                color: #262626;
            }

            input {
                cursor: pointer;
                width: 16px;
                height: 16px;
                accent-color: var(--color);
            }
        }
    }

    .input-with-btns {
        display: flex;
        gap: 10px;
        align-items: center;

        .form-input {
            flex: 1;
            min-width: 0;
        }

        .btn-select {
            flex-shrink: 0;
            padding: 10px 18px;
            border: 1px solid var(--color);
            border-radius: 6px;
            font-size: 14px;
            color: var(--color);
            background: #fff;
            cursor: pointer;
            white-space: nowrap;
            transition: all 0.2s ease;

            &:hover {
                background: color-mix(in srgb, var(--color) 8%, white);
                border-color: var(--color);
                color: var(--color);
            }
        }
    }
}

.modal-footer {
    padding: 18px 24px;
    border-top: 1px solid #f0f0f0;
    display: flex;
    justify-content: flex-end;
    gap: 12px;
    background: #fafafa;
    border-radius: 0 0 12px 12px;

    .btn-cancel {
        padding: 10px 28px;
        border: 1px solid #d9d9d9;
        border-radius: 6px;
        font-size: 14px;
        color: #595959;
        background: #fff;
        cursor: pointer;
        transition: all 0.2s ease;

        &:hover {
            border-color: var(--color);
            color: var(--color);
            background: color-mix(in srgb, var(--color) 8%, white);
        }
    }

    .btn-confirm {
        padding: 10px 28px;
        border: none;
        border-radius: 6px;
        font-size: 14px;
        color: #fff;
        background: var(--color);
        cursor: pointer;
        transition: all 0.2s ease;
        box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 30%, transparent);

        &:hover {
            opacity: 0.9;
            box-shadow: 0 4px 8px color-mix(in srgb, var(--color) 35%, transparent);
        }
    }
}

.empty-tip {
    padding: 32px 24px;
    text-align: center;
    color: #8c8c8c;
    font-size: 14px;
}

.address-table-wrapper {
    max-height: 360px;
    overflow: auto;
    border: 1px solid #e8e8e8;
    border-radius: 6px;
}

.address-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;

    th,
    td {
        padding: 12px 16px;
        text-align: left;
        border-bottom: 1px solid #f0f0f0;
    }

    th {
        background: #fafafa;
        font-weight: 600;
        color: #333;
        white-space: nowrap;
    }

    tbody tr:last-child td {
        border-bottom: none;
    }

    .address-row {
        cursor: pointer;
        transition: background-color 0.2s ease;

        &:hover {
            background: color-mix(in srgb, var(--color) 8%, white);
        }

        &.is-default {
            .default-tag {
                display: inline-block;
            }
        }

        .address-name {
            font-weight: 500;
            color: #262626;
        }

        .default-tag {
            display: none;
            font-size: 12px;
            color: var(--color);
            background: color-mix(in srgb, var(--color) 8%, white);
            padding: 2px 8px;
            border-radius: 4px;
            margin-left: 8px;
        }
    }
}

@media (max-width: 768px) {
    .modal-body--two-col {
        flex-direction: column;
        gap: 24px;

        .modal-col.address-col {
            padding-left: 0;
            padding-top: 24px;
            border-left: none;
            border-top: 1px solid #f0f0f0;
        }
    }
}
</style>
