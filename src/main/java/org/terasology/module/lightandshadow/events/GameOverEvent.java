// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.module.lightandshadow.events;

import org.terasology.engine.network.OwnerEvent;
import org.terasology.gestalt.entitysystem.event.Event;

/**
 * Notification Event to inform clients that the game is over.
 */
@OwnerEvent
public class GameOverEvent implements Event {
    public String winningTeam;
    public int blackTeamScore;
    public int redTeamScore;

    public GameOverEvent() {
    }

    public GameOverEvent(String winningTeam, int blackTeamScore, int redTeamScore) {
        this.winningTeam = winningTeam;
        this.blackTeamScore = blackTeamScore;
        this.redTeamScore = redTeamScore;
    }
}
