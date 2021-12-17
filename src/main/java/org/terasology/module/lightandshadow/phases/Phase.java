// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.phases;

import org.terasology.gestalt.entitysystem.event.Event;

import java.util.function.Supplier;

public enum Phase {
    IDLE(OnIdlePhaseStartedEvent::new, OnIdlePhaseEndedEvent::new),
    PRE_GAME(OnPreGamePhaseStartedEvent::new, OnPreGamePhaseEndedEvent::new),
    COUNTDOWN(OnCountdownPhaseStartedEvent::new, OnCountdownPhaseEndedEvent::new),
    IN_GAME(OnInGamePhaseStartedEvent::new, OnInGamePhaseEndedEvent::new),
    POST_GAME(OnPostGamePhaseStartedEvent::new, OnPostGamePhaseEndedEvent::new);

    public final Supplier<Event> endEvent;
    public final Supplier<Event> startEvent;

    Phase(Supplier<Event> startEvent, Supplier<Event> endEvent) {
        this.startEvent = startEvent;
        this.endEvent = endEvent;
    }
}
