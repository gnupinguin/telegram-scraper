package io.github.gnupinguin.tlgscraper.model.scraper;

public enum MessageType {
    Text(0),
    Photo(1),
    Video(2),
    Audio(3),
    Document(4),
    Other(-1);

    private final int typeId;

    MessageType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }
}
