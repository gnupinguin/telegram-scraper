package io.github.gnupinguin.tlgscraper.tlgservice.telegram;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class UpdatesHandler implements Client.ResultHandler {

    private final TelegramAuthenticator authenticator;

    public UpdatesHandler(TelegramAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public void onResult(TdApi.Object object) {
        if (object.getConstructor() == TdApi.UpdateAuthorizationState.CONSTRUCTOR) {
            authenticator.onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
        }
    }

}
