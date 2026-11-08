package com.houjiang.commons.util.secverifyapiutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import okhttp3.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author Amos
 */
public class Auth {


	public static Map<String, String> getPhoneUtil(Map<String, String> param) throws Exception {
		System.out
				.println(param.get("token") + param.get("opToken") + param.get("operator") + VerifyConfig.VERIFYAPPKEY);
		String url = VerifyConfig.AUTHHOST;
		HashMap<String, Object> request = new HashMap<>();
		request.put("appkey", VerifyConfig.VERIFYAPPKEY);
		request.put("token", param.get("token"));
		request.put("opToken", param.get("opToken"));
		request.put("operator", param.get("operator"));
		request.put("timestamp", System.currentTimeMillis());
		Map<String, String> map = new HashMap<String, String>();
		if (null != param.get("md5") && !("".equals(param.get("md5")))) {
			request.put("md5", param.get("md5"));
		}

		request.put("sign", SignUtil.getSign(request, VerifyConfig.APPSECRET));
		String response = postRequestNoSecurity(url, null, request);
		JSONObject jsonObject = JSONObject.parseObject(response);
		if (200 == jsonObject.getInteger("status")) {
			String res = jsonObject.getString("res");
			byte[] decode = DES.decode(Base64.getDecoder().decode(res.getBytes()), VerifyConfig.APPSECRET.getBytes());

			JSONObject json = JSONObject.parseObject(new String(decode));
			String password = MessageMD5Util.encoder32ByMd5(json.getString("phone"));

			map.put("phone", json.getString("phone"));
			map.put("isValid", json.getString("isValid"));
			map.put("status", "" + jsonObject.getInteger("status"));
			map.put("randomNum", password);
			return map;
		} else {
			map.put("phone", "");
			map.put("isValid", "2");
			map.put("status", "" + jsonObject.getInteger("status"));
			return map;
		}
	}

	public static String postRequestNoSecurity(String url, Map<String, String> headers, Object data) throws Exception {
		String securityReq = JSON.toJSONString(data);
		OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(30, TimeUnit.SECONDS).build();
		RequestBody body = RequestBody.create(MediaType.parse("application/json"), securityReq);
		Request.Builder builder = new Request.Builder();
		if (!BaseUtils.isEmpty(headers)) {
			for (Map.Entry<String, String> entry : headers.entrySet()) {
				builder.addHeader(entry.getKey(), entry.getValue());
			}
		}
		final Request request = builder.addHeader("Content-Length", String.valueOf(securityReq.length())).url(url)
				.post(body).build();
		Call call = okHttpClient.newCall(request);
		Response response = call.execute();

		String securityRes = response.body().string();
		return securityRes;
	}
}
