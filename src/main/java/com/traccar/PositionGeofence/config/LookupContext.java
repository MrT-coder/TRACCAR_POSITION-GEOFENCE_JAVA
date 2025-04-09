package com.traccar.PositionGeofence.config;

public interface LookupContext {

    class Global implements LookupContext {
    }

    class User implements LookupContext {

        private final long userId;

        public long getUserId() {
            return userId;
        }

        public User(long userId) {
            this.userId = userId;
        }

    }

    class Device implements LookupContext {

        private final long deviceId;

        public long getDeviceId() {
            return deviceId;
        }

        public Device(long deviceId) {
            this.deviceId = deviceId;
        }

    }

}
