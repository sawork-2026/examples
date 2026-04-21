package com.example.batch;

import com.example.batch.model.Person;
import com.example.batch.model.PersonReport;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PersonItemProcessorTest {

    private final PersonItemProcessor processor = new PersonItemProcessor();

    @Test
    void nameIsUppercased() throws Exception {
        Person person = new Person("jill", "doe", "HR", 50000);
        PersonReport report = processor.process(person);
        assertThat(report.fullName()).isEqualTo("JILL DOE");
    }

    @Test
    void engineeringBonus_is20Percent() throws Exception {
        Person person = new Person("Jane", "Doe", "Engineering", 100000);
        PersonReport report = processor.process(person);
        assertThat(report.bonus()).isEqualTo(20000);
    }

    @Test
    void salesBonus_is15Percent() throws Exception {
        Person person = new Person("Joe", "Doe", "Sales", 100000);
        PersonReport report = processor.process(person);
        assertThat(report.bonus()).isEqualTo(15000);
    }

    @Test
    void otherDeptBonus_is10Percent() throws Exception {
        Person person = new Person("John", "Doe", "HR", 100000);
        PersonReport report = processor.process(person);
        assertThat(report.bonus()).isEqualTo(10000);
    }
}
