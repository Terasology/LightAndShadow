// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

/**
 * This trigger event is responsible for sending the flag back to the base.
 */
public class ReturnFlagEvent implements Event {
    private final EntityRef flag;

    public ReturnFlagEvent(EntityRef flag) {
        this.flag = flag;
    }

    public EntityRef getFlag() {
        return flag;
    }

}
