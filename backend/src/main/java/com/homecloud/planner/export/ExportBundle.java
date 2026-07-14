package com.homecloud.planner.export;

import java.time.Instant;
import java.util.List;

public record ExportBundle(Instant exportedAt, List<ProjectExport> projects) {
}
