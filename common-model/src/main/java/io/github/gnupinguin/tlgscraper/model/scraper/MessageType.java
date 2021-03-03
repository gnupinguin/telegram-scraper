package io.github.gnupinguin.tlgscraper.model.scraper;

public enum MessageType {
    Text(0),
    Photo(1),
    Video(2),
    Audio(3),
    Document(4),
    Multimedia(5),

    Other(-1),
    ChannelInfo(-2);

    private final int typeId;

    MessageType(int typeId) {
        this.typeId = typeId;
    }

    public int getTypeId() {
        return typeId;
    }

    public static MessageType parse(Integer typeId) {
        if (typeId == null) {
            return Other;
        }
        switch (typeId) {
            case 0: return Text;
            case 1: return Photo;
            case 2: return Video;
            case 3: return Audio;
            case 4: return Document;
            case 5: return Multimedia;
            default: return Other;
        }
    }
}
