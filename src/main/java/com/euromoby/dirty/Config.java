package com.euromoby.dirty;

public class Config {
	public static final String DEFAULT_HTTP_USERAGENT = "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36";

	private int clientTimeout;
	private String proxyHost;
	private int proxyPort;
	private String httpUserAgent = DEFAULT_HTTP_USERAGENT;

	private int taskPoolSize;
	private int taskQueueSize;
	private int taskRetry;

	private int startId;
	private int endId;
	private String listId;

	private String destination;

	private String phantomJsLocation;
	private String relayJsLocation;
	private String ffmpegLocation;

	private int ffmpegThumbnailsCount;

	public int getTaskPoolSize() {
		return taskPoolSize;
	}

	public void setTaskPoolSize(int taskPoolSize) {
		this.taskPoolSize = taskPoolSize;
	}

	public int getTaskQueueSize() {
		return taskQueueSize;
	}

	public void setTaskQueueSize(int taskQueueSize) {
		this.taskQueueSize = taskQueueSize;
	}

	public int getTaskRetry() {
		return taskRetry;
	}

	public void setTaskRetry(int taskRetry) {
		this.taskRetry = taskRetry;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public int getClientTimeout() {
		return clientTimeout;
	}

	public void setClientTimeout(int clientTimeout) {
		this.clientTimeout = clientTimeout;
	}

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public String getHttpUserAgent() {
		return httpUserAgent;
	}

	public void setHttpUserAgent(String httpUserAgent) {
		this.httpUserAgent = httpUserAgent;
	}

	public String getPhantomJsLocation() {
		return phantomJsLocation;
	}

	public void setPhantomJsLocation(String phantomJsLocation) {
		this.phantomJsLocation = phantomJsLocation;
	}

	public String getRelayJsLocation() {
		return relayJsLocation;
	}

	public void setRelayJsLocation(String relayJsLocation) {
		this.relayJsLocation = relayJsLocation;
	}

	public String getFfmpegLocation() {
		return ffmpegLocation;
	}

	public void setFfmpegLocation(String ffmpegLocation) {
		this.ffmpegLocation = ffmpegLocation;
	}

	public int getFfmpegThumbnailsCount() {
		return ffmpegThumbnailsCount;
	}

	public void setFfmpegThumbnailsCount(int ffmpegThumbnailsCount) {
		this.ffmpegThumbnailsCount = ffmpegThumbnailsCount;
	}

	public int getStartId() {
		return startId;
	}

	public void setStartId(int startId) {
		this.startId = startId;
	}

	public int getEndId() {
		return endId;
	}

	public void setEndId(int endId) {
		this.endId = endId;
	}

	public String getListId() {
		return listId;
	}

	public void setListId(String listId) {
		this.listId = listId;
	}

}
