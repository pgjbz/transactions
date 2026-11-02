package dev.pgjbz.transactions.domain.account.models;

import static dev.pgjbz.transactions.domain.utils.StringUtils.normalizeDocument;

import dev.pgjbz.transactions.domain.utils.ObjectUtils;
import lombok.Getter;

@Getter 
public class Account {

    private final Long id; 
    private final String document;

    public Account(final Long id, final String document) {
        this.id = id;
        this.document = normalizeDocument(ObjectUtils.required(document, "Document is required"));
    }

    public static Account createForDocument(final String document) {
        return new Account(null, document);
    }
}
