<template>
	<view class="countdown" v-if="start">
		<view class="day iblock" v-if="d!=0"><text class="text time">{{d<10?'0'+d:d}}</text></view> 
		<text class="text" v-if="d!=0">:</text>
		<view class="time">{{h<10?'0'+h:h}}</view>
		<text class="text">:</text>
		<view class="time">{{m<10?'0'+m:m}}</view>
		<text class="text">:</text>
		<view class="time">{{s<10?'0'+s:s}}</view>
	</view>
	<text class="end-text" v-else>00:00:00</text>
</template>

<script>
	export default{
		props:{
			startTime: {
				type: String,
			},
			endTime:{
				type: String,
			},
		},
		data(){
			return{
				timer:null,
				d:0,
				h:0,
				m:0,
				s:0,
				start:true
			}
		},
		mounted() {
			this.time()
		},
		beforeDestroy(){
			clearInterval(this.timer);
			this.timer = null;
		},
		computed:{
			sTime(){return this.startTime},
			eTime(){return this.endTime}
		},
		watch:{
			endTime(){
				clearInterval(this.timer);
				this.time()
			}
		},
		methods:{ 
			time(){
				let leftTime = this.GetDateDiff(this.sTime,this.eTime)
				this.getCountdownTime(leftTime)
			},
			//计算两个时间差
			GetDateDiff(startTime, endTime) {
			    //将xxxx-xx-xx的时间格式，转换为 xxxx/xx/xx的格式
			    startTime = startTime.replace(/\-/g, "/");
				if(typeof(endTime) != 'number'){
					endTime = endTime.replace(/\-/g, "/");
					//将计算间隔类性字符转换为小写
					var sTime = new Date(startTime);
					var eTime = new Date(endTime);
					return parseInt((eTime.getTime() - sTime.getTime()) / 1000);
				}else{
					//将计算间隔类性字符转换为小写
					var sTime = new Date(startTime);
					return parseInt((sTime.getTime()+endTime - sTime.getTime()) / 1000);
				}
			    // var sTime = new Date(startTime); 
			    // var eTime = new Date(endTime); 
			},
			//计算活动结束时间
			getCountdownTime(leftTime){
				let time = leftTime
				if (time>0) {
					this.timer = setInterval(() => {
						if(time == 0){
							clearInterval(this.timer)
							this.h = 0
							this.m = 0
							this.s = 0
							this.start=false
							this.$emit('statusEdit');
						}else{
							this.d = parseInt(leftTime/3600/24);
							this.h = parseInt((time/3600)%24);
							this.m = parseInt((time/60)%60);
							this.s = parseInt(time%60);
							time --
						}
					},1000)
				}else{
					this.start=false
					this.$emit('statusEdit');
				}
			}
		}
	}
</script>

<style lang="scss" scoped>
	.end-text{color:#bbb;}
	.countdown{
		// display: flex;
		// align-items: center;
		font-size: 20rpx;
		color: #fff;
		text-align:center;
		.day{
			font-size: 22rpx;
			color: #606060;
			// display: flex;
			// align-items: center;
			.text{
				color:#fff;
				font-size: 20rpx;
			}
		}
		.time{
			display:inline-block;
			padding: 8rpx;
			background-color: #606060;
			border-radius: 4rpx;
			box-sizing: border-box;
		}
	.text{font-size: 36rpx;margin: 0 6rpx;color: #606060;}
	}
	.countdown .text{
	  font-size: 18px;
	  margin: 0 3px;
	  color: #606060;
	}
	.countdown{
	  display: inline-block;
	}
	.countdown .time{
	  background: transparent !important;
	  padding: 0 !important;
	  color: #666 !important;
	}
	.countdown .text{
	  margin: 0 !important;
	  color: #666 !important;
	  font-size: 12px;
	}
	.end-text{
	  color: #666 !important;
	}
</style>
