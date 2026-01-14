package lmc.configurableUnit;

import lmc.configurableUnit.repository.SimpleUnitRepository;
import lmc.configurableUnit.service.SimpleUnitService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SimpleUnitServiceUTest {

    @Mock
    private SimpleUnitRepository repository;

    @InjectMocks
    private SimpleUnitService simpleUnitService;


}
