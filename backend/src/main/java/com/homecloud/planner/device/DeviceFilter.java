package com.homecloud.planner.device;

public record DeviceFilter(LifecycleStatus lifecycleStatus, String role, String location) {

    public static final DeviceFilter NONE = new DeviceFilter(null, null, null);
}
