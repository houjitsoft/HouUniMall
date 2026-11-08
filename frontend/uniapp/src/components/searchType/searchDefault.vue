<template>
	<view :style="temp">
		<view class="header" :style="{top:top}">
			<view class="inputModal" @tap="toListFn">
				<icon class="iconfont icon-search1 fz-40 align-middle"></icon>
				<text>{{  getTransNew('搜索您心怡的产品')}}</text>
			</view>
		</view>
		<!-- 主题内容 -->
		<view class="contentContainer clearfix style1">
			<view class="contentContainerLeft fl">
				<view v-for="(item, index) in leftList" :key="index">
					<view :class="index == indexLeft ? 'now' : ''" @tap="clickLeftTypeFn(item.catId,index)">{{ getTransNew(item.catName)}}
					</view>
				</view>
			</view>
			<view class="contentContainerRight fr">
				<!-- v-if="!isolateShopid" -->
				<view v-for="(i, index) in rightList" :key='index' >
					<view class="catTitle" v-if="i.secordCatName" :data-catid="i.secordcatId">{{ getTransNew(i.secordCatName)}}</view>
					<view class="catContainer clearfix bgf" v-if="i.gdsCats">
						<view class="li-item fl" v-for="(j,index2) in i.gdsCats" :key="index2">
							<view :data-catid="j.catId" @tap="clickRightFn(j.catId)">
								<view class="img-box" v-if="j.catPic" :style="{backgroundImage:'url('+j.catPic+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'contain'}">
								</view>
								<view class="img-box" v-else :style="{backgroundImage:'url('+imgUrl+'/default.png)',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'contain'}">
								</view>
								<view class="catName">{{ getTransNew(j.catName)}}</view>
							</view>
						</view>
					</view>
					<view class="catContainer clearfix bgf" v-else>
						<view class="catTitle">{{ getTransNew('暂无分类')}}</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	const app = getApp({allowDefault: true})
	import {
		queryDefaultList
	} from "@/api/pages/index"
	
	export default {
		
		data() {
			return {
				
				top: '44px',
				isolateShopid: this.publicVariable.thirdPart.shopId,
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				leftList: [],
				rightList: [],
				imgUrl: this.publicVariable.imgUrl,
				indexLeft: 0,
			};
		},
		props: {},
		
		mounted() {
			this.top = uni.getSystemInfoSync().statusBarHeight + Number(44) + 'px';
			// 左侧列表
			queryDefaultList({
				catParentId: 0,
				shopId:this.publicVariable.thirdPart.shopId || null
			}).then(res => {
				if (res.code == 1) {
					this.leftList = res.data[0].gdsCats;
					if(res.data[0].gdsCats.length>0){
						this.rightListFn(res.data[0].gdsCats[0].catId);
					}
					
				}
			})

		},
		methods: {
			
			// 右侧内容
			rightListFn(typeid) {
				queryDefaultList({
					catParentId: typeid || 0,
					shopId:this.publicVariable.thirdPart.shopId || null
				}).then(res => {
					if (res.code == 1) {
						this.rightList = res.data
					}
				})
			},
			clickLeftTypeFn(catId, index) {
				this.indexLeft = index;
				this.rightListFn(catId);
				uni.pageScrollTo({
					scrollTop: 0,
					duration: 300
				})
			},
			clickRightFn(catParentId) {
				uni.navigateTo({
					url: '/general/productlist/productlist?catId=' + catParentId
				});
			},
			clickRightFntow(catParentId) {
				uni.navigateTo({
					url: '/general/productlist/productlist?catId=' + catParentId
				});
			},
			// 头部跳转函数
			toListFn() {
				if (this.isolateShopid) { //品牌店铺
					uni.navigateTo({
						url: '/store/shopsearch/shopsearch'
					})
				} else { // 全平台
					uni.reLaunch({
						url: '/general/searchpage/searchpage'
					})
				}
			}
		}
	};
</script>
<style>
	@import "./search.css";
</style>