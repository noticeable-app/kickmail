package io.noticeable.kickmail;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainListTest {

    private final DomainList list = DomainList.of(Set.of(
            "rehearsalk.com", "sharklasers.com", "=ddns.net", "corp-farm.com", "!good.corp-farm.com"));

    @Test
    void anEntryDeniesItselfAndEverySubdomain() {
        assertTrue(list.isDenied("rehearsalk.com"));
        assertTrue(list.isDenied("aas.rehearsalk.com"));
        assertTrue(list.isDenied("a.b.rehearsalk.com"));
    }

    @Test
    void aDomainThatOnlyEndsLikeAnEntryIsNotDenied() {
        assertFalse(list.isDenied("notrehearsalk.com"));
        assertFalse(list.isDenied("rehearsalk.com.example.org"));
    }

    @Test
    void anExactOnlyEntryDeniesItselfButNotItsSubdomains() {
        assertTrue(list.isDenied("ddns.net"));
        assertFalse(list.isDenied("someone.ddns.net"));
    }

    @Test
    void anExceptionUnderADeniedParentStaysAllowedWithItsSubdomains() {
        assertTrue(list.isDenied("bad.corp-farm.com"));
        assertFalse(list.isDenied("good.corp-farm.com"));
        assertFalse(list.isDenied("mail.good.corp-farm.com"));
    }

    @Test
    void unlistedDomainsAndBareTopLevelDomainsAreNotDenied() {
        assertFalse(list.isDenied("acme.com"));
        assertFalse(list.isDenied("com"));
        assertFalse(DomainList.of(Set.of("com")).isDenied("acme.com"));
    }

    @Test
    void publicationMarksSuffixesExactOnlyAndKeepsAllowedSubdomainsOfDeniedParents() throws IOException {
        final PublicSuffixes suffixes = PublicSuffixes.parse(new StringReader(
                "// comment\ncom\nnet\nddns.net\n*.ck\n!www.ck\n"));
        final TreeSet<String> lines = AggregateLists.publish(
                Set.of("farm.com", "ddns.net", "kya2.com"),
                Set.of("good.farm.com", "unrelated.org"),
                suffixes);

        assertEquals(new TreeSet<>(Set.of("farm.com", "kya2.com", "=ddns.net", "!good.farm.com")), lines);
    }

    @Test
    void anExactOnlyEntryWrittenInTheLocalDenyListIsPublishedAsIsAndIsNoParent() throws IOException {
        final PublicSuffixes suffixes = PublicSuffixes.parse(new StringReader("com\n"));
        final TreeSet<String> lines = AggregateLists.publish(
                Set.of("=hosting.com"), Set.of("customer.hosting.com"), suffixes);

        assertEquals(new TreeSet<>(Set.of("=hosting.com")), lines);
    }

    @Test
    void publicSuffixRulesHandleWildcardsAndExceptions() throws IOException {
        final PublicSuffixes suffixes = PublicSuffixes.parse(new StringReader("com\n*.ck\n!www.ck\ngithub.io\n"));

        assertTrue(suffixes.isSuffix("com"));
        assertTrue(suffixes.isSuffix("github.io"));
        assertTrue(suffixes.isSuffix("anything.ck"));
        assertFalse(suffixes.isSuffix("www.ck"));
        assertFalse(suffixes.isSuffix("acme.com"));
    }

}
