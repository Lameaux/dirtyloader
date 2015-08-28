package com.euromoby.dirty.http;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.euromoby.dirty.Config;
import com.euromoby.dirty.model.Tuple;

@Component
public class ProxyList {

	private List<Tuple<String, Integer>> proxies = new ArrayList<>();

	private static final Logger log = LoggerFactory.getLogger(ProxyList.class);	
	
	@Autowired
	public ProxyList(Config config) {
		File proxiesFile = new File(config.getProxyList());
		if (proxiesFile.exists()) {
			BufferedReader br = null;
			String line;
			try {
				br = new BufferedReader(new FileReader(proxiesFile));
				while ((line = br.readLine()) != null) {
					String[] proxyDetails = line.split(",");
					proxies.add(Tuple.of(proxyDetails[0], Integer.valueOf(proxyDetails[1])));
				}
			} catch (Exception e) {
				log.error("Error loading proxy list from " + config.getProxyList(), e);
			} finally {
				IOUtils.closeQuietly(br);
			}
		}

	}

	public Tuple<String, Integer> getProxy() {
		if (!proxies.isEmpty()) {
			return proxies.get(0);
		}
		return null;
	}
	
}
