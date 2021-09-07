// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.network.ServerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 *  Trigger event to request game restart by a client.
 */
@ServerEvent
public class RestartRequestEvent implements Event {
}
