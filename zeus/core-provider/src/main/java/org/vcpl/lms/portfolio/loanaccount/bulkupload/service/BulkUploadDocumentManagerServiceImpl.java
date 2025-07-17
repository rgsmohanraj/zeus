package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;
import org.vcpl.lms.infrastructure.documentmanagement.api.FileUploadValidator;
import org.vcpl.lms.infrastructure.documentmanagement.command.DocumentCommand;
import org.vcpl.lms.infrastructure.documentmanagement.domain.Document;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class BulkUploadDocumentManagerServiceImpl implements BulkUploadDocumentManagerService {
    @Autowired
    private FileUploadValidator fileUploadValidator;
    @Autowired
    private DocumentWritePlatformServiceJpaRepositoryImpl documentWritePlatformService;

    @Override
    public Document save(Long fileSize, FormDataContentDisposition fileDetails, FormDataBodyPart bodyPart, String name, String description,
                         ByteArrayOutputStream byteArrayOutputStream, DocumentManagementEntity documentManagementEntity) {
        InputStream inputStream = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        fileUploadValidator.validate(fileSize,inputStream, fileDetails, bodyPart);
        final DocumentCommand documentCommand = new DocumentCommand(null, null,
                documentManagementEntity.toString(),
                1l, name, fileDetails.getFileName(), fileSize, bodyPart.getMediaType().toString(),
                description, generateFileLocation(documentManagementEntity));
        if(this.documentWritePlatformService.isFileNameExist(fileDetails.getFileName())) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.loan.upload.invalid.productId",
                    "File with name '" + fileDetails.getFileName() + "' already exist",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
        return this.documentWritePlatformService.create(documentCommand, inputStream);
    }
}
