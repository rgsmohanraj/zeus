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

package org.apache.fineract.portfolio.loanproductclass.api;

import io.swagger.v3.oas.annotations.media.Schema;
/**
 * Created by Chirag Gupta on 12/08/17.
 */

public class ClassesApiResourceSwagger {
    private ClassesApiResourceSwagger() {}

    @Schema(description = "GetClassesResponse")
    public static final class GetClassesResponse {

        private GetClassesResponse() {}

        @Schema(example = "1")
        public Integer id;
        @Schema(example = "Asset")
        public String name;

    }

    @Schema(description = "PostClassesRequest")
    public static final class PostClassesRequest {

        private PostClassesRequest() {}

        @Schema(example = "Asset")
        public String name;
    }

    @Schema(description = "PostClassesResponse")
    public static final class PostClassesResponse {

        private PostClassesResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
    }

    @Schema(description = "PutClassesClassIdRequest")
    public static final class PutClassesClassIdRequest {

        private PutClassesClassIdRequest() {}

        @Schema(example = "Asset (2010-2020)")
        public String name;
    }

    @Schema(description = "PutClassesClassIdResponse")
    public static final class PutClassesClassIdResponse {

        private PutClassesClassIdResponse() {}

        @Schema(example = "1")
        public Integer resourceId;
        public ClassesApiResourceSwagger.PutClassesClassIdResponse changes;
    }
}
