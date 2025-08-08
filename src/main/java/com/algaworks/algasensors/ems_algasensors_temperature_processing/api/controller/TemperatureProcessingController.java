package com.algaworks.algasensors.ems_algasensors_temperature_processing.api.controller;

import com.algaworks.algasensors.ems_algasensors_temperature_processing.api.model.TemperatureLogOutput;
import com.algaworks.algasensors.ems_algasensors_temperature_processing.common.IdGenerator;
import com.algaworks.algasensors.ems_algasensors_temperature_processing.infrastructure.rabbitmq.RabbitMQConfig;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/sensors/{sensorId}/temperatures/data")
@Slf4j
@RequiredArgsConstructor
public class TemperatureProcessingController {

    private final RabbitTemplate rabbitTemplate;

    @PostMapping(consumes = MediaType.TEXT_PLAIN_VALUE)
    public void data(@PathVariable TSID sensorId, @RequestBody String input) {
        if(input ==  null || input.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        Double temperature;

        try {
            temperature = Double.parseDouble(input);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        TemperatureLogOutput temperatureLogOutput = TemperatureLogOutput.builder()
                .id(IdGenerator.generateTimeBasedUUID())
                .sensorId(sensorId)
                .value(temperature)
                .registerAt(OffsetDateTime.now())
                .build();

        log.info(temperatureLogOutput.toString());

        rabbitTemplate.convertAndSend(RabbitMQConfig.FANOUT_EXCHANGE_NAME, "", temperatureLogOutput);
    }
}
