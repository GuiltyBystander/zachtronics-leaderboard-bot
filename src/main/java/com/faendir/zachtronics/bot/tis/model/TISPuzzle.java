/*
 * Copyright (c) 2024
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.faendir.zachtronics.bot.tis.model;

import com.faendir.zachtronics.bot.model.Puzzle;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.faendir.zachtronics.bot.tis.model.TISType.*;

@Getter
public enum TISPuzzle implements Puzzle<TISCategory> {
    SELF_TEST_DIAGNOSTIC(TISGroup.TIS_100_SEGMENT_MAP, "00150", "SELF-TEST DIAGNOSTIC", WITH_ACHIEVEMENT),
    SIGNAL_AMPLIFIER(TISGroup.TIS_100_SEGMENT_MAP, "10981", "SIGNAL AMPLIFIER", STANDARD),
    DIFFERENTIAL_CONVERTER(TISGroup.TIS_100_SEGMENT_MAP, "20176", "DIFFERENTIAL CONVERTER", STANDARD),
    SIGNAL_COMPARATOR(TISGroup.TIS_100_SEGMENT_MAP, "21340", "SIGNAL COMPARATOR", WITH_ACHIEVEMENT),
    SIGNAL_MULTIPLEXER(TISGroup.TIS_100_SEGMENT_MAP, "22280", "SIGNAL MULTIPLEXER", STANDARD),
    SEQUENCE_GENERATOR(TISGroup.TIS_100_SEGMENT_MAP, "30647", "SEQUENCE GENERATOR", STANDARD),
    SEQUENCE_COUNTER(TISGroup.TIS_100_SEGMENT_MAP, "31904", "SEQUENCE COUNTER", STANDARD),
    SIGNAL_EDGE_DETECTOR(TISGroup.TIS_100_SEGMENT_MAP, "32050", "SIGNAL EDGE DETECTOR", STANDARD),
    INTERRUPT_HANDLER(TISGroup.TIS_100_SEGMENT_MAP, "33762", "INTERRUPT HANDLER", STANDARD),
    SIMPLE_SANDBOX(TISGroup.TIS_100_SEGMENT_MAP, "USEG0", "SIMPLE SANDBOX", SANDBOX),
    SIGNAL_PATTERN_DETECTOR(TISGroup.TIS_100_SEGMENT_MAP, "40196", "SIGNAL PATTERN DETECTOR", STANDARD),
    SEQUENCE_PEAK_DETECTOR(TISGroup.TIS_100_SEGMENT_MAP, "41427", "SEQUENCE PEAK DETECTOR", STANDARD),
    SEQUENCE_REVERSER(TISGroup.TIS_100_SEGMENT_MAP, "42656", "SEQUENCE REVERSER", WITH_ACHIEVEMENT),
    SIGNAL_MULTIPLIER(TISGroup.TIS_100_SEGMENT_MAP, "43786", "SIGNAL MULTIPLIER", STANDARD),
    STACK_MEMORY_SANDBOX(TISGroup.TIS_100_SEGMENT_MAP, "USEG1", "STACK MEMORY SANDBOX", SANDBOX),
    IMAGE_TEST_PATTERN_1(TISGroup.TIS_100_SEGMENT_MAP, "50370", "IMAGE TEST PATTERN 1", FIXED_IMAGE),
    IMAGE_TEST_PATTERN_2(TISGroup.TIS_100_SEGMENT_MAP, "51781", "IMAGE TEST PATTERN 2", FIXED_IMAGE),
    EXPOSURE_MASK_VIEWER(TISGroup.TIS_100_SEGMENT_MAP, "52544", "EXPOSURE MASK VIEWER", STANDARD),
    HISTOGRAM_VIEWER(TISGroup.TIS_100_SEGMENT_MAP, "53897", "HISTOGRAM VIEWER", STANDARD),
    IMAGE_CONSOLE_SANDBOX(TISGroup.TIS_100_SEGMENT_MAP, "USEG2", "IMAGE CONSOLE SANDBOX", SANDBOX),
    SIGNAL_WINDOW_FILTER(TISGroup.TIS_100_SEGMENT_MAP, "60099", "SIGNAL WINDOW FILTER", STANDARD),
    SIGNAL_DIVIDER(TISGroup.TIS_100_SEGMENT_MAP, "61212", "SIGNAL DIVIDER", STANDARD),
    SEQUENCE_INDEXER(TISGroup.TIS_100_SEGMENT_MAP, "62711", "SEQUENCE INDEXER", STANDARD),
    SEQUENCE_SORTER(TISGroup.TIS_100_SEGMENT_MAP, "63534", "SEQUENCE SORTER", STANDARD),
    STORED_IMAGE_DECODER(TISGroup.TIS_100_SEGMENT_MAP, "70601", "STORED IMAGE DECODER", STANDARD),

    UNKNOWN(TISGroup.TIS_100_SEGMENT_MAP, "UNKNOWN", "UNKNOWN", STANDARD),

    SEQUENCE_MERGER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.00.526.6", "SEQUENCE MERGER", STANDARD),
    INTEGER_SERIES_CALCULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.01.874.8", "INTEGER SERIES CALCULATOR", STANDARD),
    SEQUENCE_RANGE_LIMITER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.02.981.2", "SEQUENCE RANGE LIMITER", STANDARD),
    SIGNAL_ERROR_CORRECTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.03.176.9", "SIGNAL ERROR CORRECTOR", STANDARD),
    SUBSEQUENCE_EXTRACTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.04.340.5", "SUBSEQUENCE EXTRACTOR", STANDARD),
    SIGNAL_PRESCALER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.05.647.1", "SIGNAL PRESCALER", STANDARD, 129449),
    SIGNAL_AVERAGER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.06.786.0", "SIGNAL AVERAGER", STANDARD),
    SUBMAXIMUM_SELECTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.07.050.0", "SUBMAXIMUM SELECTOR", STANDARD),
    DECIMAL_DECOMPOSER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.08.633.9", "DECIMAL DECOMPOSER", STANDARD),
    SEQUENCE_MODE_CALCULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.09.904.9", "SEQUENCE MODE CALCULATOR", STANDARD),
    SEQUENCE_NORMALIZER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.10.656.5", "SEQUENCE NORMALIZER", STANDARD),
    IMAGE_TEST_PATTERN_3(TISGroup.TIS_NET_DIRECTORY, "NEXUS.11.711.2", "IMAGE TEST PATTERN 3", FIXED_IMAGE),
    IMAGE_TEST_PATTERN_4(TISGroup.TIS_NET_DIRECTORY, "NEXUS.12.534.4", "IMAGE TEST PATTERN 4", FIXED_IMAGE),
    SPATIAL_PATH_VIEWER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.13.370.9", "SPATIAL PATH VIEWER", STANDARD),
    CHARACTER_TERMINAL(TISGroup.TIS_NET_DIRECTORY, "NEXUS.14.781.3", "CHARACTER TERMINAL", STANDARD),
    BACK_REFERENCE_REIFIER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.15.897.9", "BACK-REFERENCE REIFIER", STANDARD),
    DYNAMIC_PATTERN_DETECTOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.16.212.8", "DYNAMIC PATTERN DETECTOR", STANDARD),
    SEQUENCE_GAP_INTERPOLATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.17.135.0", "SEQUENCE GAP INTERPOLATOR", STANDARD),
    DECIMAL_TO_OCTAL_CONVERTER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.18.427.7", "DECIMAL TO OCTAL CONVERTER", STANDARD),
    PROLONGED_SEQUENCE_SORTER(TISGroup.TIS_NET_DIRECTORY, "NEXUS.19.762.9", "PROLONGED SEQUENCE SORTER", STANDARD),
    PRIME_FACTOR_CALCULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.20.433.1", "PRIME FACTOR CALCULATOR", STANDARD),
    SIGNAL_EXPONENTIATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.21.601.6", "SIGNAL EXPONENTIATOR", STANDARD),
    T20_NODE_EMULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.22.280.8", "T20 NODE EMULATOR", STANDARD),
    T31_NODE_EMULATOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.23.727.9", "T31 NODE EMULATOR", STANDARD),
    WAVE_COLLAPSE_SUPERVISOR(TISGroup.TIS_NET_DIRECTORY, "NEXUS.24.511.7", "WAVE COLLAPSE SUPERVISOR", STANDARD);

    private final TISGroup group;
    private final String id;
    private final String displayName;
    private final TISType type;
    private final @Nullable String achievement;
    private final List<TISCategory> supportedCategories;
    private final int[] extraWitnessSeeds;
    private final String link;

    TISPuzzle(@NotNull TISGroup group, @NotNull String id, @NotNull String displayName, @NotNull TISType type, int... extraWitnessSeeds) {
        this.group = group;
        this.id = id;
        this.displayName = displayName;
        this.achievement = switch (id) { // sadly `this` doesn't exist yet to switch on
            case "00150" -> "BUSY_LOOP";
            case "21340" -> "UNCONDITIONAL";
            /* case "31904" -> "NO_BACKUP"; trivial, not tracked */
            case "42656" -> "NO_MEMORY";
            default -> null;
        };
        this.type = type;
        this.supportedCategories = switch (type) {
            case STANDARD -> Arrays.stream(TISCategory.values())
                                   .filter(c -> c.getAdmission() != TISMetric.ACHIEVEMENT)
                                   .toList();
            case WITH_ACHIEVEMENT -> List.of(TISCategory.values());
            case FIXED_IMAGE -> Arrays.stream(TISCategory.values())
                                      .filter(c -> c.getAdmission() == TISMetric.NOT_CHEATING)
                                      .toList();
            case SANDBOX -> Collections.emptyList();
        };
        this.extraWitnessSeeds = extraWitnessSeeds;
        this.link = "https://zlbb.faendir.com/tis/" + id;
    }

}
