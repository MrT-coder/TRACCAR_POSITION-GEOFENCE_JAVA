package com.traccar.PositionGeofence.helper;

import java.net.InetAddress;
import java.net.UnknownHostException;

import jakarta.servlet.http.HttpServletRequest;

import org.eclipse.jetty.util.URIUtil;

import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;

public final class WebHelper {

    private WebHelper() {
    }

    public static String retrieveRemoteAddress(HttpServletRequest request) {

        if (request != null) {
            String remoteAddress = request.getHeader("X-FORWARDED-FOR");

            if (remoteAddress != null && !remoteAddress.isEmpty()) {
                int separatorIndex = remoteAddress.indexOf(",");
                if (separatorIndex > 0) {
                    return remoteAddress.substring(0, separatorIndex); // remove the additional data
                } else {
                    return remoteAddress;
                }
            } else {
                return request.getRemoteAddr();
            }
        } else {
            return null;
        }
    }

    public static String retrieveWebUrl(Config config) {
        if (config.hasKey(Keys.WEB_URL)) {
            return config.getString(Keys.WEB_URL).replaceAll("/$", "");
        } else {
            String address;
            try {
                address = config.getString(Keys.WEB_ADDRESS, InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e) {
                address = "localhost";
            }
            return URIUtil.newURI("http", address, config.getInteger(Keys.WEB_PORT), "", "");
        }
    }
}
