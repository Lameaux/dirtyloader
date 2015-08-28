package com.euromoby.dirty;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.euromoby.dirty.exception.VideoRemovedException;
import com.euromoby.dirty.ffmpeg.Ffmpeg;
import com.euromoby.dirty.ffmpeg.model.FfmpegFormat;
import com.euromoby.dirty.http.HttpClientProvider;
import com.euromoby.dirty.model.VideoFile;
import com.euromoby.dirty.utils.HttpUtils;
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
	private Ffmpeg ffmpeg;

	public DirtyWorker(DirtyManager dirtyManager, String url, Integer id, Config config, HttpClientProvider httpClientProvider, Ffmpeg ffmpeg) {
		this.dirtyManager = dirtyManager;
		this.url = url;
		this.id = id;
		this.config = config;
		this.httpClientProvider = httpClientProvider;
		this.ffmpeg = ffmpeg;
	}

	@Override
	public void run() {
		log.info("{} -> {}", id, url);
		
		VideoFile videoFile = dirtyManager.findVideoFileById(id);
		if (videoFile == null) {
			videoFile = new VideoFile();
			videoFile.setId(id);
			videoFile.setError(true);
		}
		
		if (!videoFile.isError()) {
			return;
		}
		
		try {

			// find real video url
			if (url.contains("/embed")) {
				url = findVideoPageUrl();
				log.info("{} -> {}", id, url);
			}

			ShellExecutor shellExecutor = new ShellExecutor();
			List<String> command = new ArrayList<String>();
			command.add(config.getPhantomJsLocation());
			command.add(config.getRelayJsLocation());
			command.add(url);
			command.add(config.getClientTimeout()+"");
			
			log.debug("Calling {}", command.toString());
			String pageContent = shellExecutor.executeCommandLine(command.toArray(new String[]{}), PJS_TIMEOUT);
			if (StringUtils.nullOrEmpty(pageContent)) {
				throw new Exception("Empty content");
			}

			Matcher m = VIDEO_SRC_PATTERN.matcher(pageContent);
			if (!m.find()) {
				if (pageContent.contains("<video")) {
					throw new Exception("Invalid <video> tag");
				}
				throw new VideoRemovedException();
			}

			String videoUrl = StringEscapeUtils.unescapeHtml(m.group(1));
			File mp4File = new File(config.getDestination(), PathUtils.generatePath("video", id, ".mp4"));
			mp4File.getParentFile().mkdirs();

			HttpUtils.downloadUrl(videoUrl, mp4File, httpClientProvider);
			try {
				videoFile.setThumbnails(createThumbnails(mp4File));
			} catch (Exception e) {
				videoFile.setThumbnails(0);
				log.error("Failed to create thumbnails for " + id, e);
			}
			
			videoFile.setAvailable(true);
			videoFile.setError(false);
			videoFile.setErrorText(null);
		} catch (VideoRemovedException e) {
			videoFile.setAvailable(false);
			videoFile.setError(false);
			videoFile.setErrorText(null);
			videoFile.setThumbnails(0);
		} catch (Exception e) {
			videoFile.setAvailable(false);
			videoFile.setError(true);
			videoFile.setErrorText(e.getMessage());
			videoFile.setThumbnails(0);
			log.error("Error processing " + url, e);
		}
		
		dirtyManager.saveOrUpdate(videoFile);
	}

	private String findVideoPageUrl() throws Exception {
		byte[] embedBytes = HttpUtils.getUrl(url, httpClientProvider);
		String embedPage = new String(embedBytes, "UTF-8");

		Matcher m = A_HREF_PATTERN.matcher(embedPage);
		if (m.find()) {
			return m.group(1);
		}
		if (embedPage.contains("pornhub")) {
			throw new VideoRemovedException();
		}
		throw new Exception("Invalid response");
	}


	private int createThumbnails(File videoFile) throws Exception {
		log.info("Creating thumbnails for video {}", videoFile.getName());
		FfmpegFormat ffmpegFormat = ffmpeg.getFormat(videoFile.getCanonicalPath());
		if (ffmpegFormat != null) {
			int duration = (int) Math.round(ffmpegFormat.getDuration());
			int thumbnailCount = config.getFfmpegThumbnailsCount();
			int chunkLength = duration / (thumbnailCount + 1);
			
			int thumbnailWidth = 320;
			int thumbnailHeight = -1;
			
			File thumbFolder = new File(config.getDestination(), PathUtils.generatePath("thumb", id, ""));
			thumbFolder.mkdirs();			
			
			for (int i=1; i<=thumbnailCount; i++) {
				File thumbFile = new File(thumbFolder, id + "-" + i + ".jpg"); 
				log.debug("Creating thumbnail {}", thumbFile.getName());
				ffmpeg.createThumbnail(videoFile.getCanonicalPath(), chunkLength*i, thumbnailWidth, thumbnailHeight, thumbFile.getCanonicalPath());
			}
			return thumbnailCount;
		}
		return 0;
	}

}
