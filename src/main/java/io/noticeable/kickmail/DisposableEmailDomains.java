package io.noticeable.kickmail;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

public final class DisposableEmailDomains {

    private static final String DISPOSABLE_DOMAINS_LIST_URL = "https://kickmail.pages.dev/denylist.txt";

    private final AtomicReference<DomainList> domains;

    private final ListFetcher listFetcher = new ListFetcher();

    public DisposableEmailDomains() throws IOException {
        domains = new AtomicReference<>(DomainList.of(listFetcher.fetch(DISPOSABLE_DOMAINS_LIST_URL)));
    }

    /**
     * Whether a lower-cased mailbox domain is disposable: listed, or under a listed parent domain
     * (see {@link DomainList}).
     */
    public boolean contains(final String domain) {
        return domains.get().isDenied(domain);
    }

    public void refresh() throws IOException {
        domains.set(DomainList.of(listFetcher.fetch(DISPOSABLE_DOMAINS_LIST_URL)));
    }

}
