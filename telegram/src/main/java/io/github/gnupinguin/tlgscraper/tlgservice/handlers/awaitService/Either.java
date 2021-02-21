package io.github.gnupinguin.tlgscraper.tlgservice.handlers.awaitService;

import org.drinkless.tdlib.TdApi;

/**
 * Either success data or error.
 */
public class Either {

    public final TdApi.Object data;
    public final Exception exp;

    public Either(TdApi.Object data) {
        this.data = data;
        this.exp = null;
    }

    public Either(Exception exp) {
        this.data = null;
        this.exp = exp;
    }
}
