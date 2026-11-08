<template>
	<view :style="temp">
		<view class="countdown" :class="comStyle" v-if="start">
			<view class="day iblock" v-if="d!=0">
				<text class="text time">{{d}}</text>
				<text class="day">{{getTransNew('天')}}</text>
				</view>
			<view class="time" style="margin-left: 9rpx;">{{h<10?'0'+h:h}}</view>
			<text class="text">:</text>
			<view class="time">{{m<10?'0'+m:m}}</view>
			<text class="text">:</text>
			<view class="time">{{s<10?'0'+s:s}}</view>
		</view>
		<text class="end-text" v-else>{{ getTransNew('已结束')}}</text>
	</view>
</template>

<script>
	
	export default {
		
		props: {
			startTime: {
				type: String,
			},
			endTime: {
				type: String,
			},
			comStyle:{
				type: String,
				default:'style1'
			}
		},
		data() {
			return {
				temp: '--webColor: ' + this.publicVariable.webColor + ';',
				timer: null,
				d: 0,
				h: 0,
				m: 0,
				s: 0,
				start: true,
			}
		},
		mounted() {
			this.GetDateDiff(this.startTime, this.endTime)
		},
		beforeDestroy() {
			clearInterval(this.timer);
			this.timer = null;
		},
		methods: {
			
			//计算两个时间差
			GetDateDiff(startTime, endTime) {
				var that = this;
				// //将计算间隔类性字符转换为小写
				var sTime = +new Date(startTime);
				var eTime = +new Date(endTime.replace(/-/g, '/'));
				if (+new Date() < sTime) {
					this.getCountdownTime(startTime)
				} else {
					this.getCountdownTime(endTime, false)
				}
			},
			//计算活动结束时间
			getCountdownTime(value, flag) {
				var that = this;
				// 递归调用函数所以是延时器不是定时器
				var timer = setInterval(() => {
					//获取当前时间
					var now = +new Date();
					//设置截止时间
					var end = +new Date(value.replace(/-/g, '/')).getTime();
					//时间差

					var differTime = end - now;
					if (differTime > 0) {
						this.d = Math.floor(differTime / (1000 * 60 * 60 * 24));
						this.h = Math.floor(differTime / (1000 * 60 * 60) % 24);
						this.m = Math.floor(differTime / (1000 * 60) % 60);
						this.s = Math.floor(differTime / 1000 % 60);
					} else {
						this.start = flag
						return ''
					}
				}, 1000) //开启定时器
			},
		}
	}
</script>

<style lang="scss">
	.end-text {
		color: #bbb;
	}

	.countdown {
		// display: flex;
		// align-items: center;
		font-size: 20rpx;
		color: #222;
		text-align: center;

		.day {
			font-size: 22rpx;
			color: #fff;

			// display: flex;
			// align-items: center;
			.text {
				color: #444;
				font-size: 20rpx;
			}
		}
		
		.time {
			display: inline-block;
			padding: 8rpx;
			border-radius: 6rpx;
			box-sizing: border-box;
			
		}
		.text {
			font-size: 26rpx;
			margin: 0 6rpx;
			color: #444;
		}
		&.style1{
			.time{
				background-color:#fff;
				color:var(--webColor) !important;
			}
			.text{
				color:#fff;
			}
		}
		&.style2{
			.time{
				background-color:var(--webColor);
				color:#fff;
			}
			.day{
				color:#2e2d48;
			}
		}
		&.style3{
			.time{
				background-color:#2e2d48;
				color:#fff;
			}
			.day{
				color:#2e2d48;
			}
		}
		

		
	}
</style>