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
package org.apache.fineract.portfolio.loanproducttype.api;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Created by Chirag Gupta on 12/08/17.
 */

public class TypesApiResourceSwagger {
    private TypesApiResourceSwagger() {}

    @Schema(description = "GetTypesResponse")
    public static final class GetTypesResponse {

        private GetTypesResponse() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "Co-Lending")
        public String name;
        @Schema(example = "Type is Co-Lending")
        public String description;

    }

    @Schema(description = "PostTypesRequest")
    public static final class PostTypesRequest {

        private PostTypesRequest() {}

        @Schema(example = "Types")
        public String name;

    }

    @Schema(description = "PostTypesResponse")
    public static final class PostTypesResponse {

        private PostTypesResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutTypesTypeIdRequest")
    public static final class PutTypesTypeIdRequest {

        private PutTypesTypeIdRequest() {}

        @Schema(example = "Co-Lending (2010-2020)")
        public String name;
    }

    @Schema(description = "PutTypesTypeIdResponse")
    public static final class PutTypesTypeIdResponse {

        private PutTypesTypeIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
        public TypesApiResourceSwagger.PutTypesTypeIdResponse changes;
    }
}
