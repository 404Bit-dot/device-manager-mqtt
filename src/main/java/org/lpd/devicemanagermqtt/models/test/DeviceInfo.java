package org.lpd.devicemanagermqtt.models.test;

import lombok.Data;
import java.io.Serializable;

@Data
public class DeviceInfo implements Serializable {
    private String id;
    private String name;
    private String status;
}