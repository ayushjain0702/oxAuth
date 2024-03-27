package org.gluu.oxauth.service.net;

/*
 * oxAuth is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2014, Gluu
 */

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;

/**
 * Provides operations with http/https requests
 *
 * @author Yuriy Movchan Date: 04/10/2023
 */
@ApplicationScoped
public class HttpService2 extends org.gluu.net.HttpServiceUtility implements Serializable {

	private static final long serialVersionUID = -2398422090669045605L;

	private static final Logger LOG = LoggerFactory.getLogger(HttpServiceUtility.class);

	private Base64 base64;

	private PoolingHttpClientConnectionManager connectionManager;

	@Inject
	private Logger log;

	@PostConstruct
	public void init() {
		super.init();
	}

	@PreDestroy
	public void destroy() {
		super.destroy();
	}

	@Override
	public Logger getLogger() {
		return log;
	}

	public void init() {
        connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200); // Increase max total connection to 200
        connectionManager.setDefaultMaxPerRoute(50); // Increase default max connection per route to 50

        this.base64 = new Base64();
	}

	public void destroy() {
		if (connectionManager != null) {
			connectionManager.shutdown();
		}
	}

	public CloseableHttpClient getHttpsClientTrustAll() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
    	getLogger().trace("Connection manager stats: {}", connectionManager.getTotalStats());

    	TrustStrategy acceptingTrustStrategy = (cert, authType) -> true;
	    SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
	    SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslContext, 
	      NoopHostnameVerifier.INSTANCE);

	    return HttpClients.custom().setSSLSocketFactory(sslConSocFactory)
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setConnectionManager(connectionManager).build();
	}

	public CloseableHttpClient getHttpsClient() {
    	getLogger().trace("Connection manager stats: {}", connectionManager.getTotalStats());

    	return HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setConnectionManager(connectionManager).build();
	}

	public CloseableHttpClient getHttpsClient(HttpRoutePlanner routerPlanner) {
    	getLogger().trace("Connection manager stats: {}", connectionManager.getTotalStats());

    	return HttpClients.custom()
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setConnectionManager(connectionManager).setRoutePlanner(routerPlanner).build();
		
	}

	public CloseableHttpClient getHttpsClient(String trustStoreType, String trustStorePath, String trustStorePassword) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException {
    	getLogger().trace("Connection manager stats: {}", connectionManager.getTotalStats());

    	SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new File(trustStorePath), trustStorePassword.toCharArray()).build();
	    SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslContext);

	    return HttpClients.custom().setSSLSocketFactory(sslConSocFactory)
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setConnectionManager(connectionManager).build();
	}

	public CloseableHttpClient getHttpsClient(String trustStoreType, String trustStorePath, String trustStorePassword,
			String keyStoreType, String keyStorePath, String keyStorePassword) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException, IOException, UnrecoverableKeyException {
    	getLogger().trace("Connection manager stats: {}", connectionManager.getTotalStats());

    	SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(new File(trustStorePath), trustStorePassword.toCharArray())
				.loadKeyMaterial(new File(keyStorePath), keyStorePassword.toCharArray(), keyStorePassword.toCharArray()).build();
	    SSLConnectionSocketFactory sslConSocFactory = new SSLConnectionSocketFactory(sslContext);

	    return HttpClients.custom().setSSLSocketFactory(sslConSocFactory)
				.setDefaultRequestConfig(RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).build())
				.setConnectionManager(connectionManager).build();
	}

	public HttpServiceResponse executePost(HttpClient httpClient, String uri, String authData, Map<String, String> headers, String postData, ContentType contentType) {
        HttpPost httpPost = new HttpPost(uri);
        if (StringHelper.isNotEmpty(authData)) {
        	httpPost.setHeader("Authorization", "Basic " + authData);
        }

        if (headers != null) {
        	for (Entry<String, String> headerEntry : headers.entrySet()) {
            	httpPost.setHeader(headerEntry.getKey(), headerEntry.getValue());
        	}
        }

        StringEntity stringEntity = new StringEntity(postData, contentType);
		httpPost.setEntity(stringEntity);

        try {
        	HttpResponse httpResponse = httpClient.execute(httpPost);

        	return new HttpServiceResponse(httpPost, httpResponse);
		} catch (IOException ex) {
	    	getLogger().error("Failed to execute post request", ex);
		}

        return null;
	}

	public HttpServiceResponse executePost(HttpClient httpClient, String uri, String authData, Map<String, String> headers, String postData) {
		return executePost(httpClient, uri, authData, headers, postData, null);
	}

	public HttpServiceResponse executePost(HttpClient httpClient, String uri, String authData, String postData, ContentType contentType) {
        return executePost(httpClient, uri, authData, null, postData, contentType);
	}

	public String encodeBase64(String value) {
		try {
			return new String(base64.encode((value).getBytes(Util.UTF8)), Util.UTF8);
		} catch (UnsupportedEncodingException ex) {
	    	getLogger().error("Failed to convert '{}' to base64", value, ex);
		}

		return null;
	}

	public String encodeUrl(String value) {
		try {
			return URLEncoder.encode(value, Util.UTF8);
		} catch (UnsupportedEncodingException ex) {
	    	getLogger().error("Failed to encode url '{}'", value, ex);
		}

		return null;
	}

	public HttpServiceResponse executeGet(HttpClient httpClient, String requestUri, Map<String, String> headers) {
		HttpGet httpGet = new HttpGet(requestUri);
        
        if (headers != null) {
        	for (Entry<String, String> headerEntry : headers.entrySet()) {
        		httpGet.setHeader(headerEntry.getKey(), headerEntry.getValue());
        	}
        }

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);

			return new HttpServiceResponse(httpGet, httpResponse);
		} catch (IOException ex) {
	    	getLogger().error("Failed to execute get request", ex);
		}

		return null;
	}

	public HttpServiceResponse executeGet(HttpClient httpClient, String requestUri) throws ClientProtocolException, IOException {
		return executeGet(httpClient, requestUri, null);
	}

	public byte[] getResponseContent(HttpResponse httpResponse) throws IOException {
		if ((httpResponse == null) || !isResponseStastusCodeOk(httpResponse)) {
        	return null;
        }

        HttpEntity entity = httpResponse.getEntity();
		byte[] responseBytes = new byte[0];
		if (entity != null) {
			responseBytes = EntityUtils.toByteArray(entity);
		}

    	// Consume response content
		if (entity != null) {
			EntityUtils.consume(entity);
		}

    	return responseBytes;
	}

	public void consume(HttpResponse httpResponse) throws IOException {
		if ((httpResponse == null) || !isResponseStastusCodeOk(httpResponse)) {
        	return;
        }

    	// Consume response content
        HttpEntity entity = httpResponse.getEntity();
		if (entity != null) {
			EntityUtils.consume(entity);
		}
	}

	public String convertEntityToString(byte[] responseBytes) {
		if (responseBytes == null) {
			return null;
		}

		return new String(responseBytes);
	}

	public String convertEntityToString(byte[] responseBytes, Charset charset) {
		if (responseBytes == null) {
			return null;
		}

		return new String(responseBytes, charset);
	}

	public String convertEntityToString(byte[] responseBytes, String charsetName) throws UnsupportedEncodingException {
		if (responseBytes == null) {
			return null;
		}

		return new String(responseBytes, charsetName);
	}

	public boolean isResponseStastusCodeOk(HttpResponse httpResponse) {
		int responseStastusCode = httpResponse.getStatusLine().getStatusCode();
		if ((responseStastusCode == HttpStatus.SC_OK) || (responseStastusCode == HttpStatus.SC_CREATED) || (responseStastusCode == HttpStatus.SC_ACCEPTED)
				|| (responseStastusCode == HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION) || (responseStastusCode == HttpStatus.SC_NO_CONTENT) || (responseStastusCode == HttpStatus.SC_RESET_CONTENT)
				|| (responseStastusCode == HttpStatus.SC_PARTIAL_CONTENT) || (responseStastusCode == HttpStatus.SC_MULTI_STATUS)) {
			return true;
		}
		
		return false;
	}

	public boolean isContentTypeXml(HttpResponse httpResponse) {
		Header contentType = httpResponse.getEntity().getContentType();
		if (contentType == null) {
			return false;
		}

		String contentTypeValue = contentType.getValue();
		if (StringHelper.equals(contentTypeValue, ContentType.APPLICATION_XML.getMimeType()) || StringHelper.equals(contentTypeValue, ContentType.TEXT_XML.getMimeType())) {
			return true;
		}
		
		return false;
	}

	public String constructServerUrl(final HttpServletRequest request) {
    	int serverPort = request.getServerPort();

    	String redirectUrl;
    	if ((serverPort == 80) || (serverPort == 443)) {
    		redirectUrl = String.format("%s://%s%s", request.getScheme(), request.getServerName(), request.getContextPath());
    	} else {
    		redirectUrl = String.format("%s://%s:%s%s", request.getScheme(), request.getServerName(), request.getServerPort(), request.getContextPath());
    	}
    	
    	return redirectUrl.toLowerCase();
    }

	public HttpRoutePlanner buildDefaultRoutePlanner(final String proxy) {
		//Creating an HttpHost object for proxy
		HttpHost proxyHost = new HttpHost(proxy); 
    	
    	return new DefaultProxyRoutePlanner(proxyHost);
    }
	
	public Logger getLogger() {
		return LOG;
	}

}
