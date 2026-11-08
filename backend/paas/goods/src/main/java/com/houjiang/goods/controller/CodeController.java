package com.houjiang.goods.controller;

import com.houjiang.commons.util.RedisUtil;
import com.houjiang.commons.util.ResponseResult;
import com.houjiang.commons.util.VerifiCode;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;

/**
 * @author Amos
 */
@RestController
@RequestMapping("/web")
public class CodeController extends HttpServlet {
	private static final long serialVersionUID = 1L;

	@GetMapping(value = "/getCode")
	public void getCode(@RequestParam("token") String token, HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		// 调用工具类生成的验证码和验证码图片
		// 用我们的验证码类，生成验证码类对象
		VerifiCode v = new VerifiCode();
		// 获取验证码
		BufferedImage image = v.getImage();
		// 将验证码的文本存在session中
		req.getSession().setAttribute("text", v.getText());
		// 将验证码图片响应给客户端
		VerifiCode.output(image, resp.getOutputStream());
		RedisUtil.set("verification_code_" + token, v.getText(), RedisUtil.EXRP_FIVE_MIN);

	}

	/**
	 * 校验验证码
	 *
	 * @param map
	 * @return
	 */
	@PostMapping(value = "/check/code")
	public ResponseResult checkCode(@RequestBody Map<String,Object> map) {

		String redisCode = RedisUtil.get("verification_code_" + map.get("token").toString());
		if (redisCode.equalsIgnoreCase(map.get("code").toString())) {
			return ResponseResult.success("验证通过");
		}
		return ResponseResult.fail(0, "验证失败");
	}

}
