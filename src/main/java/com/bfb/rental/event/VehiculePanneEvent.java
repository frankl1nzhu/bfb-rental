package com.bfb.rental.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class VehiculePanneEvent extends ApplicationEvent {
    private final Long vehiculeId;

    public VehiculePanneEvent(Object source, Long vehiculeId) {
        super(source);
        this.vehiculeId = vehiculeId;
    }
}