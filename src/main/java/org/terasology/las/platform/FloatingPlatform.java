// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.las.platform;

import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.Rect2i;

public class FloatingPlatform {
    private final Rect2i area;
    private final int baseHeight;
    private final Region3i redTeleporterRegion;
    private final Region3i blackTeleporterRegion;

    public FloatingPlatform(Rect2i area, int baseHeight, Region3i redTeleporterRegion, Region3i blackTeleporterRegion) {
        this.area = area;
        this.redTeleporterRegion = redTeleporterRegion;
        this.blackTeleporterRegion = blackTeleporterRegion;
        this.baseHeight = baseHeight;
    }

    public Rect2i getArea() {
        return area;
    }

    public int getBaseHeight() {
        return baseHeight;
    }

    public Region3i getRedTeleporterRegion() {
        return redTeleporterRegion;
    }

    public Region3i getBlackTeleporterRegion() {
        return blackTeleporterRegion;
    }
}
