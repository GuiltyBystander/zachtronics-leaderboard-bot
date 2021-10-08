/*
 * Copyright (c) 2021
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

package com.faendir.zachtronics.bot.sz.discord;

import com.faendir.discord4j.command.annotation.ApplicationCommand;
import com.faendir.discord4j.command.annotation.Converter;
import com.faendir.zachtronics.bot.discord.LinkConverter;
import com.faendir.zachtronics.bot.discord.command.AbstractArchiveCommand;
import com.faendir.zachtronics.bot.sz.SzQualifier;
import com.faendir.zachtronics.bot.sz.archive.SzArchive;
import com.faendir.zachtronics.bot.sz.model.SzSolution;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Component
@SzQualifier
public class SzArchiveCommand extends AbstractArchiveCommand<SzSolution> implements SzSecured {
    @Getter
    private final SzArchive archive;

    @NotNull
    @Override
    public List<SzSolution> parseSolutions(@NotNull ChatInputInteractionEvent interaction) {
        Data data = SzArchiveCommand$DataParser.parse(interaction);
        SzSolution solution;
        try (InputStream is = new URL(data.link).openStream()) {
            String content = new String(is.readAllBytes());
            solution = new SzSolution(content);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Could not parse your link");
        } catch (IOException e) {
            throw new IllegalArgumentException("Couldn't read your solution");
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Could not parse a valid solution");
        }
        return Collections.singletonList(solution);
    }

    @NotNull
    @Override
    public ApplicationCommandOptionData buildData() {
        return SzArchiveCommand$DataParser.buildData();
    }

    @ApplicationCommand(name = "archive", subCommand = true)
    @Value
    public static class Data {
        @NotNull
        String link;

        public Data(@NotNull @Converter(LinkConverter.class) String link) {
            this.link = link;
        }
    }
}
