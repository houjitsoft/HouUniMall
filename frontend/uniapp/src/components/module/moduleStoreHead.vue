<template>
	<view class="moduleStoreHead" :style="{backgroundImage:'url('+propsobj.bgUrl+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover',paddingTop:propsobj.padding,paddingBottom:propsobj.padding,backgroundColor:propsobj.bgColor}">
		<view class="">
			<view class="store-logos" :style="{backgroundImage:'url('+propsobj.logoUrl+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'contain'}"></view>
			<text class="store-names span" v-if="propsobj.name==''" :style="{fontSize:propsobj.fzSize,color:propsobj.textColor}">{{ getTransNew('店铺名称')}}</text>
			<text class="store-names span" v-else :style="{fontSize:propsobj.fzSize,color:propsobj.textColor}">{{ getTransNew(propsobj.name)}}</text>
		</view>
		<view class="attention">
			<view class="attentionV1" @click="handleAttention">
				<icon type="value" class="iconfont icon-guanzhu" :class="className"></icon>
				<text>{{ getTransNew(attentionText)}}</text>
			</view>
			<view class="">
				<text>{{attentionNum}}{{ getTransNew('人关注')}}</text>
			</view>
		</view>
	</view>
</template>
<script>
	import {
		queryCheckFavoriteShoporNot,
		queryDeletefavoriteshop,
		queryAddfavoriteshop
	} from "@/api/pages/description"
	
	import {checkToken} from '@/utils/util.js'
	export default {
		
		name: 'moduleStoreHead',
		props: ['propsobj'],
		watch: {

		},
		data() {
			return {
				
				token: uni.getStorageSync('token'),
				attentionText: "关注",
				attentionData: uni.getStorageSync('attentionData'),
				className: 'activeNo',
				attentionNum: uni.getStorageSync('attentionData').shopCollect
			}
		},
		created() {
			this.fetchData();
		},
		methods: {
			
			fetchData() {
				queryCheckFavoriteShoporNot({
					shopId: this.attentionData.shopId
				}).then(res => {
					if (res.code == 1) {
						if (res.data == 0) {
							this.attentionText = '关注'
							this.className = 'activeNo'
						} else {
							this.attentionText = '已关注'
							this.className = "activeYes"
						}
					}
				})
			},
			handleAttention() {
				// 关注按钮
				checkToken().then((vdata) => {
					if (vdata) {
						if (this.attentionText == '已关注') {
							// 已关注-取消关注
							queryDeletefavoriteshop({
								token: vdata,
								shopIds: [String(this.attentionData.shopId)]
							}).then(res => {
								if (res.code == 1) {
									this.attentionText = '关注';
									this.attentionNum -= 1;
									this.className = 'activeNo';
								}
							})
						} else {
							// 未关注-添加关注
							queryAddfavoriteshop({
								token: vdata,
								shopId: this.attentionData.shopId,
								shopName: this.attentionData.shopName,
								shopLogo: this.attentionData.shopLogo
							}).then(res => {
								if (res.code == 1) {
									this.attentionText = '已关注';
									this.attentionNum += 1;
									this.className = "activeYes";
								}
							})
						}
					}
				})
			}
		}
	}
</script>

<style lang="scss">
	.moduleStoreHead {
		display: flex;
		justify-content: space-between;
		align-items: center;
		padding-right: 30rpx;
	}

	.store-logos {
		display: inline-block;
		margin-left: 20rpx;
		margin-right: 12rpx;
		width: 80rpx;
		height: 80rpx;
		border-radius: 100%;
		overflow: hidden;
		background-color: #ddd;
	}

	.store-names {
		display: inline-block;
		vertical-align: top;
		line-height: 80rpx;
	}

	.attention {
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.attention .attentionV1 {
		background-color: rgba(0, 0, 0, .2);
		padding: 5rpx 20rpx;
		border-radius: 25rpx;
		font-size: 24rpx;
	}

	.activeYes,
	.activeNo {
		font-size: 24rpx;
		padding-right: 10rpx;
	}

	.activeYes {
		color: red;
	}

	.activeNo {
		color: rgba(0, 0, 0, .4);
	}
</style>