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

package com.faendir.zachtronics.bot.om.repository

import com.faendir.zachtronics.bot.git.GitRepository
import com.faendir.zachtronics.bot.model.DisplayContext
import com.faendir.zachtronics.bot.model.Puzzle
import com.faendir.zachtronics.bot.om.model.OmCategory
import com.faendir.zachtronics.bot.om.model.OmPuzzle
import com.faendir.zachtronics.bot.om.model.OmRecord
import com.faendir.zachtronics.bot.om.model.OmScore
import com.faendir.zachtronics.bot.om.model.OmSubmission
import com.faendir.zachtronics.bot.repository.CategoryRecord
import com.faendir.zachtronics.bot.repository.SolutionRepository
import com.faendir.zachtronics.bot.repository.SubmitResult
import com.faendir.zachtronics.bot.utils.add
import com.faendir.zachtronics.bot.utils.ensurePrefix
import com.faendir.zachtronics.bot.utils.use
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.io.File
import javax.annotation.PostConstruct

@OptIn(ExperimentalSerializationApi::class)
@Component
class OmSolutionRepository(
    @Qualifier("omArchiveRepository") private val archive: GitRepository,
    @Qualifier("omGithubPagesLeaderboardRepository") private val leaderboard: GitRepository,
    private val pageGenerators: List<AbstractOmPageGenerator>
) : SolutionRepository<OmCategory, OmPuzzle, OmSubmission, OmRecord> {
    private val json = Json { prettyPrint = true }
    private lateinit var data: Map<Puzzle, MutableMap<OmRecord, MutableSet<OmCategory>>>
    private var hash: String? = null

    @PostConstruct
    fun init() {
        (leaderboard.acquireWriteAccess() to archive.acquireReadAccess()).use { leaderboardScope, archiveScope ->
            loadData(leaderboardScope, archiveScope)
            pageGenerators.forEach { it.update(leaderboardScope, OmCategory.values().toList(), data) }
            if (leaderboardScope.status().run { added.isNotEmpty() || changed.isNotEmpty() }) {
                leaderboardScope.commitAndPush("Update page formatting")
            }
        }
    }

    private fun loadData(leaderboardScope: GitRepository.ReadAccess, archiveScope: GitRepository.ReadAccess) {
        data = OmPuzzle.values().associateWith { mutableMapOf() }
        for (puzzle in OmPuzzle.values()) {
            val records = data.getValue(puzzle)
            leaderboardScope.getPuzzleDir(puzzle).takeIf { it.exists() }
                ?.listFiles()
                ?.map { file -> file.inputStream().buffered().use { json.decodeFromStream<OmRecord>(it) } }
                ?.forEach { record -> records.add(record.copy(
                    dataLink = record.dataPath?.let { archive.rawFilesUrl + it.toString().ensurePrefix("/") },
                    dataPath = record.dataPath?.let { archiveScope.repo.toPath().resolve(it) }
                ), mutableSetOf()) }
            if (records.isNotEmpty()) {
                for (category in OmCategory.values().filter { it.supportsPuzzle(puzzle) }.toMutableSet()) {
                    records.entries.filter { category.supportsScore(it.key.score) }
                        .reduceOrNull { a, b -> if (category.scoreComparator.compare(a.key.score, b.key.score) <= 0) a else b }
                        ?.value?.add(category)
                }
            }
        }
    }

    private fun loadDataIfNecessary(leaderboardScope: GitRepository.ReadAccess, archiveScope: GitRepository.ReadAccess) {
        val currentHash = leaderboardScope.currentHash()
        if (hash != currentHash) {
            loadData(leaderboardScope, archiveScope)
            hash = currentHash
        }
    }

    override fun submit(submission: OmSubmission): SubmitResult<OmRecord, OmCategory> =
        (leaderboard.acquireWriteAccess() to archive.acquireWriteAccess()).use { leaderboardScope, archiveScope ->
            loadDataIfNecessary(leaderboardScope, archiveScope)
            val records = data.getValue(submission.puzzle)
            val newRecord by lazy { submission.createRecord(archiveScope, leaderboardScope) }
            val unclaimedCategories = OmCategory.values().filter { it.supportsPuzzle(submission.puzzle) && it.supportsScore(submission.score) }.toMutableSet()
            val result = mutableListOf<CategoryRecord<OmRecord?, OmCategory>>()
            for ((record, categories) in records.toMap()) {
                if (submission.score == record.score) {
                    if (submission.displayLink != record.displayLink || record.dataLink == null) {
                        record.remove(archiveScope, leaderboardScope)
                        records.add(newRecord, categories)
                        unclaimedCategories -= categories
                        result.add(CategoryRecord(record, categories.toSet()))
                        continue
                    } else {
                        return@use SubmitResult.AlreadyPresent()
                    }
                }
                if (record.score.isStrictlyBetter(submission.score)) {
                    return@use SubmitResult.NothingBeaten(findCategoryHolders(submission.puzzle, includeFrontier = false))
                }
                if (categories.isEmpty()) {
                    if (submission.score.isStrictlyBetter(record.score)) {
                        record.remove(archiveScope, leaderboardScope)
                        records.add(newRecord, mutableSetOf())
                        result.add(CategoryRecord(record, emptySet()))
                    }
                } else {
                    unclaimedCategories -= categories
                    val beatenCategories = categories.filter { category ->
                        category.supportsScore(submission.score) && category.scoreComparator.compare(submission.score, record.score).let {
                            it < 0 || it == 0 && submission.displayLink != record.displayLink
                        }
                    }.toSet()
                    if (beatenCategories.isNotEmpty()) {
                        categories -= beatenCategories
                        if (categories.isEmpty() && submission.score.isStrictlyBetter(record.score)) {
                            record.remove(archiveScope, leaderboardScope)
                        }
                        records.add(newRecord, beatenCategories.toMutableSet())
                        result.add(CategoryRecord(record, beatenCategories))
                    }
                }
            }
            records.add(newRecord, unclaimedCategories)
            if (unclaimedCategories.isNotEmpty()) {
                result.add(CategoryRecord(null, unclaimedCategories))
            }
            pageGenerators.forEach { it.update(leaderboardScope, OmCategory.values().toList(), data) }
            archiveScope.commitAndPush(submission.author, submission.puzzle, submission.score, result.flatMap { it.categories }.map { it.toString() })
            leaderboardScope.commitAndPush(submission.author, submission.puzzle, submission.score, result.flatMap { it.categories }.map { it.toString() })
            hash = leaderboardScope.currentHash()
            SubmitResult.Success(null, result)
        }


    private fun OmSubmission.createRecord(archiveScope: GitRepository.ReadWriteAccess, leaderboardScope: GitRepository.ReadWriteAccess): OmRecord {
        val name = "${score.toFileString()}_${puzzle.name}"
        val archiveDir = archiveScope.getPuzzleDir(puzzle)
        archiveDir.mkdirs()
        val archiveFile = File(archiveDir, "$name.solution")
        archiveFile.writeBytes(data)
        archiveScope.add(archiveFile)
        val path = archiveFile.relativeTo(archiveScope.repo).toPath()

        val leaderboardDir = leaderboardScope.getPuzzleDir(puzzle)
        leaderboardDir.mkdirs()
        val leaderboardFile = File(leaderboardDir, "$name.json")
        val record = OmRecord(
            puzzle = puzzle,
            score = score,
            displayLink = displayLink,
            dataLink = archive.rawFilesUrl + path.toString().ensurePrefix("/"),
            dataPath = path,
        )
        leaderboardFile.outputStream().buffered().use { json.encodeToStream(record, it) }
        leaderboardScope.add(leaderboardFile)

        return record.copy(dataPath = archiveFile.toPath())
    }

    private fun OmRecord.remove(archiveScope: GitRepository.ReadWriteAccess, leaderboardScope: GitRepository.ReadWriteAccess) {
        dataPath?.let { dataPath -> archiveScope.rm(dataPath.toFile()) }
        leaderboardScope.rm(File(leaderboardScope.getPuzzleDir(puzzle), "${score.toFileString()}_${puzzle.name}.json"))
        data[puzzle]?.remove(this)
    }

    private fun GitRepository.ReadAccess.getPuzzleDir(puzzle: OmPuzzle): File = File(repo, "${puzzle.group.name}/${puzzle.name}")

    private fun OmScore.toFileString() = toDisplayString(DisplayContext.fileName())

    override fun find(puzzle: OmPuzzle, category: OmCategory): OmRecord? {
        (leaderboard.acquireReadAccess() to archive.acquireReadAccess()).use { l, a -> loadDataIfNecessary(l, a) }
        return data[puzzle]?.entries?.find { (_, categories) -> categories.contains(category) }?.key
    }

    override fun findCategoryHolders(puzzle: OmPuzzle, includeFrontier: Boolean): List<CategoryRecord<OmRecord, OmCategory>> {
        (leaderboard.acquireReadAccess() to archive.acquireReadAccess()).use { l, a -> loadDataIfNecessary(l, a) }
        return data[puzzle]?.entries?.filter { (_, categories) -> includeFrontier || categories.isNotEmpty() }
            ?.map { (record, categories) -> CategoryRecord(record, categories) } ?: emptyList()
    }
}