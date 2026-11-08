<template>
	<view class="moduleGoods" :style="{temp,paddingTop:propsobj.padTop,paddingBottom:propsobj.padBom,paddingLeft:propsobj.pageMargin,paddingRight:propsobj.pageMargin}">
		<view :class="[propsobj.isRadius==2?'radius':'']">
		<template v-if="style=='1'">
			<view class="goods-style1 li" v-for="(item,index) in currentProducts" :key='item.commonName' @click="goDetail(item.goodsId,item.commonId)">
				<view class="goods-img-wrap">
					<up-lazy-load class="wp100 goods-default-img" img-mode="widthFix" :image="item.commonImage" threshold="100" :error-img="defaultImg"></up-lazy-load>
					<goods-badge :propsobj="propsobj"></goods-badge>
				</view>
				<view class="goods-text">
					<text class="goods-tit goods-tit--bold title-overflow">{{ getTransNew(item.commonName)}}</text>
					<view class="flex space-between goods-text-module">
						<view class="goods-price">
							<text class="symbol" :style="{color:themeData.contMxColor}">{{currencySymbol()}}</text>
							<text class="pri span" :style="{color:themeData.contMxColor}">{{ formatGoodsPrice(item).int }}</text>
							<text class="dot-pri" v-if="formatGoodsPrice(item).dec" :style="{color:themeData.contMxColor}">.{{ formatGoodsPrice(item).dec }}</text>
							<!-- <del v-if="propsobj.priceThrough" class="ori-pri">¥{{item.commonMarketPrice}}</del> -->
						</view>
						<view class="goods-sale flex">
							<text class="span" v-if="propsobj.sale">{{getTransNew('已售' )}}{{item.commonSalenum}}{{ getTransNew('件')}}</text>
							<text v-if="propsobj.cart && propsobj.cartStyle=='1'" :style="{color:themeData.contMxColor}" class="iconfont icon-cart" @click.stop="addCart(item.goodsId)"></text>
							<button class="btn-goods" v-if="propsobj.cart && propsobj.cartStyle=='2'" :style="{backgroundColor:themeData.contMxColor}" @click.stop="goBuy(item.goodsId,item.commonId)">{{getTransNew(propsobj.cartText)}}</button>
						</view>
					</view>
				</view>
			</view>
		</template>
		<template v-if="style=='2'">
			<view class="goods-items-style2 ul pt20" v-if="propsobj.waterFull">
				<view class="waterfall-box h-flex-x h-flex-y goods-lists1">
					<view style="width: 50%;">
						<Waterfall v-for="(item,index) in leftList" :key="index" :params="item" :propsobjs="propsobj" tag="left" :index="index"></Waterfall>
					</view>
					<view style="width: 50%;">
						<Waterfall v-for="(item,index) in rightList" :key="index" :params="item" :propsobjs="propsobj" tag="right" :index="index"></Waterfall>
					</view>
				</view>
			</view>
			<view class="goods-items-style2 ul pt20" v-else>
				<view class="li" v-for="(item,index) in currentProducts" :key="index" @click="goDetail(item.goodsId,item.commonId)">
					<view class="img-box goods-img-wrap">
						<up-lazy-load class="wp100" img-mode="widthFix" :image="item.commonImage" threshold="100" :error-img="defaultImg"></up-lazy-load>
						<goods-badge :propsobj="propsobj"></goods-badge>
					</view>
					<view class="goods-text">
						<text class="goods-tit goods-tit--bold title-overflow block">{{getTransNew(item.commonName)}}</text>
						<text v-if="propsobj.sale" class="goods-sale-style2">{{getTransNew('已售')}}{{item.commonSalenum}}{{getTransNew('件')}}</text>
						<view class="flex space-between goods-text-module">
							<view class="goods-price">
								<text class="symbol" :style="{color:themeData.contMxColor}">{{currencySymbol()}}</text>
								<text class="pri span" :style="{color:themeData.contMxColor}">{{ formatGoodsPrice(item).int }}</text>
							<text class="dot-pri" v-if="formatGoodsPrice(item).dec" :style="{color:themeData.contMxColor}">.{{ formatGoodsPrice(item).dec }}</text>
							</view>
							<view class="goods-sale">
								<text v-if="propsobj.cart" class="iconfont icon-cart" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.goodsId)"></text>
							</view>
						</view>
					</view>

				</view>
			</view>
		</template>
		<template v-if="style=='3'">
			<view class="goods-items-style2 goods-items-style3 bgf pt20 pl20">
				<view class="li" v-for="(item,index) in currentProducts" :key="index" @click="goDetail(item.goodsId,item.commonId)">
					<view class="img-box goods-img-wrap" :style="{backgroundImage:'url('+item.commonImage+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}">
						<goods-badge :propsobj="propsobj"></goods-badge>
					</view>
					<view class="goods-text">
						<text class="goods-tit title-overflow">{{getTransNew(item.commonName)}}</text>
						<view class="flex space-between goods-text-module">
							<view class="goods-price">
								<text class="symbol" :style="{color:themeData.contMxColor}">{{currencySymbol()}}</text>
								<text class="pri span" :style="{color:themeData.contMxColor}">{{ formatGoodsPrice(item).int }}</text>
							<text class="dot-pri" v-if="formatGoodsPrice(item).dec" :style="{color:themeData.contMxColor}">.{{ formatGoodsPrice(item).dec }}</text>
							</view>
							<view class="goods-sale">
								<text v-if="propsobj.cart" class="iconfont icon-cart" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.goodsId)"></text>
							</view>
						</view>
					</view>

				</view>
			</view>
		</template>
		<template v-if="style=='4'">
			<view class="goods-items-style4 bgf pt20">
				<view class="li" v-for="(item,index) in currentProducts" :key="index" @click="goDetail(item.goodsId,item.commonId)">
					<view class="img-box goods-img-wrap" :style="{backgroundImage:'url('+item.commonImage+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}">
						<goods-badge :propsobj="propsobj"></goods-badge>
					</view>
					<view class="goods-text flex1">
						<view>
							<text class="goods-tit goods-tit--bold title-overflow span">{{getTransNew(item.commonName)}}</text>
							<text v-if="propsobj.sale" class="goods-sale-style2">{{getTransNew('已售' )}}{{item.commonSalenum}}{{getTransNew('件')}}</text>
						</view>
						<view class="flex space-between goods-text-module">
							<view class="goods-price">
								<text class="symbol" :style="{color:themeData.contMxColor}">{{currencySymbol()}}</text>
								<text class="pri span" :style="{color:themeData.contMxColor}">{{ formatGoodsPrice(item).int }}</text>
							<text class="dot-pri" v-if="formatGoodsPrice(item).dec" :style="{color:themeData.contMxColor}">.{{ formatGoodsPrice(item).dec }}</text>
								<!-- <del v-if="propsobj.priceThrough" class="ori-pri">¥{{item.commonMarketPrice}}</del> -->
							</view>
							<view class="goods-sale">
								<text v-if="propsobj.cart && propsobj.cartStyle=='1'" class="iconfont icon-cart" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.goodsId)"></text>
								<button class="btn-goods" v-if="propsobj.cart && propsobj.cartStyle=='2'" @click.stop="goBuy(item.goodsId,item.commonId)" :style="{backgroundColor:themeData.contMxColor}">{{getTransNew(propsobj.cartText)}}</button>
							</view>
						</view>
					</view>
				</view>
			</view>
		</template>
		<template v-if="style=='5'">
			<view class="goods-items-style2 goods-items-style5">
				<scroll-view class="goods-scroll" scroll-x='true'>
					<view class="li tc" v-for="(item,index) in currentProducts" :key="index" @click="goDetail(item.goodsId,item.commonId)">
						<view class="img-box goods-img-wrap" :style="{backgroundImage:'url('+item.commonImage+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}">
							<goods-badge :propsobj="propsobj"></goods-badge>
						</view>
						<view class="goods-text">
							<text class="goods-tit title-overflow">{{ getTransNew(item.commonName)}}</text>
							<view class="flex space-between goods-text-module">
								<view class="goods-price">
									<text class="symbol" :style="{color:themeData.contMxColor}">{{currencySymbol()}}</text>
									<text class="pri span" :style="{color:themeData.contMxColor}">{{ formatGoodsPrice(item).int }}</text>
							<text class="dot-pri" v-if="formatGoodsPrice(item).dec" :style="{color:themeData.contMxColor}">.{{ formatGoodsPrice(item).dec }}</text>
								</view>
								<view class="goods-sale">
									<text v-if="propsobj.cart" class="iconfont icon-cart" :style="{color:themeData.contMxColor}" @click.stop="addCart(item.goodsId)"></text>
								</view>
							</view>
						</view>

					</view>
				</scroll-view>
			</view>
		</template>
		</view>
	</view>
