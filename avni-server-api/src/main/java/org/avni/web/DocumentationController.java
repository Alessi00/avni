package org.avni.web;

import org.avni.application.projections.DocumentationProjection;
import org.avni.dao.DocumentationRepository;
import org.avni.domain.CHSEntity;
import org.avni.domain.Documentation;
import org.avni.service.DocumentationService;
import org.avni.util.ReactAdminUtil;
import org.avni.web.request.webapp.documentation.DocumentationContract;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;

@RestController
public class DocumentationController extends AbstractController<Documentation> implements RestControllerResourceProcessor<Documentation> {

    private final DocumentationRepository documentationRepository;
    private final DocumentationService documentationService;


    @Autowired
    public DocumentationController(DocumentationRepository documentationRepository, DocumentationService documentationService) {
        this.documentationRepository = documentationRepository;
        this.documentationService = documentationService;
    }

    @GetMapping(value = "/web/documentation")
    @PreAuthorize(value = "hasAnyAuthority('organisation_admin')")
    @ResponseBody
    public List<DocumentationContract> getAllDocumentNodes() {
        return documentationService.getAll();
    }

    @PostMapping(value = "/web/documentation")
    @PreAuthorize(value = "hasAnyAuthority('organisation_admin')")
    @Transactional
    @ResponseBody
    public ResponseEntity<DocumentationContract> saveDocumentation(@RequestBody DocumentationContract documentationContract) {
        Documentation savedDocumentation = documentationService.saveDocumentation(documentationContract);
        return ResponseEntity.ok(DocumentationContract.fromDocumentation(savedDocumentation));
    }

    @RequestMapping(value = "/search/documentation", method = RequestMethod.GET)
    @PreAuthorize(value = "hasAnyAuthority('admin', 'user')")
    public Page<DocumentationProjection> searchDocumentation(@RequestParam String name, Pageable pageable) {
        return documentationRepository.findByIsVoidedFalseAndNameIgnoreCaseContains(name, pageable);
    }

    @DeleteMapping(value = "/web/documentation/{id}")
    @PreAuthorize(value = "hasAnyAuthority('organisation_admin')")
    @ResponseBody
    @Transactional
    public void deleteIndividualRelation(@PathVariable String id) {
        Documentation documentation = documentationRepository.findByUuid(id);
        if (documentation != null) {
            documentation.setVoided(true);
            documentation.setName(ReactAdminUtil.getVoidedName(documentation.getName(), documentation.getId()));
            documentationRepository.save(documentation);
        }
    }

}
