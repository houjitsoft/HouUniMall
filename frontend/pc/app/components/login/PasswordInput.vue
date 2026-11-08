<template>
    <div class="form-field">
        <label class="field-label">
            {{ $t(fieldLabel) }} <span class="required">*</span>
        </label>
        <div class="input-wrapper">
            <i class="input-icon">🔒</i>
            <input :value="modelValue" @input="handleInput" @blur="handleBlur"
                :type="showPassword ? 'text' : 'password'" class="form-input password-input"
                :class="{ 'input-error': passwordError }" :placeholder="$t(passwordDesc) || $t('请输入密码')"
                autocomplete="new-password" />
            <i class="password-toggle" @click="togglePassword">
                {{ showPassword ? '👁️' : '👁️‍🗨️' }}
            </i>
        </div>
        <div v-if="passwordDesc && !passwordError" class="field-hint">
            {{ $t(passwordDesc) }}
        </div>
        <div v-if="passwordError" class="field-error">
            {{ $t(passwordError) }}
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { queryPasswordConfig } from '~/services/base.service'
import type { PasswordConfig } from '~/types/base'
import { validatePassword } from '~/utils/password'
import type { PasswordInputProps } from '~/types/component'

type Props = PasswordInputProps

const props = withDefaults(defineProps<Props>(), {
    fieldLabel: '密码'
})

const emit = defineEmits<{
    'update:modelValue': [value: string]
    'validation-change': [isValid: boolean]
}>()

// 密码显示/隐藏
const showPassword = ref(false)

// 密码规则配置
const passwordConfig = ref<PasswordConfig | null>(null)
const loading = ref(false)
const passwordError = ref<string | null>(null)

// 密码规则描述
const passwordDesc = computed(() => {
    return passwordConfig.value?.desc || ''
})

// 切换密码显示/隐藏
const togglePassword = () => {
    showPassword.value = !showPassword.value
}

// 处理输入
const handleInput = (e: Event) => {
    const value = (e.target as HTMLInputElement).value
    emit('update:modelValue', value)
    // 输入时清除错误提示，允许用户继续输入
    passwordError.value = null
    // 如果输入为空，验证状态为 false
    if (!value || !value.trim()) {
        emit('validation-change', false)
    }
}

// 处理失焦验证
const handleBlur = () => {
    const password = props.modelValue?.trim() || ''

    if (!password) {
        passwordError.value = null
        emit('validation-change', false)
        return
    }

    // 如果还没有加载配置，先加载
    if (!passwordConfig.value) {
        loadPasswordConfig().then(() => {
            validatePasswordInput(password)
        })
    } else {
        validatePasswordInput(password)
    }
}

// 验证密码
const validatePasswordInput = (password: string) => {
    if (!passwordConfig.value) {
        return
    }

    const result = validatePassword(
        password,
        passwordConfig.value.passwordFormat,
        passwordConfig.value.passwordLength,
        passwordConfig.value.desc
    )

    passwordError.value = result.valid ? null : result.message
    emit('validation-change', result.valid)
}

// 加载密码规则配置
const loadPasswordConfig = async () => {
    if (loading.value || passwordConfig.value) {
        return
    }

    loading.value = true
    try {
        const response = await queryPasswordConfig({})
        if (response.code === 1 && response.data) {
            passwordConfig.value = response.data
        }
    } catch (error: any) {
        console.error('加载密码规则配置失败:', error)
    } finally {
        loading.value = false
    }
}

// 暴露验证方法供父组件调用
const validate = () => {
    const password = props.modelValue?.trim() || ''
    if (!password) {
        passwordError.value = '请输入密码'
        emit('validation-change', false)
        return false
    }

    if (!passwordConfig.value) {
        // 如果配置未加载，先加载再验证
        loadPasswordConfig().then(() => {
            validatePasswordInput(password)
        })
        return false
    }

    validatePasswordInput(password)
    return !passwordError.value
}

// 暴露方法
defineExpose({
    validate
})

// 挂载后加载配置，并对已有密码同步校验（避免父级步骤切换卸载再挂载时下一步按钮一直禁用）
onMounted(() => {
    loadPasswordConfig().then(() => {
        const password = props.modelValue?.trim() || ''
        if (!password) {
            emit('validation-change', false)
            return
        }
        if (passwordConfig.value) {
            validatePasswordInput(password)
        }
    })
})
</script>

<style lang="scss" scoped>
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
                border-color: var(--color, #4caf50);
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

        // 密码输入框（有显示/隐藏按钮时增加右侧 padding）
        .password-input {
            padding-right: 50px;
        }

        // 密码显示/隐藏切换
        .password-toggle {
            position: absolute;
            right: 12px;
            font-size: 18px;
            color: #999;
            cursor: pointer;
            z-index: 2;
            transition: all 0.2s ease;

            &:hover {
                color: #666;
            }
        }
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
</style>
