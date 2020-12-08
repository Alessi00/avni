import {
  Individual,
  ModelGeneral as General,
  Observation,
  Concept,
  ProgramEncounter,
  Program,
  ProgramEnrolment,
  IndividualRelationship,
  IndividualRelationshipType,
  IndividualRelation,
  Encounter,
  EncounterType,
  Gender,
  AddressLevel,
  SubjectType,
  ConceptAnswer
} from "avni-models";
import { map, isNil } from "lodash";
import { conceptService } from "dataEntryApp/services/ConceptService";
import { subjectService } from "../dataEntryApp/services/SubjectService";
import { addressLevelService } from "../dataEntryApp/services/AddressLevelService";

export const mapIndividual = individualDetails => {
  const individual = General.assignFields(
    individualDetails,
    new Individual(),
    ["uuid", "firstName", "lastName", "voided"],
    ["dateOfBirth", "registrationDate"]
  );
  const gender = new Gender();
  gender.name = individualDetails.gender;
  gender.uuid = individualDetails.genderUUID;
  individual.gender = gender;

  const subjectType = new SubjectType();
  if (individualDetails.subjectType) {
    General.assignFields(individualDetails.subjectType, subjectType, [
      "name",
      "uuid",
      "type",
      "group",
      "household",
      "voided"
    ]);
  }
  individual.subjectType = subjectType;

  const addressLevel = new AddressLevel();
  addressLevel.uuid = individualDetails.addressLevelUUID;
  addressLevel.name = individualDetails.addressLevel;
  addressLevel.type = individualDetails.addressLevelTypeName;
  individual.lowestAddressLevel = addressLevel;

  return individual;
};

export const mapObservations = observations => {
  if (observations)
    return observations.map(observation => {
      return mapObservation(observation);
    });
};

function getAnswers(answersJson) {
  return map(answersJson, answerJson => {
    const conceptAnswer = new ConceptAnswer();
    conceptAnswer.answerOrder = answerJson.order;
    conceptAnswer.abnormal = answerJson.abnormal;
    conceptAnswer.unique = answerJson.unique;
    conceptAnswer.voided = !!answerJson.voided;
    conceptAnswer.concept = mapConcept(answerJson);
    return conceptAnswer;
  });
}

export const mapConcept = conceptJson => {
  const concept = General.assignFields(conceptJson, new Concept(), [
    "uuid",
    "name",
    "lowAbsolute",
    "lowNormal"
  ]);
  concept.datatype = conceptJson["dataType"];
  concept.hiNormal = conceptJson["highNormal"];
  concept.hiAbsolute = conceptJson["highAbsolute"];
  concept.answers = getAnswers(conceptJson["answers"]);
  conceptService.addConcept(concept);
  return concept;
};

export const mapObservation = observationJson => {
  if (observationJson) {
    const observation = new Observation();
    const concept = mapConcept(observationJson.concept);

    observationJson.subjects &&
      observationJson.subjects.forEach(subject => {
        subjectService.addSubject(subject);
      });
    observationJson.location && addressLevelService.addAddressLevel(observationJson.location);

    const value = concept.getValueWrapperFor(observationJson.value);
    observation.concept = concept;
    observation.valueJSON = value;
    return observation;
  }
};

//subject Dashboard profile Tab
export const mapProfile = subjectProfile => {
  if (subjectProfile) {
    let individual = mapIndividual(subjectProfile);
    individual.observations = mapObservations(subjectProfile["observations"]);
    individual.relationships = mapRelationships(subjectProfile["relationships"]);
    return individual;
  }
};

export const mapProgramEnrolment = (json, subject) => {
  const programEnrolment = new ProgramEnrolment();
  programEnrolment.uuid = json.uuid;
  if (json.enrolmentDateTime) programEnrolment.enrolmentDateTime = new Date(json.enrolmentDateTime);
  if (json.programExitDateTime)
    programEnrolment.programExitDateTime = new Date(json.programExitDateTime);
  programEnrolment.programExitObservations = mapObservations(json.exitObservations);
  programEnrolment.observations = mapObservations(json.observations) || [];
  const program = new Program();
  program.uuid = json.programUuid;
  programEnrolment.program = program;
  programEnrolment.voided = false;
  if (subject) programEnrolment.individual = subject;
  if (!isNil(json.programEncounters)) {
    programEnrolment.encounters = map(json.programEncounters, programEncounter =>
      mapProgramEncounter(programEncounter)
    );
  }
  return programEnrolment;
};

export const mapRelationships = relationshipList => {
  if (relationshipList) {
    return relationshipList.map(relationship => {
      return mapRelations(relationship);
    });
  }
};

export const mapRelations = relationShipJson => {
  const individualRelationship = General.assignFields(
    relationShipJson,
    new IndividualRelationship(),
    ["uuid", "id", "exitDateTime", "enterDateTime"]
  );
  individualRelationship.relationship = mapIndividualRelationshipType(
    relationShipJson["relationshipType"]
  );
  individualRelationship.individualB = mapIndividual(relationShipJson["individualB"]);
  return individualRelationship;
};

