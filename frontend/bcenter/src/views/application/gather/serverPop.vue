<template>
    <Popup ref="popRef" title="充值" width="600px" @confirm="confirm" @close="emit('close')" async>
        <div class="flex flex-col items-center">
            <el-image
                :src="getImageUrl(config.admin.kefu[0].content.qrcode)"
                class="w-[200px] h-[200px]"
            ></el-image>
            <div class="mt-5">请联系客服进行充值</div>
        </div>
    </Popup>
</template>

<script setup lang="ts">
import { ref, shallowRef, computed } from 'vue'
import useAppStore from '@/stores/modules/app'
const config = computed(() => appStore.config)

const appStore = useAppStore()
const { getImageUrl } = useAppStore()

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
    popRef.value.close()
}

const open = async (row: any) => {
    popRef.value.open()
}

defineExpose({ open })
</script>

<style scoped lang="scss"></style>
