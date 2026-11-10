<template>
  <div>
    <div class="wrap">
      <div ref="step1" class="settle-cont-box">
        <h3>{{ titleTips }}</h3>
        <el-steps :active="3" align-center style="margin-top: 50px">
          <el-step title="入驻须知" :icon="ElIconDocument"></el-step>
          <el-step title="商家基本信息填写" :icon="ElIconEdit"></el-step>
          <el-step title="缴纳店铺租金" :icon="ElIconPostcard"></el-step>
          <el-step title="入驻成功" :icon="ElIconDocumentChecked"></el-step>
        </el-steps>
        <div class="">
          <div class="text-wrapper_3">
            <span class="textwrapper10"
              >店铺名称：{{ updatequery.shopName }}</span
            >
            <span class="textwrapper11"
              >店铺等级：{{ updatequery.storeLevel }}
              <el-popover placement="right" width="650" trigger="click">
                <div
                  class="shopTable bgf"
                  v-if="storeLevelData && storeLevelData.length > 0"
                >
                  <ul>
                    <li>店铺等级</li>
                    <li>开店费用</li>
                    <li>收费模式</li>
                    <li v-if="shopType != 2">拥有VIP标识</li>
                    <li v-if="shopType != 2">店铺列表优先展示</li>
                    <li v-if="shopType != 2">商品列表优先展示</li>
                    <li v-if="shopType != 2">优惠券</li>
                    <li v-if="shopType != 2">限时折扣</li>
                    <li v-if="shopType != 2">限时秒杀</li>
                    <li v-if="shopType != 2">满减赠折</li>
                    <li>可发布商品数</li>
                    <li>可上传图片数</li>
                    <li>可上传视频数</li>
                  </ul>
                  <ul v-for="item in storeLevelData" :key="item.id">
                    <li>{{ item.storeLevel }}</li>
                    <li>{{ item.expenses }}</li>
                    <li>线下公对公</li>
                    <li v-if="shopType != 2">
                      {{ item.id == 1 ? '否' : '是' }}
                    </li>
                    <li v-if="shopType != 2">
                      {{ item.shopListFirstShow == 1 ? '是' : '否' }}
                    </li>
                    <li v-if="shopType != 2">
                      {{ item.goodsListFirstShow == 1 ? '是' : '否' }}
                    </li>
                    <li v-if="shopType != 2">
                      {{
                        item.marketingActivitiesList.includes('1') ? '是' : '否'
                      }}
                    </li>
                    <li v-if="shopType != 2">
                      {{
                        item.marketingActivitiesList.includes('2') ? '是' : '否'
                      }}
                    </li>
                    <li v-if="shopType != 2">
                      {{
                        item.marketingActivitiesList.includes('3') ? '是' : '否'
                      }}
                    </li>
                    <li v-if="shopType != 2">
                      {{
                        item.marketingActivitiesList.includes('4') ? '是' : '否'
                      }}
                    </li>
                    <li>
                      {{ item.goodsIsLimit == 1 ? item.goodsCount : '不限' }}
                    </li>
                    <li>
                      {{ item.imageIsLimit == 1 ? item.imageCount : '不限' }}
                    </li>
                    <li>
                      {{ item.videoIsLimit == 1 ? item.videoCount : '不限' }}
                    </li>
                  </ul>
                </div>
                <template v-slot:reference>
                  <el-icon @click="handleChackFee"><icon name="el-icon-QuestionFilled" /></el-icon>
                </template>
              </el-popover>
            </span>
            <span class="textwrapper12">支付方式：对公转账</span>
            <span class="textwrapper13">开店费用：</span>
            <span class="textwrapper14">¥{{ updatequery.expenses }}</span>
          </div>
        </div>
        <div style="margin-top: 40px">
          <div class="fl">
            <div class="section_2">
              <div class="box_12 fl"></div>
              <span class="text_15">对公账信息</span>
            </div>
            <div class="section_4 flex-col">
              <el-form class="mgt20 margt" ref="form" label-width="150px">
                <el-form-item label="银行卡号:">
                  <span class="func-status">{{
                    updateAccount.bankCardId
                  }}</span>
                </el-form-item>
                <el-form-item label="开户人姓名:">
                  <span class="func-status">{{
                    updateAccount.bankCardName
                  }}</span>
                </el-form-item>
                <el-form-item label="开户行名称:">
                  <span class="func-status">{{ updateAccount.bankName }}</span>
                </el-form-item>
                <el-form-item label="银联号:">
                  <span class="func-status">{{
                    updateAccount.brankUnionpayGood
                  }}</span>
                </el-form-item>
                <el-form-item>
                  <span class="func-copy fr" @click.stop="_copy(fuzhi)"
                    >复制</span
                  >
                </el-form-item>
              </el-form>
            </div>
          </div>
          <div class="fl" style="margin-left: 24px">
            <div class="section_3">
              <div class="box_12 fl"></div>
              <span class="text_15">上传支付凭证</span>
            </div>
            <div class="section_4">
              <el-form
                :model="ruleForm"
                :disabled="disabled"
                :rules="rules"
                ref="ruleForm"
                label-width="90px"
                class="demo-ruleForm"
              >
                <el-form-item
                  label="上传图片:"
                  prop="payImage"
                >
                  <div class="imagesfive">
                    <img
                      class="img-sizefive"
                      v-if="imgUrl1"
                      :src="imgUrl1"
                      alt=""
                    />
                    <img
                      class="img-sizefive"
                      v-else
                      src="../../assets/images/join/shangchuantupian.png"
                      alt=""
                    />
                  </div>
                  <div class="shtextwrapperfive relative">
                    <span class="shangchuantupiantext">上传图片</span>
                    <UploadImg
                      class="absolute top0 zIndex2"
                      @changeSrc="handleImageChange1"
                    />
                  </div>
                </el-form-item>
                <el-form-item style="width: 320px">
                  <div class="text-wrapper_7">
                    <span class="text_23"
                      >请上传付款凭证，图片大小请控制在</span
                    >
                    <span class="text_24">2M</span>
                    <span class="text_25">以内，图片格式仅支持：</span>
                    <span class="text_26">JPG、PNG、JPEG</span>
                  </div>
                </el-form-item>
                <el-form-item
                  label="付款说明:"
                  style="width: 540px"
                  prop="payComment"
                >
                  <el-input
                    type="textarea"
                    placeholder="请输入内容"
                    v-model="ruleForm.payComment"
                    maxlength="50"
                    show-word-limit
                  >
                  </el-input>
                </el-form-item>
              </el-form>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="tc settle-bottom-operate">
      <div class="" v-if="auditStatus == 0">
        <el-button type="danger" @click="next('ruleForm')"
          >上传支付凭证</el-button
        >
      </div>
      <div class="" v-if="auditStatus == 1">
        <span style="font-size: 20px"> 审核状态： 待审核 </span>
        <span style="margin-left: 50px">
          平台将在48小时内完成审核，请关注店铺入驻状态
        </span>
      </div>
      <div class="" v-if="auditStatus == 2" style="margin-left: 20px">
        <span style="font-size: 20px"> 审核状态： 失败 </span>
        <span style="margin-left: 30px"> 原因 ：{{ auditComment }} </span>
        <el-button
          v-if="btnreset"
          style="margin-left: 300px"
          type="danger"
          @click="reset('ruleForm')"
          >重新编辑</el-button
        >
        <el-button
          v-else
          style="margin-left: 300px"
          type="danger"
          @click="next('ruleForm')"
          >再次提交审核</el-button
        >
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, useTemplateRef } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification } from 'element-plus'
import {
  QuestionFilled as ElIconQuestion,
  Document as ElIconDocument,
  Edit as ElIconEdit,
  Postcard as ElIconPostcard,
  DocumentChecked as ElIconDocumentChecked,
} from '@element-plus/icons-vue'
import {
  queryBrank,
  payupload,
  payupload2,
  payquery,
  joinagree,
  joinagree2,
  payquery2,
  querySelectLevelByShop,
  querySelectLevelByShop2,
} from '@/api/join'
import UploadImg from '@/views/join/components/uploadImg/index.vue'
import { useAppConfig } from '@/composables/useAppConfig'

