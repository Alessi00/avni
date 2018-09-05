package org.openchs.excel.metadata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportNonCalculatedFields extends ArrayList<ImportNonCalculatedField> {
    public void addUserField(String userFieldName, ImportNonCalculatedField nonCalculatedField) {
        nonCalculatedField.setUserField(userFieldName);
    }

    public List<ImportField> getFieldsFor(ImportSheetMetaData sheetMetaData) {
        return this.stream().filter(field -> {
            String userField = field.getUserField();
            return field.getFormType().equals(sheetMetaData.getFormType()) && userField != null;
        }).collect(Collectors.toList());
    }

    public List<String> getUserFileTypes() {
        return this.stream().map(ImportNonCalculatedField::getUserFileType).distinct().collect(Collectors.toList());
    }
}