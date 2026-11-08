<template>
    <view class="product-spec">
        <u-popup :show="isShowProductSpec" :round="20" mode="bottom" borderRadius="14" :closeable="true"
            :z-index="10086" @close="handleClose">
            <view class="goodsBox">
                <view class="goodsHeader flex-row bgf">
                    <image class="goodsHeaderLeft" :src="goodsBases?.goodsImage"></image>
                    <view class="flex-column goodsHeaderRight">
                        <view class="goodsName">
							<text
								class="goodsNameText overflow_two">{{goodsBases?.goodsName}}</text>
						</view>
                        <view class="memberAmount flex-row">
                            <text class="price fz22">￥</text>
                            <text class="price fz35">{{ goodsBases?.goodsPrice }}</text>
                        </view>
                        <view class="codeStock flex-row">
                            <!-- <text class="fz26 c-666">订货编号：{{ goodsBases?.goodsCode }}</text> -->
                            <text
								class="fz26 c-666 ml10">库存：{{goodsBases?.goodsStock > 999 ? '999+' :goodsBases?.goodsStock }}</text>
                        </view>
                        <!-- <view class="flex-row goodsPrice">
                            <text class="fz26 c-666">未税价格：￥{{ ((goodsBases?.hyZheKou / 10) *
                                goodsBases?.goodsPrice).toFixed(2) }}</text>
                            <text class="fz26 c-666">( 税率：{{ (goodsBases?.taxRate * 100).toFixed(2) }}% )</text>
                        </view> -->
                        <view class="goodsBottomNumber flex-row">
                            <!-- <text class="valueNum fz26">起订量：{{ goodsBases?.moq }}</text> -->
                            <up-number-box v-model="goodsNumber" :min="1" :integer="true"
                                :max="goodsBases?.goodsStock" buttonWidth="25" buttonSize="25"></up-number-box>
                        </view>
                    </view>
                </view>
                <scroll-view scroll-y="true" class="goodsCenter bgf ">
                    <view class="goodsSpecContent">
                        <block v-for="(item, index) in specParamList" :key="index">
                            <view class="specLabel">{{ item.paramName }}</view>
                            <view class="specValue flex-row">
                                <block v-for="(spec, idx) in item.paramNameValues" :key="idx">
                                    <text
                                        :class="[specValueList[index].paramValue == spec ? 'now' : '', 'specValueItem']"
                                        @click="handleClickSpec(index, spec)">{{ spec }}</text>
                                   <!-- <text v-else class="specValueItem" style="color: #999999;border-color: #999999;">{{
                                        spec
                                        }}</text> -->
                                </block>
                            </view>
                        </block>
                    </view>
                </scroll-view>
                <view class="goodsBottomOperation flex-column bgf">
                    <view class="operationBtn">
                        <view v-if="goodsBases?.virtualGoods == 1" class="addCart flex-row" @click="handleAddCart">
                            <text class="c-fff">加入购物车</text>
                        </view>
                        <view v-else class="addCart flex-row" @click="handleToDetails">
                            <text class="c-fff">去抢购</text>
                        </view>
                    </view>
                </view>
            </view>
        </u-popup>
    </view>
</template>

<script setup>
import {
    ref,
    computed,
    watch,
} from "vue";
import {
    queryGoods,
    queryNew
} from "@/api/pages/description.js"
import {
	queryShoppingCart
} from "@/api/pages/description.js"
// Props定义
const isShowProductSpec = defineModel();
const emit = defineEmits(['update:modelValue']);
const props = defineProps({
    goodsId: {
        type: [String, Number],
        default: 0
    }
});
const goodsBases = ref({})
const specParamList = ref([])
const specValueList = ref([])
const goodsNumber = ref(1)

const fetchData = async () => {
    try {
        const {
            code,
            data
        } = await queryGoods({
            goodsId: props.goodsId,
            sessionTime: ''
        })
        console.log("获取商品数据信息：", data);
        if (code === 1) {
            goodsBases.value = data.goodsBases || {}; // 基础数据
            specParamList.value = data.gdsCommon.specParamList; // 规格列表
            specValueList.value = data.goodsBases.specValueList; // 初始规格值列表
            goodsNumber.value = 1
        }
    } catch (error) {
        console.log("获取商品数据失败：", error);
    }
}

