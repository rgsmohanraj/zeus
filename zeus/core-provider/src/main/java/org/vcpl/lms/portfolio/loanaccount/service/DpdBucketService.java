package org.vcpl.lms.portfolio.loanaccount.service;


import org.vcpl.lms.portfolio.loanaccount.domain.DpdBucket;

import java.util.Objects;

public interface DpdBucketService {
    static DpdBucket getBucket(Integer dpd) {
        if(Objects.isNull(dpd) || dpd <= 0) {
            return null;
        } else if (dpd > 0 && dpd <= 30) {
            return DpdBucket.SMA_0;
        } else if (dpd >= 31 && dpd <= 60) {
            return DpdBucket.SMA_1;
        } else if (dpd >= 61 && dpd <= 90) {
            return DpdBucket.SMA_2;
        } else {
            return DpdBucket.NPA;
        }
    }

    static String getDpdBucketAsString(Integer dpd) {
        if(Objects.isNull(dpd) || dpd <= 0) return null;
        return getBucket(dpd).toString();
    }
}
