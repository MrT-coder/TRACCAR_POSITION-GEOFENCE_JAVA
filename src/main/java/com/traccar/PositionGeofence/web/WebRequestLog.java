package com.traccar.PositionGeofence.web;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.DateCache;
import org.eclipse.jetty.util.component.ContainerLifeCycle;

import com.traccar.PositionGeofence.helper.SessionHelper;

import java.util.Locale;
import java.util.TimeZone;

public class WebRequestLog extends ContainerLifeCycle implements RequestLog {

    private final Writer writer;

    private final DateCache dateCache = new DateCache(
            "dd/MMM/yyyy:HH:mm:ss ZZZ", Locale.getDefault(), TimeZone.getTimeZone("GMT"));

    public WebRequestLog(Writer writer) {
        this.writer = writer;
        addBean(writer);
    }

    @Override
    public void log(Request request, Response response) {
        try {
            Long userId = (Long) request.getSession().getAttribute(SessionHelper.USER_ID_KEY);
            writer.write(String.format("%s - %s [%s] \"%s %s %s\" %d %d",
                    request.getRemoteHost(),
                    userId != null ? String.valueOf(userId) : "-",
                    dateCache.format(request.getTimeStamp()),
                    request.getMethod(),
                    request.getOriginalURI(),
                    request.getProtocol(),
                    response.getCommittedMetaData().getStatus(),
                    response.getHttpChannel().getBytesWritten()));
        } catch (Throwable ignored) {
        }
    }

}
