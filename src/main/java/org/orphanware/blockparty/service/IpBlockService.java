package org.orphanware.blockparty.service;

import org.orphanware.blockparty.utils.IpUtils;
import org.orphanware.blockparty.config.Config;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpBlockService {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private static final String IP_FILE = System.getProperty("org.orphanware.blockparty.ipblacklist.path");
	private static volatile IpBlockService instace;
	private static Object monitor = new Object();

	private Config config;
	private HashSet<Long> ipBlockSet = new HashSet<>();
	private HashSet<Integer> bitmasks = new HashSet<>();
	private boolean autoRefreshInitialized = false;


	public IpBlockService withConfig(Config config) {
		this.config = config;
		return this;
	}

	private IpBlockService() {

	}

	public void setIpBlockSet(HashSet<Long> ipBlockSet) {
		this.ipBlockSet = ipBlockSet;
	}

	public void setBitmasks(HashSet<Integer> bitmasks) {
		this.bitmasks = bitmasks;
	}

	/**
	 * Get the singleton instance
	 *
	 * @return
	 */
	public static IpBlockService getInstance() {

		if (instace == null) {
			synchronized (monitor) {
				if (instace == null) {
					instace = new IpBlockService();
				}
			}
		}

		return instace;
	}

	/**
	 * Start the auto refresh thread.
	 */
	public void startLoadIpFileIntoMapsLoop() {

		//if we've alrady called we ignore so we don't have two threads loading
		if (!autoRefreshInitialized) {
			autoRefreshInitialized = true;
			loadIpFileIntoMaps();

			new Thread(() -> {

				while (true) {
					//sleep before reloading again
					try {
						logger.debug("sleeping for " + config.getIpListRefreshMs() + " before refreshing ip list yo!");
						Thread.sleep(config.getIpListRefreshMs());
					} catch (InterruptedException e) {
						logger.error("my sleep got interrupted!  reloading file now!", e);
					}

					loadIpFileIntoMaps();
				}

			}).start();
		}

	}

	/**
	 * Lets load our ip black list from file into the set
	 */
	private void loadIpFileIntoMaps() {

		//tmp ip set to replace instance one
		HashSet<Long> tmpIpSet = new HashSet<>();
		//tmp bitmasks to replace instance one
		HashSet<Integer> tmpBitmasks = new HashSet<>();

		InputStream is = null;
		BufferedReader br = null;
		boolean isError = false;

		try {

			logger.debug("ip block list file is: " + IP_FILE);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(IP_FILE)));

			String line = null;
			while ((line = br.readLine()) != null) {
				addIpToMap(line, tmpIpSet, tmpBitmasks);
			}

		} catch (IOException e) {

			isError = true;
			logger.error("could not read " + IP_FILE
				+ " file yo!", e);

		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e2) {
			}

			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException e3) {
			}

			//if we didn't have any errors replace old set with new one
			if (!isError) {

				logger.debug("swapping ip set and bitmasks after reload");
				this.ipBlockSet = tmpIpSet;
				this.bitmasks = tmpBitmasks;
			}
		}
	}

	/**
	 * Lets add a single ip or masked ip into the given set
	 *
	 * @param ipLine
	 */
	public void addIpToMap(String ipLine, HashSet<Long> tmpIpSet,
		HashSet<Integer> tmpBitMasks) {

		try {

			logger.debug(ipLine);
			String[] ipMask = ipLine.split("/");
			if (ipMask.length == 1) {
				//if length is 1 then we block single ip
				long lIp = IpUtils.ipStringToLong(ipMask[0]);
				logger.debug("adding single ip to black list: " + ipLine + "=" + lIp);
				tmpIpSet.add(lIp);

			} else if (ipMask.length == 2) {
				//if length is 2 we block subnet 
				int maskBits = Integer.parseInt(ipMask[1].trim());
				//keep track of the bitmasks
				tmpBitMasks.add(maskBits);

				long lIp = IpUtils.ipStringToSubnetLong(ipMask[0], maskBits);
				logger.debug("adding subnet to black list: " + ipLine + "=" + lIp);
				tmpIpSet.add(lIp);

			} else {
				//otherwise we log an issue with this line and continue
				logger.error("found a faulty ip line: " + ipLine);
			}

		} catch (Exception e) {
			//if we can't parse we log and move on
			logger.error("found a faulty ip line: " + ipLine, e);
		}

	}

	/**
	 * do we have a block?
	 *
	 * @param ip
	 * @return
	 */
	public boolean isIpBlocked(String ip) {

		long lIp = IpUtils.ipStringToLong(ip);
		//first we check a exact match
		boolean result = ipBlockSet.contains(lIp);

		//if we don't have a hit then we check our bitmasks to mask the ip and try again
		if (!result) {
			logger.debug("exact match failed.  trying masked ip");
			for (Integer bitmask : this.bitmasks) {
				long maskedIp = IpUtils.ipStringToSubnetLong(ip, bitmask);
				logger.debug("masked ip: " + maskedIp);
				result = ipBlockSet.contains(maskedIp);
				//if we have a hit we stop the loop
				if (result) {
					break;
				}
			}
		}

		logger.debug(String.format("ip: %s, lIp: %d, blocked: %b", ip, lIp, result));
		return result;
	}

}
