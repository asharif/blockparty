package org.orphanware.blockparty.service;

import com.orphanware.blockparty.test.utils.TestUtils;
import java.util.HashSet;
import org.orphanware.blockparty.service.IpBlockService;
import static junit.framework.Assert.*;
import org.junit.Test;

public final class IpBlockServiceTest {


	@Test
	public void testStraightMatch() throws Exception{

		Object[] args = new Object[3];
		args[1] = new HashSet<Long>();
		args[2]= new HashSet<Integer>();

		String[] ipList = {"192.168.1.1", "192.168.1.0/24"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			args[0] = ip;
			TestUtils.callPrivateMethod(ipBlockService, "addIpToSet", args);
		}

		TestUtils.setValueOnPrivateField(ipBlockService, "ipBlockSet", args[1]);
		TestUtils.setValueOnPrivateField(ipBlockService, "bitmasks", args[2]);

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
