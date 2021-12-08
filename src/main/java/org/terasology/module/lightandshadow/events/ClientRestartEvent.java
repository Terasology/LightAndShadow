// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.events;

import org.terasology.engine.network.OwnerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 *  Notification Event to inform a client that restart is complete.
 */
@OwnerEvent
public class ClientRestartEvent implements Event {
}
