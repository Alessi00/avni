package org.avni.web;

import org.junit.Before;
import org.junit.Test;
import org.avni.common.AbstractControllerIntegrationTest;
import org.avni.dao.EncounterTypeRepository;
import org.avni.domain.EncounterType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@Sql({"/test-data.sql"})
public class EncounterTypesIntegrationTest extends AbstractControllerIntegrationTest {
    @Autowired
    private EncounterTypeRepository encounterTypeRepository;

    private void post(Object json) {
        super.post("/encounterTypes", json);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        setUser("demo-admin");
    }

    @Test
    public void shouldCreateEncounterTypes() throws IOException {
        Object json = getJSON("/ref/encounterTypes/encounterTypes.json");
        post(json);

        EncounterType encounterType = encounterTypeRepository.findByUuid("c11c579e-eadf-448c-831c-c73c205aaaca");
        assertThat(encounterType).isNotNull();
        assertThat(encounterType.getName()).isEqualTo("NA EncounterType");
    }

    @Test
    public void shouldVoidAEncounterType() throws IOException {
        Object json = getJSON("/ref/encounterTypes/voidableEncounterType.json");
        post(json);

        EncounterType voidableEncounterType = encounterTypeRepository.findByName("Voidable EncounterType");
        assertThat(voidableEncounterType).isNotNull();
        assertThat(voidableEncounterType.isVoided()).isFalse();

        json = getJSON("/ref/encounterTypes/voidedEncounterType.json");
        post(json);
        EncounterType voidedEncounterType = encounterTypeRepository.findByName("Voidable EncounterType");
        assertThat(voidedEncounterType).isNotNull();
        assertThat(voidedEncounterType.isVoided()).isTrue();
    }

    private Object getJSON(String jsonFile) throws IOException {
        return mapper.readValue(this.getClass().getResource(jsonFile), Object.class);
    }
}
