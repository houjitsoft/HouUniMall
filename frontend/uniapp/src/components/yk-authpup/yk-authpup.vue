<template>
	<view v-if="showPopup" class="uni-popup" :style="{top:isNativeHead?'':StatusBar}">
		<view :class="[type, ani, animation ? 'ani' : '']" class="uni-custom uni-popup__wrapper" @click="close(true)">
			<view class="uni-popup__wrapper-box">
				<view class="title">{{authList[permissionID].title}}</view>
				<view class="content">{{authList[permissionID].content}}</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'YkAuthpup',
		props: {
			// 开启动画
			animation: {
				type: Boolean,
				default: true
			},
			type: {
				type: String,
				default: 'top'
			},
			show: {
				type: Boolean,
				default: true
			},
			//是否是原生头部
			isNativeHead:{
				type: Boolean,
				default: true
			}
		},
		data() {
			return {
				permissionID: '',
				ani: '',
				showPopup: false,
				StatusBar:'',
				refuseNum:'',//拒绝次数，
				authList: {
					'READ_EXTERNAL_STORAGE': {
						title: "存储空间/照片权限申请说明:",
						content: "用于上传照片/图片/视频等场景中读取和写入相册和文件内容"
					},
					'WRITE_EXTERNAL_STORAGE': {
						title: "存储空间/照片权限申请说明:",
						content: "用于上传照片/图片/视频等场景中读取和写入相册和文件内容"
					},
					'ACCESS_FINE_LOCATION': {
						title: "地理位置权限申请说明:",
						content: "用于应用程序可以提供基于位置的服务、定位导航、附近搜索等功能"
					},
					'CAMERA':{
						title: "相机/摄像头权限申请说明:",
						content: "用于拍照上传照片/视频等场景"
					},
					'RECORD_AUDIO':{
						title: "麦克风权限申请说明:",
						content: "用于录音、语音通话、发布语音、与客服语音沟通等场景"
					},
					'CALL_PHONE': {
						title: "拨打/管理电话权限申请说明:",
						content: "用于联系买家、骑手或者客服、业务经理与联系等场景"
					}
				}
			}
		},
		created() {
			// #ifdef APP-PLUS
			this.getSystemInfo();
			// #endif
		},
		methods: {
			//获取状态栏高度
			getSystemInfo() {
				let _this = this;
				uni.getSystemInfo({
					success: function(e) {
						_this.StatusBar = e.statusBarHeight + 'px'; //用于自定义头部时，给手机状态栏留出位置,可通过isNativeHead这个参数控制
					}
				})
			},
			open(permissionID) {
				this.permissionID = permissionID
				this.requestPermissions(permissionID);
			},
			close(type) {
				this.ani = '';
				this.$nextTick(() => {
					setTimeout(() => {
						this.showPopup = false;
					}, 300)
				})
			},
			//权限检测
			requestPermissions(permissionID) {
				let _this = this;
				// #ifdef APP-PLUS
				//判断安卓与ios设备
				if (plus.os.name == 'Android') {
					let _permissionID = 'android.permission.' + permissionID;
					plus.android.checkPermission(_permissionID,
						granted => {
							if (granted.checkResult == -1) {
								//还未授权当前查询的权限，打开权限申请目的自定义弹框
								_this.showPopup = true;
								_this.$nextTick(() => {
									setTimeout(() => {
										_this.ani = 'uni-' + _this.type
									},30)
								})
							}
						},
						error => {
							console.log(error.message);
						}
					);
					plus.android.requestPermissions([_permissionID],
						(e) => {
							//关闭权限申请目的自定义弹框
							_this.ani = '';
							_this.$nextTick(() => {
								setTimeout(() => {
									_this.showPopup = false
								}, 0)
							})
							console.log(e,'kkkkk')
							if (e.granted.length > 0) {
								//当前查询权限已授权，此时可以通知页面执行接下来的操作
								_this.$emit('changeAuth');
							}
							if (e.deniedAlways.length > 0) {
								//当前查询权限已被永久禁用，此时需要引导用户跳转手机系统设置去开启
								uni.showModal({
									title: '温馨提示',
									content: '还没有该权限，立即去设置开启？',
									cancelText: "取消",
									confirmText: "去设置",
									showCancel: true,
									confirmColor: '#000',
									cancelColor: '#666',
									success: (res) => {
										if (res.confirm) {
											_this.goSetting();
										}
									}
								})
							}
						})
				} else {
					//IOS不需要添加自定义弹框来描述权限目的，因为在配置文件的隐私信息访问的许可描述里可添加
					//正常可以直接调用uni的API调起权限询问弹框使用各种权限，下面的判断使用场景主要是在IOS禁用某权限后，这个可以判断有无权限，进而引导用户跳转设置开启，仅列出了位置、相册、通讯录、相机、录音等权限，其他IOS权限可具体参考 https://ext.dcloud.net.cn/plugin?id=15787
					let result = 0;
					if (permissionID == 'ACCESS_FINE_LOCATION') {
						//IOS检测位置权限
						let cLLocationManager = plus.ios.importClass("CLLocationManager"),
							authStatus = cLLocationManager.authorizationStatus(),
							enable = cLLocationManager.locationServicesEnabled();
						if (enable && authStatus != 2) {
							result = 1;
						} else {
							result = 0;
						}
						plus.ios.deleteObject(cLLocationManager);
					} else if (permissionID == 'WRITE_EXTERNAL_STORAGE') {
						//IOS检测相册权限
						let PHPhotoLibrary = plus.ios.importClass("PHPhotoLibrary"),
							authStatus = PHPhotoLibrary.authorizationStatus();
						if (authStatus === 3) {
							result = 1;
						} else {
							result = 0;
						}
						plus.ios.deleteObject(PHPhotoLibrary);
					} else if (permissionID == 'CAMERA') {
						//IOS检测相机/摄像头权限
						let avCaptureDevice = plus.ios.importClass("AVCaptureDevice"),
						    authStatus = avCaptureDevice.authorizationStatusForMediaType("vide");
						if (authStatus === 3) {
						  result = 1;
						} else {
						  result = 0;
						}
						plus.ios.deleteObject(avCaptureDevice);
					} else if (permissionID == 'CALL_PHONE') {
						//IOS检测通讯录权限
						let contactStore = plus.ios.importClass("CNContactStore"),
							authStatus = contactStore.authorizationStatusForEntityType(0);
						if (authStatus === 3) {
							result = 1;
						} else {
							result = 0;
						}
						plus.ios.deleteObject(contactStore);
					}else if(permissionID == 'RECORD_AUDIO'){
						//IOS检测麦克风权限
						let aVAudioSession = plus.ios.importClass("AVAudioSession"),
						  aVAudio = aVAudioSession.sharedInstance(),
						  authStatus = aVAudio.recordPermission();
						if ([1684369017, 1970168948].includes(authStatus)) {
						  result = 0;
						} else {
						  result = 1;
						}
						plus.ios.deleteObject(aVAudioSession);
					}
					if (result) {
						//当前查询权限已授权，此时可以通知页面执行接下来的操作
						that.$emit('changeAuth')
					} else {
						//当前查询的权限已禁用,引导用户跳转手机系统设置去开启
						uni.showModal({
							title: '温馨提示',
							content: '还没有该权限，立即去设置开启？',
							cancelText: "取消",
							confirmText: "去设置",
							showCancel: true,
							confirmColor: '#000',
							cancelColor: '#666',
							success: (res) => {
								if (res.confirm) {
									_this.goSetting();
								}
							}
						})
					}
				}
				// #endif
			},
			//跳转手机系统设置
			goSetting() {
				if (plus.os.name == "iOS") {
					var UIApplication = plus.ios.import("UIApplication");
					var application2 = UIApplication.sharedApplication();
					var NSURL2 = plus.ios.import("NSURL");
					var setting2 = NSURL2.URLWithString("app-settings:");
					application2.openURL(setting2);
					plus.ios.deleteObject(setting2);
					plus.ios.deleteObject(NSURL2);
					plus.ios.deleteObject(application2);
				} else {
					var Intent = plus.android.importClass("android.content.Intent");
					var Settings = plus.android.importClass("android.provider.Settings");
					var Uri = plus.android.importClass("android.net.Uri");
					var mainActivity = plus.android.runtimeMainActivity();
					var intent = new Intent();
					intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
					var uri = Uri.fromParts("package", mainActivity.getPackageName(), null);
					intent.setData(uri);
					mainActivity.startActivity(intent);
				}
			}
		}
	}
