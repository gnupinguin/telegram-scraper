package io.github.gnupinguin.tlgscraper.tlgservice.telegram;

import org.drinkless.tdlib.TdApi;

public interface TelegramConfigurationService {

    void setTdLibParameters(TdApi.TdlibParameters parameters);

    void checkDatabaseEncryptionKey();

    void setAuthenticationPhoneNumber(String phoneNumber);

    void checkAuthenticationCode(String code);

    void registerUser(String firstName, String lastName);

    void checkAuthenticationPassword(String password);

}
