<template>
	<view class="moduleClass">
		<scroll-view :scroll-left="scrollTop" scroll-x="true" class="scroll-view-class" :show-scrollbar="false">
			<view class="classUls" :style="{marginTop:propsobj.padTop,marginBottom:propsobj.padBom,backgroundColor:propsobj.bgColor,backgroundImage:'url('+propsobj.bgImg+')',backgroundRepeat:'no-repeat',backgroundPosition:'center',backgroundSize:'cover'}" >
				<view class="li active" :style="{borderColor:propsobj.textColor}" v-if="shopId==null||shopId==''">
					<text class="text" :style="{color:propsobj.textColor}">{{ getTransNew('首页')}}</text>
				</view>
				<view class="li" @click="searchList(item.catId)" v-for="(item,index) in classLists" :key="item.catId" :style="{borderColor:propsobj.textColor}">
					<text class="text" :style="{color:propsobj.textColor}">{{item.catName}}</text>
				</view>
			</view>
		</scroll-view>
	</view>
</template>

<script>
	import {
		queryRankCat
	} from "@/api/pages/index"
	import {queryAllOneCat} from "@/api/store/index"
	const app = getApp({allowDefault: true});
	export default {
		
		name: 'moduleSearch',
		props: ['propsobj','shopId'],
		data() {
			return {
				classLists:[]
			}
		},
		mounted(){
			this.getClassList();
		},
		methods: {
			getClassList() {
				if(this.shopId!=null&&this.shopId!=''){//店铺分类
					queryAllOneCat(this.shopId).then(res => {
						if (res.code == 1) {
							this.classLists = res.data.map(item=>{
								return {
									catName:item.shopGoodsCatName,
									catId:item.shopGoodsCatId
								}
							});
							
						}
					})
				}else{//平台分类
					queryRankCat().then(res => {
						if (res.code == 1) {
							this.classLists = res.data;
							this.classLists = this.classLists.filter(item => item.catName != "积分商品")
						}
					})
				}
				
			},
			searchList(id) {
				if(this.shopId!=null&&this.shopId!=''){
					uni.navigateTo({
						url: '/store/shopsearch/shopsearch?shopId='+this.shopId+'&catId='+id
					})
				}else{
					uni.navigateTo({
						url: '/general/productlist/productlist?catId=' + id
					});
				}
				
			},
		}
	}
</script>
<style lang="scss">
	.classUls{
	  width:100%;
	  white-space:nowrap;
	  .li{
	    display:inline-block;
	    margin:0 12rpx;
	    line-height:80rpx;
	    height:80rpx;
	    font-size:27rpx;
	    &.active{
	      font-weight:bold;
	      font-size:32rpx;
	      border-bottom:4rpx solid #333;
	    }
	  }
	}
</style>