const router = useRouter()
const route = useRoute()

const btnreset = ref(true)
const disabled = ref(false)
const imgUrl1 = ref('')
const shopType = ref('')
const joinId = ref('')
const auditComment = ref('')
const ruleForm = reactive({ payImage: '', payComment: '' })
const rules = {
  payImage: [{ required: true, message: '请上传图片', trigger: 'blur' }],
}
const updateAccount = reactive({
  bankCardId: '',
  bankCardName: '',
  bankName: '',
  brankUnionpayGood: '',
  brankUpdateTime: '',
  brankNote: '',
})
const fuzhi = ref('')
const updatequery = ref({})
const auditStatus = ref(null)
const storeLevelData = ref(null)
const titleTips = ref('店铺入驻流程')
const ruleFormEl = useTemplateRef('ruleForm')

onMounted(() => {
  shopType.value = route.query.shopType
  joinId.value = route.query.joinId
  fetchData()
})

function _copy(context) {
  const oInput = document.createElement('input')
  oInput.value = context
  document.body.appendChild(oInput)
  oInput.select()
  document.execCommand('Copy')
  ElMessage.success('复制成功')
  oInput.remove()
}

function handleImageChange1(src) {
  imgUrl1.value = src
}

function reset() {
  disabled.value = false
  btnreset.value = false
}

