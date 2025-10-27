package lmc.web.dto.mapper;

import lmc.unit.model.Unit;
import lmc.web.dto.CreateNewUnitRequest;
import org.springframework.stereotype.Component;

@Component
public class CustomMapper {

    public CreateNewUnitRequest fromUnit(Unit unit){

        return CreateNewUnitRequest.builder()
                .imageUrl(unit.getImageUrl())
                .code(unit.getCode())
                .name(unit.getName())
                .description(unit.getDescription())
                .size(unit.getSize())
                .price(unit.getPrice())
                .priceUpdatedAt(unit.getPriceUpdatedAt())
                .build();
    }

}
