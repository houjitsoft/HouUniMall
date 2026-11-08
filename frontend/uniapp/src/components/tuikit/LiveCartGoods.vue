<template>
	<view class="bottom-drawer-cart" v-if="modelValue">
		<view class="drawer-cart" @tap="close"></view>
		<view class="cart-box" :class="{ 'drawer-open': modelValue }" @click.stop>
			<view class="cart-header">
				<view class="">
					<text>橱窗</text>
				</view>
				<view class="disFlex alignCenter" @tap="handleToCart">
					<up-icon name="shopping-cart" size="28"></up-icon>
					<text>购物车</text>
				</view>
			</view>
			<view class="cart-content">
				<!-- 列表 -->
				<view class="cart-content-list" v-if="isDataEmpty">
					<scroll-view class="cart-list" scroll-y="true" @scrolltolower="scrolltolower">
						<view class="cart-list-item" @click.stop v-for="(item,index) in liveGoodsList"
							:key="item.goodsId">
							<view class="item-left" @tap="handleAddCart(item)">
								<image class="commonImage" :src="item.commonImage"></image>
								<text class="itemIndex">{{index + 1}}</text>
								<view class="explainingProgress disFlex" v-if="item.goodsType === 1">
									<text class="c-fff fz26">讲解中</text>
								</view>
							</view>
							<view class="item-right">
								<view class="commonName" @tap="handleAddCart(item)">
									<text class="commonName_text overflow_two">{{item.commonName}}</text>
								</view>
								<view class="item-right-bottom disFlex">
									<view class="commonPrice disFlex">
										<text class="price" style="font-size: 22rpx;">￥</text>
										<text class="price fz35">{{item.commonPrice}}</text>
									</view>
									<view class="commonCartOrder disFlex">
										<view class="goCart disFlex" @tap="handleAddCart(item)">
											<up-icon color="#9f9f9f" name="shopping-cart" size="26"></up-icon>
										</view>
										<view class="goOrder disFlex" @tap="handleToDetails(item)">
											<text style="font-size: 26rpx;color: #fff;">去抢购</text>
										</view>
									</view>
								</view>
							</view>
						</view>
					</scroll-view>
				</view>
				<!-- 为空 -->
				<view class="cart-empty" v-else>
					<up-empty mode="data">
					</up-empty>
				</view>
			</view>
		</view>

	</view>

	<!-- 正在讲解的商品 -->
	<view class="explainingBox" v-if="Object.keys(explainingGoods).length > 0 && !modelValue">
		<view class="goodsModule" @click="handleAddCart(explainingGoods)">
			<view class="goodsModuleImage">
				<image class="goodsImage" :src="explainingGoods.commonImage" mode=""></image>
				<view class="explainingProgress disFlex">
					<text class="c-fff fz26">讲解中</text>
				</view>
			</view>
			<view class="goodsModuleName">
				<text class="goodsModuleNameText overflow_one">{{explainingGoods.commonName}}</text>
			</view>
			<view class="goodsModulePrice disFlex">
				<text class="price fz22">￥</text>
				<text class="price fz32">{{explainingGoods.commonPrice}}</text>
			</view>
		</view>
	</view>

	<!-- 规格 -->
	<view class="" v-if="isShowProductSpec">
		<ProductSpec v-model="isShowProductSpec" :goodsId="clickGoodsId"></ProductSpec>
	</view>

</template>

