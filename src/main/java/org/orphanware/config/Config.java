package org.orphanware.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Config {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private static final String CONFIG_FILE = System.getProperty("org.orphanware.blockparty.config.path");
	private static final long CONFIG_REFRESH_MS = 30 * 1000;

	private static Config instance;
	private static Object monitor = new Object();

	private long ipListRefreshMs = 60000;
	private String forwardOnPass = "/configsux";
	private String qsIpOverride = "ip";

	/**
	 * Getter for how often to refresh the ip black list.  Don't want this 
	 * to happen too often to not take up resources
	 * @return 
	 */
	public long getIpListRefreshMs(){
		return this.ipListRefreshMs;
	}

	public String getForwardOnPass() {
		return this.forwardOnPass;
	}

	public String getQsIpOverride(){
		return this.qsIpOverride;
	}

	/**
	 * The constructor will continuously reload the config every CONFIG_REFRESH_MS
	 */
	private Config() {

		loadConfig();

		new Thread(()->{

			while(true) {
				//sleep before reloading config file
				try {
					logger.debug("sleeping for " + CONFIG_FILE + " before loading config file yo!");
					Thread.sleep(CONFIG_REFRESH_MS);

				} catch(InterruptedException ie){
					logger.error("my sleep got interrupted!  reloading file now!", ie);
				}

				loadConfig();
			}
		}).start();

	}

	/**
	 * Get the singleton instance
	 * @return 
	 */
	public static Config getInstance() {

		if (instance == null) {
			synchronized (monitor) {
				if (instance == null) {
					instance = new Config();
				}
			}
		}

		return instance;
	}

	/**
	 * Lets load the config from file
	 */
	private void loadConfig() {

		Properties prop = new Properties();
		InputStream is = null;

		try {

			is = new FileInputStream(CONFIG_FILE);
			prop.load(is);

			//get how often we should refresh ip list
			this.ipListRefreshMs = Long.parseLong(prop.getProperty("org.orphanware.blockparty.ip.refresh.ms"));
			logger.info("ip refresh ms (org.orphanware.blockparty.ip.refresh.ms) is set to: " + this.ipListRefreshMs);

			//where should we forward to on pass
			this.forwardOnPass = prop.getProperty("org.orphanware.blockparty.forward.on.pass");
			logger.info("forward on pass (org.orphanware.blockparty.forward.on.pass): " + this.forwardOnPass);

			//use a request param as ip override
			this.qsIpOverride = prop.getProperty("org.orphanware.blockparty.req.param.ip.override");
			logger.info("qs param ip override (org.orphanware.blockparty.req.param.ip.override): " + this.qsIpOverride);

		} catch (Exception e) {
			logger.error("problem with config file yo! " + CONFIG_FILE, e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					//not much can be done yo
				}
			}

		}
	}

}