export const mapIndividualRelationshipType = relationShipType => {
  if (relationShipType) {
    const individualRelationShipType = General.assignFields(
      relationShipType,
      new IndividualRelationshipType(),
      ["uuid"]
    );
    individualRelationShipType.individualAIsToBRelation = mapIndividualRelation(
      relationShipType["individualAIsToBRelation"]
    );
    individualRelationShipType.individualBIsToARelation = mapIndividualRelation(
      relationShipType["individualBIsToARelation"]
    );
    return individualRelationShipType;
  }
};

export const mapIndividualRelation = individualRelation => {
  if (individualRelation) {
    return General.assignFields(individualRelation, new IndividualRelation(), ["name"]);
  }
};

// program Tab subject Dashboard
export const mapProgram = subjectProgram => {
  if (subjectProgram) {
    let programIndividual = General.assignFields(subjectProgram, new Individual(), ["uuid"]);
    programIndividual.enrolments = mapEnrolments(subjectProgram.enrolments);
    programIndividual.exitObservations = mapEnrolments(subjectProgram.exitObservations);
    return programIndividual;
  }
};
export const mapEnrolments = enrolmentList => {
  if (enrolmentList)
    return enrolmentList.map(enrolment => {
      let programEnrolment = General.assignFields(
        enrolment,
        new ProgramEnrolment(),
        ["uuid"],
        ["programExitDateTime", "enrolmentDateTime"]
      );
      programEnrolment.observations = mapObservations(enrolment["observations"]);
      programEnrolment.encounters = mapProgramEncounters(enrolment["programEncounters"]);
      programEnrolment.exitObservations = mapObservations(enrolment["exitObservations"]);
      programEnrolment.program = mapOperationalProgram(enrolment);
      programEnrolment.uuid = enrolment.uuid;
      programEnrolment.id = enrolment.id;
      return programEnrolment;
    });
};

//To get list Program Encounters
export const mapProgramEncounters = programEncountersList => {
  if (programEncountersList)
    return programEncountersList.map(programEncounters => {
      const programEncounter = General.assignFields(
        programEncounters,
        new ProgramEncounter(),
        ["uuid", "name"],
        ["maxVisitDateTime", "earliestVisitDateTime", "encounterDateTime", "cancelDateTime"]
      );
      programEncounter.encounterType = mapEncounterType(programEncounters["encounterType"]);
      return programEncounter;
    });
};

export const mapOperationalProgram = enrolment => {
  const operationalProgram = General.assignFields(enrolment, new Program(), [
    "operationalProgramName"
  ]);
  operationalProgram.name = enrolment.programName;
  operationalProgram.uuid = enrolment.programUuid;
  return operationalProgram;
};

export const mapEncounterType = encounterType => {
  return General.assignFields(encounterType, new EncounterType(), ["name", "uuid"]);
};

// general tab subject Dashbord
export const mapGeneral = subjectGeneral => {
  if (subjectGeneral && subjectGeneral.encounters) {
    return subjectGeneral.encounters.map(encounters => {
      let generalEncounter = General.assignFields(
        encounters,
        new Encounter(),
        ["uuid", "name"],
        ["encounterDateTime", "earliestVisitDateTime", "maxVisitDateTime", "cancelDateTime"]
      );
      generalEncounter.encounterType = mapEncounterType(encounters.encounterType);
      return generalEncounter;
    });
  }
};

//To get Program Encounter with observations
export const mapProgramEncounter = programEncounter => {
  if (programEncounter) {
    const programEncounterObj = General.assignFields(
      programEncounter,
      new ProgramEncounter(),
      ["uuid", "name"],
      ["maxVisitDateTime", "earliestVisitDateTime", "encounterDateTime", "cancelDateTime"]
    );
    programEncounterObj.encounterType = mapEncounterType(programEncounter["encounterType"]);
    programEncounterObj.observations = mapObservations(programEncounter["observations"]);
    programEncounterObj.cancelObservations = mapObservations(
      programEncounter["cancelObservations"]
    );
    programEncounterObj.subjectUuid = programEncounter["subjectUUID"];
    programEncounterObj.enrolmentUuid = programEncounter["enrolmentUUID"];
    return programEncounterObj;
  }
};

export const mapEncounter = encounterDetails => {
  if (encounterDetails) {
    const encounter = General.assignFields(
      encounterDetails,
      new Encounter(),
      ["uuid", "name"],
      ["earliestVisitDateTime", "maxVisitDateTime", "encounterDateTime", "cancelDateTime"]
    );
    encounter.encounterType = mapEncounterType(encounterDetails.encounterType);
    encounter.observations = mapObservations(encounterDetails["observations"]);
    encounter.cancelObservations = mapObservations(encounterDetails["cancelObservations"]);
    encounter.subjectUuid = encounterDetails["subjectUUID"];
    return encounter;
  }
};
