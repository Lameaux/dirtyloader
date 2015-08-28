package com.euromoby.dirty.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.dirty.model.VideoFile;

@Component
public class VideoFileDao {

	private DataSource dataSource;

	private static final VideoFileRowMapper ROW_MAPPER = new VideoFileRowMapper();

	@Autowired
	public VideoFileDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public VideoFile findById(Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from video_file where id = ?", ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	public void save(VideoFile videoFile) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("insert into video_file(id, available, thumbnails, error, error_text) values (?, ?, ?, ?, ?)", videoFile.getId(), videoFile.isAvailable() ? 1 : 0, videoFile.getThumbnails(), videoFile.isError() ? 1 : 0, videoFile.getErrorText());
	}	

	public void update(VideoFile videoFile) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		jdbcTemplate.update("update video_file set available=?, thumbnails=?, error=?, error_text=? where id=?", videoFile.isAvailable() ? 1 : 0, videoFile.getThumbnails(), videoFile.isError() ? 1 : 0, videoFile.getErrorText(), videoFile.getId());
	}	
	
	static class VideoFileRowMapper implements RowMapper<VideoFile> {
		@Override
		public VideoFile mapRow(ResultSet rs, int rowNum) throws SQLException {
			VideoFile videoFile = new VideoFile();
			videoFile.setId(rs.getInt("id"));
			videoFile.setAvailable(rs.getInt("available") == 1);
			videoFile.setThumbnails(rs.getInt("thumbnails"));
			videoFile.setError(rs.getInt("error") == 1);
			videoFile.setErrorText(rs.getString("error_text"));
			return videoFile;
		}
	}

}
