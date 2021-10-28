package org.avni.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.avni.domain.individualRelationship.IndividualRelationship;
import org.avni.geo.Point;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Type;
import org.joda.time.LocalDate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "individual")
@JsonIgnoreProperties({"programEnrolments", "encounters", "relationshipsFromSelfToOthers", "relationshipsFromOthersToSelf"})
@BatchSize(size = 100)
public class Individual extends OrganisationAwareEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_type_id")
    private SubjectType subjectType;

    private String legacyId;

    @NotNull
    private String firstName;

    private String lastName;

    private LocalDate dateOfBirth;

    private boolean dateOfBirthVerified;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "individuala")
    private Set<IndividualRelationship> relationshipsFromSelfToOthers = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "individualB")
    private Set<IndividualRelationship> relationshipsFromOthersToSelf = new HashSet<>();

    @NotNull
    private LocalDate registrationDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gender_id")
    private Gender gender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "address_id")
    private AddressLevel addressLevel;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "individual")
    private Set<ProgramEnrolment> programEnrolments = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "individual")
    private Set<Encounter> encounters = new HashSet<>();

    @Column
    @Type(type = "observations")
    private ObservationCollection observations;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "facility_id")
    private Facility facility;

    @Type(type = "org.avni.geo.PointType")
    @Column
    private Point registrationLocation;

    public static Individual create(String firstName, String lastName, LocalDate dateOfBirth, boolean dateOfBirthVerified, Gender gender, AddressLevel address, LocalDate registrationDate) {
        Individual individual = new Individual();
        individual.firstName = firstName;
        individual.lastName = lastName;
        individual.dateOfBirth = dateOfBirth;
        individual.dateOfBirthVerified = dateOfBirthVerified;
        individual.gender = gender;
        individual.addressLevel = address;
        individual.registrationDate = registrationDate;
        return individual;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public boolean isDateOfBirthVerified() {
        return dateOfBirthVerified;
    }

    public void setDateOfBirthVerified(boolean dateOfBirthVerified) {
        this.dateOfBirthVerified = dateOfBirthVerified;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public Set<ProgramEnrolment> getProgramEnrolments() {
        return programEnrolments;
    }

    public void setProgramEnrolments(Set<ProgramEnrolment> programEnrolments) {
        this.programEnrolments = programEnrolments;
    }

    public AddressLevel getAddressLevel() {
        return addressLevel;
    }

    public void setAddressLevel(AddressLevel addressLevel) {
        this.addressLevel = addressLevel;
    }

    public Set<Encounter> getEncounters() {
        return encounters;
    }

    public void setEncounters(Set<Encounter> encounters) {
        this.encounters = encounters;
    }

    public ObservationCollection getObservations() {
        return observations;
    }

    public void setObservations(ObservationCollection observations) {
        this.observations = observations;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public void addEnrolment(ProgramEnrolment programEnrolment) {
        this.programEnrolments.add(programEnrolment);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    public Point getRegistrationLocation() {
        return registrationLocation;
    }

    public void setRegistrationLocation(Point registrationLocation) {
        this.registrationLocation = registrationLocation;
    }

    public SubjectType getSubjectType() {
        return subjectType;
    }

    public void setSubjectType(SubjectType subjectType) {
        this.subjectType = subjectType;
    }

    public String getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(String legacyId) {
        this.legacyId = legacyId;
    }

    public Set<IndividualRelationship> getRelationshipsFromSelfToOthers() {
        return relationshipsFromSelfToOthers;
    }

    public void setRelationshipsFromSelfToOthers(Set<IndividualRelationship> relationshipsFromSelfToOthers) {
        this.relationshipsFromSelfToOthers = relationshipsFromSelfToOthers;
    }

    public Set<IndividualRelationship> getRelationshipsFromOthersToSelf() {
        return relationshipsFromOthersToSelf;
    }

    public void setRelationshipsFromOthersToSelf(Set<IndividualRelationship> relationshipsFromOthersToSelf) {
        this.relationshipsFromOthersToSelf = relationshipsFromOthersToSelf;
    }

    @JsonIgnore
    public List<Program> getActivePrograms() {
        return programEnrolments.stream().filter(x -> !x.isVoided()).filter(x -> x.getProgramExitDateTime() == null)
                .map(x -> x.getProgram()).collect(Collectors.toList());
    }

    @JsonIgnore
    public Stream<Encounter> getEncounters(boolean removeCancelledEncounters) {
        return this.nonVoidedEncounters().filter(enc -> removeCancelledEncounters ? enc.getCancelDateTime() == null : true);
    }

    @JsonIgnore
    public Stream<Encounter> getEncountersOfType(String encounterTypeName, boolean removeCancelledEncounters) {
        return this.getEncounters(removeCancelledEncounters).filter(enc -> enc.getEncounterType().getName().equals(encounterTypeName));
    }

    @JsonIgnore
    public Stream<Encounter> nonVoidedEncounters() {
        return this.getEncounters().stream().filter(enc -> !enc.isVoided());
    }

    @JsonIgnore
    public Stream<Encounter> scheduledEncounters() {
        return this.getEncounters(true).filter(enc -> enc.getEncounterDateTime() == null && enc.getCancelDateTime() == null);
    }

    @JsonIgnore
    public Stream<Encounter> scheduledEncountersOfType(String encounterTypeName) {
        return this.scheduledEncounters().filter(scheduledEncounter -> scheduledEncounter.getEncounterType().getName().equals(encounterTypeName));
    }

    public void validate() throws ValidationException {
        if (firstName == null) {
            throw new ValidationException("First name cannot be null");
        } else if (subjectType.isPerson()) {
            if (gender == null) {
                throw new ValidationException("Gender cannot be null for Person subject type");
            }
        } else if (registrationDate == null) {
            throw new ValidationException("Registration date cannot be null");
        }
    }
}
