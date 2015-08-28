-- DROP TABLE video_file;

CREATE TABLE IF NOT EXISTS video_file (
	id INT PRIMARY KEY,
	available INT DEFAULT 0,
	thumbnails INT DEFAULT 0,
	error INT DEFAULT 0,
	error_text TEXT DEFAULT NULL
) ENGINE=InnoDB;

