package dev.pgjbz.transactions.domain.account.models;

import static dev.pgjbz.transactions.domain.utils.StringUtils.normalizeDocument;
import lombok.Getter;

@Getter 
public class Account {

    private final Long id; 
    private final String document;

    public Account(final Long id, final String document) {
        this.id = id;
        this.document = normalizeDocument(document);
    }

    public static Account createForDocument(final String document) {
        return new Account(null, document);
    }
}
