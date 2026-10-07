package com.smarthr.repository;

import com.smarthr.entity.Asset;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetRepository
        extends JpaRepository<Asset, Long> {

    Optional<Asset> findByAssetTagIgnoreCase(String assetTag);

    Optional<Asset> findBySerialNumberIgnoreCase(String serialNumber);

    List<Asset> findByStatusIgnoreCase(String status);

    List<Asset> findByAssetTypeIgnoreCase(String assetType);

    List<Asset> findByEmployeeIdIgnoreCase(String employeeId);

    List<Asset> findByDepartmentIgnoreCase(String department);

    List<Asset> findByLocationIgnoreCase(String location);

    List<Asset> findByBrandIgnoreCase(String brand);

    List<Asset> findByAssetTagContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrEmployeeNameContainingIgnoreCase(
            String assetTag,
            String serialNumber,
            String employeeName
    );

    long countByStatusIgnoreCase(String status);

    long countByAssetTypeIgnoreCase(String assetType);
}