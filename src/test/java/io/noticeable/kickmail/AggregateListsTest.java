package io.noticeable.kickmail;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AggregateListsTest {

    @Test
    void localDenyListIsAddedAndAllowListRemovesEvenUpstreamDomains() {
        final TreeSet<String> result = AggregateLists.aggregate(
                List.of(Set.of("a.com", "false-positive.com"), Set.of("b.com")),
                Set.of("kya2.com"),
                Set.of("false-positive.com"));

        assertEquals(new TreeSet<>(Set.of("a.com", "b.com", "kya2.com")), result);
    }

    @Test
    void allowListWinsOverLocalDenyList() {
        final TreeSet<String> result = AggregateLists.aggregate(
                List.of(), Set.of("x.com"), Set.of("x.com"));

        assertTrue(result.isEmpty());
    }

    @Test
    void parserSkipsCommentsBlankLinesAndLowerCases() throws IOException {
        final Set<String> parsed = ListFetcher.parse(new BufferedReader(new StringReader(
                "# header\n\n  KYA2.com  \nbccto.cc # trailing comment\n   \n")));

        assertEquals(Set.of("kya2.com", "bccto.cc"), parsed);
    }

    @Test
    void missingLocalListIsEmpty(@TempDir final Path dir) throws IOException {
        assertTrue(new ListFetcher().read(dir.resolve("nope.txt")).isEmpty());
    }

    @Test
    void repoListsParse() throws IOException {
        final Path lists = Path.of("lists");
        final ListFetcher fetcher = new ListFetcher();

        assertTrue(Files.exists(lists.resolve(AggregateLists.LOCAL_DENY_LIST_FILE)));
        assertTrue(Files.exists(lists.resolve(AggregateLists.LOCAL_ALLOW_LIST_FILE)));
        assertTrue(fetcher.read(lists.resolve(AggregateLists.LOCAL_DENY_LIST_FILE)).contains("kya2.com"));
    }

}
