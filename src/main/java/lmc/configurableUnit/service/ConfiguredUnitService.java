package lmc.configurableUnit.service;


import lmc.configurableUnit.model.ConfiguredUnit;
import lmc.configurableUnit.model.ConfiguredUnitOption;
import lmc.configurableUnit.repository.ConfiguredUnitRepository;
import lmc.option.model.Option;
import lmc.option.service.OptionService;
import lmc.unit.model.CurrencyType;
import lmc.unit.model.Unit;
import lmc.unit.service.UnitService;
import lmc.web.dto.CreateNewConfiguredUnitRequest;
import lmc.web.dto.OptionSelectionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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

//    public ConfiguredUnit createConfiguredUnit(CreateNewConfiguredUnitRequest request) {
//        Unit unit = unitService.getUnitById(request.getUnitId());
//
//        List<Option> options = optionService.getOptionsByIds(request.getOptionIds());
//
//        String generatedCode = generateCode(unit, options);
//
//        return repository.findByCodeAndActiveIsTrue(generatedCode).orElseGet(()-> {
//            ConfiguredUnit newUnit = ConfiguredUnit.builder()
//                    .code(generatedCode)
//                    .unit(unit)
//                    .active(true)
//                    .currency(CurrencyType.EUR)
//                    .options(options)
//                    .build();
//
//            return repository.save(newUnit);
//        });
//
//    }

//    public ConfiguredUnit createConfiguredUnit(CreateNewConfiguredUnitRequest request) {
//        Unit unit = unitService.getUnitById(request.getUnitId());
//
//        List<OptionSelectionDTO> selections = request.getOptionIds() == null ? List.of() : request.getOptionIds();
//
//        // build map of id -> quantity (ensure at least 1)
//        Map<UUID, Integer> qtyById = selections.stream()
//                .filter(s -> s != null && s.getOptionId() != null)
//                .collect(Collectors.toMap(
//                        OptionSelectionDTO::getOptionId,
//                        s -> Math.max(1, s.getQuantity()),
//                        (a, b) -> a
//                ));
//
//        // load Option entities by ids
//        List<UUID> ids = qtyById.keySet().stream().toList();
//        List<Option> options = ids.isEmpty() ? List.of() : optionService.getOptionsByIds(ids);
//
//        // map Option -> ConfiguredUnitOption with correct quantity
//        List<ConfiguredUnitOption> configuredOptions = options.stream()
//                .map(opt -> ConfiguredUnitOption.builder()
//                        .option(opt)
//                        .quantity(qtyById.getOrDefault(opt.getId(), 1))
//                        .build())
//                .toList();
//
//        String generatedCode = generateCode(unit, options);
//
//        return repository.findByCodeAndActiveIsTrue(generatedCode).orElseGet(() -> {
//            ConfiguredUnit newUnit = ConfiguredUnit.builder()
//                    .code(generatedCode)
//                    .unit(unit)
//                    .active(true)
//                    .currency(CurrencyType.EUR)
//                    .build();
//
//            // attach join-entities and set back-reference
//            configuredOptions.forEach(newUnit::addOption);
//
//            return repository.save(newUnit);
//        });
//    }


    public ConfiguredUnit createConfiguredUnit(CreateNewConfiguredUnitRequest request) {
        Unit unit = unitService.getUnitById(request.getUnitId());

        List<OptionSelectionDTO> selections = request.getOptionIds() == null ? List.of() : request.getOptionIds();

        // build map of id -> quantity (ensure at least 1)
        Map<UUID, Integer> qtyById = selections.stream()
                .filter(s -> s != null && s.getOptionId() != null)
                .collect(Collectors.toMap(
                        OptionSelectionDTO::getOptionId,
                        s -> Math.max(1, s.getQuantity()),
                        Integer::sum
                ));

        // load Option entities by ids
        List<UUID> ids = new ArrayList<>(qtyById.keySet());
        List<Option> options = ids.isEmpty() ? List.of() : optionService.getOptionsByIds(ids);

        // map Option -> ConfiguredUnitOption with correct quantity
        List<ConfiguredUnitOption> configuredOptions = options.stream()
                .map(opt -> ConfiguredUnitOption.builder()
                        .option(opt)
                        .quantity(qtyById.getOrDefault(opt.getId(), 1))
                        .build())
                .toList();

        String generatedCode = generateCode(unit, configuredOptions);

        // try to find existing by code
        Optional<ConfiguredUnit> existingOpt = repository.findByCodeAndActiveIsTrue(generatedCode);

        if (existingOpt.isPresent()) {
            ConfiguredUnit existing = existingOpt.get();
            // compare option id -> qty maps to ensure exact match
            if (optionsMatch(existing.getOptions(), qtyById)) {
                return existing;
            }
            // fallthrough: mismatch -> create new configured unit with same code (or you may alter code generation strategy)
        }

        ConfiguredUnit newUnit = ConfiguredUnit.builder()
                .code(generatedCode)
                .unit(unit)
                .active(true)
                .currency(CurrencyType.EUR)
                .build();

        // attach join-entities and set back-reference
        configuredOptions.forEach(newUnit::addOption);

        return repository.save(newUnit);
    }

    private String generateCode(Unit unit, List<ConfiguredUnitOption> configuredOptions) {
        if (unit == null) {
            throw new IllegalArgumentException("unit must not be null");
        }

        if (configuredOptions == null || configuredOptions.isEmpty()) {
            return unit.getCode();
        }

        // produce stable representation including quantities: CODE_OPT1x2_OPT2x1 (sorted by option code)
        String optionPart = configuredOptions.stream()
                .map(co -> {
                    String optCode = co.getOption() != null && co.getOption().getCode() != null
                            ? co.getOption().getCode().trim().toUpperCase()
                            : co.getOption() != null ? co.getOption().getId().toString() : "";
                    return optCode + "x" + Math.max(1, co.getQuantity());
                })
                .filter(s -> !s.isEmpty())
                .sorted()
                .collect(Collectors.joining("_"));

        return optionPart.isEmpty() ? unit.getCode() : unit.getCode() + "_" + optionPart;
    }

    private boolean optionsMatch(List<ConfiguredUnitOption> existingOptions, Map<UUID, Integer> requestedQtyById) {
        if ((existingOptions == null || existingOptions.isEmpty()) && (requestedQtyById == null || requestedQtyById.isEmpty())) {
            return true;
        }
        if (existingOptions == null) return requestedQtyById == null || requestedQtyById.isEmpty();
        Map<UUID, Integer> existingMap = existingOptions.stream()
                .filter(Objects::nonNull)
                .filter(co -> co.getOption() != null && co.getOption().getId() != null)
                .collect(Collectors.toMap(co -> co.getOption().getId(), ConfiguredUnitOption::getQuantity, (a, b) -> a));

        return existingMap.equals(requestedQtyById);
    }


//    private String generateCode(Unit unit, List<Option> options) {
//        if (unit == null) {
//            throw new IllegalArgumentException("unit must not be null");
//        }
//
//        if (options == null || options.isEmpty()) {
//            return unit.getCode();
//        }
//
//        String optionCodes = options.stream()
//                .map(Option::getCode)
//                .filter(code -> code != null && !code.trim().isEmpty())
//                .map(String::trim)
//                .map(String::toUpperCase)
//                .sorted()
//                .collect(Collectors.joining("_"));
//
//        return optionCodes.isEmpty() ? unit.getCode() : unit.getCode() + "_" + optionCodes;
//    }





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
