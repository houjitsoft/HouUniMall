import {
    joinBase as baserequest,
    joinBusiness as businessrequest,
    joinUserms as usermsrequest,
    joinGoods as goodsrequest,
    joinThird as thirdpartyrequest,
    joinSupplier as mallSupplier
} from '@/utils/joinChannels'
import JSEncrypt from '@/utils/jsencrypt'
// 查询头部图片
export function configurationaddress(data) {
  return baserequest({
    url: '/web/index/image/configuration/address?configKey=setting_logo',
    method: 'post',
    data: {},
  })
}

// 删除类目
export function classbinddelete(data) {
  return businessrequest({
    url: '/shopBase/info/class/bind/delete',
    method: 'post',
    data: {
      shopId: data.shopId,
      productClassId: data.productClassId,
      joinId: data.joinId,
    },
  })
}

// 批量删除类目
export function operationCommon(data) {
  return businessrequest({
    url: '/shopBase/info/class/bind/deleteBatch',
    method: 'post',
    data: {
      productClassIds: data.productClassIds,
      joinId: data.joinId,
    },
  })
}

// 企业入驻信息提交
export function joininserttow(data) {
  return businessrequest({
    url: '/shop/joinBase/insert',
    method: 'post',
    data: {
      joinId: data.joinId,
      validateCode: data.validateCode,
      contactName: data.contactName,
      businessLicencePic: data.businessLicencePic,
      contactPhone: data.contactPhone,
      blAddress: data.blAddress,
      enterpriseName: data.enterpriseName,
      taxId: data.taxId,
      blProvince: data.blProvince,
      blCity: data.blCity,
      blArea: data.blArea,
      validateStartTime: data.validateStartTime,
      validateEndTime: data.validateEndTime,
      enterprisePhone: data.enterprisePhone,
      employeeNum: data.employeeNum,
      registerMoney: data.registerMoney,
      shopName: data.shopName,
      shopProvince: data.shopProvince,
      shopCity: data.shopCity,
      shopArea: data.shopArea,
      shopAddress: data.shopAddress,
      shopClass: data.shopClass,
      corporateAgreement: data.corporateAgreement,
      shopType: data.shopType,
      agreementFileName: data.agreementFileName,
      validateLongTerm: data.validateLongTerm,
      shopLevelId: data.shopLevelId,
      bankName: data.bankName,
      bankCardNo: data.bankCardNo,
      bankProvince: data.bankProvince,
      bankCity: data.bankCity,
      bankArea: data.bankArea,
      bankAddress: data.bankAddress,
      bankBranchName: data.bankBranchName,
      branchId: data.branchId,
      enterpriseNature: data.enterpriseNature,
      legalPersonName: data.legalPersonName,
      legalPersonMobile: data.legalPersonMobile,
      legalPersonIdCard: data.legalPersonIdCard,
      idCardFront: data.idCardFront,
      idCardBack: data.idCardBack,
      internationalDialingCode: data.internationalDialingCode,
      settlePW: JSEncrypt.encryptData(data.settlePW),
    },
  })
}

