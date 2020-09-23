package com.faendir.zachtronics.bot.model

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.utils.Result
import net.dv8tion.jda.api.entities.Message

interface Game<C : Category<C, S, P>, S : Score, P : Puzzle, R : Record<S>> {
    val discordChannel: String

    val leaderboards: List<Leaderboard<C, S, P, R>>

    val submissionSyntax : String

    fun parseSubmission(message: Message): Result<Pair<List<P>, R>>

    fun parseCategory(name: String): List<C>

    fun parsePuzzle(name: String): List<P>

    fun parseScore(puzzle: P, string: String): S?
}