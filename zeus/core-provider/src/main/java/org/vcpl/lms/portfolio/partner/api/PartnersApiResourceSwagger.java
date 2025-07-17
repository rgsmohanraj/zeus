/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.vcpl.lms.portfolio.partner.api;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;


final class PartnersApiResourceSwagger {

    private PartnersApiResourceSwagger() {

    }

    @Schema(description = "GetPartnersResponse")
    public static final class GetPartnersResponse {

        private GetPartnersResponse() {

        }

        @Schema(example = "1")
        public Long id;
//        @Schema(example = "Head Partner")
//        public String name;
//        @Schema(example = "Head Partner")
//        public String nameDecorated;
//        @Schema(example = "1")
//        public String externalId;
//        @Schema(example = "[2009, 1, 1]")
//        public LocalDate openingDate;
//        @Schema(example = ".")
//        public String hierarchy;
        // @Schema(example = "")
        // public Long parentId;
        // @Schema(example = "")
        // public String parentName;
    }

    @Schema(description = "GetPartnersTemplateResponse")
    public static final class GetPartnersTemplateResponse {

        private GetPartnersTemplateResponse() {

        }

        @Schema(example = "[2009, 1, 1]")
        public LocalDate openingDate;
        public Collection<GetPartnersResponse> allowedParents;
    }

    @Schema(description = "PostPartnersRequest")
    public static final class PostPartnersRequest {

        private PostPartnersRequest() {

        }

        @Schema(example = "NOCPL")
        public String partnerName;
        @Schema(example = "9 February 2023")
        public LocalDate partnerCompanyRegistrationDate;

        @Schema(example = "116")
        public Integer source;
        @Schema(example = "KLESM1234M")
        public String panCard;
        @Schema(example = "L10110UH1973PLC017654")
        public String cinNumber;

        @Schema(example = "No 12 Periyar Nagar Chennai")
        public String address1;

        @Schema(example = "No 12 Periyar Nagar Chennai")
        public String address2;

        @Schema(example = "33")
        public Integer state;

        @Schema(example = "600987")
        public Long pincode;

        @Schema(example = "117")
        public Integer country;

        @Schema(example = "118")
        public Integer constitution;
        @Schema(example = "Vivriti Capital")
        public String keyPersons;

        @Schema(example = "119")
        public Integer industry;

        @Schema(example = "120")
        public Integer sector;
        @Schema(example = "121")
        public Integer subSector;

        @Schema(example = "10LMMMM0000A1Z1")
        public String gstNumber;
        @Schema(example = "122")
        public Integer gstRegistration;
        @Schema(example = "123")
        public Integer partnerType;

        @Schema(example = "Joshi")
        public String beneficiaryName;

        @Schema(example = "123456788823401")
        public String beneficiaryAccountNumber;

        @Schema(example = "KSEP1234510")
        public String ifscCode;

        @Schema(example = "100002028")
        public Long micrCode;

        @Schema(example = "JKIUINMB210")
        public String swiftCode;

        @Schema(example = "Adyar")
        public String branch;
        @Schema(example = "100000000")
        public BigDecimal modelLimit;
        @Schema(example = "200000000")
        public BigDecimal approvedLimit;
        @Schema(example = "1292474244")
        public BigDecimal pilotLimit;
        @Schema(example = "2454657575")
        public BigDecimal partnerFloatLimit;
        @Schema(example = "4868476957695")
        public BigDecimal balanceLimit;
        @Schema(example = "9 March 2023")
        public LocalDate agreementStartDate;
        @Schema(example = "9 April 2025")
        public LocalDate agreementExpiryDate;
        @Schema(example = "124")
        public Integer underlyingAssets;

        @Schema(example = "125")
        public Integer security;
        @Schema(example = "126")
        public Integer fldgCalculationOn;

        @Schema(example = "Chennai")
        public String city;

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        @Schema(example = "en")
        public String locale;



    }

    @Schema(description = "PostPartnersResponse")
    public static final class PostPartnersResponse {

        private PostPartnersResponse() {

        }

        @Schema(example = "3")
        public Long partnerId;
        @Schema(example = "3")
        public Long resourceId;
    }

    @Schema(description = "PutPartnersPartnerIdRequest")
    public static final class PutPartnersPartnerIdRequest {

        private PutPartnersPartnerIdRequest() {

        }

        @Schema(example = "dd MMMM yyyy")
        public String dateFormat;

        @Schema(example = "en")
        public String locale;
        @Schema(example = "23 November 2022")
        public LocalDate partnerCompanyRegistrationDate;
    }

    @Schema(description = "PutPartnersPartnerIdResponse")
    public static final class PutPartnersPartnerIdResponse {

        private PutPartnersPartnerIdResponse() {

        }

        static final class PutPartnersPartnerIdResponseChanges {

            private PutPartnersPartnerIdResponseChanges() {

            }

            @Schema(example = "Name is updated")
            public String name;
        }

        @Schema(example = "3")
        public Long partnerId;
        @Schema(example = "3")
        public Long resourceId;
        public PutPartnersPartnerIdResponseChanges changes;
    }

}
