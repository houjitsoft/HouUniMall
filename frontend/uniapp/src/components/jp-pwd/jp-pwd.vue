<template>
	<view class="wallet_class" :class="type?'wallet_class_kq':'wallet_class_gb'" v-if="topayPwds">

		<view style="width: 100%;height: 100vh;" @tap="toCancel" v-if="keep"></view>
		<view v-if="payType==='one'" class="wallet_content" :class="type?'wallet_kq':'wallet_gb'" :style="'background-image: url('+keyImg+ ')'">
			<!-- 头部 -->
			<view class="wallet_content_tb" :style="keyImg?'':'background-image: url('+topImg+')'">
				<view style="width: 100rpx;">
				</view>
				<view style="width: 550rpx;text-align: center;" :style="'color:'+ titeColor">{{ getTransNew(tites)}}</view>
				<view style="width: 100rpx;text-align: center;" @tap="toCancel" :style="'color:'+ titeColor">
					×
				</view>
			</view>
			<view class="capsule">
				<view class="pay-pwd-input_text" :style="'color:'+ contentsColor">{{ getTransNew(contents)}}</view>
				<view class="pay-pwd-input" v-if="pawType==='one'">
					<view class="pay-pwd-grid uni-flex uni-row" v-for="(value, index) in payPwdGrid" :key="index">
						<view :style="'width:'+ width1 + 'rpx'">{{value.text}}</view>
					</view>
				</view>
				<view class="input-row" v-if="pawType==='two'">
					<view class="pay-pwd-grid uni-flex uni-row" v-for="(value, index) in payPwdGrid" :key="index">
						<view :class="'item'" :style="'width:'+ width1 + 'rpx'">{{ value.text  }}</view>
					</view>
				</view>
				<view class="btn-sure-setPsw" v-if="isShowBtn">
					<button @click="checkPsw">{{ getTransNew('确认')}}</button>
				</view>
				<view class="forget_text">
					<view v-if="msgs" :style="'color:'+ msgsColor">{{getTransNew(msgs)}}</view>
					<!-- <view :style="'color:'+ forgetNameColor" @tap="forget">{{forgetName}}</view> -->
				</view>
				<view class="jp_hea" :style="keyImg?'':'background-color: #f5f5f5'">
					<view :class="keyType=='one'?'jq_key':'jq_key2'">
						<button @click="inputPwd(list[1])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[1]">{{list[1]}}</button>
						<button @click="inputPwd(list[2])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[2]">{{list[2]}}</button>
						<button @click="inputPwd(list[3])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[3]">{{list[3]}}</button>
					</view>
					<view :class="keyType=='one'?'jq_key':'jq_key2'">
						<button @click="inputPwd(list[4])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[4]">{{list[4]}}</button>
						<button @click="inputPwd(list[5])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[5]">{{list[5]}}</button>
						<button @click="inputPwd(list[6])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[6]">{{list[6]}}</button>
					</view>
					<view :class="keyType=='one'?'jq_key':'jq_key2'">
						<button @click="inputPwd(list[7])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[7]">{{list[7]}}</button>
						<button @click="inputPwd(list[8])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[8]">{{list[8]}}</button>
						<button @click="inputPwd(list[9])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[9]">{{list[9]}}</button>
					</view>
					<view :class="keyType=='one'?'jq_key':'jq_key2'">
						<view class="keyboard" :style="keyImg?'':'background-color: #fff'"></view>
						<button @click="inputPwd(list[0])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[0]">{{list[0]}}</button>
						<button @click="backspace()" class="keyboard" :style="keyImg?'':'background-color: #fff'" data-char="×">×</button>
					</view>
				</view>
			</view>
		</view>
		<view v-if="payType==='two'" class="wallet_content2" :class="type?'wallet_kq':'wallet_gb'">
			<!-- 头部 -->
			<view style="width: 750rpx;height: 100vh;" @tap="toCancel" v-if="keep"></view>
			<view class="wallet_content_tb" :style="'background-image: url('+topImg+')'">
				<view class="hader" :style="'color:'+ titeColor">
					<view style="width: 50rpx;">
					</view>
					<view style="width: 590rpx;text-align: center;">{{ getTransNew(tites)}}</view>
					<view style="width: 50rpx;text-align: center;" @tap="toCancel">
						×
					</view>
				</view>
				<view class="contents" :style="'color:'+ contentsColor">
					{{ getTransNew(contents)}}
				</view>
				<view class="money">
					{{currencySymbol()}}{{getRatePrice(money)}}
				</view>
				<view class="pay-pwd-input" v-if="pawType==='one'">
					<view class="pay-pwd-grid uni-flex uni-row" v-for="(value, index) in payPwdGrid" :key="index">
						<view :style="'width:'+ width2 + 'rpx'">{{value.text}}</view>
					</view>
				</view>
				<view class="input-row" v-if="pawType==='two'">
					<view class="pay-pwd-grid uni-flex uni-row" v-for="(value, index) in payPwdGrid" :key="index">
						<view :class="'item'" :style="'width:'+ width2 + 'rpx'">{{ value.text  }}</view>
					</view>
				</view>
				<view class="forget_text">
					<view :style="'color:'+ msgsColor">{{ getTransNew(msgs)}}</view>
					<view :style="'color:'+ forgetNameColor" @tap="forget">{{ getTransNew(forgetName)}}</view>
				</view>
			</view>
			<view class="wallet_content02" :style="'background-image: url('+keyImg+ ')'">
				<view class="capsule">
					<view class="jp_hea" :style="keyImg?'':'background-color: #f5f5f5'">
						<view :class="keyType=='one'?'jq_key':'jq_key2'">
							<button @click="inputPwd(list[1])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[1]" hover-class="click_hover">{{list[1]}}</button>
							<button @click="inputPwd(list[2])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[2]" hover-class="click_hover">{{list[2]}}</button>
							<button @click="inputPwd(list[3])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[3]" hover-class="click_hover">{{list[3]}}</button>
						</view>
						<view :class="keyType=='one'?'jq_key':'jq_key2'">
							<button @click="inputPwd(list[4])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[4]" hover-class="click_hover">{{list[4]}}</button>
							<button @click="inputPwd(list[5])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[5]" hover-class="click_hover">{{list[5]}}</button>
							<button @click="inputPwd(list[6])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[6]" hover-class="click_hover">{{list[6]}}</button>
						</view>
						<view :class="keyType=='one'?'jq_key':'jq_key2'">
							<button @click="inputPwd(list[7])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[7]" hover-class="click_hover">{{list[7]}}</button>
							<button @click="inputPwd(list[8])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[8]" hover-class="click_hover">{{list[8]}}</button>
							<button @click="inputPwd(list[9])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[9]" hover-class="click_hover">{{list[9]}}</button>
						</view>
						<view :class="keyType=='one'?'jq_key':'jq_key2'">
							<button class="keyboard" :style="keyImg?'':'background-color: #fff'"></button>
							<button @click="inputPwd(list[0])" class="keyboard" :style="keyImg?'':'background-color: #fff'" :data-char="list[0]" hover-class="click_hover">{{list[0]}}</button>
							<button @click="backspace()" class="keyboard" :style="keyImg?'':'background-color: #fff'" data-char="×" hover-class="click_hover">×</button>
						</view>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	const app = getApp({allowDefault: true});
	import {
		queryUpdatePaymentPassword
	} from "@/api/pages/index"
	import JSEncrypt from '@/utils/JSEncrypt';
	
	export default {
		name: 'wallet_category',
		data() {
			return {
				width1: 110,
				width2: 100,
				topayPwds: false,
				msgs: '',
				type: true,
				password: [],
				payPwdGrid: [],
				list: [0, 1, 2, 3, 4, 5, 6, 7, 8, 9],
				pasOne: [],
				pasTwo: [],
				nums: 1,
				isShowBtn: false,
				tites:'请输入支付密码',
				isSetPsws:0,
				money:'0',
				payType: 'one',//支付方式
				keyType:'one', //键盘样式
				pawType:'one', //输入框样式
				tite:'请输入支付密码', //标题
				titeColor:'#333',//标题颜色
				contents:'请输入6位数字支付密码', //提示
				contentsColor:'#888', //提示颜色
				cancelType:false,//是否输入完密码后消失
				keep:true,//点击遮挡是否关闭
				msg:'密码错误', // 密码错误提示语
				msgsColor:'#F43F3B',
				forgetName:'忘记密码？', 
				forgetNameColor:'#0081FF',
				places:6, // 密码框位数
				topImg: '',
				keyImg:'',
				random:false,
				isPwy:true,
				isSetPsw:0,
                timerId: null,
			}
		},
		mounted() {
			if (this.random) {
				this.list.sort(function() {
					return (0.5 - Math.random());
				});
			}
			this.width1 = (750 - 90) / this.places
			this.width2 = (690 - 90) / this.places
			this.payPwdGrid = []
			for (let a = 0; a < this.places; a++) {
				this.payPwdGrid.push({
					text: ''
				})
			}
		},
		watch: {
			msg() {
				this.msgs = this.msg
			},
			tite(){
				this.tites = this.tite
			},
			isSetPsw(){
				this.isSetPsws = this.isSetPsw
			},
			places() {
				this.width1 = (750 - 90) / this.places
				this.width2 = (690 - 90) / this.places
				this.payPwdGrid = []
				for (let a = 0; a < this.places; a++) {
					this.payPwdGrid.push({
						text: ''
					})
				}
			}
		},
		methods: {
			
			async toOpen(isPsw) {
				this.type = true
				this.topayPwds = true;
				this.isSetPsws = isPsw;
				if (this.isSetPsws == 0) {
					this.tites = await this.getTransNew('请设置支付密码');
					this.contents = await this.getTransNew('您未设置支付密码，请先设置支付密码');
				} else {
					this.tites = await this.getTransNew('请输入支付密码');
					this.contents = await this.getTransNew('请输入6位数字支付密码');
				}
			},
			toCancel() {
				this.type = false
                this.timerId = setTimeout(() => {
                    try {
                        this.topayPwds = false;
                        this.backs();
                        this.$emit('toCancel');
                    } catch (error) {
                        console.error('clock method error:', error);
                    }
                }, 200);
			},
			forget() {
				this.$emit('forget');
			},
			/* 小键盘输入 */
			async inputPwd(e) {
				let char = e;
				let args = {
					target: this,
					data: char
				};
				let passIndex = this.password.length;
				if (passIndex < this.places) {
					this.password.push(char);
					if (this.isPwy) {
						this.payPwdGrid[passIndex].text = "●";
					} else {
						this.payPwdGrid[passIndex].text = char;
					}
					this.$emit('inputPwd', char);
					// 改变密码框
					if (this.password.length === this.payPwdGrid.length) {
						//长度达6位，自动验证

						if (this.isSetPsws == 1) {
							this.$emit('completed', this.password.join(''));
							if (this.cancelType) {
								this.toCancel()
							}

						} else {

							if (this.nums == 1) {
								this.contents = await this.getTransNew("请再次确认支付密码");
								this.isShowBtn = true;
								this.pasOne = JSON.parse(JSON.stringify(this.password));
								this.password.forEach((el, index) => {
									let passIndex = this.password.length;
									if (this.password.length > 0) {
										// 改变密码框
										this.password = this.password.slice(0, this.password.length - 1);
										this.payPwdGrid[passIndex - 1].text = "";
									}
								})
								this.nums = 2;
							} else {
								this.pasTwo = JSON.parse(JSON.stringify(this.password));


							}


						}

					}
				}
			},
			/* 回退 */
			backspace() {
				let passIndex = this.password.length;
				if (this.password.length > 0) {
					// 改变密码框
					this.password = this.password.slice(0, this.password.length - 1);
					this.payPwdGrid[passIndex - 1].text = "";
					return;
				}
			},
			// 清空密码
			backs() {
				this.password.forEach((el, index) => {
					let passIndex = this.password.length;
					if (this.password.length > 0) {
						// 改变密码框
						this.password = this.password.slice(0, this.password.length - 1);
						this.payPwdGrid[passIndex - 1].text = "";
					}
				})
			},
			async checkPsw() {
				var that = this;
				for (var i = 0; i < this.pasTwo.length; i++) {
					if (this.pasTwo[i] != this.pasOne[i]) {
						uni.showToast({
							title: await this.getTransNew('密码不一致请重新设置'),
							icon: 'none'
						})
						that.isShowBtn = false;
						that.nums = 1;
						that.tites = await this.getTransNew('请设置支付密码');
						that.contents = await this.getTransNew('您未设置支付密码，请先设置支付密码');
						that.password.forEach((el, index) => {
							let passIndex = this.password.length;
							if (this.password.length > 0) {
								// 改变密码框
								this.password = this.password.slice(0, this.password.length - 1);
								this.payPwdGrid[passIndex - 1].text = "";
							}
						})
						return;

					} else {
						if (i == 5) {
							var str = '';
							this.pasTwo.forEach(item => {
								str += item;
							})
							// 与支付链路一致：后端按明文 BCrypt 校验，本部署无 RSA 私钥，前端不做 RSA
							var strPassword2 = str;
							queryUpdatePaymentPassword({
								passwd: strPassword2
							}).then(async res => {
								if (res.code == 1) {
									uni.showToast({
										title: await this.getTransNew('密码设置成功'),
										icon: 'none'
									})
									this.isSetPsws = 1;
									this.isShowBtn = false;
									this.tites = await this.getTransNew('请输入支付密码');
									this.contents = await this.getTransNew('请输入6位数字支付密码');
									this.password.forEach((el, index) => {
										let passIndex = this.password.length;
										if (this.password.length > 0) {
											// 改变密码框
											this.password = this.password.slice(0, this.password.length - 1);
											this.payPwdGrid[passIndex - 1].text = "";
										}
									})
								}
							})

						}
					}
				}
			}
		},
        beforeDestroy() {
            if (this.timerId) {
                clearTimeout(this.timerId);
                this.timerId = null;
            }
        }
	}
