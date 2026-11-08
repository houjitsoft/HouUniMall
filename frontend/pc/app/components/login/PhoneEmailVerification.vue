<template>
    <div class="step-content">
        <!-- 账号类型选择 -->
        <div class="form-field">
            <label class="field-label">{{ $t('账号类型') }}</label>
            <div class="account-type-options">
                <label class="radio-option" :class="{ active: formData.accountType === 'phone' }">
                    <input type="radio" :checked="formData.accountType === 'phone'"
                        @change="updateField('accountType', 'phone')" class="radio-input" />
                    <span class="radio-label">{{ $t('手机号注册') }}</span>
                </label>
                <label class="radio-option" :class="{ active: formData.accountType === 'email' }">
                    <input type="radio" :checked="formData.accountType === 'email'"
                        @change="updateField('accountType', 'email')" class="radio-input" />
                    <span class="radio-label">{{ $t('邮箱注册') }}</span>
                </label>
            </div>
        </div>

        <!-- 手机号注册 -->
        <template v-if="formData.accountType === 'phone'">
            <!-- 手机号 -->
            <div class="form-field">
                <label class="field-label">
                    {{ $t('手机号') }} <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                    <i class="input-icon">📞</i>
                    <input :value="formData.phone"
                        @input="(e) => onPhoneInput((e.target as HTMLInputElement).value)"
                        @blur="checkPhoneDuplicate" type="tel"
                        class="form-input" :placeholder="$t('请填写手机号')" />
                </div>
                <p v-if="phoneExistError" class="field-error">{{ phoneExistError }}</p>
            </div>

            <!-- 国家/地区 -->
            <div class="form-field">
                <label class="field-label">
                    {{ $t('国家/地区') }} <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                    <i class="input-icon">🌐</i>
                    <div class="country-select" @click="$emit('showAreaCode')">
                        <span>{{ $t(selectedCountry.name || '') }} {{ $t(selectedCountry.code || '') }}</span>
                        <i class="dropdown-icon">▼</i>
                    </div>
                </div>
            </div>

            <!-- 验证码 -->
            <div class="form-field">
                <label class="field-label">
                    {{ $t('验证码') }} <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                    <i class="input-icon">🛡️</i>
                    <input :value="formData.verificationCode"
                        @input="(e) => updateField('verificationCode', (e.target as HTMLInputElement).value)"
                        type="text" class="form-input code-input" :placeholder="$t('请输入验证码')" />
                    <button type="button" class="get-code-btn-red"
                        :class="{ disabled: countdown > 0 || sendingPhoneCode || !!phoneExistError || checkingPhone, loading: sendingPhoneCode }"
                        :disabled="countdown > 0 || sendingPhoneCode || !!phoneExistError || checkingPhone" @click="handleGetVerificationCode">
                        <span v-if="sendingPhoneCode" class="get-code-btn-inner">
                            <i class="get-code-spinner" aria-hidden="true" />
                            {{ $t('发送中...') }}
                        </span>
                        <span v-else>{{ $t(getCountdownText(countdown, '获取验证码')) }}</span>
                    </button>
                </div>
            </div>
        </template>

        <!-- 邮箱注册 -->
        <template v-if="formData.accountType === 'email'">
            <!-- 邮箱 -->
            <div class="form-field">
                <label class="field-label">
                    {{ $t('邮箱') }} <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                    <i class="input-icon">📧</i>
                    <input :value="formData.email"
                        @input="(e) => updateField('email', (e.target as HTMLInputElement).value)" type="email"
                        class="form-input" :placeholder="$t('请填写邮箱')" />
                </div>
            </div>

            <!-- 邮箱验证码 -->
            <div class="form-field">
                <label class="field-label">
                    {{ $t('验证码') }} <span class="required">*</span>
                </label>
                <div class="input-wrapper">
                    <i class="input-icon">🛡️</i>
                    <input :value="formData.emailCode"
                        @input="(e) => updateField('emailCode', (e.target as HTMLInputElement).value)" type="text"
                        class="form-input code-input" :placeholder="$t('请输入验证码')" />
                    <button type="button" class="get-code-btn-red"
                        :class="{ disabled: emailCountdown > 0 || sendingEmailCode, loading: sendingEmailCode }"
                        :disabled="emailCountdown > 0 || sendingEmailCode" @click="handleGetEmailCode">
                        <span v-if="sendingEmailCode" class="get-code-btn-inner">
                            <i class="get-code-spinner" aria-hidden="true" />
                            {{ $t('发送中...') }}
                        </span>
                        <span v-else>{{ $t(getCountdownText(emailCountdown, '获取验证码')) }}</span>
                    </button>
                </div>
            </div>
        </template>

        <!-- 密码 -->
        <PasswordInput :modelValue="formData.password" @update:modelValue="updateField('password', $event)"
            @validation-change="handlePasswordValidationChange" />

        <div class="step-actions">
            <button type="button" class="btn-prev" @click="$emit('prev')">{{ $t('上一步') }}</button>
            <button type="button" :class="props.submitButtonAction === 'submit' ? 'btn-submit' : 'btn-next'"
                @click="handleSubmit"
                :disabled="!canProceed || (props.submitButtonAction === 'submit' && props.isSubmitting)">
                {{ props.submitButtonAction === 'submit' && props.isSubmitting ? $t('注册中...') :
                    $t(props.submitButtonText) }}
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { sendCode, checkPhoneAvailable } from '~/services/thirdParty.service'
import { Toast } from '~/composables/useToast'
import PasswordInput from './PasswordInput.vue'
import type { PhoneEmailFormData, CountryInfo } from '~/types/form'
import type { PhoneEmailVerificationProps } from '~/types/component'

