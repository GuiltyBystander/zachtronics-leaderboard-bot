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

package com.faendir.zachtronics.bot.tis.repository;

import com.faendir.zachtronics.bot.BotTest;
import com.faendir.zachtronics.bot.repository.CategoryRecord;
import com.faendir.zachtronics.bot.tis.model.TISCategory;
import com.faendir.zachtronics.bot.tis.model.TISPuzzle;
import com.faendir.zachtronics.bot.tis.model.TISRecord;
import com.faendir.zachtronics.bot.tis.model.TISSubmission;
import com.faendir.zachtronics.bot.validation.ValidationException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

@BotTest
@Disabled("Massive tests only for manual testing or migrations")
class TISManualTest {

    @Autowired
    private TISSolutionRepository repository;

    @Test
    public void testFullIO() throws IOException {
        /*
        cp -a ../tis100/leaderboard/* src/test/resources/repositories/tis-leaderboard/
         */
        for (TISPuzzle p : repository.getTrackedPuzzles()) {

            Iterable<TISRecord> records = repository.findCategoryHolders(p, true).stream()
                                                    .map(CategoryRecord::getRecord)
                    ::iterator;
            for (TISRecord r : records) {
                if (r.getDataPath() != null && Files.exists(r.getDataPath())) {
                    TISSubmission submission = new TISSubmission(p, r.getScore(), r.getAuthor(), r.getDisplayLink(),
                                                                 Files.readString(r.getDataPath()));
                    repository.submit(submission);
                }
            }

            System.out.println("Done " + p.getDisplayName());
        }

        /*
        rm -r src/test/resources/repositories/tis-leaderboard/
        git restore --source=HEAD --staged --worktree -- src/test/resources/repositories/tis-leaderboard/
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/tis-leaderboard* | head -n1)/* ../tis100/leaderboard/
         */

        System.out.println("Done");
    }

    @Test
    public void rebuildAllWiki() {
        repository.rebuildRedditLeaderboard(null);
        String page = repository.getRedditService().getWikiPage(repository.getSubreddit(), repository.wikiPageName(null))
                                .replaceAll("file:/tmp/tis-leaderboard[0-9]+/",
                                            "https://raw.githubusercontent.com/12345ieee/tis100-leaderboard/master");
        System.out.println(page);
    }

    @Test
    public void tagNewCategories() throws IOException {
        Path repoPath = Paths.get("../tis100/leaderboard");

        for (TISPuzzle puzzle : repository.getTrackedPuzzles()) {
            Path puzzlePath = repoPath.resolve(repository.relativePuzzlePath(puzzle));
            List<TISSolution> solutions = repository.unmarshalSolutions(puzzlePath);
            if (solutions.isEmpty())
                continue;

            solutions.stream().map(TISSolution::getCategories).forEach(Set::clear);
            for (TISCategory category : puzzle.getSupportedCategories()) {
                solutions.stream()
                         .filter(s -> category.supportsScore(s.getScore()))
                         .min(Comparator.comparing(TISSolution::getScore, category.getScoreComparator()))
                         .ifPresent(s -> s.getCategories().add(category));
            }
            repository.marshalSolutions(solutions, puzzlePath);
        }
    }

    @Test
    public void submitSaveFolder() throws IOException {
        Path savesRoot = Paths.get("../tis100/saves");
//        List<String> authors = Files.list(savesRoot).filter(Files::isDirectory).map(p -> p.getFileName().toString()).toList();
        List<String> authors = List.of("12345ieee");

        /*
        cp -a ../tis100/leaderboard/* src/test/resources/repositories/tis-leaderboard/
         */

        for (String author : authors) {
            Path savesPath = savesRoot.resolve(author).resolve("save");
//            if (!Files.exists(savesPath)) // it's in a subfolder called `random numbers`
//                savesPath = Files.list(savesPath.getParent())
//                                 .filter(Files::isDirectory)
//                                 .filter(p -> p.getFileName().toString().matches("\\d+"))
//                                 .findFirst().orElseThrow()
//                                 .resolve("save");

            for (TISPuzzle puzzle : repository.getTrackedPuzzles()) {
                try (DirectoryStream<Path> paths = Files.newDirectoryStream(savesPath, puzzle.getId() + "*.*txt")) {
                    for (Path path : paths) {
                        boolean cheating = path.getFileName().toString().matches(puzzle.getId() + ".+c.+txt");
                        String data = Files.readString(path).replace("\r\n", "\n");
                        TISSubmission submission;
                        try {
                            submission = TISSubmission.fromData(data, puzzle, cheating, author, "http://li.nk");
                        }
                        catch (Exception e) {
                            if (e instanceof ValidationException) {
                                String message = e.getMessage();
                                if (message.startsWith("ERROR: failed with exception:") ||
                                    message.contains("validation failure for output"))
                                    continue;
                            }
                            System.err.println(path);
                            throw e;
                        }

                        System.out.println(repository.submit(submission));
                    }
                }
            }
        }

        /*
        rm -r src/test/resources/repositories/tis-leaderboard/*
        git restore --source=HEAD --staged --worktree -- src/test/resources/repositories/tis-leaderboard/
        rsync -a --delete --exclude=README.txt $(ls -1dt /tmp/tis-leaderboard* | head -n1)/* ../tis100/leaderboard/
         */

        System.out.println("Done");
    }
}