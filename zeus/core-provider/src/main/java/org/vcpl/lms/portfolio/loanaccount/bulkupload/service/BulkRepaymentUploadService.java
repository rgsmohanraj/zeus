package org.vcpl.lms.portfolio.loanaccount.bulkupload.service;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.vcpl.lms.infrastructure.documentmanagement.domain.Document;
import org.vcpl.lms.portfolio.loanaccount.bulkupload.data.*;

import jakarta.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public interface BulkRepaymentUploadService {
    BulkUploadResponse initiateRepayment(final MultipartFileUploadRequest multipartFileUploadRequest,Long productId)
            throws IOException, InstantiationException, IllegalAccessException;
    List<RepaymentRecord> parseXlsxTemplates(final XSSFWorkbook workbook,final Document document,Long productId) throws InstantiationException,
            IllegalAccessException;

    default ByteArrayOutputStream copy(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        inputStream.transferTo(byteArrayOutputStream);
        byteArrayOutputStream.flush();
        return byteArrayOutputStream;
    }
    Response buildBulkRepaymentTemplate(Long productId);
}
