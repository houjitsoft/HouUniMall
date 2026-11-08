<template>
    <div class="product-detail-content">
        <div class="detail-grid">
            <div class="detail-item" v-for="row in categoryAttributesList" :key="row.id">
                <span class="label">{{ $t(row.attributeName || '') }}:</span>
                <span class="value">{{ $t(row.attributeValueName || '') }}</span>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import type { CategoryAttribute } from '~/types/goods'

defineProps<{
    categoryAttributesList?: CategoryAttribute[]
}>()
</script>

<style lang="scss" scoped>
.product-detail-content {
    .detail-grid {
        display: grid;
        grid-template-columns: repeat(4, minmax(0, 1fr));
        gap: 12px 16px;
    }

    .detail-item {
        display: flex;
        min-width: 0;

        .label {
            flex: 0 0 auto;
            max-width: 120px;
            font-size: 14px;
            color: #666;
            font-weight: 500;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }

        .value {
            flex: 1;
            min-width: 0;
            font-size: 14px;
            color: #333;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    }
}
</style>
