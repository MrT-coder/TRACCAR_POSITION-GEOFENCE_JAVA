package com.traccar.PositionGeofence.helper;


import java.util.Date;

import com.traccar.PositionGeofence.modelo.User;

import jakarta.servlet.http.HttpServletRequest;

public final class SessionHelper {

    public static final String USER_ID_KEY = "userId";
    public static final String EXPIRATION_KEY = "expiration";

    private SessionHelper() {
    }

    public static void userLogin(LogAction actionLogger, HttpServletRequest request, User user, Date expiration) {
        request.getSession().invalidate();
        request.getSession().setAttribute(USER_ID_KEY, user.getId());

        if (expiration != null) {
            request.getSession().setAttribute(EXPIRATION_KEY, expiration);
        }

        actionLogger.login(request, user.getId());
    }

}