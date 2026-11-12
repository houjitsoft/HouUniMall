<template>
  <div>
    <div class="wrap">
      <div ref="step1" class="settle-cont-box">
        <h3 class="join-step-title">
          <span>{{ Number(route.query.shopType) === 0 ? '个人店铺入驻流程' : '企业店铺入驻流程' }}</span>
          <el-tooltip
            v-if="Number(route.query.shopType) === 0"
            placement="right-start"
            effect="light"
            :show-after="80"
            popper-class="join-note-popper"
          >
            <template #content>
              <div class="join-note-tip">
                <strong>个人入驻资料说明</strong>
                <p>当前平台资料采集表单为统一主体信息表。个人经营者请在「企业性质」选择「个体工商户」，并按实际持有的资质材料填写；如尚未办理营业执照，请先联系平台客服确认开通方式，避免审核被退回。</p>
              </div>
            </template>
            <el-icon class="join-note-icon" aria-label="个人入驻资料说明"><InfoFilled /></el-icon>
          </el-tooltip>
        </h3>
        <el-steps :active="2" align-center class="join-steps">
          <el-step title="入驻须知" :icon="ElIconDocument"></el-step>
          <el-step title="商家基本信息填写" :icon="ElIconEdit"></el-step>
          <el-step title="缴纳店铺租金" :icon="ElIconPostcard"></el-step>
          <el-step title="入驻成功" :icon="ElIconDocumentChecked"></el-step>
        </el-steps>
        <div class="flexrow">
          <div class="flexcol fl"></div>
          <div class="textwrap fl">企业联系人</div>
          <div class="textwrapperone fl">
            <span class="textword">*</span>
            <span class="texttwo">为必填项</span>
          </div>
        </div>
        <el-form
          :model="ruleForm"
          :rules="rules"
          ref="ruleForm"
          label-width="144px"
          class="demo-ruleForm"
          :disabled="disabled"
        >
          <el-form-item
            label="联系人姓名:"
            style="width: 550px"
            prop="contactName"
          >
            <el-input
              v-model="ruleForm.contactName"
              placeholder="请填写运营人员姓名"
            ></el-input>
          </el-form-item>
          <el-form-item
            label="联系人手机号/邮箱:"
            prop="contactPhone"
			style="width:710px"
          >
            <el-input
              class="contactInput wd370"
              :class="[internationalization_switch ? 'inter' : '']"
              v-model="ruleForm.contactPhone"
              placeholder="请填写店铺主账号手机号/邮箱"
            >
              <template v-slot:prefix>
                <el-select
                  v-if="internationalization_switch"
                  size="mini"
                  class="wd80 areaCode"
                  v-model="ruleForm.internationalDialingCode"
                  placeholder="请选择"
                >
                  <el-option
                    v-for="item in countryOptions"
                    :key="item.internationalDialingCode"
                    :label="item.internationalDialingCode"
                    :value="item.internationalDialingCode"
                  >
                  </el-option>
                </el-select>
              </template>
            </el-input>
            <span class="text_17">{{
              originType == 1
                ? '请填写正确的手机号/邮箱。认证过程中平台将通过该账号与您联系，入驻成功后将成为店铺主账号'
                : '请填写平台已注册手机号 / 邮箱，认证时平台将通过该渠道联系您。'
            }}</span>
          </el-form-item>
          <el-form-item
            label="验证码:"
            style="width: 550px"
            prop="validateCode"
          >
            <el-input
              v-model="ruleForm.validateCode"
              placeholder="请输入验证码"
            >
              <template v-slot:suffix>
                <button
                  class="join-code-btn"
                  type="button"
                  :disabled="!canClick"
                  @click="handleGetCode"
                >
                  {{ content }}
                </button>
              </template>
            </el-input>

            <span class="text_17">运营者手机号/邮箱接收验证码</span>
          </el-form-item>
          <el-form-item label="密码:" v-if="originType == 1" style="width: 550px" prop="settlePW">
            <el-input
              v-model="ruleForm.settlePW"
              type="password"
              show-password
              placeholder="请输入密码"
              autocomplete="new-password"
            >
            </el-input>
            <span class="text_17">{{
              '密码为店铺主账号登录密码；' +
              (passwordConfig.desc || '请按平台密码规则设置密码')
            }}</span>
          </el-form-item>
          <div class="flexrow">
            <div class="flexcol fl"></div>
            <div class="textwrap fl">{{ isPersonal ? '经营者证件' : '公司信息' }}</div>
            <div class="textwrapperone fl">
              <span class="textword">*</span>
              <span class="texttwo">为必填项</span>
            </div>
          </div>
          <el-form-item
            label="企业性质:"
            style="width: 550px; margin-top: 50px"
            prop="enterpriseNature"
            v-if="!isPersonal"
          >
            <el-radio-group v-model="ruleForm.enterpriseNature">
              <el-radio :value="1">个体工商户</el-radio>
              <el-radio :value="2">企业</el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item
            label="公司名称:"
            style="width: 550px; margin-top: 50px"
            prop="enterpriseName"
            v-if="!isPersonal"
          >
            <el-input
              v-model="ruleForm.enterpriseName"
              placeholder="请输入公司名称"
              @blur="handleCheckCompanyName(ruleForm.enterpriseName)"
            ></el-input>
            <span class="text_17">请按照营业执照上登记的完成名称填写</span>
          </el-form-item>
          <el-form-item label="税号:" style="width: 550px" prop="taxId" v-if="!isPersonal">
            <el-input
              v-model="ruleForm.taxId"
              placeholder="请填写公司税号"
            ></el-input>
            <span class="text_17"
              >请按照营业执照上的统一社会信用代码进行填写</span
            >
          </el-form-item>
          <el-form-item class="fi-full fi-img" label="营业执照图片" prop="businessLicencePic" v-if="!isPersonal">
            <div>
              <div class="fl">
                <img class="img-size" v-if="imgUrl1" :src="imgUrl1" alt="" />
                <img
                  class="img-size"
                  v-else
                  src="../../assets/images/join/xiangji.png"
                  alt=""
                />
              </div>
              <div
                class="shtextwrapper relative fl"
                style="text-align: center; line-height: 30px"
                v-if="!imgUrl1"
              >
                <span class="text_39" style="margin: 0">上传图片</span>
                <UploadImg
                  class="absolute top0 zIndex2"
                  @changeSrc="handleImageChange1"
                />
              </div>
              <div class="relative fl" v-if="imgUrl1">
                <button
                  class="button2 fl"
                  type="button"
                  @click="shanchutupian1"
                >
                  删除图片
                </button>
              </div>
            </div>
          </el-form-item>
          <el-form-item class="fi-full" style="width: 550px; margin-bottom: 40px">
            <div>
              <div class="text-group_55">
                <span class="text_55"
                  >请上传清晰的营业执照图片，方便系统自动识别，若为复印件需加盖公司红章。</span
                >
                <div class="text-wrapper_11">
                  <span class="text_40">图片尺寸请确保</span>
                  <span class="text_41">800px*800px</span>
                  <span class="text_42">以上，图片大小请控制在</span>
                  <span class="text_43">2M</span>
                  <span class="text_44">以内，图片格式仅支持：</span>
                  <span class="text_45">JPG、PNG、JPEG</span>
                </div>
              </div>
            </div>
          </el-form-item>
          <el-form-item
            label="营业执照所在地:"
            prop="blLocation"
            style="width: 550px"
            v-if="!isPersonal"
          >
            <AreaCascader
              ref="adds"
              v-model="ruleForm.blLocation"
              :placeholder="ruleForm.blLocation"
              @expand-change="handleItemChangetow"
              @change="handleChangestow"
            />
          </el-form-item>
          <el-form-item
            label="营业执照详细地址:"
            style="width: 550px"
            prop="blAddress"
            v-if="!isPersonal"
          >
            <el-input
              v-model="ruleForm.blAddress"
              placeholder="请输入营业执照详细地址"
            ></el-input>
          </el-form-item>
          <el-form-item class="fi-full" label="营业执照有效期:" prop="validateStartTime" v-if="!isPersonal">
            <el-date-picker
              v-model="ruleForm.validateStartTime"
              @change="deleteTime"
              type="date"
              placeholder="选择日期"
              class="fl"
            >
            </el-date-picker>
            <span style="margin-left: 12px; margin-right: 12px" class="fl"
              >至</span
            >
            <el-date-picker
              v-model="ruleForm.validateEndTime"
              @change="deleteTimetwo"
              type="date"
              placeholder="选择日期"
              class="fl"
            >
            </el-date-picker>
            <div class="imagetextthree fl">
              <div class="checkedone">
                <el-checkbox
                  v-model="checked"
                  color="#fd2d53"
                  >长期有效
                </el-checkbox>
              </div>
            </div>
          </el-form-item>
          <el-form-item
            label="法人姓名:"
            style="width: 550px"
            prop="legalPersonName"
            v-if="!isPersonal"
          >
            <el-input
              v-model="ruleForm.legalPersonName"
              placeholder="请输入法人姓名"
            ></el-input>
          </el-form-item>
          <el-form-item
            label="法人手机号:"
            style="width: 550px"
            prop="legalPersonMobile"
            v-if="!isPersonal"
          >
            <el-input
              v-model="ruleForm.legalPersonMobile"
              placeholder="请输入法人手机号"
            ></el-input>
          </el-form-item>
          <el-form-item
            label="法人身份证:"
            style="width: 550px"
            prop="legalPersonIdCard"
            v-if="!isPersonal"
          >
            <el-input
              v-model="ruleForm.legalPersonIdCard"
              placeholder="请输入法人身份证"
            ></el-input>
          </el-form-item>
          <el-form-item
            class="fi-img"
            label="身份证正面:"
            style="width: 550px"
            prop="idCardFront"
          >
            <div>
              <div class="fl">
                <img
                  class="img-size"
                  v-if="ruleForm.idCardFront"
                  :src="ruleForm.idCardFront"
                  alt=""
                />
                <img
                  class="img-size"
                  v-else
                  src="../../assets/images/join/xiangji.png"
                  alt=""
                />
              </div>
              <div
                class="shtextwrapper relative fl"
                v-if="!ruleForm.idCardFront"
              >
                <span class="text_39">上传图片</span>
                <UploadImg
                  class="absolute top0 zIndex2"
                  @changeSrc="handleImageChange2"
                />
              </div>
              <div
                class="relative fl"
                v-if="ruleForm.idCardFront && auditStatus != 0"
              >
                <button
                  class="button2 fl"
                  type="button"
                  @click="shanchutupian2"
                  v-show="!editNo"
                >
                  删除图片
                </button>
              </div>
            </div>
          </el-form-item>
          <el-form-item
            class="fi-img"
            label="身份证反面:"
            style="width: 550px"
            prop="idCardBack"
          >
            <div>
              <div class="fl">
                <img
                  class="img-size"
                  v-if="ruleForm.idCardBack"
                  :src="ruleForm.idCardBack"
                  alt=""
                />
                <img
                  class="img-size"
                  v-else
                  src="../../assets/images/join/xiangji.png"
                  alt=""
                />
              </div>
              <div
                class="shtextwrapper relative fl"
                v-if="!ruleForm.idCardBack"
              >
                <span class="text_39">上传图片</span>
                <UploadImg
                  class="absolute top0 zIndex2"
                  @changeSrc="handleImageChange4"
                />
              </div>
              <div
                class="relative fl"
                v-if="ruleForm.idCardBack && auditStatus != 0"
              >
                <button
                  class="button2 fl"
                  type="button"
                  @click="shanchutupian3"
                  v-show="!editNo"
                >
                  删除图片
                </button>
              </div>
            </div>
          </el-form-item>
          <el-form-item
            label="企业电话:"
            style="width: 550px"
            prop="enterprisePhone"
            v-if="!isPersonal"
          >
            <el-input
              v-model="ruleForm.enterprisePhone"
              placeholder="请输入企业电话"
              maxlength="15"
            ></el-input>
          </el-form-item>
          <el-form-item
            label="员工总数:"
            style="width: 650px"
            prop="employeeNum"
            v-if="!isPersonal"
          >
            <div class="fl">
              <el-input-number class="wd370 tl"
                min="1"
                :controls="false"
                :step="1"
                step-strictly
                v-model="ruleForm.employeeNum"
                placeholder="请输入员工总数"
                maxlength="8"
              />
            </div>
            <span class="fl" style="margin-left: 15px"> 人 </span>
          </el-form-item>
          <el-form-item
            label="注册资金:"
            style="width: 650px"
            prop="registerMoney"
            v-if="!isPersonal"
          >
            <div class="fl">
              <el-input-number class="wd370 tl"
                min="0.00"
                :precision="2"
                :controls="false"
                v-model="ruleForm.registerMoney"
                placeholder="请输入注册资金"
                maxlength="5"
              />
            </div>
            <span class="fl" style="margin-left: 15px">
              万{{ currencyRemark }}
            </span>
          </el-form-item>
          <div class="flexrow">
            <div class="flexcol fl"></div>
            <div class="textwrap fl">店铺经营信息</div>
            <div class="textwrapperone fl">
              <span class="textword">*</span>
              <span class="texttwo">为必填项</span>
            </div>
          </div>
          <el-form-item
            label="店铺名称:"
            maxlength="20"
            style="width: 650px; margin-top: 50px"
            prop="shopName"
          >
            <el-input class="wd370"
              v-model="ruleForm.shopName"
              placeholder="请输入店铺名称"
            ></el-input>
            <span class="text_17">请设置独特且具有代表性的店铺名称</span>
          </el-form-item>
          <el-form-item label="店铺所在地" prop="shopLocation">
            <AreaCascader
              ref="addr"
              v-model="ruleForm.shopLocation"
              :placeholder="ruleForm.shopLocation"
              width="370px"
              @expand-change="handleItemChange"
              @change="handleChanges"
            />
          </el-form-item>
          <el-form-item
            label="店铺详细地址:"
            prop="shopAddress"
          >
            <el-input class="wd370"
              v-model="ruleForm.shopAddress"
              placeholder="请输入店铺详细地址"
            ></el-input>
          </el-form-item>
          <el-form-item class="fi-full" label="店铺等级" prop="shopLevelId">
            <el-select
              v-model="ruleForm.shopLevelId"
              placeholder="请选择店铺等级"
              class="wd370"
              @change="handleLevChange"
            >
              <el-option
                v-for="item in storeLevelData"
                :key="item.id"
                :label="(item.id == 4 ? '* ' : '') + (item.levelName || item.storeLevel)"
                :value="item.id"
              ></el-option>
            </el-select>
            <span class="textdianpushoufei"
              >不同店铺等级缴费金额不同，点击
              <span class="dianpushoufei">
                <el-popover placement="left-end" width="650">
                  <div
                    class="shopTable"
                    v-if="storeLevelData && storeLevelData.length > 0"
                  >
                    <ul>
                      <li>店铺等级</li>
                      <li>开店费用</li>
                      <li>收费模式</li>
                      <li>拥有VIP标识</li>
                      <li>店铺列表优先展示</li>
                      <li>商品列表优先展示</li>
                      <li>优惠券</li>
                      <li>限时折扣</li>
                      <li>限时秒杀</li>
                      <li>满减赠折</li>
                      <li>可发布商品数</li>
                      <li>可上传图片数</li>
                      <li>可上传视频数</li>
                    </ul>
                    <ul v-for="item in storeLevelData" :key="item.id">
                      <li>{{ item.levelName || item.storeLevel }}</li>
                      <li>{{ item.expenses }}</li>
                      <li>线下公对公</li>
                      <li>{{ item.id == 1 ? '否' : '是' }}</li>
                      <li>{{ item.shopListFirstShow == 1 ? '是' : '否' }}</li>
                      <li>{{ item.goodsListFirstShow == 1 ? '是' : '否' }}</li>
                      <li>
                        {{
                          item.marketingActivitiesList.includes('1')
                            ? '是'
                            : '否'
                        }}
                      </li>
                      <li>
                        {{
                          item.marketingActivitiesList.includes('2')
                            ? '是'
                            : '否'
                        }}
                      </li>
                      <li>
                        {{
                          item.marketingActivitiesList.includes('3')
                            ? '是'
                            : '否'
                        }}
                      </li>
                      <li>
                        {{
                          item.marketingActivitiesList.includes('4')
                            ? '是'
                            : '否'
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
                    <span @click="handleChackFee"
                      >查看店铺权益和收费标准>>
                    </span>
                  </template>
                </el-popover>
              </span>
            </span>
            <br /><span class="text_17"
              >请按照您的需求选择店铺等级，等级中标记有”*”形状代表品牌店铺，入驻成功后可在店铺后台-首页升级店铺等级;</span
            >
          </el-form-item>
          <el-form-item
            style="width: 550px"
            label="银行开户名:"
            prop="bankName"
          >
            <el-input
              v-model="ruleForm.bankName"
              placeholder="请输入银行开户名"
            ></el-input>
          </el-form-item>
          <el-form-item
            style="width: 550px"
            label="银行卡号:"
            prop="bankCardNo"
          >
            <el-input
              v-model="ruleForm.bankCardNo"
              oninput="value=value.replace(/[^\d]/g, '')"
              placeholder="请输入银行卡号"
            ></el-input>
            <div class="text_17">银行账号由16、17、19或20位数字组成</div>
          </el-form-item>
          <el-form-item
            style="width: 550px"
            label="开户银行所在地:"
            prop="bankLocation"
          >
            <AreaCascader
              ref="addsr"
              v-model="ruleForm.bankLocation"
              :placeholder="ruleForm.bankLocation || '请选择开户银行所在地'"
              width="370px"
              @expand-change="handleItemChange1"
              @change="handleChanges1"
            />
          </el-form-item>
          <el-form-item
            style="width: 550px"
            label="开户银行支行名称:"
            prop="bankBranchName"
          >
            <el-input
              v-model="ruleForm.bankBranchName"
              placeholder="请输入开户银行支行名称"
            ></el-input>
          </el-form-item>
          <el-form-item
            style="width: 550px"
            label="开户银行支行联行号:"
            prop="branchId"
          >
            <el-input
              v-model="ruleForm.branchId"
              oninput="value=value.replace(/[^\d]/g, '')"
              placeholder="请输入开户银行支行联行号"
            ></el-input>
            <div class="text_17">
              地区银行的唯一识别标志，由12位数字组成：3位银行代码，4位城市代码，4位银行编号，1位校验位
            </div>
          </el-form-item>
          <el-form-item class="fi-full" label="合作协议" prop="corporateAgreement">
            <div>
              <UploadImg2
                class="absolute top0 zIndex2"
                @changeSrc="handleImageChange3"
                @changeSrctow="handleImageChangetow"
              />
              <a
                v-if="ruleForm.corporateAgreement"
                target="_blank"
                class="fl"
                style="margin-left: 100px; line-height: 15px; height: 15px"
                rel="noopener noreferrer"
                :href="ruleForm.corporateAgreement"
                >{{ ruleForm.agreementFileName }}</a
              >
            </div>
          </el-form-item>
          <el-form-item class="fi-full" style="margin-bottom: 40px; margin-top: 50px">
            <div>
              <div class="text-group_55">
                <p class="ruzhuxieyi">
                  如还未下载商家入驻合作协议，可点击
                  <a
                    target="_blank"
                    rel="noopener noreferrer"
                    v-if="shopType != 3"
                    :href="tableData2.url"
                    class="downloadFile"
                    >商家入驻合作协议>></a
                  >
                  <a
                    target="_blank"
                    rel="noopener noreferrer"
                    v-else
                    :href="tableData2.url2"
                    class="downloadFile"
                    >品牌商家入驻合作协议>></a
                  >
                </p>
                <div class="textwrapperfivr">
                  <span class="text_75"
                    >请上传附有红色公章的合作协议，支持上传</span
                  >
                  <span class="text_76">pdf</span>
                  <span class="text_77">格式</span>
                </div>
              </div>
            </div>
          </el-form-item>
          <el-form-item class="fi-full"
            label="主营类目"
            prop="productClassId"
            style="margin-bottom: 200px; margin-top: 50px"
          >
            <div style="margin-bottom: 14px">
              <div class="group_28" @click="handleAdd">
                <img
                  class="thumbnail_1"
                  referrerpolicy="no-referrer"
                  src="../../assets/images/join/jiahao.png"
                />
                <span class="text_71">添加类目</span>
              </div>
              <span v-if="pendingCatCount" class="join-cat-pending-hint">
                已选 {{ pendingCatCount }} 个经营类目，点弹窗「确认提交」后生效
              </span>
              <span v-else-if="hasSelectedCategories()" class="join-cat-selected-hint">
                已添加 {{ tableData.length }} 个经营类目
              </span>
              <div class="image-text_2">
                <el-button
                  link
                  size="mini"
                  :icon="ElIconSetting"
                  @click="operationCommonState"
				  :disabled="productClassIds.length === 0"
                >
                  批量删除</el-button
                >
              </div>
            </div>
            <el-table ref="categoryTable"
              :data="tableData"
              stripe
              :header-cell-class-name="'reset-header-cell'"
              @selection-change="handleSelectionChange"
              :cell-class-name="'reset-cell'"
              style="width: 100%; margin-top: 20px; margin-bottom: 20px"
            >
              <el-table-column type="selection" label="全选" width="55" />
              <el-table-column
                label="序号"
                show-overflow-tooltip
                align="center"
              >
                <template v-slot="scope">{{
                  scope.row.productClassId
                }}</template>
              </el-table-column>
              <el-table-column label="一级类目" align="center">
                <template v-slot="scope">{{ scope.row.catNames?.[0] }}</template>
              </el-table-column>
              <el-table-column label="二级类目" align="center">
                <template v-slot="scope">{{ scope.row.catNames?.[1] }}</template>
              </el-table-column>
              <el-table-column label="三级类目" align="center">
                <template v-slot="scope">{{ scope.row.catNames?.[2] }}</template>
              </el-table-column>
              <el-table-column label="平台抽佣" align="center">
                <template v-slot="scope"
                  >{{ Number(scope.row.commissionRate) }}%</template
                >
              </el-table-column>
              <el-table-column label="操作" align="center">
                <template v-slot="scope">
                  <el-button
                    style="color: red"
                    size="mini"
                    link
                    @click="handleDelete(scope.row)"
                    round
                    >删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-pagination
          :current-page="messagedate.currentPage"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :page-sizes="[10, 20, 30, 40]"
              :page-size="messagedate.pageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
            >
            </el-pagination>
          </el-form-item>
          <!-- 新增经营类目 -->
          <el-dialog
            title="新增经营类目"
            v-model="addDialogVisible"
            width="50%"
          >
            <div
              v-loading="catLoading"
              element-loading-text="加载中，请稍候..."
              style="min-height: 120px"
            >
              <el-form
                label-position="right"
                label-width="100px"
                :model="addParams"
              >
                <el-form-item label="经营类目:" prop="typeId">
                  <el-cascader
                    v-model="catName"
                    :options="optionsone"
                    style="width: 80%"
                    :props="{
                      expandTrigger: 'hover',
                      value: 'catId',
                      label: 'catName',
                      children: 'childMenu',
                      multiple: true,
                    }"
                    clearable
                    @change="changeCat"
                    :collapse-tags="true"
                  ></el-cascader>
                </el-form-item>
              </el-form>
              <div class="mgt20">
                <el-button
                  type="primary"
                  class="submit"
                  size="mini"
                  @click="handleConfirm"
                  round
                  >确认提交</el-button
                >
              </div>
            </div>
          </el-dialog>
        </el-form>
      </div>
    </div>
    <div class="tc settle-bottom-operate">
      <div class="wrap settle-bottom-inner" v-if="auditStatus == null">
        <el-button @click="back">上一步</el-button>
        <el-button type="danger" :loading="submitLoading" :disabled="!loggedIn" @click="next('ruleForm')">提交入驻审核</el-button>
      </div>
      <div class="wrap settle-bottom-inner settle-bottom-status" v-if="auditStatus == 0">
        <div class="status-badge is-pending">审核状态：待审核</div>
        <p class="status-tip">
          您的店铺入驻申请正在审核中，审核完成后将通过短信通知您。审核期间您可登录商家后台查看入驻详情；审核通过后，即可正常使用店铺管理功能。
        </p>
      </div>
      <div class="wrap settle-bottom-inner settle-bottom-fail" v-if="auditStatus == 2">
        <div class="fail-info">
          <div class="status-badge is-fail">审核状态：失败</div>
          <p class="fail-reason">
            <span class="fail-reason-label">原因</span>
            <span class="fail-reason-text">{{ auditComment }}</span>
          </p>
        </div>
        <div class="fail-actions">
          <el-button size="medium" @click="back">上一步</el-button>
          <el-button size="medium" v-if="btnreset" type="danger" @click="reset('ruleForm')">重新编辑</el-button>
          <el-button size="medium" v-else type="danger" :loading="submitLoading"
            @click="nexttow('ruleForm')">重新提交</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount, watch, nextTick, getCurrentInstance, useTemplateRef } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import {
  Document as ElIconDocument,
  Edit as ElIconEdit,
  Postcard as ElIconPostcard,
  DocumentChecked as ElIconDocumentChecked,
  Setting as ElIconSetting,
  InfoFilled,
} from '@element-plus/icons-vue'
import {
  sendPhoneCode,
  classquerylist,
  messagequery,
  queryexpense,
  queryGoodsCats,
  catquery,
  joininserttow,
  shopinfoquery,
  joinagree,
  joininserttowupdate,
  classbinddelete,
  operationCommon,
  phonecheck,
  companyNamecheck,
  businessUserisBusiness,
  queryLimitGroup,
  querySelectLevelByShop,
  queryCountryAndCurrency,
  queryPasswordConfig,
} from '@/api/join'
import UploadImg from '@/views/join/components/uploadImg/index.vue'
import UploadImg2 from '@/views/join/components/uploadImg/file.vue'
import AreaCascader from '@/views/join/components/AreaCascader/index.vue'
import { dateFormat } from '@/utils/joinUtil'
import { validatePassword } from '@/utils/password'
import { useAppConfig } from '@/composables/useAppConfig'
import { setToken, hasJoinToken } from '@/utils/joinAuth'
import {
  draftKey,
  serializeForm,
  deserializeForm,
  hasDraftContent,
  readDraft,
  isOwnDraft,
  writeDraft,
  removeDraft,
} from '@/utils/joinDraft'

