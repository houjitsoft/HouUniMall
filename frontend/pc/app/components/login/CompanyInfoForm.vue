<template>
    <div class="step-content">
        <!-- 公司名称 -->
        <div class="form-field">
            <label class="field-label">
                {{ $t('公司名称') }} <span class="required">*</span>
            </label>
            <div class="input-wrapper">
                <i class="input-icon">📄</i>
                <input :value="formData.companyName" @input="handleCompanyNameInput" @blur="handleCompanyNameBlur"
                    type="text" class="form-input" :class="{ 'input-error': companyNameError }"
                    :placeholder="$t('请务必与贵司营业执照抬头保持一致')" />
            </div>
            <div v-if="companyNameError" class="field-error">{{ $t(companyNameError || '') }}</div>
        </div>

        <!-- 税号 -->
        <div class="form-field">
            <label class="field-label">
                {{ $t('税号') }} <span class="required">*</span>
            </label>
            <div class="input-wrapper">
                <i class="input-icon">📋</i>
                <input 
                    :value="formData.taxId" 
                    @input="handleTaxIdInput"
                    @blur="handleTaxIdBlur"
                    type="text"
                    class="form-input"
                    :class="{ 'input-error': taxIdError }"
                    :placeholder="$t('请务必与贵司税务登记证保持一致')" />
            </div>
            <div class="field-hint">
                {{ $t('纳税人识别号是税务登记证上的号码，通常简称为"税号"，每个企业的纳税人识别号都是唯一的。由15位、17位、18或者20位字母或数字组成。') }}
            </div>
            <div v-if="taxIdError" class="field-error">{{ $t(taxIdError || '') }}</div>
        </div>

        <!-- 注册地址 -->
        <div class="form-field">
            <label class="field-label">
                {{ $t('注册地址') }} <span class="required">*</span>
            </label>
            <div class="input-wrapper">
                <i class="input-icon">📍</i>
                <input 
                    :value="formData.registrationAddress"
                    @input="handleRegistrationAddressInput"
                    @blur="handleRegistrationAddressBlur"
                    type="text" 
                    class="form-input"
                    :class="{ 'input-error': registrationAddressError }"
                    :placeholder="$t('请输入企业注册地址')" />
            </div>
            <div v-if="registrationAddressError" class="field-error">{{ $t(registrationAddressError || '') }}</div>
        </div>

        <!-- 营业执照 -->
        <div class="form-field">
            <label class="field-label">
                {{ $t('营业执照') }} <span class="required">*</span>
            </label>
            <div class="upload-wrapper">
                <UploadImages v-model="businessLicenseValue" :placeholder="$t('点击上传营业执照')"
                    :hint="$t('支持png / jpg / jpeg图片格式 (大小5M以内)')" :alt="$t('营业执照')" />
            </div>
        </div>

        <div class="step-actions">
            <button type="button" class="btn-prev" @click="$emit('prev')">{{ $t('上一步') }}</button>
            <button type="button" class="btn-next" @click="$emit('next')" :disabled="!canProceed">
                {{ $t('下一步') }}
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { checkDuplicateCompanyName } from '~/services/userms.service'
import type { CompanyFormData } from '~/types/form'
import type { CompanyInfoFormProps } from '~/types/component'

type Props = CompanyInfoFormProps

const props = defineProps<Props>()

const emit = defineEmits<{
    'update:formData': [data: CompanyFormData]
    'next': []
    'prev': []
}>()

const updateField = (field: keyof CompanyFormData, value: string) => {
    emit('update:formData', {
        ...props.formData,
        [field]: value
    })
}

// 公司名称重复检查
const companyNameError = ref<string | null>(null)
const checkingCompanyName = ref(false)

// 税号格式验证
const taxIdError = ref<string | null>(null)
const validTaxIdLengths = [15, 17, 18, 20] // 有效的税号长度

// 注册地址验证
const registrationAddressError = ref<string | null>(null)

