package com.fakesibwork.channel_bot.service;

import com.fakesibwork.channel_bot.config.BotConfig;
import com.fakesibwork.channel_bot.repo.UserRepo;
import com.tecknobit.telegrammanager.botapi.managers.TelegramManager;
import com.tecknobit.telegrammanager.botapi.managers.identifiers.chat.ChatManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.groupadministration.*;
import org.telegram.telegrambots.meta.api.methods.invoices.CreateInvoiceLink;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMemberMember;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.payments.LabeledPrice;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.exceptions.TelegramApiValidationException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    @Autowired
    private UserRepo userRepo;

    BotConfig botConfig;

    public TelegramBot(BotConfig botConfig) {
        this.botConfig = botConfig;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "Start Command"));
        listOfCommands.add(new BotCommand("/test", "Test of member"));
        listOfCommands.add(new BotCommand("/post", "Send post"));
        listOfCommands.add(new BotCommand("/product", "Test price product"));
        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
        }

    }


    public String getPayment(){
        return botConfig.getPayment();
    }

    @Override
    public String getBotToken() {
        return botConfig.getToken();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {

        String chatId = "-100*********";

        if (update.hasChatJoinRequest()){
            long userId = update.getChatJoinRequest().getUserChatId();

            try {
                ChatManager chatManager = new ChatManager(getBotToken());
                chatManager.approveChatJoinRequest(chatId, userId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            userRepo.addNewUser(userId, update.getChatJoinRequest().getUser().getUserName());
            sendMessage(userId, "Приветственное сообщение");

        } else if (update.hasMessage() && update.getMessage().hasText()) {

            long userId = update.getMessage().getChatId();
            String message = update.getMessage().getText();

            if (message.equals("/test")){
                GetChatMember chatMember = new GetChatMember(chatId, userId);

                try {
                    ChatMember member = this.execute(chatMember);
                    if (member.getStatus().equals("member"))
                    sendMessage(userId, "вы в канале");
                    else
                    sendMessage(userId, "вы не в канале");
                } catch (TelegramApiException e){
                }

            } else if (message.equals("/post")) {
                InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                rowInline.add(makeButton("text", "BUTTON"));
                rowsInline.add(rowInline);
                markup.setKeyboard(rowsInline);
                sendMessageWithButtons(Long.parseLong(chatId), "text", markup);
                sendMessage(userId, "Пост в канале, ток баловаться не надо пж");
                System.out.println("here");
            } else if (message.equals("/product")) {
                LabeledPrice price = new LabeledPrice("Цена", 10000);
                List<LabeledPrice> prices = new ArrayList<>();
                prices.add(price);
                CreateInvoiceLink invoiceLink = new CreateInvoiceLink();
                invoiceLink.setTitle("Цветы");
                invoiceLink.setDescription("Вкусные цветы");
                invoiceLink.setCurrency("RUB");
                invoiceLink.setProviderToken(getPayment());
                invoiceLink.setPayload("1");
                invoiceLink.setPrices(prices);
                try {
                    String link = execute(invoiceLink);
                    sendMessage(userId, link);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void sendMessage(Long chatId, String textToSend) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setParseMode(ParseMode.HTML);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    private void sendMessageWithButtons(Long chatId, String textToSend, ReplyKeyboard replyMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chatId));
        sendMessage.setText(textToSend);
        sendMessage.setReplyMarkup(replyMarkup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    private InlineKeyboardButton makeButton(String text, String callbackData) {
        var button = new InlineKeyboardButton();
        button.setText(text);
        button.setCallbackData(callbackData);
        return button;
    }
}
