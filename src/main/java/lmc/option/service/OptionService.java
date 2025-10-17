package lmc.option.service;
import lmc.option.model.Option;
import lmc.option.repository.OptionRepository;
import lmc.unit.model.CurrencyType;
import lmc.web.dto.CreateNewOptionRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OptionService {

    private final OptionRepository optionRepository;


    public OptionService(OptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    public List<Option> getOptionsByIds(List<UUID>optionIds){

        return optionRepository.findAllById(optionIds);
    }

    public List<Option> getOptionsByCode(List<String>optionCodes){
        return optionRepository.findAllByCodeIn(optionCodes);
    }

    public List<Option> getAllActiveOptions(){
        return optionRepository.findAllByActiveIs(true);
    }

    public Option getOptionByCode(String code) {
        return optionRepository.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Опция с код: %s не беше намерена в базата данни!".formatted(code)));
    }

    public Option createNewOption(CreateNewOptionRequest request) {

        Optional<Option> existingOption = optionRepository.findByCode(request.getCode());

        if (existingOption.isPresent()) {

            throw new RuntimeException("Опция с този код вече съществува!");
        }

        Option newOption = Option.builder()
                .code(request.getCode())
                .name(request.getName())
                .description(request.getDescription())
                .compatibleWith(request.getCompatibleWith())
                .price(request.getPrice())
                .active(true)
                .currency(CurrencyType.EUR)
                .build();

        optionRepository.save(newOption);

        return newOption;
    }

}
