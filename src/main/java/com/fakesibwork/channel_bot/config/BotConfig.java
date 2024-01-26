package com.fakesibwork.channel_bot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("application.properties")
@Data
@Configuration
public class BotConfig {

    @Value("${bot.payment}")
    String payment;

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;
}
