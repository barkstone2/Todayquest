package todayquest.admin.service

import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockedStatic
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.data.redis.core.BoundHashOperations
import org.springframework.data.redis.core.HashOperations
import org.springframework.data.redis.core.RedisTemplate
import todayquest.admin.dto.SystemSettingsRequest
import todayquest.common.MessageUtil
import todayquest.properties.RedisKeyProperties

@ExtendWith(MockitoExtension::class)
@DisplayName("관리자 서비스 유닛 테스트")
class AdminServiceUnitTest {


    @InjectMocks
    lateinit var adminService: AdminService

    @Mock
    lateinit var redisKeyProperties: RedisKeyProperties

    @Mock
    lateinit var redisTemplate: RedisTemplate<String, String>

    private lateinit var messageUtil: MockedStatic<MessageUtil>

    @BeforeEach
    fun beforeEach() {
        messageUtil = Mockito.mockStatic(MessageUtil::class.java)
        Mockito.`when`(MessageUtil.getMessage(any())).thenReturn("")
        Mockito.`when`(MessageUtil.getMessage(any(), any())).thenReturn("")
    }

    @AfterEach
    fun afterEach() {
        messageUtil.close()
    }


    @DisplayName("시스템 설정값 조회 시")
    @Nested
    inner class GetSystemSettingsTest {

        @DisplayName("조회된 값이 알맞은 생성자 파라미터에 사용된다")
        @Test
        fun `조회된 값이 알맞은 생성자 파라미터에 사용된다`() {
            //given
            val settingsKey = "settingsKey"
            doReturn(settingsKey).`when`(redisKeyProperties).settings

            val mockOps = mock<BoundHashOperations<String, String, Int>>()
            doReturn(mockOps).`when`(redisTemplate).boundHashOps<String, Int>(settingsKey)

            val clearExpKey = "1"
            val clearGoldKey = "2"
            val maxRewardCountKey = "3"

            doReturn(clearExpKey).`when`(redisKeyProperties).questClearExp
            doReturn(clearGoldKey).`when`(redisKeyProperties).questClearGold
            doReturn(maxRewardCountKey).`when`(redisKeyProperties).maxRewardCount

            val clearExp = 1
            val clearGold = 2
            val maxRewardCount = 3

            doReturn(clearExp).`when`(mockOps)[clearExpKey]
            doReturn(clearGold).`when`(mockOps)[clearGoldKey]
            doReturn(maxRewardCount).`when`(mockOps)[maxRewardCountKey]

            //when
            val systemSettings = adminService.getSystemSettings()

            //then
            assertThat(systemSettings.questClearExp).isEqualTo(clearExp)
            assertThat(systemSettings.questClearGold).isEqualTo(clearGold)
            assertThat(systemSettings.maxRewardCount).isEqualTo(maxRewardCount)
        }

    }

    @DisplayName("시스템 설정값 업데이트 시")
    @Nested
    inner class UpdateSystemSettingsTest {

        @DisplayName("각 설정값 업데이트 로직이 호출된다")
        @Test
        fun `각 설정값 업데이트 로직이 호출된다`() {
            //given
            val mockOps = mock<BoundHashOperations<String, String, Int>>()
            doReturn(mockOps).`when`(redisTemplate).boundHashOps<String, Int>(anyOrNull())

            val mockRequest = mock<SystemSettingsRequest>()

            val clearExpKey = "1"
            val clearGoldKey = "2"
            val maxRewardCountKey = "3"

            doReturn(clearExpKey).`when`(redisKeyProperties).questClearExp
            doReturn(clearGoldKey).`when`(redisKeyProperties).questClearGold
            doReturn(maxRewardCountKey).`when`(redisKeyProperties).maxRewardCount

            val clearExp = 1
            val clearGold = 2
            val maxRewardCount = 3

            doReturn(clearExp).`when`(mockRequest).questClearExp
            doReturn(clearGold).`when`(mockRequest).questClearGold
            doReturn(maxRewardCount).`when`(mockRequest).maxRewardCount

            //when
            adminService.updateSystemSettings(mockRequest)

            //then
            verify(mockOps).put(clearExpKey, clearExp)
            verify(mockOps).put(clearGoldKey, clearGold)
            verify(mockOps).put(maxRewardCountKey, maxRewardCount)
        }

    }

    @DisplayName("경험치 테이블 조회 시")
    @Nested
    inner class GetExpTableTest {

        @DisplayName("알맞은 Redis 키를 이용한 로직이 호출된다")
        @Test
        fun `알맞은 Redis 키를 이용한 로직이 호출된다`() {
            //given
            val mockOps = mock<BoundHashOperations<String, String, Int>>()
            doReturn(mockOps).`when`(redisTemplate).boundHashOps<String, Int>(any())

            val expTableKey = "expTableKey"
            doReturn(expTableKey).`when`(redisKeyProperties).expTable

            //when
            adminService.getExpTable()

            //then
            verify(mockOps).entries()
        }

    }

    @DisplayName("경험치 테이블 업데이트 시")
    @Nested
    inner class UpdateExpTableTest {

        @DisplayName("새로운 테이블의 중간 레벨이 빈 경우 오류가 발생한다")
        @Test
        fun `새로운 테이블의 중간 레벨이 빈 경우 오류가 발생한다`() {
            //given
            val mockTable = mock<Map<Int, Long>>()
            doReturn(setOf(1, 2, 4)).`when`(mockTable).keys

            //when
            val run = { adminService.updateExpTable(mockTable) }

            //then
            assertThatThrownBy(run).isInstanceOf(IllegalArgumentException::class.java)
            verify(redisTemplate, times(0)).delete(anyOrNull<String>())
        }

        @DisplayName("마지막이 아닌 레벨의 필요 경험치가 0인 경우 오류가 발생한다")
        @Test
        fun `마지막이 아닌 레벨의 필요 경험치가 0인 경우 오류가 발생한다`() {
            //given
            val mockTable = mock<Map<Int, Long>>()
            val keys = setOf(1, 2, 3, 4)
            doReturn(keys).`when`(mockTable).keys
            doReturn(0L).`when`(mockTable)[any()]

            //when
            val run = { adminService.updateExpTable(mockTable) }

            //then
            assertThatThrownBy(run).isInstanceOf(IllegalArgumentException::class.java)
            verify(redisTemplate, times(0)).delete(anyOrNull<String>())
        }

        @DisplayName("마지막 레벨의 필요 경험치만 0인 경우 정상 호출된다")
        @Test
        fun `마지막 레벨의 필요 경험치만 0인 경우 정상 호출된다`() {
            //given
            val mockTable = mock<Map<Int, Long>>()

            val keys = setOf(1, 2, 3, 4)
            doReturn(keys).`when`(mockTable).keys

            for (key in keys) {
                if(key == keys.last()) continue
                doReturn(1L).`when`(mockTable)[key]
            }

            val expTableKey = "expTableKey"
            doReturn(expTableKey).`when`(redisKeyProperties).expTable

            val mockOps = mock<BoundHashOperations<String, Int, Long>>()
            doReturn(mockOps).`when`(redisTemplate).boundHashOps<String, Long>(expTableKey)

            //when
            adminService.updateExpTable(mockTable)

            //then
            verify(mockTable, times(keys.size-1))[any()]
            verify(redisTemplate, times(1)).delete(expTableKey)
            verify(redisTemplate, times(1)).boundHashOps<String, Long>(expTableKey)
            verify(mockOps, times(1)).putAll(mockTable)
        }

    }


}