package com.homecloud.planner.servicecatalog;

public record ManagedServiceFilter(ManagedServiceStatus status, RuntimeType runtimeType, Sensitivity sensitivity, Boolean externallyExposed) {

    public static final ManagedServiceFilter NONE = new ManagedServiceFilter(null, null, null, null);
}
