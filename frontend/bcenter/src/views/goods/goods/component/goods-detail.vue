<template>
    <el-form-item label="商品详情" prop="content">
        <div class="flex flex-wrap">
            <editor v-model="formData.content" :height="667" :width="375" v-if="!read_only" />

            <div class="border-[14px] phone rounded-lg border-solid w-[375px] h-[667px] ml-8">
                <el-scrollbar style="height: 100%">
                    <div v-html="formData.content"></div>
                </el-scrollbar>
            </div>
        </div>
    </el-form-item>
</template>

<script lang="ts" setup>
import { useVModels } from '@vueuse/core'

const props = withDefaults(
    defineProps<{
        modelValue?: any
    }>(),
    {
        modelValue: {}
    }
)

const emit = defineEmits(['update:modelValue'])
const { modelValue: formData } = useVModels(props, emit)

const route = useRoute()
const router = useRouter()

const read_only = route.query.read_only
</script>

<style scoped>
.phone {
    padding: 5px 10px;
    border-color: #f0eff5;
    overflow-wrap: break-word;
}
</style>