</script>

<style lang="scss" scoped>
	@keyframes downIn {
		from {
			transform: translateY(50vh);
			opacity: 1;
		}

		to {
			transform: translateY(0vh);
			opacity: 1;
		}
	}

	@keyframes downInClose {
		from {
			transform: translateY(0vh);
			opacity: 1;
		}

		to {
			transform: translateY(50vh);
			opacity: 1;
		}
	}

	.wallet_class {
		position: fixed;
		z-index: 9;
		height: 100vh;
		width: 750rpx;
		top: 0;
		left: 0;
		background-color: rgba(0, 0, 0, .5);

		.wallet_kq {
			animation: downIn 0.3s;
			-webkit-animation: downIn 0.3s;
		}

		.wallet_gb {
			animation: downInClose 0.3s;
			-webkit-animation: downInClose 0.3s;
		}

		.wallet_content {
			background-size: 100% 100%;
			background-position: top left;
			background-repeat: no-repeat;
			position: fixed;
			bottom: 0rpx;
			border-top-left-radius: 20rpx;
			border-top-right-radius: 20rpx;
			width: 100%;
			height: 50vh;
			background-color: #fff;


			.wallet_content_tb {
				display: flex;
				border-top-left-radius: 20rpx;
				border-top-right-radius: 20rpx;
				background-size: 100%;
				background-position: top left;
				background-repeat: no-repeat;
				justify-content: space-between;
				height: 100rpx;
				border-bottom: #ccc solid 1px;
				width: 100%;
				line-height: 100rpx;
			}

			.capsule {
				height: calc(50vh - 100rpx);
				position: relative;

				.pay-pwd-input_text {
					margin-top: 14rpx;
					font-size: 28rpx;
					width: 100%;
					color: #999;
					text-align: center;
					height: 5%;
				}

				.forget_text {
					width: 730rpx;
					font-size: 28rpx;
					// color: #3D9BE4;
					// text-align: right;
					padding: 0 10rpx;
					height: 10%;
					margin-top: 5rpx;
					display: flex;
					justify-content: space-between;
				}

				.pay-pwd-input {
					width: 100%;
					height: 20%;
					line-height: 20%;
					display: flex;
					justify-content: flex-start;

					.pay-pwd-grid {
						margin: 0upx auto;
						margin-top: 20rpx;
						height: 80%;
						line-height: 80%;
						display: flex;
						justify-content: center;

						view {
							width: 110upx;
							height: 100%;
							display: flex;
							align-items: center;
							justify-content: center;
							border: #cececd solid 2rpx;
							border-radius: 10upx;
							font-size: 36upx;
							font-weight: 600;
						}
					}

				}

				.input-row {
					width: 750upx;
					height: 20%;
					line-height: 20%;
					display: flex;
					justify-content: flex-start;

					.pay-pwd-grid {
						margin: 0upx auto;
						margin-top: 20rpx;
						height: 80%;
						line-height: 80%;
						display: flex;
						justify-content: center;

						.item {
							width: 110rpx;
							height: 100%;
							display: flex;
							align-items: center;
							justify-content: center;
							font-size: 36upx;
							font-weight: 600;
							border-bottom: 1px solid #c8c8c8;
						}

						.item-active {
							position: relative;
							transform: scale(1.2);
						}
					}

				}

				.jp_hea {
					position: absolute;
					bottom: 5rpx;
					height: 60%;

					.jq_key {
						display: flex;
						justify-content: flex-start;
						height: 25%;

						.keyboard {
							box-sizing: border-box;
							width: 250upx;
							height: 100%;
							line-height: 100%;
							background-color: rgba(255, 255, 255, .3);
							border-top: #f7f4f4 solid 0.1upx;
							border-left: #f7f4f4 solid 0.1upx;
							font-size: 40upx;
							font-weight: 600;
							display: flex;
							justify-content: center;
							align-items: Center;
						}

						.click_hover {
							animation: showPopup 0.1s linear both;
						}
					}

					.jq_key2 {
						display: flex;
						justify-content: flex-start;
						height: 23%;
						padding-top: 1%;
						width: 750rpx;
						// background-color: #eee;

						.keyboard {
							box-sizing: border-box;
							width: 240upx;
							margin: 0 7rpx;
							border-radius: 20rpx;
							height: 100%;
							line-height: 100%;
							background-color: rgba(255, 255, 255, .3);
							font-size: 40upx;
							font-weight: 600;
							display: flex;
							justify-content: center;
							align-items: Center;
						}

						.click_hover {
							animation: showPopup 0.1s linear both;
						}
					}
				}


				@keyframes showPopup {
					0% {
						opacity: 0;
						background-color: #d8d8d8;
					}

					100% {
						opacity: 1;
					}
				}
			}
		}

		.wallet_content2 {
			position: fixed;
			z-index: 8;
			height: 100vh;
			width: 100%;
			top: 0;
			left: 0;

			.wallet_content_tb {
				background-size: 100% 100%;
				background-position: top left;
				background-repeat: no-repeat;
				position: fixed;
				z-index: 10;
				width: 690rpx;
				height: 400rpx;
				background-color: #fff;
				top: 20vh;
				left: 30rpx;
				border-radius: 20rpx;

				.hader {
					display: flex;
					justify-content: space-between;
					height: 80rpx;
					border-bottom: #ccc solid 1px;
					width: 690rpx;
					line-height: 80rpx;
				}

				.contents {
					width: 690rpx;
					text-align: center;
					font-size: 28rpx;
					color: #333;
					line-height: 60rpx;
				}

				.money {
					width: 690rpx;
					text-align: center;
					font-size: 48rpx;
					color: #333;
					line-height: 90rpx;
				}

				.pay-pwd-input {
					width: 690rpx;
					height: 100rpx;
					line-height: 100rpx;
					display: flex;
					justify-content: flex-start;

					.pay-pwd-grid {
						margin: 0upx auto;
						margin-top: 10rpx;
						height: 80%;
						line-height: 80%;
						display: flex;
						justify-content: center;

						view {
							width: 100upx;
							height: 100%;
							display: flex;
							align-items: center;
							justify-content: center;
							border: #cececd solid 0.1upx;
							border-radius: 10upx;
							font-size: 36upx;
							font-weight: 600;
						}
					}

				}

				.input-row {
					height: 20%;
					display: flex;
					align-items: center;
					box-sizing: border-box;
					font-size: 20rpx;
					text-align: center;

					.pay-pwd-grid {
						margin: 0upx auto;
						margin-top: 20rpx;
						height: 80%;
						line-height: 80%;
						display: flex;
						justify-content: center;

						.item {
							width: 100rpx;
							height: 100%;
							display: flex;
							align-items: center;
							justify-content: center;
							font-size: 36upx;
							font-weight: 600;
							border-bottom: 1px solid #c8c8c8;
						}

						.item-active {
							position: relative;
							transform: scale(1.2);
						}
					}
				}

				.forget_text {
					width: 650rpx;
					font-size: 28rpx;
					padding: 0 10rpx;
					height: 10%;
					margin-top: 15rpx;
					display: flex;
					justify-content: space-between;
				}
			}

			.wallet_content02 {
				position: fixed;
				bottom: 0rpx;
				border-top-left-radius: 20rpx;
				border-top-right-radius: 20rpx;
				width: 100%;
				height: 35vh;
				background-color: #fff;
				overflow: hidden;
				background-size: 100% 100%;
				background-position: top left;
				background-repeat: no-repeat;

				.capsule {
					height: 35vh;
					position: relative;

					.jp_hea {
						position: absolute;
						bottom: 5rpx;
						height: calc(100% - 5rpx);

						.jq_key {
							display: flex;
							justify-content: flex-start;
							height: 25%;

							.keyboard {
								box-sizing: border-box;
								width: 250upx;
								height: 100%;
								line-height: 100%;
								background-color: rgba(255, 255, 255, .3);
								border-top: #f7f4f4 solid 0.1upx;
								border-left: #f7f4f4 solid 0.1upx;
								font-size: 40upx;
								font-weight: 600;
								display: flex;
								justify-content: center;
								align-items: Center;
							}

							.click_hover {
								animation: showPopup 0.1s linear both;
							}
						}

						.jq_key2 {
							display: flex;
							justify-content: flex-start;
							height: 24%;
							width: 100%;
							padding-top: 1%;
							// background-color: rgba(0,0,0,.3);


							.keyboard {
								box-sizing: border-box;
								width: 240upx;
								margin: 0 6rpx;
								border-radius: 20rpx;
								height: 100%;
								line-height: 100%;
								background-color: rgba(255, 255, 255, .3);
								font-size: 40upx;
								font-weight: 600;
								display: flex;
								justify-content: center;
								align-items: Center;
							}

							.click_hover {
								animation: showPopup 0.1s linear both;
							}
						}
					}


					@keyframes showPopup {
						0% {
							opacity: 0;
							background-color: #d8d8d8;
						}

						100% {
							opacity: 1;
						}
					}
				}
			}

		}

	}

	.btn-sure-setPsw {
		text-align: center;
	}

	.btn-sure-setPsw button {
		display: inline-block;
		margin-top: 10rpx;
		padding: 0 160rpx;
		background: #21a23a;
		line-height: 66rpx;
		font-size: 28rpx;
		color: #fff;
		border-radius: 4rpx;
	}
</style>