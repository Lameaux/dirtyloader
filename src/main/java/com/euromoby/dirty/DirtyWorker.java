package com.euromoby.dirty;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.dirty.http.HttpClientProvider;
import com.euromoby.dirty.http.ProxyList;
import com.euromoby.dirty.model.Tuple;
import com.euromoby.dirty.utils.PathUtils;
import com.euromoby.dirty.utils.ShellExecutor;
import com.euromoby.dirty.utils.StringUtils;

public class DirtyWorker implements Runnable {

	private static final Logger log = LoggerFactory.getLogger(DirtyWorker.class);

	private static final Pattern A_HREF_PATTERN = Pattern.compile(".*<a.*href=\"([^\"]+view_video[^\"]+)\"[^>]*>.*");
	private static final Pattern VIDEO_SRC_PATTERN = Pattern.compile(".*<video.*html5.*src=\"([^\"]+mp4[^\"]+)\"[^>]*>.*");
	public static final int PJS_TIMEOUT = 60 * 1000;

	private DirtyManager dirtyManager;
	private String url;
	private Integer id;
	private Config config;
	private HttpClientProvider httpClientProvider;
	private ProxyList proxyList;	

	public DirtyWorker(DirtyManager dirtyManager, String url, Integer id, Config config, HttpClientProvider httpClientProvider, ProxyList proxyList) {
		this.dirtyManager = dirtyManager;
		this.url = url;
		this.id = id;
		this.config = config;
		this.httpClientProvider = httpClientProvider;
		this.proxyList = proxyList;
	}

	@Override
	public void run() {
		log.info("{} -> {}", id, url);

		Tuple<String, Integer> proxy = proxyList.getProxy();

		if (proxy != null) {
			log.info("Using proxy " + proxy.joinString(":"));
		}
		
		try {

			// find real video url
			if (url.contains("/embed")) {
				url = findVideoPageUrl(proxy);
				log.info("{} -> {}", id, url);
			}

			ShellExecutor shellExecutor = new ShellExecutor();
			List<String> command = new ArrayList<String>();
			command.add(config.getPhantomJsLocation());
			if (proxy != null) {
				command.add("--proxy=" + proxy.joinString(":"));
			}
			command.add(config.getRelayJsLocation());
			command.add(url);
			
			String pageContent = shellExecutor.executeCommandLine(command.toArray(new String[]{}), PJS_TIMEOUT);
			if (StringUtils.nullOrEmpty(pageContent)) {
				throw new Exception("Empty content");
			}

			Matcher m = VIDEO_SRC_PATTERN.matcher(pageContent);
			if (!m.find()) {
				throw new Exception("Unable to find video src");
			}

			String videoUrl = StringEscapeUtils.unescapeHtml(m.group(1));
			File videoFile = new File(config.getDestination(), PathUtils.generatePath("video", id, ".mp4"));
			videoFile.getParentFile().mkdirs();

			downloadUrl(videoUrl, videoFile, proxy);

		} catch (Exception e) {
			log.error("Error processing " + url, e);
		}
	}

	private String findVideoPageUrl(Tuple<String, Integer> proxy) throws IOException {
		byte[] embedBytes = getUrl(url, proxy);
		String embedPage = new String(embedBytes, "UTF-8");

		Matcher m = A_HREF_PATTERN.matcher(embedPage);
		if (m.find()) {
			return m.group(1);
		}

		throw new IOException("Video URL was not found");
	}

	private byte[] getUrl(String url, Tuple<String, Integer> proxy) throws IOException {

		HttpGet request = new HttpGet(url);
		RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder(proxy);
		request.setConfig(requestConfigBuilder.build());
		CloseableHttpResponse response = httpClientProvider.executeRequest(request);
		try {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				EntityUtils.consumeQuietly(response.getEntity());
				throw new IOException(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			byte[] content = EntityUtils.toByteArray(entity);
			EntityUtils.consumeQuietly(entity);
			return content;
		} finally {
			response.close();
		}
	}

	private void downloadUrl(String url, File file, Tuple<String, Integer> proxy) throws IOException {

		log.info("Downloading video {} {}", file.getName(), url);

		HttpGet request = new HttpGet(url);
		RequestConfig.Builder requestConfigBuilder = httpClientProvider.createRequestConfigBuilder(proxy);
		request.setConfig(requestConfigBuilder.build());
		CloseableHttpResponse response = httpClientProvider.executeRequest(request);
		try {
			StatusLine statusLine = response.getStatusLine();
			if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
				EntityUtils.consumeQuietly(response.getEntity());
				throw new IOException(statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
			}

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				InputStream inputStream = entity.getContent();
				OutputStream outputStream = new FileOutputStream(file);
				try {
					IOUtils.copy(inputStream, outputStream);
					IOUtils.closeQuietly(outputStream);
					log.debug("File saved to " + file.getPath());
				} finally {
					IOUtils.closeQuietly(inputStream);
					IOUtils.closeQuietly(outputStream);
				}
			} else {
				throw new IOException("Empty response");
			}
		} finally {
			response.close();
		}
	}

}
