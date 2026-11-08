<template>
	<view>
		<tab1 v-if="active==1" ref='tab1' :shopid='params.shopId' :isFixed="isFixed"/>
		<tab2 v-else-if="active==2" :shopid='params.shopId' />
		<view class="loadingText" v-else>
			<span>{{getTransNew('店铺正在拼命加载中~')}}</span>
		</view>
	</view>
</template>
<script>
	let app = getApp({allowDefault: true});
	import {
		queryModularizationdShopIndex
	} from "@/api/store/index"
	import tab1 from '@/components/store/tab1';
	import tab2 from '@/components/store/tab2';
	
	export default {
		props: ['storeId','isFixed'],
		components: {
			tab1,
			tab2
		},
		data() {
			return {
				shopId:'',
				params: {
					shopId: ''
				},
				active: 0,
			}

		},
		mounted() {
			if(this.publicVariable.thirdPart.shopId){
				this.params.shopId = this.publicVariable.thirdPart.shopId;
			}else{
				this.params.shopId = this.storeId;
			}
			console.log(this.params.shopId,'this.params.shopId--')
			this.check();
		},
		methods: {
			init(){
				if(this.active==1){
					this.$refs.tab1.init();
				}
			},
			scrollChange(e){
				if(this.active==1){
					this.$refs.tab1.scrollChange(e);
				}
			},
			check() {
				queryModularizationdShopIndex({
                    ...this.params,
                    shopId: Number(this.params.shopId)
                }).then(res => {
					if (res.code == 1) {
						this.active = 1
					} else {
						this.active = 2
					}
				})
			}
		}
	}
</script>

<style lang="scss">
	@import '@/components/homeIndex/homeIndex.css';
</style>