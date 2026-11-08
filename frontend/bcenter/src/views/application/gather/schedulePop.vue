<template>
    <Popup
        ref="popRef"
        title="采集商品"
        width="600px"
        @confirm="confirm"
        @close="cancel"
        async
        :confirmButtonText="false"
        :cancelButtonText="false"
    >
        <div class="flex flex-col justify-center items-center">
            <div class="ml-10 w-full">
                <!-- <div>采集进度 <el-progress :percentage="progressval" /></div> -->
                <div
                    v-loading="checking"
                    element-loading-text="正在处理中，请稍等..."
                    style="height: 200px"
                ></div>
                <div class="text-[#666666] text-center mt-5 text-xs">
                    采集商品处理中请耐心等待，期间请不要关闭对话框
                </div>
            </div>
        </div>
    </Popup>
</template>

<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue'
import useAppStore from '@/stores/modules/app'
import { apiGatherDeal, apiGatherDetial } from '@/api/application/gather'

const config = computed(() => appStore.config)

const appStore = useAppStore()
const { getImageUrl } = useAppStore()
const checking = ref(false)
const emit = defineEmits(['confirm', 'close'])

//弹框ref
const popRef = shallowRef()
//用户数据
const data: any = ref({})

const radioValue = ref(1)
//下拉列表

const formData = ref({
    id: '',
    gather_money: ''
})
//确定
const confirm = async () => {
    emit('confirm')
    // clearTimer()
    popRef.value.close()
}
const cancel = () => {
    emit('close')
    // clearTimer()
}
// const progressval = ref(0)
// const timer = ref()
const open = async (id: any) => {
    popRef.value.open()
    checking.value = true
    try {
        await apiGatherDeal({ id })
    } catch (error) {
        console.log(error)
    }
    emit('close')

    // createTimer(id)
}
// const clearTimer = () => {
//     clearInterval(timer.value)
// }
// const createTimer = (id: number) => {
//     if (timer.value) clearInterval(timer.value)
//     setInterval(async () => {
//         progressval.value = progressval.value + 10
//     }, 100)
//     timer.value = setInterval(async () => {
//         const { progress } = await apiGatherDetial({ id })
//         progressval.value = progress
//         if (progress == 100) {
//             confirm()
//         }
//     }, 1000)
// }
defineExpose({ open })
</script>

<style scoped lang="scss"></style>