const { baseUrl, safeOpenUrl } = useAppConfig()
const router = useRouter()
const route = useRoute()

// 入驻接口全部要求登录态；未登录直接去登录页，登录成功后由登录页按 redirect 回跳本页。
const loggedIn = ref(hasJoinToken())

const currencyRemark = computed(() => {
  const gp = getCurrentInstance()?.appContext.config.globalProperties
  return gp?.currencyRemark ?? '元'
})
const internationalization_switch = ref(false)

const ruleFormEl = useTemplateRef('ruleForm')
const categoryTableEl = useTemplateRef('categoryTable')
const addsEl = useTemplateRef('adds')
const addsrEl = useTemplateRef('addsr')

const countryOptions = ref([])
const storeLevelData = ref([])
const shopType = ref('')
const isPersonal = computed(() => Number(route.query.shopType) === 0)
const url = baseUrl
const selectProductIds = ref([])
const btnreset = ref(true)
const submitLoading = ref(false)
const disabled = ref(false)
const productClassIdtow = ref('')
const queryshopType = ref(1)
const shopLocation = ref('请选择店铺所在地')
const blLocation = ref('')
const auditComment = ref('')
const checked = ref(false)
const radio = ref('')
const imgUrl1 = ref('')
const addDialogVisible = ref(false)
const content = ref('获取验证码')
const totalTime = ref(60)
const canClick = ref(true)
const editNo = ref(false)
const fileName = ref('')
const originType = ref(route.query.origin || 0);
const ruleForm = reactive({
  joinId: '',
  idCardBack: '',
  idCardFront: '',
  enterpriseNature: '',
  bankBranchName: '',
  bankLocation: '',
  bankCardNo: '',
  bankAddress: '',
  bankName: '',
  branchId: '',
  legalPersonName: '',
  legalPersonMobile: '',
  legalPersonIdCard: '',
  frontOfIDCard: '',
  reverseSideOfIDCard: '',
  regAddress: '',
  validateCode: '',
  contactName: '',
  contactPhone: '',
  trueName: '',
  idNo: '',
  idPicOne: '',
  idPicTwo: '',
  businessLicencePic: '',
  manufacturePermitPic: '',
  validateLongTerm: '',
  openPermitPic: '',
  blProvince: '',
  blCity: '',
  blArea: '',
  blAddress: '',
  validateStartTime: '',
  validateEndTime: '',
  enterpriseName: '',
  taxId: '',
  enterprisePhone: '',
  employeeNum: null,
  registerMoney: null,
  shopName: '',
  shopProvince: '',
  shopCity: '',
  shopArea: '',
  shopAddress: '',
  shopClass: '',
  corporateAgreement: '',
  shopType: 1,
  idType: '',
  agreementFileName: '',
  tempArray: [],
  tempArray1: [],
  tempArraytow: [],
  productClassId: [],
  shopLevelId: '',
  internationalDialingCode: '',
  settlePW: '',
})

