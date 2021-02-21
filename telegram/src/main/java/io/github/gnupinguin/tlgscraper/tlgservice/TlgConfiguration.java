package io.github.gnupinguin.tlgscraper.tlgservice;

import io.github.gnupinguin.tlgscraper.tlgservice.config.TlgClientProperties;
import io.github.gnupinguin.tlgscraper.tlgservice.config.TlgLoginProperties;
import io.github.gnupinguin.tlgscraper.tlgservice.telegram.*;
import io.github.gnupinguin.tlgscraper.tlgservice.utils.TlgUtils;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import static io.github.gnupinguin.tlgscraper.tlgservice.utils.TimeRangeMethodCallCountLimiterProxyFactory.getProxy;

public class TlgConfiguration {

    private final TelegramClient telegram;

    public TlgConfiguration(TlgLoginProperties loginProperties, TlgClientProperties clientProperties) {
        loadTelegramLibrary();

        TelegramConfigurationServiceImpl telegramConfigService = new TelegramConfigurationServiceImpl(null);
        TelegramAuthenticator telegramAuthenticator = new TelegramAuthenticator(telegramConfigService,
                loginProperties.getPhoneNumber(),
                TlgUtils.fileAuthCodeSource(loginProperties.getAuthCodeFile()),
                clientProperties);

        Client nativeClient = Client.create(new UpdatesHandler(telegramAuthenticator), null, null);
        Client.execute(new TdApi.SetLogVerbosityLevel(1)); //log config

        telegramConfigService.setClient(nativeClient);

        telegram = getProxy(new TelegramClientImpl(nativeClient), TelegramClient.class);
    }


    public static void loadTelegramLibrary() {
        try {
            System.loadLibrary("tdjni");
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
    }

    public TelegramClient getTelegram() {
        return telegram;
    }

}