async function handleChackFee() {
  let res
  if (shopType.value == 2) {
    res = await querySelectLevelByShop2()
  } else {
    res = await querySelectLevelByShop({ levelType: 1 })
  }
  if (res && res.code == 1) {
    storeLevelData.value = res.data
  }
}

function fetchData() {
  queryBrank().then((res) => {
    if (res.code == 1) {
      Object.assign(updateAccount, res.data)
      fuzhi.value =
        res.data.bankCardName + ',' + res.data.bankCardId + ',' + res.data.bankName
    }
  })
  if (shopType.value == 2) {
    joinagree2().then((res) => {
      if (res.code == 1) {
        joinId.value = res.data.oldJoinId == null ? res.data.newJoinId : res.data.oldJoinId
      }
    })
    payquery2().then((res) => {
      if (res.code == 1) {
        handlePayQueryResult(res.data)
      }
    })
  } else {
    joinagree().then((res) => {
      if (res.code == 1) {
        joinId.value = res.data.oldJoinId == null ? res.data.newJoinId : res.data.oldJoinId
      }
    })
    payquery().then((res) => {
      if (res.code == 1) {
        handlePayQueryResult(res.data)
      }
    })
  }
}

function handlePayQueryResult(data) {
  const titleMap = {
    1: '企业店铺入驻流程',
    2: '供应商店铺入驻流程',
    3: '品牌店铺入驻流程',
  }
  if (!data) {
    titleTips.value = titleMap[shopType.value] || '店铺入驻流程'
    updatequery.value = {}
    auditStatus.value = 0
    auditComment.value = ''
    ruleForm.payComment = ''
    imgUrl1.value = ''
    disabled.value = false
    return
  }
  if (data.auditStatus == 1 || data.auditStatus == 2) {
    disabled.value = true
    btnreset.value = true
  }
  titleTips.value = titleMap[data.shopType] || '店铺入驻流程'
  updatequery.value = data
  auditStatus.value = data.auditStatus
  auditComment.value = data.auditComment
  ruleForm.payComment = data.payComment
  imgUrl1.value = data.payImage
  if (data.auditStatus == 3) {
    const query = { joinId: joinId.value, shopType: shopType.value }
    if (data.shopType == 2) {
      // 供应商入驻成功页已迁至 supplier，跨端跳转
      const { safeOpenUrl } = useAppConfig()
      safeOpenUrl('/supplier/#/join/step7', query, '_self')
    } else {
      router.push({ path: '/join/step8', query })
    }
  }
}

