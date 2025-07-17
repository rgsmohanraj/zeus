package org.vcpl.lms.portfolio.partner.exception;

import org.vcpl.lms.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class CannotUpdatePartner extends AbstractPlatformDomainRuleException {

    public CannotUpdatePartner(final Long partnerId, final Long parentId) {
        super("error.msg.office.parentId.same.as.id", "Cannot update office with parent same as self.", partnerId, parentId);
    }
}
