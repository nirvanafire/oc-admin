package com.nirvanafire.ocadmin.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AssignPermissionsRequest {
    private Set<Long> menuIds;
}
