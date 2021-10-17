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

package com.faendir.zachtronics.bot.discord.command

import discord4j.core.event.domain.interaction.InteractionCreateEvent
import discord4j.core.spec.InteractionReplyEditSpec
import reactor.core.publisher.Mono

abstract class AbstractSubCommand<T> : SubCommand<T> {
    override val data by lazy {
        buildData()
    }

    override fun handle(event: InteractionCreateEvent, parameters: T): Mono<Void> {
        return event.editReply(handle(parameters)).then()
    }

    open fun handle(parameters: T): InteractionReplyEditSpec {
        return InteractionReplyEditSpec.builder().contentOrNull("Not yet implemented").build()
    }
}