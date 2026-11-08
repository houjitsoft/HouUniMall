<template>
    <div class="modal" v-if="visible" @click="handleClose">
        <div class="modal-content" @click.stop>
            <div class="modal-header">
                <div class="modal-title">{{ $t(title || '') }}</div>
                <div class="modal-close" @click="handleClose">×</div>
            </div>
            <div class="modal-body">
                <div class="password-input">
                    <div class="password-wrapper" @click="focusInput">
                        <input ref="passwordInputRef" :type="showPassword ? 'text' : 'password'" :value="password"
                            :placeholder="placeholder" maxlength="6" class="password-field" autocomplete="off"
                            inputmode="numeric" @input="onPasswordInput" />
                        <i class="password-toggle" :class="{ 'eye-open': showPassword }"
                            @click.stop="togglePasswordVisibility">
                            {{ showPassword ? '👁️' : '👁️‍🗨️' }}
                        </i>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button class="cancel-btn" @click="handleClose">{{ $t('取消') }}</button>
                <button class="confirm-btn" @click="handleConfirm">{{ $t('确认支付') }}</button>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { PasswordInputModalProps, PasswordInputModalEmits } from '~/types/payment'
import { Toast } from '~/composables/useToast'

const props = defineProps<PasswordInputModalProps>()
const emit = defineEmits<PasswordInputModalEmits>()

const password = ref('')
const showPassword = ref(false)
const passwordInputRef = ref<HTMLInputElement | null>(null)

/** 仅允许输入数字（0-9），最多 6 位 */
const onPasswordInput = (e: Event) => {
    const el = e.target instanceof HTMLInputElement ? e.target : null
    if (!el) return
    const next = el.value.replace(/\D/g, '').slice(0, 6)
    password.value = next
    el.value = next
}

const focusInput = () => {
    passwordInputRef.value?.focus()
}

const togglePasswordVisibility = () => {
    showPassword.value = !showPassword.value
}

const handleClose = () => {
    password.value = ''
    showPassword.value = false
    emit('close')
}

const handleConfirm = () => {
    if (!password.value) {
        Toast.warning('请输入密码')
        return
    }
    emit('confirm', password.value)
    password.value = ''
    showPassword.value = false
}
</script>

<style lang="scss" scoped>
/* 弹窗样式 */
.modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 1000;
}

.modal-content {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
    width: 400px;
    max-width: 90%;
    overflow: hidden;
}

.modal-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 15px 20px;
    border-bottom: 1px solid #e0e0e0;

    .modal-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
    }

    .modal-close {
        width: 24px;
        height: 24px;
        display: flex;
        align-items: center;
        justify-content: center;
        cursor: pointer;
        color: #999;
        font-size: 20px;
        line-height: 1;

        &:hover {
            color: #333;
        }
    }
}

.modal-body {
    padding: 20px;

    .password-input {
        margin-bottom: 15px;

        .password-wrapper {
            width: 100%;
            display: flex;
            position: relative;
            align-items: center;
        }

        .password-field {
            width: 100%;
            padding: 12px;
            padding-right: 48px;
            border: 1px solid #e0e0e0;
            border-radius: 4px;
            font-size: 16px;
            box-sizing: border-box;

            &:focus {
                outline: none;
                border-color: var(--color);
                box-shadow: 0 0 0 2px rgba(255, 77, 79, 0.1);
            }
        }

        .password-toggle {
            position: absolute;
            right: 14px;
            top: 50%;
            transform: translateY(-50%);
            cursor: pointer;
            user-select: none;
            font-style: normal;
            color: #999;
            font-size: 16px;

            &.eye-open {
                color: var(--color);
            }
        }
    }
}

.modal-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
    padding: 15px 20px;
    border-top: 1px solid #e0e0e0;

    .cancel-btn {
        padding: 8px 16px;
        background-color: #fff;
        color: #666;
        border: 1px solid #e0e0e0;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
            background-color: #f5f5f5;
            border-color: #d9d9d9;
        }
    }

    .confirm-btn {
        padding: 8px 16px;
        background-color: var(--color);
        color: #fff;
        border: none;
        border-radius: 4px;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.3s;

        &:hover {
            background-color: #ff7875;
        }
    }
}
</style>