// 企业入驻信息修改
export function joininserttowupdate(data) {
  return businessrequest({
    url: '/shop/joinBase/update',
    method: 'post',
    data: {
      joinId: data.joinId,
      validateCode: data.validateCode,
      contactName: data.contactName,
      blAddress: data.blAddress,
      contactPhone: data.contactPhone,
      businessLicencePic: data.businessLicencePic,
      enterpriseName: data.enterpriseName,
      taxId: data.taxId,
      blProvince: data.blProvince,
      blCity: data.blCity,
      blArea: data.blArea,
      validateStartTime: data.validateStartTime,
      validateEndTime: data.validateEndTime,
      enterprisePhone: data.enterprisePhone,
      employeeNum: data.employeeNum,
      registerMoney: data.registerMoney,
      shopName: data.shopName,
      shopProvince: data.shopProvince,
      shopCity: data.shopCity,
      shopArea: data.shopArea,
      shopAddress: data.shopAddress,
      shopClass: data.shopClass,
      corporateAgreement: data.corporateAgreement,
      shopType: data.shopType,
      agreementFileName: data.agreementFileName,
      validateLongTerm: data.validateLongTerm,
      shopLevelId: data.shopLevelId,
      bankName: data.bankName,
      bankCardNo: data.bankCardNo,
      bankProvince: data.bankProvince,
      bankCity: data.bankCity,
      bankArea: data.bankArea,
      bankAddress: data.bankAddress,
      bankBranchName: data.bankBranchName,
      branchId: data.branchId,
      enterpriseNature: data.enterpriseNature,
      legalPersonName: data.legalPersonName,
      legalPersonMobile: data.legalPersonMobile,
      legalPersonIdCard: data.legalPersonIdCard,
      idCardFront: data.idCardFront,
      idCardBack: data.idCardBack,
      internationalDialingCode: data.internationalDialingCode,
      settlePW: JSEncrypt.encryptData(data.settlePW),
    },
  })
}

// 查询商户基本信息
export function shopinfoquery() {
  return businessrequest({
    url: '/shop/joinBase/query',
    method: 'post',
    data: {},
  })
}

// 查看店铺收费标准
export function queryexpense(data) {
  return businessrequest({
    url: '/shopBase/info/query/expense',
    method: 'post',
    data: {
      shopType: data,
    },
  })
}

// 查看支付凭证
export function payquery(data) {
  return businessrequest({
    url: '/shop/joinBase/pay/query',
    method: 'post',
    data: {},
  })
}

// 查询入驻协议
export function agreement(data) {
  return baserequest({
    url: '/base/merchant/agreement/selectAll',
    method: 'post',
    data: {
      sign: data,
    },
  })
}

//商户入驻同意协议
export function joinagree(data) {
  return businessrequest({
    url: '/shop/joinBase/queryAgree',
    method: 'post',
    data: {},
  })
}

// 查询客服电话邮箱
export function queryPhoneAndMail(data) {
  return baserequest({
    url: '/base/sysConfig/queryPhoneAndMail',
    method: 'post',
    data: {},
  })
}

// 手机验证码
export function sendPhoneCode(data) {
  return thirdpartyrequest({
    url: '/sms/verifiPhoneCode2',
    method: 'post',
    data: data,
  })
}

// 查询地址
export function queryDistinct(data) {
  return baserequest({
    url: '/base/district/query',
    method: 'get',
    params: {
      districtParentId: data.districtParentId,
    },
  })
}

// 查询店铺分类
export function classquerylist(data) {
  return businessrequest({
    url: '/shopBase/info/classList',
    method: 'post',
    data: {
      shopType: data,
    },
  })
}

// 查询地址
export function messagequery(data) {
  return businessrequest({
    url: '/shopBase/info/operate/message/query',
    method: 'post',
    data: {
      joinId: data.joinId,
      currentPage: data.currentPage || 1,
      pageSize: data.pageSize,
    },
  })
}

// 查询经营类目
export function catquery(data) {
  return goodsrequest({
    url: '/goods/cat/cat/query?shopId=1',
    method: 'get',
    data: {
      shopId: data,
    },
  })
}

// 商户添加经营类目
export function queryGoodsCats(data) {
  return businessrequest({
    url: '/shopBase/info/class/bind/updateBatch',
    method: 'post',
    data: {
      productClassIds: data.productClassId,
      joinId: data.joinId,
    },
  })
}

// 查询平台银行卡信息
export function queryBrank() {
  return baserequest({
    url: '/base/platformBrank/queryBrank',
    method: 'post',
    data: {},
  })
}

// 上传支付凭证-个人+商家
export function payupload(data) {
  return businessrequest({
    url: '/shop/join/pay/upload',
    method: 'post',
    data: {
      payImage: data.payImage,
      payComment: data.payComment,
    },
  })
}

