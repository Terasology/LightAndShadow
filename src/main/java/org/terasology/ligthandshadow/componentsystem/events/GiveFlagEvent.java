// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;


public class GiveFlagEvent implements Event {
    public EntityRef flag;

    public GiveFlagEvent(EntityRef flag) {
        this.flag = flag;
    }
}