package org.orphanware.blockparty.init;

import org.orphanware.blockparty.service.IpBlockService;
import org.orphanware.config.Config;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public final class AppContextListener implements ServletContextListener{

	private Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	private ServletContext context;

	public ServletContext getServletContext(){
		return this.context;
	}

	/**
	 * We do this on app startup yo!
	 * @param servletContextEvent 
	 */
    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {

		context = servletContextEvent.getServletContext();

        logger.info("block-party is starting up yo!");
		logger.info("loading ip black list...");

		IpBlockService
			.getInstance()
			.withConfig(Config.getInstance())
			.startLoadIpFileIntoMapsLoop();
			
    }

	/**
	 * We do this on app shutdown yo!
	 * @param servletContextEvent 
	 */
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        System.out.println("block-party is shutting down yo!");
    }

	
}

