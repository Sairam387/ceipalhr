package com.smarthr.service;

import com.smarthr.entity.Asset;
import com.smarthr.repository.AssetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AssetService {

    private final AssetRepository assetRepository;

    public AssetService(AssetRepository assetRepository) {
        this.assetRepository = assetRepository;
    }

    // ============================================================
    // GET ALL ASSETS
    // ============================================================

    public List<Asset> getAllAssets() {
        return assetRepository.findAll();
    }

    // ============================================================
    // GET ASSET BY ID
    // ============================================================

    public Optional<Asset> getAssetById(Long id) {
        return assetRepository.findById(id);
    }

    // ============================================================
    // GET BY ASSET TAG
    // ============================================================

    public Optional<Asset> getByAssetTag(String assetTag) {
        return assetRepository.findByAssetTagIgnoreCase(assetTag);
    }

    // ============================================================
    // GET BY SERIAL NUMBER
    // ============================================================

    public Optional<Asset> getBySerialNumber(String serialNumber) {
        return assetRepository.findBySerialNumberIgnoreCase(serialNumber);
    }

    // ============================================================
    // GET BY STATUS
    // ============================================================

    public List<Asset> getByStatus(String status) {
        return assetRepository.findByStatusIgnoreCase(status);
    }

    // ============================================================
    // GET BY ASSET TYPE
    // ============================================================

    public List<Asset> getByAssetType(String assetType) {
        return assetRepository.findByAssetTypeIgnoreCase(assetType);
    }

    // ============================================================
    // GET BY EMPLOYEE
    // ============================================================

    public List<Asset> getByEmployeeId(String employeeId) {
        return assetRepository.findByEmployeeIdIgnoreCase(employeeId);
    }

    // ============================================================
    // GET BY DEPARTMENT
    // ============================================================

    public List<Asset> getByDepartment(String department) {
        return assetRepository.findByDepartmentIgnoreCase(department);
    }

    // ============================================================
    // GET BY LOCATION
    // ============================================================

    public List<Asset> getByLocation(String location) {
        return assetRepository.findByLocationIgnoreCase(location);
    }

    // ============================================================
    // GET BY BRAND
    // ============================================================

    public List<Asset> getByBrand(String brand) {
        return assetRepository.findByBrandIgnoreCase(brand);
    }

    // ============================================================
    // SEARCH
    // ============================================================

    public List<Asset> searchAssets(String keyword) {

        if (keyword == null || keyword.isBlank()) {
            return assetRepository.findAll();
        }

        return assetRepository
                .findByAssetTagContainingIgnoreCaseOrSerialNumberContainingIgnoreCaseOrEmployeeNameContainingIgnoreCase(
                        keyword,
                        keyword,
                        keyword
                );
    }

    // ============================================================
    // CREATE ASSET
    // ============================================================

    public Asset createAsset(Asset asset) {

        if (asset.getStatus() == null || asset.getStatus().isBlank()) {
            asset.setStatus("AVAILABLE");
        }

        return assetRepository.save(asset);
    }

    // ============================================================
    // UPDATE ASSET
    // ============================================================

    public Asset updateAsset(Long id, Asset updatedAsset) {

        Asset existingAsset = assetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Asset not found with id: " + id
                        )
                );

        existingAsset.setAssetTag(updatedAsset.getAssetTag());
        existingAsset.setAssetType(updatedAsset.getAssetType());
        existingAsset.setBrand(updatedAsset.getBrand());
        existingAsset.setModel(updatedAsset.getModel());
        existingAsset.setSerialNumber(updatedAsset.getSerialNumber());
        existingAsset.setEmployeeId(updatedAsset.getEmployeeId());
        existingAsset.setEmployeeName(updatedAsset.getEmployeeName());
        existingAsset.setDepartment(updatedAsset.getDepartment());
        existingAsset.setLocation(updatedAsset.getLocation());
        existingAsset.setPurchaseDate(updatedAsset.getPurchaseDate());
        existingAsset.setWarrantyExpiry(updatedAsset.getWarrantyExpiry());
        existingAsset.setStatus(updatedAsset.getStatus());
        existingAsset.setAssignedDate(updatedAsset.getAssignedDate());
        existingAsset.setReturnedDate(updatedAsset.getReturnedDate());
        existingAsset.setNotes(updatedAsset.getNotes());

        return assetRepository.save(existingAsset);
    }

    // ============================================================
    // DELETE ASSET
    // ============================================================

    public void deleteAsset(Long id) {

        if (!assetRepository.existsById(id)) {
            throw new RuntimeException(
                    "Asset not found with id: " + id
            );
        }

        assetRepository.deleteById(id);
    }

    // ============================================================
    // STATISTICS
    // ============================================================

    public Map<String, Object> getStatistics() {

        long total = assetRepository.count();

        long available =
                assetRepository.countByStatusIgnoreCase("AVAILABLE");

        long assigned =
                assetRepository.countByStatusIgnoreCase("ASSIGNED");

        long repair =
                assetRepository.countByStatusIgnoreCase("REPAIR");

        long damaged =
                assetRepository.countByStatusIgnoreCase("DAMAGED");

        long retired =
                assetRepository.countByStatusIgnoreCase("RETIRED");

        Map<String, Object> statistics = new HashMap<>();

        statistics.put("total", total);
        statistics.put("totalAssets", total);
        statistics.put("available", available);
        statistics.put("availableAssets", available);
        statistics.put("assigned", assigned);
        statistics.put("assignedAssets", assigned);
        statistics.put("repair", repair);
        statistics.put("damaged", damaged);
        statistics.put("retired", retired);

        return statistics;
    }

    // ============================================================
    // WARRANTY EXPIRING
    // ============================================================

    public List<Asset> getWarrantyExpiring(int days) {

        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(days);

        return assetRepository.findAll()
                .stream()
                .filter(asset -> asset.getWarrantyExpiry() != null)
                .filter(asset ->
                        !asset.getWarrantyExpiry().isBefore(today)
                                &&
                        !asset.getWarrantyExpiry().isAfter(expiryDate)
                )
                .toList();
    }

    // ============================================================
    // ASSIGN ASSET
    // ============================================================

    public Asset assignAsset(
            Long id,
            String employeeId,
            String employeeName,
            String department,
            String location
    ) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Asset not found with id: " + id
                        )
                );

        asset.setEmployeeId(employeeId);
        asset.setEmployeeName(employeeName);
        asset.setDepartment(department);
        asset.setLocation(location);
        asset.setAssignedDate(LocalDate.now());
        asset.setReturnedDate(null);
        asset.setStatus("ASSIGNED");

        return assetRepository.save(asset);
    }

    // ============================================================
    // RETURN ASSET
    // ============================================================

    public Asset returnAsset(Long id) {

        Asset asset = assetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Asset not found with id: " + id
                        )
                );

        asset.setReturnedDate(LocalDate.now());
        asset.setEmployeeId(null);
        asset.setEmployeeName(null);
        asset.setDepartment(null);
        asset.setStatus("AVAILABLE");

        return assetRepository.save(asset);
    }

    // ============================================================
    // MARK AVAILABLE
    // ============================================================

    public Asset markAvailable(Long id) {

        Asset asset = getExistingAsset(id);

        asset.setStatus("AVAILABLE");

        return assetRepository.save(asset);
    }

    // ============================================================
    // MARK REPAIR
    // ============================================================

    public Asset markRepair(Long id) {

        Asset asset = getExistingAsset(id);

        asset.setStatus("REPAIR");

        return assetRepository.save(asset);
    }

    // ============================================================
    // MARK DAMAGED
    // ============================================================

    public Asset markDamaged(Long id) {

        Asset asset = getExistingAsset(id);

        asset.setStatus("DAMAGED");

        return assetRepository.save(asset);
    }

    // ============================================================
    // RETIRE ASSET
    // ============================================================

    public Asset retireAsset(Long id) {

        Asset asset = getExistingAsset(id);

        asset.setStatus("RETIRED");

        return assetRepository.save(asset);
    }

    // ============================================================
    // HELPER
    // ============================================================

    private Asset getExistingAsset(Long id) {

        return assetRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Asset not found with id: " + id
                        )
                );
    }

    // ============================================================
    // BASIC STATISTICS METHODS
    // ============================================================

    public long getTotalAssets() {
        return assetRepository.count();
    }

    public long getAvailableAssets() {
        return assetRepository.countByStatusIgnoreCase("AVAILABLE");
    }

    public long getAssignedAssets() {
        return assetRepository.countByStatusIgnoreCase("ASSIGNED");
    }

    public long getAssetTypeCount(String assetType) {
        return assetRepository.countByAssetTypeIgnoreCase(assetType);
    }
}