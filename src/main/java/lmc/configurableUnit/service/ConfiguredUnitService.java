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

import java.math.BigDecimal;
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

    public ConfiguredUnit createConfiguredUnit(CreateNewConfiguredUnitRequest request){
        Unit unit = unitService.getUnitById(request.getUnitId());

        List<Option>options = optionService.getOptionsByIds(request.getOptionIds());

        ConfiguredUnit newUnit = ConfiguredUnit.builder()
                .unit(unit)
                .quantity(request.getQuantity())
                .options(options)
                .build();

        //TODO: трябва да се добави цена на ConfiguredUnit или да се изчислява в Configuration


        return repository.save(newUnit);
    }


    public BigDecimal unitTotalPrice(ConfiguredUnit unit){
        BigDecimal unitPrice = unit.getUnit().getPrice()
                .multiply(new BigDecimal(unit.getQuantity()));

        BigDecimal optionsPrice = unit.getOptions().stream()
                .map(Option::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(new BigDecimal(unit.getQuantity()));

        return unitPrice.add(optionsPrice);
    }
}
