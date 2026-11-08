<template>
    <div class="step-content">
        <h2 class="step-title">{{ $t('确认信息') }}</h2>

        <div class="review-section">
            <div class="review-item">
                <span class="review-label">{{ $t('用户类型') }}：</span>
                <span class="review-value">{{ userType === 'enterprise' ? $t('企业用户') : $t('个人用户') }}</span>
            </div>
            <div v-if="userType === 'enterprise'" class="review-item">
                <span class="review-label">{{ $t('公司名称') }}：</span>
                <span class="review-value">{{ formData.companyName }}</span>
            </div>
            <div v-if="userType === 'enterprise'" class="review-item">
                <span class="review-label">{{ $t('税号') }}：</span>
                <span class="review-value">{{ formData.taxId }}</span>
            </div>
            <div v-if="userType === 'enterprise'" class="review-item">
                <span class="review-label">{{ $t('注册地址') }}：</span>
                <span class="review-value">{{ formData.registrationAddress }}</span>
            </div>
            <div v-if="(formData.phone ?? '').trim()" class="review-item">
                <span class="review-label">{{ $t('手机号码') }}：</span>
                <span class="review-value">{{ selectedCountry.code }} {{ formData.phone }}</span>
            </div>
            <div v-if="(formData.email ?? '').trim()" class="review-item">
                <span class="review-label">{{ $t('邮箱地址') }}：</span>
                <span class="review-value">{{ formData.email }}</span>
            </div>
        </div>

        <div class="step-actions">
            <button type="button" class="btn-prev" @click="$emit('prev')">{{ $t('上一步') }}</button>
            <button type="button" class="btn-submit" @click="$emit('submit')" :disabled="isSubmitting">
                {{ isSubmitting ? $t('提交中...') : $t('提交申请') }}
            </button>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { ReviewInfoProps } from '~/types/component'

type Props = ReviewInfoProps

defineProps<Props>()

defineEmits<{
    'prev': []
    'submit': []
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

.review-section {
    background-color: #f9f9f9;
    border-radius: 8px;
    padding: 24px;
    margin-bottom: 30px;
}

.review-item {
    display: flex;
    padding: 12px 0;
    border-bottom: 1px solid #e0e0e0;

    &:last-child {
        border-bottom: none;
    }

    .review-label {
        font-size: 14px;
        color: #666;
        width: 120px;
        flex-shrink: 0;
    }

    .review-value {
        font-size: 14px;
        color: #333;
        font-weight: 500;
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
        color: #fff;

        &:hover:not(:disabled) {
            background-color: color-mix(in srgb, var(--color) 85%, black);
            box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
        }
    }

    .btn-submit {
        background-color: var(--color);
        color: #fff;

        &:hover:not(:disabled) {
            background-color: color-mix(in srgb, var(--color) 85%, black);
            box-shadow: 0 2px 8px color-mix(in srgb, var(--color) 30%, transparent);
        }
    }
}
</style>
