package ru.i_novus.common.file.storage.api.exception;

import javax.ejb.ApplicationException;

@ApplicationException
public class EmptyFileException extends RuntimeException {
    public EmptyFileException() {
        super();
    }
}
