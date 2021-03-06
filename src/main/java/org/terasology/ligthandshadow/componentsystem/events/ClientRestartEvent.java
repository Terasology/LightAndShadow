// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

/**
 *  Notification Event to inform a client that restart is complete.
 */
@OwnerEvent
public class ClientRestartEvent implements Event {
}
