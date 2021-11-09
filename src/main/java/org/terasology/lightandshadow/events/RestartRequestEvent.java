// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.ServerEvent;

/**
 *  Trigger event to request game restart by a client.
 */
@ServerEvent
public class RestartRequestEvent implements Event {
}
