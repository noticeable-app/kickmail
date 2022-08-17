package io.noticeable.kickmail;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;

public class AggregateLists {

    private static final String ALLOW_LIST_SOURCE1_URL = "https://raw.githubusercontent.com/disposable-email-domains/disposable-email-domains/master/allowlist.conf";

    private static final String DENY_LIST_SOURCE1_URL = "https://raw.githubusercontent.com/disposable-email-domains/disposable-email-domains/master/disposable_email_blocklist.conf";

    private static final String DENY_LIST_SOURCE2_URL = "https://raw.githubusercontent.com/7c/fakefilter/main/txt/data.txt";

    private static final String DENY_LIST_SOURCE3_URL = "https://raw.githubusercontent.com/FGRibreau/mailchecker/master/list.txt";


    public static void main(String[] args) throws IOException {
        final ListFetcher listFetcher = new ListFetcher();

        final TreeSet<String> aggregatedAllowList = new TreeSet<>();
        aggregatedAllowList.addAll(listFetcher.fetch(DENY_LIST_SOURCE1_URL));
        aggregatedAllowList.addAll(listFetcher.fetch(DENY_LIST_SOURCE2_URL));
        aggregatedAllowList.addAll(listFetcher.fetch(DENY_LIST_SOURCE3_URL));
        aggregatedAllowList.removeAll(listFetcher.fetch(ALLOW_LIST_SOURCE1_URL));

        try (final BufferedWriter writer = new BufferedWriter(new FileWriter("denylist.txt"))) {
            for (final String domain : aggregatedAllowList) {
                writer.write(domain);
                writer.newLine();
            }
            writer.flush();
        }
    }

}
