<template>
	<view class="rpickerBox">
		<view :class="{'pickerMask':showPicker}" @touchmove.stop.prevent="returnHandle">
			<view :class="{'maskBg':showPicker}" @click="maskClick"></view>
			<view class="r-dtpicker" :class="{'r-dtpicker-show':showPicker}">
				<view class="rdtBtn" @touchmove.stop.prevent="returnHandle" @tap.stop="returnHandle">
					<view @click="pickerCancel">重置</view>
					<view :style="{color:themeColor}" @click="pickerConfirm">确定</view>
				</view>

				<view class="rangeBox">
					<text @tap="changeDateType('startDate')" :style="{ color:themeColor,'border-color':themeColor,opacity:dateType=='startDate'?1:.5}" >
						{{startDate || '请选择'}}
					</text>
					至
					<text @tap="changeDateType('endDate')" :style="{color:themeColor,'border-color':themeColor,opacity:dateType=='endDate'?1:.5}" >
						{{endDate || '请选择'}}
					</text>
				</view>
				
				<picker-view indicator-style="height: 40px;" class="mpvue-picker-view" :value="pickerValue" @change="pickerChangeMul">
					<picker-view-column>
						<view class="picker-item" v-for="(item,index) in yearArr" :key="item">{{item}}</view>
					</picker-view-column>
					<picker-view-column v-if="fields!='year'">
						<view class="picker-item" v-for="(item,index) in monthArr" :key="item">{{item}}</view>
					</picker-view-column>
					<picker-view-column v-if="fields=='day'">
						<view class="picker-item" v-for="(item,index) in dayArr" :key="item">{{item}}</view>
					</picker-view-column>
				</picker-view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		name: 'range-dtpicker',
		props: {
			fields:{
			  type: String,
			  default: 'day'
			},
			start: {
			  type: String,
			  default: '2000-01-01'
			},
			end: {
			  type: String,
			  default: '2200-12-31'
			},
			value: {
                type: Array,
                default(){
					return ['', '']
				}
            },
			show: {
				type: Boolean,
				default: false
			},
			themeColor:{
			  type: String,
			  default: '#4C83D6'
			}
		},
		data() {
			return {
				showPicker: this.show,
				dayArr:[],
				pickerValue:[0,0,0],
				dateType:"startDate",
				startDate:'',
				endDate:""
			};
		},
		watch: {
			show(val) {
				this.showPicker = val;
				if(val){
					// 👇 👇 【核心修复】打开时自动恢复父组件的日期
					this.startDate = this.value[0] || ''
					this.endDate = this.value[1] || ''

					if(this.startDate) {
						if(this.fields == 'day'){
							this.dayArr = this.getMonthDay(this.startDate.slice(0,4), this.startDate.slice(5,7))
						}
						this.pickerValue = this.getIndex(this.startDate)
					}else{
						this.setToday()
					}
				}
			},
		},
		computed: {
			yearArr(){
				var arr = [],start = parseInt(this.start.slice(0,4)),end=parseInt(this.end.slice(0,4));
				for(var i=0;i<=end-start;i++){
					arr.push(start+i)
				}
				return arr;
			},
			monthArr(){
				var arr = [];
				for(var i=1;i<=12;i++){
					var v = i;
					if(v<10)v="0"+v;
					arr.push(v.toString())
				}
				return arr;
			}
		},
		methods:{
			returnHandle(){},
			setToday(){
				const d = new Date()
				const y = d.getFullYear()
				const m = (d.getMonth()+1).toString().padStart(2,'0')
				const day = d.getDate().toString().padStart(2,'0')
				const today = `${y}-${m}-${day}`
				this.pickerValue = this.getIndex(today)
				if(this.fields == 'day') this.dayArr = this.getMonthDay(y,m)
			},
			maskClick(){
				this.$emit("showchange",false);
			},
			// 👇 👇 【核心修复】确定正确触发 change
			pickerConfirm(){
				if(!this.startDate){
					uni.showToast({title:"请选择开始时间",icon:"none"})
					return;
				}
				if(!this.endDate){
					uni.showToast({title:"请选择结束时间",icon:"none"})
					return;
				}
				if(this.endDate < this.startDate){
					uni.showToast({title:"结束时间不得小于开始时间",icon:"none"})
					return;
				}
				this.$emit("change", [this.startDate, this.endDate])
				this.$emit("showchange",false);
			},
			pickerCancel(){
				this.startDate = ''
				this.endDate = ''
				this.dateType = 'startDate'
				this.setToday()
				this.$emit("change", ['', ''])
				this.$emit("showchange",false);
			},
			// 👇 👇 【修复】点击请选择恢复响应
			changeDateType(dateType){
				this.dateType = dateType;
				if(this[dateType]){
					this.pickerValue = this.getIndex(this[dateType])
				}else{
					this.syncPickerVal()
				}
			},
			pickerChangeMul(e){
				this.pickerValue = e.detail.value
				if(this.fields == 'day'){
					let y = this.yearArr[this.pickerValue[0]]
					let m = this.monthArr[this.pickerValue[1]]
					this.dayArr = this.getMonthDay(y,m)
				}
				this.syncPickerVal()
			},
			syncPickerVal(){
				const v = this.pickerValue
				let str = ""
				if(this.fields == 'day'){
					str = `${this.yearArr[v[0]]}-${this.monthArr[v[1]]}-${this.dayArr[v[2]]}`
				}else if(this.fields == 'month'){
					str = `${this.yearArr[v[0]]}-${this.monthArr[v[1]]}`
				}else{
					str = `${this.yearArr[v[0]]}`
				}
				this[this.dateType] = str
			},
			getIndex(value){
				if(!value)return [0,0,0]
				let y = this.yearArr.findIndex(i=> i == value.slice(0,4))
				let m = this.monthArr.findIndex(i=> i == value.slice(5,7))
				let d = this.dayArr.findIndex(i=> i == value.slice(8,10))
				if(this.fields == 'year')return [y]
				if(this.fields == 'month')return [y,m]
				return [y,m,d]
			},
			getMonthDay(year, month){
			  let flag = year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)
			  switch (month) {
			    case '01': case '03': case '05': case '07': case '08': case '10': case '12':
			      return this.getLoopArray(1, 31)
			    case '04': case '06': case '09': case '11':
			      return this.getLoopArray(1, 30)
			    case '02':
			      return flag ? this.getLoopArray(1, 29) : this.getLoopArray(1, 28)
			    default:
			      return []
			  }
			},
			getLoopArray(s,e){
				let arr = []
				for(let i=s;i<=e;i++)arr.push(i.toString().padStart(2,'0'))
				return arr
			}
		}
	}
