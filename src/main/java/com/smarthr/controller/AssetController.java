package com.smarthr.controller;

import com.smarthr.entity.Asset;
import com.smarthr.service.AssetService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assets")
@CrossOrigin(origins = "*")
public class AssetController {

    private final AssetService assetService;

    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    // ============================================================
    // CREATE ASSET
    // ============================================================

    @PostMapping
    public ResponseEntity<?> createAsset(
            @RequestBody(required = false) Asset asset) {

        try {

            if (asset == null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error", "Request body is missing",
                                "message", "Send JSON data with assetTag and assetType"
                        ));
            }

            if (asset.getAssetTag() == null ||
                    asset.getAssetTag().isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error", "assetTag is required",
                                "message", "Example: LAP-00483"
                        ));
            }

            if (asset.getAssetType() == null ||
                    asset.getAssetType().isBlank()) {

                return ResponseEntity
                        .badRequest()
                        .body(Map.of(
                                "error", "assetType is required",
                                "message", "Example: LAPTOP"
                        ));
            }

            Asset createdAsset = assetService.createAsset(asset);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(createdAsset);

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "Invalid asset data",
                            "message", e.getMessage()
                    ));

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .body(Map.of(
                            "error", "Asset creation failed",
                            "message", e.getMessage()
                    ));
        }
    }

    // ============================================================
    // GET ALL ASSETS
    // ============================================================

    @GetMapping
    public ResponseEntity<List<Asset>> getAllAssets() {

        return ResponseEntity.ok(
                assetService.getAllAssets()
        );
    }

    // ============================================================
    // GET ASSET BY ID
    // ============================================================

    @GetMapping("/{id}")
    public ResponseEntity<Asset> getAssetById(
            @PathVariable Long id) {

        return assetService.getAssetById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // ============================================================
    // GET BY ASSET TAG
    // ============================================================

    @GetMapping("/tag/{assetTag}")
    public ResponseEntity<Asset> getByAssetTag(
            @PathVariable String assetTag) {

        return assetService.getByAssetTag(assetTag)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // ============================================================
    // GET BY SERIAL NUMBER
    // ============================================================

    @GetMapping("/serial/{serialNumber}")
    public ResponseEntity<Asset> getBySerialNumber(
            @PathVariable String serialNumber) {

        return assetService.getBySerialNumber(serialNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() ->
                        ResponseEntity.notFound().build()
                );
    }

    // ============================================================
    // SEARCH
    // ============================================================

    @GetMapping("/search")
    public ResponseEntity<List<Asset>> search(
            @RequestParam String keyword) {

        return ResponseEntity.ok(
                assetService.searchAssets(keyword)
        );
    }

    // ============================================================
    // STATISTICS
    // ============================================================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {

        return ResponseEntity.ok(
                assetService.getStatistics()
        );
    }

    // ============================================================
    // WARRANTY EXPIRING
    // ============================================================

    @GetMapping("/warranty-expiring")
    public ResponseEntity<List<Asset>> getWarrantyExpiring(
            @RequestParam(defaultValue = "30") int days) {

        if (days < 0) {
            return ResponseEntity
                    .badRequest()
                    .build();
        }

        return ResponseEntity.ok(
                assetService.getWarrantyExpiring(days)
        );
    }

    // ============================================================
    // GET BY STATUS
    // ============================================================

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Asset>> getByStatus(
            @PathVariable String status) {

        return ResponseEntity.ok(
                assetService.getByStatus(status)
        );
    }

    // ============================================================
    // GET BY ASSET TYPE
    // ============================================================

    @GetMapping("/type/{assetType}")
    public ResponseEntity<List<Asset>> getByType(
            @PathVariable String assetType) {

        return ResponseEntity.ok(
                assetService.getByAssetType(assetType)
        );
    }

    // ============================================================
    // GET BY EMPLOYEE
    // ============================================================

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Asset>> getByEmployee(
            @PathVariable String employeeId) {

        return ResponseEntity.ok(
                assetService.getByEmployeeId(employeeId)
        );
    }

    // ============================================================
    // GET BY DEPARTMENT
    // ============================================================

    @GetMapping("/department/{department}")
    public ResponseEntity<List<Asset>> getByDepartment(
            @PathVariable String department) {

        return ResponseEntity.ok(
                assetService.getByDepartment(department)
        );
    }

    // ============================================================
    // GET BY LOCATION
    // ============================================================

    @GetMapping("/location/{location}")
    public ResponseEntity<List<Asset>> getByLocation(
            @PathVariable String location) {

        return ResponseEntity.ok(
                assetService.getByLocation(location)
        );
    }

    // ============================================================
    // GET BY BRAND
    // ============================================================

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Asset>> getByBrand(
            @PathVariable String brand) {

        return ResponseEntity.ok(
                assetService.getByBrand(brand)
        );
    }

    // ============================================================
    // UPDATE ASSET
    // ============================================================

    @PutMapping("/{id}")
    public ResponseEntity<?> updateAsset(
            @PathVariable Long id,
            @RequestBody Asset asset) {

        try {

            Asset updatedAsset =
                    assetService.updateAsset(id, asset);

            return ResponseEntity.ok(updatedAsset);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Asset update failed",
                            "message", e.getMessage()
                    ));
        }
    }

    // ============================================================
    // DELETE ASSET
    // ============================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAsset(
            @PathVariable Long id) {

        try {

            assetService.deleteAsset(id);

            return ResponseEntity.noContent().build();

        } catch (RuntimeException e) {

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of(
                            "error", "Asset deletion failed",
                            "message", e.getMessage()
                    ));
        }
    }

    // ============================================================
    // ASSIGN ASSET
    // ============================================================

    @PutMapping("/{id}/assign")
    public ResponseEntity<?> assignAsset(
            @PathVariable Long id,
            @RequestParam String employeeId,
            @RequestParam String employeeName,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String location) {

        try {

            Asset asset = assetService.assignAsset(
                    id,
                    employeeId,
                    employeeName,
                    department,
                    location
            );

            return ResponseEntity.ok(asset);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "Asset assignment failed",
                            "message", e.getMessage()
                    ));
        }
    }

    // ============================================================
    // RETURN ASSET
    // ============================================================

    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnAsset(
            @PathVariable Long id) {

        try {

            Asset asset = assetService.returnAsset(id);

            return ResponseEntity.ok(asset);

        } catch (RuntimeException e) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "error", "Asset return failed",
                            "message", e.getMessage()
                    ));
        }
    }

    // ============================================================
    // MARK AVAILABLE
    // ============================================================

    @PutMapping("/{id}/available")
    public ResponseEntity<?> markAvailable(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    assetService.markAvailable(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // ============================================================
    // MARK REPAIR
    // ============================================================

    @PutMapping("/{id}/repair")
    public ResponseEntity<?> markRepair(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    assetService.markRepair(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // ============================================================
    // MARK DAMAGED
    // ============================================================

    @PutMapping("/{id}/damaged")
    public ResponseEntity<?> markDamaged(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    assetService.markDamaged(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // ============================================================
    // RETIRE ASSET
    // ============================================================

    @PutMapping("/{id}/retire")
    public ResponseEntity<?> retireAsset(
            @PathVariable Long id) {

        try {

            return ResponseEntity.ok(
                    assetService.retireAsset(id)
            );

        } catch (RuntimeException e) {

            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    // ============================================================
    // BASIC STATISTICS
    // ============================================================

    @GetMapping("/count/total")
    public ResponseEntity<Long> getTotalAssets() {

        return ResponseEntity.ok(
                assetService.getTotalAssets()
        );
    }

    @GetMapping("/count/available")
    public ResponseEntity<Long> getAvailableAssets() {

        return ResponseEntity.ok(
                assetService.getAvailableAssets()
        );
    }

    @GetMapping("/count/assigned")
    public ResponseEntity<Long> getAssignedAssets() {

        return ResponseEntity.ok(
                assetService.getAssignedAssets()
        );
    }

    @GetMapping("/count/type/{assetType}")
    public ResponseEntity<Long> getAssetTypeCount(
            @PathVariable String assetType) {

        return ResponseEntity.ok(
                assetService.getAssetTypeCount(assetType)
        );
    }
}
