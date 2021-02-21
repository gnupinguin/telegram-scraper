package io.github.gnupinguin.tlgscraper.tlgservice.handlers;

import org.drinkless.tdlib.TdApi;

public class HandlerFactory {

    public static <T extends TdApi.Object> BaseAwaitHandler<T> defaultHandler(String handlerId) {
        return new BaseAwaitHandler<T>() {
            @Override
            public String toString() {
                return handlerId;
            }
        };
    }

}
