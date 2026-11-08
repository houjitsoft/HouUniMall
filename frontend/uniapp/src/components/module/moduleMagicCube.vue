<template>
	<view class="moduleMagicCube">
		<div class="conts">
			<div v-if='propsobj.type<=2' class="decorate-cube decorate-cube2" :style="{'width':getBoxWidth()+'px','height':getImgheight2+'px','margin':-getBorderWidth()+'px'}">
			    <div class="cube-selected" @click="goDetail(item.link)" v-for="(item, index) in propsobj.items" :class="[current==index?'cube-selected-click':'']"
			        :style="{'width':getSelectedWidth(item)+'px','height':getImgheight+'px','top':getSelectedTop(item)+'px','left':getSelectedLeft(item)+'px','margin':getBorderWidth()+'px',backgroundImage:'url('+item.image_url+')',backgroundRepeat:'no-repeat',backgroundPosition:'top',backgroundSize:'cover'}"
			       >
			    </div>
			</div>
			<div v-else class="decorate-cube decorate-cube2" :style="{'width':getBoxWidth()+'px','height':getBoxWidth()+'px','margin':-getBorderWidth()+'px'}">
			    <div class="cube-selected" @click="goDetail(item.link)" v-for="(item, index) in propsobj.items" :class="[current==index?'cube-selected-click':'']"
			        :style="{'width':getSelectedWidth(item)+'px','height':getSelectedHeight(item)+'px','top':getSelectedTop(item)+'px','left':getSelectedLeft(item)+'px','margin':getBorderWidth()+'px',backgroundImage:'url('+item.image_url+')',backgroundRepeat:'no-repeat',backgroundPosition:'top',backgroundSize:'cover'}"
			       >
			    </div>
			</div>
		</div>
	</view>
</template>

<script>
	let app = getApp({allowDefault: true});
	export default {
		name: 'moduleMagicCube',
		props: ['propsobj'],
		watch: {
			
		},
		data() {
			return {
				themeData:this.publicVariable.themeData,
				token:uni.getStorageSync('token'),
				cubeWidth: 380, //魔方宽度。
				cubeHeight: 380, //魔方高度。
				current:''
			}
		},
		computed: {
			getImgheight(){
			  var item=this.propsobj.items[0];
			  var width=item.width * this.cubeCellWidth;
			  var height;
			  var imgWidth=item.image_width;
			  var imgHeight=item.image_height;
			  height=imgHeight*width/imgWidth;
			  return height;
			
			},
			getImgheight2(){
				var item=this.propsobj.items[0];
				var width=item.width * this.cubeCellWidth;
				var height;
				var imgWidth=item.image_width;
				var imgHeight=item.image_height;
				height=imgHeight*width/imgWidth+this.getBorderWidth()*2;
				return height;
			},
		    //单元魔方高度。
		    cubeCellHeight: function () {
		        return this.cubeHeight / Number(this.propsobj.layoutType);
		    },
		    //单元魔方宽度。
		    cubeCellWidth: function () {
		        return this.cubeWidth / Number(this.propsobj.layoutType);
		    },
		    isHasUpload(){
		     const isupLoad = this.propsobj.items.some((item)=>{return item.image_url !=''});
		     return isupLoad;
		    }
		},
		mounted() {
			var that=this;
			let getWindowInfo = uni.getWindowInfo()
			this.cubeWidth=getWindowInfo.screenWidth-20;
			this.cubeHeight=getWindowInfo.screenWidth-20;
		},
		methods: {
			getBorderWidth(){
			  if(this.propsobj.borderWidth>0){
			    var wid=this.propsobj.borderWidth/2;
			    return wid;
			  }else{
			    return 0;
			  }
			},
			getBoxWidth(){
			  return this.cubeWidth+this.getBorderWidth()*2
			},
			//计算选中层的宽度。
			getSelectedWidth: function (item) {
				var deWid=item.width * this.cubeCellWidth;
				if(deWid==this.cubeWidth){
				  return this.cubeWidth;
				}else{
				  return item.width * this.cubeCellWidth-this.getBorderWidth();
				}
			    // return item.width * this.cubeCellWidth;
			},
			//计算选中层的高度。
			getSelectedHeight: function (item) {
			  var deHig=item.height * this.cubeCellHeight;
			  if(deHig==this.cubeWidth){
			    return this.cubeWidth;
			  }else{
			    return item.height * this.cubeCellHeight-this.getBorderWidth();
			  }
			
			},
			//计算选中层的右边距离。
			getSelectedTop: function (item) {
			  if(item.y !=0){
			    return item.y * this.cubeCellHeight+this.getBorderWidth();
			  }else{
			    return item.y * this.cubeCellHeight;
			  }
			
			},
			//计算选中层的左边距离。
			getSelectedLeft: function (item) {
			   if(item.x !=0){
			      return item.x * this.cubeCellWidth+this.getBorderWidth();
			   }else{
			     return item.x * this.cubeCellWidth;
			   }
			},
			// 进去详情
			goDetail(link){
				if(!link){return;}
				uni.navigateTo({
					url:link
				})
			}
		}
	}
</script>

<style lang="scss">
	.moduleMagicCube{
		padding:2rpx 10px;
	}
	.pt20{padding-top:20rpx !important};
	.pl20{padding-left:20rpx !important;}
	.iblock{display:inline-block;}
	.decorate-cube {
	    position: relative;
	}
	.decorate-cube2{
	  width:100%;
	  // height: 375px !important;
	}
	
	.decorate-cube .cube-item {
	    border-right: 1px solid #e5e5e5;
	}
	
	.decorate-cube .cube-selected {
	    position: absolute;
	    background-color: #fff;
	    // border: 1px solid #ebedf0;
	    text-align: center;
	    color: #7d7e80;
	    cursor: pointer;
	    box-sizing: border-box;
		border-radius:10rpx;
	}
	
	.decorate-cube .cube-selected.cube-selected-click {
	    background: #e0edff;
	    z-index: 2;
	    cursor: auto;
	}
	
	.decorate-cube .cube-selected-text {
	    font-size: 12px;
	    width: 100%;
	    position: absolute;
	    top: 50%;
	    left: 50%;
	    transform: translateX(-50%) translateY(-50%);
	}
	
	.decorate-cube .cube-col {
	    float: left;
	    list-style: none;
	    padding: 0;
	    margin: 0;
	}
	
	.decorate-cube .cube-item:first-child {
	    border-top: 1px solid #e5e5e5;
	}
	
	.decorate-cube .cube-item {
	    background: #f8f8f8;
	    border-left: 1px solid #e5e5e5;
	    border-bottom: 1px solid #e5e5e5;
	    cursor: pointer;
	    text-align: center;
	    box-sizing: border-box;
	}
	
	.decorate-cube .cube-item.item-selecting {
	    background: #e0edff;
	}
	
	.decorate-cube .cube-item.item-selected {
	    background: #e0edff;
	    visibility: hidden;
	}
	.bold{
	  font-weight:bold;
	}
	.cube-selected .i{
	  display:none;
	  position:absolute;
	  right:-10px;
	  top:-10px;
	  z-index:2;
	  font-size:20px;
	  color:#bbb;
	}
	.cube-selected:hover .i{
	  display:inline-block;
	}
	.preImg{
	  height: inherit;
	  vertical-align: baseline;
	  object-fit: cover;
	}
	.default-area{
	  padding-top:20px;
	  background:#f3f5f7;
	}
	.default-area .p{
	 font-size: 14px;
	 color: #999;
	 margin-top: 10px;
	 padding-bottom: 10px;
	}
</style>
