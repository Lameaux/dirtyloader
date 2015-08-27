package com.euromoby.dirty.utils;

import org.springframework.util.StringUtils;

public class PathUtils {

	private static final int DIVIDER = 100;
	private static final int FOLDERS = 3;

	public static String generatePath(String type, int id, String extension) {

		Integer[] path = new Integer[FOLDERS];

		int fileId = id / DIVIDER; // skip last part
		for (int i = FOLDERS - 1; i >= 0; i--) {
			path[i] = fileId % DIVIDER;
			fileId /= DIVIDER;
		}

		String folders = StringUtils.arrayToDelimitedString(path, "/");

		return type + "/" + folders + "/" + id + extension;

	}

	public static void main(String[] args) {
		System.out.println(generatePath("fb2", 1235678, ".mp4"));
	}

}
