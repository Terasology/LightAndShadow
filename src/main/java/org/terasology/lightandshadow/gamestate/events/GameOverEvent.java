// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.lightandshadow.gamestate.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.OwnerEvent;

/**
 * Event to indicate clients about GameOver.
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