<script setup>
	import {
		ref,
		reactive,
		watch,
		computed
	} from 'vue';
	import {
		apiQueryGoodsByPage
	} from "@/api/tuikit/index.js"
	import {
		queryShoppingCart
	} from "@/api/pages/description.js"
	import ProductSpec from "@/components/ProductModule/ProductSpec.vue"

	const emit = defineEmits(['update:modelValue']);
	const props = defineProps({
		modelValue: {
			type: Boolean,
			default: false
		},
		userInfo: {
			type: Object,
			default: () => ({
				userName: '',
				userID: '',
				avatarURL: '',
				isMessageDisabled: false,
				userRole: 1
			})
		},
		liveID: {
			type: String,
			default: ''
		}
	});

	const liveID = computed(() => props.liveID || uni?.$liveID);
	const isDataEmpty = ref(false);
	const isShowProductSpec = ref(false);
	const liveGoodsList = ref([]);
	const clickGoodsId = ref(0);
	const totalCount = ref(0);
	const explainingGoods = ref({});
	const params = reactive({
		roomId: liveID.value,
		page: 1,
		limit: 10
	})
	// #ifdef APP
	import {
		useBarrageState
	} from '@/uni_modules/tuikit-atomic-x/state/BarrageState';

	const {
		messageList
	} = useBarrageState(liveID.value);
	// #endif

	// #ifndef APP
	import {
		useBarrageState
	} from 'tuikit-atomicx-vue3';
	const {
		messageList
	} = useBarrageState();
	// #endif

	watch(
		() => props.modelValue,
		(val) => {
			liveGoodsList.value = [];
			if (val) getLiveGoodsList();
		}
	);

	watch(messageList, (newVal) => {
		if (newVal && newVal.length > 0) {
			const obj = newVal[newVal.length - 1];
			
			if (!obj?.data) return
			
			const obj2 = JSON.parse(obj.data);

			const obj3 = obj2.msgBody[0].msgContent.data.Data;
			
			// #ifdef APP
			const businessID = newVal[newVal.length - 1].businessID;
			// #endif
			
			// #ifndef APP
			const businessID = newVal[newVal.length - 1].businessId;
			// #endif
			
			if (businessID === 'cart') {
				console.log("小黄车自定义消息：", newVal);
				liveGoodsList.value = [];
				getLiveGoodsList();
			}
			if (businessID === 'explain') {
				console.log("讲解商品自定义消息：", obj3);
				explainingGoods.value = obj3 || {};
			}
		}
	}, {
		deep: true
	});
	// 查询商品
	const getLiveGoodsList = async () => {
		try {
			const {
				data
			} = await apiQueryGoodsByPage(params);
			liveGoodsList.value = [...liveGoodsList.value, ...data.list] || [];
			totalCount.value = data.totalCount || 0;
			if (data.list.length > 0) {
				isDataEmpty.value = true;
			}
			console.log('获取到的商品：', data);
		} catch (error) {
			console.log(`获取商品失败：${error}`);
		}
	}

	// 加入购入车
	const handleAddCart = async (item) => {
		clickGoodsId.value = item.goodsId;
		isShowProductSpec.value = true;
	}

	// 去购物车
	const handleToCart = () => {
		uni.switchTab({
			url: "/pages/cart/cart"
		});
	}

	// 去抢购
	const handleToDetails = (item) => {
		uni.navigateTo({
			url: `/pages/description/descrption?goodsId=${item.goodsId}&commonId=${item.commonId}`
		})
	}
	const scrolltolower = () => {
		if (totalCount.value > liveGoodsList.value.length) {
			params.page += 1;
			getLiveGoodsList();
		} else {
			uni.showToast({
				title: "没有更多数据了",
				icon: "none"
			})
		}
	}
	// 取消
	const close = () => {
		emit('update:modelValue', false);
	};
</script>

