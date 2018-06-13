/*
 * Copyright 2017 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.las.UI;

import org.terasology.engine.Time;
import org.terasology.registry.In;
import org.terasology.rendering.nui.layers.hud.CoreHudWidget;
import org.terasology.rendering.nui.widgets.UIText;

public class ScoreHud extends CoreHudWidget {
    public int redScore;
    public int blackScore;

    private UIText scoreArea;

    @In
    private Time time;

    @Override
    public void initialise() {
        scoreArea = find("scoreArea", UIText.class);
        String scoreText = "Red Team: " + redScore + "        " +
                "Black Team: " + blackScore;
        scoreArea.setText(String.format(scoreText));
    }
}
