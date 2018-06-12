/*
 * Copyright 2015 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.terasology.las.platform;

import org.terasology.math.Region3i;
import org.terasology.math.geom.Rect2i;

public class FloatingPlatform {
    private Rect2i area;
    private int baseHeight;
    private Region3i redTeleporterRegion;
    private Region3i blackTeleporterRegion;

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