type Props = PhoneEmailVerificationProps

const props = withDefaults(defineProps<Props>(), {
    submitButtonText: '下一步',
    submitButtonAction: 'next',
    isSubmitting: false
})

const emit = defineEmits<{
    'update:formData': [data: PhoneEmailFormData]
    'showAreaCode': []
    'getVerificationCode': []
    'getEmailCode': []
    'next': []
    'prev': []
    'submit': []
}>()

const updateField = (field: keyof PhoneEmailFormData, value: string) => {
    emit('update:formData', {
        ...props.formData,
        [field]: value
    })
}

const getCountdownText = (countdown: number, defaultText: string) => {
    return countdown > 0 ? `${countdown}秒后重新获取` : defaultText
}

const isNotEmpty = (value: string) => value.trim() !== ''

// 获取验证码：请求中（防重复点击 + 加载展示）
const sendingPhoneCode = ref(false)
const sendingEmailCode = ref(false)
const phoneExistError = ref('')
const checkingPhone = ref(false)

// 密码验证状态
const isPasswordValid = ref(false)

// 处理密码验证状态变化
const handlePasswordValidationChange = (isValid: boolean) => {
    isPasswordValid.value = isValid
}

const onPhoneInput = (value: string) => {
    if (phoneExistError.value) {
        phoneExistError.value = ''
    }
    updateField('phone', value)
}

/** 校验手机号是否已注册；已注册返回 false */
const ensurePhoneNotRegistered = async (phone: string, showToast = true): Promise<boolean> => {
    checkingPhone.value = true
    try {
        const response = await checkPhoneAvailable(phone)
        if (response.code === 1) {
            phoneExistError.value = ''
            return true
        }
        const msg = response.message || '该手机号已被注册'
        phoneExistError.value = msg
        if (showToast) {
            Toast.error(msg)
        }
        return false
    } catch (error: any) {
        console.error('检查手机号失败:', error)
        // 校验服务异常时不阻断发送，由后端 send-code 再兜底
        return true
    } finally {
        checkingPhone.value = false
    }
}

const checkPhoneDuplicate = async () => {
    const phone = props.formData.phone?.trim()
    if (!phone || phone.length < 6) {
        phoneExistError.value = ''
        return
    }
    await ensurePhoneNotRegistered(phone, false)
}

// 发送手机验证码
const handleGetVerificationCode = async () => {
    if (props.countdown > 0 || sendingPhoneCode.value || checkingPhone.value) return

    const phone = props.formData.phone?.trim()
    if (!phone) {
        Toast.warning('请先填写手机号')
        return
    }

    const available = await ensurePhoneNotRegistered(phone, true)
    if (!available) return

    sendingPhoneCode.value = true
    try {
        const response = await sendCode({
            userMobile: phone,
            interDialingCode: props.selectedCountry.code,
            type: 1,
            scene: 'register'
        })

        if (response.code === 1) {
            // 发送成功，显示 API 返回的成功消息（如果有）
            if (response.message) {
                Toast.success(response.message)
            }
            // 触发父组件的倒计时
            emit('getVerificationCode')
        } else {
            const msg = response.message || '发送验证码失败，请重试'
            if (/已注册|已经注册/.test(msg)) {
                phoneExistError.value = msg
            }
            // 使用 API 返回的错误消息
            Toast.error(msg)
        }
    } catch (error: any) {
        console.error('发送验证码失败:', error)
        // 使用错误对象中的 message（可能是 API 返回的错误信息）
        Toast.error(error.message || error.response?.data?.message || '发送验证码失败，请重试')
    } finally {
        sendingPhoneCode.value = false
    }
}

// 发送邮箱验证码
const handleGetEmailCode = async () => {
    if (props.emailCountdown > 0 || sendingEmailCode.value) return

    const email = props.formData.email?.trim()
    if (!email) {
        Toast.warning('请先填写邮箱')
        return
    }

    // 简单的邮箱格式验证
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!emailRegex.test(email)) {
        Toast.warning('请输入正确的邮箱地址')
        return
    }

    sendingEmailCode.value = true
    try {
        const response = await sendCode({
            userMobile: email,
            interDialingCode: '+86', // 邮箱时可以为空或默认值
            type: 2,
            scene: 'register'
        })

        if (response.code === 1) {
            // 发送成功，显示 API 返回的成功消息（如果有）
            if (response.message) {
                Toast.success(response.message)
            }
            // 触发父组件的倒计时
            emit('getEmailCode')
        } else {
            // 使用 API 返回的错误消息
            Toast.error(response.message || '发送验证码失败，请重试')
        }
    } catch (error: any) {
        console.error('发送邮箱验证码失败:', error)
        // 使用错误对象中的 message（可能是 API 返回的错误信息）
        Toast.error(error.message || error.response?.data?.message || '发送验证码失败，请重试')
    } finally {
        sendingEmailCode.value = false
    }
}

