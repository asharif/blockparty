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

		//lets try to see if we have a ip override
		String ip = req.getParameter(config.getQsIpOverride());

		if(ip == null || ip.length() == 0) {
			//if we don't have a override default to remote address
			ip = req.getRemoteAddr();
			logger.debug("original ip: " + ip);

			//lets now check to see if the request was forwarded for some other homie
			String xForwardedIp = req.getHeader("X-FORWARDED-FOR");
			if(xForwardedIp != null && xForwardedIp.length() > 0) {
				//since the header gives a client,proxy1,proxy2,...,proxyn we split and just take the first one
				String[] xfa = xForwardedIp.split(",");
				ip = xfa[0];
			}
		}

		logger.debug("final ip: " + ip);

		//now that we have our client ip lets see if it's blocked
		boolean isIpBlocked = ipBlockService.isIpBlocked(ip);

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
