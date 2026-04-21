package com.example.batch;

import com.example.batch.model.Person;
import com.example.batch.model.PersonReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

// Filter: 将 Person 转换为 PersonReport，计算奖金
public class PersonItemProcessor implements ItemProcessor<Person, PersonReport> {

    private static final Logger log = LoggerFactory.getLogger(PersonItemProcessor.class);

    @Override
    public PersonReport process(Person person) {
        String fullName = person.firstName().toUpperCase() + " " + person.lastName().toUpperCase();
        int bonus = switch (person.department()) {
            case "Engineering" -> (int) (person.salary() * 0.20);
            case "Sales"       -> (int) (person.salary() * 0.15);
            default            -> (int) (person.salary() * 0.10);
        };

        PersonReport report = new PersonReport(fullName, person.department(), person.salary(), bonus);
        log.info("Processing: {} -> {}", person, report);
        return report;
    }
}