function back() {
  router.back()
}

function next() {
  ruleForm.payImage = imgUrl1.value
  ruleFormEl.value?.validate((valid) => {
    if (!valid) return
    const uploadFn = shopType.value == 2 ? payupload2 : payupload
    uploadFn(ruleForm).then((res) => {
      if (res.code == 1) {
        ElNotification({ title: '成功', message: '添加成功', type: 'success' })
        fetchData()
      } else {
        ElNotification({ message: res.desc, type: 'error' })
      }
    })
  })
}
</script>

<style lang="scss">
.zIndex2 {
  z-index: 2;
}
.absolute {
  position: absolute;
}
.top0 {
  top: 0;
}
.func-copy {
  margin-right: 50px;
  cursor: pointer;
  color: red;
  font-weight: 900;
}
.margt {
  margin-top: 40px;
}
.demo-ruleForm {
  margin-top: 40px;
}
.text-wrapper_3 {
  background-color: rgba(219, 44, 44, 0.1);
  border-radius: 1px;
  width: 1136px;
  height: 54px;
  margin-top: 50px;
  color: #333333;
  font-weight: 900;
}
.textwrapper10 {
  width: 140px;
  height: 54px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Medium;
  text-align: left;
  white-space: nowrap;
  line-height: 54px;
  margin: 19px 0 0 35px;
}
.textwrapper11 {
  width: 126px;
  height: 54px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Medium;
  text-align: left;
  white-space: nowrap;
  line-height: 54px;
  margin: 19px 0 0 140px;
}
.textwrapper12 {
  width: 126px;
  height: 54px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Medium;
  text-align: left;
  white-space: nowrap;
  line-height: 54px;
  margin: 19px 0 0 140px;
}
.textwrapper13 {
  width: 70px;
  height: 54px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Medium;
  text-align: left;
  white-space: nowrap;
  line-height: 54px;
  margin: 19px 0 0 140px;
}
.textwrapper14 {
  width: 78px;
  height: 54px;
  overflow-wrap: break-word;
  color: rgba(219, 44, 44, 1);
  font-size: 18px;
  font-family: PingFangSC-Medium;
  text-align: left;
  white-space: nowrap;
  line-height: 54px;
}
.section_2 {
  background-color: rgba(247, 248, 250, 1);
  border-radius: 1px;
  width: 519px;
  height: 50px;
  font-weight: 900;
}
.section_3 {
  background-color: rgba(247, 248, 250, 1);
  border-radius: 1px;
  width: 590px;
  height: 50px;
  font-weight: 900;
}
.box_12 {
  background-color: rgba(219, 44, 44, 1);
  width: 3px;
  height: 10px;
  margin: 20px 0 0 24px;
}
.text_15 {
  width: 100px;
  height: 50px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 16px;
  font-family: PingFangSC-Medium;
  text-align: left;
  white-space: nowrap;
  line-height: 50px;
  margin: 17px 0 0 8px;
}
.section_4 {
  border-radius: 1px;
  height: 365px;
  border: 1px solid rgba(247, 248, 250, 1);
  width: 519px;
}
.text-wrapper_4 {
  width: 196px;
  height: 20px;
  margin: 32px 0 0 35px;
}
.text_17 {
  width: 196px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
}
.text-wrapper_5 {
  width: 154px;
  height: 20px;
  margin: 32px 0 0 35px;
}
.text_18 {
  width: 154px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
}
.text-wrapper_6 {
  width: 252px;
  height: 21px;
  margin: 32px 0 208px 35px;
}
.text_19 {
  width: 202px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
  margin-top: 1px;
}
.text_20 {
  width: 28px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(219, 44, 44, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
}
.section_5 {
  border-radius: 1px;
  height: 365px;
  border: 1px solid rgba(247, 248, 250, 1);
  width: 593px;
}
.block_1 {
  position: relative;
  width: 525px;
  height: 160px;
  margin: 32px 0 0 35px;
}
.text_21 {
  width: 70px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
  margin-top: 1px;
}
.text_22 {
  width: 126px;
  height: 14px;
  overflow-wrap: break-word;
  color: rgba(153, 153, 153, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 14px;
  margin: 114px 311px 0 18px;
}
.image-text_1 {
  width: 56px;
  height: 58px;
  margin: 51px 0 0 195px;
}
.text-group_1 {
  width: 56px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(204, 204, 204, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
  margin-top: 6px;
}
.block_2 {
  width: 445px;
  height: 17px;
  margin: 8px 0 0 115px;
}
.text-wrapper_7 {
  width: 445px;
  height: 17px;
  overflow-wrap: break-word;
  font-size: 0;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 17px;
}
.text_23 {
  width: 445px;
  height: 17px;
  overflow-wrap: break-word;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 17px;
}
.text_24 {
  width: 445px;
  height: 17px;
  overflow-wrap: break-word;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 17px;
}
.text_25 {
  width: 445px;
  height: 17px;
  overflow-wrap: break-word;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 17px;
}
.text_26 {
  width: 445px;
  height: 17px;
  overflow-wrap: break-word;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 17px;
}
.block_3 {
  width: 525px;
  height: 94px;
  margin: 32px 0 22px 35px;
}
.text_27 {
  width: 70px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
  margin-top: 3px;
}
.block_4 {
  border-radius: 3px;
  height: 94px;
  border: 1px solid rgba(220, 220, 220, 1);
  width: 445px;
  justify-content: flex-center;
}
.text-wrapper_8 {
  width: 42px;
  height: 20px;
  margin: 8px 0 0 11px;
}
.text_28 {
  width: 42px;
  height: 20px;
  overflow-wrap: break-word;
  color: rgba(153, 153, 153, 1);
  font-size: 14px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 20px;
}
.text-wrapper_9 {
  width: 28px;
  height: 17px;
  margin: 41px 0 8px 409px;
}
.text_29 {
  width: 28px;
  height: 17px;
  overflow-wrap: break-word;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 17px;
}
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409eff;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 440px;
  height: 158px;
  line-height: 178px;
  margin-left: 30px;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.relative {
  position: relative;
}
.shtextwrapperfive {
  background-color: rgba(219, 44, 44, 1);
  width: 445px;
  text-align: center;
  margin-top: -15px;
}
.imagesfive {
  width: 430px;
  height: 160px;
}
.img-sizefive {
  width: 430px;
  height: 160px;
}
.shangchuantupiantext {
  width: 60px;
  height: 30px;
  overflow-wrap: break-word;
  color: #fff;
  font-size: 18px;
  font-family: PingFangSC-Regular;
  text-align: left;
  white-space: nowrap;
  line-height: 32px;
}
.shopTable {
  display: flex;
  justify-content: center;
  background:#fff;
  ul,
  li {
    list-style: none;
  }

  ul {
    width: 20%;
    border-right: 1px solid #d2d2d2;
    border-bottom: 1px solid #d2d2d2;
    padding: 0px;
    font-size: 12px;
  }

  ul:nth-of-type(1) {
    background: #f3f3f3;
  }

  li {
    width: 100%;
    height: 30px;
    line-height: 30px;
    text-align: center;
    border-top: 1px solid #d2d2d2;
    border-left: 1px solid #d2d2d2;
  }
  .nowUl {
    background: #e9f8e5;
  }
}
</style>
