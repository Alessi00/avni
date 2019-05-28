package org.openchs.builder;

import org.openchs.domain.AddressLevelType;
import org.openchs.web.request.AddressLevelTypeContract;

import java.util.UUID;

public class AddressLevelTypeBuilder extends BaseBuilder<AddressLevelType, AddressLevelTypeBuilder> {

    public AddressLevelTypeBuilder(AddressLevelType existingEntity) {
        super(existingEntity, new AddressLevelType());
    }

    public AddressLevelTypeBuilder copy(AddressLevelTypeContract addressLevelTypeContract) {
        String uuid = addressLevelTypeContract.getUuid() == null
                ? UUID.randomUUID().toString()
                : addressLevelTypeContract.getUuid();
        get().setUuid(uuid);
        get().setName(addressLevelTypeContract.getName());
        get().setLevel(addressLevelTypeContract.getLevel());
        return this;
    }

}
