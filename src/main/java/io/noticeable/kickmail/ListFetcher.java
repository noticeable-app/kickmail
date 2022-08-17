package io.noticeable.kickmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public final class ListFetcher {

    public ListFetcher() {
    }

    public Set<String> fetch(final String url) throws IOException {
        final HashSet<String> domains = new HashSet<>();

        final URLConnection connection = new URL(url).openConnection();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.charAt(0) == '#') {
                    continue;
                }

                domains.add(line);
            }
        }

        return domains;
    }

}
