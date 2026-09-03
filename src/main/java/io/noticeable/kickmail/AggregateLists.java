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
 * {@code <listsDir>/allowlist.txt}.
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

        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }
        try (final BufferedWriter writer = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            for (final String domain : denyList) {
                writer.write(domain);
                writer.newLine();
            }
        }

        System.out.printf("%d domains written to %s (upstream %d, local +%d -%d)%n",
                denyList.size(), output,
                upstream.stream().mapToInt(Set::size).sum(), localDeny.size(), localAllow.size());
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
