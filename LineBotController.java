package com.linefood.bot;

import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.StickerMessageContent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.StickerMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import com.linefood.bot.flex.*;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@LineMessageHandler
public class LineBotController {

    @Autowired
    private LineMessagingClient lineMessagingClient;

    @EventMapping
    public void handleTextMessageEvent(MessageEvent<TextMessageContent> event) {
        log.info(event.toString());
        TextMessageContent message = event.getMessage();
        handleTextContent(event.getReplyToken(), event, message);
    }

    @EventMapping
    public void handleStickerMessageEvent(MessageEvent<StickerMessageContent> event) {
        log.info(event.toString());
        handleSticker(event.getReplyToken(), event.getMessage());
    }

    private void handleSticker(String replyToken, StickerMessageContent content) {
        reply(replyToken, new StickerMessage(content.getPackageId(), content.getStickerId()));
    }

    @EventMapping
    public void handleDefaultEvent(Event event) {
        System.out.println("event: " + event);
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse response = lineMessagingClient.replyMessage(
                    new ReplyMessage(replyToken, messages)).get();
            log.info("Sent message: " + response);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void replyText(@NonNull String replyToken, String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken must not be empty");
        }
        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 2) + "....";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content) {
        String text = content.getText();

        String flexMenu = "richmenu-7f3ab049e1c434eac55b8aec74ea8689";

        log.info("Got text message from " + replyToken + ": " + text);
        switch (text) {
            case "profile": {
                String userId = event.getSource().getUserId();
                if(userId != null) {
                    lineMessagingClient.getProfile(userId)
                            .whenComplete((profile, throwable) -> {
                                if(throwable != null) {
                                    this.replyText(replyToken, throwable.getMessage());
                                    return;
                                }

                                this.reply(replyToken, Arrays.asList(
                                        new TextMessage("Display name: " + profile.getDisplayName()),
                                        new TextMessage("Status message: " + profile.getStatusMessage())
                                ));

                            });
                }
                break;
            }
            case "Flex Back": {
                String userId = event.getSource().getUserId();
                if(userId != null) {
                    lineMessagingClient.linkRichMenuIdToUser(userId, flexMenu);
                    return;
                }
                break;
            }
            case "อาหารจังหวัดลำพูน ": {
                this.reply(replyToken, new province8().get());
                break;
            }
            case "วิธีหมูตุ๋นเนื้อลำไย": {
                this.reply(replyToken, new Food8Pv1().get());
                break;
            }
            case "วิธีทำแกงแคไก่": {
                this.reply(replyToken, new Food8Pv2().get());
                break;
            }
            case "วิธีทำแกงโฮะ": {
                this.reply(replyToken, new Food8Pv3().get());
                break;
            }
            case "วิธีทำแกงหยวกกล้วย": {
                this.reply(replyToken, new Food8Pv4().get());
                break;
            }
            case "อาหารจังหวัดอุตรดิตถ์": {
                this.reply(replyToken, new province9().get());
                break;
            }
            case "วิธีทำหมี่พันลับแล": {
                this.reply(replyToken, new Food9Pv1().get());
                break;
            }
            case "วิธีทำข้าวพันผัก": {
                this.reply(replyToken, new Food9Pv2().get());
                break;
            }
            case "วิธีทำน้ำยาปลาตะโกก": {
                this.reply(replyToken, new Food9Pv3().get());
                break;
            }
            default:
                log.info("Return echo message " + replyToken + ":" + text);
                this.replyText(replyToken, text);
                break;
        }
    }

}


//ใส่วิธีทำชื่ออาหาร ใส่class