</template>

<script>
	import Waterfall from "@/components/waterfall/waterfallIndex";
	import GoodsBadge from "@/components/module/goodsBadge.vue";
	import {checkToken} from '@/utils/util.js'
	import {
	  startAutoSwitch,
	  calculateCurrentProducts
	} from '@/utils/periodicUtils';
	let app = getApp({allowDefault: true});
	import {
		queryShoppingCart
	} from "@/api/pages/description"
	
	export default {
		name: 'moduleGoods',
		props: ['propsobj'],
		components: {
			Waterfall,
			GoodsBadge
		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				defaultImg:this.publicVariable.defaultImg,
				themeData: this.publicVariable.themeData,
				token: uni.getStorageSync('token'),
				leftList: [],
				rightList: [],
				leftHeight: 0,
				rightHeight: 0,
				ajax: {
					// 是否可以加载
					load: true,
					// 加载中提示文字
					loadTxt: '',
					// 每页的请求条件
					rows: 10,
					// 页码
					page: 1,
				},
				timer:null,
				currentProducts:[],
				style:''
			}
		},
		watch:{
			propsobj: {
				handler(newVal, oldVal) {
					if(newVal.randomEnabled){
						this.updateCurrentProducts();
						this.startTimerGoods();
					}else{
						this.currentProducts=newVal.items;
						if(oldVal&&oldVal.style=='2'&&oldVal.waterFull){
							this.addList(this.currentProducts);
						}
					}
				},
				immediate: true,
				deep: true
			}
		},
		mounted() {
			this.style=this.propsobj.style;
			if(this.propsobj.randomEnabled){
				this.updateCurrentProducts();
				this.startTimerGoods();
			}else{
				this.currentProducts=this.propsobj.items;
				if(this.propsobj.style='2'&&this.propsobj.waterFull){
					this.addList(this.currentProducts);
				}
			}
		},
		methods: {
			//切换商品
			updateCurrentProducts() {
			  const result = calculateCurrentProducts(
				this.propsobj.items, 
				this.propsobj.goodsDays, 
				this.propsobj.goodsNum,
				this.propsobj.startDate
			  );
			  this.currentProducts = result.products;
			  if(this.propsobj.style='2'&&this.propsobj.waterFull){
				this.addList(this.currentProducts);
			  }
			},
			startTimerGoods() {
			  // 清理之前的定时器
			  if (this.timer) {
				this.timer.clear();
			  }
			  
			  // 启动新的定时器
			  this.timer = startAutoSwitch(this.propsobj.goodsDays, () => {
				this.updateCurrentProducts();
			  }, this.propsobj.startDate);
			},
			addList(res) {
				// 获取到的数据，请注意数据结构
				if (!res || res.length < 1) {
					this.ajax.loadTxt = '没有更多了';
					return;
				}
		
				// 左右列表高度的差
				let differ = this.leftHeight - this.rightHeight;
				// 计算差值，用于确定优先插入那一边
				let differVal = 0;
		
				// 初始化左右列表的数据
				let i = differ > 0 ? 1 : 0;
		
				let [left, right] = [
					[],
					[]
				];
			
			
				// 获取插入的方向
				let getDirection = (index) => {
					/* 左侧高度大于右侧超过 600px 时，则前3条数据都插入到右边 */
					if (differ >= 600 && index < 3) {
						differVal = 1;
						return 'right';
					}
		
					/* 右侧高度大于左侧超过 600px 时，则前3条数据都插入到左边 */
					if (differ <= -600 && index < 3) {
						differVal = -1;
						return 'left';
					}
		
					/* 左侧高度大于右侧超过 350px 时，则前2条数据都插入到右边 */
					if (differ >= 350 && index < 2) {
						return 'right';
					}
					/* 右侧高度大于左侧超过 350px 时，则前2条数据都插入到左边 */
					if (differ <= -350 && index < 2) {
						differVal = -1;
						return 'left';
					}
		
					/* 当前数据序号为偶数时，则插入到左边 */
					if ((i + differVal) % 2 == 0) {
						return 'left';
					} else {
						/* 当前数据序号为奇数时，则插入到右边 */
						return 'right';
					}
				}
		
				// 将数据源分为左右两个列表，容错差值请自行根据项目中的数据情况调节
				res.forEach((item, index) => {
					if (getDirection(index) == 'left') {
						left.push(item);
					} else {
						right.push(item);
					}
					i++;
				});
		
				// 将左右列表的数据插入，第一页时则覆盖
				this.leftList = [];
				this.rightList =  [];
				if (this.ajax.page == 1) {
					this.leftList = left;
					this.rightList = right;
					uni.stopPullDownRefresh();
				} else {
					this.leftList = [...this.leftList, ...left];
					this.rightList = [...this.rightList, ...right];
				}
		
				this.ajax.load = true;
				this.ajax.loadTxt = '上拉加载更多';
				this.ajax.page++;
			},		
			// 加入购物车
			addCart(id) {
				checkToken().then(async (vdata) => {
					if (vdata) {
						queryShoppingCart({
							goodsId: id,
							goodsNum: 1
						}).then(async res => {
							if (res.code == 1) {
								uni.showToast({
									icon:'none',
									title:await this.getTransNew("成功加入购物车")
								})
							} else {
								uni.showToast({
									icon: 'none',
									title:await this.getTransNew(res.message)
								})
							}
						})
					} else {
						uni.showToast({
							icon: 'none',
							title: await this.getTransNew('请先登录')
						})
						return false;
					}
				})



			},
			// 立即购买
			goBuy(goodsid, commonid) {
				uni.navigateTo({
					url: '../../pages/description/descrption?goodsId=' + goodsid + '&commonId=' + commonid
				})
			},
			// 进去详情
			goDetail(goodsId, commonId) {
				uni.navigateTo({
					url: '../../pages/description/descrption?goodsId=' + goodsId + '&commonId=' + commonId
				})
			},
			formatGoodsPrice(item) {
				if (item.commonPrice == '*') {
					return { int: '***', dec: '' }
				}
				const raw = this.getRatePrice(item.commonPrice)
				const [int, dec] = String(raw).split('.')
				return { int, dec: dec || '' }
			}
		}
	}
