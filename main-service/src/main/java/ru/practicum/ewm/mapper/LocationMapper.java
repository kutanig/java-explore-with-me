package ru.practicum.ewm.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.ewm.dto.location.LocationDto;
import ru.practicum.ewm.model.location.Location;

@Component
public class LocationMapper {

    public LocationDto toDto(Location model) {
        if (model == null) {
            return null;
        }
        return LocationDto.builder()
                .lat(model.getLat())
                .lon(model.getLon())
                .build();
    }

    public Location toModel(LocationDto dto) {
        if (dto == null) {
            return null;
        }
        return Location.builder()
                .lat(dto.getLat())
                .lon(dto.getLon())
                .build();
    }
}
