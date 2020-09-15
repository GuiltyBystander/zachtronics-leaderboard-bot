package com.faendir.zachtronics.bot.discord.commands

import com.faendir.zachtronics.bot.leaderboards.Leaderboard
import com.faendir.zachtronics.bot.leaderboards.UpdateResult
import com.faendir.zachtronics.bot.model.Category
import com.faendir.zachtronics.bot.model.Game
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.model.Score
import com.faendir.zachtronics.bot.utils.LeaderBoardCategoriesPair
import com.faendir.zachtronics.bot.utils.findCategories
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.entities.User
import org.springframework.stereotype.Component

@Component
class SubmitCommand : Command {
    override val regex = Regex("!submit\\s+(?<puzzle>[^:]*)(:|\\s)\\s*(?<score>[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?/[\\d.]+[a-zA-Z]?(/[\\d.]+[a-zA-Z]?)?)(\\s+(?<link>http.*\\.(gif|gifv|mp4|webm)))?\\s*")
    override val name: String = "submit"
    override val helpText: String = "!submit <puzzle>:<score1/score2/score3>(e.g. 3.5w/320c/400g) <link>(or attach file to message)"
    override val requiresRoles: List<String> = listOf("trusted-leaderboard-poster")

    override fun <S : Score<S, *>, P : Puzzle> handleMessage(game: Game<S, P>, leaderboards: List<Leaderboard<*, S, P>>, author: User, channel: TextChannel, message: Message,
                                                             command: MatchResult): String {
        return findPuzzle(game, command.groups["puzzle"]!!.value) { puzzle ->
            val scoreString = command.groups["score"]!!.value
            val score = game.parseScore(puzzle, scoreString) ?: return@findPuzzle "sorry, I couldn't parse your score ($scoreString)."
            val leaderboardCategories: List<LeaderBoardCategoriesPair<*, S, P>> = leaderboards.mapNotNull { it.findCategories(puzzle, score) }
            if (leaderboardCategories.isEmpty()) {
                return@findPuzzle "sorry, I could not find any category for ${score.parts.keys.joinToString("/") { it.key.toString() }}"
            }
            val link = command.groups["link"]?.value ?: message.attachments.firstOrNull()?.takeIf { listOf("gif", "gifv", "mp4", "webm").contains(it.fileExtension) }?.url
            ?: return@findPuzzle "sorry, I could not find a valid link or attachment in your message."
            val results = leaderboardCategories.map { pair -> getResult(pair, author, puzzle, score, link) }
            val successes = results.filterIsInstance<UpdateResult.Success<*, *>>()
            val pareto = results.filterIsInstance<UpdateResult.ParetoUpdate<*, *>>()
            val betterExists = results.filterIsInstance<UpdateResult.BetterExists<*, *>>()
            val brokenLink = results.filterIsInstance<UpdateResult.BrokenLink<*, *>>()
            val notSupported = results.filterIsInstance<UpdateResult.NotSupported<*, *>>()
            when {
                successes.isNotEmpty() -> "thanks, the site will be updated shortly with ${puzzle.displayName} ${
                    successes.flatMap { it.oldScores.keys }.map { it.displayName }
                } ${score.reorderToStandard().toString("/")} (previously ${
                    successes.flatMap { it.oldScores.entries }.joinToString { "`${it.key.displayName} ${it.value?.reorderToStandard()?.toString("/") ?: "none"}`" }
                })."
                pareto.isNotEmpty() -> "thanks, your submission for ${puzzle.displayName} (${score.reorderToStandard().toString("/")}) was included in the pareto frontier."
                betterExists.isNotEmpty() -> "sorry, your submission did not beat any of the existing scores for ${puzzle.displayName} ${
                    betterExists.flatMap { it.scores.entries }.joinToString { "`${it.key.displayName} ${it.value.reorderToStandard().toString("/")}`" }
                }"
                brokenLink.isNotEmpty() -> "sorry, I could not load the file at $link."
                notSupported.isNotEmpty() -> "sorry, submitting is not supported for this leaderboard."
                else -> "sorry, something went wrong."
            }
        }
    }

    private fun <C : Category<C, S, *>, S : Score<S, *>, P : Puzzle> getResult(pair: LeaderBoardCategoriesPair<C, S, P>, author: User, puzzle: P, score: S, link: String) =
        pair.leaderboard.update(author.name, puzzle, pair.categories.toList(), score, link)

}