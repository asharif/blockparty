package org.orphanware.blockparty.service;

import com.orphanware.blockparty.test.utils.TestUtils;
import java.util.HashMap;
import java.util.HashSet;
import org.orphanware.blockparty.service.IpBlockService;
import static junit.framework.Assert.*;
import org.junit.Test;

public final class IpBlockServiceTest {

	@Test
	public void testStraightMatch() throws Exception{

		Object[] args = new Object[2];
		args[1] = new HashMap<Integer, HashSet<Long>>();

		String[] ipList = {"192.168.1.1"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			args[0] = ip;
			TestUtils.callPrivateMethod(ipBlockService, "addIpToSet", args);
		}

		TestUtils.setValueOnPrivateField(ipBlockService, "maskToIpMap", args[1]);

		String ip = "192.168.1.1";
		//lets check to see if it blocks exacts correctly
		assertTrue(ipBlockService.isIpBlocked(ip));
		
		//lets check to see if it does not block incorrectly
		ip = "192.168.1.2";
		assertFalse(ipBlockService.isIpBlocked(ip));

	}

	@Test
	public void test24bitsMatch() throws Exception{

		Object[] args = new Object[2];
		args[1] = new HashMap<Integer, HashSet<Long>>();

		String[] ipList = {"192.168.1.0/24"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			args[0] = ip;
			TestUtils.callPrivateMethod(ipBlockService, "addIpToSet", args);
		}

		TestUtils.setValueOnPrivateField(ipBlockService, "maskToIpMap", args[1]);
		
		//lets check to see if it blocks bitmasks correctly
		String ip = "192.168.1.2";
		assertTrue(ipBlockService.isIpBlocked(ip));

		//check failure
		ip = "192.168.2.1";
		assertFalse(ipBlockService.isIpBlocked(ip));

	}

	@Test
	public void test8bitsMatch() throws Exception{

		Object[] args = new Object[2];
		args[1] = new HashMap<Integer, HashSet<Long>>();

		String[] ipList = {"192.168.1.0/8"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			args[0] = ip;
			TestUtils.callPrivateMethod(ipBlockService, "addIpToSet", args);
		}

		TestUtils.setValueOnPrivateField(ipBlockService, "maskToIpMap", args[1]);

		String ip = "192.169.1.1";
		//lets check to see if it blocks bitmasks correctly
		assertTrue(ipBlockService.isIpBlocked(ip));
		
		//check failure
		ip = "193.168.1.1";
		assertFalse(ipBlockService.isIpBlocked(ip));
	
	}

	@Test
	public void test32bitsMatch() throws Exception{

		Object[] args = new Object[2];
		args[1] = new HashMap<Integer, HashSet<Long>>();

		String[] ipList = {"192.168.1.0/32"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			args[0] = ip;
			TestUtils.callPrivateMethod(ipBlockService, "addIpToSet", args);
		}

		TestUtils.setValueOnPrivateField(ipBlockService, "maskToIpMap", args[1]);

		String ip = "192.168.1.0";
		//lets check to see if it blocks bitmasks correctly
		assertTrue(ipBlockService.isIpBlocked(ip));

		//check another ip
		ip = "192.168.1.1";
		//check failure
		assertFalse(ipBlockService.isIpBlocked(ip));
		
	}

	@Test
	public void test31bitsMatch() throws Exception{

		Object[] args = new Object[2];
		args[1] = new HashMap<Integer, HashSet<Long>>();

		String[] ipList = {"192.168.1.0/31"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			args[0] = ip;
			TestUtils.callPrivateMethod(ipBlockService, "addIpToSet", args);
		}

		TestUtils.setValueOnPrivateField(ipBlockService, "maskToIpMap", args[1]);

		String ip = "192.168.1.0";
		//lets check to see if it blocks bitmasks correctly
		assertTrue(ipBlockService.isIpBlocked(ip));

		//check another ip
		ip = "192.168.1.1";
		assertTrue(ipBlockService.isIpBlocked(ip));

		//check another ip for fail
		ip = "192.168.1.2";
		assertFalse(ipBlockService.isIpBlocked(ip));
		
	}
	@Test
	public void test7bitsMatch() throws Exception{

		Object[] args = new Object[2];
		args[1] = new HashMap<Integer, HashSet<Long>>();

		String[] ipList = {"1.168.1.0/7"};
		//make instance with map
		IpBlockService ipBlockService = IpBlockService.getInstance();
		for(String ip : ipList) {
			args[0] = ip;
			TestUtils.callPrivateMethod(ipBlockService, "addIpToSet", args);
		}

		TestUtils.setValueOnPrivateField(ipBlockService, "maskToIpMap", args[1]);

		String ip = "1.168.1.0";
		//lets check to see if it blocks bitmasks correctly
		assertTrue(ipBlockService.isIpBlocked(ip));

		ip = "2.168.1.0";
		//lets check to see if it blocks bitmasks correctly
		assertFalse(ipBlockService.isIpBlocked(ip));

		
	}
	
	
}