const passwordConfig = reactive({
  passwordFormat: '',
  passwordLength: '',
  desc: '',
})

const formshopName = reactive({
  shopName: '',
  enterpriseName: '',
  userMobile: '',
})

const bindupdate = reactive({
  joinId: '',
  productClassId: [],
})

const binddelete = reactive({
  shopId: '',
  productClassId: '',
  joinId: '',
})

const messagedate = reactive({
  joinId: '',
  currentPage: 1,
  pageSize: 10,
})

const addMobile = reactive({
  userMobile: '',
  interDialingCode: '',
})

const agreementFileName = ref('')
const productClassIds = ref([])
const operationItem = reactive({
  productClassIds: [],
  joinId: '',
})

const addParams = reactive({
  productClassId: '',
  joinId: '',
})

const gridData = ref([])
const tableData = ref([])
const tableData2 = reactive({ url: '', url2: '' })
const params = ref({})
const joinId = ref('')
const total = ref(0)
const catName = ref('')
const shopId = ref(1)
const optionsClass = ref([])
const optionsone = ref([])
const catLoading = ref(false)

// ===== 经营类目：已选类目的全路径索引 =====
// 类目树由 /goods/cat/cat/query 一次性返回；摊平索引只用于把已选路径
// 还原成「一级 / 二级 / 三级」文本，供待确认提示与主营类目表格回显使用。
const catLeafIndex = computed(() => {
  const leaves = []
  const walk = (nodes, path, labels) => {
    if (!Array.isArray(nodes)) return
    for (const node of nodes) {
      if (!node || node.catId == null) continue
      const nextPath = [...path, node.catId]
      const nextLabels = [...labels, String(node.catName ?? '')]
      const children = Array.isArray(node.childMenu) ? node.childMenu : []
      if (children.length) {
        walk(children, nextPath, nextLabels)
      } else {
        leaves.push({
          key: nextPath.join('-'),
          path: nextPath,
          text: nextLabels.join(' / '),
        })
      }
    }
  }
  walk(optionsone.value, [], [])
  return leaves
})

