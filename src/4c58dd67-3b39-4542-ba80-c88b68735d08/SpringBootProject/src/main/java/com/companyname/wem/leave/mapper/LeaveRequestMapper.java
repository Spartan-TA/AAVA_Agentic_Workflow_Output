package com.companyname.wem.leave.mapper;

import com.companyname.wem.leave.domain.LeaveRequest;
import com.companyname.wem.leave.dto.LeaveRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface LeaveRequestMapper {
    LeaveRequestMapper INSTANCE = Mappers.getMapper(LeaveRequestMapper.class);

    @Mapping(source = "employee.id", target = "employeeId")
    LeaveRequestDTO toDto(LeaveRequest leaveRequest);

    @Mapping(source = "employeeId", target = "employee.id")
    LeaveRequest toEntity(LeaveRequestDTO leaveRequestDTO);
}
