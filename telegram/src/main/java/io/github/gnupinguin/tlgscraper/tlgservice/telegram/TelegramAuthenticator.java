package io.github.gnupinguin.tlgscraper.tlgservice.telegram;

import io.github.gnupinguin.tlgscraper.tlgservice.config.TlgClientProperties;
import lombok.extern.slf4j.Slf4j;
import org.drinkless.tdlib.TdApi;

import javax.annotation.Nonnull;
import java.util.function.Supplier;


@Slf4j
public class TelegramAuthenticator {

    public void setTelegram(TelegramConfigurationService telegram) {
        this.telegram = telegram;
    }

    private TelegramConfigurationService telegram;
    private final String phoneNumber;
    private final Supplier<String> authCodeSrc;
    private final TlgClientProperties clientProperties;

    public TelegramAuthenticator(TelegramConfigurationService telegram,
                                 String phoneNumber, Supplier<String> authCodeSrc,
                                 TlgClientProperties clientProperties) {
        this.telegram = telegram;
        this.phoneNumber = phoneNumber;
        this.authCodeSrc = authCodeSrc;
        this.clientProperties = clientProperties;
    }

    public void onAuthorizationStateUpdated(TdApi.AuthorizationState authorizationState) {
        switch (authorizationState.getConstructor()) {
            case TdApi.AuthorizationStateWaitTdlibParameters.CONSTRUCTOR:
                telegram.setTdLibParameters(getTdlibParameters());
                break;
            case TdApi.AuthorizationStateWaitEncryptionKey.CONSTRUCTOR:
                telegram.checkDatabaseEncryptionKey();
                break;
            case TdApi.AuthorizationStateWaitPhoneNumber.CONSTRUCTOR: {
                log.info("Telegram login with phone number: " + phoneNumber);
                telegram.setAuthenticationPhoneNumber(phoneNumber);
                break;
            }
            case TdApi.AuthorizationStateWaitCode.CONSTRUCTOR: {
                telegram.checkAuthenticationCode(authCodeSrc.get());
                break;
            }
            case TdApi.AuthorizationStateReady.CONSTRUCTOR:
                break;
            case TdApi.AuthorizationStateLoggingOut.CONSTRUCTOR:
                log.info("Logging out");
                break;
            case TdApi.AuthorizationStateClosing.CONSTRUCTOR:
                log.info("Telegram closing");
                break;
            case TdApi.AuthorizationStateClosed.CONSTRUCTOR:
                log.info("Telegram closed");
                break;
            default:
                log.info("Unsupported Telegram authorization state: {}", authorizationState);
        }

    }

    @Nonnull
    private TdApi.TdlibParameters getTdlibParameters() {
        TdApi.TdlibParameters parameters = new TdApi.TdlibParameters();
        parameters.databaseDirectory = clientProperties.getDatabaseDirectory();
        parameters.useMessageDatabase = clientProperties.isUseMessageDatabase();
        parameters.useSecretChats = clientProperties.isUseSecretChats();
        parameters.apiId = clientProperties.getApiId();
        parameters.apiHash = clientProperties.getApiHash();
        parameters.systemLanguageCode = clientProperties.getSystemLanguageCode();
        parameters.deviceModel = clientProperties.getDeviceModel();
        parameters.systemVersion = clientProperties.getSystemVersion();
        parameters.applicationVersion = clientProperties.getApplicationVersion();
        parameters.enableStorageOptimizer = clientProperties.isEnableStorageOptimizer();
        return parameters;
    }

}
