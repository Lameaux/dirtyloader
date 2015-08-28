package com.euromoby.dirty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.euromoby.dirty.dao.VideoDao;
import com.euromoby.dirty.dao.VideoFileDao;
import com.euromoby.dirty.model.Video;
import com.euromoby.dirty.model.VideoFile;

@Component
public class DirtyManager {

	@Autowired
	private VideoDao videoDao;
	@Autowired
	private VideoFileDao videoFileDao;
	
	@Transactional(readOnly=true)
	public Video findVideoById(Integer id) {
		return videoDao.findById(id);
	}

	@Transactional(readOnly=true)
	public VideoFile findVideoFileById(Integer id) {
		return videoFileDao.findById(id);
	}

	public void saveOrUpdate(VideoFile videoFile) {
		VideoFile exists = videoFileDao.findById(videoFile.getId());
		if (exists == null) {
			videoFileDao.save(videoFile);
		} else {
			videoFileDao.update(videoFile);
		}
	}
	
}
