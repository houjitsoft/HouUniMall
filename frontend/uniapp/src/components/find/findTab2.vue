<template>
	<view class="find-contents index-follow">
		<view v-if="list.length>0" class="follow-find-content">
			<view class="li" v-for="(item,index) in list" :key="index">
				<view class="heads flex">
					<view class="left flex1" @click="toPerson(item.userId)">
						<view class="flex wp100">
							<view class="img-box" :style="{backgroundImage:'url('+item.userAvatar+')',backgroundSize:'cover',backgroundRepeat:'no-repeat',backgroundPosition:'center'}">
							</view>
							<view class="flex1">
								<text class="name">{{ getTransNew(item.userName)}}</text>
								<text class="block fans">{{item.followerNum}}{{ getTransNew('粉丝')}}</text>
							</view>
						</view>
					</view>
					<view class="right" v-if="userId != item.userId">
						<button v-if="item.hasFollowed==1" @click="attention(item.userId,0,index,1)">{{ getTransNew('已关注')}}</button>
						<button v-else @click="attention(item.userId,1,index,1)">{{ getTransNew('关注ta')}}</button>
					</view>
				</view>
				<view class="detail-conts" @click="goDetails(item.articleId,item.articleType)">
					<view class="pictureBox pictureA" :style="{backgroundImage:'url('+item.articleCover+')',backgroundSize:'cover',backgroundRepeat:'no-repeat',backgroundPosition:'center'}">
						<icon v-if="item.articleType==2" class="iconfont icon-bofang follow-article-type"></icon>
					</view>
					<view class="text-cont">
						<text class="hd text-overflow">{{ getTransNew(item.articleContent)}}</text>
					</view>

				</view>

			</view>
		</view>
		<view v-else class="recordListEmpty pt100">
			<view class="recordListEmptyContainer">
				<image class="deleteImage2" :src="imgUrl+'followDefault.png'" mode="widthFix"></image>
				<view class="mt10 fz28 text-desc">{{ getTransNew('还没有关注的人哦')}}</view>
			</view>

		</view>
		<!-- 为你推荐   -->
		<view class="intro-lists" v-if="totalCount<=10 ||introShow">
			<view class="intro-title-head tc">
				<image mode="widthFix" :src="iconLeft"></image>
				<text class="intro-title">{{ getTransNew('为你推荐')}}</text>
				<image mode="widthFix" :src="iconRight"></image>
			</view>
			<view v-if="introList.length>0" class="follow-find-content">
				<view class="li" v-for="(item,index) in introList" :key="index">
					<view class="heads flex">
						<view class="left flex1 tl" @click="toPerson(item.userId)">
							<view class="flex wp100">
								<view class="img-box" :style="{backgroundImage:'url('+item.userAvatar+')',backgroundSize:'cover',backgroundRepeat:'no-repeat',backgroundPosition:'center'}">
								</view>
								<view class="flex1">
									<text class="name">{{ getTransNew(item.userName)}}</text>
									<text class="block fans">{{item.followerNum}}{{ getTransNew('粉丝')}}</text>
								</view>
							</view>
						</view>
						<view class="right" v-if="userId != item.userId">
							<button v-if="item.hasFollowed==1" @click="attention(item.userId,0,index,2)">{{getTransNew('已关注' )}}</button>
							<button v-else @click="attention(item.userId,1,index,2)">{{ getTransNew('关注ta')}}</button>
						</view>
					</view>
					<view class="detail-conts" @click="goDetails(item.articleId,item.articleType)">
						<view class="pictureBox pictureA" :style="{backgroundImage:'url('+item.articleCover+')',backgroundSize:'cover',backgroundRepeat:'no-repeat',backgroundPosition:'center'}">
							<icon v-if="item.articleType==2" class="iconfont icon-bofang follow-article-type"></icon>
						</view>
						<view class="text-cont">
							<text class="hd text-overflow">{{ getTransNew(item.articleContent)}}</text>
						</view>

					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	let app = getApp({allowDefault: true});
	import {
		queryRandomInvitation,
		queryArticleUserList,
		queryUnitFollow
	} from "@/api/find/index"
	
	export default {
		name: "findTab2",
		props: ['keywords','userId'],
		data() {
			return {
				
				introShow: false,
				imgUrl: this.publicVariable.imgUrl,
				iconLeft: this.publicVariable.imgUrl + 'zuo.png',
				iconRight: this.publicVariable.imgUrl + 'you.png',
				totalCount: 0,
				list: [],
				introList: [],
				params: {
					page: 1,
					limit: 10,
					queryStr: ''
				},
				attentionParams: {
					followedUserId: '',
					followFlag: 1
				},
			};
		},
		mounted() {
			this.fetchData();
			this.getIntroData();
		},
		methods: {
			getIntroData() {
				this.introList = []
				queryRandomInvitation().then(res => {
					if (res.code == 1) {
						this.introList = res.data;
					}
				})
			},
			loadMore() {
				var that = this;
				var count = that.params.page * that.params.limit;
				if (that.totalCount > count) {
					that.params.page += 1;
					that.fetchData();
				} else {
					return false;
				}
			},
			fetchData() {
				this.params.queryStr = this.keywords;
				this.list = [];
				this.totalCount = 0;
				queryArticleUserList(this.params).then(res => {
					if (res.code == 1 && res.data) {
						if (this.params.page == 1) {
							var tempArr = res.data.list;
							
						} else {
							var tempArr = this.list;
							tempArr = tempArr.concat(res.data.list);
						}
						this.list = tempArr;
						this.totalCount = res.data.totalCount;
					}else{
						this.list = []
					}
				})
			},
			toPerson(id) {
				uni.navigateTo({
					url: '../../find/homePage/homePage?id=' + id + '&isMine=0'
				})
			},
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
			attention(userId, status, index, type) {
				this.attentionParams.followFlag = status;
				this.attentionParams.followedUserId = userId;
				queryUnitFollow(this.attentionParams).then(res => {
					if (res.code == 1) {
						if (type == 1) {
							if (status == 0) {
								this.$set(this.list[index], 'hasFollowed', 0)
							} else {
								this.$set(this.list[index], 'hasFollowed', 1)
							}

						} else {
							if (status == 0) {
								this.$set(this.introList[index], 'hasFollowed', 0)
							} else {
								this.$delete(this.introList[index], 'hasFollowed');
								this.$set(this.introList[index], 'hasFollowed', 1)
							}
							
						}
						this.fetchData();
						this.getIntroData();
					}else{
						uni.showToast({
							title:res.message,
							icon:'none'
						})
					}
				})
			},
		},
		/**
		 * 页面上拉触底事件的处理函数
		 */
		onReachBottom() {

			var count = this.params.limit * this.params.page;
			if (this.totalCount > count) {
				this.params.page += 1;
				this.fetchData();
			} else {
				if (!this.introShow) {
					this.introShow = true;
				}
				return false;
			}
		},
	}
</script>

<style>
	@import "./findTab.css";
</style>