package com.resumeiq.mapper;

import com.resumeiq.dto.UserProfileResponse;
import com.resumeiq.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * MapStruct mapper for User entities.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "roles", ignore = true) // Handled manually in service layer
    @Mapping(target = "planType", source = "subscription.planType")
    @Mapping(target = "subscriptionStatus", source = "subscription.status")
    UserProfileResponse toProfileResponse(User user);
}
