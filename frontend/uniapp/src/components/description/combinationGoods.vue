<template>
	<view class="combination-goods" v-if="combinationShow">
		<view class="combinationGoods">
			<view class="combinationTitle">
				<text class="fz35">{{ getTransNew('组合搭配')}}</text>
			</view>
			<view class="combinationMain">
				<view class="combinationLi" v-for="(item,index) in getCombinationList()" :key="item.combinationId">
					<scroll-view class="combinationLiLeft" enable-flex :scroll-x="true" :scroll-left="0">
						<view class="combinationLiLeftGoodsItem" v-for="(value,ind) in item.goodsList" :key="value.goodsId">
							<view class="goodsItem">
								<view class="checked" v-if="item.combinationType == 2 && value.checked" @click="handleSelectFlag(item,value)">
									<image :src="`${uniImages}/selectTrue.png`" alt="" />
								</view>
								<image class="goodsImage" :src="value.goodsImage" @click="handleSelectFlag(item,value)"></image>
								<view class="" @click="handleGoDetails(value.goodsId,value.commonId)">
									<text class="overflow_two">{{ getTransNew(value.goodsName)}}</text>
								</view>
								<view class="overflow_one goodsSpec mt10">
									<text class="">{{ getTransNew('规格')}}：</text>
									<text>{{value.goodsSpec}}</text>
								</view>
								<view class="goodsNum mt10" v-if="item.combinationType == 2">
									<text @click="handleGoodsItemReduce(value,item.combinationId,2)">-</text>
									<text>{{value.goodsNum}}</text>
									<text @click="handleGoodsItemAdd(value,item.combinationId,2)">+</text>
								</view>
								<view class="textGoodsNum mt10" v-else>
									<text>{{ getTransNew('数量')}}：</text>
									<text>{{value.goodsNum}}</text>
								</view>
							</view>
							<image class="jiahao" v-if="ind < item.goodsList.length-1" :src="uniImages + '/jiahao.svg'"></image>
						</view>
					</scroll-view>
					<view class="combinationLiRight">
						<view class="mt10">
							<text>{{ getTransNew("组合价")}}：</text>
							<text class="price">{{item.combinationPrice.toFixed(2)}}</text>
						</view>
						<view class="mt10">
							<del class="del fz11">{{ getTransNew("原价")}}：{{item.goodsOriginalPrice}}</del>
						</view>
						<view class="comGoodsNum mt10" v-if="item.combinationType == 1">
							<text @click="handleGoodsItemReduce(item,item.combinationId,1)">-</text>
							<text>{{item.goodsNum}}</text>
							<text @click="handleGoodsItemAdd(item,item.combinationId,1)">+</text>
						</view>
						<view class="comGoodsNum mt20" v-else></view>
						<view class=" mt10">
							<button class="oneOrdering" @click="handleOneOrdering(item)">{{ getTransNew("一 键 下 单")}}</button>
						</view>
					</view>
					<view class="combinationTitle titleName">
						<text v-if="item.combinationType == 1">{{ getTransNew("固定搭配")}}</text>
						<text v-else>{{ getTransNew("组合搭配")}}</text>
					</view>
				</view>
			</view>
			<view class="tac" v-if="combinationList && combinationList.length > 2">
				<button class="oneOrdering" @click="combinaShowAndHide = !combinaShowAndHide">{{ getTransNew(combinaShowAndHide ? '收起' : '展开')}}</button>
			</view>
		</view>

		<u-toast ref="uToast" />
	</view>
</template>

