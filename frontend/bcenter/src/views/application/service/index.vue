<template>
    <div>
        <el-card shadow="never" class="!border-none">
            <template #header>
                <div class="flex items-center justify-between">
                    <span class="font-extrabold text-lg">客服设置</span>
                    <el-button type="primary" plain @click="openImManagement">
                        进入在线客服管理
                    </el-button>
                </div>
            </template>
            <el-form :model="formData" label-width="120px">
                <el-form-item label="场景">
                    <div class="flex">
                        <div
                            class="service-item flex-col justify-end"
                            :class="active == item.id ? 'active' : ''"
                            v-for="item in serviceLists"
                            :key="item.id"
                            @click="handleClick(item.id)"
                        >
                            <div>{{ item.name }}</div>
                            <div class="form-tips">扫二维码</div>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="客服方式">
                    <el-radio-group v-model="formData[serviceLists[active].key].way">
                        <el-radio value="1" label="1">扫二维码</el-radio>
                        <el-radio value="2" label="2">拨打电话</el-radio>
                        <!-- <el-radio value="3" label="3">企业微信客服</el-radio>
                        <el-radio value="4" v-if="active == 0" label="4">微信小程序客服</el-radio> -->
                    </el-radio-group>
                </el-form-item>
                <el-form-item label="" v-if="formData[serviceLists[active].key].way == 4">
                    需前往小程序后台绑定客服人员
                </el-form-item>
                <el-form-item label="企业ID" v-if="formData[serviceLists[active].key].way == 3">
                    <el-input
                        class="ls-input"
                        v-model="formData[serviceLists[active].key].enterprise_id"
                        placeholder="请输入企业ID"
                    />
                </el-form-item>
                <el-form-item
                    label="企业客服链接"
                    v-if="formData[serviceLists[active].key].way == 3"
                >
                    <div>
                        <el-input
                            class="ls-input"
                            v-model="formData[serviceLists[active].key].kefu_link"
                            placeholder="请输入企业客服链接"
                        />
                        <div class="form-tips">
                            小程序中如何接入企业微信客服请查看
                            <a
                                class="text-primary"
                                :href="serviceLists[active].link"
                                target="_blank"
                                rel="noopener noreferrer"
                            >
                                在{{ serviceLists[active].name }}中接入
                            </a>
                        </div>
                    </div>
                </el-form-item>
                <el-form-item label="客服电话" v-if="formData[serviceLists[active].key].way == 2">
                    <el-input
                        class="ls-input"
                        v-model="formData[serviceLists[active].key].service_phone"
                        placeholder="请输入客服电话"
                    />
                </el-form-item>
                <el-form-item label="平台名称" v-if="formData[serviceLists[active].key].way == 1">
                    <el-input
                        class="ls-input"
                        v-model="formData[serviceLists[active].key].name"
                        placeholder="请输入平台名称"
                    />
                </el-form-item>
                <el-form-item label="备注" v-if="formData[serviceLists[active].key].way == 1">
                    <el-input
                        class="ls-input"
                        v-model="formData[serviceLists[active].key].remarks"
                        placeholder="请输入备注"
                    />
                </el-form-item>
                <el-form-item label="客服电话" v-if="formData[serviceLists[active].key].way == 1">
                    <el-input
                        class="ls-input"
                        v-model="formData[serviceLists[active].key].phone"
                        placeholder="请输入客服电话"
                    />
                </el-form-item>
                <el-form-item label="服务时间">
                    <el-input
                        class="ls-input"
                        v-model="formData[serviceLists[active].key].business_time"
                        placeholder="请输入服务时间"
                    />
                </el-form-item>
                <el-form-item label="客服二维码" v-if="formData[serviceLists[active].key].way == 1">
                    <material-picker
                        v-model="formData[serviceLists[active].key].qr_code"
                        :limit="1"
                    />
                </el-form-item>
            </el-form>
        </el-card>
        <!-- Footer Start -->
        <footer-btns>
            <el-button v-perms="['config/get_service:set_service']" type="primary" @click="onSubmit"
                >保存</el-button
            >
        </footer-btns>
        <!-- Footer End -->
    </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'

import { getcustomerService, setcustomerService } from '@/api/service'
import FooterBtns from '@/components/footer-btns/index.vue'
import feedback from '@/utils/feedback'
const active = ref(0)
const serviceLists = ref([
    {
        id: 0,
        name: '微信小程序',
        key: 'mnp',
        link: 'https://work.weixin.qq.com/nl/act/p/a733314375294bdd'
    },
    {
        id: 1,
        name: '微信公众号',
        key: 'oa',
        link: 'https://work.weixin.qq.com/nl/act/p/3f8820e724cb44c5'
    },
    {
        id: 2,
        name: 'H5商城',
        key: 'h5',
        link: 'https://work.weixin.qq.com/nl/act/p/4030a5b69149404d'
    }
])
type obj = {
    business_time: string
    enterprise_id: string
    kefu_link: string
    name: string
    phone: string
    qr_code: string
    remarks: string
    way: string
    service_phone: string
}
interface FormDataObj {
    h5: obj
    mnp: obj
    oa: obj
}

const formData = ref<FormDataObj>({
    h5: {
        business_time: '',
        enterprise_id: '',
        kefu_link: '',
        name: '',
        phone: '',
        qr_code: '',
        remarks: '',
        way: '1',
        service_phone: ''
    },
    mnp: {
        business_time: '',
        enterprise_id: '',
        kefu_link: '',
        name: '',
        phone: '',
        qr_code: '',
        remarks: '',
        way: '1',
        service_phone: ''
    },
    oa: {
        business_time: '',
        enterprise_id: '',
        kefu_link: '',
        name: '',
        phone: '',
        qr_code: '',
        remarks: '',
        way: '1',
        service_phone: ''
    }
})

const initData = async (): Promise<void> => {
    try {
        formData.value = await getcustomerService()
    } catch (err) {
        console.log('初始化充值数据', err)
    }
}
const handleClick = (id: number) => {
    active.value = id
}
const openImManagement = () => {
    window.open('/imchat/#/merchant/settings', '_blank', 'noopener,noreferrer')
}
//提交数据
const onSubmit = async () => {
    await setcustomerService(formData.value)
    // feedback.msgSuccess('保存成功！')
    await initData()
}

initData()
</script>

<style lang="scss" scoped>
.active {
    border: 1px solid rgba(64, 115, 250, 1) !important;
    background-color: rgba(245, 248, 255, 1);
}
.service-item {
    width: 120px;
    height: 60px;
    line-height: 30px;
    text-align: center;
    border-radius: 10px;
    margin-right: 20px;
    border: 1px solid rgba(238, 238, 238, 1);
    cursor: pointer;
}
.ls-input {
    width: 320px;
}
</style>
