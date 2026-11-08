package io.github.yaforster.trails.core.notification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

/** A validated group of unique dynamic notification email recipients. */
public record EmailRecipients(List<String> values) {

    public static final int MAXIMUM_RECIPIENTS = 100;
    public static final int MAXIMUM_ADDRESS_LENGTH = 254;
    private static final Pattern LOCAL_PART = Pattern.compile(
            "[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[A-Za-z0-9!#$%&'*+/=?^_`{|}~-]+)*");
    private static final Pattern DOMAIN = Pattern.compile(
            "[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)*");

    public EmailRecipients {
        values = normalize(values);
    }

    private static List<String> normalize(List<String> values) {
        Objects.requireNonNull(values, "Email recipients are required");
        if (values.isEmpty() || values.size() > MAXIMUM_RECIPIENTS) {
            throw new IllegalArgumentException("Recipient count must be between one and " + MAXIMUM_RECIPIENTS);
        }
        List<String> normalized = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String value : values) {
            String recipient = normalize(value);
            if (!seen.add(recipient)) {
                throw new DuplicateEmailRecipientException();
            }
            normalized.add(recipient);
        }
        return List.copyOf(normalized);
    }

    private static String normalize(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Invalid email recipient");
        }
        if (value.chars()
                 .anyMatch(character -> character < 32 || character > 126)) {
            throw new IllegalArgumentException("Invalid email recipient");
        }
        String recipient = value.trim();
        if (recipient.length() > MAXIMUM_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("Invalid email recipient");
        }
        int separator = recipient.indexOf('@');
        if (separator < 1 || separator > 64 || recipient.indexOf('@', separator + 1) >= 0
                || !LOCAL_PART.matcher(recipient.substring(0, separator))
                              .matches()
                || !DOMAIN.matcher(recipient.substring(separator + 1))
                          .matches()) {
            throw new IllegalArgumentException("Invalid email recipient");
        }
        return recipient.substring(0, separator) + "@" + recipient.substring(separator + 1)
                                                                  .toLowerCase(Locale.ROOT);
    }

}