<script>
	import {
		queryCombinationBaseByPcDetails,
		queryCombinationBasePrice
	} from "@/api/activities/index.js"
	import {checkToken} from '@/utils/util.js'
	export default {
		name: "combination-goods",
		props: {
			goodsBases: {
				type: Object,
				default: {}
			},
			shopDetail: {
				default: null
			},
			goodsId:{
				type:String,
				default:null
			}
		},
		data() {
			return {
				
				uniImages:this.appPreImg,
				combinationShow: false,
				combinationList: [],
				combinaShowAndHide: false
			}
		},
		created() {
			this.fetchData();

		},
		methods: {
			async fetchData() {
				
				let {
					code,
					data
				} = await queryCombinationBaseByPcDetails({
					goodsId: this.goodsId
				});
				if (code == 1) {
					this.combinationShow = data && data.length > 0 ? true : false;
					let list = data;
					list.forEach(item => {
						item.goodsList.map(item1 => {
							if (item1.isMainProduct == 1) {
								item1.checked = true;
								item1.disabled = true;
							} else {
								item1.checked = false;
								item1.disabled = false;
								item1.goodsNum = 0;
							}
							return item1
						})
					})
					this.combinationList = list;
				}
			},
			handleSelectFlag(rows, row) {
				// this.goodsIdList // 备用字段
				
				if (row.isMainProduct == 1) {
					return
				}
				this.combinationList.map(item=>{
					if(item.combinationId == rows.combinationId){
						return item.goodsList.map(item2=>{
							if(row.goodsId == item2.goodsId){
								item2.checked = !item2.checked;
								item2.goodsNum = item2.checked ? 1 : 0;
							}
							return item2
						})
					}
				})
				this.combinationBasePrice(rows);
				let list = rows.goodsList.filter(item => item.checked);
				this.goodsIdList = list.map(item => item.goodsId);
			},
			async handleGoodsItemReduce(row, combinationId, type) {
				
				if (type == 2 && !row.checked) {
					this.uToastShow({
						title: await this.getTransNew('请先勾选商品'),
						type: 'warning',
					})
					return false
				} else if (row.goodsNum <= 1) {
					this.uToastShow({
						title: await this.getTransNew('最小数量不能小于1'),
						type: 'warning',
					})
					return false
				}
				this.combinationList.map(item => {
					item.goodsNum = type == 1 && row.combinationId == item.combinationId ? item.goodsNum - 1 : item.goodsNum
					if (item.combinationId == combinationId) {
						item.goodsList.map(item2 => {
							if (type == 1) {
								// item2.goodsNum--
							} else {
								if (item2.goodsId == row.goodsId) {
									
									item2.goodsNum--
								}
							}
							return item2
						})
					}

					return item
				})
				this.combinationBasePrice(this.combinationList.find(item => row.combinationId == item.combinationId))
			},
			async handleGoodsItemAdd(row, combinationId, type) {
				let maxNum = 0;
				if (type == 1) {
					this.combinationList.forEach(item => {
						if (item.combinationId == combinationId) {
							maxNum = item.goodsList.sort((item1, item2) => item2.goodsNum - item1.goodsNum)[0].goodsNum;
						}
					})

				}

				if (type == 1 && (row.goodsNum * maxNum) >= row.goodsStock) {
					this.uToastShow({
						title: await this.getTransNew('最大数量不能大于最大库存'),
						type: 'warning',
					})
					return false
				} else if (type == 2 && !row.checked) {
					this.uToastShow({
						title: await this.getTransNew('请先勾选商品'),
						type: 'warning',
					})
					return false
				} else if (type == 2 && row.goodsNum >= row.goodsStock) {
					this.uToastShow({
						title: await this.getTransNew('最大数量不能大于最大库存'),
						type: 'warning',
					})
					return false
				}
				this.combinationList.map(item => {
					item.goodsNum = type == 1 && row.combinationId == item.combinationId ? item.goodsNum + 1 : item.goodsNum
					if (item.combinationId == combinationId) {
						item.goodsList.map(item2 => {
							if (type == 1) {
								// item2.goodsNum++
							} else {
								if (item2.goodsId == row.goodsId) {
									item2.goodsNum++
								}
							}
							return item2
						})
					}

					return item
				})
				this.combinationBasePrice(this.combinationList.find(item => row.combinationId == item.combinationId))
			},
			// 计算价格 
			async combinationBasePrice(item) {
				let {
					code,
					data,
					mesages
				} = await queryCombinationBasePrice(item);
				this.combinationList.map(item => {
					if (item.combinationId == data.combinationId) {
						item.combinationPrice = data.combinationPrice
						item.goodsOriginalPrice = data.originalPrice
					}
					return item
				})
			},
			// 一键下单
			async handleOneOrdering(rows) {
				let tokenFlag = await checkToken();
				if (!tokenFlag) {
					return
				}
				let rowsItem = rows;
				let itemsList = rowsItem.goodsList.map(item => {
					if (rowsItem.combinationType == 1) {
						return this.getSummaryParams(rowsItem, item)
					} else {
						if (item.goodsNum && item.checked) {
							return this.getSummaryParams(rowsItem, item)
						}
					}
				})
				let orderArr = [{
					shopId: this.goodsBases.shopId,
					shopName: this.goodsBases.shopName,
					invoiceActive: this.shopDetail.invoiceActive,
					wapShopLogo: this.shopDetail.wapShopLogo,
					goodsLowerLimit: 1,
					itemsList: itemsList.filter(item => item != null),
				}]
				let buyObj = {
					orderArr
				}
				uni.setStorageSync('buyObj', buyObj);
				this.publicVariable.buyObj = buyObj;
				uni.navigateTo({
					url: `/general/sureorder/sureorder?goodsId=${this.goodsBases.goodsId}&combinationType=${rowsItem.combinationType}&combinationId=${rowsItem.combinationId}&combinationGoodsNum=${rowsItem.goodsNum}`
				});
			},
			handleGoDetails(id,comId) {

				uni.navigateTo({
					url: `/pages/description/descrption?goodsId=${id}&commonId=${comId}`
				})
			},
			getSummaryParams(rowsItem, item) {
				return {
					commonId: item.commonId,
					goodsNum: rowsItem.combinationType == 1 ? (rowsItem.goodsNum * item.goodsNum) : item.goodsNum,
					maxSale: item.goodsStock,
					moq: 1,
					goodsId: item.goodsId,
					goodsName: item.goodsName,
					virtualGoods: 1,
					goodsImage: item.goodsImage,
					goodsPrice: item.goodsPrice,
					goodsStock: item.goodsStock,
					goodsSpecChineseValue: item.goodsSpec,
					activityType: 10,
					combinationType: rowsItem.combinationType
				}
			},
			getCombinationList(flag) {
				if (this.combinaShowAndHide) {
					return this.combinationList
				} else {
					return this.combinationList.slice(0, 2)
				}
			},
			uToastShow(obj) {
				this.$refs.uToast.show({
					title: obj.title,
					type: obj.type,
					message:obj.title,
					position: 'bottom'
				})
			},
			
		}
	}
