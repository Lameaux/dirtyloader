package com.euromoby.dirty.model;

public class VideoFile {

	private Integer id;
	private boolean available;
	private int thumbnails;
	private boolean error;
	private String errorText;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public boolean isError() {
		return error;
	}

	public void setError(boolean error) {
		this.error = error;
	}

	public String getErrorText() {
		return errorText;
	}

	public void setErrorText(String errorText) {
		this.errorText = errorText;
	}

	public int getThumbnails() {
		return thumbnails;
	}

	public void setThumbnails(int thumbnails) {
		this.thumbnails = thumbnails;
	}

}
