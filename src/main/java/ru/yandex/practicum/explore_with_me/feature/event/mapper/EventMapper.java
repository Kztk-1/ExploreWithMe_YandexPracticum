package ru.yandex.practicum.explore_with_me.feature.event.mapper;

import org.mapstruct.*;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.explore_with_me.exception.NotFoundException;
import ru.yandex.practicum.explore_with_me.feature.category.mapper.CategoryMapper;
import ru.yandex.practicum.explore_with_me.feature.category.model.Category;
import ru.yandex.practicum.explore_with_me.feature.category.repository.CategoryRepository;
import ru.yandex.practicum.explore_with_me.feature.event.dto.*;
import ru.yandex.practicum.explore_with_me.feature.event.model.Event;
import ru.yandex.practicum.explore_with_me.feature.user.mapper.UserMapper;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(
        componentModel = "spring",
        uses = {
                UserMapper.class,         // будет искать в нём метод map(User → UserShortDto)
                CategoryMapper.class      // аналогично, для Category → CategoryDto
        },
        imports = {
                DateTimeFormatter.class,
                LocalDateTime.class
        }
)
public abstract class EventMapper {

    @Autowired
    protected CategoryRepository categoryRepository;

    public abstract EventFullDto toFullDto(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", expression = "java(fetchCategory(eventDto.getCategoryId()))")
    @Mapping(target = "state", constant = "PENDING")
    @Mapping(target = "initiator", ignore = true)
    @Mapping(target = "createdOn", ignore = true)
    @Mapping(target = "publishedOn", ignore = true)
    @Mapping(target = "confirmedRequests", ignore = true)
    @Mapping(target = "views", ignore = true)
    public abstract Event fromNewEventDto(NewEventDto eventDto);

    @BeanMapping(
//            ignoreByDefault = true,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    public abstract Event updateFromUserRequest(UpdateEventUserRequest dto,
                                                @MappingTarget Event event);

    @BeanMapping(
//            ignoreByDefault = true,
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    public abstract Event updateFromAdminRequest(UpdateEventAdminRequest dto,
                                                 @MappingTarget Event event);



    /** Главный метод конвертации в Short DTO */
    @Mapping(target = "eventDate",
            expression = "java(event.getEventDate()"
                    + ".format(DateTimeFormatter.ofPattern(\"yyyy-MM-dd HH:mm:ss\")))")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "initiator", source = "initiator")
    public abstract EventShortDto toShortDto(Event event);




    protected Category fetchCategory(Integer id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category n found (1)"));
        return category;
    }


}