</script>

<style>
    .pickerMask {
        position: fixed;
        z-index: 998;
        top: 0;
        right: 0;
        left: 0;
        bottom: 0;
    }
	.maskBg{
		position: absolute;
		left:0;
		top:0;
		bottom:0;
		right: 0;
		width:100%;
		height:100%;
		background: rgba(0, 0, 0, 0.6);
	}
    .r-dtpicker {
        position: fixed;
        bottom: 0;
        left: 0;
        width: 100%;
        transition: all 0.3s ease;
        transform: translateY(100%);
        z-index: 998;
    }
    .r-dtpicker-show {
        transform: translateY(0);
    }
    .rdtBtn {
        display: flex;
        padding: 9px 15px;
        background-color: #fff;
        position: relative;
        text-align: center;
        font-size: 17px;
    }
    .rdtBtn:after {
        content: ' ';
        position: absolute;
        left: 0;
        bottom: 0;
        right: 0;
        height: 1px;
        border-bottom: 1px solid #e5e5e5;
        transform-origin: 0 100%;
        transform: scaleY(0.5);
    }
    .rdtBtn view{
        flex: 1;
        color: #1aad19;
    }
    .rdtBtn view:first-child {
        text-align: left;
        color: #888;
    }
    .rdtBtn view:last-child {
        text-align: right;
    }
    .picker-item {
        text-align: center;
        line-height: 40px;
        font-size: 16px;
    }
    .mpvue-picker-view {
        width: 100%;
        height: 238px;
        background: #fff;
    }
	.rangeBox{
		background: #fff;
		display: flex;
		justify-content: center;
		padding: 15px 0;
		font-size: 16px;
	}
	.rangeBox text{
		width:250rpx;
		height:60rpx;
		line-height:60rpx;
		text-align:center;
		border-bottom: 1px solid #000;
	}
</style>