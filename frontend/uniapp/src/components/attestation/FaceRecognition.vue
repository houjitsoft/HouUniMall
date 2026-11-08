<template>
	<u-popup :show="show" mode="center" round="15" :closeOnClickOverlay="false" :mask="true">
		<view class="face-recognition-container">
			<!-- 关闭按钮 -->
			<view class="close-btn" @click="closePopup">
				<text class="close-icon">×</text>
			</view>

			<!-- 刷脸标题 -->
			<view class="title">{{getTransNew('刷脸认证')}}</view>

			<!-- 视频预览区域 -->
			<view class="video-preview">
				<video v-if="videoUrl" :src="videoUrl" controls class="preview-video"></video>
				<view v-else class="preview-placeholder">
					<image :src="`${mallRequest.MALL_IMG}/liveImage/default-avatar.png`" class="placeholder-icon" mode="aspectFit">
					</image>
					<view class="placeholder-text">{{getTransNew('点击开始刷脸')}}</view>
				</view>
			</view>

			<!-- 操作按钮 -->
			<view class="button-group">
				<!-- 录制提示语 -->
				<view class="recording-tips">
					{{getTransNew('温馨提示：核验的时候尽量不要在窗边、车门边、头顶强光、摄像头不要模糊、晃动幅度不要过大忽近忽远，人脸在屏幕中占大部分且保持完整，以保证视频中人脸特征明显。')}}
				</view>
				<button class="start-button" @click="startFaceRecognition" :disabled="isRecording">
					{{ getTransNew(isRecording ? '录制中...' : '开始刷脸') }}
				</button>
				<button class="reset-button" @click="resetFaceRecognition" :disabled="!videoUrl">
					{{getTransNew('重新录制')}}
				</button>
				<button class="submit-button" @click="submitFaceRecognition" :disabled="!videoUrl || isSubmitting">
					{{ getTransNew(isSubmitting ? '提交中...' : '提交认证') }}
				</button>
			</view>

			<!-- 提示信息 -->
			<view class="tips" v-if="tipsMessage">
				{{ tipsMessage }}
			</view>
		</view>
	</u-popup>
</template>

<script setup>
	import {
		ref,
		onMounted,
		defineEmits,
		watch
	} from 'vue';
	import {
		uploadVideo
	} from "@/api/set.js"
    import {
        mallRequest
    } from "@/utils/request.js"
	// 定义组件事件
	const emit = defineEmits(['recognitionSuccess', 'close']);

	// 状态变量
	const show = defineModel();
	const videoUrl = ref('');
	const isRecording = ref(false);
	const isSubmitting = ref(false);
	const tipsMessage = ref('');


	watch(() => show.value, (newValue) => {
		if (newValue) {
			initData();
			openPopup();
		} else {
			closePopup();
		}
	})
	
	// 数据初始化
	const initData = ()=>{
		videoUrl.value = '';
		isRecording.value = false;
		isSubmitting.value = false;
		tipsMessage.value = '';
	}
	// 打开弹窗
	const openPopup = () => {
		// 打开弹窗时检查摄像头权限
		checkCameraPermission();
	};

	// 关闭弹窗
	const closePopup = () => {
		// 重置状态
		resetFaceRecognition();
		// 触发关闭事件
		emit('close');
	};

	// 初始化
	onMounted(() => {
		// 组件挂载时不自动检查权限，改为在打开弹窗时检查
	});

	// 检查摄像头权限
	const checkCameraPermission = () => {
		// 获取当前平台
		const platform = uni.getSystemInfoSync().platform;

		// uni.getSetting主要用于小程序平台，H5和App平台可能不支持
		// 只在小程序平台尝试检查和请求摄像头权限
		if (platform.includes('mp-')) {
			// 检查当前平台是否支持getSetting API
			if (typeof uni.getSetting === 'function') {
				uni.getSetting({
					success: (res) => {
						if (!res.authSetting['scope.camera']) {
							// 未授权，请求授权
							uni.authorize({
								scope: 'scope.camera',
								success: () => {
									tipsMessage.value = '摄像头权限已获取';
								},
								fail: () => {
									tipsMessage.value = '请在设置中开启摄像头权限';
								}
							});
						}
					}
				});
			} else {
				console.log('当前平台不支持uni.getSetting API');
			}
		} else if (platform === 'h5') {
			// H5平台使用浏览器的MediaDevices API检查摄像头权限
			if (navigator.mediaDevices && navigator.mediaDevices.getUserMedia) {
				navigator.mediaDevices.getUserMedia({
						video: true
					})
					.then(() => {
						tipsMessage.value = '摄像头权限已获取';
					})
					.catch(() => {
						tipsMessage.value = '请在浏览器中允许摄像头访问';
					});
			}
		} else {
			// App平台通常会在使用摄像头时自动请求权限，无需提前检查
			console.log('App平台将在使用摄像头时自动请求权限');
		}
	};

	// 开始刷脸认证
	const startFaceRecognition = () => {
		isRecording.value = true;
		tipsMessage.value = '正在录制视频...';

		// 获取当前平台
		const platform = uni.getSystemInfoSync().platform;

		// 构建不同平台的参数
		const chooseVideoParams = {
			sourceType: ['camera'],
			maxDuration: 5, // 最大录制时长5秒
			camera: 'front', // 使用前置摄像头
			compressed: true, // 压缩视频
			success: (res) => {
				// 录制成功，获取视频临时路径
				videoUrl.value = res.tempFilePath;
				console.log(res);
				tipsMessage.value = '视频录制成功';
				isRecording.value = false;
			},
			fail: (err) => {
				console.error('录制视频失败:', err);
				// 处理用户取消的情况
				if (err.errMsg && err.errMsg.includes('cancel')) {
					tipsMessage.value = '视频录制已取消';
				} else {
					tipsMessage.value = '视频录制失败，请重试';
				}
				isRecording.value = false;
			}
		};

		// 使用uni.chooseVideo录制视频
		uni.chooseVideo(chooseVideoParams);
	};



	// 重置刷脸认证
	const resetFaceRecognition = () => {
		videoUrl.value = '';
		tipsMessage.value = '';
	};

	// 提交刷脸认证
	const submitFaceRecognition = async () => {
		if (!videoUrl.value) {
			uni.showToast({
				title: '请先录制视频',
				icon: 'none'
			});
			return;
		}

		isSubmitting.value = true;
		uni.showLoading({
			title: '正在提交认证...'
		});
		try {
			const {
				code,
				data,
				message
			} = await uploadVideo(videoUrl.value);
			uni.hideLoading();
			if (code === 1) {
				isSubmitting.value = false;
				emit('recognitionSuccess', data);
			} else {
				uni.showToast({
					title: '认证失败，请重试',
					icon: 'none'
				});
			}
		} catch (error) {
			//TODO handle the exception
			uni.hideLoading();
			console.log("上传失败：", error);
		}
	};
