package org.orphanware.blockparty.controller;

import org.orphanware.blockparty.service.IpBlockService;
import org.orphanware.config.Config;
import java.io.IOException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet("/")
public final class BlockController extends HttpServlet {

	private static Logger logger = LoggerFactory.getLogger(BlockController.class);
	private IpBlockService ipBlockService = IpBlockService.getInstance();
	private Config config = Config.getInstance();

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getOutputStream().write("not supported yo!".getBytes());
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.getOutputStream().write("not supported yo!".getBytes());
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		//forward to get
		doGet(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

		logger.debug("starting the block party yo!");
		long start = System.nanoTime();

		logger.debug("checking client ip...");
		String ip = req.getRemoteAddr();
		//first lets check client ip
		boolean isIpBlocked = ipBlockService.isIpBlocked(ip);
		if(!isIpBlocked) {
			logger.debug("client ip is not blocked.  checking X-FORWARDED-FOR");
			//if client ip is not blocked lets check X-FORWARDED-FOR
			String xForwardedIp = req.getHeader("X-FORWARDED-FOR");
			if(xForwardedIp != null && xForwardedIp.length() > 0) {
				//since the header gives a client,proxy1,proxy2,...,proxyn we split and just take the first one
				String[] xfa = xForwardedIp.split(",");
				ip = xfa[0];
				isIpBlocked = ipBlockService.isIpBlocked(ip);

				if(!isIpBlocked) {
					logger.debug("X-FORWARDED-FOR is not blocked.  Checking qs param");
					//if X-FORWARDED-FOR is not blocked then lets check query string param
					ip = req.getParameter(config.getQsIpOverride());
					isIpBlocked = ipBlockService.isIpBlocked(ip);
				}
			} else {
				logger.debug("X-FORWARDED-FOR does not exist.  Checking qs param");
				//if we don't have a X-FORWARDED-FOR check query string param 
				ip = req.getParameter(config.getQsIpOverride());
				isIpBlocked = ipBlockService.isIpBlocked(ip);
			}
		}
		long end = System.nanoTime() - start;
		logger.debug("block party took: " + end + "ns");

		if(isIpBlocked) {
			//if after all that we find out the ip is blocked 
			String msg = "ip (" + ip + ") or subnet has been blocked yo!  peace out!";
			logger.debug(msg);
			resp.getOutputStream().write(msg.getBytes());

		} else {
			logger.debug("ip is good homie! come on in" + ip);
			ServletContext forwardToContext = getServletContext().getContext(config.getForwardOnPass());
			if(getServletContext() != forwardToContext) {
				logger.debug("forwarding to context: " + config.getForwardOnPass());
				forwardToContext.getRequestDispatcher("/").forward(req, resp);
			} else {
				resp.getOutputStream().write("your ip is good homie!".getBytes());
			}
		}
	}
	
}
