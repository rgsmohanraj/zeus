package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.vcpl.lms.infrastructure.documentmanagement.domain.Document;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.BulkUploadResponse;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.MultipartFileUploadRequest;
import org.vcpl.lms.useradministration.domain.AppUser;

import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


public interface BulkChargeRepaymentService {
    Response buildBulkChargeRepaymentUploadTemplate(Long partnerId, Long productId);
    public BulkUploadResponse initiateChargeRepayment(final MultipartFileUploadRequest multipartFileUploadRequest, final Long productId) throws IOException, InstantiationException, IllegalAccessException;

    default ByteArrayOutputStream copy(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        inputStream.transferTo(byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return byteArrayOutputStream;
    }
}
