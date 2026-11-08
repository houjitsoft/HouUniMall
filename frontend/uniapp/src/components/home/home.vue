<template>
	<view class="module-box">
		<view class="diy-module">
			<scroll-view v-if="allpropsobj" scroll-y="true" class="scroll-view-class" :show-scrollbar="false">
				<view class="module" v-for="(item,index) in allpropsobj" :key="index">
					<view v-if="item.moduleSign=='moduleSearch'">
						<moduleSearch :propsobj="curProps(item)" :shopId="shopid"></moduleSearch>
					</view>
					<view v-if="item.moduleSign=='moduleImg'">
						<moduleImg :propsobj="curProps(item)"></moduleImg>
					</view>
					<view v-if="item.moduleSign=='moduleSwiper'">
						<moduleSwiper :propsobj="curProps(item)"></moduleSwiper>
					</view>
					<view v-if="item.moduleSign=='moduleGoods'">
						<moduleGoods :propsobj="curProps(item)"></moduleGoods>
					</view>
					<view v-if="item.moduleSign=='moduleTitle'">
						<moduleTitle :propsobj="curProps(item)"></moduleTitle>
					</view>
					<view v-if="item.moduleSign=='moduleText'">
						<moduleText :propsobj="curProps(item)"></moduleText>
					</view>
					<view v-if="item.moduleSign=='moduleTab'">
						<moduleTab :propsobj="curProps(item)" :shopId="shopid"></moduleTab>
					</view>
					<view v-if="item.moduleSign=='moduleStoreHead'">
						<moduleStoreHead :propsobj="curProps(item)"></moduleStoreHead>
					</view>
					<view v-if="item.moduleSign=='moduleStoreNotice'">
						<moduleStoreNotice :propsobj="curProps(item)"></moduleStoreNotice>
					</view>
					<view v-if="item.moduleSign=='moduleArticle'">
						<moduleArticle :propsobj="curProps(item)"></moduleArticle>
					</view>
					<view v-if="item.moduleSign=='moduleVideo'">
						<moduleVideo :propsobj="curProps(item)"></moduleVideo>
					</view>
					<view v-if="item.moduleSign=='moduleDiscount'">
						<moduleDiscount :propsobj="curProps(item)"></moduleDiscount>
					</view>
					<view v-if="item.moduleSign=='modulePintuan'">
						<modulePintuan :propsobj="curProps(item)"></modulePintuan>
					</view>
					<view v-if="item.moduleSign=='moduleSecKill'">
						<moduleSecKill :propsobj="curProps(item)"></moduleSecKill>
					</view>
					<view v-if="item.moduleSign=='moduleRecommendedTopic'">
						<moduleRecommendedTopic :propsobj="curProps(item)"></moduleRecommendedTopic>
					</view>
					<view v-if="item.moduleSign=='moduleGoodsGroup'">
						<moduleGoodsGroup :propsobj="curProps(item)"></moduleGoodsGroup>
					</view>
					<view v-if="item.moduleSign=='moduleMagicCube'">
						<moduleMagicCube :propsobj="curProps(item)"></moduleMagicCube>
					</view>
					<view v-if="item.moduleSign=='moduleHotArea'">
						<moduleHotArea :propsobj="curProps(item)"></moduleHotArea>
					</view>
					<view v-if="item.moduleSign=='moduleCombination'">
						<moduleCombination :propsobj="curProps(item)" :shopId="shopid" :isFixed='isFixed' @imgSearch="handleImgSearch"></moduleCombination>
					</view>
					<view v-if="item.moduleSign=='modulePlace'">
						<modulePlace :propsobj="curProps(item)"></modulePlace>
					</view>
					<view v-if="item.moduleSign=='moduleVaried'">
						<moduleVaried ref="variedRef" :propsobj="curProps(item)"></moduleVaried>
					</view>
					<view v-if="item.moduleSign=='moduleHotGoods'">
						<moduleHotGoods :propsobj="curProps(item)"></moduleHotGoods>
					</view>
					<view v-if="item.moduleSign=='moduleCoupon'">
						<moduleCoupon :propsobj="curProps(item)" :shopId="shopid"></moduleCoupon>
					</view>
					<view v-if="item.moduleSign=='moduleIntegral'">
						<moduleIntegral :propsobj="curProps(item)"></moduleIntegral>
					</view>
					<view v-if="item.moduleSign=='moduleFloat'">
						<moduleFloat :propsobj="curProps(item)"></moduleFloat>
					</view>
					<view v-if="item.moduleSign=='moduleClass'">
						<moduleClass :propsobj="curProps(item)" :shopId="shopid"></moduleClass>
					</view>
					<view v-if="item.moduleSign=='moduleChain'">
						<moduleChain :propsobj="curProps(item)" :shopId="shopid"></moduleChain>
					</view>
				</view>
			</scroll-view>
			
		</view>
	</view>
