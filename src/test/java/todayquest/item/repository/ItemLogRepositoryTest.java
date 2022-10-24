package todayquest.item.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import todayquest.config.JpaAuditingConfiguration;
import todayquest.item.entity.ItemLog;
import todayquest.item.entity.ItemLogType;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("아이템 로그 리포지토리 단위 테스트")
@Import(JpaAuditingConfiguration.class)
@DataJpaTest
public class ItemLogRepositoryTest {

    @Autowired
    ItemLogRepository itemLogRepository;


    @DisplayName("아이템 통계 조회 테스트")
    @Test
    public void testGetQuestAnalytics() throws Exception {
        //given
        Long userId = 1L;
        ItemLog il1 = ItemLog.builder()
                .rewardId(1L).userId(userId)
                .type(ItemLogType.EARN).build();
        ItemLog il2 = ItemLog.builder()
                .rewardId(2L).userId(userId)
                .type(ItemLogType.EARN).build();
        ItemLog il3 = ItemLog.builder()
                .rewardId(3L).userId(userId)
                .type(ItemLogType.USE).build();
        ItemLog il4 = ItemLog.builder()
                .rewardId(4L).userId(userId)
                .type(ItemLogType.ABANDON).build();

        itemLogRepository.save(il1);
        itemLogRepository.save(il2);
        itemLogRepository.save(il3);
        itemLogRepository.save(il4);

        //when
        Map<String, Long> itemAnalytics = itemLogRepository.getItemAnalytics(userId);

        //then
        assertThat(itemAnalytics.get(ItemLogType.EARN.name())).isEqualTo(2);
        assertThat(itemAnalytics.get(ItemLogType.USE.name())).isEqualTo(1);
        assertThat(itemAnalytics.get(ItemLogType.ABANDON.name())).isEqualTo(1);
    }
}