// ===== 经营类目：已选状态的统一事实源 =====
// 服务端类目列表可能因为登录态失效、绑定落库延迟等原因返回空，
// 这只能说明「暂时查不到」，不等于「用户没选类目」。用户点「确认提交」且绑定
// 接口成功后，本地立即记住这批类目并回显到主营类目表格，不再被空响应抹掉。
const confirmedCatRows = ref([])

const pendingCatRows = computed(() => {
  const paths = Array.isArray(catName.value) ? catName.value : []
  return paths.map((item) => {
    const path = Array.isArray(item) ? item : [item]
    const key = path.join('-')
    const hit = catLeafIndex.value.find((leaf) => leaf.key === key)
    return { key, path, text: hit?.text ?? path.join(' / ') }
  })
})

const pendingCatCount = computed(() => pendingCatRows.value.length)

function hasSelectedCategories() {
  return tableData.value.length > 0 || confirmedCatRows.value.length > 0
}

function mergeConfirmedCatRows(rows) {
  const map = new Map()
  for (const row of [...confirmedCatRows.value, ...(rows || [])]) {
    if (row && row.productClassId != null) {
      map.set(String(row.productClassId), row)
    }
  }
  confirmedCatRows.value = Array.from(map.values())
}

function dropConfirmedCatRows(ids) {
  const removed = new Set((ids || []).map((id) => String(id)))
  confirmedCatRows.value = confirmedCatRows.value.filter(
    (row) => !removed.has(String(row.productClassId)),
  )
}

function catRowsFromPaths(paths) {
  return (paths || []).map((item) => {
    const path = Array.isArray(item) ? item : [item]
    const hit = catLeafIndex.value.find((leaf) => leaf.key === path.join('-'))
    return {
      productClassId: path[path.length - 1],
      catNames: hit ? hit.text.split(' / ') : path.map(String),
      commissionRate: 0,
    }
  })
}

/** 表格、计数、提交哨兵统一由「当前可见类目 + 本地已确认类目」推导 */
function syncCategoryView() {
  if (!tableData.value.length && confirmedCatRows.value.length) {
    tableData.value = [...confirmedCatRows.value]
    total.value = tableData.value.length
  }
  productClassIdtow.value = hasSelectedCategories() ? 1 : ''
  selectProductIds.value = tableData.value
    .map((row) => row?.productClassId)
    .filter((id) => id != null)
}

const agreement = reactive({ sign: 'GEN_COR_AGREEMENT' })
const agreementtow = reactive({ sign: 'BR_AGREEMENT' })
const auditStatus = ref(null)
const src = ref('')

// ===== 登记信息本机草稿 =====
// 按「当前会员 + 入驻类型」隔离，7 天有效；密码、短信验证码、joinId 不落盘。
// 键在本次挂载时固定，避免提交成功写入新 token 后清不掉旧草稿。
const currentDraftKey = draftKey(route.query.shopType)
const draftRestored = ref(false)
const draftTouchedKeys = new Set()
let draftEnabled = true
let draftTimer = null

function draftExtra() {
  return {
    imgUrl1: imgUrl1.value,
    fileName: fileName.value,
    agreementFileName: agreementFileName.value,
    checked: checked.value,
    radio: radio.value,
    originType: originType.value,
    shopLocation: shopLocation.value,
    blLocation: blLocation.value,
  }
}

function saveDraft() {
  if (!draftEnabled) return
  draftTimer = null
  const form = serializeForm(ruleForm)
  const extra = draftExtra()
  if (!hasDraftContent(form, extra)) {
    removeDraft(currentDraftKey)
    return
  }
  writeDraft(currentDraftKey, {
    form,
    extra,
    joinId: joinId.value,
    shopType: String(route.query.shopType ?? ''),
  })
}

function scheduleSaveDraft() {
  if (!draftEnabled) return
  if (draftTimer !== null) window.clearTimeout(draftTimer)
  draftTimer = window.setTimeout(saveDraft, 400)
}

function flushDraft() {
  if (draftTimer !== null) {
    window.clearTimeout(draftTimer)
    draftTimer = null
  }
  saveDraft()
}

/** 表单有草稿时，后端异步回填不得覆盖用户刚录入、还没提交的内容 */
function applyServerField(key, value) {
  // 入驻主键以当前会话为准；密码与验证码只由用户当场录入
  if (key === 'joinId' || key === 'settlePW' || key === 'validateCode') return
  if (draftRestored.value && draftTouchedKeys.has(key)) return
  ruleForm[key] = value
}

function applyServerData(data) {
  Object.keys(data || {}).forEach((key) => applyServerField(key, data[key]))
}

function restoreDraft() {
  const data = readDraft(currentDraftKey)
  if (!data || !isOwnDraft(data)) return false
  const form = deserializeForm(data.form || {})
  const touched = Object.keys(form).filter((key) => {
    const value = form[key]
    if (Array.isArray(value)) return value.length > 0
    return value !== '' && value !== null && value !== undefined
  })
  if (!touched.length && !hasDraftContent({}, data.extra)) return false
  // 逐字段覆盖：joinId / 密码 / 验证码不落盘，恢复后仍取服务端与当前会话的值
  Object.assign(ruleForm, form)
  touched.forEach((key) => draftTouchedKeys.add(key))
  const extra = data.extra || {}
  if (extra.imgUrl1 !== undefined) imgUrl1.value = extra.imgUrl1
  if (extra.fileName !== undefined) fileName.value = extra.fileName
  if (extra.agreementFileName !== undefined) agreementFileName.value = extra.agreementFileName
  if (extra.checked !== undefined) checked.value = !!extra.checked
  if (extra.radio !== undefined) radio.value = extra.radio
  if (extra.originType !== undefined) originType.value = extra.originType
  if (extra.shopLocation !== undefined) shopLocation.value = extra.shopLocation
  if (extra.blLocation !== undefined) blLocation.value = extra.blLocation
  draftRestored.value = true
  return true
}

/** 提交成功后丢弃草稿；后续 fetchData 回填不应再把它写回来 */
function discardDraftAfterSubmit() {
  draftEnabled = false
  if (draftTimer !== null) {
    window.clearTimeout(draftTimer)
    draftTimer = null
  }
  removeDraft(currentDraftKey)
  draftTouchedKeys.clear()
  draftRestored.value = false
}

watch(
  [
    () => ruleForm,
    imgUrl1,
    fileName,
    agreementFileName,
    checked,
    radio,
    originType,
    joinId,
    shopLocation,
    blLocation,
  ],
  scheduleSaveDraft,
  { deep: true },
)
window.addEventListener('beforeunload', flushDraft)

const validatePass = (rule, value, callback) => {
  ruleForm.productClassId = selectProductIds.value
  // 与「提交入驻审核」共用同一份判断，避免表格已回显但表单校验仍误报
  if (!hasSelectedCategories()) {
    callback(new Error('请添加经营类目'))
  } else {
    callback()
  }
}

function validatePasswordRule(rule, value, callback) {
  if (originType.value != 1) {
    callback()
    return
  }
  if (!value || !String(value).trim()) {
    callback(new Error('请输入密码'))
    return
  }
  if (!passwordConfig.passwordFormat) {
    callback()
    return
  }
  const result = validatePassword(
    String(value).trim(),
    passwordConfig.passwordFormat,
    passwordConfig.passwordLength,
    passwordConfig.desc
  )
  if (!result.valid) {
    callback(new Error(result.message))
    return
  }
  callback()
}

