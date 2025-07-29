package ru.practicum.ewm.service.request;

import ru.practicum.ewm.dto.participationRequest.ParticipationRequestDto;

import java.util.List;

public interface RequestService {
    ParticipationRequestDto addParticipationRequest(Long userId, Long eventId);
    List<ParticipationRequestDto> getUserRequests(Long userId);
    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
    List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId);
    ParticipationRequestDto confirmRequest(Long userId, Long eventId, Long reqId);
    ParticipationRequestDto rejectRequest(Long userId, Long eventId, Long reqId);
}
