package dailyquest.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import dailyquest.user.entity.UserInfo;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long> {

    UserInfo findByOauth2Id(String userId);
    boolean existsByNickname(String nickname);
}