const rules = computed(() => {
  // 个人入驻（shopType=0）仅需经营者身份证，企业专属字段不校验
  const companyRequired = (msg, trigger = 'blur') =>
    isPersonal.value ? [] : [{ required: true, message: msg, trigger }]
  return {
    contactName: [{ required: true, message: '请输入运营人员姓名', trigger: 'blur' }],
    contactPhone: [{ required: true, message: '请输入联系人手机', trigger: 'blur' }],
    validateCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
    settlePW: [
      {
        validator: (rule, value, callback) => {
          if (originType.value != 1) {
            callback()
            return
          }
          if (!value || !String(value).trim()) {
            callback(new Error('请输入密码'))
            return
          }
          validatePasswordRule(rule, value, callback)
        },
        trigger: 'blur',
      },
    ],
    shopName: [
      { required: true, message: '请输入店铺名称', trigger: 'blur' },
      { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' },
    ],
    shopLocation: [{ required: true, trigger: 'change', message: '请选择地区' }],
    shopAddress: [{ required: true, message: '请输入店铺详细地址', trigger: 'blur' }],
    shopClass: [{ required: true, message: '请输入店铺分类', trigger: 'blur' }],
    shopLevelId: [{ required: true, message: '请选择店铺等级', trigger: 'change' }],
    corporateAgreement: [{ required: true, message: '请上传合作协议', trigger: 'change' }],
    productClassId: [{ required: true, validator: validatePass }],
    // 经营者身份证：个人与企业均必填
    idCardFront: [{ required: true, message: '请上传身份证正面', trigger: 'change' }],
    idCardBack: [{ required: true, message: '请上传身份证反面', trigger: 'change' }],
    // 以下为企业专属字段，个人入驻不校验
    businessLicencePic: companyRequired('请上传营业执照'),
    taxId: companyRequired('请输入公司税号'),
    blAddress: companyRequired('请输入营业执照详细地址'),
    enterpriseName: companyRequired('请输入公司名称'),
    enterprisePhone: companyRequired('请输入企业电话'),
    enterpriseNature: companyRequired('请选择企业性质', 'change'),
    legalPersonName: companyRequired('请输入法人姓名'),
    legalPersonMobile: companyRequired('请输入法人手机号'),
    legalPersonIdCard: companyRequired('请输入法人身份证'),
    validateStartTime: companyRequired('请选择证件有效期', 'change'),
    bankName: companyRequired('请输入银行开户名'),
    bankCardNo: companyRequired('请输入银行卡号'),
    bankLocation: companyRequired('请输入开户银行所在地', 'change'),
    bankBranchName: companyRequired('请输入开户银行支行名称'),
    branchId: companyRequired('请输入开户银行支行联行号'),
  }
})


onMounted(async () => {
  joinId.value = route.query.joinId || ''
  shopType.value = Number(route.query.shopType)
  ruleForm.joinId = joinId.value
  // 没登录就别让用户看到一张填了也提交不了的表单，直接去登录，登录成功后回到本页
  if (!loggedIn.value) {
    router.replace({ path: '/login', query: { redirect: route.fullPath } })
    return
  }
  // 直接打开 step3 或链接里 joinId 为空时，从 queryAgree 补一个入驻主键。
  // 否则经营类目绑定 / 列表查询 / 提交都拿不到 joinId，表现为「添加类目不显示」。
  if (!joinId.value) {
    try {
      const res = await joinagree()
      // 首次入驻时 oldJoinId 常为空字符串，用 || 才能回退到 newJoinId；
      // 否则类目绑定与列表查询都会带着空主键，表现为「选了类目不显示」。
      const id = res?.data?.oldJoinId || res?.data?.newJoinId
      if (res?.code === 1 && id) {
        joinId.value = String(id)
        ruleForm.joinId = joinId.value
        messagedate.joinId = joinId.value
        bindupdate.joinId = joinId.value
        binddelete.joinId = joinId.value
        operationItem.joinId = joinId.value
        addParams.joinId = joinId.value
      }
    } catch (e) {
      // 登录态失效等由请求统一拦截器提示，这里保持空值，后续提交会给出明确提示
    }
  }
  // 先恢复本机草稿，再发起后端异步回填；回填侧会跳过草稿已填字段
  restoreDraft()
  fetchData()
  fetchquery()
  handleChackFee()
  getCountry()
  loadPasswordConfig()
})


function getCountry() {
      queryCountryAndCurrency().then((res) => {
        if (res.code == 1) {
          countryOptions.value = res.data.baseCountryEntities
          // 草稿里已有用户选过的区号时保留，别被默认国家覆盖
          ruleForm.internationalDialingCode =
            (draftRestored.value && ruleForm.internationalDialingCode) ||
            res.data.baseCountryEntities[0].internationalDialingCode
        }
      })
}

function loadPasswordConfig() {
  queryPasswordConfig().then((res) => {
    if (res.code == 1 && res.data) {
      passwordConfig.passwordFormat = res.data.passwordFormat
      passwordConfig.passwordLength = res.data.passwordLength
      passwordConfig.desc = res.data.desc || ''
    }
  })
}

// 提交成功后移除 origin，隐藏密码字段
function clearOriginAfterSubmit() {
  if (originType.value != 1 && !route.query.origin) {
    return
  }
  originType.value = 0
  ruleForm.settlePW = ''
  const query = { ...route.query }
  delete query.origin
  router.replace({ path: route.path, query })
}

//店铺等级改变
function handleLevChange(val) {
      if (val == 4) {
        shopType.value = 3
      } else {
        shopType.value = 1
      }
}

// 检验公司名称是否重复
function handleCheckCompanyName(enterpriseName) {
      // 只有用户输入店铺名称再进行判断，不对空值进行判断
      if (enterpriseName) {
        formshopName.enterpriseName = ruleForm.enterpriseName
        companyNamecheck(formshopName).then((res) => {
          if (res.data == 1) {
            ElMessage.error('该公司名称已存在！请重新输入！')
            ruleForm.enterpriseName = ''
          }
        })
      }
}

//长期有效
    // btnCheckBox(ev) {
    //   checked.value = !checked.value
    // },
    //上传图片
function handleImageChange1(src, key) {
      imgUrl1.value = src
}

function handleImageChange2(src, key) {
      ruleForm.idCardFront = src
}

function handleImageChange4(src, key) {
      ruleForm.idCardBack = src
}

//重新编辑
function reset() {
      disabled.value = false
      btnreset.value = false
      // 审核失败后重新编辑，继续自动暂存这一轮改动
      draftEnabled = true
}

// 删除图片
function shanchutupian1() {
      ElMessageBox.confirm('是否确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          imgUrl1.value = ''
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '已取消删除',
          })
        })
}

function shanchutupian2() {
      ElMessageBox.confirm('是否确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          ruleForm.idCardFront = ''
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '已取消删除',
          })
        })
}

function shanchutupian3() {
      ElMessageBox.confirm('是否确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          ruleForm.idCardBack = ''
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '已取消删除',
          })
        })
}

//合作协议
function handleImageChange3(src, key, name) {
      ruleForm.corporateAgreement = key
      ruleForm.agreementFileName = name
}

//删除
function handleImageChangetow() {
      ruleForm.agreementFileName = ''
}

function fetchData() {
      //是否是企业用户
      businessUserisBusiness().then((res) => {
        if (res.code == 1 && res.data) {
          editNo.value = true
          applyServerField('taxId', res.data.taxId)
          applyServerField('enterpriseName', res.data.companyName)
          if (!draftRestored.value || !imgUrl1.value) {
            imgUrl1.value = res.data.businessLicense
          }
        }
      }).catch(() => {})
      // 初始地址由 AreaCascader 组件内共享缓存加载
      // 查询入驻合作协议
      queryLimitGroup(agreement).then((res) => {
        tableData2.url = res.data.url
      }).catch(() => {})
      // 查询入驻品牌协议
      queryLimitGroup(agreementtow).then((res) => {
        tableData2.url2 = res.data.url
      }).catch(() => {})
      // 查询是否新号 是否填写信息
      joinagree().then((res) => {
        if (res.code == 1) {
          if (res.data.isExist == 1) {
            handleshopinfoquery()
          }
        }
      }).catch(() => {})
      // 店铺分类
      classquerylist(queryshopType.value).then((res) => {
        for (var i = 0; i < res.data.list.length; i++) {
          optionsClass.value.push({
            label: res.data.list[i].shopClassName,
            shopClass: res.data.list[i].shopClassId,
            shopType: res.data.list[i].shopType,
          })
        }
        for (var i = 0; i < optionsClass.value.length; i++) {
          if (optionsClass.value[i].shopType == 3) {
            optionsClass.value[i].label = optionsClass.value[i].label + '★'
          }
        }
      }).catch(() => {})
}

// 基本信息查询
function handleshopinfoquery() {
      shopinfoquery().then((res) => {
        if (res.code == 1) {
          if (res.data == null) {
            auditStatus.value = null
          } else if (res.data != null) {
            if (res.data.auditStatus == 0 || res.data.auditStatus == 2) {
              // 待审核0   审核失败2
              disabled.value = true
              btnreset.value = true
              auditStatus.value = res.data.auditStatus //审核状态
              if (!draftRestored.value || !imgUrl1.value) {
                imgUrl1.value = res.data.businessLicencePic //营业执照照片
              }
              auditComment.value = res.data.auditComment //平台审核失败原因
              // 有本机草稿时保留用户尚未提交的录入，只补草稿里没有的字段
              applyServerData(res.data) //基本信息提交完成返回信息
              applyServerField('shopLocation', res.data.shopLocation) //地址回显
              applyServerField('blLocation', res.data.blLocation)
              applyServerField('bankLocation', res.data.bankAddress)
              applyServerField('agreementFileName', res.data.agreementFileName) //合作协议
              applyServerField('validateLongTerm', res.data.validateLongTerm) //是否长期有效 回显
              if (!draftRestored.value && ruleForm.validateLongTerm == 1) {
                checked.value = !checked.value
              }
            } else if (res.data.auditStatus == 1) {
              router.push({
                path: '/join/step6',
                query: {
                  joinId: joinId.value,
                  shopType: ruleForm.shopType,
                },
              })
            }
          }
        }
      })
}

// 删除
function handleDelete(row) {
      binddelete.productClassId = row.productClassId
      binddelete.joinId = joinId.value
      ElMessageBox.confirm('是否确定删除?', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      })
        .then(() => {
          classbinddelete(binddelete).then((res) => {
            if (res.code == 1) {
              dropConfirmedCatRows([row.productClassId])
              ElNotification({
                title: '提示',
                message: '该类目已删除',
                type: 'success',
              })
              var lastTotal = total.value - 1
              var totalPage = Math.ceil(lastTotal / messagedate.pageSize)
              if (totalPage < messagedate.currentPage) {
                messagedate.currentPage = totalPage || 1
              }
              fetchquery()
            } else {
              ElMessage({
                type: 'info',
                message: res.message,
              })
            }
          })
        })
        .catch(() => {
          ElMessage({
            type: 'info',
            message: '已取消删除',
          })
        })
}

// 多选
function handleSelectionChange(rows) {
      productClassIds.value = []
      if (rows) {
        rows.forEach((row) => {
          if (row) {
            productClassIds.value.push(row.productClassId)
          }
        })
      }
}

//批量删除
function operationCommonState() {
		const selectedRows = categoryTableEl.value?.getSelectionRows?.() || []
		productClassIds.value = selectedRows
			.map((row) => row && row.productClassId)
			.filter((id) => id)
      if (productClassIds.value.length > 0) {
        operationItem.joinId = joinId.value
        operationItem.productClassIds = productClassIds.value
        ElMessageBox.confirm('是否确定删除?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning',
        })
          .then(() => {
            operationCommon(operationItem).then((res) => {
              dropConfirmedCatRows([...productClassIds.value])
              ElNotification({
                title: '提示',
                message: '类目已删除',
                type: 'success',
              })
              var lastTotal = total.value - productClassIds.value.length
              var totalPage = Math.ceil(lastTotal / messagedate.pageSize)
              if (totalPage < messagedate.currentPage) {
                messagedate.currentPage = totalPage
              }
              fetchquery()
            })
          })
          .catch(() => {
            ElMessage({
              type: 'info',
              message: '已取消删除',
            })
          })
      } else {
        ElMessage({
          message: '请选择要操作的类目！',
          type: 'warning',
        })
      }
}

//查看店铺收费标准
function fetchgridData() {
      queryexpense(queryshopType.value).then((res) => {
        gridData.value = res.data
        gridData.value.forEach((item) => {
          if (item.shopType == 3) {
            item.shopClassName = item.shopClassName + '★'
          }
        })
      })
}

