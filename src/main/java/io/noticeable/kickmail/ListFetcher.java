package io.noticeable.kickmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Reads domain lists, remote or local: one domain per line, blank lines and
 * lines starting with {@code #} ignored, everything lower-cased.
 */
public final class ListFetcher {

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;

    private static final int READ_TIMEOUT_MILLIS = 10_000;

    public ListFetcher() {
    }

    public Set<String> fetch(final String url) throws IOException {
        final URLConnection connection = URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            return parse(reader);
        }
    }

    /** Reads a local list file; a missing file is an empty list. */
    public Set<String> read(final Path file) throws IOException {
        if (!Files.exists(file)) {
            return new HashSet<>();
        }

        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            return parse(reader);
        }
    }

    static Set<String> parse(final BufferedReader reader) throws IOException {
        final HashSet<String> domains = new HashSet<>();

        String line;
        while ((line = reader.readLine()) != null) {
            final int comment = line.indexOf('#');
            if (comment >= 0) {
                line = line.substring(0, comment);
            }
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            domains.add(line.toLowerCase(Locale.ROOT));
        }

        return domains;
    }

}
