package ru.practicum.ewm.dto.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.location.LocationDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateEventAdminRequest {
    @Size(min = 20, max = 2000)
    private String annotation;

    private Long category;

    @Size(min = 20, max = 7000)
    private String description;

    @Future
    private String eventDate;

    private LocationDto location;
    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private String stateAction;

    @Size(min = 3, max = 120)
    private String title;

    public enum StateActionAdmin {
        PUBLISH_EVENT,
        REJECT_EVENT
    }
}

