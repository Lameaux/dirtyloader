package com.euromoby.dirty.http;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class SSLContextProvider implements InitializingBean {

	private SSLContext sslContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		initSSLContext();
	}

	protected void initSSLContext() throws Exception {
		TrustManager tm = new X509TrustManager() {
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
			}

			@Override
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sslContext = SSLContext.getInstance("TLS");
		sslContext.init(null, new TrustManager[] { tm }, null);
	}

	public SSLContext getSSLContext() {
		return sslContext;
	}

}