<style scoped>
	.bottom-drawer-cart {
		width: 750rpx;
		position: fixed;
		bottom: 0;
		top: 0;
		left: 0;
		right: 0;
		z-index: 1000;
	}

	.drawer-cart {
		position: absolute;
		top: 0;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: rgba(0, 0, 0, 0.4);
	}

	.drawer-open {
		/* #ifdef APP */
		transform: translateY(0%);
		/* #endif */
		/* #ifndef APP */
		transform: translateY(100%);
		/* #endif */
	}

	.cart-box {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		background-color: #F1F1F1;
		/* #ifdef APP */
		transform: translateY(100%);
		/* #endif */
		/* #ifndef APP */
		transform: translateY(0%);
		/* #endif */
		transition-property: transform;
		transition-duration: 0.5s;
		transition-timing-function: ease;
		border-top-left-radius: 32rpx;
		border-top-right-radius: 32rpx;
	}

	.cart-header,
	.cart-content,
	.cart-list-item {
		width: 750rpx;
		display: flex;
		flex-direction: row;
	}

	.cart-header {
		color: #000;
		padding: 25rpx 20rpx 20rpx;
		border-bottom: 1rpx solid #e2e2e2;
		justify-content: space-between;
	}

	.cart-empty {
		flex: 1;
		text-align: center;
		padding-top: 100rpx;
		padding-bottom: 100rpx;
	}

	.cart-list {
		width: 750rpx;
		min-height: 300rpx;
		max-height: 800rpx;
		box-sizing: border-box;
		padding: 15rpx;
	}

	.cart-list-item {
		width: 720rpx;
		padding: 15rpx;
		background-color: #fff;
		border-radius: 20rpx;
		margin: 10rpx 0rpx;
		box-sizing: border-box;
	}

	.cart-list-item .item-left {
		margin-right: 10rpx;
		position: relative;
	}

	.cart-list-item .item-left .commonImage {
		width: 200rpx;
		height: 200rpx;
		border-radius: 15rpx;
	}

	.cart-list-item .item-left .itemIndex {
		padding: 3rpx 10rpx;
		position: absolute;
		top: 0rpx;
		left: 0rpx;
		color: #fff;
		border-top-left-radius: 15rpx;
		border-bottom-right-radius: 15rpx;
		background-color: rgba(0, 0, 0, 0.4);
	}

	.cart-list-item .item-left .explainingProgress,
	.goodsModule .goodsModuleImage .explainingProgress {
		position: absolute;
		left: 0;
		bottom: 0;
		background: var(--theme-color, #002FA7);
		justify-content: center;
		align-items: center;
	}

	.cart-list-item .item-left .explainingProgress {
		width: 200rpx;
		height: 40rpx;
		border-bottom-left-radius: 15rpx;
		border-bottom-right-radius: 15rpx;
	}

	.item-right {
		width: 470rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.item-right .commonName {
		color: #000;
	}

	.item-right .item-right-bottom {
		justify-content: space-between;
	}

	.item-right .item-right-bottom .commonPrice {
		align-items: flex-end;
	}

	.commonCartOrder {
		font-size: 26rpx;
		align-items: center;
		justify-content: space-around;
	}

	.commonCartOrder .goCart {
		width: 80rpx;
		height: 60rpx;
		align-items: center;
		justify-content: center;
		border-top-left-radius: 25rpx;
		border-bottom-left-radius: 25rpx;
		background-color: #f3f3f3;
	}

	.commonCartOrder .goOrder {
		width: 120rpx;
		height: 60rpx;
		align-items: center;
		justify-content: center;
		background: var(--theme-color, #002FA7);
		border-top-right-radius: 25rpx;
		border-bottom-right-radius: 25rpx;
	}

	/* 公共 */
	.disFlex {
		display: flex;
		flex-direction: row;
	}

	.alignCenter {
		align-items: center;
	}

	/* 显示模块 */
	.explainingBox {
		width: 230rpx;
		position: fixed;
		right: 30rpx;
		bottom: 130rpx;
		padding: 15rpx 0rpx;
		z-index: 100;
	}

	.goodsModule {
		border-radius: 15rpx;
		background-color: #F1F1F1;
		padding-bottom: 15rpx;
	}

	.goodsModule .goodsModuleImage {
		position: relative;
	}

	.goodsModule .goodsModuleImage .goodsImage {
		width: 230rpx;
		height: 230rpx;
		border-top-left-radius: 15rpx;
		border-top-right-radius: 15rpx;
	}

	.goodsModule .goodsModuleImage .explainingProgress {
		width: 230rpx;
		height: 40rpx;
	}

	.goodsModule .goodsModuleName {
		padding: 10rpx;
	}

	.goodsModule .goodsModuleName .goodsModuleNameText {
		lines: 1;
		overflow: hidden;
		text-overflow: ellipsis;
		color: #000;
		font-size: 28rpx;
	}

	.goodsModule .goodsModulePrice {
		align-items: flex-end;
	}
</style>