package k1ryl.meldunekbot.bot;

import k1ryl.meldunekbot.meldunek.ApplicationService;
import k1ryl.meldunekbot.meldunek.validation.model.FieldStatus;
import k1ryl.meldunekbot.openai.OpenAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;

import static k1ryl.meldunekbot.meldunek.ApplicationState.*;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {

    //todo move to config
    private static final String BOT_TOKEN = "7271633236:AAFA2YyUmt9M7A-3n1XuJWuRmzn_aOk5Vsg";
    private static final String BOT_USERNAME = "MeldunekHelpBot";

    private final ApplicationService applicationService;
    private final OpenAIService openAIService;

    public TelegramBot(ApplicationService applicationService, OpenAIService openAIService) {
        super(BOT_TOKEN);
        this.applicationService = applicationService;
        this.openAIService = openAIService;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            User tgUser = update.getMessage().getFrom();
            String chatId = update.getMessage().getChatId().toString();
            log.info(update.getMessage().getText());
            String messageText = update.getMessage().getText();
            if (messageText.startsWith("/")) {
                try {
                    Command command = Command.fromText(messageText.toLowerCase());
                    String responseText = switch (command) {
                        case START -> "start message";
                        case MELDUNEK -> {
                            applicationService.createNewApplication(tgUser.getId(), PERSONAL_DATA);
                            sendMessage(chatId, "Заполняем заявление");
                            yield """
                                    Этап 1 - Персональные данные
                                    Пожалуйста введите данные заявителя:
                                        1. Имя (латиницей как в паспорте)
                                        2. Фамилия (латиницей как в паспорте)
                                        3. Дата рождения
                                        4. Страна рождения
                                        5. Город рождения""";

                        }
                        case HELP -> """
                                Доступные команды:
                                /meldunek - начать заполнение заявления
                                /help - помощь""";
                    };

                    sendMessage(update.getMessage().getChatId().toString(), responseText);
                } catch (IllegalArgumentException e) {
                    //todo The command is not recognized, handle this case
                }
            } else {
                String responseText;
                try {
                    var application = applicationService.getLatestApplicationRequestByTgUserId(tgUser.getId());
                    responseText = switch (application.getState()) {
                        case PERSONAL_DATA -> {
                            Map<String, FieldStatus> invalidFields = applicationService.extractAndSavePersonalData(messageText, application);
                            if (!invalidFields.isEmpty()) {
                                yield invalidPersonalDataMessage(invalidFields);
                            } else if (application.getState().equals(PESEL)) {
                                yield "У вас есть номер PESEL? Если да, введите номер. Если нет, введите 'нет номера'";
                            } else {
                                yield "Пожалуйста, введите персональные данные еще раз";
                            }
                        }
                        case PESEL -> {
                            Map<String, FieldStatus> invalidFields = applicationService.extractAndSavePesel(messageText, application);
                            if (!invalidFields.isEmpty()) {
                                yield invalidPeselMessage(invalidFields);
                            } else if (application.getState().equals(CONTACT_DATA)) {
                                yield """
                                        Этап 2 - Контактные данные
                                        Пожалуйста введите:
                                            1. Телефон
                                            2. Email""";
                            } else {
                                yield "Введите номер PESEL или 'нет номера'";
                            }
                        }
                        case CONTACT_DATA -> {
                            Map<String, FieldStatus> invalidFields = applicationService.extractAndSaveContactData(messageText, application);
                            if (!invalidFields.isEmpty()) {
                                yield invalidContactDataMessage(invalidFields);
                            } else if (application.getState().equals(APARTMENT_DATA)) {
                                yield """
                                        Этап 3 - Данные места жительства
                                        Пожалуйста введите:
                                            1. Город
                                            2. Почтовый код
                                            3. Улица и номер дома""";
                            } else {
                                yield "Пожалуйста, введите контактные данные еще раз";
                            }
                        }
                        case APARTMENT_DATA -> {
                            Map<String, FieldStatus> invalidFields = applicationService.extractAndSaveContactData(messageText, application);
                            if (!invalidFields.isEmpty()) {
                                yield invalidContactDataMessage(invalidFields);
                            } else if (application.getState().equals(APARTMENT_DATA)) {
                                yield """
                                        Этап 3 - Данные места жительства
                                        Пожалуйста введите:
                                            1. Город
                                            2. Почтовый код
                                            3. Улица и номер дома""";
                            } else {
                                yield "Пожалуйста, введите контактные данные еще раз";
                            }
                        }
                        default -> "Для начала работы введите команду /meldunek";
                    };
                } catch (Exception e) {
                    log.error("Error processing user message", e);
                    responseText = "Для начала работы введите команду /meldunek";
                }

                sendMessage(update.getMessage().getChatId().toString(), responseText);
            }
        }

    }

    private String invalidPersonalDataMessage(Map<String, FieldStatus> invalidFields) {
        StringBuilder responseBuilder = new StringBuilder("Пожалуйста, введите недостающие данные:\n");
        int fieldNumber = 1;
        if (invalidFields.containsKey("name")) {
            responseBuilder.append("    ").append(fieldNumber++).append(". Имя (латиницей как в паспорте)\n");
        }
        if (invalidFields.containsKey("surname")) {
            responseBuilder.append("    ").append(fieldNumber++).append(". Фамилия (латиницей как в паспорте)\n");
        }
        if (invalidFields.containsKey("dateOfBirth")) {
            responseBuilder.append("    ").append(fieldNumber++).append(". Дата рождения\n");
        }
        if (invalidFields.containsKey("countryOfBirth")) {
            responseBuilder.append("    ").append(fieldNumber++).append(". Страна рождения\n");
        }
        if (invalidFields.containsKey("placeOfBirth")) {
            responseBuilder.append("    ").append(fieldNumber).append(". Город рождения");
        }
        return responseBuilder.toString();
    }

    private String invalidPeselMessage(Map<String, FieldStatus> invalidFields) {
        return "PESEL не прошел проверку. Номер должен состоять из 11 цифр. Пожалуйста, введите номер PESEL еще раз или введите 'нет номера'";
    }

    private String invalidContactDataMessage(Map<String, FieldStatus> invalidFields) {
        StringBuilder responseBuilder = new StringBuilder("Пожалуйста, введите недостающие данные:\n");
        int fieldNumber = 1;
        if (invalidFields.containsKey("phone")) {
            responseBuilder.append("    ").append(fieldNumber++).append(". Телефон\n");
        }
        if (invalidFields.containsKey("email")) {
            responseBuilder.append("    ").append(fieldNumber).append(". Email");
        }
        return responseBuilder.toString();
    }

    private void sendMessage(final String chatId, final String text) {
        final var message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error("Error sending message to chat id: %s".formatted(chatId), e);
        }
    }
}