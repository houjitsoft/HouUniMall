<template>
    <div class="step-content">
        <h2 class="step-title">{{ $t('选择用户类型') }}</h2>
        <div class="user-type-options" :class="{ 'is-single': visibleOptionCount === 1 }">
            <div v-if="showEnterprise" class="user-type-card" :class="{ active: modelValue === 'enterprise' }"
                @click="$emit('update:modelValue', 'enterprise')">
                <div class="type-icon">🏢</div>
                <div class="type-title">{{ $t('企业用户') }}</div>
                <div class="type-desc">{{ $t('适用于企业、公司等组织') }}</div>
            </div>
            <div v-if="showIndividual" class="user-type-card" :class="{ active: modelValue === 'individual' }"
                @click="$emit('update:modelValue', 'individual')">
                <div class="type-icon">👤</div>
                <div class="type-title">{{ $t('个人用户') }}</div>
                <div class="type-desc">{{ $t('适用于个人商户') }}</div>
            </div>
        </div>

        <!-- 协议同意 -->
        <div class="agreement-section">
            <label class="agreement-checkbox">
                <input type="checkbox" :checked="agreedToTerms"
                    @change="$emit('update:agreedToTerms', ($event.target as HTMLInputElement).checked)"
                    class="checkbox-input" />
                <span class="agreement-text">
                    {{ $t('我已阅读并同意') }}
                    <!-- 企业用户显示企业注册协议，个人用户显示用户注册协议 -->
                    <a href="#" class="agreement-link"
                        @click.prevent="$emit('openAgreement', modelValue === 'enterprise' ? 'enterprise' : 'user')">
                        《{{ modelValue === 'enterprise' ? $t('企业注册协议') : $t('用户注册协议') }}》
                    </a>
                    {{ $t('与') }}
                    <a href="#" class="agreement-link" @click.prevent="$emit('openAgreement', 'privacy')">
                        《{{ $t('隐私协议') }}》
                    </a>
                </span>
            </label>
        </div>

        <div class="step-actions">
            <button type="button" class="btn-next" @click="$emit('next')" :disabled="!modelValue || !agreedToTerms">
                {{ $t('下一步') }}
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { UserTypeSelectionProps } from '~/types/component'

type Props = UserTypeSelectionProps

const props = defineProps<Props>()

const visibleOptionCount = computed(() => {
    const enterprise = (props as any).showEnterprise !== false
    const individual = (props as any).showIndividual !== false
    return Number(enterprise) + Number(individual)
})

defineEmits<{
    'update:modelValue': [value: 'enterprise' | 'individual' | '']
    'update:agreedToTerms': [value: boolean]
    'next': []
    'openAgreement': [type: string]
}>()
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

.user-type-options {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 30px;

    @media (max-width: 768px) {
        grid-template-columns: 1fr;
    }

    &.is-single {
        grid-template-columns: 1fr;
        justify-items: center;

        .user-type-card {
            width: 100%;
            max-width: 420px;
        }
    }
}

.user-type-card {
    border: 2px solid #e0e0e0;
    border-radius: 8px;
    padding: 30px;
    text-align: center;
    cursor: pointer;
    transition: all 0.3s ease;
    background-color: #fff;

    &:hover {
        border-color: var(--color);
        box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 18%, transparent);
        transform: translateY(-2px);
    }

    &.active {
        border-color: var(--color);
        background-color: color-mix(in srgb, var(--color) 8%, #fff);
        box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 22%, transparent);
    }

    .type-icon {
        font-size: 48px;
        margin-bottom: 12px;
        transition: transform 0.3s ease;
    }

    &:hover .type-icon {
        transform: scale(1.1);
    }

    .type-title {
        font-size: 18px;
        font-weight: 600;
        color: #333;
        margin-bottom: 8px;
    }

    .type-desc {
        font-size: 14px;
        color: #999;
        line-height: 1.5;
    }
}

.agreement-section {
    display: flex;
    justify-content: center;
    margin-bottom: 30px;
    padding: 0 10px;

    @media (max-width: 768px) {
        margin-bottom: 20px;
        padding: 0;
    }
}

.agreement-checkbox {
    display: inline-flex;
    align-items: center;
    justify-content: flex-start;
    padding: 14px 20px;
    border: 1px solid color-mix(in srgb, var(--color) 25%, #fff);
    border-radius: 8px;
    background-color: color-mix(in srgb, var(--color) 4%, #fff);
    cursor: pointer;
    transition: all 0.3s ease;
    user-select: none;
    max-width: 100%;

    @media (max-width: 768px) {
        padding: 12px 16px;
        flex-wrap: wrap;
        width: 100%;
        max-width: 100%;
    }

    &:hover {
        border-color: color-mix(in srgb, var(--color) 40%, #fff);
        background-color: color-mix(in srgb, var(--color) 6%, #fff);
        box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 10%, transparent);
    }

    .checkbox-input {
        margin-right: 10px;
        width: 18px;
        height: 18px;
        cursor: pointer;
        accent-color: var(--color);
        flex-shrink: 0;
        margin-top: 2px;

        @media (max-width: 768px) {
            margin-right: 8px;
        }

        &:checked {
            accent-color: var(--color);
        }
    }

    &:has(.checkbox-input:checked) {
        border-color: var(--color);
        background-color: color-mix(in srgb, var(--color) 6%, #fff);
        box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 12%, transparent);
    }

    .agreement-text {
        font-size: 14px;
        color: #555;
        line-height: 1.6;
        display: inline;
        word-break: keep-all;

        @media (max-width: 768px) {
            font-size: 13px;
            line-height: 1.6;
            text-align: center;
            justify-content: center;
        }

        .agreement-link {
            color: var(--color);
            text-decoration: underline;
            text-decoration-color: color-mix(in srgb, var(--color) 50%, transparent);
            text-underline-offset: 2px;
            transition: all 0.2s ease;
            cursor: pointer;
            white-space: nowrap;

            &:hover {
                color: var(--color);
                text-decoration-color: var(--color);
            }

            &:active {
                color: color-mix(in srgb, var(--color) 75%, black);
            }
        }
    }
}

.step-actions {
    display: flex;
    justify-content: center;
    gap: 16px;
    margin-top: 40px;

    @media (max-width: 768px) {
        flex-direction: column;
    }

    button {
        min-width: 200px;
        height: 48px;
        border-radius: 6px;
        font-size: 16px;
        font-weight: 500;
        cursor: pointer;
        transition: all 0.3s ease;
        border: none;

        @media (max-width: 768px) {
            width: 100%;
            min-width: unset;
        }

        &:disabled {
            opacity: 0.5;
            cursor: not-allowed;
        }
    }

    .btn-next {
        background-color: var(--color);
        color: white;

        &:hover:not(:disabled) {
            background-color: color-mix(in srgb, var(--color) 85%, black);
            box-shadow: 0 4px 12px color-mix(in srgb, var(--color) 30%, transparent);
            transform: translateY(-1px);
        }

        &:disabled {
            background-color: #e0e0e0;
            color: #999;
        }
    }
}
</style>
