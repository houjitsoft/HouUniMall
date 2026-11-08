<template>
	<view :style="{paddingTop:propsobj.mrT+'px',paddingBottom:propsobj.mrB+'px',paddingLeft:propsobj.mrL+'px',paddingRight:propsobj.mrR+'px'}">
		<view class="moduleStoreNotice textArr" :style="{backgroundColor:propsobj.bgColor,paddingTop:propsobj.pdT+'px', paddingRight:propsobj.pdR+'px',paddingBottom:propsobj.pdB+'px',paddingLeft:propsobj.pdL+'px',borderTopLeftRadius:propsobj.rdsTL+'px',borderTopRightRadius:propsobj.rdsTR+'px',borderBottomLeftRadius:propsobj.rdsBL+'px',borderBottomRightRadius:propsobj.rdsBR+'px'}">
			<view class="slice-enter-active" :class="[propsobj.scrollDirection=='top'?'tops':'lefts']">
				<image v-show="text.imgIcon" class="noticeImg" mode="widthFix" :src="text.imgIcon"></image>
				<navigator class="iblock" :url="text.link">
					<text class="em" :style="{color:propsobj.textColor}">{{ getTransNew(text.value)}}</text>
				</navigator>
			</view>
		</view>
	</view>
	
</template>

<script>
	
	export default {
		
		name: 'moduleStoreNotice',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				
				id: this.propsobj.scroll.number,
				value: this.propsobj.scroll.items[this.propsobj.scroll.number].value,
				imgIcon:this.propsobj.scroll.items[this.propsobj.scroll.number].imgIcon
			}
		},
		computed: {
			text() {
				return {
					value: this.propsobj.scroll.items[this.id].value,
					link: this.propsobj.scroll.items[this.id].link,
					imgIcon:this.propsobj.scroll.items[this.id].imgIcon
				}
			}
		},
		mounted() {
			this.startMove();
		},
		methods: {
			
			delCompent(name, index) {
				this.$emit('del-event', name, index);
			},
			editModule(name, index) {
				this.$emit('show-edit', name, index);
			},
			//滚动公告
			startMove() {
				let timer = setInterval(() => {
					if (this.id === this.propsobj.scroll.items.length - 1) {
						this.id = 0;
					} else {
						this.id += 1;
					}
				}, 4000);
			},
			
		}
	}
</script>

<style lang="scss">
	/* 滚动公告 */
	 .textArr{
			line-height:60rpx;
			overflow: hidden;
	 }
	 .slice-enter-active.tops {
			animation: bounce-in 4s infinite;
	 }
	 @keyframes bounce-in {
			  0% {
			  transform:translateY(50rpx) ;
			  }
			  40% {
			  transform: translateY(0px);
			  }
			  80% {
			  transform: translateY(0px);
			  }
			  100% {
			  transform:translateY(-25px);
			  }
	 }
	 .slice-enter-active.lefts{
			  animation: bounce-left 4s infinite;
	 }
	 @keyframes bounce-left {
			  0% {
			  transform:translateX(200px) ;
			  }
			  40% {
			  transform: translateX(0px);
			  }
			  80% {
			  transform: translateX(0px);
			  }
			  100% {
			  transform:translateX(-200px);
			  }
	 }
	 .slice-enter-active .iconfont{
			  position:relative;
			  padding-left:10px;
			  padding-right:10px;
			  font-size:18px;
			  color:rgb(33, 33, 33);
			  opacity: .7;
	 }
	 .slice-enter-active .iconfont:after{
			  content:'';
			  width:1px;
			  background:#212121;
			  opacity:.2;
			  position:absolute;
			  right:0;
			  top:0;
			  bottom:0;
	 }
	 .slice-enter-active .em{
			  display: inline-block;
			  padding-left: 10px;
			  font-style: normal;
			  font-size: 26rpx;
			  color:#555;
			  vertical-align: middle;
	 }
	 .noticeImg{
		 width:80rpx;
		 vertical-align:middle;
	 }
	
</style>