// 检查公司名称是否重复
const checkCompanyNameDuplicate = async (companyName: string) => {
    const trimmedName = companyName?.trim() || ''

    if (!trimmedName) {
        companyNameError.value = null
        return
    }

    checkingCompanyName.value = true
    companyNameError.value = null

    try {
        const response = await checkDuplicateCompanyName({
            companyName: trimmedName
        })

        if (response.code === 1 && response.data === 1) {
            companyNameError.value = '公司名称已被申请'
        } else {
            companyNameError.value = null
        }
    } catch (err: any) {
        console.error('检查公司名称失败:', err)
        // 检查失败时不显示错误，避免影响用户体验
        companyNameError.value = null
    } finally {
        checkingCompanyName.value = false
    }
}

// 处理公司名称输入
const handleCompanyNameInput = (e: Event) => {
    const value = (e.target as HTMLInputElement).value
    updateField('companyName', value)
    // 输入时清除错误提示
    if (!value || !value.trim()) {
        companyNameError.value = null
    }
}

// 失焦时检查（输入完成后）
const handleCompanyNameBlur = () => {
    const companyName = props.formData.companyName?.trim() || ''

    if (!companyName) {
        companyNameError.value = null
        return
    }

    // 输入完成后立即执行检查
    checkCompanyNameDuplicate(companyName)
}

// 验证税号格式（只允许大小写字母和数字）
const validateTaxId = (taxId: string): string | null => {
    const trimmedTaxId = taxId?.trim() || ''
    
    if (!trimmedTaxId) {
        return null // 空值时不验证，由必填验证处理
    }

    // 验证是否只包含大小写字母和数字
    if (!/^[0-9a-zA-Z]+$/.test(trimmedTaxId)) {
        return '税号只能输入字母和数字'
    }

    const length = trimmedTaxId.length
    
    if (!validTaxIdLengths.includes(length)) {
        return `税号长度不正确，应为${validTaxIdLengths.join('、')}位`
    }

    return null
}

// 处理税号输入
const handleTaxIdInput = (e: Event) => {
    // 只允许用户输入大小写字母或数字
    const input = e.target as HTMLInputElement
    // 过滤掉所有非字母/数字字符，并限制最大长度避免超出校验范围
    const value = input.value.replace(/[^0-9a-zA-Z]/g, '').slice(0, Math.max(...validTaxIdLengths))
    // 更新输入框的值
    input.value = value
    updateField('taxId', value)
    // 输入时清除错误提示（允许用户继续输入）
    // 只有在失焦时才进行完整验证
    if (!value || !value.trim()) {
        taxIdError.value = null
    } else {
        // 如果输入了内容，但还在输入过程中，暂时清除错误
        // 让用户能够继续输入，直到失焦时再进行完整验证
        taxIdError.value = null
    }
}

// 失焦时验证税号（输入完成后）
const handleTaxIdBlur = () => {
    const taxId = props.formData.taxId?.trim() || ''
    
    if (!taxId) {
        taxIdError.value = null
        return
    }

    // 输入完成后立即验证
    const error = validateTaxId(taxId)
    taxIdError.value = error
}

// 验证注册地址格式
const validateRegistrationAddress = (address: string): string | null => {
    const trimmedAddress = address?.trim() || ''
    
    if (!trimmedAddress) {
        return '注册地址不能为空'
    }

    // 可以添加更多验证规则，比如长度限制等
    if (trimmedAddress.length < 5) {
        return '注册地址至少需要5个字符'
    }

    if (trimmedAddress.length > 200) {
        return '注册地址不能超过200个字符'
    }

    return null
}

// 处理注册地址输入
const handleRegistrationAddressInput = (e: Event) => {
    const value = (e.target as HTMLInputElement).value
    updateField('registrationAddress', value)
    // 输入时清除错误提示
    if (!value || !value.trim()) {
        registrationAddressError.value = null
    } else {
        // 输入时清除错误，允许用户继续输入
        registrationAddressError.value = null
    }
}

// 失焦时验证注册地址（输入完成后）
const handleRegistrationAddressBlur = () => {
    const address = props.formData.registrationAddress?.trim() || ''
    
    if (!address) {
        registrationAddressError.value = '注册地址不能为空'
        return
    }

    // 输入完成后立即验证
    const error = validateRegistrationAddress(address)
    registrationAddressError.value = error
}

