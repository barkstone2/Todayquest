package dailyquest.admin.controller

import dailyquest.admin.dto.SystemSettingsRequest
import dailyquest.admin.dto.SystemSettingsResponse
import dailyquest.admin.service.AdminService
import dailyquest.common.ResponseData
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*

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
    fun getExpTable(): ResponseEntity<ResponseData<Map<Int, Long>>> {
        return ResponseEntity.ok(ResponseData(adminService.getExpTable()))
    }

    @PutMapping("/exp-table")
    fun updateExpTable(
        @RequestBody expTable: Map<Int, Long>,
    ) {
        adminService.updateExpTable(expTable)
    }
}