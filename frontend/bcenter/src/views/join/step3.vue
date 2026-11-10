<template>
  <div>
    <div class="wrap">
      <div ref="step1" class="settle-cont-box">
        <h3>企业店铺入驻流程</h3>
        <el-steps :active="2" align-center style="margin-top: 50px">
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
          label-width="180px"
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
                <button class="button fl mrf10" type="button" @click="handleGetCode">
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
            <div class="textwrap fl">公司信息</div>
            <div class="textwrapperone fl">
              <span class="textword">*</span>
              <span class="texttwo">为必填项</span>
            </div>
          </div>
          <el-form-item
            label="企业性质:"
            style="width: 550px; margin-top: 50px"
            prop="enterpriseNature"
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
          >
            <el-input
              v-model="ruleForm.enterpriseName"
              placeholder="请输入公司名称"
              @blur="handleCheckCompanyName(ruleForm.enterpriseName)"
            ></el-input>
            <span class="text_17">请按照营业执照上登记的完成名称填写</span>
          </el-form-item>
          <el-form-item label="税号:" style="width: 550px" prop="taxId">
            <el-input
              v-model="ruleForm.taxId"
              placeholder="请填写公司税号"
            ></el-input>
            <span class="text_17"
              >请按照营业执照上的统一社会信用代码进行填写</span
            >
          </el-form-item>
          <el-form-item label="营业执照图片" prop="businessLicencePic">
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
          <el-form-item style="width: 550px; margin-bottom: 40px">
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
          >
            <el-input
              v-model="ruleForm.blAddress"
              placeholder="请输入营业执照详细地址"
            ></el-input>
          </el-form-item>
          <el-form-item label="营业执照有效期:" prop="validateStartTime">
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
          >
            <el-input
              v-model="ruleForm.legalPersonIdCard"
              placeholder="请输入法人身份证"
            ></el-input>
          </el-form-item>
          <el-form-item
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
          <el-form-item label="店铺等级" prop="shopLevelId">
            <el-select
              v-model="ruleForm.shopLevelId"
              placeholder="请选择店铺等级"
              class="wd370"
              @change="handleLevChange"
            >
              <el-option
                v-for="item in storeLevelData"
                :key="item.id"
                :label="item.id == 4 ? '* ' + item.storeLevel : item.storeLevel"
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
                      <li>{{ item.storeLevel }}</li>
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
          <el-form-item label="合作协议" prop="corporateAgreement">
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
          <el-form-item style="margin-bottom: 40px; margin-top: 50px">
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
          <el-form-item
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
                <template v-slot="scope">{{ scope.row.catNames[0] }}</template>
              </el-table-column>
              <el-table-column label="二级类目" align="center">
                <template v-slot="scope">{{ scope.row.catNames[1] }}</template>
              </el-table-column>
              <el-table-column label="三级类目" align="center">
                <template v-slot="scope">{{ scope.row.catNames[2] }}</template>
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
        <el-button type="danger" :loading="submitLoading" @click="next('ruleForm')">提交入驻审核</el-button>
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
import { ref, reactive, computed, onMounted, nextTick, getCurrentInstance, useTemplateRef } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElNotification, ElMessageBox } from 'element-plus'
import {
  Document as ElIconDocument,
  Edit as ElIconEdit,
  Postcard as ElIconPostcard,
  DocumentChecked as ElIconDocumentChecked,
  Setting as ElIconSetting,
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
import { setToken } from '@/utils/joinAuth'

const { baseUrl, safeOpenUrl } = useAppConfig()
const router = useRouter()
const route = useRoute()

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
const agreement = reactive({ sign: 'GEN_COR_AGREEMENT' })
const agreementtow = reactive({ sign: 'BR_AGREEMENT' })
const auditStatus = ref(null)
const src = ref('')

const validatePass = (rule, value, callback) => {
  ruleForm.productClassId = selectProductIds.value
  if (tableData.value.length == 0) {
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

const rules = {
  contactName: [{ required: true, message: '请输入运营人员姓名', trigger: 'blur' }],
  businessLicencePic: [{ required: true, message: '请上传营业执照', trigger: 'blur' }],
  taxId: [{ required: true, message: '请输入公司税号', trigger: 'blur' }],
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
  blAddress: [{ required: true, message: '请输入营业执照详细地址', trigger: 'blur' }],
  idNo: [{ required: true, message: '请输入身份证号码', trigger: 'blur' }],
  validateStartTime: [{ required: true, message: '请选择证件有效期', trigger: 'change' }],
  shopName: [
    { required: true, message: '请输入店铺名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' },
  ],
  enterpriseName: [{ required: true, message: '请输入公司名称', trigger: 'blur' }],
  shopLocation: [{ required: true, trigger: 'change', message: '请选择地区' }],
  blLocation: [{ required: true, trigger: 'change', message: '请选择地区' }],
  shopAddress: [{ required: true, message: '请输入店铺详细地址', trigger: 'blur' }],
  shopClass: [{ required: true, message: '请输入店铺分类', trigger: 'blur' }],
  enterprisePhone: [{ required: true, message: '请输入企业电话', trigger: 'blur' }],
  enterpriseNature: [{ required: true, message: '请选择企业性质', trigger: 'change' }],
  legalPersonName: [{ required: true, message: '请输入法人姓名', trigger: 'blur' }],
  legalPersonMobile: [{ required: true, message: '请输入法人手机号', trigger: 'blur' }],
  legalPersonIdCard: [{ required: true, message: '请输入法人身份证', trigger: 'blur' }],
  idCardFront: [{ required: true, message: '请上传身份证正面', trigger: 'change' }],
  idCardBack: [{ required: true, message: '请上传身份证反面', trigger: 'change' }],
  bankName: [{ required: true, message: '请输入银行开户名', trigger: 'blur' }],
  bankCardNo: [{ required: true, message: '请输入银行卡号', trigger: 'blur' }],
  bankLocation: [{ required: true, message: '请输入开户银行所在地', trigger: 'change' }],
  bankBranchName: [{ required: true, message: '请输入开户银行支行名称', trigger: 'blur' }],
  branchId: [{ required: true, message: '请输入开户银行支行联行号', trigger: 'blur' }],
  productClassId: [{ required: true, validator: validatePass }],
  shopLevelId: [{ required: true, message: '请选择店铺等级', trigger: 'change' }],
  corporateAgreement: [{ required: true, message: '请上传合作协议', trigger: 'change' }],
}


onMounted(() => {
  joinId.value = route.query.joinId
  ruleForm.joinId = joinId.value
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
          ruleForm.internationalDialingCode =
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
          ruleForm.taxId = res.data.taxId
          imgUrl1.value = res.data.businessLicense
          ruleForm.enterpriseName = res.data.companyName
        }
      })
      // 初始地址由 AreaCascader 组件内共享缓存加载
      // 查询入驻合作协议
      queryLimitGroup(agreement).then((res) => {
        tableData2.url = res.data.url
      })
      // 查询入驻品牌协议
      queryLimitGroup(agreementtow).then((res) => {
        tableData2.url2 = res.data.url
      })
      // 查询是否新号 是否填写信息
      joinagree().then((res) => {
        if (res.code == 1) {
          if (res.data.isExist == 1) {
            handleshopinfoquery()
          }
        }
      })
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
      })
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
              imgUrl1.value = res.data.businessLicencePic //营业执照照片
              auditComment.value = res.data.auditComment //平台审核失败原因
              Object.assign(ruleForm, res.data) //基本信息提交完成返回信息
              ruleForm.shopLocation = res.data.shopLocation //地址回显
              ruleForm.blLocation = res.data.blLocation
              ruleForm.bankLocation = res.data.bankAddress
              ruleForm.agreementFileName = res.data.agreementFileName //合作协议
              ruleForm.validateLongTerm = res.data.validateLongTerm //是否长期有效 回显
              if (ruleForm.validateLongTerm == 1) {
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
      let res = await querySelectLevelByShop({ levelType: 1 })
      if (res && res.code == 1) {
        storeLevelData.value = res.data
      }
}