// 营业执照的双向绑定
const businessLicenseValue = computed({
    get: () => props.formData.businessLicense,
    set: (value: string) => {
        updateField('businessLicense', value)
    }
})

const isNotEmpty = (value: string) => value.trim() !== ''

const canProceed = computed(() => {
    const hasCompanyName = isNotEmpty(props.formData.companyName)
    const hasTaxId = isNotEmpty(props.formData.taxId)
    const hasRegistrationAddress = isNotEmpty(props.formData.registrationAddress)
    const hasBusinessLicense = props.formData.businessLicense !== ''
    
    // 所有必填字段都已填写
    const allFieldsFilled = hasCompanyName && hasTaxId && hasRegistrationAddress && hasBusinessLicense
    
    // 没有验证错误（包括公司名称重复检查、税号格式验证和注册地址验证）
    const noErrors = !companyNameError.value && !taxIdError.value && !registrationAddressError.value
    
    // 如果正在检查公司名称，暂时不允许继续
    const notChecking = !checkingCompanyName.value
    
    return allFieldsFilled && noErrors && notChecking
})
</script>

<style lang="scss" scoped>
.step-content {
    .step-title {
        font-size: 24px;
        font-weight: 600;
        color: #333;
        margin-bottom: 10px;
        text-align: center;
    }
}

.form-field {
    margin-bottom: 24px;

    .field-label {
        display: block;
        font-size: 14px;
        font-weight: 500;
        color: #333;
        margin-bottom: 8px;

        .required {
            color: #f44336;
        }
    }

    .input-wrapper {
        position: relative;
        display: flex;
        align-items: center;

        .input-icon {
            position: absolute;
            left: 12px;
            font-size: 18px;
            color: #999;
            z-index: 1;
        }

        .form-input {
            width: 100%;
            height: 48px;
            padding: 0 16px 0 40px;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            font-size: 14px;
            transition: all 0.3s ease;

            &:focus {
                outline: none;
                border-color: var(--color);
                box-shadow: 0 0 0 2px rgba(76, 175, 80, 0.1);
            }

            &.input-error {
                border-color: #f44336;

                &:focus {
                    border-color: #f44336;
                    box-shadow: 0 0 0 2px rgba(244, 67, 54, 0.1);
                }
            }

            &::placeholder {
                color: #bbb;
            }
        }
    }

    .upload-wrapper {
        width: 100%;
    }

    .field-hint {
        margin-top: 8px;
        padding: 10px 12px;
        background-color: #f5f7fa;
        border-left: 3px solid var(--color, #4caf50);
        border-radius: 4px;
        font-size: 12px;
        line-height: 1.6;
        color: #666;
        word-break: break-word;
        transition: all 0.3s ease;

        &:hover {
            background-color: #f0f4f8;
        }

        @media (max-width: 768px) {
            font-size: 11px;
            padding: 8px 10px;
            line-height: 1.5;
        }
    }

    .field-error {
        margin-top: 6px;
        font-size: 12px;
        color: #f44336;
        line-height: 1.5;
        display: flex;
        align-items: center;
        gap: 4px;

        &::before {
            content: '⚠️';
            font-size: 14px;
        }
    }
}

.step-actions {
    display: flex;
    justify-content: space-between;
    gap: 16px;
    margin-top: 40px;

    @media (max-width: 768px) {
        flex-direction: column;
    }

    button {
        flex: 1;
        height: 48px;
        border-radius: 6px;
        font-size: 16px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        border: none;

        @media (max-width: 768px) {
            width: 100%;
        }

        &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
    }

    .btn-prev {
        background-color: var(--color);
        color: white;

        &:hover:not(:disabled) {
            background-color: #d32f2f;
            box-shadow: 0 2px 8px rgba(244, 67, 54, 0.3);
        }
    }

    .btn-next {
        background-color: var(--color);
        color: white;

        &:hover:not(:disabled) {
            background-color: #45a049;
            box-shadow: 0 2px 8px rgba(76, 175, 80, 0.3);
        }

        &:disabled {
            background-color: #e0e0e0;
            color: #999;
        }
    }
}
</style>
