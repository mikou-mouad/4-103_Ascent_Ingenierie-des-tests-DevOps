package fr.iroise.embarq.reservation;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class IntrouvableException extends RuntimeException {

    public IntrouvableException(String message) {
        super(message);
    }
}
