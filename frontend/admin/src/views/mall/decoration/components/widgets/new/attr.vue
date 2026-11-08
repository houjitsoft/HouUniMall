<template>
    <div>
        <el-form label-width="100px">
            <el-form-item label="是否启用">
                <el-radio-group v-model="content.enabled">
                    <el-radio :label="1">开启</el-radio>
                    <el-radio :label="0">停用</el-radio>
                </el-radio-group>
            </el-form-item>
            <div class="muted text-lg font-medium">头部设置</div>
            <el-form-item label="标题">
                <el-input
                    class="w-[230px]"
                    v-model="content.title"
                    show-word-limit
                    maxlength="10"
                ></el-input>
            </el-form-item>
            <el-form-item label="标题颜色">
                <view class="w-[230px]">
                    <color-picker
                        v-model="content.topTitleColor"
                        defaultColor="#000000"
                    ></color-picker>
                </view>
            </el-form-item>
            <el-form-item label="副标题">
                <el-input
                    class="w-[230px]"
                    v-model="content.subtitle"
                    show-word-limit
                    maxlength="15"
                ></el-input>
            </el-form-item>
            <el-form-item label="副标题颜色">
                <view class="w-[230px]">
                    <color-picker
                        v-model="content.topSubTitleColor"
                        defaultColor="#666666"
                    ></color-picker>
                </view>
            </el-form-item>
            <div class="muted text-lg font-medium">推荐商品</div>
            <el-form-item label="推荐商品">
                <el-radio-group v-model="content.getType">
                    <el-radio :label="1">自动获取</el-radio>
                    <el-radio :label="0" v-show="false">手动添加</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item label="商品排序">
                <div>
                    <el-select v-model="content.sort" placeholder="按销量">
                        <el-option label="按销量" value="1"> 按销量</el-option>
                        <el-option label="访问量" value="2">访问量 </el-option>
                        <el-option label="价格升序" value="3">价格升序 </el-option>
                        <el-option label="价格降序" value="4"> 价格降序</el-option>
                        <el-option label="新品推荐" value="5"> 新品推荐</el-option>
                        <el-option label="成交转化率" value="6"> 成交转化率</el-option>
                        <el-option label="浏览历史行为" value="7"> 浏览历史行为</el-option>
                    </el-select>
                </div>
            </el-form-item>
            <el-form-item label="显示数量">
                <div class="w-[400px]">
                    <div class="flex">
                        <el-slider
                            class="w-[250px]"
                            v-model="content.showNum"
                            :max="50"
                            :min="1"
                            show-input
                        />
                    </div>
                </div>
            </el-form-item>
            <div class="muted text-lg font-medium" v-show="false">
                <div class="flex">
                    推荐广告
                    <div class="form-tips ml-[10px]">最多可添加5张，建议图片尺寸：330px*550px</div>
                </div>
                <del-wrap
                    v-for="(item, index) in content.data"
                    :key="index"
                    @close="handleDelete(index)"
                    class="max-w-[400px]"
                >
                    <div class="flex items-center w-full p-4 mt-4 bg-fill-light">
                        <material-picker
                            v-model="item.image"
                            upload-class="bg-body"
                            exclude-domain
                        />
                        <div class="flex-1 ml-3">
                            <el-form-item class="mt-[18px]" label="图片链接">
                                <link-picker v-model="item.link" />
                            </el-form-item>
                        </div>
                    </div>
                </del-wrap>
                <el-button type="primary" @click="handleAdd" class="mt-[10px]">添加图片</el-button>
            </div>
        </el-form>
    </div>
</template>
<script setup>
import feedback from '@/utils/feedback'

const props = defineProps({
    content: {
        type: Object,
        default: () => ({})
    },
    styles: {
        type: Object,
        default: () => ({})
    }
})
const handleAdd = () => {
    if (props.content.data.length >= 5) {
        return feedback.msgError('最多添加五张图片')
    }
    props.content.data.push({
        image: '',
        name: '',
        link: {}
    })
}
const handleDelete = (index) => {
    if (props.content.data?.length <= 1) {
        return feedback.msgError('最少保留一张图片')
    }
    props.content.data.splice(index, 1)
}
</script>

<style lang="scss" scoped></style>
