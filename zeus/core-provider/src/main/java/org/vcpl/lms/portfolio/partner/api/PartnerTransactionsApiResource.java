///**
// * Licensed to the Apache Software Foundation (ASF) under one
// * or more contributor license agreements. See the NOTICE file
// * distributed with this work for additional information
// * regarding copyright ownership. The ASF licenses this file
// * to you under the Apache License, Version 2.0 (the
// * "License"); you may not use this file except in compliance
// * with the License. You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied. See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.vcpl.lms.organisation.partner.api;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.HashSet;
//import java.util.Set;
//import jakarta.ws.rs.Consumes;
//import jakarta.ws.rs.DELETE;
//import jakarta.ws.rs.GET;
//import jakarta.ws.rs.POST;
//import jakarta.ws.rs.Path;
//import jakarta.ws.rs.PathParam;
//import jakarta.ws.rs.Produces;
//import jakarta.ws.rs.core.Context;
//import jakarta.ws.rs.core.MediaType;
//import jakarta.ws.rs.core.UriInfo;
//import org.vcpl.lms.commands.domain.CommandWrapper;
//import org.vcpl.lms.commands.service.CommandWrapperBuilder;
//import org.vcpl.lms.commands.service.PortfolioCommandSourceWritePlatformService;
//import org.vcpl.lms.infrastructure.core.api.ApiRequestParameterHelper;
//import org.vcpl.lms.infrastructure.core.data.CommandProcessingResult;
//import org.vcpl.lms.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
//import org.vcpl.lms.infrastructure.core.serialization.DefaultToApiJsonSerializer;
//import org.vcpl.lms.infrastructure.security.service.PlatformSecurityContext;
//import org.vcpl.lms.organisation.partner.data.PartnerTransactionData;
//import PartnerReadPlatformService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Scope;
//import org.springframework.stereotype.Component;
//
//@Path("/partnertransactions")
//@Component
//@Scope("singleton")
//
//public class PartnerTransactionsApiResource {
//
//    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(
//            Arrays.asList("id", "transactionDate", "fromPartnerId", "fromPartnerName", "toPartnerId", "toPartnerIdName", "currencyCode",
//                    "digitsAfterDecimal", "inMultiplesOf", "transactionAmount", "description", "allowedPartners", "currencyOptions"));
//
//    private final String resourceNameForReadPermissions = "OFFICE";
//
//    private final PlatformSecurityContext context;
//    private final PartnerReadPlatformService readPlatformService;
//    private final DefaultToApiJsonSerializer<PartnerTransactionData> toApiJsonSerializer;
//    private final ApiRequestParameterHelper apiRequestParameterHelper;
//    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
//
//    @Autowired
//    public PartnerTransactionsApiResource(final PlatformSecurityContext context, final PartnerReadPlatformService readPlatformService,
//            final DefaultToApiJsonSerializer<PartnerTransactionData> toApiJsonSerializer,
//            final ApiRequestParameterHelper apiRequestParameterHelper,
//            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
//        this.context = context;
//        this.readPlatformService = readPlatformService;
//        this.toApiJsonSerializer = toApiJsonSerializer;
//        this.apiRequestParameterHelper = apiRequestParameterHelper;
//        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
//    }
//
//    @GET
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String retrievePartnerTransactions(@Context final UriInfo uriInfo) {
//
//        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForReadPermissions);
//
//        final Collection<PartnerTransactionData> partnerTransactions = this.readPlatformService.retrieveAllPartnerTransactions();
//
//        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
//        return this.toApiJsonSerializer.serialize(settings, partnerTransactions, RESPONSE_DATA_PARAMETERS);
//    }
//
//    @GET
//    @Path("template")
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String newPartnerTransactionDetails(@Context final UriInfo uriInfo) {
//
//        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForReadPermissions);
//
//        final PartnerTransactionData partnerTransactionData = this.readPlatformService.retrieveNewPartnerTransactionDetails();
//
//        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
//        return this.toApiJsonSerializer.serialize(settings, partnerTransactionData, RESPONSE_DATA_PARAMETERS);
//    }
//
//    @POST
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String transferMoneyFrom(final String apiRequestBodyAsJson) {
//
//        final CommandWrapper commandRequest = new CommandWrapperBuilder().createPartnerTransaction().withJson(apiRequestBodyAsJson).build();
//
//        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
//
//        return this.toApiJsonSerializer.serialize(result);
//    }
//
//    @DELETE
//    @Path("{transactionId}")
//    @Consumes({ MediaType.APPLICATION_JSON })
//    @Produces({ MediaType.APPLICATION_JSON })
//    public String delete(@PathParam("transactionId") final Long transactionId) {
//
//        final CommandWrapper commandRequest = new CommandWrapperBuilder().deletePartnerTransaction(transactionId).build();
//
//        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
//
//        return this.toApiJsonSerializer.serialize(result);
//    }
//}