async function handleChackFee() {
      try {
        let res = await querySelectLevelByShop({ levelType: 1 })
        if (res && res.code == 1) {
          storeLevelData.value = res.data
        }
      } catch (e) {
        // 未登录/接口异常时不阻断页面，等级下拉保持为空
      }
}

// 添加类目数据展示
function fetchquery() {
      messagedate.joinId = joinId.value
      messagequery(messagedate).then((res) => {
        // 接口异常（如未登录返回 code=0、data=[]）时 data 里没有 classBindList，
        // 原写法直接 .length 会抛 Cannot read properties of undefined，整页红字。
        const list = Array.isArray(res?.data?.classBindList) ? res.data.classBindList : []
        const totalCount = Number(res?.data?.totalCount ?? 0) || 0
        // 服务端暂时查不到（登录态失效、绑定落库延迟）不等于用户没选类目：
        // 以本地已确认类目回显；服务端有数据时以其为准并同步进本地事实源。
        tableData.value = list.length ? list : [...confirmedCatRows.value]
        total.value = list.length ? totalCount : tableData.value.length
        if (list.length) mergeConfirmedCatRows(list)
		productClassIds.value = []
		nextTick(() => {
			categoryTableEl.value?.clearSelection?.()
		})
        // validateField 在字段校验失败时会 reject 字段对象，这里只是刷新校验态，
        // 失败属正常（类目未选），必须 catch 掉，否则变成 Uncaught (in promise) 抛到控制台。
        ruleFormEl.value?.validateField('productClassId').catch(() => {})
        syncCategoryView()
      }).catch(() => {
        // 登录态失效 / 接口异常时不再清空已确认类目，只以本地事实源回显
        tableData.value = [...confirmedCatRows.value]
        total.value = tableData.value.length
        productClassIds.value = []
        syncCategoryView()
      })
}

// 时间
function deleteTime() {
      ruleForm.validateStartTime = dateFormat(
        ruleForm.validateStartTime.toLocaleDateString() + ' 00:00:00',
        'yyyy/MM/dd HH:mm:ss'
      )
}

// 时间
function deleteTimetwo() {
      ruleForm.validateEndTime = dateFormat(
        ruleForm.validateEndTime.toLocaleDateString() + ' 23:59:59',
        'yyyy/MM/dd HH:mm:ss'
      )
}

// 验证码：请求失败必须恢复按钮，只有发送成功后才进入倒计时。
async function handleGetCode() {
  if (!canClick.value) return
  const account = String(ruleForm.contactPhone || '').trim()
  if (!account) {
    ElMessage.warning('请输入手机号或邮箱')
    return
  }
  ruleForm.contactPhone = account
  formshopName.userMobile = addMobile.userMobile = account
  addMobile.interDialingCode = ruleForm.internationalDialingCode
  canClick.value = false
  let sent = false
  try {
    const res0 = await phonecheck(formshopName)
    if (res0.data == 1 && originType.value == 1) {
      try {
        await ElMessageBox.confirm(
          '当前手机号 / 邮箱已经注册成功, 是否去登录?',
          '温馨提示',
          { confirmButtonText: '去登录', cancelButtonText: '重新填写', type: 'warning' }
        )
        safeOpenUrl(baseUrl + '/bcenter/#/login', { source: 'web' })
      } catch (_) {
        // 取消登录提示后允许重新填写并发送。
      }
      return
    }
    if (!(res0.data == 1 || (res0.data == 0 && originType.value == 1))) {
      ElMessage.error('请填写平台已注册手机号 / 邮箱，认证时平台将通过该渠道联系您。')
      return
    }
    const res = await sendPhoneCode(addMobile)
    if (res.code != 1) {
      ElMessage.error(res.message || '验证码发送失败，请重试')
      return
    }
    sent = true
    totalTime.value = 60
    content.value = totalTime.value + 's后重新发送'
    codeClock = window.setInterval(() => {
      totalTime.value--
      if (totalTime.value <= 0) {
        window.clearInterval(codeClock)
        codeClock = null
        content.value = '重新发送验证码'
        totalTime.value = 60
        canClick.value = true
      } else {
        content.value = totalTime.value + 's后重新发送'
      }
    }, 1000)
  } catch (_) {
    // 请求通道已经展示业务/网络错误；这里只恢复交互，避免未处理的 Promise 拒绝。
  } finally {
    if (!sent) canClick.value = true
  }
}

let codeClock = null
onBeforeUnmount(() => {
  if (codeClock !== null) window.clearInterval(codeClock)
  window.removeEventListener('beforeunload', flushDraft)
  // 离开页面（含上一步、登录跳转）时立即落盘，不等防抖
  flushDraft()
})

//店铺所在地址
function handleChanges(val) {
      if (val) {
        ruleForm.tempArray[2] = val[2] //区
        ruleForm.shopArea = ruleForm.tempArray[2] //区
      }
}

function handleChangesthree() {
      if (ruleForm.shopClass) {
        for (var i = 0; i < optionsClass.value.length; i++) {
          if (ruleForm.shopClass == optionsClass.value[i].shopClass) {
            ruleForm.shopType = optionsClass.value[i].shopType
            // shopType.value = optionsClass.value[i].shopType
          }
        }
      }
}

/** 店铺所在地展开路径时同步省、市编码（子数据由 lazyLoad 加载） */
function handleItemChange(val) {
      if (!val || !val.length) return
      ruleForm.tempArray = val
      ruleForm.shopProvince = val[0]
      ruleForm.shopCity = val[1]
}

//营业执照所在地
function handleChangestow(val) {
      if (val) {
        ruleForm.tempArraytow[2] = val[2] //区
        ruleForm.blArea = ruleForm.tempArraytow[2] //区
      }
}

/** 营业执照所在地展开路径时同步省、市编码 */
function handleItemChangetow(val) {
      if (!val || !val.length) return
      ruleForm.tempArraytow = val
      ruleForm.blProvince = val[0]
      ruleForm.blCity = val[1]
}

/** 开户银行所在地展开路径时同步省、市编码 */
function handleItemChange1(val) {
      if (!val || !val.length) return
      ruleForm.tempArray1 = val
      ruleForm.bankProvince = val[0]
      ruleForm.bankCity = val[1]
}

function handleChanges1(val) {
      if (val && val.length > 2) {
        ruleForm.tempArray1[2] = val[2]
        ruleForm.bankArea = ruleForm.tempArray1[2]
      }
}

// 提交经营类目
function handleConfirm() {
      bindupdate.joinId = joinId.value
      // 每次提交前清空，避免多次点「确认提交」把上一轮类目重复累加
      bindupdate.productClassId = []
      const selected = Array.isArray(catName.value) ? catName.value : []
      if (!selected.length) {
        ElMessage({ message: '请先选择经营类目', type: 'warning' })
        return
      }
      if (!joinId.value) {
        ElMessage({ message: '入驻信息未初始化，请刷新页面或重新登录后再添加类目', type: 'warning' })
        return
      }
      for (const item of selected) {
        const leaf = item?.[2] ?? item?.[item.length - 1]
        if (leaf != null) bindupdate.productClassId.push(leaf)
      }
      if (!bindupdate.productClassId.length) {
        ElMessage({ message: '请选择到最末级经营类目', type: 'warning' })
        return
      }
      ruleForm.productClassId = bindupdate.productClassId
      const confirmedRows = catRowsFromPaths(selected)
      queryGoodsCats(bindupdate)
        .then((res) => {
          if (res?.code == 1) {
            // 绑定成功后立即把类目写入主营类目表格，不等列表查询；
            // 后续列表查询即使为空也不会把已选类目抹掉。
            mergeConfirmedCatRows(confirmedRows)
            syncCategoryView()
            ElNotification({
              title: '成功',
              message: '添加成功',
              type: 'success',
            })
            catName.value = []
            fetchquery()
            addDialogVisible.value = false
          } else {
            // 失败时保留弹窗与已选类目，避免「点了没反应、列表还是空」
            ElNotification({
              message: res?.message || '添加失败，请稍后重试',
              type: 'error',
            })
            fetchquery()
          }
        })
        .catch(() => {
          ElNotification({
            message: '添加失败，请检查登录状态或稍后重试',
            type: 'error',
          })
        })
}

function changeCat() {
      // 多选级联的每一项是完整路径数组，这里只记录末级类目 id
      const list = Array.isArray(catName.value) ? catName.value : []
      const last = list[list.length - 1]
      addParams.productClassId = Array.isArray(last) ? last[last.length - 1] : (last ?? '')
}

//点击经营类目
async function handleAdd() {
      optionsone.value = []
      addParams.productClassId = ''
      catName.value = []
      addDialogVisible.value = true
      catLoading.value = true
      try {
        const res = await catquery(shopId.value)
        optionsone.value = Array.isArray(res?.data) ? res.data : []
        if (optionsone.value.length === 0) {
          ElMessage({ message: '经营类目数据为空，请稍后重试', type: 'warning' })
        }
        fetchquery()
      } catch (e) {
        optionsone.value = []
        ElMessage({ message: '经营类目加载失败，请检查登录状态或稍后重试', type: 'error' })
      } finally {
        catLoading.value = false
      }
}

// 分页
function handleSizeChange(val) {
      messagedate.pageSize = val
      fetchquery()
}

// 分页
function handleCurrentChange(val) {
      messagedate.currentPage = val
      fetchquery()
}

function back() {
      router.back()
}

