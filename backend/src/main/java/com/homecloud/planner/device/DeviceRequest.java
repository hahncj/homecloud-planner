package com.homecloud.planner.device;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record DeviceRequest(
        @NotBlank @Size(max = 200) String name,
        String manufacturer,
        String model,
        String serialNumber,
        String role,
        String location,
        String hostname,
        @Pattern(
                regexp = "^$|^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
                        + "|^([0-9A-Fa-f]{1,4}:){7}[0-9A-Fa-f]{1,4}$",
                message = "ipAddress must be a valid IPv4 or IPv6 address")
                String ipAddress,
        @Pattern(regexp = "^$|^([0-9A-Fa-f]{2}[:-]){5}[0-9A-Fa-f]{2}$", message = "macAddress must be a valid MAC address")
                String macAddress,
        @Min(1) @Max(4094) Integer vlan,
        String operatingSystem,
        String firmwareVersion,
        LocalDate purchaseDate,
        LocalDate warrantyExpiration,
        @NotNull LifecycleStatus lifecycleStatus,
        LocalDate replacementTarget,
        String notes) {
}
