package ru.practicum.ewm.controller.authorized;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.dto.participationRequest.ParticipationRequestDto;
import ru.practicum.ewm.service.request.RequestService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/requests")
@RequiredArgsConstructor
public class PrivateRequestController {
    private final RequestService requestService;

    @GetMapping
    public List<ParticipationRequestDto> getUserRequests(@PathVariable Long userId) {
        return requestService.getUserRequests(userId);
    }

    @PostMapping
    public ResponseEntity<ParticipationRequestDto> addParticipationRequest(@PathVariable Long userId,
                                                                           @RequestParam Long eventId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(requestService.addParticipationRequest(userId, eventId));
    }

    @PatchMapping("/{requestId}/cancel")
    public ParticipationRequestDto cancelRequest(@PathVariable Long userId,
                                                 @PathVariable Long requestId) {
        return requestService.cancelRequest(userId, requestId);
    }
}
