package com.houjiang.thirdparty.dto;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Amos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TencentSmsDTO {

	@JSONField(name = "PhoneNumberSet")
	private List<String> PhoneNumberSet;

	@JSONField(name = "TemplateID")
	private String TemplateID;

	@JSONField(name = "Sign")
	private String Sign;

	@JSONField(name = "TemplateParamSet")
	private List<String> TemplateParamSet;

	@JSONField(name = "SmsSdkAppid")
	private String SmsSdkAppid;

	public TencentSmsDTO(List<String> phoneNumberSet, String templateID, List<String> templateParamSet) {
		super();
		PhoneNumberSet = phoneNumberSet;
		TemplateID = templateID;
		TemplateParamSet = templateParamSet;
	}

}
