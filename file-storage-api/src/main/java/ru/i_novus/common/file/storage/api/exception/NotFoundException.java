package ru.i_novus.common.file.storage.api.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class NotFoundException extends RuntimeException {
    public NotFoundException() {
        super();
    }
}
