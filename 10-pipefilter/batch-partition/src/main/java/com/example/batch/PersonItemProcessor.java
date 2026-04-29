package com.example.batch;

import com.example.batch.model.Person;
import com.example.batch.model.PersonReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PersonItemProcessor implements ItemProcessor<Person, PersonReport> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    private static final ThreadLocal<MessageDigest> MD = ThreadLocal.withInitial(() -> {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    });

    @Override
    public PersonReport process(Person person) {
        String fullName = person.firstName().toUpperCase() + " " + person.lastName().toUpperCase();
        int bonus = switch (person.department()) {
            case "Engineering" -> (int) (person.salary() * 0.20);
            case "Sales"       -> (int) (person.salary() * 0.15);
            default            -> (int) (person.salary() * 0.10);
        };

        // 模拟 CPU 密集型处理（如复杂业务规则、数据校验）
        MessageDigest md = MD.get();
        byte[] data = fullName.getBytes();
        for (int i = 0; i < 200; i++) {
            data = md.digest(data);
        }

        PersonReport report = new PersonReport(fullName, person.department(), person.salary(), bonus);
        log.debug("[{}] Processing: {} -> {}", Thread.currentThread().getName(), person, report);
        return report;
    }
}
