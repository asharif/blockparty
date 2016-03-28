package org.orphanware.blockparty.service;

import java.util.HashSet;
import org.orphanware.blockparty.service.IpBlockService;
import static junit.framework.Assert.*;
import org.junit.Test;

public final class IpBlockServiceTest {


	@Test
	public void testStraightMatch() {

		HashSet<Long> ips = new HashSet<>();
		HashSet<Integer> bitmasks = new HashSet<>();

		String[] ipList = {"192.168.1.1", "192.168.1.0/24"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			ipBlockService.addIpToMap(ip, ips, bitmasks);
		}

		ipBlockService.setIpBlockSet(ips);
		ipBlockService.setBitmasks(bitmasks);

		String ip = "192.168.1.1";
		//lets check to see if it blocks exacts correctly
		assertTrue(ipBlockService.isIpBlocked(ip));
		
		//lets check to see if it blocks bitmasks correctly
		ip = "192.168.1.2";
		assertTrue(ipBlockService.isIpBlocked(ip));

		//lets check to see if it does not block incorrectly
		ip = "192.168.2.1";
		assertFalse(ipBlockService.isIpBlocked(ip));
	}
	
}
