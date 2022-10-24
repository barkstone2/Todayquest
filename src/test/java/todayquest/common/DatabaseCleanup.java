package todayquest.common;

import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Disabled
@RequiredArgsConstructor
@Service
public class DatabaseCleanup implements InitializingBean {

    @Autowired
    private final EntityManager em;

    private List<String> tableNames;

    @Override
    public void afterPropertiesSet() throws Exception {
        tableNames = em.getMetamodel().getEntities().stream()
                .filter(e -> e.getJavaType().getAnnotation(Entity.class) != null)
                .filter(e -> !e.getName().equals("UserInfo"))
                .map(e -> CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, e.getName()))
                .collect(Collectors.toList());

    }

    @Transactional
    public void execute() {
        em.flush();

        for (String tableName : tableNames) {
            em.createNativeQuery("truncate table " + tableName).executeUpdate();
        }
    }

}
