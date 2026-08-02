package io.github.rohergun.budgetmanager.user;

import io.github.rohergun.budgetmanager.user.dto.UserResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AppUserMapper{
    UserResponse toResponse(AppUser user);
}
