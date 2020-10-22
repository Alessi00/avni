package org.openchs.web.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.joda.time.DateTime;
import org.openchs.web.request.common.CommonAbstractEncounterRequest;

import java.util.ArrayList;
import java.util.List;

public class ProgramEncountersContract extends CHSRequest {
    String name;
    DateTime cancelDateTime;
    DateTime earliestVisitDateTime;
    DateTime maxVisitDateTime;
    EncounterTypeContract encounterType;
    private DateTime encounterDateTime;
    @JsonInclude
    private List<ObservationContract> observations = new ArrayList<>();
    @JsonInclude
    private List<ObservationContract> cancelObservations = new ArrayList<>();
    private String subjectUUID;

    public String getEnrolmentUUID() {
        return enrolmentUUID;
    }

    public void setEnrolmentUUID(String enrolmentUUID) {
        this.enrolmentUUID = enrolmentUUID;
    }

    private String enrolmentUUID;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateTime getCancelDateTime() {
        return cancelDateTime;
    }

    public void setCancelDateTime(DateTime cancelDateTime) {
        this.cancelDateTime = cancelDateTime;
    }

    public DateTime getEarliestVisitDateTime() {
        return earliestVisitDateTime;
    }

    public void setEarliestVisitDateTime(DateTime earliestVisitDateTime) {
        this.earliestVisitDateTime = earliestVisitDateTime;
    }

    public DateTime getMaxVisitDateTime() {
        return maxVisitDateTime;
    }

    public void setMaxVisitDateTime(DateTime maxVisitDateTime) {
        this.maxVisitDateTime = maxVisitDateTime;
    }

    public EncounterTypeContract getEncounterType() {
        return encounterType;
    }

    public void setEncounterType(EncounterTypeContract encounterType) {
        this.encounterType = encounterType;
    }

    public DateTime getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(DateTime encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public List<ObservationContract> getObservations() {
        return observations;
    }

    public void setObservations(List<ObservationContract> observations) {
        this.observations = observations;
    }

    public List<ObservationContract> getCancelObservations() {
        return cancelObservations;
    }

    public void setCancelObservations(List<ObservationContract> cancelObservations) {
        this.cancelObservations = cancelObservations;
    }

    public String getSubjectUUID() {
        return subjectUUID;
    }

    public void setSubjectUUID(String subjectUUID) {
        this.subjectUUID = subjectUUID;
    }
}
