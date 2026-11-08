<template>
    <PopupModal v-model="visible" title="充值记录" width="98vw" content-class="plus-record-dialog" @close="handleClose">
        <div class="plus-record-dialog__body">
            <table class="plus-record-dialog__table">
                <thead>
                    <tr>
                        <th>{{ $t('序号') }}</th>
                        <th>{{ $t('套餐类型') }}</th>
                        <th>{{ $t('充值金额(元)') }}</th>
                        <th>{{ $t('充值单号') }}</th>
                        <th>{{ $t('充值方式') }}</th>
                        <th>{{ $t('充值时间') }}</th>
                        <th>{{ $t('本次生效时间') }}</th>
                        <th>{{ $t('本次到期时间') }}</th>
                        <th>{{ $t('权益状态') }}</th>
                    </tr>
                </thead>
                <tbody>
                    <tr v-if="loading">
                        <td colspan="9" class="plus-record-dialog__stateCell">
                            <Loading :text="$t('加载中...')" :compact="true" />
                        </td>
                    </tr>
                    <tr v-else-if="!records.length">
                        <td colspan="9" class="plus-record-dialog__stateCell">
                            <EmptyState :text="$t('暂无记录')" :compact="true" :icon-size="36" />
                        </td>
                    </tr>
                    <tr v-for="(row, idx) in records" v-else :key="row.plusOrderid ?? idx">
                        <td>{{ idx + 1 }}</td>
                        <td>{{ formatThaliTypeText(row.thaliType) }}</td>
                        <td>{{ Number(row.payPrice ?? 0) }}</td>
                        <td>{{ row.plusOrderid || '--' }}</td>
                        <td>{{ formatPayTypeText(row.payType) }}</td>
                        <td>{{ row.creatTime || '--' }}</td>
                        <td>{{ row.startTime || '--' }}</td>
                        <td>{{ row.endTime || '--' }}</td>
                        <td>{{ formatRenewStatusText(row.renewStatus) }}</td>
                    </tr>
                </tbody>
            </table>
            <div v-if="!loading && totalCount > 0" class="plus-record-dialog__pagination">
                <UPagination v-model:page="currentPage" :total="totalCount" :items-per-page="props.limit"
                    @update:page="handlePageChange" />
            </div>
        </div>

        <template #footer>
            <button type="button" class="plus-record-dialog__btn" @click="handleClose">
                {{ $t('继续逛逛') }}
            </button>
        </template>
    </PopupModal>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import PopupModal from '~/components/Modal/PopupModal.vue'
import Loading from '~/components/Load/Loading.vue'
import EmptyState from '~/components/Empty/EmptyState.vue'
import { getPlusInfoOrder } from '~/services/userms.service'
import type { PlusInfoOrderItem } from '~/types/plus'

const props = withDefaults(
    defineProps<{
        /** 弹窗可见状态（v-model） */
        modelValue: boolean
        /** 接口分页参数：每页条数 */
        limit?: number
    }>(),
    {
        limit: 10
    }
)

const emit = defineEmits<{
    'update:modelValue': [value: boolean]
}>()

const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
})

const loading = ref(false)
const records = ref<PlusInfoOrderItem[]>([])
const currentPage = ref(1)
const totalCount = ref(0)

/**
 * 套餐类型文案。
 * @param type 套餐类型
 * @returns 月卡/季卡/年卡
 */
function formatThaliTypeText(type: unknown): string {
    const t = Number(type ?? 0)
    if (t === 1) return '月卡'
    if (t === 2) return '季卡'
    if (t === 3) return '年卡'
    return '--'
}

/**
 * 支付方式文案。
 * @param payType 支付方式枚举
 * @returns 支付方式中文
 */
function formatPayTypeText(payType: unknown): string {
    const t = Number(payType ?? 0)
    if (t === 1) return '余额支付'
    if (t === 2) return '购物卡'
    if (t === 3) return '微信支付'
    if (t === 4) return '支付宝'
    return '平台赠送'
}

/**
 * 权益状态文案。
 * @param renewStatus 权益状态
 * @returns 生效状态中文
 */
function formatRenewStatusText(renewStatus: unknown): string {
    return Number(renewStatus ?? 0) === 1 ? '生效' : '未生效'
}

/**
 * 拉取充值记录列表。
 * @returns {Promise<void>}
 */
async function fetchRechargeRecords(): Promise<void> {
    loading.value = true
    try {
        const res = await getPlusInfoOrder({ limit: props.limit, page: currentPage.value })
        if (res?.code === 1 && Array.isArray(res?.data?.list)) {
            records.value = res.data.list
            totalCount.value = Number(res?.data?.totalCount ?? 0)
        } else {
            records.value = []
            totalCount.value = 0
        }
    } catch (_) {
        records.value = []
        totalCount.value = 0
    } finally {
        loading.value = false
    }
}

/**
 * 关闭弹窗。
 * @returns {void}
 */
function handleClose(): void {
    visible.value = false
}

/**
 * 分页切换。
 * @param page 页码
 */
function handlePageChange(page: number): void {
    currentPage.value = page
    fetchRechargeRecords()
}

watch(
    () => props.modelValue,
    (val) => {
        if (val) {
            currentPage.value = 1
            fetchRechargeRecords()
        }
    }
)
</script>

<style scoped lang="scss">
:deep(.plus-record-dialog) {
    max-width: min(98vw, 1520px);
}

.plus-record-dialog__body {
    max-height: 70vh;
    overflow: auto;
}

.plus-record-dialog__table {
    width: 100%;
    border-collapse: collapse;
    font-size: 14px;
    font-weight: 400;
}

.plus-record-dialog__table th,
.plus-record-dialog__table td {
    border: 1px solid #eee;
    padding: 12px 10px;
    text-align: center;
    white-space: nowrap;
    font-weight: 400;
}

.plus-record-dialog__table th {
    background: #f7f7f7;
    color: #222;
}

.plus-record-dialog__stateCell {
    padding: 0 !important;
}

.plus-record-dialog__pagination {
    margin-top: 14px;
    display: flex;
    justify-content: flex-end;
}

.plus-record-dialog__btn {
    min-width: 180px;
    height: 42px;
    border: 0;
    border-radius: 999px;
    background: linear-gradient(180deg, #fde2b9 0%, #d9a86f 100%);
    color: #333;
    font-size: 14px;
    font-weight: 400;
    cursor: pointer;
}
</style>
