package ru.nsu.server.configuration.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();

        // Таймаут на соединение (например, 5000 миллисекунд = 5 секунд)
        // Этот таймаут определяет количество времени, которое RestTemplate будет ждать,
        // пытаясь установить соединение с удаленным сервером.
        // Если соединение не будет установлено за указанный интервал времени, то будет сгенерировано исключение.
        requestFactory.setConnectTimeout(10000);

        // Таймаут на чтение (например, 10000 миллисекунд = 10 секунд)
        // После установления соединения этот таймаут определяет количество времени,
        // в течение которого RestTemplate будет ждать данных (или полного ответа) от сервера.
        // Если сервер не отправит ответ за указанный интервал времени после установления соединения,
        // то будет сгенерировано исключение.

        requestFactory.setReadTimeout(10000);

        restTemplate.setRequestFactory(requestFactory);

        return restTemplate;
    }
}