// 添加类目数据展示
function fetchquery() {
      messagedate.joinId = joinId.value
      messagequery(messagedate).then((res) => {
        tableData.value = res.data.classBindList
        total.value = res.data.totalCount
		productClassIds.value = []
		nextTick(() => {
			categoryTableEl.value?.clearSelection?.()
		})
        ruleFormEl.value?.validateField('productClassId')
        if (res.data.classBindList.length > 0) {
          productClassIdtow.value = 1
        } else {
          productClassIdtow.value = ''
        }
        selectProductIds.value = []
        if (res.data.totalCount > 0) {
          res.data.classBindList.forEach((item) => {
            selectProductIds.value.push(item.productClassId)
          })
        }
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
      if (catName.value.length) {
        for (var i = 0; i < catName.value.length; i++) {
          bindupdate.productClassId.push(catName.value[i][2])
        }
      }
      ruleForm.productClassId = bindupdate.productClassId
      queryGoodsCats(bindupdate).then((res) => {
        if (res.code == 1) {
          ElNotification({
            title: '成功',
            message: '添加成功',
            type: 'success',
          })
          fetchquery()
          addDialogVisible.value = false
        } else {
          ElNotification({
            message: res.message,
            type: 'error',
          })
          fetchquery()
          addDialogVisible.value = false
        }
      })
}

function changeCat(val) {
      addParams.productClassId = val[val.length - 1]
}

//点击经营类目
async function handleAdd() {
      optionsone.value = []
      ;(addParams.productClassId = ''), (catName.value = '')
      addDialogVisible.value = true
      catLoading.value = true
      try {
        const res = await catquery(shopId.value)
        optionsone.value = res.data
        fetchquery()
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
      if (
        addsEl.value.getCheckedNodes() &&
        addsEl.value.getCheckedNodes().length > 0
      ) {
        let data = addsEl.value.getCheckedNodes()[0].pathLabels.toString()
        //用for循环遍历获取的数据,并把逗号裁掉
        for (let i = 0; i < data.length; i++) {
          if (data[i] === ',') {
            data = data.replace(data[i], '')
          }
        }
        ruleForm.regAddress = data
      }

      if (
        addsrEl.value.getCheckedNodes() &&
        addsrEl.value.getCheckedNodes().length > 0
      ) {
        console.log(addsrEl.value.getCheckedNodes()[0])

        let data = addsrEl.value.getCheckedNodes()[0].pathLabels.toString()
        //用for循环遍历获取的数据,并把逗号裁掉
        for (let i = 0; i < data.length; i++) {
          if (data[i] === ',') {
            data = data.replace(data[i], '')
          }
        }
        ruleForm.bankAddress = data
      }
      // if (ruleForm.shopClass) {
      // 	for (var i = 0; i < optionsClass.value.length; i++) {
      // 		if (ruleForm.shopClass == optionsClass.value[i].shopClass) {
      // 			ruleForm.shopType = optionsClass.value[i].shopType
      // 		}
      // 	}
      // }
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
      if (ruleForm.corporateAgreement == '') {
        ElMessage({
          message: '请上传合作协议',
          type: 'warning',
        })
        return false
      }
      ruleForm.productClassId = productClassIdtow.value
      if (ruleForm.enterpriseNature == 1) {
        if (ruleForm.enterpriseName.includes('公司')) {
          ElMessage({
            message: '公司名称中存在"公司"时，企业性质不可选择个体工商户！ ',
            type: 'warning',
          })
          return false
        }
      }
      if (ruleForm.productClassId == '') {
        ElMessage({
          message: '请选择经营类目',
          type: 'warning',
        })
        return false
      }

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
      if (ruleForm.corporateAgreement == '') {
        ElMessage({
          message: '请上传合作协议',
          type: 'warning',
        })
        return false
      }
      ruleForm.productClassId = productClassIdtow.value
      if (ruleForm.productClassId == '') {
        ElMessage({
          message: '请选择经营类目',
          type: 'warning',
        })
        return false
      }

      ruleForm.businessLicencePic = imgUrl1.value //营业执照
      ruleFormEl.value.validate((valid) => {
        if (valid) {
          submitLoading.value = true
          joininserttowupdate(ruleForm)
            .then((res) => {
              if (res.code === 1) {
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
</style>
