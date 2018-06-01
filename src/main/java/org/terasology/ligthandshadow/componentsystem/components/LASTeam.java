/*
 * Copyright 2014 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.ligthandshadow.componentsystem.components;

import org.terasology.entitySystem.Component;

/**
 * Created by synopia on 25.01.14.
 */
public final class LASTeam implements Component {
    public String team;

    public LASTeam() {
    }

    public LASTeam(String team) {
        this.team = team;
    }
    public void setTeam(String team) {
        this.team = team;
    }
}
