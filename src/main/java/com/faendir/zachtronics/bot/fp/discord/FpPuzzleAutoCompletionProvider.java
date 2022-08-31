/*
 * Copyright (c) 2022
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

package com.faendir.zachtronics.bot.fp.discord;

import com.faendir.discord4j.command.parse.AutoCompletionProvider;
import com.faendir.zachtronics.bot.fp.model.FpPuzzle;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

class FpPuzzleAutoCompletionProvider implements AutoCompletionProvider {
    private static final List<FpPuzzle> list = Arrays.asList(FpPuzzle.values());

    @NotNull
    @Override
    public List<String> autoComplete(@NotNull String partial) {
        return UtilsKt.fuzzyMatch(list, partial, FpPuzzle::getDisplayName).stream()
                      .map(FpPuzzle::getDisplayName)
                      .toList();
    }
}