package com.ncrossley.controlfreak;

/**
 * An exception while parsing or generating Breville Control Freak FA1 files.
 *
 * @author ndjc
 * Copyright (c) 2023 Nick Crossley.  Licensed under the MIT license - see LICENSE.txt.
 */
class CFException extends Exception
{
    /**
     * Construct a new CFException.
     * @param msg the description of the exception
     */
    CFException(String msg)
    {
        super(msg);
    }
}
