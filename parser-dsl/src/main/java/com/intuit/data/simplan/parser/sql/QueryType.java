package com.intuit.data.simplan.parser.sql;

import java.io.Serializable;

/**
 * @author Abraham, Thomas - tabraham1
 * Created on 03-Mar-2022 at 3:33 PM
 */
public enum QueryType implements Serializable {
    CTAS,
    EXT,
    DROP,
    TRUNCATE,
    LOAD_DATA,
    INSERT,
    INSERT_OVERWRITE,
    USE,
    SELECT,
    UNKNOWN
}
