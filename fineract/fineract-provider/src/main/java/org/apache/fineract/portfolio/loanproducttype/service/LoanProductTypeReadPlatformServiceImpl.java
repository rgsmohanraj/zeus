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
package org.apache.fineract.portfolio.loanproducttype.service;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.loanproducttype.data.LoanProductTypeData;
import org.apache.fineract.portfolio.loanproducttype.exception.LoanTypeNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
@Service
public class LoanProductTypeReadPlatformServiceImpl implements LoanProductTypeReadPlatformService {
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

//    @Autowired
//   private LoanProductTypeRepository loanProductTypeRepository;


    @Autowired
    public LoanProductTypeReadPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final class TypeMapper implements RowMapper<LoanProductTypeData> {

        public String schema() {
            return " t.id as id, t.name as name, t.description as description,t.class_id as classId from m_loan_product_type t ";
        }

        @Override
        public LoanProductTypeData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");
//            final Long lpcdId=rs.getLong("lpcdId");
            final Long classId=rs.getLong("class_id");

            return LoanProductTypeData.instance(id, name, description,classId);
        }
    }

//    @Override
//    public LoanProductClass get(String name) {
//        return loanProductTypeRepository.findByName(name);
//    }

    @Override
    @Cacheable(value = "types", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('fn')")
    public Collection<LoanProductTypeData> retrieveAllTypes() {

        this.context.authenticatedUser();

        final TypeMapper rm = new TypeMapper();
        final String sql = "select " + rm.schema() + " order by t.name";

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public LoanProductTypeData retrieveType(final Long typeId) {

        try {
            this.context.authenticatedUser();
            final TypeMapper rm = new TypeMapper();
            final String sql = "select " + rm.schema() + " where t.id = ?";

            final LoanProductTypeData selectedType = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { typeId }); // NOSONAR

            return selectedType;
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanTypeNotFoundException(typeId, e);
        }
    }

}
