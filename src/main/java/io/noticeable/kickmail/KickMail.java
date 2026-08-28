package io.noticeable.kickmail;

import com.sanctionco.jmail.EmailValidator;
import com.sanctionco.jmail.JMail;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;
import java.time.Duration;
import java.util.Locale;

public final class KickMail {

    private static final Duration DNS_TIMEOUT = Duration.ofSeconds(3);

    private final EmailValidator STRICT_VALIDATOR = JMail.strictValidator();

    private final DisposableEmailDomains disposableEmailDomains;

    private final ExtendedResolver resolver;


    public KickMail() throws IOException {
        disposableEmailDomains = new DisposableEmailDomains();
        // System-configured servers with a bounded timeout so an MX check can
        // never hang a caller for the default resolver's much longer budget.
        resolver = new ExtendedResolver();
        resolver.setTimeout(DNS_TIMEOUT);
    }

    public boolean isDisposable(final String email) {
        final String domain = domainOf(email);

        if (domain == null) {
            return false;
        }

        return disposableEmailDomains.contains(domain);
    }

    public boolean isValid(final String email) {
        return STRICT_VALIDATOR.isValid(email);
    }

    public boolean hasMxRecord(final String email) {
        final String domain = domainOf(email);
        if (domain == null) {
            return false;
        }

        try {
            final Lookup lookup = new Lookup(domain, Type.MX);
            lookup.setResolver(resolver);
            final Record[] records = lookup.run();
            return records != null && records.length > 0;
        } catch (TextParseException e) {
            return false;
        }
    }

    public void refresh() throws IOException {
        disposableEmailDomains.refresh();
    }

    public boolean shouldKick(final String email) {
        return !isValid(email) || isDisposable(email) || !hasMxRecord(email);
    }

    /**
     * The part after the last {@code @}, lower-cased (mailbox domains are
     * case-insensitive and the deny list is lower-case), or {@code null}
     * when the address has none.
     */
    private static String domainOf(final String email) {
        final int at = email.lastIndexOf('@');

        if (at < 0 || at == email.length() - 1) {
            return null;
        }

        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

}
