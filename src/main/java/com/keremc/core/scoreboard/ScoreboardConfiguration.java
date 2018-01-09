package com.keremc.core.scoreboard;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Scoreboard Configuration class. This class can be used to
 * create scoreboard objects. This configuration object provides
 * the title/scores, along with some other settings. This should be passed to
 * CoreScoreboardHandler#setConfiguration.
 */
@NoArgsConstructor
public final class ScoreboardConfiguration {

    @Getter @Setter private TitleGetter titleGetter;
    @Getter @Setter private ScoreGetter scoreGetter;

}