</script>

<style scoped lang="scss">
	/* 组合商品 S */

	.combinationGoods {
		width: 100vw;
		min-height: 10rpx;
		background-color: white;
		margin: 12rpx 0rpx;
		padding: 15rpx 0rpx;
	}

	.combinationGoods .combinationTitle {
		text-align: center;
		color: var(--webColor);
	}

	.combinationMain {
		width: 100%;
		margin: 15rpx 0rpx;
		background-color: #e5e5e5;
		padding: 5rpx;
		border-radius: 10rpx;
		box-sizing: border-box;
	}

	.combinationGoods .combinationLi {
		width: 100%;
		border-radius: 15rpx;
		margin: 15rpx 0rpx;
		padding: 10rpx;
		background-color: white;
		box-sizing: border-box;
		position: relative;

		.titleName {
			position: absolute;
			top: 0rpx;
			left: 0rpx;
			color: white;
			z-index: 0;
			padding: 5rpx 8rpx;
			border-top-left-radius: 15rpx;
			border-bottom-right-radius: 15rpx;
			background-color: var(--webColor);
		}
	}



	.combinationGoods .combinationLiLeft {
		width: 100%;
		padding: 0rpx 15rpx;
		/* #ifndef H5 */
		display: flex;
		height: 340rpx;
		/* #endif */
		/* #ifdef H5 */
		::v-deep .uni-scroll-view-content {
			display: flex;
		}
		/* #endif */

		.uCheckboxGroup {
			flex-wrap: nowrap;
		}

		.combinationLiLeftGoodsItem {
			display: flex;
			align-items: center;

			.goodsItem {
				width: 175rpx;
				display: flex;
				flex-direction: column;
				position: relative;
			}

			.checked {
				width: 100%;
				height: 175rpx;
				position: absolute;
				right: 0;
				z-index: 1;
				display: flex;
				align-items: center;
				justify-content: center;
				background-color: rgba(0, 0, 0, .3);

				image {
					width: 50%;
					height: 50%;
				}
			}

			.goodsSpec,
			.textGoodsNum {
				font-size: 24rpx;
			}

			.goodsItem .goodsNum {
				width: 100%;
				height: 40rpx;
				border-bottom: 1rpx solid var(--rgba6);
				box-sizing: border-box;
				display: flex;
				justify-content: space-between;
				border-radius: 10rpx;

				& text:nth-of-type(1),
				& text:nth-of-type(3) {
					width: 40rpx;
					height: 100%;
					display: inline-block;
					text-align: center;
					line-height: 40rpx;
					background-color: var(--rgba2);
				}

				& text:nth-of-type(2) {
					height: 100%;
					text-align: center;
					line-height: 40rpx;
					display: inline-block;
				}
			}

			.goodsImage {
				width: 100%;
				height: 175rpx;
			}
		}

		.jiahao {
			width: 50rpx;
			height: 50rpx;
			margin: 0rpx 20rpx;
		}
	}

	.combinationGoods .combinationLiRight {
		width: 100%;
		height: 80rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 25rpx;
		box-shadow: 0px -3px 5px 0px #e5e5e5;

		.comGoodsNum {
			border-bottom: 1rpx solid var(--rgba6);
			border-radius: 10rpx;

			& text:nth-of-type(1),
			& text:nth-of-type(3) {
				width: 45rpx;
				height: 45rpx;
				display: inline-block;
				text-align: center;
				line-height: 45rpx;
				background-color: var(--rgba2);
			}

			& text:nth-of-type(2) {
				max-width: 70rpx;
				min-width: 45rpx;
				height: 45rpx;
				text-align: center;
				line-height: 45rpx;
				display: inline-block;
			}
		}


	}

	.oneOrdering {
		width: 150rpx;
		line-height: 40rpx;
		padding: 5rpx;
		font-size: 24rpx;
		color: white;
		margin: 0 auto;
		background-color: var(--webColor);
	}

	.red {
		color: red;
	}

	.fz11 {
		font-size: 22rpx;
	}

	/* 组合商品 E */
</style>