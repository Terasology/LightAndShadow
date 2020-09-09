// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.las.bases;

import org.terasology.engine.math.Region3i;

public class Base {
    private final Region3i area;
    private final Region3i flagArea;

    public Base(Region3i baseRegion, Region3i flagRegion) {
        this.area = baseRegion;
        this.flagArea = flagRegion;
    }

    public Region3i getArea() {
        return area;
    }

    public Region3i getFlagArea() {
        return flagArea;
    }
}