watch(() => props.goodsId, (newValue) => {
    console.log("获取GoodsId信息：", newValue);
    if (newValue) fetchData(newValue)
}, {
    deep: true,
    immediate: true
})

const handleClickSpec = (index, item2) => {
    specValueList.value[index].paramValue = item2;
    console.log("点击的规格数组：", specValueList.value);
    const selectSpecList = specValueList.value.map((ele) => {
        return {
            paramName: ele.paramName,
            paramValue: ele.paramValue
        }
    })
    switchSpecifications(selectSpecList);
}

const handleClose = () => {
    isShowProductSpec.value = false;
    emit('update:modelValue', false)
}

// 去抢购
const handleToDetails = () => {
    uni.navigateTo({
        url: `/pages/description/descrption?goodsId=${goodsBases.value?.goodsId}&commonId=${goodsBases.value?.commonId}`
    })
}
// 加入购入车
const handleAddCart = async () => {
    uni.showLoading({
        title: "正在加购中..."
    })
    try {
        const {
            code,
            message
        } = await queryShoppingCart({
            goodsId: goodsBases.value?.goodsId,
            goodsNum: goodsNumber.value
        })

        uni.showToast({
            title: code === 1 ? "加购成功" : message,
            icon: "none"
        });

    } catch (error) {
        console.log(`加入购物车失败：${error}`);
    }
}

// 切换规格
async function switchSpecifications(specValues) {
    try {
        const {
            code,
            data
        } = await queryNew({
            commonId: goodsBases.value?.commonId,
            pintTuanTyep: 1,
            specValues
        });
        if (code === 1) {
            goodsBases.value = data || {};
        }
    } catch (error) {
        console.log("切换规格失败：", error);
    }
}
</script>

<style scoped>
/* 公共样式 */
.flex-row {
    display: flex;
    flex-direction: row;
}

.flex-column {
    display: flex;
    flex-direction: column;
}

/* 主轮廓 */
.goodsBox {
    color: #000;
    position: relative;
    padding: 10rpx 10rpx 20rpx;
    background-color: #F1F1F1;
}

/* 头部 */
.goodsHeader {
    display: flex;
    flex-direction: row;
    margin-top: 60rpx;
    padding: 10rpx;
    border-radius: 15rpx;
}

.goodsHeader .goodsHeaderLeft {
    width: 230rpx;
    height: 230rpx;
    border-radius: 15rpx;
}

.goodsHeader .goodsHeaderRight {
    height: 230rpx;
    padding-left: 10rpx;
    justify-content: space-between;
}

.goodsHeader .goodsHeaderRight .goodsNameText {
    max-width: 470rpx;
    height: 100rpx;
    lines: 2;
    overflow: hidden;
    text-overflow: ellipsis;
    font-weight: 600;
}

.goodsHeader .goodsHeaderRight .codeStock {}

.goodsHeader .goodsHeaderRight .memberAmount,
.goodsHeader .goodsHeaderRight .goodsPrice {
    align-items: flex-end;
}

.goodsBottomNumber {
    width: 470rpx;
    padding: 15rpx;
    align-items: flex-end;
    justify-content: space-between;
}

/* 中部 */
.goodsCenter {
    margin-top: 20rpx;
    min-height: 400rpx;
    max-height: 700rpx;
    padding: 15rpx 15rpx 25rpx;
    border-radius: 15rpx;
}

.goodsCenter .goodsSpecContent {}

.goodsCenter .goodsSpecContent .specLabel {
    font-weight: 600;
    line-height: 35rpx;
    margin-top: 15rpx;
}

.goodsCenter .goodsSpecContent .specValue {
    margin-top: 10rpx;
}

.goodsSpecContent .specValue .specValueItem {
    border: 1rpx solid #F7F7F7;
    padding: 5rpx 20rpx;
    background-color: #f7f7f7;
    border-radius: 10rpx;
    margin: 0rpx 15rpx;
}

.goodsSpecContent .specValue .specValueItem.now {
    border: 1rpx solid #EC335F;
    color: #EC335F;
}



/* 底部部分 */
.goodsBottomOperation {
    align-items: center;
    margin-top: 20rpx;
    padding: 10rpx;
    border-radius: 15rpx;
}

.operationBtn .addCart {
    width: 680rpx;
    height: 80rpx;
    justify-content: center;
    align-items: center;
    border-radius: 40rpx;
    background: var(--theme-color, #002FA7);
}
</style>