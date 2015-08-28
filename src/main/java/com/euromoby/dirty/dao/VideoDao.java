package com.euromoby.dirty.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.euromoby.dirty.model.Video;

@Component
public class VideoDao {

	private DataSource dataSource;

	private static final VideoRowMapper ROW_MAPPER = new VideoRowMapper();

	@Autowired
	public VideoDao(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public Video findById(Integer id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return jdbcTemplate.queryForObject("select * from video where id = ?", ROW_MAPPER, id);
		} catch (EmptyResultDataAccessException e) {
			return null;
		}
	}

	static class VideoRowMapper implements RowMapper<Video> {
		@Override
		public Video mapRow(ResultSet rs, int rowNum) throws SQLException {
			Video video = new Video();
			video.setId(rs.getInt("id"));
			video.setSourceUrl(rs.getString("source_url"));
			return video;
		}
	}
	
}
