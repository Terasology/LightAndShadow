// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.flag.components;

import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.network.Replicate;

/**
 * This is attached to player entities to indicate whether or not they have a
 * flag in their inventory
 * String flag indicates the team of the flag being held
 */

public class HasFlagComponent implements Component {
    @Replicate
    public String flag;
}
