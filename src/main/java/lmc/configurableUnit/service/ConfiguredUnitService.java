package lmc.configurableUnit.service;


import lmc.configurableUnit.model.ConfiguredUnit;
import lmc.configurableUnit.repository.ConfiguredUnitRepository;
import lmc.option.model.Option;
import lmc.option.service.OptionService;
import lmc.unit.model.Unit;
import lmc.unit.service.UnitService;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConfiguredUnitService {

    private final ConfiguredUnitRepository repository;
    private final OptionService optionService;
    private final UnitService unitService;

    @Autowired
    public ConfiguredUnitService(ConfiguredUnitRepository repository, OptionService optionService, UnitService unitService) {
        this.repository = repository;
        this.optionService = optionService;
        this.unitService = unitService;
    }

    public ConfiguredUnit createConfiguredUnit(CreateNewConfiguredUnitRequest request) {
        Unit unit = unitService.getUnitById(request.getUnitId());

        List<Option> options = optionService.getOptionsByIds(request.getOptionIds());

        String code = unit.getCode();

        return repository.findByCodeAndActiveIsTrue(code).orElseGet(()-> {
            ConfiguredUnit newUnit = ConfiguredUnit.builder()
                    .code(code)
                    .unit(unit)
                    .active(true)
                    .options(options)
                    .build();

            return repository.save(newUnit);
        });

    }

    private String generateCode(Unit unit, List<Option>options ){
        String optionCodes = options.stream()
                .map(Option::getCode)
                .reduce("", (acc, optionCode) -> acc + "_" + optionCode);

        return unit.getCode() + (optionCodes.isEmpty() ? "" : "_" + optionCodes);

    }





//    public BigDecimal unitTotalPrice(ConfiguredUnit unit){
//        BigDecimal unitPrice = unit.getUnit().getPrice()
//                .multiply(new BigDecimal(unit.getQuantity()));
//
//        BigDecimal optionsPrice = unit.getOptions().stream()
//                .map(Option::getPrice)
//                .reduce(BigDecimal.ZERO, BigDecimal::add)
//                .multiply(new BigDecimal(unit.getQuantity()));
//
//        return unitPrice.add(optionsPrice);
//    }
}
