package com.kanaetochi.audio_alchemists.config;

import com.kanaetochi.audio_alchemists.model.Role;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelMapperConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        Converter<Role, String> roleToStringConverter = context -> context.getSource().name();
        modelMapper.addConverter(roleToStringConverter, Role.class, String.class);
        return modelMapper;
    }
}