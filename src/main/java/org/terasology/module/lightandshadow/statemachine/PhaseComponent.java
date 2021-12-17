// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.module.lightandshadow.statemachine;

import org.terasology.gestalt.entitysystem.component.Component;

public interface PhaseComponent extends Component<PhaseComponent> {

    public PhaseSystem.Phase toPhase();
}
