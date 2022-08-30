package org.avni.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.BatchSize;
import org.joda.time.DateTime;
import org.avni.application.projections.BaseProjection;
import org.avni.domain.EncounterType.EncounterTypeProjection;
import org.springframework.data.rest.core.config.Projection;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Entity
@Table(name = "program_encounter")
@JsonIgnoreProperties({"programEnrolment", "individual"})
@BatchSize(size = 100)
public class ProgramEncounter extends AbstractEncounter {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_enrolment_id")
    private ProgramEnrolment programEnrolment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_id")
    private Individual individual;

    public ProgramEnrolment getProgramEnrolment() {
        return programEnrolment;
    }

    public void setProgramEnrolment(ProgramEnrolment programEnrolment) {
        this.programEnrolment = programEnrolment;
    }

    @JsonIgnore
    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    @Projection(name = "ProgramEncounterProjectionMinimal", types = {ProgramEncounter.class})
    public interface ProgramEncounterProjectionMinimal extends BaseProjection {
        EncounterTypeProjection getEncounterType();

        String getName();

        DateTime getEncounterDateTime();

        DateTime getEarliestVisitDateTime();

        DateTime getMaxVisitDateTime();

        DateTime getCancelDateTime();
    }
}
