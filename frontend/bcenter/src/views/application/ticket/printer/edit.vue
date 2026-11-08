<template>
    <el-card class="!border-none" shadow="never">
        <el-page-header
            :content="route.query.id ? '编辑打印机' : '新增打印机'"
            @back="$router.back()"
        />
    </el-card>
    <el-card class="!border-none mt-2" shadow="never">
        <div class="text-xl">打印机设置</div>
        <el-form ref="formRef" :model="formData" label-width="120px" :rules="rules" class="mt-2">
            <el-form-item label="打印机名称" prop="name">
                <el-input placeholder="请输入打印机名称" v-model="formData.name" class="w-[300px]">
                </el-input>
            </el-form-item>
            <el-form-item label="设备品牌" prop="brand">
                <el-select v-model="formData.brand">
                    <el-option label="飞蛾打印（新接口)" :value="0">飞蛾打印（新接口）</el-option>
                </el-select>
            </el-form-item>
            <el-form-item label="USER" prop="content.moth.user">
                <el-input
                    placeholder="请输入飞鹅云后台注册用户名"
                    v-model="formData.content.moth.user"
                    class="w-[300px]"
                >
                </el-input>
            </el-form-item>
            <el-form-item label="UKEY" prop="content.moth.ukey">
                <el-input
                    placeholder="请输入飞鹅云后台登录生成的UKEY"
                    v-model="formData.content.moth.ukey"
                    class="w-[300px]"
                >
                </el-input>
            </el-form-item>
            <el-form-item label="打印机编号" prop="content.moth.sn">
                <el-input
                    placeholder="请输入打印机编号"
                    v-model="formData.content.moth.sn"
                    class="w-[300px]"
                >
                </el-input>
            </el-form-item>
            <el-form-item label="打印机key" prop="content.moth.key">
                <el-input
                    placeholder="请输入打印机key"
                    v-model="formData.content.moth.key"
                    class="w-[300px]"
                >
                </el-input>
            </el-form-item>
        </el-form>
    </el-card>
    <el-card class="!border-none mt-2" shadow="never">
        <div class="text-xl">商品订单</div>
        <div class="mt-5 p-3">
            <div class="flex px-5">
                <div class="w-1/4">打印时机</div>
                <div class="w-1/4">状态</div>
                <div class="w-1/4">使用模版</div>
                <div class="w-1/4">打印联数</div>
            </div>
            <div class="flex bg-[#F8F8F9] p-5 mt-3">
                <div class="w-1/4">下单后</div>
                <div class="w-1/4">
                    <el-switch
                        v-model="formData.order_print.order_create.status"
                        active-value="1"
                        inactive-value="0"
                    ></el-switch>
                </div>
                <div class="w-1/4">
                    <el-select v-model="formData.order_print.order_create.template_id">
                        <el-option
                            :label="item.name"
                            :value="String(item.id)"
                            v-for="item in optionLists"
                            :key="item.id"
                        ></el-option>
                    </el-select>
                </div>
                <div>
                    <el-select v-model="formData.order_print.order_create.nums">
                        <el-option label="1" value="1">1</el-option>
                        <el-option label="2" value="2">2</el-option>
                        <el-option label="3" value="3">3</el-option>
                        <el-option label="4" value="4">4</el-option>
                        <el-option label="5" value="5">5</el-option> </el-select
                    ><span class="ml-2"> 联 </span>
                </div>
            </div>
            <div class="flex bg-[#F8F8F9] p-5 mt-3">
                <div class="w-1/4">付款后</div>
                <div class="w-1/4">
                    <el-switch
                        v-model="formData.order_print.order_pay.status"
                        active-value="1"
                        inactive-value="0"
                    ></el-switch>
                </div>
                <div class="w-1/4">
                    <el-select v-model="formData.order_print.order_pay.template_id">
                        <el-option
                            :label="item.name"
                            :value="String(item.id)"
                            v-for="item in optionLists"
                            :key="item.id"
                        ></el-option>
                    </el-select>
                </div>
                <div>
                    <el-select v-model="formData.order_print.order_pay.nums">
                        <el-option label="1" value="1">1</el-option>
                        <el-option label="2" value="2">2</el-option>
                        <el-option label="3" value="3">3</el-option>
                        <el-option label="4" value="4">4</el-option>
                        <el-option label="5" value="5">5</el-option> </el-select
                    ><span class="ml-2"> 联 </span>
                </div>
            </div>
            <div class="flex bg-[#F8F8F9] p-5 mt-3">
                <div class="w-1/4">手动打印</div>
                <div class="w-1/4">
                    <el-switch
                        v-model="formData.order_print.self_print.status"
                        active-value="1"
                        inactive-value="0"
                    ></el-switch>
                </div>
                <div class="w-1/4">
                    <el-select v-model="formData.order_print.self_print.template_id">
                        <el-option
                            :label="item.name"
                            :value="String(item.id)"
                            v-for="item in optionLists"
                            :key="item.id"
                        ></el-option>
                    </el-select>
                </div>
                <div>
                    <el-select v-model="formData.order_print.self_print.nums">
                        <el-option label="1" value="1">1</el-option>
                        <el-option label="2" value="2">2</el-option>
                        <el-option label="3" value="3">3</el-option>
                        <el-option label="4" value="4">4</el-option>
                        <el-option label="5" value="5">5</el-option> </el-select
                    ><span class="ml-2"> 联 </span>
                </div>
            </div>
        </div>
    </el-card>
    <footer-btns>
        <el-button type="" @click="handletest">测试打印</el-button>
        <el-button type="primary" @click="handlesave">保存</el-button>
    </footer-btns>
</template>
<script lang="ts" setup>
import { ref, reactive } from 'vue'
import {
    apiPrinterAdd,
    apiPrinterEdit,
    getprinterDetial,
    gettemLists,
    apitestprint
} from '@/api/marketing/ticket'

const route = useRoute()
const router = useRouter()
const optionLists = ref<any>([])
const formData = reactive({
    id: '',
    name: '',
    brand: 0,
    content: {
        moth: {
            user: '',
            ukey: '',
            sn: '',
            key: ''
        }
    },
    order_print: {
        order_create: {
            status: '1',
            template_id: '',
            nums: 1
        },
        order_pay: {
            status: '1',
            template_id: '',
            nums: 1
        },
        self_print: {
            status: '1',
            template_id: '',
            nums: 1
        }
    }
})
const rules = reactive<any>({
    name: [{ required: true, message: '请输入打印机名称', trigger: 'blur' }],
    brand: [{ required: true, message: '请选择打印机设备品牌', trigger: 'blur' }],
    'content.moth.user': [
        { required: true, message: '请输入飞鹅云后台注册用户名', trigger: 'blur' }
    ],
    'content.moth.ukey': [
        { required: true, message: '请输入飞鹅云后台登录生成的UKEY', trigger: 'blur' }
    ],
    'content.moth.sn': [{ required: true, message: '请输入打印机编号', trigger: 'blur' }],
    'content.moth.key': [{ required: true, message: '请输入打印机key', trigger: 'blur' }]
})
const handlesave = async () => {
    if (!route.query.id) await apiPrinterAdd(formData)
    else await apiPrinterEdit(formData)
    router.back()
}
const getData = async () => {
    if (!route.query.id) return
    const data = await getprinterDetial({ id: route.query.id })
    Object.keys(formData).forEach((key) => {
        //@ts-ignore
        formData[key] = data[key]
    })
}
const getOption = async () => {
    const data = await gettemLists()
    optionLists.value = data.lists
}
const handletest = async () => {
    await apitestprint(formData)
}
getData()
getOption()
</script>
