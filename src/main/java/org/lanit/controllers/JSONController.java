package org.lanit.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.lanit.modelsJson.RequestJson;
import org.lanit.modelsJson.ResponseJson;
import org.lanit.validate.CheckSnils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.regex.Pattern;

@Controller
public class JSONController {

    private final Pattern SNILS_PATTERN = Pattern.compile("^\\d{3}-\\d{3}-\\d{3}\\s\\d{2}$");
    @PostMapping(value = "snils")
    public Object postResponse(@RequestBody String requestBody) throws IOException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            CheckSnils checkSnils = new CheckSnils();
            RequestJson requestJson = objectMapper.readValue(requestBody, RequestJson.class);
            ResponseJson responseJson = new ResponseJson();
            String snils = requestJson.getSnils();

            if (SNILS_PATTERN.matcher(snils).matches()) {
                String normalizedSNILS = snils.replace("-", "").substring(0, 9);
                Logger.info("normalizedSNILS: " + normalizedSNILS);
                Logger.info("snils: " + snils);
                int checksum = Integer.parseInt(snils.replace(" ", "").substring(11, 13));
                Logger.info("checksum: " + checksum);
                if (!checkSnils.eligible_for_calculation(normalizedSNILS)) {
                    responseJson.setMessage("success");
                    responseJson.setSnils(snils);
                    return ResponseEntity.status(200).header("Content-Type", "application/json").body(responseJson);
                }
                Logger.info("normalizedSNILS: " + normalizedSNILS);
                int calculated_checksum = checkSnils.calculate_checksum(normalizedSNILS);
                Logger.info("calculated_checksum: " + calculated_checksum);
                if (checksum == calculated_checksum) {
                    responseJson.setMessage("success");
                    responseJson.setSnils(snils);
                    return ResponseEntity.status(200).header("Content-Type", "application/json").body(responseJson);
                } else {
                    return ResponseEntity.status(400).header("Content-Type", "application/json").body(String.format("{\n   \"message\": \"Error: uncorrected snils\",\n    \"snils\": \"%s\"\n}", snils));
                }
            } else {
                return ResponseEntity.status(400).header("Content-Type", "application/json").body(String.format("{\n   \"message\": \"Error: uncorrected snils\",\n    \"snils\": \"%s\"\n}", snils));
            }
        } catch (IOException e) {
            Logger.error("Ошибка при парсинге тела запроса: " + requestBody);
            return ResponseEntity.status(400).header("Content-Type", "application/json").body(String.format("{\n   \"message\": \"Error: uncorrected json\",\n    \"request\": %s\n}", requestBody));
        }
    }
}
