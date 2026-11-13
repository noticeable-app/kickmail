package io.noticeable.kickmail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Builds the published deny list: the union of the upstream sources, plus the
 * domains in {@code <listsDir>/denylist.txt}, minus the domains in
 * {@code <listsDir>/allowlist.txt}, written in the format of {@link DomainList}
 * (an entry also denies its subdomains, except public suffixes).
 *
 * <p>Usage: {@code AggregateLists [listsDir] [outputFile]}, defaulting to
 * {@code lists} and {@code denylist.txt} relative to the working directory.
 */
public class AggregateLists {

    private static final String DENY_LIST_SOURCE1_URL = "https://raw.githubusercontent.com/disposable-email-domains/disposable-email-domains/refs/heads/main/disposable_email_blocklist.conf";

    private static final String DENY_LIST_SOURCE2_URL = "https://raw.githubusercontent.com/7c/fakefilter/main/txt/data.txt";

    private static final String DENY_LIST_SOURCE3_URL = "https://raw.githubusercontent.com/FGRibreau/mailchecker/master/list.txt";

    static final String LOCAL_DENY_LIST_FILE = "denylist.txt";

    static final String LOCAL_ALLOW_LIST_FILE = "allowlist.txt";


    public static void main(String[] args) throws IOException {
        final Path listsDir = Path.of(args.length > 0 ? args[0] : "lists");
        final Path output = Path.of(args.length > 1 ? args[1] : "denylist.txt");

        final ListFetcher listFetcher = new ListFetcher();

        final List<Set<String>> upstream = List.of(
                listFetcher.fetch(DENY_LIST_SOURCE1_URL),
                listFetcher.fetch(DENY_LIST_SOURCE2_URL),
                listFetcher.fetch(DENY_LIST_SOURCE3_URL));
        final Set<String> localDeny = listFetcher.read(listsDir.resolve(LOCAL_DENY_LIST_FILE));
        final Set<String> localAllow = listFetcher.read(listsDir.resolve(LOCAL_ALLOW_LIST_FILE));

        final TreeSet<String> denyList = aggregate(upstream, localDeny, localAllow);
        final TreeSet<String> published = publish(denyList, localAllow, PublicSuffixes.fetch());

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        try (final BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (final String line : published) {
                writer.write(line);
                writer.newLine();
            }
        }

        System.out.printf("%d domains written to %s (upstream %d, local +%d -%d; %d exact only, %d exceptions)%n",
                denyList.size(), output,
                upstream.stream().mapToInt(Set::size).sum(), localDeny.size(), localAllow.size(),
                published.stream().filter(line -> line.startsWith("=")).count(),
                published.stream().filter(line -> line.startsWith("!")).count());
    }

    /**
     * The lines of the published list (format in {@link DomainList}): a deny entry denies its
     * subdomains too, except the entries that are public suffixes, published as {@code =domain}
     * (denied as written only); an allow-list entry under a denied parent is published as
     * {@code !domain} so that it stays allowed.
     */
    static TreeSet<String> publish(final Set<String> denyList, final Set<String> localAllow, final PublicSuffixes suffixes) {
        final TreeSet<String> lines = new TreeSet<>();
        final Set<String> parents = new java.util.HashSet<>();
        for (final String domain : denyList) {
            if (domain.startsWith("=")) {
                // written so in lists/denylist.txt: a domain to block without its subdomains
                lines.add(domain);
            } else if (suffixes.isSuffix(domain)) {
                lines.add("=" + domain);
            } else {
                lines.add(domain);
                parents.add(domain);
            }
        }
        for (final String domain : localAllow) {
            if (hasParentIn(domain, parents)) {
                lines.add("!" + domain);
            }
        }
        return lines;
    }

    private static boolean hasParentIn(final String domain, final Set<String> parents) {
        String current = domain;
        for (int dot = current.indexOf('.'); dot >= 0; dot = current.indexOf('.')) {
            current = current.substring(dot + 1);
            if (parents.contains(current)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Union of {@code upstream} and {@code localDeny}, minus {@code localAllow}.
     * The allow list wins over everything so a false positive can be fixed
     * without waiting for the upstream source.
     */
    static TreeSet<String> aggregate(final Collection<Set<String>> upstream,
                                     final Set<String> localDeny,
                                     final Set<String> localAllow) {
        final TreeSet<String> denyList = new TreeSet<>();
        upstream.forEach(denyList::addAll);
        denyList.addAll(localDeny);
        denyList.removeAll(localAllow);
        return denyList;
    }

}
