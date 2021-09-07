// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.ligthandshadow.componentsystem.events;

import org.terasology.engine.network.BroadcastEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Notification event to indicate that the score has been updated.
 */
@BroadcastEvent
public class ScoreUpdateFromServerEvent implements Event {
    public String team;
    public int score;

    public ScoreUpdateFromServerEvent() {
    }

    public ScoreUpdateFromServerEvent(String team, int score) {
        this.team = team;
        this.score = score;
    }
}