</script>

<style scoped>
	.face-recognition-container {
		width: 95%;
		max-width: 680rpx;
		padding: 40rpx;
		box-sizing: border-box;
		background-color: #fff;
		border-radius: 20rpx;
		position: relative;
		overflow: hidden;
	}

	.close-btn {
		position: absolute;
		top: 20rpx;
		right: 15rpx;
		width: 50rpx;
		height: 50rpx;
		border-radius: 50%;
		background-color: rgba(0, 0, 0, 0.1);
		display: flex;
		justify-content: center;
		align-items: center;
		z-index: 10;
	}

	.close-icon {
		font-size: 40rpx;
		color: #666;
		font-weight: bold;
	}

	.title {
		font-size: 36rpx;
		font-weight: bold;
		text-align: center;
		margin: 40rpx 0;
		color: #333;
	}

	.video-preview {
		width: 100%;
		height: 500rpx;
		background-color: #fff;
		border-radius: 10rpx;
		overflow: hidden;
		margin-bottom: 40rpx;
		box-shadow: 0 2rpx 10rpx rgba(0, 0, 0, 0.1);
	}

	.preview-video {
		width: 100%;
		height: 100%;
	}

	.preview-placeholder {
		width: 100%;
		height: 100%;
		display: flex;
		flex-direction: column;
		justify-content: center;
		align-items: center;
		background-color: #fafafa;
	}

	.placeholder-icon {
		width: 150rpx;
		height: 150rpx;
		margin-bottom: 20rpx;
	}

	.placeholder-text {
		font-size: 28rpx;
		color: #999;
	}

	.button-group {
		width: 100%;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.recording-tips {
		font-size: 24rpx;
		color: #666;
		line-height: 36rpx;
		padding: 16rpx;
		background-color: #f5f5f5;
		border-radius: 8rpx;
		text-align: left;
		margin-bottom: 10rpx;
	}

	.start-button,
	.reset-button,
	.submit-button {
		width: 100%;
		height: 88rpx;
		border-radius: 44rpx;
		font-size: 32rpx;
		font-weight: 500;
		border: none;
		margin-top: 10rpx;
	}

	.start-button {
		background-color: #007aff;
		color: #fff;
	}

	.reset-button {
		background-color: #fff;
		color: #666;
		border: 2rpx solid #ddd;
	}

	.submit-button {
		background-color: #4cd964;
		color: #fff;
	}

	.start-button:disabled,
	.reset-button:disabled,
	.submit-button:disabled {
		opacity: 0.6;
	}

	.tips {
		margin-top: 30rpx;
		font-size: 28rpx;
		color: #666;
		text-align: center;
		padding: 0 20rpx;
	}
</style>