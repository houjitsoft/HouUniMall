<template>
    <div class="steps-indicator">
        <div v-for="(step, index) in steps" :key="index" class="step-wrapper">
            <div class="step-item" :class="{
                active: currentStep === step.stepNumber,
                completed: currentStep > step.stepNumber
            }">
                <div class="step-circle">
                    <span v-if="currentStep > step.stepNumber" class="step-check">✓</span>
                    <span v-else>{{ step.stepNumber }}</span>
                </div>
                <div class="step-label">{{ step.label }}</div>
            </div>
            <div v-if="index < steps.length - 1" class="step-line" :class="{ completed: currentStep > step.stepNumber }">
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { StepIndicatorProps, Step } from '~/types/component'

type Props = StepIndicatorProps

defineProps<Props>()
</script>

<style lang="scss" scoped>
.steps-indicator {
    display: flex;
    align-items: flex-start;
    justify-content: center;
    margin-bottom: 36px;
    padding: 0 16px;
    gap: 0;
}

.step-wrapper {
    display: flex;
    align-items: center;
    flex: 1;
    max-width: 140px;
}

.step-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    position: relative;
    flex: 1;

    .step-circle {
        width: 44px;
        height: 44px;
        border-radius: 50%;
        background-color: #f0f0f0;
        color: #999;
        border: 2px solid #e8e8e8;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: 600;
        font-size: 15px;
        transition: background-color 0.25s ease, color 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
        flex-shrink: 0;

        .step-check {
            font-size: 18px;
            font-weight: 700;
            line-height: 1;
        }
    }

    .step-label {
        margin-top: 10px;
        font-size: 13px;
        color: #999;
        transition: color 0.25s ease, font-weight 0.25s ease;
        white-space: nowrap;
        text-align: center;
        line-height: 1.35;
    }

    &.active {
        .step-circle {
            background-color: var(--color);
            color: #fff;
            border-color: var(--color);
            box-shadow: none;
        }

        .step-label {
            color: var(--color);
            font-weight: 600;
        }
    }

    &.completed {
        .step-circle {
            background-color: var(--color);
            color: #fff;
            border-color: var(--color);
        }

        .step-label {
            color: #666;
            font-weight: 500;
        }
    }
}

.step-line {
    width: 48px;
    flex-shrink: 0;
    height: 3px;
    border-radius: 2px;
    background-color: #e8e8e8;
    margin: 0 8px;
    margin-top: -18px;
    transition: background-color 0.35s ease;

    &.completed {
        background-color: var(--color);
    }
}

@media (max-width: 768px) {
    .steps-indicator {
        padding: 0 8px;
        margin-bottom: 28px;
    }

    .step-wrapper {
        max-width: none;
    }

    .step-item {
        .step-circle {
            width: 38px;
            height: 38px;
            font-size: 14px;

            .step-check {
                font-size: 16px;
            }
        }

        .step-label {
            font-size: 12px;
            margin-top: 8px;
        }
    }

    .step-line {
        width: 32px;
        margin-top: -16px;
    }
}
</style>
