package org.vcpl.lms.portfolio.loanaccount.bulkupload.data;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Builder;
import lombok.Data;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.QueryParam;
import java.io.InputStream;

public record MultipartFileUploadRequest(
    Long fileSize,
    Long productId,
    InputStream inputStream,
    FormDataContentDisposition fileDetails,
    FormDataBodyPart bodyPart,
    String name,
    String description){}