</script>

<style lang="scss">
	.moduleBg{
		padding:20rpx 24rpx;
		background:#fff;
		&.radius{
			border-radius:16rpx;
		}
	}
	.pt20 {
		padding-top: 20rpx !important;
	}

	.pl20 {
		padding-left: 20rpx !important;
	}

	.iblock {
		display: inline-block;
	}

	.flex {
		display: flex;
	}

	.space-between {
		justify-content: space-between;
	}

	.img-box {
		display: inline-block;
		width: 100%;
		height: 332rpx;
		overflow:hidden;
	}

	.goods-img-wrap {
		position: relative;
	}

	.goods-style1 .goods-img-wrap {
		::v-deep .goods-badge,
		::v-deep .goods-badge-custom {
			left: 20rpx;
			top: 20rpx;
		}
	}

	.goods-style1 {
		padding-bottom: 20rpx;
		margin-bottom: 20rpx;
		background: #fff;
	}

	.goods-default-img {
		padding: 20rpx;
		padding-bottom: 0;
		box-sizing: border-box;
	}

	.goods-price {
		display: inline-flex;
		align-items: baseline;
	}

	.goods-price .rmb,
	.goods-price .pri {
		margin-right: 6rpx;
		font-size: 32rpx;
	}

	.goods-price .symbol {
		font-size: 22rpx;
		font-weight: 600;
		margin-right: 2rpx;
	}

	.goods-price .pri {
		font-size: 32rpx;
	}

	.goods-price .dot-pri,
	.goods-price .ori-pri {
		font-size: 26rpx;
	}

	.goods-price .rmb,
	.goods-price .pri,
	.goods-price .dot-pri {
		font-weight: bold;
		color: #fa6868;
	}

	.goods-tit {
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		line-clamp: 2;
		overflow: hidden;
		text-overflow: ellipsis;
		word-break: break-word;
		width: 100%;
		max-width: 100%;
		margin-bottom: 12rpx;
		font-size: 28rpx;
		line-height: 1.4;
		height: calc(1.4em * 2);
		max-height: calc(1.4em * 2);
		color: #383d41;
		white-space: normal;
	}

	.goods-tit--bold {
		font-weight: 700;
	}

	.goods-text-module {
		// padding-top: 10rpx;
	}

	.goods-text-module .del {
		font-size: 24rpx;
		margin-left: 8rpx;
		color: #9e9e9e;
	}

	.goods-text-module .icon-cart {
		font-size: 32rpx;
		color: #fa6868;
	}

	.goods-sale text {
		display: inline-block;
		margin-right: 14rpx;
		vertical-align: middle;
		line-height: 44rpx;
		font-size: 24rpx;
		color: #5c5c5c;
	}
	.goods-items-style2 {
		// padding: 0 10rpx;
		overflow: hidden;
	}

	.goods-items-style2 {
		.li {
			display: inline-block;
			margin: 0 1% 20rpx;
			// padding:0 10rpx;
			width: 48%;
			vertical-align: top;
			box-sizing: border-box;
			font-size: 0;
			background: #fff;
			border-radius: 15rpx;
			overflow: hidden;
		}

		.img-box {
			height: auto;
		}
	}

	.goods-text {
		padding: 20rpx;
	}

	.goods-sale-style2 {
		display: block;
		margin-bottom: 16rpx;
		font-size: 24rpx;
		color: #9e9e9e;
		margin-top: 10rpx;
	}

	.goods-items-style2 .icon-cart {
		font-size: 36rpx;
	}

	.goods-items-style4 {
		padding: 10rpx 20rpx;
	}

	.goods-items-style4 .li {
		display: flex;
		margin-bottom: 20rpx;
	}

	.goods-items-style4 .img {
		width: 200rpx;
		height: 200rpx;
	}

	.goods-items-style4 .goods-text {
		padding-top: 0;
		padding-bottom: 0;
	}

	.goods-items-style4 .goods-text-module {
		padding-top: 30rpx;
	}

	.goods-items-style5 {
		overflow: hidden;
	}

	.goods-items-style5 .li {
		width: 200rpx;
		flex-shrink: 0;
	}

	.goods-items-style5 .goods-tit {
		font-size: 24rpx;
	}

	.btn-goods {
		margin-top: -6rpx;
		padding: 0;
		line-height: 60rpx;
		height: 60rpx;
		width: 172rpx;
		font-size: 24rpx;
		color: #fff;
		background: #fd3d53;
		border: none;
		border-radius: 4rpx;
		vertical-align: top;
	}

	.goods-items-style3 .img-box {
		height: 214rpx;
	}

	.goods-items-style3 .li {
		padding: 10rpx;
		width: 31.3%;
		// flex:0 0 120px;
	}

	.goods-items-style3 .li .rmb,
	.goods-items-style5 .li .rmb {
		font-size: 26rpx;
	}

	.goods-items-style3 .li .icon-cart,
	.goods-items-style5 .li .icon-cart {
		font-size: 28rpx;
		margin-top:-5rpx;
	}

	.goods-items-style3 .goods-text,
	.goods-items-style5 .goods-text {
		padding: 20rpx 0 0;
	}

	.goods-items-style4 .li .img-box {
		display: inline-block;
		width: 200rpx;
		height: 200rpx;
	}

	.goods-items-style5 .li .img-box {
		display: inline-block;
		width: 100%;
		height: 180rpx;
	}

	.goods-scroll {
		white-space: nowrap;
	}
	.h-flex-x{
		margin-bottom:10rpx !important;
	}
</style>