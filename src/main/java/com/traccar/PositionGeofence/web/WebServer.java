package com.traccar.PositionGeofence.web;

import com.google.inject.Injector;
import com.traccar.PositionGeofence.LifecycleObject;
import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;


import org.eclipse.jetty.http.HttpCookie;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLogWriter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ErrorHandler;
import org.eclipse.jetty.server.handler.gzip.GzipHandler;
import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.ee10.websocket.server.config.JettyWebSocketServletContainerInitializer;

import org.eclipse.jetty.session.DatabaseAdaptor;
import org.eclipse.jetty.session.DefaultSessionCache;
import org.eclipse.jetty.session.JDBCSessionDataStoreFactory;
import org.eclipse.jetty.session.SessionCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
//import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.util.EnumSet;

@Component
public class WebServer implements LifecycleObject {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebServer.class);


    private final Config config;
    private final Server server;

    public WebServer(Config config) {

        this.config = config;
        String address = config.getString(Keys.WEB_ADDRESS);
        int port = config.getInteger(Keys.WEB_PORT);
        if (address == null) {
            server = new Server(port);
        } else {
            server = new Server(new InetSocketAddress(address, port));
        }

        ServletContextHandler servletHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        JettyWebSocketServletContainerInitializer.configure(servletHandler, null);
        //servletHandler.addFilter(GuiceFilter.class, "/*", EnumSet.allOf(DispatcherType.class));

        //initApi(servletHandler);
        initSessionConfig(servletHandler);

        if (config.getBoolean(Keys.WEB_CONSOLE)) {
            servletHandler.addServlet(new ServletHolder(new ConsoleServlet(config)), "/console/*");
        }

        initWebApp(servletHandler);

        servletHandler.setErrorHandler(new ErrorHandler() {
            //@Override
            protected void handleErrorPage(
                    HttpServletRequest request, Writer writer, int code, String message) throws IOException {
                writer.write("<!DOCTYPE><html><head><title>Error</title></head><html><body>"
                        + code + " - " + HttpStatus.getMessage(code) + "</body></html>");
            }
        });

        ContextHandlerCollection handlers = new ContextHandlerCollection();
        initClientProxy(handlers);
        handlers.addHandler(servletHandler);
        handlers.addHandler(new GzipHandler());
        server.setHandler(handlers);

        if (config.hasKey(Keys.WEB_REQUEST_LOG_PATH)) {
            RequestLogWriter logWriter = new RequestLogWriter(config.getString(Keys.WEB_REQUEST_LOG_PATH));
            logWriter.setAppend(true);
            logWriter.setRetainDays(config.getInteger(Keys.WEB_REQUEST_LOG_RETAIN_DAYS));
            server.setRequestLog(new WebRequestLog(logWriter));
        }
    }

    private void initClientProxy(ContextHandlerCollection handlers) {
        int port = config.getInteger(Keys.PROTOCOL_PORT.withPrefix("osmand"));
        if (port != 0) {
            ServletContextHandler servletHandler = new ServletContextHandler() {
                public void doScope(
                        String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                        throws IOException, ServletException {
                    if (target.equals("/") && request.getMethod().equals(HttpMethod.POST.asString())) {
                        doScope(target, baseRequest, request, response);
                    }
                }
            };
            ServletHolder servletHolder = new ServletHolder(TransparentHttpProxyServlet.class,new TransparentHttpProxyServlet());
            servletHolder.setInitParameter("proxyTo", "http://localhost:" + port);
            servletHandler.addServlet(servletHolder, "/");
            handlers.addHandler(servletHandler);
        }
    }

    private void initWebApp(ServletContextHandler servletHandler) {
        ServletHolder servletHolder = new ServletHolder("DefaultOverrideServlet", new DefaultOverrideServlet(config));
        servletHolder.setInitParameter("resourceBase", new File(config.getString(Keys.WEB_PATH)).getAbsolutePath());
        servletHolder.setInitParameter("dirAllowed", "false");
        if (config.getBoolean(Keys.WEB_DEBUG)) {
            servletHandler.setWelcomeFiles(new String[] {"debug.html", "index.html"});
        } else {
            String cache = config.getString(Keys.WEB_CACHE_CONTROL);
            if (cache != null && !cache.isEmpty()) {
                servletHolder.setInitParameter("cacheControl", cache);
            }
            servletHandler.setWelcomeFiles(new String[] {"release.html", "index.html"});
        }
        servletHandler.addServlet(servletHolder, "/*");
    }

    // private void initApi(ServletContextHandler servletHandler) {
    //     String mediaPath = config.getString(Keys.MEDIA_PATH);
    //     if (mediaPath != null) {
    //         ServletHolder servletHolder = new ServletHolder(DefaultServlet.class);
    //         servletHolder.setInitParameter("resourceBase", new File(mediaPath).getAbsolutePath());
    //         servletHolder.setInitParameter("dirAllowed", "false");
    //         servletHolder.setInitParameter("pathInfoOnly", "true");
    //         servletHandler.addServlet(servletHolder, "/api/media/*");
    //     }

    //     ResourceConfig resourceConfig = new ResourceConfig();
    //     resourceConfig.property("jersey.config.server.wadl.disableWadl", true);
    //     resourceConfig.registerClasses(
    //             JacksonFeature.class,
    //             ObjectMapperContextResolver.class,
    //             DateParameterConverterProvider.class,
    //             SecurityRequestFilter.class,
    //             CorsResponseFilter.class,
    //             ResourceErrorHandler.class);
    //     resourceConfig.packages(ServerResource.class.getPackage().getName());
    //     if (resourceConfig.getClasses().stream().filter(ServerResource.class::equals).findAny().isEmpty()) {
    //         LOGGER.warn("Failed to load API resources");
    //     }
    //     servletHandler.addServlet(new ServletHolder(new ServletContainer(resourceConfig)), "/api/*");
    // }

    private void initSessionConfig(ServletContextHandler servletHandler) {
        if (config.getBoolean(Keys.WEB_PERSIST_SESSION)) {
            DatabaseAdaptor databaseAdaptor = new DatabaseAdaptor();
           // databaseAdaptor.setDatasource(injector.getInstance(DataSource.class));
            JDBCSessionDataStoreFactory jdbcSessionDataStoreFactory = new JDBCSessionDataStoreFactory();
            jdbcSessionDataStoreFactory.setDatabaseAdaptor(databaseAdaptor);
            SessionHandler sessionHandler = servletHandler.getSessionHandler();
            SessionCache sessionCache = new DefaultSessionCache(sessionHandler);
            sessionCache.setSessionDataStore(jdbcSessionDataStoreFactory.getSessionDataStore(sessionHandler));
            sessionHandler.setSessionCache(sessionCache);
        }

        SessionCookieConfig sessionCookieConfig = servletHandler.getServletContext().getSessionCookieConfig();

        int sessionTimeout = config.getInteger(Keys.WEB_SESSION_TIMEOUT);
        if (sessionTimeout > 0) {
            servletHandler.getSessionHandler().setMaxInactiveInterval(sessionTimeout);
            sessionCookieConfig.setMaxAge(sessionTimeout);
        }

        // String sameSiteCookie = config.getString(Keys.WEB_SAME_SITE_COOKIE);
        // if (sameSiteCookie != null) {
        //     switch (sameSiteCookie.toLowerCase()) {
        //         case "lax":
        //             sessionCookieConfig.setComment(HttpCookie.SAME_SITE_LAX_COMMENT);
        //             break;
        //         case "strict":
        //             sessionCookieConfig.setComment(HttpCookie.SAME_SITE_STRICT_COMMENT);
        //             break;
        //         case "none":
        //             sessionCookieConfig.setSecure(true);
        //             sessionCookieConfig.setComment(HttpCookie.SAME_SITE_NONE_COMMENT);
        //             break;
        //         default:
        //             break;
        //     }
        // }

        sessionCookieConfig.setHttpOnly(true);
    }

    @Override
    public void start() throws Exception {
        server.start();
    }

    @Override
    public void stop() throws Exception {
        server.stop();
    }

}
