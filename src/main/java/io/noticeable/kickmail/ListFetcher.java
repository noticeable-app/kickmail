package io.noticeable.kickmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class ListFetcher {

    private static final int CONNECT_TIMEOUT_MILLIS = 10_000;

    private static final int READ_TIMEOUT_MILLIS = 10_000;

    public ListFetcher() {
    }

    public Set<String> fetch(final String url) throws IOException {
        final HashSet<String> domains = new HashSet<>();

        final URLConnection connection = URI.create(url).toURL().openConnection();
        connection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
        connection.setReadTimeout(READ_TIMEOUT_MILLIS);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }

                domains.add(line.toLowerCase(Locale.ROOT));
            }
        }

        return domains;
    }

}
