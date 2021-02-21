package io.github.gnupinguin.tlgscraper.tlgservice.telegram;

import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

@Slf4j
public class AuthorizationRequestHandler implements Client.ResultHandler {

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.Error.CONSTRUCTOR:
                log.info("Receive an Telegram error: {}", object);
                throw new RuntimeException("Can not send request"); //TODO prepare retry
            case TdApi.Ok.CONSTRUCTOR:
                // result is already received through UpdateAuthorizationState, nothing to do
                break;
            default:
                log.info("Receive wrong response from TDLib: {}", object);
        }
    }

}