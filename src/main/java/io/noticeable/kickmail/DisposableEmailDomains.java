package io.noticeable.kickmail;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public final class DisposableEmailDomains {

    private static final String DISPOSABLE_DOMAINS_LIST_URL = "https://kickmail.pages.dev/denylist.txt";

    private final AtomicReference<Set<String>> domains;

    private final ListFetcher listFetcher = new ListFetcher();

    public DisposableEmailDomains() throws IOException {
        domains = new AtomicReference<>(listFetcher.fetch(DISPOSABLE_DOMAINS_LIST_URL));
    }

    public boolean contains(final String email) {
        return domains.get().contains(email);
    }

    public void refresh() throws IOException {
        domains.set(listFetcher.fetch(DISPOSABLE_DOMAINS_LIST_URL));
    }

}
