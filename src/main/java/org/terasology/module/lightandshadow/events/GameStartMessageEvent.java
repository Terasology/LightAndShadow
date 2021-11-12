// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

/**
 * Notification event that indicates the pregame phase has started so that the pregame message can be displayed.
 */
@OwnerEvent
public class GameStartMessageEvent implements Event {
}