const canProceed = computed(() => {
    if (props.formData.accountType === 'phone') {
        return isNotEmpty(props.formData.phone) &&
            !phoneExistError.value &&
            isNotEmpty(props.formData.verificationCode) &&
            isNotEmpty(props.formData.password) &&
            isPasswordValid.value
    } else {
        return isNotEmpty(props.formData.email) &&
            isNotEmpty(props.formData.emailCode) &&
            isNotEmpty(props.formData.password) &&
            isPasswordValid.value
    }
})

// 处理提交按钮点击
const handleSubmit = async () => {
    if (!canProceed.value) return

    if (props.formData.accountType === 'phone') {
        const phone = props.formData.phone?.trim()
        if (!phone) return
        const available = await ensurePhoneNotRegistered(phone, true)
        if (!available) return
    }

    if (props.submitButtonAction === 'submit') {
        emit('submit')
    } else {
        emit('next')
    }
}
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

    .field-error {
        margin: 8px 0 0;
        padding: 8px 12px;
        font-size: 13px;
        line-height: 1.4;
        color: #cf1322;
        background: #fff1f0;
        border-left: 3px solid #ff4d4f;
        border-radius: 2px;
    }

    // 账号类型选择
    .account-type-options {
        display: flex;
        gap: 24px;

        .radio-option {
            display: flex;
            align-items: center;
            cursor: pointer;
            user-select: none;

            .radio-input {
                width: 18px;
                height: 18px;
                margin-right: 8px;
                cursor: pointer;
                accent-color: var(--color);
            }

            .radio-label {
                font-size: 14px;
                color: #333;
            }

            &.active {
                .radio-label {
                    color: var(--color);
                    font-weight: 500;
                }
            }
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
                box-shadow: 0 0 0 2px color-mix(in srgb, var(--color) 18%, transparent);
            }

            &::placeholder {
                color: #bbb;
            }
        }

        // 验证码输入框（有按钮时增加右侧 padding）
        .code-input {
            padding-right: 120px;
        }

        // 国家/地区选择器
        .country-select {
            width: 100%;
            height: 48px;
            padding: 0 16px 0 40px;
            border: 1px solid #e0e0e0;
            border-radius: 6px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            cursor: pointer;
            background-color: #fff;
            transition: all 0.3s ease;

            &:hover {
                border-color: var(--color);
            }

            span {
                font-size: 14px;
                color: #333;
            }

            .dropdown-icon {
                font-size: 12px;
                color: #999;
            }
        }

        // 获取验证码按钮（使用主题色）
        .get-code-btn-red {
            position: absolute;
            right: 12px;
            padding: 8px 12px;
            min-width: 96px;
            background-color: var(--color);
            color: white;
            border: none;
            border-radius: 4px;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.3s ease;
            z-index: 2;
            display: inline-flex;
            align-items: center;
            justify-content: center;

            .get-code-btn-inner {
                display: inline-flex;
                align-items: center;
                gap: 6px;
            }

            .get-code-spinner {
                display: inline-block;
                width: 14px;
                height: 14px;
                border: 2px solid rgba(255, 255, 255, 0.35);
                border-top-color: #fff;
                border-radius: 50%;
                animation: get-code-spin 0.65s linear infinite;
                flex-shrink: 0;
            }

            &:hover:not(:disabled):not(.disabled) {
                background-color: color-mix(in srgb, var(--color) 85%, black);
                box-shadow: 0 2px 4px color-mix(in srgb, var(--color) 30%, transparent);
            }

            &.disabled,
            &:disabled {
                background-color: #ccc;
                color: #fff;
                cursor: not-allowed;
                box-shadow: none;
            }

            &.loading:disabled {
                color: #555;
            }

            &.loading:disabled .get-code-spinner {
                border-color: rgba(0, 0, 0, 0.12);
                border-top-color: rgba(0, 0, 0, 0.5);
            }
        }

        @keyframes get-code-spin {
            to {
                transform: rotate(360deg);
            }
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
            background-color: color-mix(in srgb, var(--color) 85%, black);
            box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
        }
    }

    .btn-next {
        background-color: var(--color);
        color: white;

        &:hover:not(:disabled) {
            background-color: color-mix(in srgb, var(--color) 85%, black);
            box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
        }

        &:disabled {
            background-color: #e0e0e0;
            color: #999;
        }
    }

    .btn-submit {
        background-color: var(--color);
        color: white;

        &:hover:not(:disabled) {
            background-color: color-mix(in srgb, var(--color) 85%, black);
            box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
        }

        &:disabled {
            background-color: #ccc;
            cursor: not-allowed;
        }
    }
}
</style>
