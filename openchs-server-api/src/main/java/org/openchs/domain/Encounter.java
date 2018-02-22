package org.openchs.domain;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "encounter")
public class Encounter extends OrganisationAwareEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @NotNull
    @JoinColumn(name = "encounter_type_id")
    private EncounterType encounterType;

    @Column
    private DateTime encounterDateTime;

    @Column
    @Type(type = "observations")
    private ObservationCollection observations;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_id")
    private Individual individual;

    @Column(nullable = false)
    private boolean isVoided = false;

    public EncounterType getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterType encounterType) {
        this.encounterType = encounterType;
    }

    public DateTime getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(DateTime encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public Individual getIndividual() {
        return individual;
    }

    public void setIndividual(Individual individual) {
        this.individual = individual;
    }

    public ObservationCollection getObservations() {
        return observations;
    }

    public void setObservations(ObservationCollection observations) {
        this.observations = observations;
    }

    public boolean isVoided() {
        return isVoided;
    }

    public void setVoided(boolean voided) {
        this.isVoided = voided;
    }
}