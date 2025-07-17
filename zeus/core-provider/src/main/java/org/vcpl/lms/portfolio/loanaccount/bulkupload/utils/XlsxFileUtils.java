package org.vcpl.lms.portfolio.loanaccount.bulkupload.utils;

import org.vcpl.lms.infrastructure.core.data.ApiParameterError;
import org.vcpl.lms.infrastructure.core.exception.PlatformApiDataValidationException;

import java.util.ArrayList;
import java.util.List;

public class XlsxFileUtils {

    public static void checkIsSupportedFileType(String fileName) {
        String extension = fileName.substring(fileName.length() - 5, fileName.length());
        if(!extension.equals(".xlsx")) {
            List<ApiParameterError> errors = new ArrayList<>();
            errors.add(ApiParameterError.generalError("validation.msg.filetype.does.no.match",
                    "Invalid Filetype",
                    ""));
            throw new PlatformApiDataValidationException(errors);
        }
    }

    /**
     * Units is
     * 0 - B
     * 1 - KB
     * 2 - MB
     * 3 - GB
     * 4 - TB
     * @param size
     */
    public static void checkFileSize(Long size) {
        // final String[] units = new String[] {"B", "KB", "MB", "GB", "TB"};
        int unit = (int)(Math.log10(size) / Math.log10(1024));
        if(unit > 1 ) {
            double fileSize = (size / Math.pow(1024, unit));
            if(fileSize > 5.0) {
                List<ApiParameterError> errors = new ArrayList<>();
                errors.add(ApiParameterError.generalError("validation.msg.fileSize.is.too.large",
                        "Uploaded file size should less than 5MB.",
                        ""));
                throw new PlatformApiDataValidationException(errors);
            }
        }
    }
}
