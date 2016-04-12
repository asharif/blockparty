package org.orphanware.blockparty.service;

import org.orphanware.blockparty.utils.IpUtils;
import org.orphanware.blockparty.config.Config;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpBlockService {

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	private static final String IP_FILE = System.getProperty("org.orphanware.blockparty.ipblacklist.path");
	private static volatile IpBlockService instace;
	private static Object monitor = new Object();

	private Config config;
	private HashMap<Integer, HashSet<Long>> maskToIpMap = new HashMap<>();
	private boolean autoRefreshInitialized = false;


	public IpBlockService withConfig(Config config) {
		this.config = config;
		return this;
	}

	private IpBlockService() {

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

		//temp map to replace instance one
		HashMap<Integer, HashSet<Long>> tmpMaskToIpMap = new HashMap<>();

		InputStream is = null;
		BufferedReader br = null;
		boolean isError = false;

		try {

			logger.debug("ip block list file is: " + IP_FILE);
			br = new BufferedReader(new InputStreamReader(new FileInputStream(IP_FILE)));

			String line = null;
			while ((line = br.readLine()) != null) {
				addIpToSet(line, tmpMaskToIpMap);
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

				logger.debug("swapping ip set after reload");
				this.maskToIpMap = tmpMaskToIpMap;
			}
		}
	}

	/**
	 * Lets add a single ip or masked ip into the given set
	 *
	 * @param ipLine
	 */
	private void addIpToSet(String ipLine, HashMap<Integer, HashSet<Long>> tmpMaskToIpMap) {

		try {

			logger.debug(ipLine);
			String[] ipMask = ipLine.split("/");
			int bitmask = 32;
			long lIp = -1;
			if (ipMask.length == 1) {
				//if length is 1 then we block single ip
				lIp = IpUtils.ipStringToLong(ipMask[0]);
				logger.debug("adding single ip to black list: " + ipLine + "=" + lIp);

			} else if (ipMask.length == 2) {
				//if length is 2 we block subnet 
				bitmask = Integer.parseInt(ipMask[1].trim());
				//ip is now a staring point to a subnet
				lIp = IpUtils.ipStringToSubnetLong(ipMask[0], bitmask);
				logger.debug("adding subnet to black list: " + ipLine + "=" + lIp);

			} else {
				//otherwise we log an issue with this line and continue
				logger.error("found a faulty ip line: " + ipLine);
			}

			if(lIp > -1) {
				//we have something to work with
				HashSet<Long> bitMaskIpSet = tmpMaskToIpMap.get(bitmask);
				if(bitMaskIpSet == null) {
					//if we don't have an entry lets make one
					bitMaskIpSet = new HashSet<>();
					tmpMaskToIpMap.put(bitmask, bitMaskIpSet);
				}
				bitMaskIpSet.add(lIp);
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

		boolean result = false;
		for (Map.Entry<Integer, HashSet<Long>> entry : this.maskToIpMap.entrySet()) {

			long maskedIp = IpUtils.ipStringToSubnetLong(ip, entry.getKey());
			result = entry.getValue().contains(maskedIp);
			//if we have a hit we stop the loop
			if (result) {
				break;
			}
		}

		return result;
	}

}
