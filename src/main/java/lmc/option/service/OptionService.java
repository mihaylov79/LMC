package lmc.option.service;
import lmc.option.model.Option;
import lmc.option.repository.OptionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class OptionService {

    private final OptionRepository optionRepository;


    public OptionService(OptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

}
