package io.github.gnupinguin.tlgscraper.scraper.persistence;

import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ApplicationStorageImplTest {
//
//    private static final long CHAT_ID = -1001L;
//    private static final String CHANNEL_NAME = "channel";
//    private static final String URL = "https://u.rl";
//
//    private static final String TEXT = "text";
//
//    public static final Date DATE = new Date();
//
//    @Mock
//    private MessageRepository messageRepository;
//
//    @Mock
//    private MentionRepository mentionRepository;
//
//    @Mock
//    private LinkRepository linkRepository;
//
//    @Mock
//    private HashTagRepository hashTagRepository;
//
//    @Mock
//    private ChatRepository chatRepository;
//
//    @InjectMocks
//    private ApplicationStorageImpl storage;
//
//    @Test
//    public void testSave() {
//        Chat channel = new Chat(CHAT_ID, CHANNEL_NAME, "description", 32);
//        MessageInfo message1 = baseMessage(1);
//        MessageInfo message2 = baseMessage(2);
//
//        Mockito.when(messageRepository.save(anyCollection()))
//                .thenAnswer(invocation -> {
//                    List<Message> messages = invocation.getArgument(0);
//                    LongStream.range(0, messages.size())
//                            .forEach(i -> messages.get((int) i).setInternalId(i));
//                    return true;
//                });
//        storage.save(channel, List.of(message1, message2));
//
//        verify(chatRepository, times(1)).save(channel);
//        verify(messageRepository, times(1)).save(List.of(
//                mappedMessage(0L, 1L),
//                mappedMessage(1L, 2L)
//        ));
//
//        verify(mentionRepository, times(1)).save(List.of(
//                new Mention(null, 0L, "mention1"),
//                new Mention(null, 1L, "mention2")
//        ));
//        verify(linkRepository, times(1)).save(List.of(
//                new Link(null, 0L, "https://u.rl1"),
//                new Link(null, 1L, "https://u.rl2")
//        ));
//        verify(hashTagRepository, times(1)).save(List.of(
//                new HashTag(null, 0L, "hashtag1"),
//                new HashTag(null, 1L, "hashtag2")
//        ));
//    }
//
//    private MessageInfo baseMessage(long id) {
//        MessageInfo message = new MessageInfo();
//        message.setChatId(CHAT_ID);
//        message.setId(id);
//        message.setType(MessageType.Text);
//        message.setLinks(List.of(URL + id));
//        message.setMentions(List.of("mention" + id));
//        message.setHashTags(List.of("hashtag" + id));
//        message.setTextContent(TEXT);
//        message.setLoadDate(DATE);
//        message.setPublishDate(DATE);
//        return message;
//    }
//
//    private Message mappedMessage(long internalId, long id) {
//        return new Message(internalId, CHAT_ID, id, null, null, null, 0, TEXT, new Timestamp(DATE.getTime()), new Timestamp(DATE.getTime()), 0);
//    }

}