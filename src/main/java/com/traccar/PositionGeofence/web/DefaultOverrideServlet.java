package com.traccar.PositionGeofence.web;

import com.traccar.PositionGeofence.config.Config;
import com.traccar.PositionGeofence.config.Keys;

//import jakarta.inject.Inject;
import java.io.File;
import java.io.IOException;

import org.eclipse.jetty.ee10.servlet.DefaultServlet;
import org.eclipse.jetty.util.resource.Resource;

public class DefaultOverrideServlet extends DefaultServlet {

    private Resource overrideResource;

    public DefaultOverrideServlet(Config config) {
        String override = config.getString(Keys.WEB_OVERRIDE);
        if (override != null) {
            overrideResource = Resource.newResource(new File(override));
        }
    }

    @Override
    public Resource getResource(String pathInContext) {
        if (overrideResource != null) {
            try {
                Resource override = overrideResource.resolve(pathInContext);
                if (override.exists()) {
                    return override;
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return super.getResource(pathInContext.indexOf('.') < 0 ? "/" : pathInContext);
    }

    @Override
    public String getWelcomeFile(String pathInContext) {
        return super.getWelcomeFile("/");
    }

}