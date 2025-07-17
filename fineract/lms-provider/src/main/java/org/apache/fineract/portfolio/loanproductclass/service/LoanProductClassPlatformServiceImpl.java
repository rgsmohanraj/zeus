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
package org.apache.fineract.portfolio.loanproductclass.service;

import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;

import org.apache.fineract.portfolio.loanproductclass.data.LoanProductClassData;
import org.apache.fineract.portfolio.loanproductclass.exception.LoanClassNotFoundException;
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
public class LoanProductClassPlatformServiceImpl implements LoanProductClassReadPlatformService {
    private final JdbcTemplate jdbcTemplate;
    private final PlatformSecurityContext context;

    @Autowired
    public LoanProductClassPlatformServiceImpl(final PlatformSecurityContext context, final JdbcTemplate jdbcTemplate) {
        this.context = context;
        this.jdbcTemplate = jdbcTemplate;
    }

    private static final class ClassMapper implements RowMapper<LoanProductClassData> {

        public String schema() {
            return " c.id as id, c.name as name, c.description as description from m_loan_product_class c ";
        }

        @Override
        public LoanProductClassData mapRow(final ResultSet rs, @SuppressWarnings("unused") final int rowNum) throws SQLException {

            final Long id = rs.getLong("id");
            final String name = rs.getString("name");
            final String description = rs.getString("description");

            return LoanProductClassData.instance(id, name, description);
        }
    }

    @Override
    @Cacheable(value = "classes", key = "T(org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil).getTenant().getTenantIdentifier().concat('cl')")
    public Collection<LoanProductClassData> retrieveAllClasses() {

        this.context.authenticatedUser();

        final LoanProductClassPlatformServiceImpl.ClassMapper rm = new LoanProductClassPlatformServiceImpl.ClassMapper();
        final String sql = "select " + rm.schema() + " order by c.name";

        return this.jdbcTemplate.query(sql, rm); // NOSONAR
    }

    @Override
    public LoanProductClassData retrieveClass(final Long classId) {

        try {
            this.context.authenticatedUser();

            final LoanProductClassPlatformServiceImpl.ClassMapper rm = new LoanProductClassPlatformServiceImpl.ClassMapper();
            final String sql = "select " + rm.schema() + " where c.id = ?";

            final LoanProductClassData selectedClass = this.jdbcTemplate.queryForObject(sql, rm, new Object[] { classId }); // NOSONAR

            return selectedClass;
        } catch (final EmptyResultDataAccessException e) {
            throw new LoanClassNotFoundException(classId, e);
        }
    }
}
