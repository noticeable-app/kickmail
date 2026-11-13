package io.noticeable.kickmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * The Public Suffix List (https://publicsuffix.org), ICANN and private sections: domains under
 * which unrelated people register names ({@code co.uk}, {@code github.io}, {@code ddns.net}).
 * A deny entry that is one of them must not deny its subdomains.
 */
public final class PublicSuffixes {

    static final String LIST_URL = "https://publicsuffix.org/list/public_suffix_list.dat";

    private final Set<String> rules;

    private PublicSuffixes(final Set<String> rules) {
        this.rules = rules;
    }

    public static PublicSuffixes fetch() throws IOException {
        final URLConnection connection = URI.create(LIST_URL).toURL().openConnection();
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(10_000);
        try (Reader reader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    /** One rule per line, {@code //} comments, {@code *.} wildcards and {@code !} exceptions, as published. */
    static PublicSuffixes parse(final Reader source) throws IOException {
        final Set<String> rules = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(source)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                rules.add(line.split("\\s")[0].toLowerCase(Locale.ROOT));
            }
        }
        if (rules.isEmpty()) {
            throw new IOException("The Public Suffix List is empty");
        }
        return new PublicSuffixes(rules);
    }

    /** Whether a lower-cased domain is itself a public suffix. */
    public boolean isSuffix(final String domain) {
        if (rules.contains("!" + domain)) {
            return false;
        }
        if (rules.contains(domain)) {
            return true;
        }
        final int dot = domain.indexOf('.');
        return dot > 0 && rules.contains("*." + domain.substring(dot + 1));
    }

}
