package com.houjiang.commons.aspect;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.Charset;

/**
 * @author Amos
 */
public class JsonRequestWrapper extends HttpServletRequestWrapper {
	private final String body;
	public JsonRequestWrapper(HttpServletRequest request) throws IOException {
		super(request);
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		InputStream inputStream=null;
		InputStreamReader inputStreamReader=null;
		try {
			 inputStream = request.getInputStream();

			inputStreamReader=new InputStreamReader(inputStream, Charset.forName("UTF-8"));

			if (inputStream != null) {

				bufferedReader = new BufferedReader(inputStreamReader);

				char[] charBuffer = new char[128];

				int bytesRead = -1;

				while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {

					stringBuilder.append(charBuffer, 0, bytesRead);

				}
			} else {
				stringBuilder.append("");
			}
		} catch (IOException ex) {


			System.out.println(ex.getMessage());
		} finally {
			if (bufferedReader != null) {
				bufferedReader.close();
			}
			if (inputStreamReader != null) {
				inputStreamReader.close();

			}
			if (inputStream != null) {
					inputStream.close();
			}

		}
		body = stringBuilder.toString();
	}

	@Override
	public ServletInputStream getInputStream() throws IOException {
		final ByteArrayInputStream byteArrayInputStream = new     ByteArrayInputStream(body.getBytes(Charset.forName("UTF-8")));
		ServletInputStream servletInputStream = new ServletInputStream() {
			@Override
			public int read() throws IOException {
				return byteArrayInputStream.read();
			}

			@Override
			public boolean isFinished() {
				// body 已经在内存里：读完了才是结束。
				// 早期恒返回 false，容器会认为流永远没结束，非阻塞读取会一直等下去
				return byteArrayInputStream.available() == 0;
			}

			@Override
			public boolean isReady() {
				// 内存流不会阻塞，随时可读。恒返回 false 会让容器判定「数据未就绪」而读不到 body
				return true;
			}

			@Override
			public void setReadListener(ReadListener readListener) {
				// 早期是空实现：容器一旦走非阻塞路径就会挂在等事件上。
				// 数据已全在内存，这里直接同步通知可读 + 读完即可
				if (readListener == null) {
					return;
				}
				try {
					readListener.onDataAvailable();
					readListener.onAllDataRead();
				} catch (java.io.IOException e) {
					throw new java.io.UncheckedIOException(e);
				}
			}
		};
		return servletInputStream;
	}

	@Override
	public BufferedReader getReader() throws IOException {
		return new BufferedReader(new InputStreamReader(this.getInputStream()));
	}

	public String getBody() {
		return this.body;
	}
}