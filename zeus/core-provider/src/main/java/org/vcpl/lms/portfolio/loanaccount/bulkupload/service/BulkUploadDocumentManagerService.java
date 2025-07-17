package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.vcpl.lms.infrastructure.core.service.ThreadLocalContextUtil;
import org.vcpl.lms.infrastructure.documentmanagement.contentrepository.FileSystemContentRepository;
import org.vcpl.lms.infrastructure.documentmanagement.domain.Document;
import org.vcpl.lms.infrastructure.documentmanagement.service.DocumentWritePlatformServiceJpaRepositoryImpl.DocumentManagementEntity;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public interface BulkUploadDocumentManagerService {
    Document save(Long fileSize, FormDataContentDisposition fileDetails, FormDataBodyPart bodyPart,
                  String name, String description, ByteArrayOutputStream byteArrayOutputStream, DocumentManagementEntity documentManagementEntity);
    default String generateFileLocation(final DocumentManagementEntity documentManagementEntity) {
        StringBuilder fileLocationBuilder = new StringBuilder();
        fileLocationBuilder.append(FileSystemContentRepository.FINERACT_BASE_DIR).append(File.separator)
                .append(ThreadLocalContextUtil.getTenant().getName().replaceAll(" ", "").trim()).append(File.separator)
                .append("documents").append(File.separator)
                .append(documentManagementEntity).append(File.separator)
                .append(LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        return fileLocationBuilder.toString();
    }
}
