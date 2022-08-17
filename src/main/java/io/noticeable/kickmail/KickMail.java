package io.noticeable.kickmail;

import com.sanctionco.jmail.EmailValidator;
import com.sanctionco.jmail.JMail;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.io.IOException;

public final class KickMail {

    private final EmailValidator STRICT_VALIDATOR = JMail.strictValidator();

    private final DisposableEmailDomains disposableEmailDomains;


    public KickMail() throws IOException {
        disposableEmailDomains = new DisposableEmailDomains();
    }

    public boolean isDisposable(final String email) {
        final String[] chunks = email.split("@");

        if (chunks.length < 2) {
            return false;
        }

        return disposableEmailDomains.contains(chunks[1]);
    }

    public boolean isValid(final String email) {
        return STRICT_VALIDATOR.isValid(email);
    }

    public boolean hasMxRecord(final String email) {
        final String[] chunks = email.split("@");
        if (chunks.length < 2) {
            return false;
        }

        try {
            final Record[] records = new Lookup(chunks[1], Type.MX).run();
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

}