// 校验公司名是否重复
export function companyNamecheck(data) {
  return businessrequest({
    url: '/shop/joinBase/companyName/check',
    method: 'post',
    data: {
      enterpriseName: data.enterpriseName,
    },
  })
}

// 校验联系人电话是否存在
export function phonecheck(data) {
  return businessrequest({
    url: '/shop/joinBase/phone/check',
    method: 'post',
    data: {
      userMobile: data.userMobile,
    },
  })
}

// 校验是否是企业用户
export function businessUserisBusiness(data) {
  return usermsrequest({
    url: '/user/businessInfo/isBusiness',
    method: 'post',
    data: {},
  })
}

// 查询入驻合作协议
export function queryLimitGroup(data) {
  return baserequest({
    url: 'base/merchant/agreement/selectAll',
    method: 'post',
    data: {
      sign: data.sign,
    },
  })
}

//  查询店铺等级权益
export function querySelectLevelByShop(data) {
  return businessrequest({
    url: '/shopLevel/select',
    method: 'post',
    data: {
      ...data,
    },
  })
}
// 供应商入驻情况
export function joinagree2(data) {
  return mallSupplier({
    url: 'supplierShopJoinBase/agree',
    method: 'post',
    data: {},
  })
}

// 删除类目-supplier
export function classbinddelete2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/class/bind/delete',
    method: 'post',
    data: {
      shopId: data.shopId,
      productClassId: data.productClassId,
      joinId: data.joinId,
    },
  })
}
// 批量删除类目-supplier
export function operationCommon2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/class/bind/deleteBatch',
    method: 'post',
    data: {
      productClassIds: data.productClassIds,
      joinId: data.joinId,
    },
  })
}
// 查询商户基本信息-supplier
export function shopinfoquery2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/querySupplierBase',
    method: 'post',
    data: {
      joinId: data,
    },
  })
}
// 查询地址-supplier
export function messagequery2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/operate/message',
    method: 'post',
    data: {
      joinId: data.joinId,
      page: data.currentPage,
      limit: data.pageSize,
    },
  })
}
// 商户添加经营类目-supplier
export function queryGoodsCats2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/class/bind/update/batch',
    method: 'post',
    data: {
      productClassIds: data.productClassId,
      joinId: data.joinId,
    },
  })
}
// 上传支付凭证-供应商
export function payupload2(data) {
  return mallSupplier({
    url: '/supplierShopJoinPay/pay/upload',
    method: 'post',
    data: {
      payImage: data.payImage,
      payComment: data.payComment,
    },
  })
}
// 校验公司名是否重复-supplier
export function companyNamecheck2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/companyName/check',
    method: 'post',
    data: {
      enterpriseName: data.enterpriseName,
    },
  })
} // 入驻信息提交-supplier
export function joininsertone2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/insertBase',
    method: 'post',
    data: {
      joinId: data.joinId,
      validateCode: data.validateCode,
      contactName: data.contactName,
      businessLicencePic: data.businessLicencePic,
      blAddress: data.blAddress,
      contactPhone: data.contactPhone,
      manufacturePermitPic: data.manufacturePermitPic,
      openPermitPic: data.openPermitPic,
      enterpriseName: data.enterpriseName,
      taxId: data.taxId,
      blProvince: data.blProvince,
      blCity: data.blCity,
      blArea: data.blArea,
      validateStartTime: data.validateStartTime,
      validateEndTime: data.validateEndTime,
      enterprisePhone: data.enterprisePhone,
      employeeNum: data.employeeNum,
      registerMoney: data.registerMoney,
      shopName: data.shopName,
      shopProvince: data.shopProvince,
      shopCity: data.shopCity,
      shopArea: data.shopArea,
      shopAddress: data.shopAddress,
      shopLevelId: data.shopLevelId,
      corporateAgreement: data.corporateAgreement,
      shopType: data.shopType,
      agreementFileName: data.agreementFileName,
      validateLongTerm: data.validateLongTerm,
      shopLevelId: data.shopLevelId,
      bankName: data.bankName,
      bankCardNo: data.bankCardNo,
      bankProvince: data.bankProvince,
      bankCity: data.bankCity,
      bankArea: data.bankArea,
      bankAddress: data.bankAddress,
      bankBranchName: data.bankBranchName,
      branchId: data.branchId,
      enterpriseNature: data.enterpriseNature,
      legalPersonName: data.legalPersonName,
      legalPersonMobile: data.legalPersonMobile,
      legalPersonIdCard: data.legalPersonIdCard,
      idCardFront: data.idCardFront,
      idCardBack: data.idCardBack,
      internationalDialingCode: data.internationalDialingCode,
      settlePW: JSEncrypt.encryptData(data.settlePW),
    },
  })
} // 入驻信息修改-supplier
export function joininsertoneupdate2(data) {
  return mallSupplier({
    url: '/supplierShopJoinBase/updateBase',
    method: 'post',
    data: {
      joinId: data.joinId,
      validateCode: data.validateCode,
      contactName: data.contactName,
      businessLicencePic: data.businessLicencePic,
      blAddress: data.blAddress,
      manufacturePermitPic: data.manufacturePermitPic,
      openPermitPic: data.openPermitPic,
      contactPhone: data.contactPhone,
      enterpriseName: data.enterpriseName,
      taxId: data.taxId,
      blProvince: data.blProvince,
      blCity: data.blCity,
      blArea: data.blArea,
      validateStartTime: data.validateStartTime,
      validateEndTime: data.validateEndTime,
      enterprisePhone: data.enterprisePhone,
      employeeNum: data.employeeNum,
      registerMoney: data.registerMoney,
      shopName: data.shopName,
      shopProvince: data.shopProvince,
      shopCity: data.shopCity,
      shopArea: data.shopArea,
      shopAddress: data.shopAddress,
      shopLevelId: data.shopLevelId,
      corporateAgreement: data.corporateAgreement,
      shopType: data.shopType,
      agreementFileName: data.agreementFileName,
      validateLongTerm: data.validateLongTerm,
      shopLevelId: data.shopLevelId,
      bankName: data.bankName,
      bankCardNo: data.bankCardNo,
      bankProvince: data.bankProvince,
      bankCity: data.bankCity,
      bankArea: data.bankArea,
      bankAddress: data.bankAddress,
      bankBranchName: data.bankBranchName,
      branchId: data.branchId,
      enterpriseNature: data.enterpriseNature,
      legalPersonName: data.legalPersonName,
      legalPersonMobile: data.legalPersonMobile,
      legalPersonIdCard: data.legalPersonIdCard,
      idCardFront: data.idCardFront,
      idCardBack: data.idCardBack,
      settlePW: JSEncrypt.encryptData(data.settlePW),
    },
  })
}

