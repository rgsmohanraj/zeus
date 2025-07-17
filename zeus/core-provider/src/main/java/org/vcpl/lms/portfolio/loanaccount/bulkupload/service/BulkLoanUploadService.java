package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.vcpl.lms.infrastructure.documentmanagement.domain.Document;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.*;
import org.vcpl.lms.useradministration.domain.AppUser;

import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public interface BulkLoanUploadService {

    Response buildBulkLoansUploadTemplate(Long partnerId, Long productId);

    public BulkUploadResponse initiateLoanProcessing(final MultipartFileUploadRequest multipartFileUploadRequest, final Long productId,
                                                     final AppUser systemAppUser) throws Exception;

    default ByteArrayOutputStream copy(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        inputStream.transferTo(byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return byteArrayOutputStream;
    }
}
