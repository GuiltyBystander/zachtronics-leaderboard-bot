package com.faendir.zachtronics.bot.sz.model;

import com.faendir.zachtronics.bot.generic.discord.Command;
import com.faendir.zachtronics.bot.model.Leaderboard;
import com.faendir.zachtronics.bot.model.Game;
import com.faendir.zachtronics.bot.utils.UtilsKt;
import discord4j.core.object.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShenzhenIO implements Game {
    @Getter
    private final String discordChannel = "shenzhen-io";
    @Getter
    private final String displayName = "Shenzhen I/O";
    @Getter
    private final String commandName = "sz";
    @Getter
    private final List<Leaderboard<SzCategory, SzPuzzle, SzRecord>> leaderboards;
    @Getter
    private final List<Command> commands;
    @Getter
    private final String submissionSyntax = "<puzzle> (<cost/power/lines>) by <author> <link>";
    @Getter
    private final String categoryHelp = "Categories are named by Primary metric, then Secondary.\n" +
                                        "Ties in both are broken by the remaining metric.\n" +
                                        "\n" +
                                        "All categories are:\n" +
                                        Arrays.stream(SzCategory.values())
                                              .map(c -> c.getDisplayName() + " (" + c.getContentDescription() + ")")
                                              .collect(Collectors.joining("\n", "```", "```"));
    @Getter
    private final String scoreHelp = "Scores are in the format cost/power/lines.";

    private static final Pattern SUBMISSION_REGEX = Pattern.compile(
            "!submit\\s+" +
            "(?<puzzle>.+)\\s+" +
            "\\(" + SzScore.REGEX_SIMPLE_SCORE + "\\)\\s+" +
            "(?:by\\s+)?(?<author>.+?)?\\s+" +
            "(?<link>\\S+)\\s*",
            Pattern.CASE_INSENSITIVE);

/*
    @NotNull
    public Result<Pair<SzPuzzle, SzRecord>> parseSubmission(@NotNull Message message) {
        Matcher m = SUBMISSION_REGEX.matcher(message.getContentRaw());
        if (!m.matches())
            return Result.parseFailure("couldn't parse request");

        return parsePuzzle(m.group("puzzle")).flatMap(puzzle -> {
            SzScore score = SzScore.parseSimpleScore(m);
            SzRecord record = new SzRecord(score, m.group("author"), m.group("link"));
            return Result.success(new Pair<>(puzzle, record));
        });
    }*/

    @NotNull
    public List<SzCategory> parseCategory(@NotNull String name) {
        return Arrays.stream(SzCategory.values()).filter(c -> c.getDisplayName().equalsIgnoreCase(name))
                     .collect(Collectors.toList());
    }

    @NotNull
    public static SzPuzzle parsePuzzle(@NotNull String name) {
        return UtilsKt.getSingleMatchingPuzzle(SzPuzzle.values(), name);
    }

    private static final long WIKI_ADMIN = 295868901042946048L; // 12345ieee
    @Override
    public Mono<Boolean> hasWritePermission(@Nullable User user) {
        if (user == null)
            return Mono.just(false);
        return Mono.just(user.getId().asLong() == WIKI_ADMIN);
    }
}