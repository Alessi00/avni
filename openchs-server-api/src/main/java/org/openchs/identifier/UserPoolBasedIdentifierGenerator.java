package org.openchs.identifier;

import org.openchs.domain.IdentifierAssignment;
import org.openchs.domain.IdentifierSource;
import org.openchs.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;


@Service
@Qualifier("userPoolBasedIdentifierGenerator")
public class UserPoolBasedIdentifierGenerator implements IdentifierGenerator {

    private static final String PREFIX = "prefix";
    private PrefixedUserPoolBasedIdentifierGenerator prefixedUserPoolBasedIdentifierGenerator;


    @Autowired
    public UserPoolBasedIdentifierGenerator(PrefixedUserPoolBasedIdentifierGenerator prefixedUserPoolBasedIdentifierGenerator) {
        this.prefixedUserPoolBasedIdentifierGenerator = prefixedUserPoolBasedIdentifierGenerator;
    }

    @Override
    @Transactional
    public void generateIdentifiers(IdentifierSource identifierSource, User user) {
        String prefix = (String) identifierSource.getOptions().get(PREFIX);
        prefixedUserPoolBasedIdentifierGenerator.generateIdentifiers(identifierSource, user, prefix);
    }

    @Override
    public IdentifierAssignment generateSingleIdentifier(IdentifierSource identifierSource, User user) {
        String prefix = (String) identifierSource.getOptions().get(PREFIX);
        IdentifierAssignment identifierAssignment = prefixedUserPoolBasedIdentifierGenerator.generateSingleIdentifier(identifierSource, user, prefix);
        return identifierAssignment;
    }
}
