package org.orphanware.blockparty.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IpUtils {

	private static Logger logger = LoggerFactory.getLogger(IpUtils.class);
	/**
	 * Lets quickly calculate a long value of the string ip.
	 * @param ip
	 * @return 
	 */
	public static long ipStringToLong(String ip) {

		long start = System.nanoTime();
		long result = 0;

		//lets make sure we have something to work with yo!
		if(ip != null && ip.length() != 0) {

			//lets break this ip up by parts yo!
			String[] octets = ip.split("\\.");

			//do we have a valid ip?
			if(octets.length == 4) {

				for(int i=3; i >=0; --i) {

					//lets get the first octet from left to right
					long octet = Integer.parseInt(octets[3-i]);

					//lets shift these suckers over to represent the correct magnitude
					result |= octet << (8*i);

				}

			} 
			
		}

		long end = System.nanoTime() - start;
		logger.debug("ip string to long took: " + end + "ns");

		return result;
	}

	/**
	 * Given a string ip and a mask.  Get the subnet long of the ip
	 * @param ip
	 * @param maskedBits
	 * @return 
	 */
	public static long ipStringToSubnetLong(String ip, int maskedBits) {

		long start = System.nanoTime();

		//lets get ip as long first
		long lIp = ipStringToLong(ip);
		
		//calculate how much we are gonna be shifting
		int shiftAmount = 32 - maskedBits;
		//now lets right shift to only leave the left most maskedBits
		long subnetIp = lIp >> shiftAmount;
		//now lets shift back to give the bits their original magnitude yo!
		subnetIp = subnetIp << shiftAmount;
		
		long end = System.nanoTime() - start;
		logger.debug("ip string to subnet long took: " + end + "ns");

		return subnetIp;
	
	}
	
}
