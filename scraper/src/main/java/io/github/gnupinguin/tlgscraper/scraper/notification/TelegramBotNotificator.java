package io.github.gnupinguin.tlgscraper.scraper.notification;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramBotNotificator implements Notificator {

    private final TelegramBot bot;

    private final TelegramBotConfiguration botConfiguration;

    @Override
    public void send(String message) {
        bot.execute(new SendMessage(botConfiguration.getChatId(), message));
    }

    @Override
    public boolean waitApprove(String message) {
        bot.execute(new SendMessage(botConfiguration.getChatId(), message)
                .replyMarkup(new ReplyKeyboardMarkup("Approve", "Discard").oneTimeKeyboard(true)));

        Date startDate = new Date();
        log.info("Waiting approving");
        List<String> updates = waitUpdates(startDate);

        return updates.get(updates.size()-1).equals("Approve");
    }

    @Override
    public boolean approveRestoration(Collection<String> channels) {
        String collect = channels.stream()
                .map(c -> "https://t.me/" + c)
                .collect(Collectors.joining(" \n"));
        return waitApprove("Do we need to stop scrapping and restore the chats?\n" + collect);
    }

    private List<String> waitUpdates(Date startDate) {
        GetUpdates getUpdates = new GetUpdates().limit(100).offset(0);
        List<String> updates = fetchLastUpdates(getUpdates, startDate);
        while (updates.isEmpty()) {
            waitSec();
            updates = fetchLastUpdates(getUpdates, startDate);
        }
        return updates;
    }

    private void waitSec() {
       try {
           TimeUnit.SECONDS.sleep(5);
       } catch (Exception e) {
           throw new RuntimeException(e);
       }
    }

    @Nonnull
    private List<String> fetchLastUpdates(GetUpdates getUpdates, Date startDate) {
        return bot.execute(getUpdates).updates().stream()
                .map(Update::message)
                .filter(Objects::nonNull)
                .filter(m -> m.chat().id() == botConfiguration.getChatId())
                .filter(m -> messageDate(m).after(startDate))
                .map(Message::text)
                .collect(Collectors.toList());
    }

    @Nonnull
    private Date messageDate(Message m) {
        return new Date(m.date()*1000L);
    }

}