</template>
<script>
	import moduleImg from "@/components/module/moduleImg.vue"
	import moduleSwiper from "@/components/module/moduleSwiper.vue"
	import moduleGoods from "@/components/module/moduleGoods.vue"
	import moduleTitle from "@/components/module/moduleTitle.vue"
	import moduleText from "@/components/module/moduleText.vue"
	import moduleTab from "@/components/module/moduleTab.vue"
	import moduleStoreHead from "@/components/module/moduleStoreHead.vue"
	import moduleStoreNotice from "@/components/module/moduleStoreNotice.vue"
	import moduleArticle from "@/components/module/moduleArticle.vue"
	import moduleVideo from "@/components/module/moduleVideo.vue"
	import moduleDiscount from "@/components/module/moduleDiscount.vue"
	import modulePintuan from "@/components/module/modulePintuan.vue"
	import moduleSecKill from "@/components/module/moduleSecKill.vue"
	import moduleRecommendedTopic from "@/components/module/moduleRecommendedTopic.vue"
	import moduleSearch from "@/components/module/moduleSearch.vue"
	import moduleGoodsGroup from "@/components/module/moduleGoodsGroup.vue"
	import moduleMagicCube from "@/components/module/moduleMagicCube.vue"
	import moduleHotArea from "@/components/module/moduleHotArea.vue"
	import moduleCombination from "@/components/module/moduleCombination.vue"
	import modulePlace from "@/components/module/modulePlace.vue"
	import moduleVaried from "@/components/module/moduleVaried.vue"
	import moduleHotGoods from "@/components/module/moduleHotGoods.vue"
	import moduleCoupon from "@/components/module/moduleCoupon.vue"
	import moduleIntegral from "@/components/module/moduleIntegral.vue"
	import moduleFloat from "@/components/module/moduleFloat.vue"
	import moduleClass from "@/components/module/moduleClass.vue"
	import moduleChain from "@/components/module/moduleChain.vue"
	import {
		pathToBase64
	} from 'image-tools'
	import {
		apiSerachGoodsData
	} from "@/api/general/index"
	import {queryUpload} from '@/api/distributionCenter/index.js'
	import { compressImage } from '@/utils/compressImage.js'
	export default {
		props:{
			allpropsobj:{
				type:Array,
				required:true
			},
			isFixed:{
				type:Boolean,
				default:false
			},
			shopid:{
				type:Number,
				default:null
			},
			/** 图片压缩质量，范围 0~1，默认 0.7 */
			compressQuality: {
				type: Number,
				default: 0.7
			},
			/**
			 * 后台选定的模板描述（style / themeColor / columns / templateCode）。
			 * 空对象表示未套用模板，渲染行为与改造前一致。
			 */
			templateView: {
				type: Object,
				default: () => ({})
			}
		},
		components: {
			moduleImg,
			moduleSwiper,
			moduleGoods,
			moduleTitle,
			moduleText,
			moduleTab,
			moduleStoreHead,
			moduleStoreNotice,
			moduleArticle,
			moduleVideo,
			moduleDiscount,
			modulePintuan,
			moduleSecKill,
			moduleRecommendedTopic,
			moduleSearch,
			moduleGoodsGroup,
			moduleMagicCube,
			moduleHotArea,
			moduleCombination,
			modulePlace,
			moduleVaried,
			moduleHotGoods,
			moduleCoupon,
			moduleIntegral,
			moduleFloat,
			moduleClass,
			moduleChain
		},
		watch:{
			allpropsobj: {
				handler(newName, oldName) {
				},
				immediate: true,
				deep: true
			}
		},
		data() {
			return {
				imagesList:[],
				componentLists: [],
				navScrollLeft: 0,
				navData: {},
				currentTab:0,
				top:'44px',
				themeData:{}
				
			};
		},
		methods: {
			//向不同组件传相应的值
			curProps(component) {
				// 模板只为缺失的旧模块配置提供默认值，不覆盖保存的显式配置。
				return this.applyTemplateLayout(component)
			},
			/**
			 * 按当前模板调整模块版式
			 *
			 * <p>后端按页面 page_template_code 下发 templateView：
			 * <ul>
			 *   <li>double-column（活力橙）→ 商品类模块双列 style=2</li>
			 *   <li>single-column（简约白）/ banner-first（高端黑）→ 单列 style=1</li>
			 * </ul>
			 * banner-first 额外把轮播的内外边距归零，做宽幅首屏。
			 * 未套模板（templateView 为空）时原样返回，行为与改造前完全一致。
			 *
			 * @param {Object} component 页面模块项
			 * @returns {Object} 传给子组件的 props
			 */
			applyTemplateLayout(component) {
				var json = component.moduleJson
				if (!json || !this.templateView || !this.templateView.style) {
					return json
				}
				// 模板只补未配置项；已有版式、边距和圆角始终以保存内容为准。
				if (Object.prototype.hasOwnProperty.call(json, 'style')) return json
				var style = this.templateView.style
				var sign = component.moduleSign

				// 高端黑：首屏轮播撑满，去掉左右留白
				if ((style === 'banner-first' || style === 'tea-brand') && sign === 'moduleSwiper') {
					var wide = Object.assign({}, json)
					if (wide.padding == null) wide.padding = '0px'
					if (wide.pageMargin == null) wide.pageMargin = '0px'
					if (wide.padTop == null) wide.padTop = '0px'
					if (wide.padBom == null) wide.padBom = '0px'
					return wide
				}

				// 商品/活动类模块按模板的列数渲染
				var goodsSigns = [
					'moduleGoods', 'moduleRecommendedTopic', 'moduleHotGoods',
					'moduleDiscount', 'moduleSecKill', 'modulePintuan'
				]
				if (goodsSigns.indexOf(sign) === -1) {
					return json
				}
				// 不改动原对象，避免污染 pageData 缓存
				var patched = Object.assign({}, json)
				var targetStyle = style === 'double-column' || style === 'tea-brand' ? '2' : '1'
				patched.style = targetStyle
				if (targetStyle === '2') {
					patched.waterFull = json.waterFull
				}
				return patched
			},
			handleImgSearch(){
				uni.chooseImage({
					count:1,
					sizeType: ['original', 'compressed'],
					sourceType: ['album', 'camera'],
					success: async (res) => {
						uni.showLoading({
							title: await this.getTransNew('图片正在上传中')
						})
						//拿到图片地址
						var imagesList = this.imagesList.concat(res.tempFilePaths);
						this.imagesList = imagesList.length <= 1 ? imagesList : imagesList.slice(0,
							1); //图片base64位
						// #ifdef H5
						const imgType = '.' + res.tempFiles[0].name.split('.')[1]
						// #endif
						// #ifndef H5
						var spliArr = res.tempFilePaths[0].split('.');
						const imgType = '.' + spliArr[spliArr.length - 1];
						// #endif
						var streamName = imgType || '';
						try {
							const compressedSrc = await compressImage(
								res.tempFilePaths[0],
								this.compressQuality,
								res.tempFiles && res.tempFiles[0]
							)
							// H5 压缩结果可能已是 dataURL，可直接上传；其它端仍转 base64
							const base64 = compressedSrc.indexOf('data:image') === 0
								? compressedSrc
								: await pathToBase64(compressedSrc)
							if (base64.indexOf('data:image/jpeg') === 0) {
								streamName = '.jpg'
							}
							const resUpload = await queryUpload({
								base64Str: base64.replace(
									/^data:image\/\w+;base64,/, ""),
								streamName: streamName
							});
							if (resUpload.code == 1) {
								var obj={
									shopId:'',
									attributeContentName: '',
									brandName: '',
									attributeContentIdList: [],
									catId:'',
									minPrice: '',
									maxPrice: '',
									shopSelfSupport: '',
									orderType: 3,
									priceOrder: '',
									saleNum: '',
									page: 1,
									limit: 10,
									commentOrder: 1,
									goodsType: 1,
									isApp: 1,
									brandIdList: [],
									attrName: '',
									imagePath:resUpload.data
								}
								const {
									code,
									data
								} = await apiSerachGoodsData(obj);
								if (code == 1) {
									uni.hideLoading();
									uni.setStorageSync('imagePath', resUpload.data);
									uni.navigateTo({
										url: '/general/productlist/productlist?image=true&commonName=&type='
									});
								} else {
									uni.hideLoading();
								}
							} else {
								uni.hideLoading();
								uni.showToast({
									title: await this.$t(resUpload.message),
									icon: 'none'
								})
							}
						} catch (error) {
							console.error(error)
							uni.hideLoading();
							uni.showToast({
								title: await this.getTransNew('抱歉，图片上传失败'),
								icon: 'none'
							})
						}
				
					}
				});
				
			},
			reachBottomFun(type){
				if(this.allpropsobj[this.allpropsobj.length-1].moduleSign=='moduleVaried'){
					this.$nextTick(() => {
					    if (this.$refs.variedRef) {
						  this.$refs.variedRef[0].reachBottomFun2(type);
					    }
					  });
					
				}
			}
		}

	}
</script>

<style lang="scss">
	@import "./home.css";
	.module-box {
		padding-bottom: 100rpx;
	}
	.nav {
		height: 70rpx;
		width: 100%;
		box-sizing: border-box;
		overflow: hidden;
		line-height: 70rpx;
		font-size: 28rpx;
		white-space: nowrap;
		position: fixed;
		top: 64rpx;
		left: 0;
		z-index: 99;
		padding-right: 10rpx;
	}

	.nav-item {
		padding-left: 40rpx;
		display: inline-block;
		text-align: center;
		color: #fff;
	}

	.nav-item.active {
		font-size: 35rpx;
		font-weight: 600;
	}
	.c-888 {
		color: #888;
	}
	.fz-36 {
		font-size: 36rpx;
	}
</style>


