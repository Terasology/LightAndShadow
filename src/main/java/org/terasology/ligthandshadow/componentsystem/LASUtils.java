/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.ligthandshadow.componentsystem;

import org.terasology.math.geom.Vector3i;

public final class LASUtils {
    public static final String BLACK_FLAG_URI = "LightAndShadowResources:blackFlag";
    public static final String RED_FLAG_URI = "LightAndShadowResources:redFlag";
    public static final String RED_TEAM = "red";
    public static final String BLACK_TEAM = "black";
    /** Position of Red base */
    public static final Vector3i CENTER_RED_BASE_POSITION = new Vector3i(30, 10, 0);
    /** Position of Black base */
    public static final Vector3i CENTER_BLACK_BASE_POSITION = new Vector3i(-30, 10, 0);
}
