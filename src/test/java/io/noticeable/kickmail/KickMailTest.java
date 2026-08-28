package io.noticeable.kickmail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KickMailTest {

    private static KickMail kickMail;

    @BeforeAll
    static void setUp() throws IOException {
        kickMail = new KickMail();
    }

    @Test
    void disposableEmailIsDetected() {
        assertTrue(kickMail.isDisposable("qgm6ca+3avb6y3wziqbs@sharklasers.com"));
    }

    @Test
    void disposableDetectionIgnoresDomainCase() {
        assertTrue(kickMail.isDisposable("qgm6ca@SharkLasers.COM"));
    }

    @Test
    void nonDisposableEmailPassesDetection() {
        assertFalse(kickMail.isDisposable("john.doe@acme.com"));
        assertFalse(kickMail.isDisposable("estelle+test@gmail.com"));
    }

    @Test
    void addressWithoutDomainIsNotDisposable() {
        assertFalse(kickMail.isDisposable("no-at-sign"));
        assertFalse(kickMail.isDisposable("trailing@"));
    }

    @Test
    void validEmailPassesVerification() {
        assertTrue(kickMail.isValid("john.doe@acme.com"));
    }

    @Test
    void invalidEmailDoesNotPassVerification() {
        assertFalse(kickMail.isValid("john.doe@@acme.com"));
    }

    @Test
    void mxRecordFoundForRealMailDomain() {
        assertTrue(kickMail.hasMxRecord("john.doe@gmail.com"));
        assertFalse(kickMail.hasMxRecord("nobody@mx-less-domain-that-does-not-exist.invalid"));
    }

}
