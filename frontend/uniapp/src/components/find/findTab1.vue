<template>
	<view class="find-contents">
		<swiper class="find-swiper" :circular="true" next-margin="300rpx" :autoplay="true" :interval="5000" :duration="500" indicator-active-color="#FF4139" indicator-color="#AEAEAE">
			<swiper-item class="li" v-for="(item,index) in topicLists" :key="index" @click="goShows(item.tagId)">
				<view class="li-cont">
					<view class="img-box" :style="{backgroundImage:'url('+item.tagCover+')',backgroundSize:'cover',backgroundRepeat:'no-repeat',backgroundPosition:'center'}"></view>
					<view class="topic-con">
						<text class="block tit">{{ getTransNew(item.tagContent)}}</text>
						<text class="des">{{ getTransNew(item.tagSubtitle)}}</text>
					</view>
				</view>
			</swiper-item>
		</swiper>
		<scroll-view :scroll-x="true" class="topic-scroll">
			<view class="scroll-view-item" :class="[isActive==null?'active':'']" @click="searchClass('',null)">{{ getTransNew('全部')}}<text class="line"></text></view>
			<view class="scroll-view-item" :class="[isActive==index?'active':'']" v-for="(item,index) in classLists" :key="index" :data-classid="item.classId" @click="searchClass(item.classId,index)">{{ getTransNew(item.className)}}<text class="line"></text></view>
		</scroll-view>
		<view v-if="totalCount>0">
			<view class="waterfall-box h-flex-x h-flex-y goods-lists1">
				<view style="width: 50%;" v-if="leftList.length>0">
					<Waterfall v-for="(item,index) in leftList" :key="index" :params="item" tag="left" :index="index" @height="onHeight" @click="goDetails(item.articleId,item.articleType)"></Waterfall>
				</view>
				<view style="width: 50%;" v-if="rightList.length>0">
					<Waterfall v-for="(item,index) in rightList" :key="index" :params="item" @height="onHeight" @click="goDetails(item.articleId,item.articleType)" tag="right" :index="index"></Waterfall>
				</view>
			</view>
		</view>
		<view v-else class="recordListEmpty pt200">
			<view class="recordListEmptyContainer">
				<image class="deleteImage2" :src="imgUrl+'followDefault.png'" mode="widthFix"></image>
				<view class="mt10 fz28">{{ getTransNew('还没有心得秀呢~')}}</view>
			</view>
		</view>

	</view>
</template>

<script>
	let app = getApp({allowDefault: true});
	import Waterfall from "@/components/waterfall/waterfall";

	import {
		queryCommunityTopics,
		queryCommunityClassification,
		queryArticleAllInfoList
	} from "@/api/find/index"
	
	export default {
		name: "findTab1",
		components: {
			Waterfall
		},
		props: ['keywords'],
		data() {
			return {
				isActive: null,
				imgUrl: this.publicVariable.imgUrl,
				classLists: [],
				topicLists: [],
				leftHeight: 0,
				rightHeight: 0,
				leftList: [],
				rightList: [],
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
				totalCount: 1,
				params: {
					tagContent: '',
					articleTitle: '',
					userName: '',
					page: 1,
					limit: 10,
					auditState: 1,
					token: '',
					userId: '',
					catId: '',
					tagId: '',
					queryStr: ''

				}
			};
		},
		mounted() {
			this.getList();
			this.getTopicList();
			this.getClassList();
		},
		watch: {},

		// 下拉刷新
		onPullDownRefresh() {
			// 正常情况下接口返回应该很会很快。故意延迟调用，让用户有在刷新的体验感
			setTimeout(() => {
				this.ajax.page = 1;
				this.leftHeight = 0;
				this.rightHeight = 0;
				this.ajax.load = true;
				this.getList();
			}, 800);
		},

		methods: {
			
			loadMore() {
				var that = this;
				var count = that.params.page * that.params.limit;
				if (that.totalCount > count) {
					that.params.page += 1;
					that.getList();
				} else {
					return false;
				}
			},
			searchClass(classId, index) {
				this.params.catId = classId;
				this.getList(1);
				this.isActive = index;
			},
			goShows(id) {
				uni.navigateTo({
					url: '/find/relShows/relShows?tagid=' + id
				})
			},
			//获取分类
			getClassList() {
				queryCommunityTopics().then(res => {
					if (res.code == 1) {
						this.classLists = res.data;
					}
				})
			},
			//获取话题
			getTopicList() {
				queryCommunityClassification().then(res => {
					if (res.code == 1) {
						this.topicLists = res.data;
					}
				})
			},
			// 获取心得
			getList(page) {
				var that = this;
				this.params.queryStr = this.keywords;
				queryArticleAllInfoList(this.params).then(res=>{
					if(res.code == 1){
						if (res.data.list.length > 0) {
							if (page == 1) {
								that.leftList = []
								that.rightList = []
								that.leftHeight = 0
								that.rightHeight = 0
							}
							this.addList(res.data.list);
						}
						this.totalCount = res.data.totalCount;
					}
				})
			},
			// 监听高度变化
			onHeight(height, tag) {
				if (tag == 'left') {
					this.leftHeight += height;
				} else {
					this.rightHeight += height;
				}
			},
			// 组件点击事件
			goDetails(id, type) {
				if (type == 1) {
					//图片
					uni.navigateTo({
						url: '/find/details/details?id=' + id
					})
				} else {
					//视频
					uni.navigateTo({
						url: '/find/video/video?id=' + id
					})
				}
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
				this.leftList =[];
				this.rightList =[];
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


		}
	}
</script>

<style>
	@import "./findTab.css";
</style>