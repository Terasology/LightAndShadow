// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

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