// 提交
function next(formName) {
      // 个人入驻(shopType=0)时营业执照所在地整块 v-if 不渲染，addsEl.value 为 null；
      // AreaCascader 也可能在懒加载前返回空值。统一取一次并做空值兜底，避免提交时抛
      // TypeError: Cannot read properties of null (reading 'getCheckedNodes')。
      const blNodes = addsEl.value?.getCheckedNodes?.() ?? []
      if (blNodes.length > 0) {
        ruleForm.regAddress = (blNodes[0]?.pathLabels ?? []).toString().replace(/,/g, '')
      }

      const bankNodes = addsrEl.value?.getCheckedNodes?.() ?? []
      if (bankNodes.length > 0) {
        ruleForm.bankAddress = (bankNodes[0]?.pathLabels ?? []).toString().replace(/,/g, '')
      }
      // if (ruleForm.shopClass) {
      // 	for (var i = 0; i < optionsClass.value.length; i++) {
      // 		if (ruleForm.shopClass == optionsClass.value[i].shopClass) {
      // 			ruleForm.shopType = optionsClass.value[i].shopType
      // 		}
      // 	}
      // }
      if (!isPersonal.value) {
        if (checked.value) {
          ruleForm.validateLongTerm = 1
        } else if (!checked.value) {
          ruleForm.validateLongTerm = 0
        }
        if (ruleForm.validateLongTerm == 0) {
          if (ruleForm.validateEndTime == '') {
            ElMessage({
              message: '证件有效期未填写完成，请继续填写',
              type: 'warning',
            })
            return false
          }
          if (ruleForm.validateEndTime == null) {
            ElMessage({
              message: '证件有效期未填写完成，请继续填写',
              type: 'warning',
            })
            return false
          }
        }
        if (ruleForm.validateLongTerm == 1) {
          if (ruleForm.validateEndTime) {
            ElMessage({
              message: '证件有效期结束与长期只能选择一个，请重新填写',
              type: 'warning',
            })
            ruleForm.validateEndTime = ''
            checked.value = !checked.value
            return false
          }
        }
      }
      if (ruleForm.corporateAgreement == '') {
        ElMessage({
          message: '请上传合作协议',
          type: 'warning',
        })
        return false
      }
      // 类目状态以「已回显的表格行 + 本地已确认行」为准，不再依赖列表接口的异步哨兵
      ruleForm.productClassId = hasSelectedCategories() ? 1 : ''
      if (!hasSelectedCategories()) {
        ElMessage({
          message: pendingCatCount.value > 0
            ? '已选经营类目尚未保存，请在弹窗中点击「确认提交」'
            : '请选择经营类目',
          type: 'warning',
        })
        return false
      }
      if (ruleForm.enterpriseNature == 1) {
        if (ruleForm.enterpriseName.includes('公司')) {
          ElMessage({
            message: '公司名称中存在"公司"时，企业性质不可选择个体工商户！ ',
            type: 'warning',
          })
          return false
        }
      }
      // 类目校验已在上方按统一事实源处理，此处不再重复判定

      ruleForm.businessLicencePic = imgUrl1.value //营业执照
      ruleForm.shopArea = ruleForm.tempArray[2] //区
      ruleForm.blArea = ruleForm.tempArraytow[2] //区
      ruleForm.bankArea = ruleForm.tempArray1[2] //区
      ruleFormEl.value.validate((valid) => {
        if (valid) {
          submitLoading.value = true
          joininserttow(ruleForm)
            .then((res) => {
              if (res.code === 1) {
                res.data &&  res.data?.token ? setToken(res.data?.token) : '';
                // 已成功提交，本机暂存立即作废，避免下次打开又回填旧草稿
                discardDraftAfterSubmit()
                ElMessage({
                  message: '提交成功',
                  type: 'success',
                })
                clearOriginAfterSubmit()
                fetchData()
                fetchquery()
                handleshopinfoquery()
              } else {
                ElMessage({
                  message: res.message,
                  type: 'warning',
                })
                return
              }
            })
            .catch(() => {
              ElMessage({
                message: '提交失败，请检查登录状态或稍后重试',
                type: 'error',
              })
            })
            .finally(() => {
              submitLoading.value = false
            })
        } else {
          ElMessage({
            message: '请检查表单内容！',
            type: 'warning',
          })
          return false
        }
      })
}

// 修改
function nexttow(formName) {
      if (ruleForm.shopClass) {
        for (var i = 0; i < optionsClass.value.length; i++) {
          if (ruleForm.shopClass == optionsClass.value[i].shopClass) {
            ruleForm.shopType = optionsClass.value[i].shopType
          }
        }
      }
      if (fileName.value) {
        ruleForm.agreementFileName = fileName.value
      }
      if (!isPersonal.value) {
        if (checked.value) {
          ruleForm.validateLongTerm = 1
        } else if (!checked.value) {
          ruleForm.validateLongTerm = 0
        }
        if (ruleForm.validateLongTerm == 0) {
          if (ruleForm.validateEndTime == '') {
            ElMessage({
              message: '证件有效期未填写完成，请继续填写',
              type: 'warning',
            })
            return false
          }
          if (ruleForm.validateEndTime == null) {
            ElMessage({
              message: '证件有效期未填写完成，请继续填写',
              type: 'warning',
            })
            return false
          }
        }
        if (ruleForm.validateLongTerm == 1) {
          if (ruleForm.validateEndTime) {
            ElMessage({
              message: '证件有效期结束与长期只能选择一个，请重新填写',
              type: 'warning',
            })
            ruleForm.validateEndTime = ''
            checked.value = !checked.value
            return false
          }
        }
      }
      if (ruleForm.corporateAgreement == '') {
        ElMessage({
          message: '请上传合作协议',
          type: 'warning',
        })
        return false
      }
      // 类目状态以「已回显的表格行 + 本地已确认行」为准，不再依赖列表接口的异步哨兵
      ruleForm.productClassId = hasSelectedCategories() ? 1 : ''
      if (!hasSelectedCategories()) {
        ElMessage({
          message: pendingCatCount.value > 0
            ? '已选经营类目尚未保存，请在弹窗中点击「确认提交」'
            : '请选择经营类目',
          type: 'warning',
        })
        return false
      }
      // 类目校验已在上方按统一事实源处理，此处不再重复判定

      ruleForm.businessLicencePic = imgUrl1.value //营业执照
      ruleFormEl.value.validate((valid) => {
        if (valid) {
          submitLoading.value = true
          joininserttowupdate(ruleForm)
            .then((res) => {
              if (res.code === 1) {
                discardDraftAfterSubmit()
                ElMessage({
                  message: '提交成功',
                  type: 'warning',
                })
                clearOriginAfterSubmit()
                fetchData()
                fetchquery()
                handleshopinfoquery()
              } else {
                ElMessage({
                  message: res.message,
                  type: 'warning',
                })
                return
              }
            })
            .finally(() => {
              submitLoading.value = false
            })
        } else {
          ElMessage({
            message: '请检查表单内容！',
            type: 'warning',
          })
          return false
        }
      })
    }
</script>

