package io.noticeable.kickmail;

import java.util.HashSet;
import java.util.Set;

/**
 * The published deny list, parsed: which mailbox domains are disposable.
 *
 * <p>A line of the list is one of:
 * <ul>
 *     <li>{@code example.com}: denied, and so is every subdomain of it ({@code mail.example.com},
 *     {@code a.b.example.com}). Account farms use random subdomains of a domain they own.</li>
 *     <li>{@code =example.com}: denied as written only, its subdomains are not. Used for the entries
 *     that are public suffixes ({@code ddns.net}, {@code it.com}): their subdomains belong to
 *     unrelated people.</li>
 *     <li>{@code !mail.example.com}: never denied, nor its subdomains, even when a parent domain is
 *     denied. The allow list, for a false positive under a denied parent.</li>
 * </ul>
 *
 * <p>The most specific level decides: walking up from the domain of the address, the first
 * exception or deny entry met wins. A bare top-level domain is never looked up.
 *
 * <p>Clients before this format read {@code =} and {@code !} lines as domains that no address
 * has: they keep matching the plain entries exactly.
 */
public final class DomainList {

    private final Set<String> denied;

    private final Set<String> exactOnly;

    private final Set<String> allowed;

    private DomainList(final Set<String> denied, final Set<String> exactOnly, final Set<String> allowed) {
        this.denied = denied;
        this.exactOnly = exactOnly;
        this.allowed = allowed;
    }

    /** From the lines of the list, as {@link ListFetcher} reads them (trimmed, lower-cased, no comments). */
    public static DomainList of(final Set<String> lines) {
        final Set<String> denied = new HashSet<>();
        final Set<String> exactOnly = new HashSet<>();
        final Set<String> allowed = new HashSet<>();
        for (final String line : lines) {
            if (line.startsWith("!")) {
                allowed.add(line.substring(1));
            } else if (line.startsWith("=")) {
                exactOnly.add(line.substring(1));
            } else {
                denied.add(line);
            }
        }
        return new DomainList(denied, exactOnly, allowed);
    }

    /** Whether a lower-cased mailbox domain is disposable. */
    public boolean isDenied(final String domain) {
        String current = domain;
        boolean asWritten = true;
        while (true) {
            if (allowed.contains(current)) {
                return false;
            }
            if (denied.contains(current) || (asWritten && exactOnly.contains(current))) {
                return true;
            }
            final int dot = current.indexOf('.');
            if (dot < 0) {
                return false;
            }
            current = current.substring(dot + 1);
            if (current.indexOf('.') < 0) {
                // a bare top-level domain is never an entry
                return false;
            }
            asWritten = false;
        }
    }

    /** The number of plain deny entries. */
    public int size() {
        return denied.size();
    }

}
