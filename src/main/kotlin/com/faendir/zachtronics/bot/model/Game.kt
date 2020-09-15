package com.faendir.zachtronics.bot.model

interface Game<S: Score<S, *>, P: Puzzle> {
    val discordChannel: String

    fun findPuzzleByName(name: String) : List<P>

    fun parseScore(puzzle: P, string: String) : S?
}