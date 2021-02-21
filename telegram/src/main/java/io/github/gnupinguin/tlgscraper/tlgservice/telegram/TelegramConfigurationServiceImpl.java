package io.github.gnupinguin.tlgscraper.tlgservice.telegram;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class TelegramConfigurationServiceImpl implements TelegramConfigurationService {

    private Client client;

    public TelegramConfigurationServiceImpl(Client client) {
        this.client = client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @Override
    public void setTdLibParameters(TdApi.TdlibParameters parameters) {
        client.send(new TdApi.SetTdlibParameters(parameters), new AuthorizationRequestHandler());
    }

    @Override
    public void checkDatabaseEncryptionKey() {
        client.send(new TdApi.CheckDatabaseEncryptionKey(), new AuthorizationRequestHandler());
    }

    @Override
    public void setAuthenticationPhoneNumber(String phoneNumber) {
        client.send(new TdApi.SetAuthenticationPhoneNumber(phoneNumber, null), new AuthorizationRequestHandler());
    }

    @Override
    public void checkAuthenticationCode(String code) {
        client.send(new TdApi.CheckAuthenticationCode(code), new AuthorizationRequestHandler());
    }

    @Override
    public void registerUser(String firstName, String lastName) {
        client.send(new TdApi.RegisterUser(firstName, lastName), new AuthorizationRequestHandler());
    }

    @Override
    public void checkAuthenticationPassword(String password) {
        client.send(new TdApi.CheckAuthenticationPassword(password), new AuthorizationRequestHandler());
    }

}
