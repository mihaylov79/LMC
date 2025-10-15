package lmc.option.service;
import lmc.option.model.Option;
import lmc.option.repository.OptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public List<Option> getAllActiveOptions(){
        return optionRepository.findAllByActiveIs(true);
    }

}
