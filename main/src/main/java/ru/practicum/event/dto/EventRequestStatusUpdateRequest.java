package ru.practicum.event.dto;

import lombok.*;
import ru.practicum.request.model.RequestStatusToUpdate;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class EventRequestStatusUpdateRequest {

    private List<Long> requestIds;
    private RequestStatusToUpdate status;
}