//查看供应商支付凭证
export function payquery2(data) {
  return mallSupplier({
    url: '/supplierShopJoinPay/pay/query',
    method: 'post',
    data: {},
  })
}
// 供应商店铺名是否重复
export function apiSupplierCheckGoodName(data) {
  return mallSupplier({
    url: '/supplierShopBase/info/checkShopNameExist',
    method: 'get',
    params: {
      ...data,
    },
  })
}
//  查询店铺等级权益-supplier
export function querySelectLevelByShop2(data) {
  return mallSupplier({
    url: '/supplierShopLevel/select',
    method: 'post',
    data: {
      ...data,
    },
  })
}
// 国际-查询国家及货币
export function queryCountryAndCurrency(data) {
  return baserequest({
    url: '/baseCountry/queryCountryAndCurrency',
    method: 'post',
    data: data,
  })
}

// 获取系统密码格式和长度规则
export function queryPasswordConfig() {
  return baserequest({
    url: '/base/sysConfig/business/passwordConfig',
    method: 'post',
    data: {},
  })
}

/**
 * 获取网站Favicon
 */
export function getFavicon() {
  return baserequest({
    url: '/base/sysConfig/quertNavLogo',
    method: 'post',
	data:{
		configKey:'logo_copywriting_ico'
	}
  })
}