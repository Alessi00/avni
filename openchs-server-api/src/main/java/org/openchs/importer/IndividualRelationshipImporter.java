package org.openchs.importer;

import org.apache.poi.ss.usermodel.Row;
import org.joda.time.DateTime;
import org.openchs.dao.ConceptRepository;
import org.openchs.dao.UserRepository;
import org.openchs.dao.application.FormElementRepository;
import org.openchs.dao.individualRelationship.IndividualRelationshipTypeRepository;
import org.openchs.domain.individualRelationship.IndividualRelationshipType;
import org.openchs.excel.ImportSheetHeader;
import org.openchs.excel.metadata.ImportAnswerMetaDataList;
import org.openchs.excel.metadata.ImportCalculatedFields;
import org.openchs.excel.metadata.ImportField;
import org.openchs.excel.metadata.ImportSheetMetaData;
import org.openchs.web.IndividualRelationshipController;
import org.openchs.web.request.IndividualRelationshipRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component
public class IndividualRelationshipImporter extends Importer<IndividualRelationshipRequest> {

    private final IndividualRelationshipTypeRepository individualRelationshipTypeRepository;
    private final IndividualRelationshipController individualRelationshipController;

    @Autowired
    public IndividualRelationshipImporter(ConceptRepository conceptRepository, FormElementRepository formElementRepository, IndividualRelationshipTypeRepository individualRelationshipTypeRepository, IndividualRelationshipController individualRelationshipController, UserRepository userRepository) {
        super(conceptRepository, formElementRepository, userRepository);
        this.individualRelationshipTypeRepository = individualRelationshipTypeRepository;
        this.individualRelationshipController = individualRelationshipController;
    }

    @Override
    protected Boolean processRequest(IndividualRelationshipRequest entityRequest) {
        logger.info(String.format("Saving Relationship %s, between %s and %s", entityRequest.getRelationshipTypeUUID(), entityRequest.getIndividualAUUID(), entityRequest.getIndividualBUUID()));
        individualRelationshipController.save(entityRequest);
        return true;
    }

    @Override
    protected IndividualRelationshipRequest makeRequest(List<ImportField> importFields, ImportSheetHeader header, ImportSheetMetaData importSheetMetaData, Row row, ImportAnswerMetaDataList answerMetaDataList, ImportCalculatedFields calculatedFields) {
        IndividualRelationshipRequest individualRelationshipRequest = new IndividualRelationshipRequest();
        Map<String, Consumer<Date>> dateSetters = new HashMap<String, Consumer<Date>>() {{
            put("EnterDateTime", (dateTime) -> {
                individualRelationshipRequest.setEnterDateTime(new DateTime(dateTime));
            });
            put("ExitDateTime", (dateTime) -> {
                individualRelationshipRequest.setExitDateTime(new DateTime(dateTime));
            });
        }};
        Map<String, Consumer<String>> propSetters = new HashMap<String, Consumer<String>>() {{
            put("IndividualAUUID", individualRelationshipRequest::setIndividualAUUID);
            put("IndividualBUUID", individualRelationshipRequest::setIndividualBUUID);
            put("RelationshipType", (name) -> {
                IndividualRelationshipType relationshipType = individualRelationshipTypeRepository.findByName(name);
                if (relationshipType != null) {
                    individualRelationshipRequest.setRelationshipTypeUUID(relationshipType.getUuid());
                } else {
                    throw new IllegalArgumentException(String.format("Invalid relationship type '%s'.", name));
                }
            });
        }};
        for (ImportField importField : importFields) {
            String fieldName = importField.getSystemFieldName();
            Consumer<String> setter = propSetters.get(fieldName);
            Consumer<Date> dateSetter = dateSetters.get(fieldName);
            if (setter != null) {
                String value = importField.getTextValue(row, header, importSheetMetaData);
                setter.accept(value);
            } else if (dateSetter != null) {
                Date dateValue = importField.getDateValue(row, header, importSheetMetaData);
                dateSetter.accept(dateValue);
            } else if (fieldName.equals("User")) {
                setUser(header, importSheetMetaData, row, importField);
            } else if (fieldName.equals("Voided")) {
                individualRelationshipRequest.setVoided(importField.getBooleanValue(row, header, importSheetMetaData));
            } else {
                System.out.println(String.format("Field '%s' not recognised.", fieldName));
            }
        }
        individualRelationshipRequest.setupUuidIfNeeded();
        return individualRelationshipRequest;
    }
}
