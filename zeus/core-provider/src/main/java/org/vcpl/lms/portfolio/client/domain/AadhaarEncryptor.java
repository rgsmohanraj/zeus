package org.vcpl.lms.portfolio.client.domain;

import org.apache.commons.lang3.StringUtils;
import org.vcpl.lms.portfolio.client.utils.AESEncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;


@Converter
public class AadhaarEncryptor implements AttributeConverter<Object,String> {

    @Override
    public String convertToDatabaseColumn(Object attribute) {
        try {
            return !StringUtils.isEmpty(attribute.toString())
                    ? AESEncryptionUtils.encryptWithKey(attribute.toString())
                    : null;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                 InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbValue) {
            return AESEncryptionUtils.decryptWithKey(dbValue);
    }
}