</script>
<style lang="scss">
	.uni-popup {
		position: fixed;
		top: 0;
		bottom: 0;
		left: 0;
		right: 0;
		z-index: 99999;
		overflow: hidden;
		&__wrapper {
			position: absolute;
			z-index: 999;
			/* #ifndef APP-NVUE */
			box-sizing: border-box;
			/* #endif */
			&.ani {
				/* #ifndef APP-NVUE */
				transition: all 0.3s;
				/* #endif */
			}
			&.top {
				top: 0;
				width:705rpx;
				/* #ifdef APP-NVUE */
				left:22.5rpx;
				/* #endif */
				/* #ifndef APP-NVUE */
				left:0;
				transform: translateY(-705rpx);
				/* #endif */
			}
			&-box {
				position: relative;
				/* #ifndef APP-NVUE */
				box-sizing: border-box;
				/* #endif */
			}
			&.uni-custom {
				& .uni-popup__wrapper-box {
					width: 705rpx;
					/* #ifndef APP-NVUE */
					margin: 0 22.5rpx;
					/* #endif */
					padding: 30upx;
					background: #fff;
					border: solid 2rpx #ddd;
					/* #ifndef APP-NVUE */
					box-sizing: border-box;
					/* #endif */
					border-radius: 16rpx;
					.title{
						font-size: 32rpx;
						font-weight: bold;
					}
					.content{
						margin-top: 16rpx;
						line-height: 1.6;
					}
				}
				&.top{
					& .uni-popup__wrapper-box {
						width: 705rpx;
					}
				}
			}
			&.uni-top{
				transform: translateY(0);
			}
		}
	}
</style>