<style lang="scss" scoped>
.join-code-btn {
  display: inline-flex;
  align-items: center;
  height: 22px;
  margin-left: 8px;
  padding: 0 2px 0 10px;
  border: 0;
  border-left: 1px solid #eceaf3;
  background: transparent;
  color: var(--brand-500, #ef4f55);
  font-size: 13px;
  font-weight: 500;
  line-height: 1;
  white-space: nowrap;
  cursor: pointer;
  transition: opacity 0.2s;

  &:hover:not(:disabled) {
    opacity: 0.72;
  }

  &:disabled {
    color: #b9bcc4;
    cursor: not-allowed;
  }
}


.join-step-title {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.join-note-icon {
  margin-top: -10px;
  color: var(--neutral-400, #b3b7c2);
  font-size: 15px;
  cursor: help;
  transition: color 0.2s;

  &:hover {
    color: var(--brand-500, #ef4f55);
  }
}

.flexrow {
  background-color: rgba(247, 248, 250, 1);
  border-radius: 1px;
  width: 1136px;
  height: 50px;
  margin-top: 30px;
}
.flexcol {
  background-color: rgba(219, 44, 44, 1);
  width: 3px;
  height: 10px;
  margin: 20px 0 0 24px;
}
.textwrap {
  width: 96px;
  height: 16px;
  color: rgba(51, 51, 51, 1);
  font-size: 16px;
  text-align: left;
  line-height: 16px;
  margin: 17px 0 0 8px;
}
.textwrapperone {
  width: 55px;
  height: 17px;
  font-size: 0;
  text-align: right;
  line-height: 17px;
  margin-left: 30px;
  margin-top: 15px;
}
.textword {
  width: 55px;
  height: 17px;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.texttwo {
  width: 55px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.demo-ruleForm {
  margin-top: 50px;
}
.imagetextthree {
  width: 150px;
  margin-left: 50px;
}
.box_29 {
  background-color: rgba(255, 255, 255, 1);
  border-radius: 2px;
  height: 16px;
  border: 1px solid rgba(220, 222, 224, 1);
  margin-top: 2px;
  width: 16px;
}
.textgroup {
  width: 56px;
  height: 20px;
  color: rgba(51, 51, 51, 1);
  font-size: 14px;
  text-align: left;
  line-height: 20px;
  margin-left: 15px;
}
.text_17 {
	display:block;
  margin-top: 10px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.textdianpushoufei {
  /*// width: 340px;*/
  height: 40px;
  font-size: 14px;
  font-weight: 400;
  color: #333333;
  line-height: 40px;
  margin-left: 12px;
}
.dianpushoufei {
  color: #007aff;
  font-weight: 400;
  margin-left: 8px;
  cursor: pointer;
}
.shtextwrapper {
  background-color: rgba(219, 44, 44, 1);
  border-radius: 0px 0px 2px 2px;
  height: 32px;
  width: 180px;
  margin-top: 60px;
  margin-left: 30px;
}
.text_39 {
  width: 60px;
  height: 32px;
  color: #fff;
  font-size: 12px;
  text-align: left;
  line-height: 32px;
  margin: 0 0 0 60px;
  display: inline-block;
  vertical-align: top;
}
.block {
  margin-left: 40px;
}
.text_41 {
  width: 460px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_42 {
  width: 460px;
  height: 17px;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_43 {
  width: 460px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_44 {
  width: 460px;
  height: 17px;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.group_28 {
  border-radius: 2px;
  width: 102px;
  height: 40px;
  border: 1px solid rgba(219, 44, 44, 1);
  margin-left: 8px;
  justify-content: flex-center;
  cursor: pointer;
}
.thumbnail_1 {
  width: 12px;
  height: 12px;
  margin: 10px 0 0 14px;
}
.text_71 {
  width: 56px;
  height: 20px;
  color: rgba(219, 44, 44, 1);
  font-size: 14px;
  text-align: left;
  line-height: 20px;
  margin: 5px 0 0 8px;
}
.image-text_2 {
  width: 150px;
  height: 20px;
  margin: -30px 0 0 880px;
  cursor: pointer;
}
.group_30 {
  width: 13px;
  height: 14px;
  background: url(../../assets/images/join/shanchu.png) 100% no-repeat;
  background-size: 100% 100%;
  margin-top: 4px;
}
.text-group_2 {
  width: 100px;
  height: 20px;
  color: rgba(219, 44, 44, 1);
  font-size: 14px;
  text-align: left;
  line-height: 20px;
}
.mgt20 {
  margin-top: 20px;
  text-align: center;
}
.img-size {
  display: inline-block;
  width: 160px;
  height: 160px;
}
.relative {
  position: relative;
}
.absolute {
  position: absolute;
}
.top0 {
  top: 0;
}
.text-group_55 {
  width: 566px;
  margin-top: -20px;
}
.text_55 {
  width: 420px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text-wrapper_11 {
  width: 566px;
  height: 17px;
  font-size: 0;
  text-align: left;
  line-height: 17px;
  margin-top: 8px;
}
.text_40 {
  width: 566px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_41 {
  width: 566px;
  height: 17px;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_42 {
  width: 566px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_43 {
  width: 566px;
  height: 17px;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_44 {
  width: 566px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_45 {
  width: 566px;
  height: 17px;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_75 {
  width: 369px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_76 {
  width: 369px;
  height: 17px;
  color: rgba(219, 44, 44, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.text_77 {
  width: 369px;
  height: 17px;
  color: rgba(153, 153, 153, 1);
  font-size: 12px;
  text-align: left;
  line-height: 17px;
}
.textwrapperfivr {
  width: 566px;
  height: 17px;
  text-align: left;
  line-height: 17px;
}
.button2 {
  width: 180px;
  height: 40px;
  border: 0;
  background: #ddd;
  border-radius: 0;
  line-height: 1;
  font-size: 12px;
  cursor: pointer;
  outline: none;
  border: 1px solid #ccc;
  padding: 7px 15px;
  margin-top: 60px;
  margin-left: 30px;
}
.downloadFile {
  color: #3764ff;
  cursor: pointer;
}
.detail-agreements {
  display: inline-block;
  position: absolute;
  left: 100px;
  color: #777;
}
.el-upload {
  text-align: inherit;
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
.wd370{
	width:370px;
}

.mrf10{
	margin-right:-10px;
}
:deep(.tl .el-input__inner){
	text-align:left !important;
}

.settle-bottom-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  min-height: 40px;
  padding: 0 8px 24px;
  box-sizing: border-box;
}

.settle-bottom-status {
  justify-content: flex-start;
  gap: 20px;
  text-align: left;
}

.settle-bottom-fail {
  justify-content: space-between;
  gap: 24px;
  text-align: left;
}

.status-badge {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  line-height: 1.4;
  white-space: nowrap;

  &::before {
    content: '';
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }

  &.is-pending {
    color: #d48806;

    &::before {
      background: #faad14;
      box-shadow: 0 0 0 3px rgba(250, 173, 20, 0.2);
    }
  }

  &.is-fail {
    color: #db2c2c;

    &::before {
      background: #db2c2c;
      box-shadow: 0 0 0 3px rgba(219, 44, 44, 0.15);
    }
  }
}

.status-tip {
  margin: 0;
  flex: 1;
  min-width: 0;
  color: #666;
  font-size: 14px;
  line-height: 1.6;
  text-align: left;
}

.fail-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.fail-reason {
  margin: 0;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  font-size: 14px;
  line-height: 1.6;
}

.fail-reason-label {
  flex-shrink: 0;
  color: #999;

  &::after {
    content: '：';
  }
}

.fail-reason-text {
  color: #333;
  word-break: break-all;
}

.fail-actions {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 12px;
}

/* ────────────────────────────────────────────────────────────────
   紧凑版式（2026-11-05）
   原页面是设计稿转的绝对像素布局：每个表单项固定 width:550px 竖排、
   分区标题 margin-top:50px、图片 160px，整页要滚很久。
   这里统一改成两列栅格 + 收紧间距，字段一个不删；逻辑与校验不动。
   ──────────────────────────────────────────────────────────────── */

.join-steps {
  margin-top: 20px;
  padding: 18px 20px;
  border-radius: var(--radius-lg, 12px);
  background: var(--neutral-50, #fafafa);

  :deep(.el-step__title) {
    font-size: 13px;
    line-height: 1.5;
  }

  :deep(.el-step__icon) {
    width: 24px;
    height: 24px;
    font-size: 12px;
  }
}

.demo-ruleForm {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  column-gap: 36px;
  row-gap: 0;
  align-items: start;
  margin-top: 20px;

  /* 两列：短字段并排，避免一屏只有三行 */
  :deep(.el-form-item) {
    width: auto !important;
    margin: 0 0 10px !important;
    grid-column: span 1;
  }

  /* 整行：图片上传、日期区间、长说明、表格、协议 */
  :deep(.fi-full) {
    grid-column: 1 / -1;
  }

  :deep(.el-form-item__label) {
    padding-right: 8px;
    color: var(--neutral-700, #4b5563);
    font-size: 12.5px;
    line-height: 32px;
    white-space: nowrap;
  }

  :deep(.el-form-item__content) {
    min-width: 0;
    line-height: 32px;
  }

  /* 输入类统一撑满列宽，压掉 wd370 / 内联 550px */
  :deep(.el-input),
  :deep(.el-input-number),
  :deep(.el-select),
  :deep(.el-date-editor.el-input),
  :deep(.el-cascader) {
    width: 100% !important;
  }

  :deep(.wd370),
  :deep(.wd80) {
    width: 100% !important;
  }

  :deep(.el-form-item__error) {
    position: static;
    padding-top: 2px;
  }

  /* 「员工总数 人」「注册资金 万」这类输入框 + 单位的组合保持同行 */
  :deep(.el-form-item:has(.el-input-number) .el-form-item__content) {
    display: flex;
    align-items: center;
    gap: 8px;
  }

  :deep(.el-form-item:has(.el-input-number) .el-input-number) {
    flex: 1 1 auto;
  }
}

/* 分区小标题：整行通栏、34px 高、左侧红条 */
.demo-ruleForm :deep(.flexrow) {
  grid-column: 1 / -1;
  display: flex;
  align-items: center;
  width: auto !important;
  height: 34px;
  margin: 14px 0 10px !important;
  padding: 0 12px;
  border-radius: 8px;
  background: var(--neutral-50, #f7f8fa);
}

.demo-ruleForm :deep(.flexcol) {
  width: 3px;
  height: 12px;
  margin: 0 8px 0 0;
  border-radius: 2px;
  background: var(--brand-500, #db2c2c);
}

.demo-ruleForm :deep(.textwrap) {
  width: auto;
  height: auto;
  margin: 0;
  color: var(--neutral-800, #1f2937);
  font-size: 14px;
  font-weight: 600;
  line-height: 34px;
}

.demo-ruleForm :deep(.textwrapperone) {
  display: flex;
  align-items: center;
  gap: 4px;
  width: auto;
  height: auto;
  margin: 0 0 0 auto;
  font-size: 0;
  line-height: 34px;
}

/* 提示文字：更小、更淡、贴住输入框 */
.demo-ruleForm :deep(.text_17) {
  margin-top: 2px;
  font-size: 12px;
  line-height: 1.55;
}

/* 上传图：160 → 120，图片与「上传图片」按钮上下堆叠 */
.demo-ruleForm :deep(.img-size) {
  width: 120px;
  height: 120px;
  border: 1px dashed var(--neutral-200, #e5e7eb);
  border-radius: 8px;
  object-fit: contain;
  background: var(--neutral-50, #fafafa);
}

.demo-ruleForm :deep(.fi-img .el-form-item__content) {
  display: block !important;
}

/* 上传按钮落到图片正下方：图片是 float，这里清浮动并取消左浮动 */
.demo-ruleForm :deep(.shtextwrapper) {
  float: none;
  clear: both;
  display: block;
  width: 120px;
  height: 28px;
  margin: 8px 0 0;
  border-radius: 6px;
}

.demo-ruleForm :deep(.text_39) {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  margin: 0;
  padding: 0;
  font-size: 12px;
  line-height: 1;
}

.demo-ruleForm :deep(.text-group_55) {
  width: auto;
  margin-top: 0;
}

.demo-ruleForm :deep(.text_55),
.demo-ruleForm :deep(.text-wrapper_11) {
  width: auto;
  font-size: 12px;
  line-height: 1.6;
}

/* 添加类目：按钮与批量删除同一行 */
.demo-ruleForm :deep(.group_28) {
  display: inline-flex;
  align-items: center;
  width: auto;
  height: 32px;
  padding: 0 12px;
  border-radius: 6px;
}

.demo-ruleForm :deep(.thumbnail_1) {
  margin: 0 4px 0 0;
}

.demo-ruleForm :deep(.text_71) {
  width: auto;
  height: auto;
  margin: 0;
  font-size: 13px;
  line-height: 1;
}

.demo-ruleForm :deep(.image-text_2) {
  float: right;
  width: auto;
  height: auto;
  margin: 6px 0 0;
}

/* 主营类目表格不再留 200px 空白 */
.demo-ruleForm :deep(.el-table) {
  margin: 12px 0 8px !important;
}

.demo-ruleForm :deep(.el-pagination) {
  padding: 0 !important;
  justify-content: flex-end;
}

/* 店铺等级：下拉独占一行，说明与权益入口移到下一行 */
.demo-ruleForm :deep(.textdianpushoufei) {
  display: block;
  height: auto;
  margin-left: 0;
  font-size: 12px;
  line-height: 1.7;
}

/* 底部操作栏吸底，长表单滚到底不用来回找按钮 */
.settle-bottom-operate {
  position: sticky;
  bottom: 0;
  z-index: 10;
  padding: 12px 0;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-top: 1px solid var(--neutral-100, #f0f0f0);
}

.settle-bottom-inner {
  padding-bottom: 0;
}

/* 主营类目区：已选 / 待确认数量提示 */
.join-cat-selected-hint,
.join-cat-pending-hint {
  display: inline-block;
  margin-left: 12px;
  font-size: 12px;
  line-height: 24px;
  color: #67c23a;
}

.join-cat-pending-hint {
  color: #e6a23c;
}
</style>

<style lang="scss">
/* Tooltip 内容由 Element Plus teleport 到 body，scoped 选不中，只能走全局层 */
.join-note-popper.el-popper {
  max-width: 380px;
  padding: 12px 14px;
  line-height: 1.7;

  .join-note-tip {
    strong {
      display: block;
      margin-bottom: 6px;
      font-size: 13px;
      color: #303133;
    }

    p {
      margin: 0;
      font-size: 12.5px;
      color: #606266;
    }
  }
}
</style>
