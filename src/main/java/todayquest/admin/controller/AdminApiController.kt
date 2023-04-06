package todayquest.admin.controller

import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import todayquest.admin.dto.SystemSettingsRequest
import todayquest.admin.dto.SystemSettingsResponse
import todayquest.admin.service.AdminService
import todayquest.common.ResponseData

@Validated
@RequestMapping("/admin/api/v1")
@RestController
class AdminApiController (
    private val adminService: AdminService,
) {

    @GetMapping("/reward")
    fun getSystemRewardValues(): ResponseEntity<ResponseData<SystemSettingsResponse>> {
        return ResponseEntity.ok(ResponseData(adminService.getSystemSettings()))
    }

    @PutMapping("/reward")
    fun updateSystemRewardValues(
        @Valid @RequestBody settingsRequest: SystemSettingsRequest
    ) {
        adminService.updateSystemSettings(settingsRequest)
    }


    @GetMapping("/exp-table")
    fun getExpTable(): ResponseEntity<ResponseData<Map<String, Long>>> {
        return ResponseEntity.ok(ResponseData(adminService.getExpTable()))
    }


    @PutMapping("/exp-table")
    fun updateExpTable(
        @RequestBody expTable: Map<String, Long>,
    ) {
        adminService.updateExpTable(expTable)
    }

}