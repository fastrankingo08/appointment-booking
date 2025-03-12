package com.fastranking.appointment_booking.repository;

import com.fastranking.appointment_booking.entity.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ConfigurationRepository extends JpaRepository<Configuration , Integer> {

    @Query("SELECT c FROM Configuration c")
    List<Configuration> findAllConfigurations();

    default Map<String, String> getAllAsMap() {
        return findAllConfigurations().stream().collect(Collectors.toMap(Configuration::getKey, Configuration::getValue));
    }

    default String getValue(String Key){
        return getAllAsMap().get(Key);
    }
}
