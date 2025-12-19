package com.popspot.popupplatform.controller.internal;

import com.popspot.popupplatform.service.popup.PopupGeoSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/geo")
public class GeoSyncController {

    private final PopupGeoSyncService popupGeoSyncService;

    @PostMapping("/sync-all")
    public long syncAll() {
        return popupGeoSyncService.syncAllFromMySqlToPostgres();
    }
}
