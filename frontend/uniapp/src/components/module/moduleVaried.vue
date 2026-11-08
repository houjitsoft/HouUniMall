<template>
	<view class="moduleVaried" :style="temp">
		<view class="goods-items-style2 ul pt20">
			<view class="waterfall-box h-flex-x h-flex-y goods-lists1">
				<view style="width: 50%;">
					<Waterfall v-for="(item,index) in leftList" :key="index" :params="item" :propsobjs="propsobj" tag="left" :index="index" @click="goDetails(item)"></Waterfall>
				</view>
				<view style="width: 50%;">
					<Waterfall v-for="(item,index) in rightList" :key="index" :params="item" :propsobjs="propsobj" tag="right" :index="index" @click="goDetails(item)"></Waterfall>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import Waterfall from "@/components/waterfall/waterintro";
	import {checkToken} from '@/utils/util.js'
	let app = getApp({allowDefault: true});
	import {
		intelligentRecommendationGoods
	} from "@/api/pages/index"
	
	export default {
		name: 'moduleVaried',
		props: ['propsobj'],
		watch: {

		},
		components: {
			Waterfall
		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
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
				params:{
					page:1,
					limit:10
				}
			}
		},
		mounted() {
			this.getGoodsList();
		},
		methods: {
			getGoodsList(){
				intelligentRecommendationGoods(this.params).then(res => {
					if (res.code == 1) {
						this.addList(res.data);
					}
				})
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
				// this.leftList = [];
				// this.rightList =  [];
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
			reachBottomFun2(type){
				if(type==1){
					this.ajax.page=this.params.page=1;
				}else{
					this.params.page++;
				}
				
				this.getGoodsList();
			},
			goDetails(item) {
				uni.navigateTo({
					url: '/pages/description/descrption?goodsId=' + item.goodsId + '&commonId=' + item.commonId
				})
			},
		},
		
	}
</script>

<style lang="scss">
	.pt20 {
		padding-top: 20rpx !important;
	}

	.pl20 {
		padding-left: 20rpx !important;
	}
</style>