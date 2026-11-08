package io.github.yaforster.trails.core.notification;

/** Reports a recipient collision after mailbox normalization. */
public final class DuplicateEmailRecipientException extends IllegalArgumentException {
    public DuplicateEmailRecipientException() {
        super("Duplicate email recipient");
    }
}
