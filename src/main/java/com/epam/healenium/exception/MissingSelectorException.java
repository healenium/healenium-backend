package com.epam.healenium.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "No information exists for given locator. Healing results can't be persisted!")
public class MissingSelectorException extends RuntimeException {
}
